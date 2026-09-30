# Arquitetura — VouPassar

> TASK 0.2 — Definição da arquitetura (TASKS.md). Documento de intenção arquitetural: nada abaixo está implementado (ver `docs/project-inventory.md`). Versões exatas serão fixadas no momento da implementação a partir da documentação oficial; nada de credenciais, domínios, IPs ou secrets consta neste documento (placeholders em `MAIÚSCULAS`).

## 1. Visão geral

```
GitHub (código + dataset docs)
  ↓ GitHub Actions
  ├─→ GitHub Pages ............. Frontend estático (HTML5 + CSS3 + JS ES Modules, sem framework)
  └─→ Imagem Docker ............ Backend (Java + Spring Boot, API REST) → VPS
                                    ↓ JDBC
                                  PostgreSQL (VPS, volume persistente)
```

Princípios (AGENTS.md): evidência antes de opinião; rastreabilidade prova→questão→recomendação; frontend publicável como estático; backend separado e conteinerizado; PostgreSQL como fonte operacional; algoritmo inicial determinístico e explicável; nada de gamificação dominante; mobile-first, leve e acessível.

## 2. Frontend

- **Stack obrigatória:** HTML5 semântico, CSS3 (custom properties, sem framework pesado), JavaScript moderno em **ES Modules** (`js/` organizado por domínio: `api/`, `state/`, `views/`, `components/`), Fetch API, Web APIs padrão. Nenhum bundler obrigatório; se um dia necessário, deve gerar saída estática publicável e ser justificado em ADR.
- **Compatibilidade GitHub Pages:** apenas arquivos estáticos; sem SSR, sem segredos, sem acesso direto ao banco. Toda dinâmica via `fetch()` à API. **URL da API centralizada** em um único `js/config.js` lido de variável de ambiente no build (`API_BASE_URL`), nunca hardcoded em dezenas de arquivos.
- **Páginas previstas (Fase 6):** landing pública, login/cadastro/recuperação, dashboard ("onde estou / o que estudar / como evoluo / próximo passo"), área de estudos, tela de questão (3 modos), simulados, perfil/histórico, admin (Fase 12, protegida por role).
- **Estados obrigatórios por tela:** loading, sucesso, erro, vazio, sem-dados. Respeito a `prefers-reduced-motion`, foco visível, navegação por teclado, contraste WCAG AA, alvos de toque adequados.
- **Performance:** sem bibliotecas desnecessárias; uma fonte (ou sistema), imagens otimizadas com `alt`; JS dividido por página; cache de GETs de conteúdo com invalidação por versão do dataset.

## 3. Backend

- **Stack preferencial (AGENTS.md §5):** Java LTS atual estável + **Spring Boot 3.x** (Web, Validation, Security, Data JPA, Actuator; Flyway para migrations; springdoc-openapi para OpenAPI; testes com JUnit 5 + Mockito + Testcontainers/MockMvc). Versão exata do Java/Spring a confirmar na TASK 3.1 contra a documentação oficial vigente.
- **Organização por domínio** (nunca regra de negócio em controllers):
  `controller → service → repository`, mais `dto/`, `mapper/`, `config/`, `security/`, `exception/` (tratamento global, sem stack trace em produção). Domínios: `exams`, `content` (disciplines/topics), `questions`, `auth/users`, `attempts`, `simulations`, `studyplan` (motor), `admin/curation`, `progress`.
