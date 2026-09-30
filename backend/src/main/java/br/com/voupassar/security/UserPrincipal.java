package br.com.voupassar.security;

import java.util.List;

/**
 * Identidade autenticada via JWT (TASK 3.5).
 *
 * <p>Principal do {@code SecurityContext}: id + e-mail + papéis + versão
 * credencial vigente no momento da emissão do token. O filtro recusa tokens
 * cuja versão diverge da atual (troca/reset de senha, desativação).
 */
public record UserPrincipal(long userId, String email, List<String> roles, int credentialVersion) {}
