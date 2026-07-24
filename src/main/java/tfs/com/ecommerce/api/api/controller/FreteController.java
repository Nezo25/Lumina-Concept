package tfs.com.ecommerce.api.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tfs.com.ecommerce.api.domain.dto.CalculoFreteRequestDTO;
import tfs.com.ecommerce.api.domain.dto.FreteResponseDTO;
import tfs.com.ecommerce.api.domain.service.FreteService;

@RestController
@RequestMapping("/api/fretes")
@RequiredArgsConstructor
public class FreteController {

    private final FreteService freteService;

    @PostMapping("/calcular")
    public ResponseEntity<FreteResponseDTO> calcularFrete(@RequestBody @Valid CalculoFreteRequestDTO dto) {
        return ResponseEntity.ok(freteService.calcularFrete(dto));
    }
}
