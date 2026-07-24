package tfs.com.ecommerce.api.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tfs.com.ecommerce.api.domain.dto.CheckoutRequestDTO;
import tfs.com.ecommerce.api.domain.dto.ItemCheckoutDTO;
import tfs.com.ecommerce.api.domain.dto.PedidoResponseDTO;
import tfs.com.ecommerce.api.domain.dto.PaymentResponseDTO;
import tfs.com.ecommerce.api.domain.enums.FormaPagamento;
import tfs.com.ecommerce.api.domain.enums.StatusPedido;
import tfs.com.ecommerce.api.domain.exception.EstoqueInsuficienteException;
import tfs.com.ecommerce.api.domain.model.Cliente;
import tfs.com.ecommerce.api.domain.model.ItemPedido;
import tfs.com.ecommerce.api.domain.model.Pedido;
import tfs.com.ecommerce.api.domain.model.Produto;
import tfs.com.ecommerce.api.domain.repository.ClienteRepository;
import tfs.com.ecommerce.api.domain.repository.PedidoRepository;
import tfs.com.ecommerce.api.domain.repository.ProdutoRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final MercadoPagoService mercadoPagoService;
    private final FreteService freteService;
    private final BlingService blingService;

    // Updated constructor is handled by @RequiredArgsConstructor
    
    public PedidoResponseDTO realizarCheckout(CheckoutRequestDTO dto) {
        Pedido pedidoSalvo = salvarPedidoNoBanco(dto);
        
        PaymentResponseDTO paymentResponse = mercadoPagoService.processarPagamento(pedidoSalvo, dto);
        
        if (paymentResponse != null) {
            if ("approved".equalsIgnoreCase(paymentResponse.statusPagamentoMP())) {
                atualizarStatusParaPago(pedidoSalvo.getId());
                // Trigger Bling asincronamente
                blingService.enviarPedidoVendaAsync(pedidoSalvo);
                pedidoSalvo.setStatus(StatusPedido.PAGO);
            } else if ("rejected".equalsIgnoreCase(paymentResponse.statusPagamentoMP())) {
                atualizarStatusParaCancelado(pedidoSalvo.getId());
                pedidoSalvo.setStatus(StatusPedido.CANCELADO);
            }
            // in_process or pending keep AGUARDANDO_PAGAMENTO
        }
        
        return PedidoResponseDTO.fromEntity(pedidoSalvo, paymentResponse);
    }

    @Transactional
    protected Pedido salvarPedidoNoBanco(CheckoutRequestDTO dto) {
        Cliente cliente = buscarOuCriarCliente(dto);
        
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        
        pedido.setCepEntrega(dto.cep());
        pedido.setLogradouroEntrega(dto.logradouro());
        pedido.setNumeroEntrega(dto.numero());
        pedido.setComplementoEntrega(dto.complemento());
        pedido.setBairroEntrega(dto.bairro());
        pedido.setCidadeEntrega(dto.cidade());
        pedido.setEstadoEntrega(dto.estado());
        
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        
        if (dto.formaPagamento() != null && !dto.formaPagamento().isBlank()) {
            try {
                pedido.setFormaPagamento(tfs.com.ecommerce.api.domain.enums.FormaPagamento.valueOf(dto.formaPagamento().toUpperCase()));
            } catch (IllegalArgumentException e) {
                pedido.setFormaPagamento(tfs.com.ecommerce.api.domain.enums.FormaPagamento.PIX);
            }
        } else {
            pedido.setFormaPagamento(tfs.com.ecommerce.api.domain.enums.FormaPagamento.PIX);
        }
        
        // Calculo dinâmico e seguro de frete (Anti-Spoofing)
        java.util.List<tfs.com.ecommerce.api.domain.dto.ItemFreteDTO> itensFrete = dto.itens().stream()
                .map(i -> new tfs.com.ecommerce.api.domain.dto.ItemFreteDTO(i.idProduto(), i.quantidade()))
                .toList();
                
        tfs.com.ecommerce.api.domain.dto.FreteResponseDTO fretesCalculados = freteService.calcularFrete(
                new tfs.com.ecommerce.api.domain.dto.CalculoFreteRequestDTO(dto.cep(), itensFrete));

        tfs.com.ecommerce.api.domain.dto.OpcaoFreteDTO freteEscolhido = fretesCalculados.opcoes().stream()
                .filter(op -> op.nomeTransportadora().equalsIgnoreCase(dto.servicoFreteEscolhido()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço de frete inválido ou indisponível: " + dto.servicoFreteEscolhido()));

        pedido.setValorFrete(freteEscolhido.valor());
        
        BigDecimal valorTotalItens = BigDecimal.ZERO;

        for (ItemCheckoutDTO itemDto : dto.itens()) {
            Produto produto = produtoRepository.findById(itemDto.idProduto())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado: " + itemDto.idProduto()));
            
            if (produto.getQuantidadeEstoque() < itemDto.quantidade()) {
                throw new EstoqueInsuficienteException(
                    String.format("Estoque insuficiente para o produto '%s'. Requisitado: %d, Disponível: %d", 
                        produto.getNome(), itemDto.quantidade(), produto.getQuantidadeEstoque())
                );
            }
            
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - itemDto.quantidade());
            produtoRepository.save(produto); // Optimistic Locking atuará aqui
            
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProduto(produto);
            itemPedido.setQuantidade(itemDto.quantidade());
            itemPedido.setPrecoUnitario(produto.getPreco());
            
            pedido.adicionarItem(itemPedido);
            
            BigDecimal valorItem = itemPedido.getPrecoUnitario().multiply(BigDecimal.valueOf(itemPedido.getQuantidade()));
            valorTotalItens = valorTotalItens.add(valorItem);
        }
        
        pedido.setValorTotal(valorTotalItens.add(pedido.getValorFrete()));
        
        return pedidoRepository.save(pedido);
    }

    private Cliente buscarOuCriarCliente(CheckoutRequestDTO dto) {
        return clienteRepository.findByCpf(dto.cpf())
                .orElseGet(() -> {
                    Cliente novoCliente = new Cliente();
                    novoCliente.setNome(dto.nome());
                    novoCliente.setEmail(dto.email());
                    novoCliente.setCpf(dto.cpf());
                    novoCliente.setTelefone(dto.telefone());
                    novoCliente.setCep(dto.cep());
                    novoCliente.setLogradouro(dto.logradouro());
                    novoCliente.setNumero(dto.numero());
                    novoCliente.setComplemento(dto.complemento());
                    novoCliente.setBairro(dto.bairro());
                    novoCliente.setCidade(dto.cidade());
                    novoCliente.setEstado(dto.estado());
                    return clienteRepository.save(novoCliente);
                });
    }

    @Transactional
    public Pedido atualizarStatusParaPago(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado: " + idPedido));
                
        // Força o carregamento dos itens (Evita LazyInitializationException fora da transação)
        pedido.getItens().size();
                
        // Regra Crítica de Idempotência: Só atualiza se o status estiver pendente
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            // Se já estiver Pago, Cancelado, etc., ignoramos (Webhook duplicado)
            return null;
        }
        
        pedido.setStatus(StatusPedido.PAGO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido atualizarStatusParaCancelado(Long idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado: " + idPedido));
                
        // Estornar estoque poderia entrar aqui, mas para MVP vamos focar apenas no status
        pedido.setStatus(StatusPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void atualizarStatusBling(Long idPedido, boolean integrado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado: " + idPedido));
        pedido.setIntegradoBling(integrado);
        pedidoRepository.save(pedido);
    }
}
