/* VouPassar — teste do raio-X em Estudos (TASK 19.2).
 * Roda sem dependências: `node scripts/analysis/test_study_raiox.mjs`.
 * Cobre o reaproveitamento de summarizeResult/buildNextActions com itens no
 * formato da sessão de estudo (sem frozenAnswerKey, assunto via detailsMap):
 * erros distintos, "sem acerto prévio" contado na view, máx. 3 CTAs e
 * fallback honesto sem classificação.
 */
import assert from "node:assert/strict";
import { summarizeResult, buildNextActions, MAX_NEXT_ACTIONS } from "../../frontend/js/components/result-next.js";

let n = 0;
const ok = (name, fn) => { fn(); n += 1; console.log(`ok ${n} - ${name}`); };

const details = new Map([
  [101, { topic: { id: 7, code: "PORCENTAGEM", name: "Porcentagem" }, discipline: { code: "MATEMATICA" } }],
  [102, { topic: { id: 7, code: "PORCENTAGEM", name: "Porcentagem" }, discipline: { code: "MATEMATICA" } }],
  [103, { topic: null, discipline: { code: "MATEMATICA" } }],
]);
details.set("101", details.get(101)); details.set("102", details.get(102)); details.set("103", details.get(103));

ok("sessão com 2 erros gera resumo com topGroup e 3 CTAs", () => {
  const s = summarizeResult({ items: [
    { position: 1, questionId: 101, disciplineCode: "MATEMATICA", isCorrect: false, wasAnnulled: false, unanswered: false },
    { position: 2, questionId: 102, disciplineCode: "MATEMATICA", isCorrect: true, wasAnnulled: false, unanswered: false },
    { position: 3, questionId: 103, disciplineCode: "MATEMATICA", isCorrect: false, wasAnnulled: false, unanswered: false },
  ], scored: 3, correct: 1 }, details);
  assert.equal(s.errorCount, 2);
  assert.ok(s.topGroup);
  const actions = buildNextActions(s);
  assert.ok(actions.length <= MAX_NEXT_ACTIONS);
  assert.deepEqual(actions.map((a) => a.id), ["review", "practice", "plan"]);
});

ok("anulada fora, sem assunto cai em disciplina (nunca inventa)", () => {
  const s = summarizeResult({ items: [
    { position: 1, questionId: 103, disciplineCode: "MATEMATICA", isCorrect: false, wasAnnulled: false, unanswered: false },
  ], scored: 1, correct: 0 }, details);
  assert.equal(s.topGroup.topicId, null);
  assert.match(s.topGroup.disciplineCode, /MATEMATICA/);
});

ok("sem itens devolve null (view não renderiza)", () => {
  assert.equal(summarizeResult({ items: [] }, details), null);
});

console.log(`\n${n} testes do raio-X em Estudos OK`);
