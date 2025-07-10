package com.BookAffix.Book_Affix.controller;

import com.BookAffix.Book_Affix.model.Livro;
import com.BookAffix.Book_Affix.repository.LivroRepository;
import com.BookAffix.Book_Affix.service.LivroService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final LivroService livroService;
    private final LivroRepository livroRepository;

    public LivroController(LivroService livroService, LivroRepository livroRepository) {
        this.livroService = livroService;
        this.livroRepository = livroRepository;
    }

    @GetMapping
    public List<Livro> listarTodos() {
        return livroService.listarLivros();
    }

    @GetMapping("/buscar")
    public List<Livro> buscarPorTitulo(@RequestParam String titulo) {
        return livroService.listarPorTituloParcial(titulo);
    }

    @PostMapping("/importar-da-api")
    public ResponseEntity<?> importarLivroDaApi(@RequestBody Map<String, String> payload) {
        String titulo = payload.get("titulo");
        if (titulo == null || titulo.isBlank()) {
            return ResponseEntity.badRequest().body("Título não informado.");
        }
        Optional<Livro> livroOpt = livroService.buscarNoBancoOuApiPorTitulo(titulo);
        if (livroOpt.isPresent()) {
            Livro salvo = livroService.salvar(livroOpt.get());
            return ResponseEntity.ok(salvo);
        } else {
            return ResponseEntity.status(404).body("Livro não encontrado na API.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLivro(@PathVariable Long id) {
        if (!livroRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        livroRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
