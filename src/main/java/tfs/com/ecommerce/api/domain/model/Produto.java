package tfs.com.ecommerce.api.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Version
    private Long versao;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "imagem_url")
    private String imagemUrl;

    @Column(nullable = false, precision = 10, scale = 2) 
    private BigDecimal preco;

    @Column(nullable = false, name = "quantidade_estoque")
    private Integer quantidadeEstoque;

    @Column(length = 50)
    private String tamanho;
    
    @Column(length = 50)
    private String cor;

    @Column(name = "peso_kg")
    private Double pesoKg = 0.3;

    @Column(name = "altura_cm")
    private Integer alturaCm = 20;

    @Column(name = "largura_cm")
    private Integer larguraCm = 15;

    @Column(name = "comprimento_cm")
    private Integer comprimentoCm = 5;
}
