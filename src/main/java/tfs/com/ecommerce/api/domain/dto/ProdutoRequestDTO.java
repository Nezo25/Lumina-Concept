package tfs.com.ecommerce.api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record ProdutoRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        
        String descricao,
        
        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        BigDecimal preco,
        
        @NotNull(message = "A quantidade em estoque é obrigatória")
        @PositiveOrZero(message = "A quantidade não pode ser negativa")
        Integer quantidadeEstoque,
        
        String tamanho,
        
        String cor,

        String sku,

        @PositiveOrZero(message = "O preço de custo não pode ser negativo")
        BigDecimal precoCusto,

        @PositiveOrZero(message = "O alerta mínimo não pode ser negativo")
        Integer alertaMinimo,

        String marca,
        String modeloDispositivo,
        String categoriaPeca,

        @jakarta.validation.constraints.Size(max = 2000, message = "A URL da imagem não pode exceder 2000 caracteres")
        String imagemUrl
) {
}
