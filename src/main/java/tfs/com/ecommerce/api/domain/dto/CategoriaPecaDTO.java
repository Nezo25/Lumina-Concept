package tfs.com.ecommerce.api.domain.dto;

import tfs.com.ecommerce.api.domain.model.CategoriaPeca;

public record CategoriaPecaDTO(Long id, String nome) {
    public static CategoriaPecaDTO fromEntity(CategoriaPeca categoria) {
        return new CategoriaPecaDTO(categoria.getId(), categoria.getNome());
    }
}
