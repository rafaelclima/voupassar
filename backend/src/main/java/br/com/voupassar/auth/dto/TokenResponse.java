package br.com.voupassar.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Par de tokens (TASK 3.5). Access JWT curto em memória no cliente; refresh
 * opaco com rotação. Sem cookie neste MVP (frontend Pages × API em origens
 * distintas — ADR futura quando o domínio de produção existir).
 */
public record TokenResponse(
    @Schema(description = "Access token JWT (usar como `Authorization: Bearer ...`).")
        String accessToken,
    @Schema(example = "Bearer") String tokenType,
    @Schema(description = "Segundos até expirar o access token.", example = "900")
        long expiresIn,
    @Schema(description = "Refresh token opaco (uso único — cada refresh devolve outro).")
        String refreshToken,
    UserResponse user) {}
