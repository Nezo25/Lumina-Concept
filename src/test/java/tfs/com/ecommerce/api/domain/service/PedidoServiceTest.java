package tfs.com.ecommerce.api.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tfs.com.ecommerce.api.domain.enums.StatusPedido;
import tfs.com.ecommerce.api.domain.model.Pedido;
import tfs.com.ecommerce.api.domain.repository.PedidoRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    // Dependências mockadas para o PedidoService não quebrar (mesmo que não usadas neste teste específico)
    @Mock
    private ProdutoService produtoService;
    @Mock
    private FreteService freteService;
    @Mock
    private MercadoPagoService mercadoPagoService;
    @Mock
    private BlingService blingService;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        pedido.setId(1L);
    }

    @Test
    void atualizarStatusParaPago_DeveIgnorarSeJaEstiverPago() {
        // Arrange
        pedido.setStatus(StatusPedido.PAGO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act
        pedidoService.atualizarStatusParaPago(1L);

        // Assert
        assertEquals(StatusPedido.PAGO, pedido.getStatus());
        // Garante que o repositório NÃO foi chamado para salvar, provando a idempotência
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(blingService, never()).enviarPedidoVendaAsync(any(Pedido.class));
    }

    @Test
    void atualizarStatusParaPago_DeveIgnorarSeEstiverCancelado() {
        // Arrange
        pedido.setStatus(StatusPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act
        pedidoService.atualizarStatusParaPago(1L);

        // Assert
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        // Garante que o repositório NÃO foi chamado para salvar
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(blingService, never()).enviarPedidoVendaAsync(any(Pedido.class));
    }

    @Test
    void atualizarStatusParaPago_DeveAtualizarSeEstiverAguardandoPagamento() {
        // Arrange
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act
        pedidoService.atualizarStatusParaPago(1L);

        // Assert
        assertEquals(StatusPedido.PAGO, pedido.getStatus());
        verify(pedidoRepository, times(1)).save(pedido);
        verify(blingService, times(1)).enviarPedidoVendaAsync(pedido);
    }
}
