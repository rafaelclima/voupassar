package br.com.voupassar.questions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Questão do banco com proveniência e classificação vigente (TASK 3.4),
 * mais ocultação do Modo Prova (TASK 5.3).
 *
 * <p>Regras de evidência (AGENTS.md §§2, 4, 11, 12):
 * <ul>
 *   <li>{@code answerKey = "X"} ⟺ anulada: contou como conteúdo, nunca pontua
 *       (regra de pontuação DESCONHECIDA).</li>
 *   <li>{@code explanation = null} = correção ainda não redigida (nunca
 *       inventada) — ver {@code notes} — ou gabarito oculto em {@code PROVA}
 *       em andamento (TASK 5.3, com nota explícita).</li>
 *   <li>{@code difficultyEstimate} é palpite com confiança BAIXA global.</li>
 *   <li>Assunto/subassunto vêm da classificação não-rejeitada mais recente
 *       (revisão humana PENDENTE); nulos = assunto NÃO CONFIRMADO.</li>
 *   <li>{@code publicationStatus} em todas as importadas é
 *       {@code PENDENTE_REVISAO} (curadoria TASK 12.2).</li>
 *   <li>Em simulado {@code PROVA} {@code IN_PROGRESS} deste aluno, {@code
 *       answerKey} e {@code explanation} saem NULL (ocultos até encerrar).</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Questão com enunciado, alternativas, gabarito e proveniência.")
public record QuestionResponse(
    @Schema(example = "1") Long id,
    @Schema(example = "OFFICIAL", description = "OFFICIAL = prova real; demais valores nunca são do IFRN.")
        String sourceType,
    @Schema(example = "2020", description = "Ano-fonte; nulo quando a origem não é uma edição.")
        Integer examYear,
    @Schema(example = "1", description = "Número na prova-fonte; nulo fora de OFFICIAL.")
        Integer questionNumber,
    DisciplineRef discipline,
    String statement,
    List<QuestionOptionResponse> options,
    @Schema(example = "A", description = "A–D ou X (anulada).") String answerKey,
    @Schema(description = "Anulada: X no gabarito.") boolean annulled,
    @Schema(example = "MEDIA", description = "Estimativa, confiança BAIXA; nula quando NÃO CONFIRMADA.")
        String difficultyEstimate,
    @Schema(description = "Correção redigida; nula quando ainda não redigida.") String explanation,
    @Schema(example = "3") Integer pageStart,
    @Schema(example = "3") Integer pageEnd,
    @Schema(description = "Enunciado com figura no PDF-fonte.") boolean hasFigure,
    TopicRef topic,
    SubtopicRef subtopic,
    @Schema(example = "ALTA") String classificationConfidence,
    @Schema(example = "v1.1") String taxonomyVersion,
    @Schema(example = "PENDING") String classificationStatus,
    @Schema(example = "PENDING") String validationStatus,
    @Schema(example = "PENDENTE_REVISAO") String publicationStatus,
    @Schema(description = "Notas de evidência/auditoria desta questão.") List<String> notes,
    @Schema(description = "Figuras oficiais associadas (caminho + metadados). Vazio quando sem recorte publicado.")
        java.util.List<FigureResponse> figures) {}
