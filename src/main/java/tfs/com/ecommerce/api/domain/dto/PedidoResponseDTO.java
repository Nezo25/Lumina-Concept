package tfs.com.ecommerce.api.domain.dto;

import tfs.com.ecommerce.api.domain.enums.StatusPedido;
import tfs.com.ecommerce.api.domain.model.Pedido;
import java.math.BigDecimal;

public record PedidoResponseDTO(
        Long idPedido,
        BigDecimal valorTotal,
        StatusPedido status,
        PaymentResponseDTO pagamento
) {
    public static PedidoResponseDTO fromEntity(Pedido pedido, PaymentResponseDTO pagamento) {
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getValorTotal(),
                pedido.getStatus(),
                pagamento
        );
    }
}
