package com.BookAffix.Book_Affix.controller;

import com.BookAffix.Book_Affix.model.Autor;
import com.BookAffix.Book_Affix.service.AutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/autores")
public class AutorController {

    @Autowired
    private AutorService autorService;

    @GetMapping
    public List<Autor> listarTodos() {
        return autorService.listarTodos();
    }

    @GetMapping("/buscar")
    public List<Autor> listarPorNomeParcial(@RequestParam String nome) {
        return autorService.listarPorNomeParcial(nome);
    }

    @GetMapping("/{id}")
    public Optional<Autor> buscarPorId(@PathVariable Long id) {
        return autorService.buscarPorId(id);
    }

    @GetMapping("/vivos/{ano}")
    public List<Autor> autoresVivosEmAno(@PathVariable int ano) {
        return autorService.autoresVivosEmAno(ano);
    }

    @PostMapping
    public Autor salvar(@RequestBody Autor autor) {
        return autorService.salvar(autor);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        autorService.deletar(id);
    }
}
