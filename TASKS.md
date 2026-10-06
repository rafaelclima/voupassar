# PORTAL IFRN — ROADMAP DE DESENVOLVIMENTO

## OBJETIVO

Construir uma plataforma web completa de preparação para o processo seletivo dos Cursos Técnicos Integrados do IFRN. Nomeei essa plataforma como "VouPassar".

Arquitetura alvo:

GitHub
↓
GitHub Actions
↓
GitHub Pages
↓
Frontend HTML5/CSS3/JavaScript
↓
API REST
↓
Spring Boot
↓
PostgreSQL
↓
VPS

---

# REGRAS DE EXECUÇÃO

A LLM deve executar as etapas na ordem.

Não avançar para uma task quando uma dependência crítica ainda não estiver concluída.

Uma task só deve ser marcada como DONE após:

* implementação;
* testes;
* validação;
* documentação necessária;
* critérios de aceitação atendidos.

Quando uma task estiver bloqueada por informação inexistente, não inventar a informação.

Registrar o bloqueio em `docs/blockers.md`.

Nunca marcar uma task como concluída apenas porque o código foi criado.

---

# FASE 0 — RECONHECIMENTO DO PROJETO

## TASK 0.1 — Inspeção inicial

Objetivo:

Entender completamente o estado atual do repositório.

Executar:

* listar arquivos;
* analisar estrutura;
* detectar tecnologias;
* detectar configurações;
* localizar provas;
* localizar gabaritos;
* localizar documentação;
* localizar arquivos existentes;
* identificar arquivos potencialmente inúteis;
* identificar configurações de CI/CD existentes.

Entrega:

`docs/project-inventory.md`

Critérios:

* arquitetura atual documentada;
* dataset identificado;
* riscos identificados;
* lacunas identificadas.

---

## TASK 0.2 — Definição da arquitetura

Criar:

`docs/architecture.md`

Documentar:

* frontend;
* backend;
* banco;
* autenticação;
* deploy;
* comunicação;
* ambientes;
* segurança;
* estratégia de testes.

Critérios:

* arquitetura coerente;
* GitHub Pages preservado para frontend;
* backend separado;
* PostgreSQL definido;
* fluxo de deploy documentado.

---

# FASE 1 — ANÁLISE DAS PROVAS

## TASK 1.1 — Inventário das edições

Analisar:

* 2026;
* 2025;
* 2024;
* 2023;
* 2022;
* 2020.

Detectar explicitamente a ausência de 2021.

Criar:

`docs/provas-inventario.md`

Para cada edição registrar:

* arquivos encontrados;
* tipo;
* páginas;
* disciplinas;
* número de questões;
* existência de produção textual;
* existência de gabarito;
* diferenças relevantes;
* pendências.

Critério:

Nenhuma informação pode ser inventada.

---

## TASK 1.2 — Extração das questões

Criar pipeline de extração.

Para cada prova:

* número;
* enunciado;
* alternativas;
* página;
* disciplina;
* documento de origem.

Produzir dados intermediários para auditoria.

Criar:

`data/extracted/`

Nunca sobrescrever a fonte original.

---

## TASK 1.3 — Vinculação com gabaritos

Relacionar cada questão ao respectivo gabarito.

Detectar:

* respostas ausentes;
* questões anuladas;
* divergência entre gabarito preliminar e definitivo;
* inconsistências.

Criar relatório:

`docs/gabaritos-validation.md`

---

## TASK 1.4 — Classificação pedagógica

A partir das questões reais, identificar:

* disciplina;
* assunto;
* subassunto;
* habilidade;
* tipo de raciocínio;
* dificuldade estimada;
* competências relacionadas.

Cada classificação deve possuir:

* evidência;
* origem;
* confiança.

Gerar:

`docs/content-analysis/`

---

## TASK 1.5 — Mapa do conteúdo cobrado

Construir uma análise histórica.

Para cada assunto:

