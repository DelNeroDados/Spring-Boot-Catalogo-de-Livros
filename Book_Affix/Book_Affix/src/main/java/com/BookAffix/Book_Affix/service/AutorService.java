package com.BookAffix.Book_Affix.service;

import com.BookAffix.Book_Affix.model.Autor;
import com.BookAffix.Book_Affix.repository.AutorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AutorService {

    private final AutorRepository autorRepository;

    public AutorService(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    // Buscar todos os autores
    public List<Autor> listarTodos() {
        return autorRepository.findAll();
    }

    // Buscar por nome (parcial, ignore case)
    public List<Autor> listarPorNomeParcial(String nome) {
        return autorRepository.findByNomeContainingIgnoreCase(nome);
    }

    // Buscar por ID (único)
    public Optional<Autor> buscarPorId(Long id) {
        return autorRepository.findById(id);
    }

    // Buscar autores vivos em determinado ano
    public List<Autor> autoresVivosEmAno(int ano) {
        return autorRepository.findByNascimentoLessThanEqualAndFalecimentoGreaterThanEqual(ano, ano);
    }

    // Salvar autor
    public Autor salvar(Autor autor) {
        return autorRepository.save(autor);
    }

    // Deletar autor por ID
    public void deletar(Long id) {
        autorRepository.deleteById(id);
    }
}
