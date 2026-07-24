package tfs.com.ecommerce.api.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import tfs.com.ecommerce.api.domain.enums.StatusPedido;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, length = 9)
    private String cepEntrega;
    
    @Column(nullable = false, length = 200)
    private String logradouroEntrega;
    
    @Column(nullable = false, length = 20)
    private String numeroEntrega;
    
    @Column(length = 100)
    private String complementoEntrega;
    
    @Column(nullable = false, length = 100)
    private String bairroEntrega;
    
    @Column(nullable = false, length = 100)
    private String cidadeEntrega;
    
    @Column(nullable = false, length = 2)
    private String estadoEntrega;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFrete;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Column(name = "transacao_mercado_pago_id", length = 100)
    private String transacaoMercadoPagoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private tfs.com.ecommerce.api.domain.enums.FormaPagamento formaPagamento = tfs.com.ecommerce.api.domain.enums.FormaPagamento.PIX;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "integrado_bling", nullable = false)
    private Boolean integradoBling = false;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
    }

    public void adicionarItem(ItemPedido item) {
        this.itens.add(item);
        item.setPedido(this);
    }
}
