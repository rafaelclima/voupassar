package br.com.voupassar.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Uma checagem de inconsistência do banco de questões (TASK 12.1).
 *
 * <p>Versão viva das auditorias 11.1/11.2: conta ocorrências atuais e devolve
 * até 20 ids de amostra para investigação. Contagem zero = saudável; qualquer
 * positivo é fila de curadoria, nunca deleção automática (AGENTS.md §23).
 */
public record InconsistencyResponse(
    @Schema(example = "OPTIONS_COUNT") String check,
    @Schema(example = "Questões objetivas com número de alternativas diferente de 4.") String description,
    @Schema(example = "0") long count,
    List<Long> sampleIds) {}
