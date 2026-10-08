/* VouPassar — teste do raio-X pós-atividade (TASK 19.1).
 * Roda sem dependências: `node scripts/analysis/test_result_next.mjs`.
 * Cobre js/components/result-next.js (erros, agrupamento por assunto com
 * fallback por disciplina, determinismo, CTAs e fallbacks honestos) — o
 * mesmo bloco exibido ao final de cada simulado concluído.
 */
import assert from "node:assert/strict";

import {
  MAX_NEXT_ACTIONS,
  errorItemsOf,
  errorRef,
  summarizeResult,
  buildNextActions,
  groupDisplayName,
} from "../../frontend/js/components/result-next.js";

let n = 0;
const ok = (name, fn) => {
  fn();
  n += 1;
  console.log(`ok ${n} - ${name}`);
};

// Resultado com 6 erros (E2E da TASK 19.1): bloco mostra 6 + top + 3 CTAs.
const SIX_ERRORS = {
  attemptId: 55,
  scored: 8,
  correct: 2,
  accuracy: 0.25,
  items: [
    { position: 1, questionId: 11, disciplineCode: "MATEMATICA", sourceYear: 2024, sourceQuestionNumber: 28, selectedOption: "A", isCorrect: false, wasAnnulled: false, unanswered: false, frozenAnswerKey: "D" },
    { position: 2, questionId: 12, disciplineCode: "MATEMATICA", sourceYear: 2024, sourceQuestionNumber: 29, selectedOption: "B", isCorrect: false, wasAnnulled: false, unanswered: false, frozenAnswerKey: "C" },
    { position: 3, questionId: 13, disciplineCode: "MATEMATICA", sourceYear: 2024, sourceQuestionNumber: 30, selectedOption: "A", isCorrect: false, wasAnnulled: false, unanswered: false, frozenAnswerKey: "B" },
    { position: 4, questionId: 14, disciplineCode: "LINGUA_PORTUGUESA", sourceYear: 2024, sourceQuestionNumber: 1, selectedOption: "C", isCorrect: false, wasAnnulled: false, unanswered: false, frozenAnswerKey: "A" },
    { position: 5, questionId: 15, disciplineCode: "LINGUA_PORTUGUESA", sourceYear: 2024, sourceQuestionNumber: 2, selectedOption: "D", isCorrect: false, wasAnnulled: false, unanswered: false, frozenAnswerKey: "B" },
    { position: 6, questionId: 16, disciplineCode: "LINGUA_PORTUGUESA", sourceYear: 2024, sourceQuestionNumber: 3, selectedOption: "BLANK", isCorrect: false, wasAnnulled: false, unanswered: false, frozenAnswerKey: "A" },
    { position: 7, questionId: 17, disciplineCode: "MATEMATICA", sourceYear: 2024, sourceQuestionNumber: 31, selectedOption: "D", isCorrect: true, wasAnnulled: false, unanswered: false, frozenAnswerKey: "D" },
    { position: 8, questionId: 18, disciplineCode: "MATEMATICA", sourceYear: 2024, sourceQuestionNumber: 32, selectedOption: "A", isCorrect: true, wasAnnulled: false, unanswered: false, frozenAnswerKey: "A" },
  ],
};

const DETAILS = new Map([
  [11, { topic: { id: 5, code: "RAZAO_PROPORCAO", name: "Razão e proporção" }, discipline: { code: "MATEMATICA" } }],
  [12, { topic: { id: 5, code: "RAZAO_PROPORCAO", name: "Razão e proporção" }, discipline: { code: "MATEMATICA" } }],
  [13, { topic: { id: 5, code: "RAZAO_PROPORCAO", name: "Razão e proporção" }, discipline: { code: "MATEMATICA" } }],
  [14, { topic: { id: 2, code: "INTERPRETACAO_TEXTUAL", name: "Interpretação textual" }, discipline: { code: "LINGUA_PORTUGUESA" } }],
  [15, { topic: { id: 2, code: "INTERPRETACAO_TEXTUAL", name: "Interpretação textual" }, discipline: { code: "LINGUA_PORTUGUESA" } }],
  [16, { topic: { id: 2, code: "INTERPRETACAO_TEXTUAL", name: "Interpretação textual" }, discipline: { code: "LINGUA_PORTUGUESA" } }],
  [17, { topic: { id: 5, code: "RAZAO_PROPORCAO", name: "Razão e proporção" }, discipline: { code: "MATEMATICA" } }],
  [18, { topic: null, discipline: { code: "MATEMATICA" } }],
]);

