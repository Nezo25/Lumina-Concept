package tfs.com.ecommerce.api.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemFreteDTO(
        @NotNull
        Long idProduto,
        
        @NotNull
        @Positive
        Integer quantidade
) {}
