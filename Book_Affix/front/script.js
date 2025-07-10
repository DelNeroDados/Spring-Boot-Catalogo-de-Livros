/******************************************************************************
 *                         FUNÇÕES COMUNS A TODOS OS HTMLs
 ******************************************************************************/

const API_LOCAL = "http://localhost:8080/livros";
const API_GUTENDEX = "https://gutendex.com/books/";

function showLoader(show = true) {
  const el = document.getElementById('loader');
  if (el) el.classList.toggle('hide', !show);
}

function renderBooks(books, options = {}) {
  const container = document.getElementById('books-container');
  if (!container) return;
  container.innerHTML = "";

  if (!books || books.length === 0) {
    container.innerHTML = "<p>Nenhum livro encontrado.</p>";
    return;
  }

  books.forEach(book => {
    const title = book.title || book.titulo || "";
    const authors =
      (book.authors && Array.isArray(book.authors))
        ? book.authors.map(a => a.name).join(", ")
        : (book.autor && typeof book.autor === "object" && book.autor.nome)
          ? book.autor.nome
          : (typeof book.autor === "string" ? book.autor : "Desconhecido");
    const lang = (book.languages && book.languages[0]) || book.idioma || "";
    const cover = book.formats?.["image/jpeg"] || book.capa || "https://via.placeholder.com/220x300?text=Sem+Capa";
    const summary =
      book.summaries || book.description || book.resumo || "Sem resumo.";
    const subjects = book.subjects || [];
    const downloads = book.download_count || book.downloads || 0;
    const link = book.formats?.["text/html"] || book.link || "#";

    let actions = "";

    // Botões específicos dependendo do HTML/Opção
    if (options.saveHandler) {
      actions += `<button class="saveBtn">Salvar no sistema</button>`;
    }
    if (options.deleteHandler) {
      actions += `<button class="deleteBtn" style="margin-top:10px;background:#cc2d2d">Excluir</button>`;
    }

    container.insertAdjacentHTML('beforeend', `
      <div class="book-card">
        <img class="book-cover" src="${cover}" alt="Capa de ${title}">
        <div class="card-content">
          <div class="book-title" title="${title}">${title}</div>
          <div class="book-author"><b>Autor:</b> ${authors}</div>
          <div class="book-lang"><b>Idioma:</b> ${lang.toUpperCase()}</div>
          <div class="book-downloads"><b>Downloads:</b> ${downloads}</div>
          <div class="book-subjects"><b>Assuntos:</b> ${subjects.join(', ') || "-"}</div>
          <details class="book-summary">
            <summary><b>Resumo</b></summary>
            <div>${summary}</div>
          </details>
          <div class="book-link">
            <a href="${link}" target="_blank" ${link !== "#" ? "" : "style='pointer-events:none;opacity:0.4;'"}>Abrir Livro Online</a>
          </div>
          ${actions}
        </div>
      </div>
    `);
    const card = container.lastElementChild;
    // Eventos dinâmicos
    if (options.saveHandler) card.querySelector(".saveBtn").onclick = () => options.saveHandler(book, card);
    if (options.deleteHandler) card.querySelector(".deleteBtn").onclick = () => options.deleteHandler(book, card);
  });
}

/******************************************************************************
 *                   ESPECÍFICO: buscar_api.html (API Externa)
 ******************************************************************************/
