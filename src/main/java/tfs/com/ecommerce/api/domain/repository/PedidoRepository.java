package tfs.com.ecommerce.api.domain.repository;

import tfs.com.ecommerce.api.domain.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tfs.com.ecommerce.api.domain.enums.StatusPedido;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    @Query("SELECT p FROM Pedido p JOIN FETCH p.itens WHERE p.cliente.id = :clienteId")
    List<Pedido> findPedidosComItensByClienteId(Long clienteId);

    List<Pedido> findByStatusAndIntegradoBling(StatusPedido status, Boolean integradoBling);
}
