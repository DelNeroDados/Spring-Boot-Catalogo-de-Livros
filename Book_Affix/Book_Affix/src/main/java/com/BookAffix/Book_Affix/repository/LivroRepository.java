package com.BookAffix.Book_Affix.repository;

import com.BookAffix.Book_Affix.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    Optional<Livro> findByTituloIgnoreCase(String titulo);
    List<Livro> findByIdiomaIgnoreCase(String idioma);

    // Busca parcial por título (case insensitive)
    List<Livro> findByTituloContainingIgnoreCase(String parcial);
}