* quantas vezes apareceu;
* em quais edições;
* em quais disciplinas;
* quantidade de questões;
* percentual do total;
* tendência histórica;
* confiança da classificação.

Exemplo:

Matemática
├── Razão e proporção
├── Porcentagem
├── Geometria
├── Álgebra
└── ...

Os tópicos devem vir das provas, não de uma lista inventada.

Criar:

`docs/content-map.md`

---

# FASE 2 — MODELAGEM DE DADOS

## TASK 2.1 — Modelo conceitual

Projetar ERD.

Arquivo:

`docs/database-erd.md`

Definir:

* entidades;
* relacionamentos;
* cardinalidade;
* índices;
* constraints.

---

## TASK 2.2 — Banco PostgreSQL

Implementar:

* migrations;
* tabelas;
* índices;
* constraints;
* timestamps;
* seed inicial.

Critério:

Banco deve subir via Docker Compose.

---

## TASK 2.3 — Importador das questões

Criar processo que converta o dataset analisado para PostgreSQL.

O processo deve ser:

* repetível;
* idempotente;
* auditável.

Executar múltiplas vezes sem duplicar questões.

---

# FASE 3 — BACKEND

## TASK 3.1 — Bootstrap Spring Boot

Criar projeto backend.

Implementar:

* configuração;
* profiles;
* logging;
* tratamento global de erros;
* health endpoint.

---

## TASK 3.2 — API de provas

Endpoints para:

* listar edições;
* consultar edição;
* consultar documentos;
* consultar estatísticas.

---

## TASK 3.3 — API de disciplinas e conteúdos

Endpoints para:

* disciplinas;
* assuntos;
* subassuntos;
* estatísticas históricas.

---

## TASK 3.4 — API de questões

Implementar:

* listagem;
* filtros;
* disciplina;
* assunto;
* edição;
* dificuldade;
* origem;
* paginação.

---

## TASK 3.5 — Autenticação

Implementar:

* registro;
* login;
* logout;
* refresh;
* recuperação de senha;
* alteração de senha;
* validação.

---

## TASK 3.6 — Perfil do aluno

Implementar:

* perfil;
* preferências;
* progresso;
* estatísticas;
* histórico.

---

## TASK 3.7 — Tentativas

Registrar:

* questão;
* resposta;
* resultado;
* tempo;
* data;
* modo;
* simulado quando aplicável.

---

# FASE 4 — MOTOR EDUCACIONAL

## TASK 4.1 — Cálculo de desempenho

Calcular:

* acerto geral;
* acerto por disciplina;
* acerto por assunto;
* acerto por subassunto;
* evolução temporal.

---

## TASK 4.2 — Diagnóstico

Criar diagnóstico inicial.

Entrada:

respostas do aluno.

Saída:

* pontos fortes;
* pontos fracos;
* lacunas;
* assuntos prioritários;
* nível de domínio estimado.

---

## TASK 4.3 — Motor de recomendação

Criar algoritmo transparente para priorização de estudos.

Fatores mínimos:

* frequência histórica;
* desempenho;
* dificuldade;
* recência;
* número de tentativas.

Toda recomendação deve ser explicável.

---

## TASK 4.4 — Roteiro de estudos

Gerar roteiro baseado no diagnóstico.

Estrutura esperada:

* prioridade;
* assunto;
* motivo;
* recomendação de prática;
* revisão;
* progresso.

---

## TASK 4.5 — Modo revisão

Priorizar:

* erros;
* assuntos fracos;
* questões não dominadas;
* conteúdos com baixa retenção.

---

# FASE 5 — SIMULADOS

## TASK 5.1 — Simulado por disciplina

Permitir:

* escolher disciplina;
* escolher quantidade;
* escolher dificuldade;
* iniciar;
* pausar quando aplicável;
* concluir;
* visualizar resultado.

---

## TASK 5.2 — Modo Estudo

Implementar feedback imediato.

Mostrar:

