package tfs.com.ecommerce.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tfs.com.ecommerce.api.domain.model.CategoriaPeca;

import java.util.Optional;

@Repository
public interface CategoriaPecaRepository extends JpaRepository<CategoriaPeca, Long> {
    Optional<CategoriaPeca> findByNomeIgnoreCase(String nome);
}
