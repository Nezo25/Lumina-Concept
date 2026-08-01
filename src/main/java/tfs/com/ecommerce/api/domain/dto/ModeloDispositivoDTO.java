package tfs.com.ecommerce.api.domain.dto;

import tfs.com.ecommerce.api.domain.model.ModeloDispositivo;

public record ModeloDispositivoDTO(Long id, String nome, Long marcaId, String marcaNome) {
    public static ModeloDispositivoDTO fromEntity(ModeloDispositivo modelo) {
        return new ModeloDispositivoDTO(
                modelo.getId(),
                modelo.getNome(),
                modelo.getMarca() != null ? modelo.getMarca().getId() : null,
                modelo.getMarca() != null ? modelo.getMarca().getNome() : null
        );
    }
}