* acerto/erro;
* resposta correta;
* conteúdo relacionado.

Nota 2026-10-04: passo a passo textual removido do escopo por decisão de
produto (plataforma testa conhecimento, não ensina); feedback = gabarito
oficial + assunto.

---

## TASK 5.3 — Modo Prova

Durante a execução:

* ocultar resultado;
* ocultar gabarito;
* preservar estado.

Ao final:

* correção;
* estatísticas;
* análise.

---

## TASK 5.4 — Modo Revisão

Criar experiência voltada exclusivamente para revisão.

---

## TASK 5.5 — Simulado real por edição

Permitir selecionar uma edição real.

O sistema deve reproduzir a estrutura daquela edição.

Não assumir estrutura universal.

Cada edição deverá possuir configuração própria.

---

# FASE 6 — FRONTEND

## TASK 6.1 — Design system

Criar:

* tipografia;
* cores;
* espaçamentos;
* botões;
* inputs;
* cards;
* tabelas;
* badges;
* feedbacks;
* modais;
* estados vazios;
* loading;
* erros.

---

## TASK 6.2 — Landing page

Criar página pública.

Objetivos:

* explicar propósito;
* transmitir confiança;
* demonstrar funcionalidades;
* apresentar proposta de valor;
* direcionar cadastro/login.

---

## TASK 6.3 — Autenticação

Criar:

* login;
* cadastro;
* recuperação;
* redefinição de senha.

---

## TASK 6.4 — Dashboard

O dashboard deve mostrar:

* desempenho;
* progresso;
* assuntos prioritários;
* próximo estudo;
* últimos simulados;
* evolução.

---

## TASK 6.5 — Área de estudos

Implementar:

* conteúdo;
* questões;
* progresso;
* navegação.

---

## TASK 6.6 — Tela de questão

Criar experiência de alta qualidade para:

* leitura;
* seleção;
* confirmação;
* feedback (acerto/erro + resposta correta + assunto, sem passo a passo —
  decisão de produto 2026-10-04).

---

## TASK 6.7 — Simulado

Criar interface específica.

Deve parecer uma experiência de prova real.

---

## TASK 6.8 — Perfil

Mostrar:

* dados;
* estatísticas;
* evolução;
* histórico;
* conquistas;
* metas;
* conteúdos dominados;
* pontos de atenção.

---

## TASK 6.9 — Textos-base junto das questões [DONE — piloto 2020 em 2026-10-03]

Exibir o contexto compartilhado que a questão exige (Texto N, trecho,
tabela, gráfico, imagem) em painel expansível ("Mostrar texto" /
"Mostrar imagem"), colapsado por padrão, nas telas de questão, estudos e
simulado (inclusive Modo Prova — é enunciado, não gabarito).

Entregas:

* `data/passages/<ano>.json` (transcrição literal curada, regra de
  vínculo auditável) + `scripts/db/extract_passages.py` (`--suggest`,
  `--check`) — ver `docs/passagens-estrategia.md`;
* migração `V9__passages.sql` (`passages` + `question_passages`) +
  `scripts/db/import_passages.py` (idempotente, `--check`);
* `QuestionResponse.passages[]` (leitura em lote, sem N+1) + testes;
* `frontend/js/components/passage.js` (`<details>` nativo, tokens do
  design system) ligado em `questao`, `estudos` e `simulado`;
* validadores atualizados (`check_frontend.py`).

Piloto: 2020 com 4 passagens e 30 vínculos (Q11–Q15 validada fim a fim no
navegador). Fases seguintes: demais edições (mesmo pipeline) e recortes
de imagem (fase de figuras).

---

# FASE 7 — GAMIFICAÇÃO

## TASK 7.1 — Pontuação

Definir sistema transparente.

---

## TASK 7.2 — Conquistas

Criar conquistas relacionadas a:

* consistência;
* conclusão;
* evolução;
* domínio de conteúdo.

---

## TASK 7.3 — Sequência de estudos

Implementar streak.

