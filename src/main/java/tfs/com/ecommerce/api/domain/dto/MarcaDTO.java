package tfs.com.ecommerce.api.domain.dto;

import tfs.com.ecommerce.api.domain.model.Marca;

public record MarcaDTO(Long id, String nome) {
    public static MarcaDTO fromEntity(Marca marca) {
        return new MarcaDTO(marca.getId(), marca.getNome());
    }
}
