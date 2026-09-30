package br.com.voupassar.questions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Envelope de paginação (TASK 3.4).
 *
 * <p>Ordem fixa e determinística (ano-fonte, número, id); {@code page} é
 * 0-based e {@code size} é limitado a {@code [1, 100]}.
 */
@Schema(description = "Página de resultados com ordem fixa (ano-fonte, número, id).")
public record PageResponse<T>(
    List<T> content,
    @Schema(example = "0") int page,
    @Schema(example = "20") int size,
    @Schema(example = "240") long totalElements,
    @Schema(example = "12") int totalPages,
    boolean first,
    boolean last) {}
