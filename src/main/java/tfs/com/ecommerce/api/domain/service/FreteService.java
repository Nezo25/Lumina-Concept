package tfs.com.ecommerce.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tfs.com.ecommerce.api.domain.dto.CalculoFreteRequestDTO;
import tfs.com.ecommerce.api.domain.dto.FreteResponseDTO;
import tfs.com.ecommerce.api.domain.dto.OpcaoFreteDTO;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class FreteService {

    private final RestClient restClient;

    public FreteService(RestClient.Builder restClientBuilder) {
        // Exemplo genérico apontando para Kangu/MelhorEnvio
        this.restClient = restClientBuilder.baseUrl("https://sandbox.melhorenvio.com.br/api/v2/me").build();
    }

    public FreteResponseDTO calcularFrete(CalculoFreteRequestDTO request) {
        try {
            log.info("Iniciando cálculo de frete externo para o CEP: {}", request.cepDestino());
            
            // Aqui montariamos o payload complexo iterando sobre os itens e somando as dimensões e pesos.
            // O RestClient faria o POST para a API externa.
            /*
            Map response = restClient.post()
                    .uri("/shipment/calculate")
                    .header("Authorization", "Bearer TOKEN_FRETE")
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            */

            // Simulando o parse de sucesso para fins de MVP demonstrativo
            return gerarOpcoesMockadas();

        } catch (Exception e) {
            log.error("Falha ao comunicar com a API de Frete. Acionando Fallback. Erro: {}", e.getMessage());
            // Circuit Breaker local: Retorna opções salvadoras em caso de falha total do provedor.
            return gerarOpcoesFallback();
        }
    }

    private FreteResponseDTO gerarOpcoesMockadas() {
        return new FreteResponseDTO(List.of(
                new OpcaoFreteDTO("Correios PAC", BigDecimal.valueOf(25.50), 7),
                new OpcaoFreteDTO("Correios SEDEX", BigDecimal.valueOf(45.90), 2),
                new OpcaoFreteDTO("Jadlog Package", BigDecimal.valueOf(22.00), 5)
        ));
    }

    private FreteResponseDTO gerarOpcoesFallback() {
        return new FreteResponseDTO(List.of(
                new OpcaoFreteDTO("Frete Padrão Fixo (Fallback)", BigDecimal.valueOf(30.00), 7)
        ));
    }
}
