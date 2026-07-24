package tfs.com.ecommerce.api.domain.dto;

public record PaymentResponseDTO(
        String qrCodeBase64,
        String qrCodeCopiaECola,
        String statusPagamentoMP,
        String statusDetalhe
) {}
