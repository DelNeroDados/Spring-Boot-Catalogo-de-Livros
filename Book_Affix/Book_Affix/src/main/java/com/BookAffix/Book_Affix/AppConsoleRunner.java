package com.BookAffix.Book_Affix;

import com.BookAffix.Book_Affix.model.Livro;
import com.BookAffix.Book_Affix.service.LivroService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class AppConsoleRunner implements CommandLineRunner {

    private final LivroService livroService;

    public AppConsoleRunner(LivroService livroService) {
        this.livroService = livroService;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        String menu = """
        \n=========================================
                  B O O K A F F I X
        =========================================
        1) Buscar livro por título
        2) Listar todos os livros
        3) Filtrar livros por idioma
        4) Buscar livros por título parcial
        5) Buscar livros NA API por título parcial
        0) Sair
        -----------------------------------------
        Escolha uma opção: """;

        boolean sair = false;
        while (!sair) {
            System.out.print(menu);
            String escolha = scanner.nextLine();
            switch (escolha) {
                case "1" -> {
                    System.out.print("\nDigite o título do livro: ");
                    String titulo = scanner.nextLine();
                    System.out.println("Buscando...");
                    Optional<Livro> livroOpt = livroService.buscarNoBancoOuApiPorTitulo(titulo);
                    if (livroOpt.isPresent()) {
                        Livro livro = livroOpt.get();
                        System.out.println("[✓] Livro encontrado:");
                        System.out.println(livro);

                        // Só salva se não existe ainda no banco!
                        if (!livroService.existeNoBanco(livro)) {
                            System.out.print("Deseja salvar/cadastrar esse livro? (s/n): ");
                            String resp = scanner.nextLine().trim().toLowerCase();
                            if (resp.equals("s")) {
                                livroService.salvar(livro);
                                System.out.println("[✓] Livro salvo com sucesso!");
                            } else {
                                System.out.println("Livro não foi salvo.");
                            }
                        }
                    } else {
                        System.out.println("[✗] Livro não encontrado ou erro ao buscar.");
                    }
                }
                case "2" -> {
                    List<Livro> livros = livroService.listarLivros();
                    System.out.println("\n--- Todos os Livros Cadastrados ---");
                    if (livros.isEmpty()) {
                        System.out.println("Nenhum livro cadastrado.");
                    } else {
                        livros.forEach(System.out::println);
                    }
                }
                case "3" -> {
                    System.out.print("\nDigite o idioma (ex: 'en', 'pt', 'es'): ");
                    String idioma = scanner.nextLine();
                    List<Livro> livros = livroService.filtrarLivrosPorIdioma(idioma);
                    if (livros.isEmpty()) {
                        System.out.println("Nenhum livro encontrado nesse idioma.");
                    } else {
                        livros.forEach(System.out::println);
                    }
                }
                case "4" -> {
                    System.out.print("\nDigite parte do título do livro: ");
                    String parcial = scanner.nextLine();
                    List<Livro> livros = livroService.listarPorTituloParcial(parcial);
                    if (livros.isEmpty()) {
                        System.out.println("Nenhum livro encontrado com esse termo.");
                        List<Livro> sugestoes = livroService.sugerirPorTitulo(parcial);
                        if (!sugestoes.isEmpty()) {
                            System.out.println("Sugestões de livros próximos:");
                            sugestoes.forEach(l -> System.out.println("• " + l.getTitulo()));
                        }
                    } else {
                        livros.forEach(System.out::println);
                    }
                }
                case "5" -> {
                    System.out.print("\nDigite parte do título para buscar na API: ");
                    String parcial = scanner.nextLine();
                    List<Livro> livrosDaApi = livroService.buscarLivrosNaApiPorTituloParcial(parcial);
                    if (livrosDaApi.isEmpty()) {
                        System.out.println("Nenhum livro encontrado na API com esse termo.");
                    } else {
                        System.out.println("\n--- Livros encontrados na API ---");
                        livrosDaApi.forEach(System.out::println);
                    }
                }
                case "0" -> {
                    System.out.println("Saindo...");
                    sair = true;
                }
                default -> System.out.println("Opção inválida! Tente novamente.");
            }
            if (!sair) {
                System.out.println("\nPressione ENTER para continuar...");
                scanner.nextLine();
            }
        }
        System.out.println("Programa encerrado.");
    }
}
