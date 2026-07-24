package tfs.com.ecommerce.api.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.br.CPF;
import java.util.List;

public record CheckoutRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,
        
        @NotBlank(message = "O CPF é obrigatório")
        String cpf,
        
        @NotBlank(message = "O telefone é obrigatório")
        String telefone,
        
        @NotBlank(message = "O CEP é obrigatório")
        String cep,
        
        @NotBlank(message = "O logradouro é obrigatório")
        String logradouro,
        
        @NotBlank(message = "O número é obrigatório")
        String numero,
        
        String complemento,
        
        @NotBlank(message = "O bairro é obrigatório")
        String bairro,
        
        @NotBlank(message = "A cidade é obrigatória")
        String cidade,
        
        @NotBlank(message = "O estado é obrigatório")
        String estado,
        
        @NotBlank(message = "O serviço de frete deve ser escolhido")
        String servicoFreteEscolhido,
        
        String formaPagamento,
        String tokenCartao,
        Integer parcelas,
        String metodoPagamentoId,
        String deviceId,
        
        @NotEmpty(message = "O carrinho não pode estar vazio")
        @Valid
        List<ItemCheckoutDTO> itens
) {}
