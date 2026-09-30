## 1. IDENTIDADE DO PROJETO

Este repositório contém uma plataforma educacional web dedicada exclusivamente à preparação de estudantes para o processo seletivo dos Cursos Técnicos de Nível Médio na Forma Integrada do Instituto Federal de Educação, Ciência e Tecnologia do Rio Grande do Norte (IFRN).

O objetivo do projeto é ajudar estudantes que estão concluindo ou concluíram o Ensino Fundamental a:

* entender o perfil das provas do IFRN;
* descobrir quais conteúdos são efetivamente mais cobrados;
* identificar suas próprias lacunas de conhecimento;
* seguir um roteiro de estudos orientado por evidências;
* praticar questões reais e questões de apoio;
* realizar simulados por disciplina;
* realizar simulados que reproduzam o formato de uma edição real;
* receber feedback imediato ou somente ao final;
* acompanhar sua evolução individual;
* utilizar a plataforma em computador e dispositivos móveis.

O projeto deve ser desenvolvido como um produto educacional sério, confiável, moderno, acessível e tecnicamente bem estruturado.

---

# 2. PRINCÍPIO FUNDAMENTAL DO PROJETO

## EVIDÊNCIA ANTES DE OPINIÃO

As provas reais do IFRN presentes no repositório são a principal fonte de evidência para determinar:

* disciplinas;
* conteúdos;
* subconteúdos;
* habilidades;
* tipos de questões;
* distribuição de assuntos;
* frequência de cobrança;
* padrões de dificuldade;
* tipos de raciocínio exigidos;
* recorrência histórica;
* evolução do perfil da prova.

A aplicação NÃO deve inventar um conteúdo programático simplesmente porque ele é comum no Ensino Fundamental.

Todo conteúdo utilizado para gerar recomendações deverá possuir rastreabilidade.

Sempre que possível, deve ser possível responder:

> "Por que a plataforma está dizendo que o aluno precisa estudar este assunto?"

A resposta deve apontar para questões e edições de prova que sustentem a recomendação.

---

# 3. FONTES PRIMÁRIAS

O repositório possuirá diretórios contendo provas e gabaritos oficiais de diferentes edições do IFRN.

Estrutura inicial esperada:

data/
provas/
2026/
2025/
2024/
2023/
2022/
2020/

Cada edição pode possuir:

* caderno de prova;
* gabarito preliminar;
* gabarito definitivo;
* eventuais documentos complementares;
* eventualmente documentos de produção textual.

Não assumir que todas as edições possuem exatamente a mesma estrutura.

Não assumir que todas possuem a mesma quantidade de questões.

Não assumir que todas possuem as mesmas disciplinas.

Não assumir que todas possuem o mesmo formato.

A estrutura da prova deve ser inferida a partir da documentação daquela edição.

A edição de 2021 não está presente no dataset inicial. Essa ausência deve ser documentada e jamais preenchida com informações inventadas.

---

# 4. REGRA DE OURO PARA O CONTEÚDO

Nunca inventar:

* questão;
* alternativa;
* gabarito;
* assunto cobrado;
* frequência de cobrança;
* dificuldade;
* explicação;
* formato da prova;
* regra de pontuação;
* característica de uma edição.

Quando uma informação não puder ser comprovada pelo material disponível, marcar como:

* DESCONHECIDA;
* NÃO CONFIRMADA;
* NECESSITA REVISÃO;
* ou semelhante.

Nunca transformar inferência em fato.

A análise feita pela IA deve distinguir claramente:

1. informação encontrada diretamente na fonte;
2. classificação pedagógica derivada da fonte;
3. inferência estatística;
4. informação proveniente de documentação oficial externa.

---

# 5. ARQUITETURA GERAL

O sistema será dividido conceitualmente em:

frontend/
backend/
data/
docs/
scripts/
.github/

## Frontend

Tecnologias obrigatórias:

* HTML5;
* CSS3;
* JavaScript moderno;
* módulos ES;
* Web APIs padrão;
* Fetch API.

O frontend deverá ser compatível com GitHub Pages.

Não utilizar frameworks pesados sem uma justificativa técnica explícita.

O frontend deve produzir arquivos estáticos publicáveis.

A interface deve ser responsiva e mobile-first.

## Backend

Tecnologia preferencial:

* Java;
* Spring Boot;
* API REST;
* autenticação;
* autorização;
* validação;
* tratamento global de erros;
* logs;
* testes automatizados.

Utilizar versões atuais e estáveis das tecnologias no momento da implementação, validando a documentação oficial.

## Banco

* PostgreSQL;
* migrations;
* índices;
* constraints;
* foreign keys;
* integridade referencial;
* timestamps;
* auditoria quando necessário.

## Infraestrutura

