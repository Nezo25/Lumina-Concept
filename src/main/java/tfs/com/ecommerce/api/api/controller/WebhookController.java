package tfs.com.ecommerce.api.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tfs.com.ecommerce.api.domain.service.MercadoPagoWebhookService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final MercadoPagoWebhookService webhookService;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> receberNotificacao(@RequestBody Map<String, Object> payload) {
        log.info("Webhook recebido do Mercado Pago");
        
        // Dispara o processamento assíncrono para liberar a thread do Spring imediatamente
        webhookService.processarNotificacao(payload);
        
        // Retorna HTTP 200 rápido conforme exigência de resiliência
        return ResponseEntity.ok().build();
    }
}
