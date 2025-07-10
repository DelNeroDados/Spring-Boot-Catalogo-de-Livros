package com.BookAffix.Book_Affix.repository;

import com.BookAffix.Book_Affix.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findByNomeIgnoreCase(String nome);

    List<Autor> findByNomeContainingIgnoreCase(String nome);
    List<Autor> findByNascimentoLessThanEqualAndFalecimentoGreaterThanEqual(Integer nascimento, Integer falecimento);

}