- **API RESTful:** DTOs de entrada/saída, validação Bean Validation, paginação (`page/size/sort`), filtros (`discipline, topic, edition, difficulty, source_type`), códigos HTTP corretos, envelope de erro consistente `{code, message, details, traceId}`, OpenAPI publicada.
- **Endpoints iniciais (Fases 3–5):** `GET /api/v1/editions`, `/editions/{id}`, `/disciplines`, `/topics`, `/questions?filtros`, `/auth/*`, `/attempts`, `/simulations`, `/study-plan`, `/progress`. Questões oficiais × autorais × adaptadas diferenciadas por `source_type`; conteúdo `SOMENTE REFERÊNCIA` nunca exposto no GET público.
- **Restrição de VPS (2 GB RAM / 2 vCPU / 80 GB):** uma réplica do backend com limites explícitos (ex.: `mem_limit` + `-Xmx` conservador a calibrar, ex. ordem de 512M–768M, a validar em carga), PostgreSQL com `shared_buffers` modesto, build da imagem no CI (nunca compilar na VPS), logs em JSON com rotação, Actuator restrito.

## 4. Banco de dados

- **PostgreSQL** na VPS via Docker Compose com volume persistente + backup (Fase 13). **Todas** as mudanças de schema por **migrations versionadas** (Flyway), com constraints, FKs, índices e `created_at/updated_at`.
- **Entidades previstas (AGENTS.md §13, a refinar no ERD da TASK 2.1):** `users, roles, student_profiles, exams, exam_versions, exam_documents, disciplines, topics, subtopics, questions, question_options, question_sources, question_tags, question_attempts, study_sessions, study_plans, study_plan_items, simulations, simulation_questions, simulation_attempts, student_topic_performance, achievements, student_achievements, progress_snapshots` + campos transversais de curadoria: `provenance (edition, document, page, question_number)`, `source_type (OFFICIAL/AUTHORAL/ADAPTED)`, `publication_status (PUBLICAVEL/NAO_PUBLICAVEL/PENDENTE_REVISAO/SOMENTE_REFERENCIA)`, `validation_status`, `classification_confidence`, `annulled` + motivo.
- **Importação idempotente (TASK 2.3):** chave natural `(source_type, source_year, source_question_number, source_document)` com `ON CONFLICT DO NOTHING/UPDATE` + checksum do enunciado; reexecução sem duplicatas; relatório de divergências em vez de delete automático.
- **Detecção de duplicidade (AGENTS.md §23):** normalização (caixa, acentos, espaços) + similaridade gera *suspeitas* para fila de curadoria; nunca apaga automaticamente.

## 5. Autenticação e autorização

- **Fluxo:** JWT `access` curto + `refresh` rotativo (rotação com reuso-detecção quando aplicável), expiração configurável, revogação (denylist de refresh / versão de credencial). Senhas só com hash adaptativo (bcrypt/argon2 — parâmetro a calibrar na VPS fraca), nunca em log. Recuperação por token único de uso único com expiração.
- **Autorização:** RBAC mínimo `STUDENT / CURATOR / ADMIN`; endpoints administrativos e de curadoria exigem role; estudante só acessa seus próprios attempts/planos; questões `NÃO_PUBLICÁVEL/SOMENTE_REFERÊNCIA` exigem `CURATOR+`.
- **Sessão no frontend:** access em memória, refresh em cookie `HttpOnly; Secure; SameSite` (ou armazenamento documentado em ADR se o Pages exigir ajuste); logout invalida refresh no servidor.

## 6. Comunicação e ambientes

| Ambiente | Frontend | Backend | Banco |
|---|---|---|---|
| Local | arquivo estático / servidor simples + `API_BASE_URL=http://localhost:8080` | Spring profile `dev`, Compose local | Postgres em container, Flyway `migrate`, seed mínimo |
| CI | validação + testes + build estático (artefato) | build + testes + imagem (sem push de secret) | Testcontainers/serviço efêmero |
| Produção | GitHub Pages (`https://<USUARIO>.github.io/<REPO>/`) | VPS via Compose: `app + db + reverse-proxy`, HTTPS, `API_BASE_URL=https://<API-PRODUCAO-A-DEFINIR>` | Postgres em volume + backup diário + retenção documentada |

