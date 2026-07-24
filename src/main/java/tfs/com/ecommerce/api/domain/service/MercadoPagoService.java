package tfs.com.ecommerce.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tfs.com.ecommerce.api.domain.dto.CheckoutRequestDTO;
import tfs.com.ecommerce.api.domain.dto.PaymentResponseDTO;
import tfs.com.ecommerce.api.domain.model.Pedido;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MercadoPagoService {

    private final RestClient restClient;
    private final String accessToken;
    private final String webhookUrl;

    public MercadoPagoService(
            RestClient.Builder restClientBuilder,
            @Value("${mercadopago.api-url}") String apiUrl,
            @Value("${mercadopago.access-token}") String accessToken,
            @Value("${mercadopago.webhook-url}") String webhookUrl) {
        this.restClient = restClientBuilder.baseUrl(apiUrl).build();
        this.accessToken = accessToken;
        this.webhookUrl = webhookUrl;
    }

    public PaymentResponseDTO processarPagamento(Pedido pedido, CheckoutRequestDTO dto) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("transaction_amount", pedido.getValorTotal());
            payload.put("description", "Pedido #" + pedido.getId() + " - E-commerce MVP");
            
            String forma = dto.formaPagamento() != null ? dto.formaPagamento().toUpperCase() : "PIX";
            
            if ("PIX".equals(forma)) {
                payload.put("payment_method_id", "pix");
            } else {
                payload.put("payment_method_id", dto.metodoPagamentoId() != null ? dto.metodoPagamentoId() : "visa");
                payload.put("token", dto.tokenCartao());
                payload.put("installments", dto.parcelas() != null ? dto.parcelas() : 1);
            }
            
            // Referências cruciais para o Webhook encontrar o pedido na volta
            payload.put("external_reference", pedido.getId().toString());
            if (webhookUrl != null && !webhookUrl.isBlank()) {
                payload.put("notification_url", webhookUrl);
            }

            Map<String, Object> payer = new HashMap<>();
            payer.put("email", pedido.getCliente().getEmail());
            payer.put("first_name", pedido.getCliente().getNome());
            
            // Tratamento simplificado de nome (para fins do MVP)
            String[] nomes = pedido.getCliente().getNome().split(" ");
            if (nomes.length > 1) {
                payer.put("first_name", nomes[0]);
                payer.put("last_name", nomes[nomes.length - 1]);
            }
            
            Map<String, String> identification = new HashMap<>();
            identification.put("type", "CPF");
            identification.put("number", pedido.getCliente().getCpf().replaceAll("\\D", ""));
            payer.put("identification", identification);

            payload.put("payer", payer);

            // Chamada externa com RestClient do Spring
            var request = restClient.post()
                    .uri("/v1/payments")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("X-Idempotency-Key", UUID.randomUUID().toString());
                    
            // Requisito MP Antifraude (device_id deve ir no Header X-Meli-Session-Id e não no JSON)
            if (dto.deviceId() != null && !dto.deviceId().isBlank()) {
                request.header("X-Meli-Session-Id", dto.deviceId());
            }

            Map response = request
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return null;
            }

            String status = (String) response.get("status");
            String statusDetail = (String) response.get("status_detail");

            if ("PIX".equals(forma)) {
                if (response.containsKey("point_of_interaction")) {
                    Map<String, Object> poi = (Map<String, Object>) response.get("point_of_interaction");
                    if (poi.containsKey("transaction_data")) {
                        Map<String, Object> transactionData = (Map<String, Object>) poi.get("transaction_data");
                        String qrCodeBase64 = (String) transactionData.get("qr_code_base64");
                        String qrCode = (String) transactionData.get("qr_code");
                        return new PaymentResponseDTO(qrCodeBase64, qrCode, status, statusDetail);
                    }
                }
                log.warn("Mercado Pago retornou sucesso, mas sem os dados do PIX para o pedido {}", pedido.getId());
            } 
            
            // Retorna o status seja PIX (pending) ou CARTAO (approved/rejected/in_process)
            return new PaymentResponseDTO(null, null, status, statusDetail);

        } catch (RestClientException ex) {
            log.error("Falha ao comunicar com o Mercado Pago para o pedido {}: {}", pedido.getId(), ex.getMessage());
            return null;
        } catch (Exception ex) {
            log.error("Erro inesperado ao gerar pagamento para o pedido {}: {}", pedido.getId(), ex.getMessage());
            return null;
        }
    }
}