Não transformar isso em mecanismo de pressão excessiva.

---

# FASE 8 — ACESSIBILIDADE E UX

## TASK 8.1 — Auditoria de acessibilidade

Executar auditoria.

Corrigir:

* navegação;
* teclado;
* foco;
* contraste;
* labels;
* semântica;
* leitores de tela;
* mobile.

---

## TASK 8.2 — Responsividade

Validar:

* desktop;
* tablet;
* mobile.

---

## TASK 8.3 — UX review

Revisar o produto como estudante.

Perguntas:

* consigo saber o que estudar?
* consigo começar um exercício sem ajuda?
* entendo meus erros?
* entendo meu desempenho?
* sei o próximo passo?

---

# FASE 8.5 — DARK MODE

## TASK 8.4 — Dark mode harmônico [DONE — 2026-10-03]

* Toggle visível no header de todas as páginas (`landing-page` corrigido).
* `data-theme` aplicado antes da primeira pintura (script inline `<head>`).
* `localStorage` + `prefers-color-scheme` respeitados.
* [2026-10-04] Decisão de produto: padrão alterado para `light`
  (sem seguir `prefers-color-scheme`); escuro só com escolha manual.
  Ver `docs/fracoes-correcao.md` §4.
* `dark.css` carregado dinamicamente; `tokens.css` com `[data-theme="dark"]` completo.
* Nenhum componente visual quebrado (validado estático + navegador real).

Objetivo:

* Permitir que o usuário alterne entre tema claro e escuro.
* Usar CSS variables existentes (`tokens.css`) para cores, tipografia e componentes.
* Preservar contraste, acessibilidade e harmonia visual.
* Não quebrar o design system existente (`base.css`, `components.css`, `landing.css`, etc.).
* Incluir toggle visível no header (todas as páginas).
* Respeitar `prefers-color-scheme` quando nenhuma preferência manual estiver definida.
* Persistir preferência (localStorage) sem expor dados sensíveis.

Critério:

* Todas as páginas (`frontend/*.html`) renderizam corretamente no modo claro e escuro.
* Nenhum componente (`btn`, `card`, `table`, `badge`, `input`, `select`, `progress`, `alert`) quebra visualmente.
* Contraste mínimo respeitado em ambos os modos.
* Responsividade mantida.
* Nenhum arquivo de código existente destruído sem motivo técnico documentado.

---

# FASE 9 — SEGURANÇA

## TASK 9.1 — Auditoria de segurança

Verificar:

* autenticação;
* autorização;
* CORS;
* validação;
* SQL injection;
* XSS;
* CSRF quando aplicável;
* exposição de secrets;
* logs;
* rate limiting.

---

# FASE 10 — CI/CD

## TASK 10.1 — GitHub Actions frontend

Pipeline:

* checkout;
* instalação;
* validação;
* testes;
* build;
* deploy GitHub Pages.

---

## TASK 10.2 — Pipeline backend

Preparar:

* build;
* testes;
* imagem Docker;
* publicação;
* deploy VPS.

A solução deve permitir futura automação total.

---

## TASK 10.3 — Observabilidade

Implementar:

* health check;
* logs estruturados;
* métricas essenciais;
* endpoint de diagnóstico.

---

# FASE 11 — DADOS E QUALIDADE (auditorias executadas 2026-10-02; ver docs/auditoria-questoes.md e docs/relatorio-cobertura.md)

## TASK 11.1 — Auditoria do banco de questões [HISTÓRICO]

Verificou (estado 2026-10-02, contrato então vigente):
* duplicatas;
* respostas;
* fontes;
* classificações;
* questões anuladas;
* dados incompletos.

---

## TASK 11.2 — Relatório de cobertura [HISTÓRICO]

Produziu (estado 2026-10-02):

* número de questões por edição;
* disciplina;
* assunto;
* subassunto;
* ~~percentual de classificação confirmada~~ (métrica do contrato removido em 2026-10-06);
* pendências.

