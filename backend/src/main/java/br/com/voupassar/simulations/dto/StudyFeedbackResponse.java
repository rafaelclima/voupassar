package br.com.voupassar.simulations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Feedback imediato de uma posição do caderno (TASK 5.2 — Modo Estudo).
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Vale a <b>última</b> tentativa vinculada a esta execução (o fato
 *       {@code question_attempts} é imutável — correção só via nova tentativa,
 *       TASK 3.7); sem resposta, não há feedback (nunca inventado).</li>
 *   <li>O confronto é contra o gabarito <b>congelado</b> ({@code
 *       frozen_answer_key}), nunca contra o gabarito "atual" — consistente com
 *       o placar da TASK 5.1 mesmo após reclassificação posterior.</li>
 *   <li>Anuladas saem com {@code isCorrect} NULL (pontuação DESCONHECIDA, TASK
 *       1.3 §4): contam como conteúdo respondido, nunca como acerto nem erro.</li>
 *   <li>Assunto/subassunto vêm da classificação vigente mais recente.</li>
 *   <li>Enunciado e alternativas NÃO são duplicados aqui: resolve-se via
 *       {@code GET /api/v1/questions/{id}} (TASK 3.4).</li>
 * </ul>
 */
public record StudyFeedbackResponse(
    @Schema(example = "55") long attemptId,
    @Schema(example = "1") int position,
    @Schema(example = "12") long questionId,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    @Schema(example = "2026", nullable = true) Integer sourceYear,
    @Schema(example = "17", nullable = true) Integer sourceQuestionNumber,
    @Schema(example = "C", description = "Última resposta vinculada a esta execução.")
    String selectedOption,
    @Schema(example = "true", nullable = true,
        description = "NULL quando anulada (pontuação DESCONHECIDA).")
    Boolean isCorrect,
    @Schema(description = "true quando anulada no gabarito (congelado ou atual).")
    boolean wasAnnulled,
    @Schema(example = "C", description = "Resposta correta (gabarito congelado na criação).")
    String correctAnswer,
    @Schema(example = "3", nullable = true) Long topicId,
    @Schema(example = "PORCENTAGEM", nullable = true) String topicCode,
    @Schema(example = "Porcentagem", nullable = true) String topicName,
    @Schema(example = "11", nullable = true) Long subtopicId,
    @Schema(example = "JUROS_SIMPLES", nullable = true) String subtopicCode,
    @Schema(example = "Juros simples", nullable = true) String subtopicName,
    @Schema(nullable = true) String classificationConfidence,
    @Schema(nullable = true) String taxonomyVersion,
    List<String> notes) {}
