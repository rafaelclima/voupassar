package br.com.voupassar.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Perfil completo do aluno (TASK 3.6).
 *
 * <p>Mesmos campos criados no registro (TASK 3.5) + papéis. Sem PII além do
 * necessário: id, e-mail, nome de exibição, ano escolar, ano-alvo e objetivo.
 */
public record ProfileResponse(
    @Schema(example = "1") long id,
    @Schema(example = "estudante@exemplo.com") String email,
    @Schema(example = "Maria da Silva") String displayName,
    @Schema(example = "9º ano") String schoolYear,
    @Schema(example = "2027") Integer targetYear,
    @Schema(example = "Passar no IFRN") String studyGoal,
    @Schema(example = "[\"STUDENT\"]") List<String> roles) {}
