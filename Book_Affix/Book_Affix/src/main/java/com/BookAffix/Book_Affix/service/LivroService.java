package com.BookAffix.Book_Affix.service;

import com.BookAffix.Book_Affix.model.Autor;
import com.BookAffix.Book_Affix.model.Livro;
import com.BookAffix.Book_Affix.repository.AutorRepository;
import com.BookAffix.Book_Affix.repository.LivroRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LivroService {

    @Value("${gutendex.api.base-url}")
    private String gutendexApiBaseUrl;

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public LivroService(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    public Optional<Livro> buscarESalvarPorTitulo(String titulo) {
        Optional<Livro> existente = livroRepository.findByTituloIgnoreCase(titulo);
        if (existente.isPresent()) return existente;

        Livro livro = buscarNaApiGutendex(titulo);
        if (livro == null) return Optional.empty();

        Autor autor = livro.getAutor();
        if (autor != null) {
            Optional<Autor> autorExistente = autorRepository.findByNomeIgnoreCase(autor.getNome());
            if (autorExistente.isPresent()) {
                livro.setAutor(autorExistente.get());
            } else {
                autorRepository.save(autor);
            }
        }
        Livro salvo = livroRepository.save(livro);
        return Optional.of(salvo);
    }

    public List<Livro> listarLivros() {
        return livroRepository.findAll();
    }

    public List<Livro> filtrarLivrosPorIdioma(String idioma) {
        return livroRepository.findByIdiomaIgnoreCase(idioma);
    }

    public List<Livro> listarPorTituloParcial(String parcial) {
        return livroRepository.findByTituloContainingIgnoreCase(parcial);
    }

    public List<Livro> sugerirPorTitulo(String parcial) {
        return listarPorTituloParcial(parcial);
    }

    public Livro salvar(Livro livro) {
        if (livro.getAutor() != null) {
            Optional<Autor> autorExistente = autorRepository.findByNomeIgnoreCase(livro.getAutor().getNome());
            if (autorExistente.isPresent()) {
                livro.setAutor(autorExistente.get());
            } else {
                Autor autorSalvo = autorRepository.save(livro.getAutor());
                livro.setAutor(autorSalvo);
            }
        }
        return livroRepository.save(livro);
    }

    public void deletar(Long id) {
        livroRepository.deleteById(id);
    }

    private Livro buscarNaApiGutendex(String titulo) {
        String url = gutendexApiBaseUrl + "?search=" + titulo;
        JsonNode resposta = restTemplate.getForObject(url, JsonNode.class);
        JsonNode resultados = resposta.get("results");
        if (resultados != null && resultados.size() > 0) {
            JsonNode primeiro = resultados.get(0);
            Livro livro = new Livro();
            livro.setTitulo(primeiro.get("title").asText());
            JsonNode idiomas = primeiro.get("languages");
            if (idiomas != null && idiomas.size() > 0) {
                livro.setIdioma(idiomas.get(0).asText());
            }
            livro.setDownloads(primeiro.path("download_count").asInt(0));

            // Capa
            String capa = "";
            JsonNode formats = primeiro.get("formats");
            if (formats != null && formats.has("image/jpeg")) {
                capa = formats.get("image/jpeg").asText();
            }
            livro.setCapa(capa);

            // Resumo (description/summary/summaries) - ALTERADO PARA PEGAR TUDO
            String resumo = "";
            if (primeiro.has("description")) {
                JsonNode descNode = primeiro.get("description");
                if (descNode.isTextual()) {
                    resumo = descNode.asText();
                } else if (descNode.has("value")) {
                    resumo = descNode.get("value").asText();
                }
            }
            if (resumo.isEmpty() && primeiro.has("summary")) {
                JsonNode sumNode = primeiro.get("summary");
                if (sumNode.isTextual()) {
                    resumo = sumNode.asText();
                } else if (sumNode.has("value")) {
                    resumo = sumNode.get("value").asText();
                }
            }
            if (resumo.isEmpty() && primeiro.has("summaries")) {
                JsonNode sumsNode = primeiro.get("summaries");
                if (sumsNode.isArray() && sumsNode.size() > 0) {
                    JsonNode firstSum = sumsNode.get(0);
                    if (firstSum.isTextual()) {
                        resumo = firstSum.asText();
                    } else if (firstSum.has("value")) {
                        resumo = firstSum.get("value").asText();
                    }
                }
            }
            livro.setResumo(resumo);

            // Link para ler online
            String link = "";
            if (formats != null && formats.has("text/html")) {
                link = formats.get("text/html").asText();
            }
            livro.setLink(link);

            // Assuntos
            List<String> assuntos = new ArrayList<>();
            JsonNode subjectsNode = primeiro.get("subjects");
            if (subjectsNode != null && subjectsNode.isArray()) {
                for (JsonNode s : subjectsNode) {
                    assuntos.add(s.asText());
                }
            }
            livro.setSubjects(assuntos);

            // Autor - primeiro autor da lista
            JsonNode autores = primeiro.get("authors");
            if (autores != null && autores.isArray() && autores.size() > 0) {
                JsonNode autorJson = autores.get(0);
                Autor autor = new Autor();
                autor.setNome(autorJson.get("name").asText());
                livro.setAutor(autor);
            } else {
                livro.setAutor(null);
            }

            return livro;
        }
        return null;
    }

    private String normalizar(String input) {
        if (input == null) return "";
        String n = Normalizer.normalize(input, Normalizer.Form.NFD);
        return n.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").toLowerCase();
    }

    public List<Livro> buscarLivrosNaApiPorTituloParcial(String parcial) {
        String url = gutendexApiBaseUrl + "?search=" + parcial;
        try {
            JsonNode resposta = restTemplate.getForObject(url, JsonNode.class);
            JsonNode resultados = resposta.get("results");
            List<Livro> lista = new ArrayList<>();
            if (resultados != null && resultados.size() > 0) {
                for (JsonNode item : resultados) {
                    Livro livro = new Livro();
                    livro.setTitulo(item.get("title").asText());
                    JsonNode idiomas = item.get("languages");
                    if (idiomas != null && idiomas.size() > 0) {
                        livro.setIdioma(idiomas.get(0).asText());
                    }
                    if (item.has("download_count")) {
                        livro.setDownloads(item.get("download_count").asInt());
                    }
                    JsonNode autores = item.get("authors");
                    if (autores != null && autores.size() > 0) {
                        JsonNode autorNode = autores.get(0);
                        Autor autor = new Autor();
                        autor.setNome(autorNode.get("name").asText());
                        if (autorNode.has("birth_year")) autor.setNascimento(autorNode.get("birth_year").asInt());
                        if (autorNode.has("death_year")) autor.setFalecimento(autorNode.get("death_year").asInt());
                        livro.setAutor(autor);
                    }
                    lista.add(livro);
                }
            }
            return lista;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public Optional<Livro> buscarNoBancoOuApiPorTitulo(String titulo) {
        Optional<Livro> existente = livroRepository.findByTituloIgnoreCase(titulo);
        if (existente.isPresent()) return existente;
        Livro daApi = buscarNaApiGutendex(titulo);
        return daApi != null ? Optional.of(daApi) : Optional.empty();
    }

    public boolean existeNoBanco(Livro livro) {
        return livroRepository.findByTituloIgnoreCase(livro.getTitulo()).isPresent();
    }
}
