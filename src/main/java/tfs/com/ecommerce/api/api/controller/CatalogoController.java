package tfs.com.ecommerce.api.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tfs.com.ecommerce.api.domain.dto.CategoriaPecaDTO;
import tfs.com.ecommerce.api.domain.dto.MarcaDTO;
import tfs.com.ecommerce.api.domain.dto.ModeloDispositivoDTO;
import tfs.com.ecommerce.api.domain.service.CategoriaPecaService;
import tfs.com.ecommerce.api.domain.service.MarcaService;
import tfs.com.ecommerce.api.domain.service.ModeloDispositivoService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    @Autowired
    private MarcaService marcaService;

    @Autowired
    private ModeloDispositivoService modeloDispositivoService;

    @Autowired
    private CategoriaPecaService categoriaPecaService;

    @GetMapping("/marcas")
    public ResponseEntity<List<MarcaDTO>> listarMarcas() {
        List<MarcaDTO> marcas = marcaService.listarTodas().stream()
                .map(MarcaDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(marcas);
    }

    @GetMapping("/modelos")
    public ResponseEntity<List<ModeloDispositivoDTO>> listarModelos(@RequestParam(required = false) Long marcaId) {
        List<ModeloDispositivoDTO> modelos;
        if (marcaId != null) {
            modelos = modeloDispositivoService.listarPorMarca(marcaId).stream()
                    .map(ModeloDispositivoDTO::fromEntity)
                    .collect(Collectors.toList());
        } else {
            modelos = modeloDispositivoService.listarTodos().stream()
                    .map(ModeloDispositivoDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(modelos);
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaPecaDTO>> listarCategorias() {
        List<CategoriaPecaDTO> categorias = categoriaPecaService.listarTodas().stream()
                .map(CategoriaPecaDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }
}
