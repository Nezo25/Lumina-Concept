package tfs.com.ecommerce.api.domain.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tfs.com.ecommerce.api.domain.enums.StatusPedido;
import tfs.com.ecommerce.api.domain.model.Pedido;
import tfs.com.ecommerce.api.domain.repository.PedidoRepository;
import tfs.com.ecommerce.api.domain.service.BlingService;

import java.util.List;

@Slf4j
@Component
public class BlingRetryScheduler {

    private final PedidoRepository pedidoRepository;
    private final BlingService blingService;

    public BlingRetryScheduler(PedidoRepository pedidoRepository, BlingService blingService) {
        this.pedidoRepository = pedidoRepository;
        this.blingService = blingService;
    }

    /**
     * Roda a cada 30 minutos procurando pedidos pagos que ainda não foram integrados no ERP.
     */
    @Scheduled(cron = "0 0/30 * * * *")
    public void reprocessarFaturamentosPendentes() {
        log.info("Iniciando rotina automática de reprocessamento do Bling...");

        List<Pedido> pedidosPendentes = pedidoRepository.findByStatusAndIntegradoBling(StatusPedido.PAGO, false);

        if (pedidosPendentes.isEmpty()) {
            log.info("Nenhum pedido pendente de faturamento encontrado.");
            return;
        }

        log.info("Encontrados {} pedidos pendentes de envio para o ERP.", pedidosPendentes.size());

        for (Pedido pedido : pedidosPendentes) {
            try {
                // Tenta enviar o pedido para o Bling
                boolean integrado = blingService.enviarPedidoVenda(pedido);
                
                if (integrado) {
                    pedido.setIntegradoBling(true);
                    pedidoRepository.save(pedido);
                    log.info("Pedido {} reprocessado e faturado com sucesso via Scheduler.", pedido.getId());
                } else {
                    log.warn("Falha no reprocessamento do pedido {} para o Bling. Será tentado na próxima rodada.", pedido.getId());
                }
            } catch (Exception e) {
                // Captura individual garante que uma falha isolada (ex: dados mal formatados do cliente) 
                // não derrube o processo para os próximos pedidos da lista.
                log.error("Exceção inesperada ao reprocessar pedido {} no Bling: {}", pedido.getId(), e.getMessage());
            }
        }
        
        log.info("Rotina automática do Bling finalizada.");
    }
}
