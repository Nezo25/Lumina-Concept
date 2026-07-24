package tfs.com.ecommerce.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Slf4j
@Service
public class MercadoPagoWebhookService {

    private final RestClient restClient;
    private final String accessToken;
    private final PedidoService pedidoService;
    private final BlingService blingService;

    public MercadoPagoWebhookService(
            RestClient.Builder restClientBuilder,
            @Value("${mercadopago.api-url}") String apiUrl,
            @Value("${mercadopago.access-token}") String accessToken,
            PedidoService pedidoService,
            BlingService blingService) {
        this.restClient = restClientBuilder.baseUrl(apiUrl).build();
        this.accessToken = accessToken;
        this.pedidoService = pedidoService;
        this.blingService = blingService;
    }

    @Async
    public void processarNotificacao(Map<String, Object> payload) {
        try {
            log.info("Iniciando processamento assíncrono do Webhook...");
            
            String idPagamento = extrairIdPagamentoDefensivo(payload);
            
            if (idPagamento == null) {
                log.warn("ID de pagamento não encontrado no payload do webhook: {}", payload);
                return;
            }

            log.info("Consultando status real do pagamento {} na API do Mercado Pago", idPagamento);
            
            // Consulta de segurança na API do MP
            Map response = restClient.get()
                    .uri("/v1/payments/" + idPagamento)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            if (response != null && "approved".equals(response.get("status"))) {
                String externalReference = (String) response.get("external_reference");
                
                if (externalReference != null && !externalReference.isEmpty()) {
                    Long idPedido = Long.valueOf(externalReference);
                    log.info("Pagamento aprovado confirmado para o Pedido ID: {}", idPedido);
                    
                    // Chama o serviço de pedido que garantirá a idempotência do status
                    tfs.com.ecommerce.api.domain.model.Pedido pedidoSalvo = pedidoService.atualizarStatusParaPago(idPedido);
                    
                    if (pedidoSalvo != null) {
                        // Gatilho de Faturamento Automático Bling
                        boolean integrado = blingService.enviarPedidoVenda(pedidoSalvo);
                        
                        // Atualiza a flag de rastreabilidade
                        pedidoService.atualizarStatusBling(idPedido, integrado);
                    }
                } else {
                    log.warn("Pagamento {} está aprovado, mas não possui external_reference vinculado", idPagamento);
                }
            } else {
                log.info("Pagamento {} não está no status 'approved'. Status atual: {}", idPagamento, response != null ? response.get("status") : "null");
            }

        } catch (Exception e) {
            // Tratamento robusto para não quebrar a thread silenciosamente e permitir debug
            log.error("Falha crítica no processamento assíncrono do Webhook: {}", e.getMessage(), e);
        }
    }

    private String extrairIdPagamentoDefensivo(Map<String, Object> payload) {
        // Formato Webhook padrão: {"data": {"id": "123456"}}
        if (payload.containsKey("data")) {
            Object dataObj = payload.get("data");
            if (dataObj instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                if (dataMap.containsKey("id")) {
                    return String.valueOf(dataMap.get("id"));
                }
            }
        }
        
        // Formato IPN (ou action dependente)
        if (payload.containsKey("resource")) {
            String resource = String.valueOf(payload.get("resource"));
            // O resource vem como /collections/notifications/123456 ou /v1/payments/123456
            String[] parts = resource.split("/");
            return parts[parts.length - 1];
        }
        
        // Formato direto de ID na raiz (para alguns eventos)
        if (payload.containsKey("id")) {
            return String.valueOf(payload.get("id"));
        }
        
        return null;
    }
}