- CORS restritivo (origens exatas do Pages + localhost DEV), HTTPS obrigatório em produção, headers (`HSTS, X-Content-Type-Options, frame-ancestors, Referrer-Policy`), rate limiting em `/auth` e escrita, erros sem stack trace.
- Health: `GET /actuator/health` (interno) + `GET /api/v1/health` público mínimo; logs estruturados com `traceId`; métricas essenciais (latência, 5xx, tentativas/dia) sem PII.

## 7. Motor educacional (determinístico e explicável)

Fases 4–5, implementado **depois** do banco/questões confiáveis (ordem TASKS.md). Versão inicial sem ML: score por assunto = função auditável de `frequência histórica × (1 − taxa de acerto) × peso de dificuldade × decaimento de recência × carência de tentativas`, com motivo textual gerado a partir dos mesmos fatores (ex.: "…aproveitamento abaixo da sua média e tema presente em N edições"). Cada recomendação referencia edições/questões que a sustentam. Modo Estudo (feedback imediato), Modo Prova (sem feedback até finalizar), Modo Revisão (erros/fracos/não-dominados). Simulado por disciplina (filtros compatíveis com o banco) e simulado de edição real **dirigido pela configuração daquela edição** (nunca quantidades universais).

## 8. Segurança, privacidade, qualidade

- **Segurança (Fase 9):** validação de entrada em todas as bordas, JPA parametrizado (sem SQL concatenado), escaping/XSS no frontend (sem `innerHTML` com dados), CSRF avaliado conforme estratégia de cookie, secrets só em ambiente (`.env.example` com placeholders), auditoria de dependências no CI.
- **Privacidade (AGENTS.md §16):** minimização; separar dados do aluno × educacionais × métricas × conteúdo público; documentar finalidade de cada campo no ERD.
- **Testes (estratégia mínima):** backend — unitários (motor, validações), integração (repositories com Testcontainers), API (MockMvc: authz, paginação, filtros), importador idempotente; frontend — lógica crítica (cálculo de desempenho, estados de simulado, validações); pipelines falhando bloqueiam deploy. Cobertura de conteúdo (Fase 11): duplicatas, respostas, fontes, classificações, anuladas, incompletude.
- **Acessibilidade/UX (Fase 8):** auditoria WCAG prática + validação desktop/tablet/mobile + revisão "consigo saber o que estudar e qual o próximo passo?".

## 9. Deploy e observabilidade

- **Frontend (`pages.yml`):** checkout → validação (links, HTML) → testes JS → build (injeta `API_BASE_URL`) → deploy Pages; falha bloqueia publicação.
- **Backend (pipeline separada):** build → testes → imagem Docker → push (registry a definir) → atualização documentada na VPS (SSH/deploy assistido; automação total futura). Nenhum secret no Git; VPS recebe via ambiente gerenciado fora do repo.
- **Observabilidade (TASK 10.3):** health checks no Compose, logs JSON com rotação, endpoint de diagnóstico autenticado, backup/restore PostgreSQL testado periodicamente.

## 10. Decisões (registrar ADRs em `docs/decisions/` a partir da TASK 2.1)

1. Sem framework SPA — Pages + Fetch atende, reduz peso em redes móveis e custo cognitivo.
2. Spring Boot em vez de stack leve — exigência do produto (AGENTS.md) + ecossistema de segurança/validação/migrations.
3. PostgreSQL operacional + `data/extracted/` auditável em repo — banco para consulta, arquivos para auditoria/reprodutibilidade.
4. Curadoria humana obrigatória para classificação pedagógica de baixa confiança (`PENDING → REVIEWED → APPROVED/REJECTED`).

## 11. Pendências explícitas (não decidir sem evidência)

Domínio/URLs finais, provedor de e-mail transacional, parâmetros JWT, retenção de backups, política de redistribuição das questões (jurídico), taxonomia de assuntos (somente após Fase 1), gamificação (Fase 7, após motor). Edição 2021 permanece `DESCONHECIDA` por ausência de fonte.
