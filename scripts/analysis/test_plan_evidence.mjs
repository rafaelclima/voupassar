/* VouPassar — teste da evidência do roteiro (TASK 18.3).
 * Roda sem dependências: `node scripts/analysis/test_plan_evidence.mjs`.
 * Cobre js/components/plan-evidence.js (parse, recência, frase do motivo,
 * links de prática/revisão e próximo item) — a mesma frase exibida no
 * dashboard, nos estudos e no perfil.
 */
import assert from "node:assert/strict";

import {
  parseEvidence,
  daysSince,
  formatRecency,
  formatEditionsShort,
  evidenceLine,
  estudosHref,
  revisaoHref,
  sampleHref,
  practiceTarget,
  nextStudyOf,
} from "../../frontend/js/components/plan-evidence.js";

let n = 0;
const ok = (name, fn) => {
  fn();
  n += 1;
  console.log(`ok ${n} - ${name}`);
};

const NOW = Date.parse("2026-10-08T12:00:00Z");

// parseEvidence: string da API vira objeto normalizado.
ok("parseia evidenceJson em string", () => {
  const ev = parseEvidence({
    topicId: 5,
    evidenceJson: JSON.stringify({
      topic_id: 5,
      historicalQuestions: 70,
      editionsCount: 6,
      editions: [2020, 2022, 2023, 2024, 2025, 2026],
      sampleQuestionIds: [101, 102, 103],
      accuracy: 0.2,
      attempts: 5,
      lastAttemptAt: "2026-09-28T12:00:00Z",
      algorithmVersion: "v2-deterministico",
    }),
  });
  assert.equal(ev.historicalQuestions, 70);
  assert.equal(ev.editionsCount, 6);
  assert.deepEqual(ev.sampleQuestionIds, [101, 102, 103]);
  assert.equal(ev.attempts, 5);
  assert.equal(ev.accuracy, 0.2);
});

// parseEvidence: objeto direto + ausência/JSON inválido.
ok("aceita objeto e rejeita inválido sem quebrar", () => {
  const ev = parseEvidence({ evidenceJson: { historicalQuestions: 3, editions: [2024], sampleQuestionIds: [] } });
  assert.equal(ev.historicalQuestions, 3);
  assert.equal(parseEvidence({}), null);
  assert.equal(parseEvidence({ evidenceJson: "{quebrado" }), null);
  assert.equal(parseEvidence(null), null);
});

// daysSince/formatRecency: hoje, 1 dia, N dias, inválido.
ok("recência em linguagem de estudante", () => {
  assert.equal(daysSince("2026-10-08T08:00:00Z", NOW), 0);
  assert.equal(daysSince("2026-10-07T12:00:00Z", NOW), 1);
  assert.equal(daysSince("2026-09-28T12:00:00Z", NOW), 10);
  assert.equal(daysSince(null, NOW), null);
  assert.equal(daysSince("não-data", NOW), null);
  assert.equal(formatRecency(0), "respondida hoje");
  assert.equal(formatRecency(1), "há 1 dia");
  assert.equal(formatRecency(10), "há 10 dias");
});

// formatEditionsShort: 1, 2, 3 e mais (sem inventar ano).
ok("editions curtas sem inventar ano", () => {
  assert.equal(formatEditionsShort([2024]), "2024");
  assert.equal(formatEditionsShort([2020, 2024]), "2020 e 2024");
  assert.equal(formatEditionsShort([2020, 2022, 2024]), "2020, 2022 e 2024");
  assert.ok(formatEditionsShort([2020, 2022, 2023, 2024]).endsWith("…"));
  assert.equal(formatEditionsShort([]), "");
});

// evidenceLine: frase completa com desempenho + peso + recência.
ok("frase completa do motivo", () => {
  const line = evidenceLine(
    {
      historicalQuestions: 70,
      editionsCount: 6,
      editions: [2020, 2022, 2023, 2024, 2025, 2026],
      sampleQuestionIds: [101],
      accuracy: 0.2,
      attempts: 5,
      lastAttemptAt: "2026-09-28T12:00:00Z",
    },
    NOW,
  );
  assert.ok(line.includes("1 de 5 (20%)"), line);
  assert.ok(line.includes("70"), line);
  assert.ok(line.includes("6 edições"), line);
  assert.ok(line.includes("há 10 dias"), line);
});

// evidenceLine: lacuna sem tentativas + sem peso some a parte da prova.
ok("lacuna honesta sem inventar zero", () => {
  const line = evidenceLine(
    { historicalQuestions: 70, editionsCount: 6, editions: [2024], sampleQuestionIds: [], accuracy: null, attempts: 0, lastAttemptAt: null },
    NOW,
  );
  assert.ok(line.includes("ainda não respondeu"), line);
  assert.ok(!line.includes("0%"), line);
  const bare = evidenceLine({ historicalQuestions: 0, editionsCount: 0, editions: [], sampleQuestionIds: [], accuracy: null, attempts: 0, lastAttemptAt: null }, NOW);
  assert.ok(!bare.includes("Caiu"), bare);
});

// Links: relativos, com recorte e sem vazar origem como filtro.
ok("links de prática e revisão com recorte", () => {
  assert.equal(estudosHref({ disciplineCode: "MATEMATICA", topicId: 5, origin: "painel" }), "./estudos.html?disciplina=MATEMATICA&topico=5&origem=painel");
  assert.equal(estudosHref({ topicId: 5 }), "./estudos.html?topico=5");
  assert.equal(revisaoHref({ disciplineCode: "MATEMATICA", topicId: 5 }), "./estudos.html?aba=revisao&disciplina=MATEMATICA&topico=5");
  assert.equal(sampleHref(101), "./questao.html?id=101");
});

// practiceTarget: disciplina vem do catálogo, nunca inventada.
ok("alvo da prática resolve disciplina do catálogo", () => {
  const catalog = new Map([[5, { disciplineCode: "MATEMATICA" }]]);
  assert.deepEqual(practiceTarget({ topicId: 5, subtopicId: 9 }, catalog), { disciplineCode: "MATEMATICA", topicId: 5, subtopicId: 9 });
  assert.deepEqual(practiceTarget({ topicId: 99 }, catalog).disciplineCode, "");
});

// nextStudyOf: menor priority entre TODO/DOING; sem aberto, menor geral.
ok("próximo item é o aberto de menor prioridade", () => {
  const plan = { items: [{ id: 1, priority: 3, status: "DONE" }, { id: 2, priority: 2, status: "DOING" }, { id: 3, priority: 1, status: "TODO" }] };
  assert.equal(nextStudyOf(plan).id, 3);
  assert.equal(nextStudyOf({ recommendations: [{ id: 7, priority: 1, status: "DONE" }] }).id, 7);
  assert.equal(nextStudyOf(null), null);
});

console.log(`\ntest_plan_evidence: ${n} testes verdes.`);
