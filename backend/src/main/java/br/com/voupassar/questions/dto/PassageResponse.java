package br.com.voupassar.questions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Texto-base compartilhado de uma questão oficial (TASK 6.9,
 * docs/passagens-estrategia.md). Transcrição literal do caderno; {@code
 * content} nulo = conteúdo puramente visual (ver {@code visualDescription}).
 * Parte do enunciado: exibido também no Modo Prova (não é gabarito).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Texto-base da questão (transcrição literal do caderno oficial).")
public record PassageResponse(
    @Schema(example = "Texto 1", description = "Rótulo como citado no enunciado.")
        String label,
    @Schema(example = "TEXTO", description = "TEXTO, TRECHO, TABELA, GRAFICO, IMAGEM, CHARGE ou TIRINHA.")
        String kind,
    @Schema(description = "Título do texto-base, quando houver.") String title,
    @Schema(description = "Autoria/veículo/data, quando houver.") String byline,
    @Schema(description = "Linha fina do texto-base, quando houver.") String subtitle,
    @Schema(description = "Cabeçalho do bloco (ex. trechos), quando houver.") String intro,
    @Schema(description = "Transcrição literal; nula quando puramente visual.") String content,
    @Schema(description = "Descrição da curadoria quando o conteúdo é visual.") String visualDescription,
    @Schema(description = "Nota de formatação do caderno (moldura, sublinhados).") String formatNote,
    @Schema(description = "Fonte/legenda impressa no caderno.") String sourceNote,
    @Schema(example = "2", description = "Primeira página do caderno onde aparece.") Integer pageStart,
    @Schema(example = "2", description = "Última página do caderno onde aparece.") Integer pageEnd) {}
