package tfs.com.ecommerce.api.domain.dto;

import tfs.com.ecommerce.api.domain.model.Produto;
import java.math.BigDecimal;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidadeEstoque,
        String tamanho,
        String cor,
        String sku,
        BigDecimal precoCusto,
        Integer alertaMinimo,
        String marca,
        String modeloDispositivo,
        String categoriaPeca,
        String imagemUrl
) {
    public static ProdutoResponseDTO fromEntity(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidadeEstoque(),
                produto.getTamanho(),
                produto.getCor(),
                produto.getSku(),
                produto.getPrecoCusto(),
                produto.getAlertaMinimo(),
                produto.getMarca() != null ? produto.getMarca().getNome() : null,
                produto.getModeloDispositivo() != null ? produto.getModeloDispositivo().getNome() : null,
                produto.getCategoriaPeca() != null ? produto.getCategoriaPeca().getNome() : null,
                produto.getImagemUrl()
        );
    }
}