---

# FASE 12 — ADMINISTRAÇÃO (redefinida em 2026-10-06: observabilidade, sem curadoria — ver docs/plano-remocao-curadoria.md)

## TASK 12.1 — Área administrativa [REDEFINIDA]

Ambiente administrativo **somente leitura** para:

* visualizar inconsistências;
* consultar métricas.

Sem fila de revisão, sem PATCH de status, sem publicação por status.

## TASK 12.2 — Curadoria [REMOVIDA DO CONTRATO em 2026-10-06]

A revisão humana das classificações foi removida do produto. Registro
histórico da operação assistida de 2026-10-02 em `docs/curadoria.md`.
Os status abaixo não existem mais no banco (V12), na API nem no frontend:

~~PENDING~~ · ~~REVIEWED~~ · ~~APPROVED~~ · ~~REJECTED~~

---

# FASE 13 — PRODUÇÃO

## TASK 13.1 — Ambiente de produção

Documentar:

* domínio;
* HTTPS;
* frontend;
* backend;
* banco;
* backups;
* secrets.

Nunca inserir credenciais reais no Git.

---

## TASK 13.2 — Backup

Implementar estratégia de:

* backup PostgreSQL;
* retenção;
* restauração;
* validação periódica.

---

## TASK 13.3 — Pré-lançamento

Checklist completo.

Validar:

* autenticação;
* questões;
* simulados;
* diagnóstico;
* roteiro;
* desempenho;
* perfil;
* responsividade;
* acessibilidade;
* segurança;
* deploy.

---

# FASE 14 — DOCUMENTAÇÃO FINAL

Criar:

`README.md`

O README deve explicar:

* propósito;
* arquitetura;
* instalação;
* desenvolvimento;
* banco;
* ambiente;
* deploy;
* contribuição;
* estrutura das provas;
* pipeline de análise;
* limitações conhecidas.

---

# FASE 15 — QUESTÕES AUTORAIS (lote piloto 60)

> Decisão de produto 2026-10-06: expandir o banco com questões não oficiais
> derivadas do perfil observado nas 6 edições, sem fila de curadoria humana —
> o gate é o veredito do pipeline (validadores determinísticos + checksum).
> Autoral consome evidência oficial, nunca a produz: `content-map` e
> `evidence_json` do roteiro seguem 100% oficiais. Simulado de edição real
> segue 100% oficial. Textos-base de LP autorais são inéditos (redação
> própria), sem copiar IFRN ou terceiros. Produção textual fora do escopo.

## TASK 15.1 — Manual do item + validadores + 10 itens-teste [DONE — 2026-10-06, aprovado pelo responsável]

Elaborar:

* `docs/authoral/manual-item-v1.md` (molde por subassunto + checklist de distrator);
* `scripts/authoral/validate_authoral.py` (`--check`, falha alta);
* `data/authoral/specs/` com 10 itens-teste (5 MAT: REGRA_DE_TRES,
  FUNCAO_AFIM, EQUACOES, CALCULO_DIRETO, MEDIA; 5 LP: INFERENCIA,
  INFORMACAO_EXPLICITA, PONTUACAO, COESAO_REFERENCIA, MORFOLOGIA).

Cada spec contém: `discipline, topic, subtopic, skill, reasoning_type,
difficulty_alvo, referencias_oficiais (IDs), texto_base próprio quando LP,
enunciado, options[4], answerKey (A–D, nunca X), distractor_rationale[3]`.

Validadores exigem: enunciado com comando; 4 opções distintas não-vazias;
códigos v1.1 válidos com par topic↔subtopic coerente; checksum SHA-256
(mesma normalização do importador) distinto dos 240 oficiais e dentro do
lote; ausência das strings `IFRN/Edital/prova 20XX`.

Critérios:

* 10/10 specs verdes no `--check`;
* 0 colisão de checksum contra os 240 oficiais;
* parar para aprovação do responsável antes da 15.2.

