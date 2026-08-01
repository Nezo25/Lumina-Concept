package tfs.com.ecommerce.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tfs.com.ecommerce.api.domain.model.ModeloDispositivo;

import java.util.Optional;
import java.util.List;

@Repository
public interface ModeloDispositivoRepository extends JpaRepository<ModeloDispositivo, Long> {
    Optional<ModeloDispositivo> findByNomeIgnoreCase(String nome);
    List<ModeloDispositivo> findByMarcaId(Long marcaId);
}
