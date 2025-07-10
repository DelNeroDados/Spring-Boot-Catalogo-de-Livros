package com.BookAffix.Book_Affix.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String idioma;
    private Integer downloads;

    private String capa;     // imagem
    private String resumo;   // resumo
    private String link;     // abrir livro online

    @ElementCollection
    private List<String> subjects; // assuntos

    @ManyToOne(fetch = FetchType.EAGER)
    private Autor autor;

    // GETTERS E SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }

    public Integer getDownloads() { return downloads; }
    public void setDownloads(Integer downloads) { this.downloads = downloads; }

    public String getCapa() { return capa; }
    public void setCapa(String capa) { this.capa = capa; }

    public String getResumo() { return resumo; }
    public void setResumo(String resumo) { this.resumo = resumo; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }

    public Autor getAutor() { return autor; }
    public void setAutor(Autor autor) { this.autor = autor; }

    @Override
    public String toString() {
        return "Livro{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", idioma='" + idioma + '\'' +
                ", downloads=" + downloads +
                ", capa='" + capa + '\'' +
                ", resumo='" + resumo + '\'' +
                ", link='" + link + '\'' +
                ", subjects=" + subjects +
                ", autor=" + (autor != null ? autor.getNome() : "N/A") +
                '}';
    }
}
