package tfs.com.ecommerce.api.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record BlingPedidoVendaRequestDTO(
        String data,
        Contato contato,
        List<Item> itens,
        List<Parcela> parcelas
) {
    public record Contato(
            String nome,
            String numeroDocumento
    ) {}

    public record Item(
            String descricao,
            Integer quantidade,
            BigDecimal valor
    ) {}

    public record Parcela(
            FormaPagamento formaPagamento,
            BigDecimal valor
    ) {}

    public record FormaPagamento(
            Integer id
    ) {}
}
