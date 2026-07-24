package tfs.com.ecommerce.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tfs.com.ecommerce.api.domain.dto.BlingPedidoVendaRequestDTO;
import tfs.com.ecommerce.api.domain.model.ItemPedido;
import tfs.com.ecommerce.api.domain.model.Pedido;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class BlingService {

    private final RestClient restClient;
    private final String accessToken;

    public BlingService(
            RestClient.Builder restClientBuilder,
            @Value("${bling.api-url:https://www.bling.com.br/Api/v3}") String apiUrl,
            @Value("${bling.access-token:}") String accessToken) {
        this.restClient = restClientBuilder.baseUrl(apiUrl).build();
        this.accessToken = accessToken;
    }

    /**
     * Método auxiliar isolado para gerenciar o OAuth2 do Bling V3.
     * No futuro, este método pode buscar o refresh_token no banco e renovar se necessário.
     */
    private String obterTokenAtivo() {
        // TODO: Implementar lógica de Refresh Token OAuth2.
        return this.accessToken;
    }

    @org.springframework.scheduling.annotation.Async
    public void enviarPedidoVendaAsync(Pedido pedido) {
        enviarPedidoVenda(pedido);
    }

    public boolean enviarPedidoVenda(Pedido pedido) {
        try {
            log.info("Iniciando integração com o Bling para o pedido {}", pedido.getId());

            String token = obterTokenAtivo();
            if (token == null || token.isBlank()) {
                log.warn("Integração com Bling ignorada: Token não configurado.");
                return false;
            }

            BlingPedidoVendaRequestDTO payload = construirPayload(pedido);

            restClient.post()
                    .uri("/pedidos/vendas")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Pedido {} integrado com sucesso no Bling!", pedido.getId());
            return true;

        } catch (RestClientException ex) {
            // Falha silenciosa para não afetar o webhook do Mercado Pago
            log.error("Falha ao comunicar com o Bling para o pedido {}: {}", pedido.getId(), ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("Erro inesperado na integração com Bling para o pedido {}: {}", pedido.getId(), ex.getMessage());
            return false;
        }
    }

    private BlingPedidoVendaRequestDTO construirPayload(Pedido pedido) {
        BlingPedidoVendaRequestDTO.Contato contato = new BlingPedidoVendaRequestDTO.Contato(
                pedido.getCliente().getNome(),
                pedido.getCliente().getCpf()
        );

        List<BlingPedidoVendaRequestDTO.Item> itens = pedido.getItens().stream()
                .map(i -> new BlingPedidoVendaRequestDTO.Item(
                        i.getProduto().getNome(),
                        i.getQuantidade(),
                        i.getPrecoUnitario()
                )).toList();

        // Forma de pagamento genérica para o PIX no Bling (17 é um id comum no Bling, mas idealmente seria dinâmico)
        BlingPedidoVendaRequestDTO.Parcela parcela = new BlingPedidoVendaRequestDTO.Parcela(
                new BlingPedidoVendaRequestDTO.FormaPagamento(17),
                pedido.getValorTotal()
        );

        return new BlingPedidoVendaRequestDTO(
                LocalDate.now().toString(),
                contato,
                itens,
                List.of(parcela)
        );
    }
}
