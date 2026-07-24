package tfs.com.ecommerce.api.domain.service;

import tfs.com.ecommerce.api.domain.dto.ProdutoRequestDTO;
import tfs.com.ecommerce.api.domain.dto.ProdutoResponseDTO;
import tfs.com.ecommerce.api.domain.model.Produto;
import tfs.com.ecommerce.api.domain.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoResponseDTO cadastrarProduto(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setQuantidadeEstoque(dto.quantidadeEstoque());
        produto.setTamanho(dto.tamanho());
        produto.setCor(dto.cor());

        Produto produtoSalvo = produtoRepository.save(produto);
        return ProdutoResponseDTO.fromEntity(produtoSalvo);
    }

    public Page<ProdutoResponseDTO> listarTodos(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return produtoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(ProdutoResponseDTO::fromEntity);
        }
        return produtoRepository.findAll(pageable)
                .map(ProdutoResponseDTO::fromEntity);
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        return ProdutoResponseDTO.fromEntity(produto);
    }

    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoRequestDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setQuantidadeEstoque(dto.quantidadeEstoque());
        produto.setTamanho(dto.tamanho());
        produto.setCor(dto.cor());

        Produto produtoAtualizado = produtoRepository.save(produto);
        return ProdutoResponseDTO.fromEntity(produtoAtualizado);
    }
}
