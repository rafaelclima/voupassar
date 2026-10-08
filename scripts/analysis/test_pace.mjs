/* VouPassar — teste do ritmo em simulado (TASK 21.1).
 * Roda sem dependências: `node scripts/analysis/test_pace.mjs`.
 * Cobre js/components/pace.js (referência só de edição conhecida, nunca
 * inventada; mesma exibição em ESTUDO e PROVA; só exibição — sem score).
 */
import assert from "node:assert/strict";

import {
  OFFICIAL_DURATION_MINUTES_BY_YEAR,
  resolveEditionYear,
  elapsedSecondsSince,
  executionElapsedSeconds,
  referenceSecondsPerQuestion,
  formatElapsed,
  formatMinutesPerQuestion,
  summarizePace,
} from "../../frontend/js/components/pace.js";

let n = 0;
const ok = (name, fn) => {
  fn();
  n += 1;
  console.log(`ok ${n} - ${name}`);
};

const REAL_2026 = {
  type: "REAL_EDITION",
  title: "Simulado Edição 2026 — 40 questões [PROVA]",
  status: "IN_PROGRESS",
  startedAt: "2026-10-08T10:00:00.000Z",
  questions: [
    { sourceYear: 2026 },
    { sourceYear: 2026 },
    { sourceYear: 2026 },
  ],
};

ok("mapa oficial cobre as 6 edições do dataset com 4h", () => {
  for (const year of [2020, 2022, 2023, 2024, 2025, 2026]) {
    assert.equal(OFFICIAL_DURATION_MINUTES_BY_YEAR[year], 240);
  }
  assert.equal(OFFICIAL_DURATION_MINUTES_BY_YEAR[2021], undefined);
});

ok("ano vem do título; fallback é o sourceYear mais frequente", () => {
  assert.equal(resolveEditionYear(REAL_2026), 2026);
  assert.equal(
    resolveEditionYear({ type: "REAL_EDITION", title: "Sem ano", questions: [{ sourceYear: 2024 }, { sourceYear: 2024 }, { sourceYear: 2025 }] }),
    2024,
  );
  assert.equal(resolveEditionYear({ type: "REAL_EDITION", title: "Sem ano", questions: [] }), null);
  assert.equal(resolveEditionYear({ type: "BY_DISCIPLINE", title: "Simulado Edição 2026", questions: [{ sourceYear: 2026 }] }), null);
  assert.equal(resolveEditionYear(null), null);
});

ok("decorrido nunca negativo nem NaN; encerrada congela no submittedAt", () => {
  assert.equal(elapsedSecondsSince("2026-10-08T10:00:00Z", new Date("2026-10-08T10:23:10Z").getTime()), 1390);
  assert.equal(elapsedSecondsSince("invalido", Date.now()), 0);
  assert.equal(elapsedSecondsSince("2026-10-08T10:00:00Z", NaN), 0);
  const finished = { ...REAL_2026, status: "SUBMITTED", submittedAt: "2026-10-08T12:00:00.000Z" };
  assert.equal(executionElapsedSeconds(finished, new Date("2026-10-08T15:00:00Z").getTime()), 7200);
  assert.equal(executionElapsedSeconds(REAL_2026, new Date("2026-10-08T10:05:00Z").getTime()), 300);
});

ok("referência = 4h ÷ total; ano desconhecido é null", () => {
  assert.equal(referenceSecondsPerQuestion(40, 2026), 360);
  assert.equal(referenceSecondsPerQuestion(10, 2026), 1440);
  assert.equal(referenceSecondsPerQuestion(40, 2021), null);
  assert.equal(referenceSecondsPerQuestion(40, null), null);
  assert.equal(referenceSecondsPerQuestion(0, 2026), null);
});

ok("formatos: HH:MM:SS e min/questão pt-BR", () => {
  assert.equal(formatElapsed(1390), "00:23:10");
  assert.equal(formatElapsed(0), "00:00:00");
  assert.equal(formatElapsed(-5), "00:00:00");
  assert.equal(formatMinutesPerQuestion(360), "6 min/questão");
  assert.equal(formatMinutesPerQuestion(null), "—");
  assert.ok(formatMinutesPerQuestion(276).includes("min/questão"));
});

ok("edição real conhecida mostra referência; por disciplina é informativo", () => {
  const real = summarizePace({ attempt: REAL_2026, answered: 5, total: 40, elapsedSeconds: 1390 });
  assert.equal(real.editionYear, 2026);
  assert.equal(real.hasReference, true);
  assert.equal(real.referenceSecondsPerQuestion, 360);
  assert.ok(real.line.includes("Questão 5 de 40"), real.line);
  assert.ok(real.line.includes("00:23:10 decorridos"), real.line);
  assert.ok(real.line.includes("referência da edição 2026"), real.line);
  assert.ok(!real.line.includes("sem tempo oficial"), real.line);

  const disc = summarizePace({
    attempt: { type: "BY_DISCIPLINE", title: "Simulado Matemática", status: "IN_PROGRESS", questions: [] },
    answered: 5,
    total: 10,
    elapsedSeconds: 720,
  });
  assert.equal(disc.hasReference, false);
  assert.equal(disc.referenceSecondsPerQuestion, null);
  assert.ok(disc.line.includes("ritmo informativo, sem tempo oficial confirmado"), disc.line);
});

ok("ano fora do mapa nunca inventa referência", () => {
  const unknown = summarizePace({
    attempt: { type: "REAL_EDITION", title: "Simulado Edição 2031", status: "IN_PROGRESS", questions: [{ sourceYear: 2031 }] },
    answered: 2,
    total: 40,
    elapsedSeconds: 600,
  });
  assert.equal(unknown.editionYear, 2031);
  assert.equal(unknown.hasReference, false);
  assert.ok(unknown.line.includes("sem tempo oficial confirmado"), unknown.line);
});

ok("zero respondidas não divide por zero; mesmo texto em ESTUDO e PROVA", () => {
  const estudo = summarizePace({ attempt: { ...REAL_2026, mode: "ESTUDO" }, answered: 0, total: 40, elapsedSeconds: 60 });
  const prova = summarizePace({ attempt: { ...REAL_2026, mode: "PROVA" }, answered: 0, total: 40, elapsedSeconds: 60 });
  assert.equal(estudo.averageSecondsPerQuestion, null);
  assert.ok(estudo.line.includes("sem média ainda"), estudo.line);
  assert.equal(estudo.line, prova.line);
});

console.log(`\ntest_pace: ${n} testes verdes.`);
