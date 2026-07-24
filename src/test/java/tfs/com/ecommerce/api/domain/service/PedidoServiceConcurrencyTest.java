package tfs.com.ecommerce.api.domain.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import tfs.com.ecommerce.api.domain.dto.CheckoutRequestDTO;
import tfs.com.ecommerce.api.domain.dto.FreteResponseDTO;
import tfs.com.ecommerce.api.domain.dto.ItemCheckoutDTO;
import tfs.com.ecommerce.api.domain.dto.OpcaoFreteDTO;
import tfs.com.ecommerce.api.domain.model.Produto;
import tfs.com.ecommerce.api.domain.repository.ClienteRepository;
import tfs.com.ecommerce.api.domain.repository.PedidoRepository;
import tfs.com.ecommerce.api.domain.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class PedidoServiceConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceConcurrencyTest.class);

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    // Isola o teste das chamadas externas
    @MockitoBean
    private MercadoPagoService mercadoPagoService;

    @MockitoBean
    private FreteService freteService;

    @MockitoBean
    private BlingService blingService;

    private Long produtoId;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        clienteRepository.deleteAll();

        // Cadastra um produto com APENAS 1 unidade de estoque
        Produto produto = new Produto();
        produto.setNome("Smartphone de Teste");
        produto.setPreco(BigDecimal.valueOf(1000.00));
        produto.setQuantidadeEstoque(1);
        produtoRepository.save(produto);
        
        produtoId = produto.getId();

        // Configura o Mock do Frete para sempre retornar sucesso para que o fluxo ande
        when(freteService.calcularFrete(any())).thenReturn(
                new FreteResponseDTO(List.of(
                        new OpcaoFreteDTO("SEDEX", BigDecimal.TEN, 2)
                ))
        );
    }

    @AfterEach
    void tearDown() {
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    void realizarCheckout_DeveBloquearVendaDupla_EmCondicaoDeCorrida() throws InterruptedException {
        int numeroDeThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numeroDeThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numeroDeThreads);

        AtomicInteger sucessoCount = new AtomicInteger(0);
        AtomicInteger falhaCount = new AtomicInteger(0);

        for (int i = 0; i < numeroDeThreads; i++) {
            final String cpfUnico = "111222333" + String.format("%02d", i); // Cliente diferente para cada thread
            executor.submit(() -> {
                try {
                    // Prepara o DTO da requisição
                    CheckoutRequestDTO dto = new CheckoutRequestDTO(
                            "Comprador " + cpfUnico,
                            "email@teste.com",
                            cpfUnico,
                            "11999999999",
                            "01001-000",
                            "Rua",
                            "123",
                            "Complemento",
                            "Centro",
                            "São Paulo",
                            "SP",
                            "SEDEX",
                            "PIX",
                            null,
                            1,
                            null,
                            null,
                            List.of(new ItemCheckoutDTO(produtoId, 1))
                    );

                    // Espera o sinal de largada para todas as threads chamarem ao mesmo tempo
                    startLatch.await();
                    
                    // Dispara a venda
                    pedidoService.realizarCheckout(dto);
                    
                    sucessoCount.incrementAndGet();
                } catch (Exception e) {
                    // Qualquer exceção (OptimisticLockingFailureException, EstoqueInsuficienteException) é considerada falha contida
                    log.error("Venda bloqueada pelo sistema: " + e.getMessage());
                    falhaCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Dá a largada! Todas as threads vão invocar realizarCheckout no mesmo milissegundo.
        startLatch.countDown();
        // Aguarda todas terminarem
        endLatch.await();
        executor.shutdown();

        // Assertivas de Defesa de Estoque
        Produto produtoFinal = produtoRepository.findById(produtoId).orElseThrow();
        
        log.info("Vendas com sucesso: {}", sucessoCount.get());
        log.info("Vendas bloqueadas (falhas): {}", falhaCount.get());
        log.info("Estoque Final: {}", produtoFinal.getQuantidadeEstoque());

        // EXATAMENTE 1 venda deve ter sucesso.
        assertEquals(1, sucessoCount.get());
        // AS OUTRAS 19 devem ter falhado protegendo o estoque.
        assertEquals(numeroDeThreads - 1, falhaCount.get());
        // O estoque deve ser zerado (1 - 1 = 0), sem números negativos.
        assertEquals(0, produtoFinal.getQuantidadeEstoque());
    }
}