O backend deve ser executável na VPS por meio de containers.

Preferir:

* Docker;
* Docker Compose;
* reverse proxy;
* HTTPS;
* variáveis de ambiente;
* health checks.

Não armazenar segredos no Git.

---

# 6. PRINCÍPIOS DE DESIGN

O produto deve transmitir:

* seriedade;
* confiança;
* organização;
* modernidade;
* clareza;
* qualidade;
* simplicidade.

Evitar aparência infantil.

O público é jovem, mas o produto não deve parecer um aplicativo de desenho animado.

Evitar:

* excesso de cores;
* excesso de efeitos;
* excesso de gradientes;
* animações desnecessárias;
* interfaces visualmente carregadas;
* gamificação exagerada.

A gamificação deve complementar o estudo, jamais dominar a experiência.

Priorizar:

* excelente tipografia;
* hierarquia visual;
* espaçamento consistente;
* componentes reutilizáveis;
* feedback visual claro;
* acessibilidade;
* leitura confortável;
* navegação intuitiva.

---

# 7. EXPERIÊNCIA DO ALUNO

O aluno deve conseguir:

1. criar sua conta;
2. entrar na plataforma;
3. preencher seu perfil;
4. realizar diagnóstico;
5. visualizar seu desempenho;
6. visualizar conteúdos recomendados;
7. iniciar estudos;
8. responder questões;
9. fazer simulados;
10. revisar erros;
11. acompanhar evolução;
12. consultar histórico.

O dashboard deve responder rapidamente:

* Onde estou?
* O que preciso estudar?
* Como estou evoluindo?
* O que devo fazer agora?

---

# 8. ROTEIRO DE ESTUDOS

O roteiro deve ser baseado no conjunto de evidências existente no banco.

A recomendação deve considerar pelo menos:

* frequência histórica do assunto;
* desempenho do aluno;
* número de tentativas;
* taxa de acerto;
* recência do desempenho;
* dificuldade das questões;
* importância daquele assunto dentro da matriz observada nas provas.

Não criar um algoritmo complexo prematuramente.

Começar com algoritmo determinístico, transparente e auditável.

O sistema deve conseguir explicar a recomendação.

Exemplo conceitual:

"Você precisa reforçar porcentagem porque seu aproveitamento neste assunto está abaixo da sua média e o tema aparece em múltiplas edições analisadas."

Nunca apresentar uma recomendação como verdade absoluta.

---

# 9. MODOS DE QUESTÕES

A plataforma deverá suportar três experiências principais.

## Modo Estudo

Após cada resposta:

* informar imediatamente se acertou;
* informar a alternativa correta;
* apresentar explicação;
* explicar o raciocínio;
* apresentar assunto;
* permitir avançar.

## Modo Prova

Durante a resolução:

* não revelar resultado;
* não revelar resposta correta;
* não revelar desempenho acumulado;
* preservar sensação de prova.

Ao finalizar:

* apresentar resultado;
* apresentar correções;
* apresentar desempenho por disciplina;
* apresentar desempenho por assunto;
* permitir revisar cada questão.

## Modo Revisão

Priorizar:

* questões erradas;
* assuntos fracos;
* questões ainda não dominadas;
* conteúdos com baixa taxa de acerto.

---

# 10. SIMULADOS

Devem existir pelo menos dois conceitos:

## Simulado por disciplina

Exemplo:

* Língua Portuguesa;
* Matemática.

O sistema deverá permitir selecionar quantidade de questões e critérios compatíveis com o banco disponível.

## Simulado de edição real

Esse tipo de simulado deve reproduzir uma edição real do IFRN.

A estrutura deve ser orientada pelos dados daquela edição.

Nunca assumir que todas as edições são iguais.

Exemplo: a prova oficial de 2026 possui 20 questões de Língua Portuguesa, 20 de Matemática e uma produção textual escrita. Essa configuração pertence àquela edição e não deve ser transformada em regra universal.

---

# 11. QUESTÕES REAIS E QUESTÕES AUTORAIS

O sistema deve distinguir claramente:

* questão oficial real;
* questão autoral;
* questão adaptada;
* questão de revisão interna;
* questão experimental.

Questões oficiais devem possuir metadados de origem.

Exemplo:

source_type = OFFICIAL
source_year = 2026
source_question_number = 17
source_document = ...
source_page = ...

Questões autorais devem possuir indicação explícita de que não são questões oficiais.

Nunca apresentar questão criada pela IA como sendo do IFRN.

---

# 12. PROVENIÊNCIA E DIREITOS

Toda questão importada deve possuir informações de proveniência.

O sistema deve armazenar:

* origem;
* edição;
* documento;
* página;
* número da questão;
* status de validação;
* status de publicação.

