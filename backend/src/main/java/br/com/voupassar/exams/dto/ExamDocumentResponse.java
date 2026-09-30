package br.com.voupassar.exams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Documento-fonte com a versão a que pertence. */
@Schema(description = "Documento-fonte (caderno, gabarito). file_name é literal no repo.")
public record ExamDocumentResponse(
    @Schema(example = "11") long id,
    @Schema(example = "CADERNO") String kind,
    @Schema(example = "data/provas/2026/Caderno_de_Provas_-Técnico_Integrado_2026.pdf")
        String fileName,
    @Schema(description = "SHA-256 auditado (docs/gabaritos-validation.md §0).")
        String sha256,
    @Schema(example = "14") int pages,
    @Schema(nullable = true, example = "PDF24") String generator,
    @Schema(nullable = true) String note,
    @Schema(example = "DEFINITIVO") String versionCode) {}