if (document.getElementById("apiForm")) {
  let booksAll = [];

  function fetchApiBooks(query = "") {
    showLoader(true);
    fetch(`${API_GUTENDEX}?search=${encodeURIComponent(query)}`)
      .then(res => res.json())
      .then(json => {
        booksAll = json.results;
        renderBooks(booksAll, { saveHandler });
        updateDropdowns(booksAll, "apiLanguageFilter", "apiSubjectFilter");
        showLoader(false);
      })
      .catch(() => {
        renderBooks([]);
        showLoader(false);
        alert("Erro ao buscar livros da API externa.");
      });
  }

  function saveBook(book, cardElement) {
    showLoader(true);
    fetch(API_LOCAL + "/importar-da-api", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ titulo: book.title })
    })
      .then(async res => {
        if (res.ok) {
          cardElement.querySelector(".saveBtn").disabled = true;
          cardElement.querySelector(".saveBtn").textContent = "Salvo!";
        } else {
          const errorText = await res.text();
          alert("Erro ao salvar livro no backend:\n" + errorText);
        }
      })
      .catch(e => alert("Erro ao salvar livro no backend. " + e.message))
      .finally(() => showLoader(false));
  }

  function updateDropdowns(books, langId, subjId) {
    let languages = new Set();
    let subjects = new Set();
    books.forEach(b => {
      (b.languages || []).forEach(l => l && languages.add(l));
      (b.subjects || []).forEach(s => s && subjects.add(s));
    });
    const langSelect = document.getElementById(langId);
    if (langSelect) langSelect.innerHTML = `<option value="">Idioma</option>` +
      Array.from(languages).sort().map(l => `<option value="${l}">${l.toUpperCase()}</option>`).join("");
    const subjSelect = document.getElementById(subjId);
    if (subjSelect) subjSelect.innerHTML = `<option value="">Assunto</option>` +
      Array.from(subjects).sort().map(s => `<option value="${s}">${s}</option>`).join("");
  }

  function saveHandler(book, card) { saveBook(book, card); }

  document.getElementById("apiForm").onsubmit = function (e) {
    e.preventDefault();
    fetchApiBooks(document.getElementById("apiSearchInput").value.trim());
  };
  document.getElementById("apiLanguageFilter").onchange = function () {
    const lang = this.value;
    if (!lang) return renderBooks(booksAll, { saveHandler });
    const filtered = booksAll.filter(b => (b.languages || []).includes(lang));
    renderBooks(filtered, { saveHandler });
  };
  document.getElementById("apiSubjectFilter").onchange = function () {
    const subject = this.value;
    if (!subject) return renderBooks(booksAll, { saveHandler });
    const filtered = booksAll.filter(b => (b.subjects || []).includes(subject));
    renderBooks(filtered, { saveHandler });
  };
  document.getElementById("apiListAllBtn").onclick = () => renderBooks(booksAll, { saveHandler });
  document.getElementById("apiSortDownloadsBtn").onclick = () => {
    let sorted = [...booksAll].sort((a, b) => (b.download_count || 0) - (a.download_count || 0));
    renderBooks(sorted, { saveHandler });
  };
  document.getElementById("apiSortAlphaBtn").onclick = () => {
    let sorted = [...booksAll].sort((a, b) => (a.title || "").localeCompare(b.title || ""));
    renderBooks(sorted, { saveHandler });
  };

  window.onload = () => fetchApiBooks("");
}

/******************************************************************************
 *                   ESPECÍFICO: buscar_local.html (Livros Locais)
 ******************************************************************************/
