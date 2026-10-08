/* VouPassar — teste da lógica crítica da revisão (TASK 17.3).
 * Roda sem dependências: `node scripts/analysis/test_review_logic.mjs`.
 * Cobre js/views/review-logic.js (vereditos, placar, progresso, guarda de
 * encerramento) + rótulos de js/vocab.js usados na fila (0–5, sem enum cru).
 */
import assert from "node:assert/strict";

import {
  attemptVerdict,
  resultItemVerdict,
  formatReviewAccuracy,
  reviewProgressLabel,
  canFinishReview,
} from "../../frontend/js/views/review-logic.js";
import { reviewCategoryLabel, reviewCategoryHint, choiceLabel } from "../../frontend/js/vocab.js";

let n = 0;
const ok = (name, fn) => {
  fn();
  n += 1;
  console.log(`ok ${n} - ${name}`);
};

// attemptVerdict: acerto / erro / anulada / sem correção (nunca chute).
ok("acerto revela badge Acertou", () => {
  assert.deepEqual(attemptVerdict({ wasAnnulled: false, isCorrect: true }).badge, "Acertou");
});
ok("erro revela badge Errou (BLANK cai aqui no servidor)", () => {
  assert.deepEqual(attemptVerdict({ wasAnnulled: false, isCorrect: false }).badge, "Errou");
});
ok("anulada sai do aproveitamento mesmo com resposta", () => {
  const v = attemptVerdict({ wasAnnulled: true, isCorrect: null });
  assert.deepEqual(v.badge, "Anulada");
});
ok("correção ausente não vira acerto nem erro", () => {
  const v = attemptVerdict({ wasAnnulled: false, isCorrect: null });
  assert.ok(v.badge !== "Acertou" && v.badge !== "Errou");
});

// resultItemVerdict: pendente nunca é erro inventado.
ok("sem resposta é pendente, nunca erro", () => {
  const v = resultItemVerdict({ wasAnnulled: false, unanswered: true, isCorrect: null });
  assert.deepEqual(v.badge, "Pendente");
});
ok("anulada no resultado fica fora do cálculo", () => {
  const v = resultItemVerdict({ wasAnnulled: true, unanswered: false, isCorrect: null });
  assert.deepEqual(v.badge, "Anulada");
});
ok("resultado propaga acerto e erro da última tentativa", () => {
  assert.deepEqual(resultItemVerdict({ isCorrect: true }).badge, "Acertou");
  assert.deepEqual(resultItemVerdict({ isCorrect: false }).badge, "Errou");
});

// Placar: NULL = sem pontuável, nunca zero inventado.
ok("accuracy NULL vira traço", () => {
  assert.deepEqual(formatReviewAccuracy(null), "—");
  assert.deepEqual(formatReviewAccuracy(undefined), "—");
});
ok("accuracy 0 é 0% (zero real, não ausência)", () => {
  assert.ok(formatReviewAccuracy(0).startsWith("0"));
});
ok("accuracy 2/3 formata em pt-BR", () => {
  assert.ok(formatReviewAccuracy(2 / 3).includes("%"));
});

// Progresso e guarda de encerramento.
ok("progresso no formato N de M", () => {
  assert.deepEqual(reviewProgressLabel(2, 3), "2 de 3 respondidas");
  assert.deepEqual(reviewProgressLabel(0, 0), "0 de 0 respondidas");
});
ok("só IN_PROGRESS encerra", () => {
  assert.equal(canFinishReview("IN_PROGRESS"), true);
  assert.equal(canFinishReview("FINISHED"), false);
  assert.equal(canFinishReview(null), false);
});

// Fila 0–5: rótulo em português, nunca enum cru; BLANK legível.
ok("categorias 0–5 têm rótulo e dica", () => {
  for (const c of ["ERRO_SEM_ACERTO", "ERRO_RECENTE", "TOPICO_FRAGIL", "REFORCO", "MANUTENCAO", "CONSOLIDADO"]) {
    assert.notEqual(reviewCategoryLabel(c), c);
    assert.ok(reviewCategoryHint(c).length > 0);
  }
  assert.deepEqual(reviewCategoryLabel("INVENTADA"), "Categoria desconhecida");
});
ok("BLANK aparece como em branco", () => {
  assert.deepEqual(choiceLabel("BLANK"), "em branco");
  assert.deepEqual(choiceLabel("C"), "C");
});

console.log(`\ntest_review_logic: ${n} testes verdes.`);
