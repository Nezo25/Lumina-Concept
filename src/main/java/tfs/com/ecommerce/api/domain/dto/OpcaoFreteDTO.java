package tfs.com.ecommerce.api.domain.dto;

import java.math.BigDecimal;

public record OpcaoFreteDTO(
        String nomeTransportadora,
        BigDecimal valor,
        Integer prazoDias
) {}
