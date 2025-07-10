package com.BookAffix.Book_Affix.dto;

public class LivroDTO {
    private Long id;
    private String titulo;
    private String idioma;
    private Integer downloads;
    private String autor;

    public LivroDTO(Long id, String titulo, String idioma, Integer downloads, String autor) {
        this.id = id;
        this.titulo = titulo;
        this.idioma = idioma;
        this.downloads = downloads;
        this.autor = autor;
    }

    // getters e setters

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getIdioma() { return idioma; }
    public Integer getDownloads() { return downloads; }
    public String getAutor() { return autor; }

    public void setId(Long id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public void setDownloads(Integer downloads) { this.downloads = downloads; }
    public void setAutor(String autor) { this.autor = autor; }
}
