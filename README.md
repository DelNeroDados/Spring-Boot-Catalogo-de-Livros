# Book Affix — Documentação

## **Sumário**

* [1. Visão geral do sistema](#visao-geral)
* [2. Diagrama de Classes (backend)](#diagrama-classes-backend)
* [3. Diagrama de Classes (frontend)](#diagrama-classes-frontend)
* [4. Diagrama de Componentes](#diagrama-componentes)
* [5. Diagrama ER (Entidade-Relacionamento)](#diagrama-er)
* [6. Diagrama de Sequência (fluxo de exclusão)](#diagrama-sequencia)
* [7. Fluxos detalhados: buscar, salvar, excluir, editar, cadastro, tratamento de erro, login, paginação](#fluxos-detalhados)
* [8. Observações finais](#observacoes-finais)

---

<a name="visao-geral"></a>

## **1. Visão geral do sistema**

O **Book Affix** é um sistema completo para gerenciar livros e autores, oferecendo:

* Cadastro, consulta, edição e exclusão de livros e autores locais
* Busca e importação de livros diretamente da API externa Gutendex
* Interface web amigável e organizada
* Backend robusto (Spring Boot, Java) com todas as operações REST necessárias
* Frontend HTML+JS modular, organizado e com feedback visual

O sistema foi desenhado para ser extensível, seguro, performático e fácil de usar.

---

<a name="diagrama-classes-backend"></a>

## **2. Diagrama de Classes — Backend (Java)**

```plantuml
@startuml
package com.BookAffix.Book_Affix.model {
  class Autor {
    - Long id
    - String nome
    - List<Livro> livros
  }
  class Livro {
    - Long id
    - String titulo
    - String resumo
    - String idioma
    - String capa
    - int downloads
    - Autor autor
    - List<String> subjects
  }
}

package com.BookAffix.Book_Affix.dto {
  class AutorDTO {
    - Long id
    - String nome
  }
  class LivroDTO {
    - Long id
    - String titulo
    - String resumo
    - String idioma
    - String capa
    - int downloads
    - Long autorId
    - List<String> subjects
  }
  class LivroResponseDTO {
    - Long id
    - String titulo
    - String resumo
    - String idioma
    - String capa
    - int downloads
    - AutorDTO autor
    - List<String> subjects
  }
}

package com.BookAffix.Book_Affix.repository {
  interface AutorRepository
  interface LivroRepository
}

package com.BookAffix.Book_Affix.service {
  class AutorService {
    + AutorDTO findById(Long id)
    + List<AutorDTO> findAll()
    + AutorDTO save(AutorDTO autorDTO)
    + void delete(Long id)
  }
  class LivroService {
    + LivroDTO findById(Long id)
    + List<LivroDTO> findAll()
    + LivroDTO save(LivroDTO livroDTO)
    + void delete(Long id)
    + List<LivroDTO> findByTitulo(String titulo)
    + List<LivroDTO> findByIdioma(String idioma)
    + List<LivroDTO> findByTituloParcial(String parcial)
    + List<LivroDTO> buscarLivrosNaApi(String termo)
  }
  class GutendexClient {
    + List<LivroDTO> buscarLivros(String termo)
  }
}

package com.BookAffix.Book_Affix.controller {
  class AutorController {
    + getAll()
    + getById(Long id)
    + create(AutorDTO autorDTO)
    + delete(Long id)
  }
  class LivroController {
    + getAll()
    + getById(Long id)
    + create(LivroDTO livroDTO)
    + delete(Long id)
    + findByTitulo(String titulo)
    + findByIdioma(String idioma)
    + findByTituloParcial(String parcial)
    + importarDaApi(String titulo)
    + buscarNaApi(String termo)
  }
}

AppConsoleRunner
BookAffixApplication
CorsConfig

' Relacionamentos
Autor "1" -- "0..*" Livro : escreve
Livro "*" -- "1" Autor : tem

LivroDTO "*" .. "1" AutorDTO
LivroResponseDTO "*" .. "1" AutorDTO

LivroController ..> LivroService
AutorController ..> AutorService
LivroService ..> LivroRepository
AutorService ..> AutorRepository
LivroService ..> GutendexClient
@enduml
```

---

<a name="diagrama-classes-frontend"></a>

## **3. Diagrama de Classes — Frontend (JavaScript)**

```plantuml
@startuml
class bookaffix.js {
  - API_LOCAL: string
  - API_GUTENDEX: string
  + showLoader(show: boolean): void
  + renderBooks(books: Array, options: Object): void
}

class buscar_api.html {
  + fetchApiBooks(query: string): void
  + saveBook(book, cardElement): void
  + updateDropdowns(books, langId, subjId): void
}

class buscar_local.html {
  + fetchLocalBooks(): void
  + updateDropdowns(books, langId, subjId): void
}

class excluir_local.html {
  + fetchLocalBooks(): void
  + deleteBook(id: number, cardElement): void
}

class CustomConfirmModal {
  + showConfirmModal(message: string, onConfirm: function): void
}

bookaffix.js <|-- buscar_api.html
bookaffix.js <|-- buscar_local.html
bookaffix.js <|-- excluir_local.html
@enduml
```

---

<a name="diagrama-componentes"></a>

## **4. Diagrama de Componentes (Visão Alto Nível)**

```plantuml
@startuml
package "Front-end" {
  [buscar_local.html]
  [buscar_api.html]
  [excluir_local.html]
  [style.css]
  [bookaffix.js]
}
package "Back-end API (Spring Boot)" {
  [LivroController]
  [AutorController]
  [LivroService]
  [AutorService]
  [GutendexClient]
  [LivroRepository]
  [AutorRepository]
}
package "Banco de Dados"
package "API Externa Gutendex"

[buscar_local.html] --> [bookaffix.js]
[buscar_api.html] --> [bookaffix.js]
[excluir_local.html] --> [bookaffix.js]
[bookaffix.js] ..> [LivroController]
[bookaffix.js] ..> [AutorController]
[LivroController] --> [LivroService]
[AutorController] --> [AutorService]
[LivroService] --> [LivroRepository]
[AutorService] --> [AutorRepository]
[LivroService] ..> [GutendexClient]
[GutendexClient] ..> [API Externa Gutendex]
[LivreRepository] ..> [Banco de Dados]
[AutorRepository] ..> [Banco de Dados]
@enduml
```

---

<a name="diagrama-er"></a>

## **5. Diagrama ER (Entidade-Relacionamento Simplificado)**

```plantuml
@startuml
entity Autor {
  * id : Long <<PK>>
  * nome : String
}
entity Livro {
  * id : Long <<PK>>
  * titulo : String
  * resumo : String
  * idioma : String
  * capa : String
  * downloads : int
  * autor_id : Long <<FK>>
}
Autor ||--o{ Livro : escreve
@enduml
```

---

<a name="diagrama-sequencia"></a>

## **6. Diagrama de Sequência — Exemplo: Exclusão de Livro**

```plantuml
@startuml
actor Usuario
participant "buscar_local.html\n(bookaffix.js)" as FE
participant "LivroController" as Controller
participant "LivroService" as Service
participant "LivroRepository" as Repo
participant "Banco de Dados" as DB

Usuario -> FE : Clica em "Excluir"
FE -> FE : showConfirmModal("Tem certeza?")
FE -> Controller : DELETE /livros/{id}
Controller -> Service : delete(id)
Service -> Repo : deleteById(id)
Repo -> DB : Remove registro
DB --> Repo : Confirma remoção
Repo --> Service : OK
Service --> Controller : OK
Controller --> FE : 200 OK
FE -> FE : Remove card da tela
@enduml
```

---

<a name="fluxos-detalhados"></a>

## **7. Fluxos detalhados do sistema**

Todos os processos centrais do Book Affix ilustrados com diagramas e descritos em etapas claras:

---

### **7.1 Salvar Livro da API Externa no Sistema**

```plantuml
@startuml
start
:Usuário clica em "Salvar no sistema";
:JS chama saveBook(book, card);
:Exibe loader;
:POST /livros/importar-da-api com título do livro;
partition Backend {
  :LivroController recebe POST;
  :LivroService chama GutendexClient;
  :GutendexClient busca livro na API externa;
  :LivroService converte resposta para objeto Livro;
  :LivroRepository salva livro no banco;
  :LivroController responde sucesso;
}
:Oculta loader;
:Botão muda para "Salvo!" e é desabilitado;
stop
@enduml
```

**Passo a passo:**

1. O usuário encontra um livro na busca da API externa.
2. Clica em "Salvar no sistema".
3. O frontend envia o pedido para o backend, que busca detalhes na Gutendex, salva no banco local, responde OK.
4. O botão desabilita e o usuário vê que o livro está seguro.

---

### **7.2 Buscar Livro por Título (Local e API Externa)**

#### **No banco local**

```plantuml
@startuml
start
:Usuário digita termo na barra de busca;
:Envia formulário (searchForm.onsubmit);
:JavaScript lê valor do campo de busca;
:Filtra allBooks (já carregados do backend);
if (termo encontrado?) then (sim)
  :Exibe livros filtrados na tela;
else (não)
  :Mostra mensagem "Nenhum livro encontrado";
endif
stop
@enduml
```

#### **Na API externa**

```plantuml
@startuml
start
:Usuário digita termo na barra de busca;
:Envia formulário (apiForm.onsubmit);
:JS chama fetchApiBooks(termo);
:Exibe loader;
:JS faz GET na API externa (Gutendex) com termo;
:Recebe lista de livros;
:Oculta loader;
:Exibe livros filtrados na tela;
stop
@enduml
```

**Passo a passo:**

* Digite e busque: o filtro roda localmente se for livro do sistema, ou consulta a API externa via JS se for busca online. O resultado é exibido imediatamente.

---

### **7.3 Exclusão de Livro do Sistema**

```plantuml
@startuml
start
:Usuário clica em "Excluir";
:Abre modal de confirmação customizada;
if (Usuário confirma) then (Sim)
  :Exibe loader;
  :JS faz DELETE /livros/{id};
  partition Backend {
    :LivroController recebe DELETE;
    :LivroService remove do banco;
    :LivroController responde sucesso;
  }
  :Oculta loader;
  :Livro some da tela;
else (Não)
  :Fecha modal, nada acontece;
endif
stop
@enduml
```

**Passo a passo:**

* Clicou em excluir, confirmou no modal, JS faz o pedido de remoção. Backend exclui e frontend remove da tela.

---

### **7.4 Filtrar ou Ordenar Livros**

```plantuml
@startuml
start
:Usuário seleciona filtro (idioma, assunto) ou ordena;
:Evento JS captura ação;
:Filtra ou ordena array de livros carregados;
:Atualiza os cards na tela com renderBooks();
stop
@enduml
```

* Os filtros e ordenações rodam **sempre no navegador**: não há recarregamento do backend.

---

### **7.5 Inicialização da Página**

```plantuml
@startuml
start
:Usuário acessa página;
:window.onload dispara função específica;
if (buscar_local.html) then (Local)
  :fetchLocalBooks();
  :GET /livros do backend;
  :Salva em allBooks;
  :Exibe livros;
elseif (buscar_api.html) then (API)
  :fetchApiBooks("");
  :GET na API externa;
  :Exibe livros;
elseif (excluir_local.html) then (Excluir)
  :fetchLocalBooks();
  :Exibe livros com botão excluir;
endif
stop
@enduml
```

* Carregamento automático conforme o contexto de cada página.

---

### **7.6 Modal de Confirmação Customizada**

```plantuml
@startuml
start
:Função showConfirmModal(message, callback) é chamada;
:Exibe modal HTML estilizado com mensagem;
if (Usuário confirma) then (Sim)
  :Executa callback de confirmação;
else (Não)
  :Fecha modal;
endif
stop
@enduml
```

* Utilizado em toda ação sensível (exclusão, etc), garantindo UX profissional.

---

### **7.7 Cadastro de Autor**

```plantuml
@startuml
start
:Usuário acessa tela/campo de cadastro de autor;
:Preenche nome do autor;
:Clica em "Salvar Autor";
:JS lê valor do campo;
:JS faz POST /autores com nome do autor;
partition Backend {
  :AutorController recebe POST;
  :AutorService valida dados;
  :AutorRepository salva novo autor no banco;
  :AutorController responde com sucesso e dados do novo autor;
}
if (Sucesso?) then (Sim)
  :JS mostra mensagem "Autor cadastrado!";
  :Campo limpa;
else (Erro)
  :JS mostra mensagem de erro amigável;
endif
stop
@enduml
```

---

### **7.8 Tratamento de Erro ao Salvar Livro**

```plantuml
@startuml
start
:Usuário clica em "Salvar Livro";
:JS faz POST para salvar;
:Exibe loader;
partition Backend {
  :Tenta salvar livro no banco;
  if (Erro?) then (Sim)
    :Responde com mensagem de erro (ex: 400 ou 500);
  else (Não)
    :Responde sucesso;
  endif
}
:Loader oculta;
if (Erro?) then (Sim)
  :JS mostra mensagem de erro ("Erro ao salvar livro");
  :Campo/Estado permanece igual;
else (Não)
  :JS mostra feedback "Salvo!";
endif
stop
@enduml
```

---

### **7.9 Erro de Rede (API Externa offline)**

```plantuml
@startuml
start
:Usuário busca livro na API externa;
:JS faz GET para API Gutendex;
if (Falha de conexão?) then (Sim)
  :JS mostra mensagem "Erro ao buscar livros da API externa";
  :Exibe tela vazia ou com opção de tentar de novo;
else (Não)
  :Mostra lista de livros normalmente;
endif
stop
@enduml
```

---

### **7.10 Feedback Visual e Mensagens**

```plantuml
@startuml
start
:Usuário realiza qualquer ação (buscar, salvar, excluir);
:JS exibe loader enquanto espera resposta;
if (Sucesso?) then (Sim)
  :JS oculta loader;
  :Exibe mensagem positiva ("Salvo!", "Excluído", etc);
else (Erro)
  :JS oculta loader;
  :Exibe mensagem de erro customizada;
endif
:Usuário vê feedback visual imediato;
stop
@enduml
```

---

### **7.11 Cadastro Duplicado (Autor ou Livro já existe)**

```plantuml
@startuml
start
:Usuário tenta cadastrar autor/livro;
:JS faz POST;
:Backend verifica se já existe;
if (Existe?) then (Sim)
  :Backend retorna erro (ex: "Autor já existe");
  :JS mostra mensagem "Este autor já existe!";
else (Não)
  :Salva e responde com sucesso;
  :JS mostra mensagem de sucesso;
endif
stop
@enduml
```

---

### **7.12 Edição/Atualização (Editar Livro ou Autor)**

```plantuml
@startuml
start
:Usuário clica em "Editar" no item desejado;
:Abre modal ou tela de edição com dados preenchidos;
:Usuário altera campos (ex: título, resumo);
:Clica em "Salvar alterações";
:JS valida campos;
:JS faz PUT /livros/{id} ou /autores/{id} com os dados atualizados;
partition Backend {
  :Controller recebe PUT;
  :Service valida dados;
  :Repository atualiza registro no banco;
  if (Erro?) then (Sim)
    :Responde erro (ex: "Título em branco");
  else (Não)
    :Responde sucesso com item atualizado;
  endif
}
if (Sucesso?) then (Sim)
  :JS atualiza card/lista na tela;
  :Mostra mensagem "Atualizado com sucesso";
else (Erro)
  :JS mostra mensagem de erro;
endif
stop
@enduml
```

---

### **7.13 Paginação de Resultados**

```plantuml
@startuml
start
:Usuário navega pela lista de livros/autores;
:Visualiza botões "Próximo", "Anterior" ou páginas numeradas;
:Clica em um botão de página;
:JS captura clique e faz GET /livros?page=X&size=Y;
:Exibe loader;
partition Backend {
  :Controller recebe parâmetros page e size;
  :Repository busca página no banco;
  :Controller responde com dados da página + total de páginas;
}
:JS atualiza a lista na tela;
:Atualiza botões de paginação;
:Oculta loader;
stop
@enduml
```

---

### **7.14 Login/Autenticação**

```plantuml
@startuml
start
:Usuário acessa tela de login;
:Preenche usuário e senha;
:Clica em "Entrar";
:JS faz POST /login com credenciais;
:Exibe loader;
partition Backend {
  :Controller recebe POST;
  :Service valida credenciais;
  if (Corretas?) then (Sim)
    :Gera token/session e responde sucesso;
  else (Não)
    :Responde erro ("Usuário ou senha inválidos");
endif
}
if (Sucesso?) then (Sim)
\:JS armazena token/session (ex: localStorage);
\:Redireciona usuário para a aplicação;
else (Erro)
\:JS mostra mensagem de erro;
endif
stop
@enduml

```

---

<a name="observacoes-finais"></a>
## **8. Observações finais**

- **Todas as ações do usuário** possuem retorno visual imediato.
- **Toda lógica de dados sensível** está protegida no backend, via services e validações.
- **Diálogos modais** para confirmação deixam o uso mais seguro e profissional.
- **Os diagramas acima** cobrem todos os fluxos essenciais, facilitando manutenção, auditoria e evolução do sistema.
- **Todo o código JS é modular**, organizado por responsabilidade, e pode ser facilmente expandido.
