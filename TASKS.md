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
* explicação;
* conteúdo relacionado.

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
* feedback;
* explicação.

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

# FASE 11 — DADOS E QUALIDADE

## TASK 11.1 — Auditoria do banco de questões

Verificar:

* duplicatas;
* respostas;
* fontes;
* classificações;
* questões anuladas;
* dados incompletos.

---

## TASK 11.2 — Relatório de cobertura

Produzir:

* número de questões por edição;
* disciplina;
* assunto;
* subassunto;
* percentual de classificação confirmada;
* pendências.

---

# FASE 12 — ADMINISTRAÇÃO

## TASK 12.1 — Área administrativa

Criar ambiente administrativo para:

* revisar questões;
* revisar classificações;
* alterar publicação;
* visualizar inconsistências;
* consultar métricas.

---

## TASK 12.2 — Curadoria

Permitir revisão humana das classificações produzidas pela IA.

Status:

PENDING
REVIEWED
APPROVED
REJECTED

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