A existência de um documento disponível publicamente não deve ser interpretada automaticamente como autorização irrestrita para redistribuição.

Criar um mecanismo que permita marcar conteúdo como:

* PUBLICÁVEL;
* NÃO PUBLICÁVEL;
* PENDENTE DE REVISÃO;
* SOMENTE REFERÊNCIA.

O desenvolvimento deve permitir que o banco contenha a análise de uma questão mesmo quando a publicação do conteúdo completo estiver pendente de revisão.

---

# 13. MODELO DE DADOS

O banco deve ser projetado de forma normalizada.

Entidades esperadas, podendo ser refinadas durante a análise:

* users;
* roles;
* student_profiles;
* exams;
* exam_versions;
* exam_documents;
* disciplines;
* topics;
* subtopics;
* questions;
* question_options;
* question_sources;
* question_tags;
* question_attempts;
* study_sessions;
* study_plans;
* study_plan_items;
* simulations;
* simulation_questions;
* simulation_attempts;
* student_topic_performance;
* achievements;
* student_achievements;
* progress_snapshots.

Não criar tabelas simplesmente porque parecem úteis.

Cada entidade deve ter finalidade clara.

---

# 14. AUTENTICAÇÃO

O backend deverá possuir autenticação real.

Avaliar implementação baseada em:

* JWT;
* access token;
* refresh token;
* expiração;
* rotação;
* revogação quando aplicável.

Senhas devem ser armazenadas somente em formato seguro utilizando algoritmo apropriado de hashing.

Nunca armazenar senha em texto puro.

Nunca enviar senha para logs.

---

# 15. SEGURANÇA

Aplicar pelo menos:

* validação de entrada;
* CORS restritivo;
* proteção contra SQL Injection;
* controle de autorização;
* rate limiting onde fizer sentido;
* proteção de endpoints administrativos;
* gestão segura de secrets;
* headers HTTP apropriados;
* HTTPS;
* tratamento seguro de erros.

Nunca retornar stack trace para o cliente em produção.

---

# 16. PRIVACIDADE

Dados de estudantes são dados pessoais.

Minimizar coleta de informações.

Não armazenar informações que não tenham finalidade clara.

Criar documentação de quais dados são armazenados e por quê.

Separar claramente:

* dados do aluno;
* dados educacionais;
* métricas de desempenho;
* conteúdo público.

---

# 17. ACESSIBILIDADE

O frontend deve perseguir conformidade prática com WCAG.

Aplicar:

* HTML semântico;
* labels;
* navegação por teclado;
* foco visível;
* contraste adequado;
* textos alternativos;
* mensagens de erro compreensíveis;
* aria apenas quando realmente necessário;
* respeito à preferência de redução de movimento;
* tamanhos adequados para uso móvel.

A acessibilidade não deve ser uma etapa esquecida no final.

---

# 18. PERFORMANCE

O site deve ser leve.

Evitar:

* bibliotecas desnecessárias;
* imagens gigantes;
* fontes excessivas;
* JavaScript monolítico;
* requisições desnecessárias;
* renderizações repetidas.

O frontend deve funcionar bem também em equipamentos modestos e redes móveis.

---

# 19. ESTRUTURA DO CÓDIGO

Preferir organização por domínio/feature em vez de arquivos gigantes.

Não criar:

* `script.js` com milhares de linhas;
* componentes gigantes;
* serviços misturados com apresentação;
* consultas SQL espalhadas;
* regras de negócio diretamente em controllers.

Manter separação clara entre:

* apresentação;
* estado;
* serviços;
* comunicação HTTP;
* regras de negócio;
* persistência.

---

# 20. API

A API deve ser RESTful e documentada.

Utilizar:

* DTOs;
* validação;
* paginação;
* filtros;
* ordenação quando necessária;
* códigos HTTP apropriados;
* respostas consistentes.

Criar documentação da API utilizando padrão apropriado, preferencialmente OpenAPI.

---

# 21. ANÁLISE DAS PROVAS

A análise das provas deve acontecer em pipeline.

Para cada edição:

1. identificar documentos;
2. identificar tipo de documento;
3. extrair questões;
4. relacionar gabarito;
5. detectar questões anuladas;
6. identificar disciplina;
7. classificar assunto;
8. classificar subassunto;
9. identificar habilidade;
10. estimar dificuldade;
11. registrar página;
12. registrar evidência;
13. registrar confiança da classificação;
14. marcar pendências.

Uma classificação feita pela IA deve possuir indicador de confiança.

---

# 22. VALIDAÇÃO DE QUESTÕES

Toda questão deve passar por validações:

