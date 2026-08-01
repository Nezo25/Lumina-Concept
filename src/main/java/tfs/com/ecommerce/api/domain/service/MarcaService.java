package tfs.com.ecommerce.api.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tfs.com.ecommerce.api.domain.model.Marca;
import tfs.com.ecommerce.api.domain.repository.MarcaRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MarcaService {

    @Autowired
    private MarcaRepository marcaRepository;

    public List<Marca> listarTodas() {
        return marcaRepository.findAll();
    }

    public Optional<Marca> buscarPorId(Long id) {
        return marcaRepository.findById(id);
    }

    @Transactional
    public Marca salvar(Marca marca) {
        return marcaRepository.save(marca);
    }

    public Marca buscarOuCriarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return null;
        return marcaRepository.findByNomeIgnoreCase(nome.trim())
                .orElseGet(() -> {
                    Marca novaMarca = new Marca();
                    novaMarca.setNome(nome.trim());
                    return marcaRepository.save(novaMarca);
                });
    }
}
