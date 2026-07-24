package tfs.com.ecommerce.api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CalculoFreteRequestDTO(
        @NotBlank(message = "O CEP de destino é obrigatório")
        String cepDestino,
        
        @NotEmpty(message = "A lista de itens não pode estar vazia")
        List<ItemFreteDTO> itens
) {}
