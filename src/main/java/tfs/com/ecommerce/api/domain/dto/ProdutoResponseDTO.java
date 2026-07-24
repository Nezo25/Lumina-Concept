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
                produto.getImagemUrl()
        );
    }
}
