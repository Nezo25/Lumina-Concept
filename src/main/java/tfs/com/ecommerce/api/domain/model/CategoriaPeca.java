package tfs.com.ecommerce.api.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias_peca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CategoriaPeca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;
}
