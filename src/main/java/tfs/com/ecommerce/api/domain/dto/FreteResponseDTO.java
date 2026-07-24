package tfs.com.ecommerce.api.domain.dto;

import java.util.List;

public record FreteResponseDTO(
        List<OpcaoFreteDTO> opcoes
) {}
