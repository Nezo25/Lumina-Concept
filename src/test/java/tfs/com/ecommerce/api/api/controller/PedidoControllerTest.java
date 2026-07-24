package tfs.com.ecommerce.api.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tfs.com.ecommerce.api.domain.dto.CheckoutRequestDTO;
import tfs.com.ecommerce.api.domain.dto.ItemCheckoutDTO;
import tfs.com.ecommerce.api.domain.dto.PedidoResponseDTO;
import tfs.com.ecommerce.api.domain.enums.StatusPedido;
import tfs.com.ecommerce.api.domain.service.PedidoService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    void realizarCheckout_DeveRetornar201Created_CaminhoFeliz() throws Exception {
        // Arrange
        CheckoutRequestDTO request = new CheckoutRequestDTO(
                "Teste", "teste@teste.com", "11122233344", "11999999999",
                "01001000", "Rua", "123", "Complemento", "Bairro", "SP", "SP", "SEDEX", "PIX",
                null, 1, null, null, List.of(new ItemCheckoutDTO(1L, 1))
        );

        PedidoResponseDTO mockResponse = new PedidoResponseDTO(1L, BigDecimal.valueOf(100.00), StatusPedido.AGUARDANDO_PAGAMENTO, null);

        when(pedidoService.realizarCheckout(any(CheckoutRequestDTO.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/pedidos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPedido").value(1L))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"));
    }
}
