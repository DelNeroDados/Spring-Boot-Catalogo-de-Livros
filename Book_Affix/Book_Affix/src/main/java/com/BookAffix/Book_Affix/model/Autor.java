package com.BookAffix.Book_Affix.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
public class Autor {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    private String nome;
    @Getter
    @Setter
    private Integer nascimento;
    @Getter
    @Setter
    private Integer falecimento;

    @OneToMany(mappedBy = "autor")
    private List<Livro> livros;

}
