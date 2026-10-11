package br.com.voupassar.aiprova;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Isolamento do caderno-IA no nível do DDL (TASK A.2).
 *
 * <p>Prova estrutural, sem banco: a migração do caderno efêmero não pode
 * criar nenhum caminho de escrita/leitura por FK entre as tabelas-IA e o
 * acervo oficial ({@code questions}, {@code question_options},
 * {@code question_sources}) — TASKS.md regra 3 do programa. A exclusão das
 * tabelas-IA nos serviços educacionais (diagnóstico, roteiro, revisão,
 * performance, content-map) é travada e testada na B.1; aqui trava-se a
 * base: o DDL.
 */
class AiProvaIsolationTest {

  private static String v23;

  @BeforeAll
  static void loadMigration() throws Exception {
    Path dir = Path.of("").toAbsolutePath();
    while (dir != null && !Files.isDirectory(dir.resolve("database/migrations"))) {
      dir = dir.getParent();
    }
    assertTrue(dir != null, "database/migrations não encontrado a partir do cwd");
    Path file = dir.resolve("database/migrations/V23__ai_generations.sql");
    assertTrue(Files.isRegularFile(file), "V23__ai_generations.sql ausente em database/migrations");
    v23 = Files.readString(file);
  }

  @Test
  void createsEphemeralNotebookTables() {
    assertTrue(v23.contains("CREATE TABLE ai_generations"),
        "ai_generations ausente na V23");
    assertTrue(v23.contains("CREATE TABLE ai_generation_questions"),
        "ai_generation_questions ausente na V23");
    assertTrue(v23.contains("CREATE TABLE ai_generation_answers"),
        "ai_generation_answers ausente na V23");
  }

  @Test
  void noForeignKeyFromAiTablesToOfficialAcervo() {
    Matcher m = Pattern.compile(
        "REFERENCES\\s+(questions|question_options|question_sources)\\b").matcher(v23);
    assertFalse(m.find(), "V23 com REFERENCES para o acervo oficial (isolamento violado no DDL)");
  }

  @Test
  void noSeedOrWriteIntoOfficialAcervo() {
    Matcher m = Pattern.compile(
        "INSERT\\s+INTO\\s+(questions|question_options|question_sources)\\b",
        Pattern.CASE_INSENSITIVE).matcher(v23);
    assertFalse(m.find(), "V23 escreve em tabela do acervo oficial");
  }

  @Test
  void aiTablesCarryNoQuestionId() {
    assertFalse(v23.contains("question_id"),
        "V23 menciona question_id: resposta-IA deve referenciar posição congelada, nunca questão oficial");
  }

  @Test
  void simulationTypeAndLinkForAi() {
    assertTrue(v23.contains("AI_GENERATED_EDITION"),
        "V23 sem o type AI_GENERATED_EDITION para simulations");
    assertTrue(v23.contains("ai_generation_id"),
        "V23 sem o vínculo simulation ↔ ai_generation");
    assertTrue(v23.contains("chk_simulations_type") && v23.contains("chk_simulations_link"),
        "V23 sem as CHECKs nomeadas de simulations");
  }

  @Test
  void frozenAnswerKeyWithoutAnnulled() {
    assertTrue(v23.contains("frozen_answer_key"),
        "V23 sem gabarito congelado em ai_generation_questions");
    assertFalse(v23.contains("was_annulled"),
        "IA nunca tem anulada X: was_annulled não pertence às tabelas-IA");
  }

  @Test
  void answersAreImmutableFact() {
    assertTrue(v23.contains("trg_ai_answers_immutable"),
        "V23 sem trigger de imutabilidade em ai_generation_answers");
    assertTrue(v23.contains("uq_ai_generations_busy"),
        "V23 sem o índice parcial de 1 geração em voo por aluno");
  }

  @Test
  void dbEditionVocabularyMatchesValidator() {
    Matcher m = Pattern.compile(
        "edition_model IN\\s*\\((.*?)\\)", Pattern.DOTALL).matcher(v23);
    assertTrue(m.find(), "CHECK de edition_model ausente na V23");
    Set<String> inDb = new HashSet<>();
    Matcher q = Pattern.compile("'([A-Z0-9-]+)'").matcher(m.group(1));
    while (q.find()) {
      inDb.add(q.group(1));
    }
    assertEquals(AiProvaSpec.MODELS.keySet(), inDb,
        "vocabulário de edition_model no banco diverge de AiProvaSpec.MODELS (duas fontes da verdade)");
  }
}
