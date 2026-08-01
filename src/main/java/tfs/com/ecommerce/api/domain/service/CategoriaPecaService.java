package tfs.com.ecommerce.api.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tfs.com.ecommerce.api.domain.model.CategoriaPeca;
import tfs.com.ecommerce.api.domain.repository.CategoriaPecaRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaPecaService {

    @Autowired
    private CategoriaPecaRepository categoriaRepository;

    public List<CategoriaPeca> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Optional<CategoriaPeca> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    @Transactional
    public CategoriaPeca salvar(CategoriaPeca categoria) {
        return categoriaRepository.save(categoria);
    }

    public CategoriaPeca buscarOuCriarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return null;
        return categoriaRepository.findByNomeIgnoreCase(nome.trim())
                .orElseGet(() -> {
                    CategoriaPeca novaCategoria = new CategoriaPeca();
                    novaCategoria.setNome(nome.trim());
                    return categoriaRepository.save(novaCategoria);
                });
    }
}