* existe enunciado;
* existem alternativas quando aplicável;
* existe resposta;
* resposta existe entre as alternativas;
* gabarito corresponde à edição correta;
* número da questão é válido;
* fonte existe;
* disciplina foi classificada;
* assunto foi classificado;
* não existe duplicidade acidental.

Questões problemáticas devem ficar em estado de revisão.

Nunca publicar dados incompletos silenciosamente.

---

# 23. DETECÇÃO DE DUPLICIDADE

Criar mecanismos para identificar:

* mesma questão importada duas vezes;
* questão duplicada entre fontes;
* pequenas diferenças de OCR;
* alternativas duplicadas;
* questões equivalentes.

A detecção automática pode gerar suspeitas, mas não deve apagar conteúdo automaticamente.

---

# 24. TESTES

Toda funcionalidade relevante deve ter testes.

Backend:

* unitários;
* integração;
* repository;
* API;
* autenticação;
* autorização.

Frontend:

* lógica crítica;
* validações;
* manipulação de respostas;
* cálculo de desempenho;
* estados de simulado.

Também criar testes de integração para fluxos críticos.

---

# 25. CI/CD

O GitHub Actions deverá:

1. instalar dependências;
2. validar o projeto;
3. executar lint quando aplicável;
4. executar testes;
5. gerar build;
6. verificar qualidade mínima;
7. publicar frontend no GitHub Pages quando apropriado.

Falha em testes deve impedir deploy.

O backend será entregue à VPS por pipeline separada ou mecanismo documentado de deploy.

Nunca colocar secrets da VPS diretamente no código.

---

# 26. DEFINITION OF DONE

Uma task somente pode ser considerada concluída quando:

* código implementado;
* testes executados;
* erros corrigidos;
* documentação atualizada quando necessária;
* acessibilidade considerada;
* responsividade considerada;
* segurança considerada;
* build executado;
* critérios de aceitação atendidos;
* nenhum arquivo desnecessário criado.

---

# 27. COMPORTAMENTO DA LLM NO OPENCODE

A LLM deve atuar como uma equipe profissional composta conceitualmente por:

* Product Manager;
* UX/UI Designer;
* Frontend Engineer;
* Backend Engineer;
* Database Engineer;
* QA Engineer;
* DevOps Engineer;
* Security Engineer;
* Technical Writer;
* Data Analyst.

A LLM deve trocar de perspectiva conforme o problema.

Antes de implementar uma funcionalidade complexa:

1. inspecionar o repositório;
2. entender arquitetura existente;
3. localizar documentação relevante;
4. verificar dependências;
5. identificar impactos;
6. implementar;
7. testar;
8. revisar;
9. corrigir;
10. documentar.

---

# 28. NÃO DESTRUIR TRABALHO EXISTENTE

Nunca reescrever arquivos grandes simplesmente para facilitar implementação.

Antes de modificar um arquivo:

* entender seu conteúdo;
* entender suas dependências;
* preservar funcionalidades existentes.

Não remover recursos existentes sem motivo técnico documentado.

---

# 29. NÃO INVENTAR INFRAESTRUTURA

Nunca inventar:

* credenciais;
* domínio;
* IP;
* senha;
* tokens;
* secrets;
* URL de produção.

Utilizar placeholders em arquivos `.env.example`.

---

# 30. DESENVOLVIMENTO INCREMENTAL

O projeto deve ser construído por etapas descritas em `TASKS.md`.

Não tentar implementar todas as funcionalidades de uma vez.

A LLM deve trabalhar na próxima task disponível cuja dependência esteja satisfeita.

Após cada etapa:

* verificar critérios de aceitação;
* atualizar documentação;
* atualizar status;
* executar testes.

---

# 31. CONTEXTO EXTERNO

Quando precisar de documentação técnica atualizada:

* preferir documentação oficial;
* utilizar ferramentas disponíveis no ambiente;
* não confiar em conhecimento desatualizado sobre bibliotecas.

Para informações sobre o IFRN:

* priorizar documentos oficiais;
* priorizar provas e gabaritos presentes no repositório;
* quando necessário, utilizar o portal oficial do IFRN;
* registrar a origem da informação.

---

# 32. PRINCÍPIO FINAL

Este projeto não deve ser apenas "mais um site de questões".

Ele deve funcionar como uma plataforma orientada por evidências:

PROVAS REAIS
↓
EXTRAÇÃO
↓
CLASSIFICAÇÃO
↓
BANCO DE QUESTÕES
↓
ANÁLISE DO PERFIL DA PROVA
↓
DIAGNÓSTICO DO ALUNO
↓
ROTEIRO DE ESTUDOS
↓
QUESTÕES E SIMULADOS
↓
DESEMPENHO
↓
NOVO DIAGNÓSTICO
↓
ROTEIRO ADAPTADO

Todas as decisões importantes do produto devem respeitar essa cadeia.