package tfs.com.ecommerce.api.domain.service;

import tfs.com.ecommerce.api.domain.dto.ProdutoRequestDTO;
import tfs.com.ecommerce.api.domain.dto.ProdutoResponseDTO;
import tfs.com.ecommerce.api.domain.model.Produto;
import tfs.com.ecommerce.api.domain.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import tfs.com.ecommerce.api.domain.model.Marca;
import tfs.com.ecommerce.api.domain.model.ModeloDispositivo;
import tfs.com.ecommerce.api.domain.model.CategoriaPeca;
import tfs.com.ecommerce.api.domain.model.MovimentacaoEstoque;
import tfs.com.ecommerce.api.domain.repository.MovimentacaoEstoqueRepository;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MarcaService marcaService;
    private final ModeloDispositivoService modeloDispositivoService;
    private final CategoriaPecaService categoriaPecaService;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    private void preencherRelacionamentos(Produto produto, ProdutoRequestDTO dto) {
        if (dto.marca() != null && !dto.marca().isBlank()) {
            Marca marca = marcaService.buscarOuCriarPorNome(dto.marca());
            produto.setMarca(marca);
            
            if (dto.modeloDispositivo() != null && !dto.modeloDispositivo().isBlank()) {
                produto.setModeloDispositivo(modeloDispositivoService.buscarOuCriarPorNomeEMarca(dto.modeloDispositivo(), marca));
            } else {
                produto.setModeloDispositivo(null);
            }
        } else {
            produto.setMarca(null);
            produto.setModeloDispositivo(null);
        }

        if (dto.categoriaPeca() != null && !dto.categoriaPeca().isBlank()) {
            produto.setCategoriaPeca(categoriaPecaService.buscarOuCriarPorNome(dto.categoriaPeca()));
        } else {
            produto.setCategoriaPeca(null);
        }
    }

    @Transactional
    public ProdutoResponseDTO cadastrarProduto(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setQuantidadeEstoque(dto.quantidadeEstoque());
        produto.setTamanho(dto.tamanho());
        produto.setCor(dto.cor());
        
        produto.setSku(dto.sku());
        produto.setPrecoCusto(dto.precoCusto());
        produto.setAlertaMinimo(dto.alertaMinimo() != null ? dto.alertaMinimo() : 3);
        
        preencherRelacionamentos(produto, dto);

        Produto produtoSalvo = produtoRepository.save(produto);
        
        if (produtoSalvo.getQuantidadeEstoque() != null && produtoSalvo.getQuantidadeEstoque() > 0) {
            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setProduto(produtoSalvo);
            mov.setTipo(MovimentacaoEstoque.TipoMovimentacao.ENTRADA);
            mov.setQuantidade(produtoSalvo.getQuantidadeEstoque());
            mov.setMotivo("Estoque Inicial");
            mov.setDataHora(LocalDateTime.now());
            movimentacaoEstoqueRepository.save(mov);
        }

        return ProdutoResponseDTO.fromEntity(produtoSalvo);
    }

    public Page<ProdutoResponseDTO> listarTodos(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return produtoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(ProdutoResponseDTO::fromEntity);
        }
        return produtoRepository.findAll(pageable)
                .map(ProdutoResponseDTO::fromEntity);
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        return ProdutoResponseDTO.fromEntity(produto);
    }

    @Transactional
    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoRequestDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        Integer estoqueAnterior = produto.getQuantidadeEstoque() != null ? produto.getQuantidadeEstoque() : 0;
        Integer estoqueNovo = dto.quantidadeEstoque() != null ? dto.quantidadeEstoque() : 0;

        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setQuantidadeEstoque(dto.quantidadeEstoque());
        produto.setTamanho(dto.tamanho());
        produto.setCor(dto.cor());
        
        produto.setSku(dto.sku());
        produto.setPrecoCusto(dto.precoCusto());
        produto.setAlertaMinimo(dto.alertaMinimo() != null ? dto.alertaMinimo() : 3);
        
        preencherRelacionamentos(produto, dto);

        Produto produtoAtualizado = produtoRepository.save(produto);

        if (!estoqueAnterior.equals(estoqueNovo)) {
            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setProduto(produtoAtualizado);
            mov.setDataHora(LocalDateTime.now());
            
            if (estoqueNovo > estoqueAnterior) {
                mov.setTipo(MovimentacaoEstoque.TipoMovimentacao.ENTRADA);
                mov.setQuantidade(estoqueNovo - estoqueAnterior);
                mov.setMotivo("Ajuste Manual / Atualização de Produto");
            } else {
                mov.setTipo(MovimentacaoEstoque.TipoMovimentacao.SAIDA);
                mov.setQuantidade(estoqueAnterior - estoqueNovo);
                mov.setMotivo("Ajuste Manual / Atualização de Produto");
            }
            movimentacaoEstoqueRepository.save(mov);
        }

        return ProdutoResponseDTO.fromEntity(produtoAtualizado);
    }
}
