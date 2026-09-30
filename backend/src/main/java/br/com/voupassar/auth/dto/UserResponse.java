package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Conta + perfil mínimo + papéis (sem PII além do necessário). */
public record UserResponse(
    @Schema(example = "1") long id,
    @Schema(example = "estudante@exemplo.com") String email,
    @Schema(example = "Maria da Silva") String displayName,
    @Schema(example = "[\"STUDENT\"]") List<String> roles) {}
