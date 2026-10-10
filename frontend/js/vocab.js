/* VouPassar — vocabulário da API em português
 *
 * A API responde com códigos técnicos (FACIL, ESTUDO, LP, AUTHORAL…).
 * Mostrar esses valores ao estudante é jargão: ninguém que estuda para o
 * IFRN sabe o que um código interno significa.
 *
 * Este módulo é o único lugar onde a tradução acontece. Todos os rótulos
 * são fixos e não MDM: nenhuma informação é inventada aqui, apenas o
 * código éposto em português. Se a API inventar um valor novo, o fallback
 * mostra um texto neutro em vez de despejar o enum cru na tela.
 *
 * Regra do projeto (AGENTS.md §4): na dúvida, marcar como desconhecido —
 * nunca apresentar o código interno como se fosse informação.
 */

/** Disciplina: o mesmo assunto chega com códigos diferentes em endpoints
 *  diferentes (diagnóstico traz MATEMATICA, catálogo traz LINGUA_PORTUGUESA). */
const DISCIPLINES = {
  LP: "Língua Portuguesa",
  LINGUA_PORTUGUESA: "Língua Portuguesa",
  MAT: "Matemática",
  MATEMATICA: "Matemática",
};

export function disciplineLabel(code, fallback) {
  if (fallback) return fallback;
  // Sem código e sem nome: devolve "" para que o chamador decida o texto
  // ("Edição real", "caderno misto" ou ocultar o selo). Nunca despejar um
  // rótulo genérico que impeça o fallback do chamador.
  return DISCIPLINES[code] || "";
}

const MODES = {
  ESTUDO: "Estudo",
  PROVA: "Prova",
  REVISAO: "Revisão",
};

export function modeLabel(mode) {
  return MODES[mode] || "";
}

/** Origem da questão. Nunca devolver o enum cru. */
const SOURCE_TYPES = {
  OFFICIAL: "Oficial do IFRN",
  AUTHORAL: "Criada pelo VouPassar",
  ADAPTED: "Adaptada de uma prova",
  INTERNAL_REVIEW: "Questão de revisão interna",
  EXPERIMENTAL: "Questão experimental",
};

export function sourceTypeLabel(value, officialLabel = "Oficial do IFRN") {
  if (!value) return "Origem desconhecida";
  if (value === "OFFICIAL") return officialLabel;
  return SOURCE_TYPES[value] || "Questão não oficial";
}

/* Selo "Oficial" por processo (TASK F.2): EAJ nunca aparece como IFRN
 * (AGENTS.md §4). IFRN mantém o rótulo legado; EAJ usa o paralelo
 * "Oficial do EAJ"; instituição ausente/DESCONHECIDA usa o neutro. */
export function officialLabelFor(institution) {
  const v = String(institution || "").trim().toUpperCase();
  if (v === "EAJ") return "Oficial do EAJ";
  if (v === "IFRN") return "Oficial do IFRN";
  return "Oficial (processo desconhecido)";
}

/* Sufixo de não-oficial sem atribuir processo errado: autoral nunca é de
 * processo algum (backend: "nem do IFRN nem do EAJ/UFRN"). */
export function nonOfficialSuffix(institution) {
  const v = String(institution || "").trim().toUpperCase();
  if (v === "EAJ") return "— não é questão do EAJ";
  if (v === "IFRN") return "— não é questão do IFRN";
  return "— não é questão oficial";
}

/* Rótulo de origem completo p/ selos (selo único, sem duplicar lógica). */
export function originLabel(q) {
  const label = sourceTypeLabel(q?.sourceType, officialLabelFor(q?.institution));
  if (!q?.sourceType || q.sourceType === "OFFICIAL") return label;
  return `${label} ${nonOfficialSuffix(q?.institution)}`;
}

/* Referência de edição sem ambiguidade ano=edição (2022/2025 existem nos
 * dois processos): EAJ prefixa o processo; IFRN mantém o legado. */
export function editionRef(q, fallback = "Origem ainda não registrada") {
  if (q?.examYear && q?.questionNumber) {
    const v = String(q?.institution || "").trim().toUpperCase();
    if (v === "EAJ") return `EAJ-${q.examYear} · Q${q.questionNumber}`;
    return `${q.examYear} · Q${q.questionNumber}`;
  }
  return fallback;
}

/* Título curto p/ cabeçalhos de cartão/questão (mesma regra do editionRef). */
export function questionRef(q, fallbackId = null) {
  if (q?.examYear && q?.questionNumber) {
    const v = String(q?.institution || "").trim().toUpperCase();
    if (v === "EAJ") return `EAJ ${q.examYear} Q${q.questionNumber}`;
    return `${q.examYear} Q${q.questionNumber}`;
  }
  if (q?.questionNumber) return `Q${q.questionNumber}`;
  return fallbackId ?? "Questão";
}

/* Nota de anulada sem atribuir a banca errada (EAJ Q23-2025 existe). */
export function annulledNote(institution) {
  const v = String(institution || "").trim().toUpperCase();
  if (v === "EAJ") return "Questão anulada (gabarito X): conta como conteúdo e fica fora do aproveitamento — a banca não informa como pontuar anuladas.";
  return "Questão anulada pelo IFRN: você pode responder, mas ela não entra no seu aproveitamento — o IFRN não publica como pontuar anuladas.";
}

/** Dificuldade estimada. O "(estimativa)" fica explícito: não é dado do IFRN. */
const DIFFICULTIES = {
  FACIL: "Fácil",
  MEDIA: "Média",
  DIFICIL: "Difícil",
};

