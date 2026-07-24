package tfs.com.ecommerce.api.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemCheckoutDTO(
        @NotNull(message = "ID do produto é obrigatório")
        Long idProduto,
        
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade
) {}
