package tfs.com.ecommerce.api.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import tfs.com.ecommerce.api.domain.dto.CalculoFreteRequestDTO;
import tfs.com.ecommerce.api.domain.dto.FreteResponseDTO;
import tfs.com.ecommerce.api.domain.dto.ItemFreteDTO;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FreteServiceTest {

    @Mock
    private RestClient.Builder restClientBuilder;
    
    @Mock
    private RestClient restClient;

    // Instanciação manual porque o construtor do FreteService exige RestClient.Builder
    private FreteService freteService;

    @Test
    void calcularFrete_DeveRetornarOpcoesMockadas_NoCaminhoFeliz() {
        // Arrange
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        freteService = new FreteService(restClientBuilder);

        CalculoFreteRequestDTO request = new CalculoFreteRequestDTO("01001-000", List.of(new ItemFreteDTO(1L, 1)));

        // Act
        FreteResponseDTO response = freteService.calcularFrete(request);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.opcoes().size());
        assertEquals("Correios PAC", response.opcoes().get(0).nomeTransportadora());
    }
}