## TASK 15.2 — Lote piloto 60 (LLM + validadores) [DONE — 2026-10-06]

Gerar via LLM travada (temperatura baixa, schema do spec, 2–3 oficiais
como molde de forma) e validar: MAT 30 (REGRA_DE_TRES 6, FUNCAO_AFIM 5,
EQUACOES 5, CALCULO_DIRETO 5, MEDIA 4, PROBABILIDADE 3, AREA_PLANA 2) +
LP 30 (INFERENCIA 8, INFORMACAO_EXPLICITA 6, PONTUACAO 5,
COESAO_REFERENCIA 4, MORFOLOGIA 4, INTENCAO_COMUNICATIVA 3).

Fora do piloto: GRANDEZAS_MEDIDAS, SISTEMAS_NUMERACAO, VOLUME/PERIMETRO/
UNIDADES (n=1), JUROS_COMPOSTOS/DESCONTO (0 observados), itens com figura.

Critérios:

* 60/60 verdes nos validadores; rejeitados retornam com motivo, sem edição
  manual silenciosa;
* relatório `docs/authoral/report-60.md` (por assunto/subassunto/dificuldade
  + taxa de reprovação nos validadores).

## TASK 15.3 — Pipeline V13 + importador de autorais [DONE — 2026-10-06]

> Nome do arquivo ajustado de `V12__` para `V13__authoral_pipeline.sql`
> (V12 ocupada pela remoção da curadoria).

Criar:

* migração `V12__authoral_pipeline.sql` (`questions.pipeline_version NOT
  NULL`, `questions.pipeline_verified_at`; autorais com `exam_id/
  exam_document_id/source_year/source_number` NULL — CHECK atual já permite);
* `scripts/db/import_authoral.py` (`--check`, `--report
  data/authoral/report.json`, idempotente por checksum);
* classificação da autoral: 1 linha `taxonomy_version='v1.1'`,
  `origin='AUTORAL_DERIVADA_PERFIL'`.

Critérios:

* `import_authoral.py --check` verde em dev; reexecução sem duplicar;
* `GET /questions?sourceType=AUTHORAL` lista as 60; `OFFICIAL` intacto (240).

## TASK 15.4 — Vitrine Estudo + simulado (toggle) [DONE — 2026-10-06]

Expor:

* filtro Origem em Estudos (Todos/Oficial/Autoral, selo "Criada pelo
  VouPassar" via `vocab.js`);
* toggle no setup do simulado por disciplina (default `OFFICIAL`);
* nota fixa de origem em `QuestionResponse.notes[]`.

Critérios:

* edição real intocada (sem toggle);
* `node --check`, `check_frontend.py`, serve 200 + navegador real
  (filtro, simulado misto, selo, mobile, console limpo).

---

# CRITÉRIO GLOBAL DE CONCLUSÃO

O projeto só pode ser considerado MVP quando o seguinte fluxo funcionar de ponta a ponta:

USUÁRIO
↓
CADASTRO
↓
LOGIN
↓
DIAGNÓSTICO
↓
ANÁLISE DO DESEMPENHO
↓
RECOMENDAÇÃO
↓
ROTEIRO DE ESTUDOS
↓
QUESTÕES
↓
SIMULADO
↓
RESULTADO
↓
ANÁLISE DE ERROS
↓
ATUALIZAÇÃO DO PERFIL
↓
NOVAS RECOMENDAÇÕES

O produto deve estar funcional tanto no frontend quanto no backend e banco.

---

# ORDEM DE PRIORIDADE

Priorizar nesta ordem:

1. qualidade e confiabilidade do dataset;
2. arquitetura;
3. banco;
4. backend;
5. motor educacional;
6. frontend;
7. simulados;
8. acompanhamento;
9. gamificação;
10. refinamento visual;
11. infraestrutura de produção.

Nunca sacrificar a confiabilidade do conteúdo para entregar interface mais rapidamente.
