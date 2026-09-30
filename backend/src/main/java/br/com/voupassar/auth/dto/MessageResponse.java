package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Confirmação genérica (logout, forgot, reset, change). */
public record MessageResponse(@Schema(example = "Operação concluída.") String message) {}
