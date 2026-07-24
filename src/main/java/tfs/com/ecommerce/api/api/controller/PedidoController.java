package tfs.com.ecommerce.api.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tfs.com.ecommerce.api.domain.dto.CheckoutRequestDTO;
import tfs.com.ecommerce.api.domain.dto.PedidoResponseDTO;
import tfs.com.ecommerce.api.domain.service.PedidoService;

import java.net.URI;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/checkout")
    public ResponseEntity<PedidoResponseDTO> realizarCheckout(@RequestBody @Valid CheckoutRequestDTO dto) {
        PedidoResponseDTO response = pedidoService.realizarCheckout(dto);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.idPedido())
                .toUri();
                
        return ResponseEntity.created(uri).body(response);
    }
}
