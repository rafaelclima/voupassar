package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Pedido de recuperação (TASK 3.5). Resposta sempre genérica, exista ou não
 * a conta (anti-enumeração). Entrega do token por e-mail: pendência de
 * infraestrutura (provedor transacional NÃO CONFIRMADO — architecture.md §11).
 */
public record ForgotPasswordRequest(
    @Schema(example = "estudante@exemplo.com")
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email) {}