export function difficultyLabel(value) {
  return DIFFICULTIES[value] || "";
}

/** Confiança da classificação automática, sem jargão de modelo. */
const CONFIDENCES = {
  ALTA: "classificação confiável",
  MEDIA: "classificação provável",
  BAIXA: "classificação ainda incerta",
};

export function confidenceLabel(value) {
  return CONFIDENCES[value] || "classificação ainda incerta";
}

/** Status de item de roteiro / tentativa. */
const STATUSES = {
  TODO: "A fazer",
  DOING: "Em andamento",
  DONE: "Concluído",
  SKIPPED: "Pulado",
  SUBMITTED: "Concluído",
  IN_PROGRESS: "Em andamento",
  ABANDONED: "Abandonado",
};

export function statusLabel(status) {
  return STATUSES[status] || "";
}

/** Nível de domínio de um assunto. */
const MASTERY = {
  DOMINADO: "Dominado",
  CONSOLIDADO: "Consolidado",
  FRAGIL: "Precisa treinar",
  INICIAL: "Começando",
  EM_DESENVOLVIMENTO: "Em andamento",
  EM_OBSERVACAO: "Em observação",
  NAO_AVALIADO: "Ainda sem dados",
  DESCONHECIDO: "Ainda sem dados",
};

export function masteryLabel(level) {
  return MASTERY[level] || "Ainda sem dados";
}

/** Fila de revisão: categorias 0–5 (menor = mais urgente). Espelha os baldes
 *  do backend (ReviewService.java — categorize/weight), sem exibir o enum
 *  cru ao aluno. Valor desconhecido → "Categoria desconhecida" (AGENTS §4). */
const REVIEW_CATEGORIES = {
  ERRO_SEM_ACERTO: "Erro sem acerto",
  ERRO_RECENTE: "Erro recente",
  TOPICO_FRAGIL: "Assunto frágil",
  REFORCO: "Reforço",
  MANUTENCAO: "Manutenção",
  CONSOLIDADO: "Consolidado",
};

export function reviewCategoryLabel(category) {
  return REVIEW_CATEGORIES[category] || "Categoria desconhecida";
}

/** Motivo curto por categoria: paráfrase da dica que o próprio backend
 *  anexa ao `reason` de cada item (ReviewService.java — itemReason). O
 *  `reason` completo continua vindo da API com os números auditáveis; aqui
 *  mora só o texto fixo em português, sem código interno. */
const REVIEW_CATEGORY_HINTS = {
  ERRO_SEM_ACERTO: "Prioridade máxima: você ainda não acertou esta questão.",
  ERRO_RECENTE: "Regressão: você já acertou antes — reveja antes que o erro se fixe.",
  TOPICO_FRAGIL: "Você acertou a questão, mas o assunto segue frágil.",
  REFORCO: "Reforço para consolidar o assunto.",
  MANUTENCAO: "Manutenção: exposição única, risco de baixa retenção.",
  CONSOLIDADO: "Manutenção do consolidado.",
};

export function reviewCategoryHint(category) {
  return REVIEW_CATEGORY_HINTS[category] || "";
}

/** Assuntos do acervo, conferidos na tabela `topics` do banco.
 *  Serve para traduzir o código que vem dentro das frases do backend
 *  (ex.: "tema GRAMATICA_NORMA") sem precisar exibir o código cru. */
const TOPICS = {
  ALGEBRA: "Álgebra",
  ARITMETICA: "Aritmética",
  ESTATISTICA_DADOS: "Estatística e dados",
  GEOMETRIA: "Geometria",
  GRAMATICA_NORMA: "Gramática e norma",
  GRANDEZAS_MEDIDAS: "Grandezas e medidas",
  INTERPRETACAO_TEXTUAL: "Interpretação textual",
  MATEMATICA_FINANCEIRA: "Matemática financeira",
  PORCENTAGEM: "Porcentagem",
  RAZAO_PROPORCAO: "Razão e proporção",
};

export function topicLabel(code, fallback) {
  if (fallback) return fallback;
  return TOPICS[code] || "";
}

/** Troca códigos de assunto embutidos em frases do backend
 *  (ex.: "tema GRAMATICA_NORMA com 60 questões…") pelo nome em
 *  português, sem exibir o código cru ao aluno. Códigos
 *  desconhecidos passam intactos — nunca inventar tradução. */
export function translateTopicCodes(text) {
  if (!text) return text;
  return String(text).replace(/\b[A-Z][A-Z_]{1,}\b/g, (word) =>
    TOPICS[word] ? TOPICS[word] : word,
  );
}

/** Opção marcada pelo aluno. BLANK é o código que a API espera para
 *  "responder em branco" — o aluno lê português, nunca o enum cru. */
export function choiceLabel(choice) {
  return choice === "BLANK" ? "em branco" : String(choice ?? "—");
}

/** Título de simulado vindo do backend ("Simulado Matemática — 3 questões
 *  [PROVA]"): o sufixo [MODO] é enum cru da API e o modo já aparece em selo
 *  próprio na tela — por isso ele é removido da exibição, sem inventar nada.
 *  Sem título válido, usa o fallback informado. */
export function simulationTitle(title, fallback) {
  const clean = String(title ?? "")
    .replace(/\s*\[(ESTUDO|PROVA|REVISAO)\]\s*$/i, "")
    .trim();
  return clean || fallback || "Simulado";
}

export function plural(n, one, many) {
  return `${n} ${n === 1 ? one : many}`;
}
