package tfs.com.ecommerce.api.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tfs.com.ecommerce.api.domain.model.ModeloDispositivo;
import tfs.com.ecommerce.api.domain.model.Marca;
import tfs.com.ecommerce.api.domain.repository.ModeloDispositivoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ModeloDispositivoService {

    @Autowired
    private ModeloDispositivoRepository modeloRepository;

    public List<ModeloDispositivo> listarTodos() {
        return modeloRepository.findAll();
    }
    
    public List<ModeloDispositivo> listarPorMarca(Long marcaId) {
        return modeloRepository.findByMarcaId(marcaId);
    }

    public Optional<ModeloDispositivo> buscarPorId(Long id) {
        return modeloRepository.findById(id);
    }

    @Transactional
    public ModeloDispositivo salvar(ModeloDispositivo modelo) {
        return modeloRepository.save(modelo);
    }

    public ModeloDispositivo buscarOuCriarPorNomeEMarca(String nome, Marca marca) {
        if (nome == null || nome.trim().isEmpty()) return null;
        return modeloRepository.findByNomeIgnoreCase(nome.trim())
                .orElseGet(() -> {
                    ModeloDispositivo novoModelo = new ModeloDispositivo();
                    novoModelo.setNome(nome.trim());
                    novoModelo.setMarca(marca); // Pode ser nulo se quisermos permitir modelos sem marca
                    return modeloRepository.save(novoModelo);
                });
    }
}