if (document.getElementById("searchForm")) {
  let allBooks = [];

  function fetchLocalBooks() {
    showLoader(true);
    fetch(API_LOCAL)
      .then(res => res.json())
      .then(data => {
        allBooks = data;
        renderBooks(data);
        updateDropdowns(data, "languageFilter", "subjectFilter");
        showLoader(false);
      })
      .catch(() => {
        renderBooks([]);
        showLoader(false);
        alert("Erro ao buscar livros do servidor local.");
      });
  }

  function updateDropdowns(books, langId, subjId) {
    let languages = new Set();
    let subjects = new Set();
    books.forEach(b => {
      (b.languages || (b.idioma ? [b.idioma] : [])).forEach(l => l && languages.add(l));
      (b.subjects || []).forEach(s => s && subjects.add(s));
    });
    const langSelect = document.getElementById(langId);
    if (langSelect) langSelect.innerHTML = `<option value="">Idioma</option>` +
      Array.from(languages).sort().map(l => `<option value="${l}">${l.toUpperCase()}</option>`).join("");
    const subjSelect = document.getElementById(subjId);
    if (subjSelect) subjSelect.innerHTML = `<option value="">Assunto</option>` +
      Array.from(subjects).sort().map(s => `<option value="${s}">${s}</option>`).join("");
  }

  document.getElementById("searchForm").onsubmit = function (e) {
    e.preventDefault();
    const term = document.getElementById("searchInput").value.trim().toLowerCase();
    if (!term) return renderBooks(allBooks);
    const filtered = allBooks.filter(b =>
      (b.title || b.titulo || "").toLowerCase().includes(term) ||
      (b.subjects || []).some(s => s.toLowerCase().includes(term))
    );
    renderBooks(filtered);
  };

  document.getElementById("languageFilter").onchange = function () {
    const lang = this.value;
    if (!lang) return renderBooks(allBooks);
    const filtered = allBooks.filter(b => {
      let languages = b.languages || (b.idioma ? [b.idioma] : []);
      return languages.includes(lang);
    });
    renderBooks(filtered);
  };

  document.getElementById("subjectFilter").onchange = function () {
    const subject = this.value;
    if (!subject) return renderBooks(allBooks);
    const filtered = allBooks.filter(b => (b.subjects || []).includes(subject));
    renderBooks(filtered);
  };

  document.getElementById("listAllBtn").onclick = () => renderBooks(allBooks);

  document.getElementById("sortDownloadsBtn").onclick = () => {
    let sorted = [...allBooks].sort((a, b) => (b.download_count || b.downloads || 0) - (a.download_count || a.downloads || 0));
    renderBooks(sorted);
  };

  document.getElementById("sortAlphaBtn").onclick = () => {
    let sorted = [...allBooks].sort((a, b) => (a.title || a.titulo || "").localeCompare(b.title || b.titulo || ""));
    renderBooks(sorted);
  };

  window.onload = fetchLocalBooks;
}

/******************************************************************************
 *                   ESPECÍFICO: excluir_local.html (Excluir Livros)
 ******************************************************************************/
/*
if (document.body && document.body.contains(document.querySelector("script[src*='excluir_local']"))) {
*/

if (document.getElementById("excluir_local")) {
  let allBooks = [];

  function fetchLocalBooks() {
    showLoader(true);
    fetch(API_LOCAL)
      .then(res => res.json())
      .then(data => {
        allBooks = data;
        renderBooks(data, { deleteHandler });
        showLoader(false);
      })
      .catch(() => {
        renderBooks([]);
        showLoader(false);
        alert("Erro ao buscar livros do servidor local.");
      });
  }

  function deleteBook(id, cardElement) {
    showLoader(true);
    fetch(`${API_LOCAL}/${id}`, { method: "DELETE" })
      .then(res => {
        if (res.ok) cardElement.remove();
        else alert("Erro ao excluir livro.");
      })
      .catch(() => alert("Erro ao excluir livro."))
      .finally(() => showLoader(false));
  }

  function deleteHandler(book, card) {
    showConfirmModal("Tem certeza que deseja excluir este livro?", () => {
      deleteBook(book.id, card);
    });
  }

  window.onload = fetchLocalBooks;


function showConfirmModal(message, onConfirm) {
  const modal = document.getElementById("customConfirm");
  modal.querySelector("p").textContent = message;
  modal.classList.remove("hide");

  const okBtn = document.getElementById("modalOk");
  const cancelBtn = document.getElementById("modalCancel");

  function closeModal() {
    modal.classList.add("hide");
    okBtn.removeEventListener("click", confirmAction);
    cancelBtn.removeEventListener("click", cancelAction);
  }

  function confirmAction() {
    closeModal();
    if (onConfirm) onConfirm();
  }
  function cancelAction() { closeModal(); }

  okBtn.addEventListener("click", confirmAction);
  cancelBtn.addEventListener("click", cancelAction);
}



}
