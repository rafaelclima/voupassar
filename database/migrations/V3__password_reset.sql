-- V3__password_reset.sql — TASK 3.5 (Autenticação: recuperação de senha)
-- Tabela password_reset_tokens: token único de uso único com expiração
-- (docs/architecture.md §5). Não existia na V1; segue as convenções do ERD:
-- PK surrogate BIGINT, created_at/updated_at + trigger, hash SHA-256
-- (nunca o token em claro), índice parcial para validação de reset ativo.
-- Provedor de e-mail transacional: NÃO CONFIRMADO — o token é devolvido
-- apenas via fluxo autenticado de entrega futura; a API responde genérico
-- (anti-enumeração). Ver docs/api-auth.md.

CREATE TABLE password_reset_tokens (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash TEXT NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ NULL,
  created_ip TEXT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (expires_at > created_at),
  CHECK (used_at IS NULL OR used_at >= created_at)
);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id, expires_at)
  WHERE used_at IS NULL;

CREATE TRIGGER trg_password_reset_updated
  BEFORE UPDATE ON password_reset_tokens
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