ok("erros excluem anulada e pendente; BLANK conta como erro", () => {
  const items = [
    ...SIX_ERRORS.items,
    { position: 9, questionId: 19, selectedOption: null, isCorrect: null, wasAnnulled: false, unanswered: true },
    { position: 10, questionId: 20, selectedOption: "A", isCorrect: null, wasAnnulled: true, unanswered: false, frozenAnswerKey: "X" },
  ];
  assert.equal(errorItemsOf({ items }).length, 6);
  assert.equal(errorItemsOf(null).length, 0);
  assert.equal(errorItemsOf({}).length, 0);
});

ok("referência do erro sem duplicar enunciado", () => {
  assert.equal(errorRef(SIX_ERRORS.items[0]), "Posição 1 · 2024 Q28");
  assert.equal(errorRef({ position: 3 }), "Posição 3");
});

ok("6 erros agrupam no assunto do caderno; top com 3", () => {
  const s = summarizeResult(SIX_ERRORS, DETAILS);
  assert.equal(s.errorCount, 6);
  assert.equal(s.unansweredCount, 0);
  assert.equal(s.annulledCount, 0);
  assert.equal(s.topGroup.topicId, 5);
  assert.equal(s.topGroup.errors, 3);
  assert.equal(groupDisplayName(s.topGroup), "Razão e proporção");
  assert.equal(s.errors.length, 6);
  assert.equal(s.errors[5].ref, "Posição 6 · 2024 Q3");
});

ok("sem detalhe agrupa por disciplina; sem nada é honesto", () => {
  const s = summarizeResult(SIX_ERRORS, new Map());
  const names = s.groups.map(groupDisplayName).sort();
  assert.ok(names.includes("Matemática"), names.join("|"));
  assert.ok(names.includes("Língua Portuguesa"), names.join("|"));
  const u = summarizeResult(
    { items: [{ position: 1, questionId: 99, isCorrect: false, wasAnnulled: false, unanswered: false }] },
    new Map(),
  );
  assert.equal(groupDisplayName(u.topGroup), "Sem classificação");
});

ok("desempate determinístico por total e nome", () => {
  const a = summarizeResult(SIX_ERRORS, DETAILS);
  const b = summarizeResult({ ...SIX_ERRORS, items: [...SIX_ERRORS.items].reverse() }, DETAILS);
  assert.deepEqual(a.groups.map((g) => g.key), b.groups.map((g) => g.key));
});

ok("3 CTAs no máximo, com rótulos da task", () => {
  const s = summarizeResult(SIX_ERRORS, DETAILS);
  const actions = buildNextActions(s);
  assert.ok(actions.length <= MAX_NEXT_ACTIONS, String(actions.length));
  assert.equal(actions.length, 3);
  assert.equal(actions[0].id, "review");
  assert.ok(actions[0].label.includes("Revisar estes 6 agora"), actions[0].label);
  assert.equal(actions[1].id, "practice");
  assert.ok(actions[1].label.startsWith("Praticar "), actions[1].label);
  assert.ok(actions[1].href.includes("./estudos.html?"), actions[1].href);
  assert.ok(actions[1].href.includes("topico=5"), actions[1].href);
  assert.equal(actions[2].id, "plan");
  assert.equal(actions[2].label, "Atualizar meu plano");
});

ok("fallbacks honestos: zero erro e resultado vazio", () => {
  const clean = summarizeResult(
    { scored: 2, correct: 2, items: SIX_ERRORS.items.filter((it) => it.isCorrect === true) },
    DETAILS,
  );
  const actions = buildNextActions(clean);
  assert.equal(clean.errorCount, 0);
  assert.deepEqual(actions.map((a) => a.id), ["plan"]);
  assert.equal(summarizeResult({ items: [] }, DETAILS), null);
  assert.deepEqual(buildNextActions(null), []);
});

console.log(`\ntest_result_next: ${n} testes verdes.`);
