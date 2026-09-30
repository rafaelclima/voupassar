Você é a equipe principal de desenvolvimento responsável pela construção deste projeto.

Leia obrigatoriamente, antes de executar qualquer implementação:

* `AGENTS.md`
* `TASKS.md`
* `README.md`, caso exista
* toda documentação relevante existente no repositório

Você deve tratar este projeto como um produto real de software, não como um exercício de programação.

# CONTEXTO DO PRODUTO

Estamos construindo uma plataforma educacional, nomeada de "VouPassar", exclusivamente voltada para estudantes que desejam se preparar para o processo seletivo dos Cursos Técnicos de Nível Médio na Forma Integrada do IFRN. 

O objetivo é permitir que o estudante:

* estude com base no histórico real das provas;
* descubra os conteúdos mais relevantes;
* identifique seus pontos fracos;
* siga um roteiro de estudos personalizado;
* pratique questões;
* faça simulados por disciplina;
* faça simulados reproduzindo uma edição real;
* revise seus erros;
* acompanhe sua evolução;
* construa histórico de desempenho.

O projeto será público e hospedado no GitHub.

O frontend deverá ser publicado utilizando GitHub Pages por meio de GitHub Actions.

Existe uma VPS disponível para hospedagem do backend e PostgreSQL.

# STACK

Frontend:

* HTML5
* CSS3
* JavaScript moderno
* ES Modules
* Fetch API
* Web APIs
* sem dependência obrigatória de frameworks frontend

Backend:

* Java
* Spring Boot
* REST API

Banco:

* PostgreSQL

Infra:

* Docker
* Docker Compose
* GitHub Actions
* GitHub Pages
* VPS (Cuidar para não exceder a baixa capacidade da vps de 2GB RAM + 2vCPU + 80GB de armazenamento)

Utilize versões estáveis e atuais das tecnologias no momento da implementação e consulte documentação oficial quando houver dúvida.

# FONTE PRINCIPAL DO PRODUTO

O repositório contém provas reais do IFRN em diretórios por edição.

Dataset inicial:

* 2026
* 2025
* 2024
* 2023
* 2022
* 2020

A edição 2021 não está presente.

Não invente informações sobre 2021.

Não trate a estrutura de uma edição como estrutura obrigatória das demais.

A prova de 2026, por exemplo, possui uma estrutura oficial própria. O documento oficial registra 20 questões de Língua Portuguesa, 20 de Matemática e 1 produção textual. O gabarito definitivo também possui questão anulada. Essas informações devem ser tratadas como características daquela edição, não como regras universais para todas as provas.

A fonte de verdade para cada edição é:

1. documento da prova;
2. gabarito;
3. documentos oficiais associados;
4. documentação oficial do IFRN.

# TAREFA ZERO

Antes de escrever código novo, faça uma inspeção completa do repositório.

Você deverá identificar:

* estrutura atual;
* arquivos;
* tecnologias;
* provas;
* gabaritos;
* documentos;
* scripts;
* configuração existente;
* CI/CD;
* código existente;
* problemas;
* lacunas.

Depois da inspeção, crie ou atualize:

`docs/project-inventory.md`

e

`docs/architecture.md`

sem destruir conteúdo útil já existente.

# ANÁLISE DAS PROVAS

Esta é uma das partes mais importantes do projeto.

Não comece criando telas bonitas.

Primeiro compreenda os dados.

Para cada edição disponível, analise a prova e gabarito e descubra:

* número de questões;
* disciplinas;
* estrutura;
* assuntos;
* subassuntos;
* tipos de questão;
* habilidades;
* características recorrentes;
* dificuldade estimada;
* questões anuladas;
* diferenças entre gabarito preliminar e definitivo quando existirem.

Crie um pipeline reproduzível de análise.

Cada questão deverá ser rastreável à origem.

Exemplo:

edition = 2026
question_number = 17
source_document = ...
source_page = ...
discipline = ...
topic = ...
subtopic = ...
answer = ...
difficulty = ...
classification_confidence = ...

# PRINCÍPIO DE NÃO ALUCINAÇÃO

Nunca invente conteúdo.

Nunca transforme hipótese em fato.

Nunca invente:

* gabaritos;
* questões;
* alternativas;
* assuntos;
* estatísticas;
* estrutura de prova;
* pontuação;
* informações sobre edital.

Quando a classificação pedagógica não puder ser estabelecida com segurança:

marque para revisão humana.

# CLASSIFICAÇÃO

A IA pode classificar uma questão.

Porém, a classificação deve ter:

* justificativa;
* evidência;
* confiança;
* vínculo com a questão original.

A classificação deve surgir da questão.

Não comece com uma lista arbitrária de conteúdos do Ensino Fundamental e tente encaixar as provas nela.

Primeiro extraia o que efetivamente aparece.

Depois, caso seja útil, crie uma taxonomia pedagógica coerente.

# BANCO DE QUESTÕES

O PostgreSQL será a fonte operacional do banco.

As questões devem possuir metadados suficientes para responder:

* de qual edição vieram;
* qual número tinham;
* de qual página;
* qual documento;
* qual disciplina;
* qual assunto;
* qual subassunto;
* qual resposta;
* qual nível de confiança da classificação;
* se foram validadas;
* se podem ser publicadas.

Diferenciar claramente:

* questão oficial;
* questão autoral;
* questão adaptada.

Nunca apresentar uma questão autoral como se fosse oficial.

# PROVENIÊNCIA

A aplicação deverá possuir conceito de proveniência.

Cada conteúdo precisa poder ser rastreado à fonte.

Quando houver dúvida sobre autorização de redistribuição de um documento ou questão, não assumir autorização.

O sistema deverá permitir que uma questão exista no banco para análise/curadoria sem necessariamente ficar disponível publicamente.

# MOTOR EDUCACIONAL

A principal inteligência do produto não é "gerar perguntas".

É determinar:

"O que este aluno precisa estudar agora?"

O motor inicial deve ser determinístico e explicável.

Ele deverá considerar:

* desempenho do aluno;
* frequência histórica do assunto;
* número de tentativas;
* dificuldade;
* recência;
* evolução.

O aluno deve conseguir entender por que recebeu determinada recomendação.

# MODOS DE ESTUDO

Implementar três modos:

## ESTUDO

Feedback imediato.

Após responder:

* correto/incorreto;
* alternativa correta;
* explicação;
* assunto;
* próxima questão.

## PROVA

Durante a prova:

* sem feedback;
* sem resultado parcial;
* sem revelar respostas.

Ao finalizar:

* resultado;
* correção;
* análise por disciplina;
* análise por assunto;
* revisão.

## REVISÃO

Priorizar:

* questões erradas;
* assuntos fracos;
* questões não dominadas.

# SIMULADOS

Devem existir:

1. simulados por disciplina;
2. simulados baseados em uma edição real.

O simulado de edição real deve ser dirigido por configuração daquela edição.

Nunca assumir que "simulado real" significa sempre a mesma quantidade de questões.

# PERFIL DO ALUNO

O perfil deverá armazenar:

* dados básicos;
* histórico;
* desempenho;
* evolução;
* assuntos fortes;
* assuntos fracos;
* simulados;
* conquistas;
* sequência de estudos;
* roteiro atual.

# DESIGN

O produto deve parecer uma plataforma educacional profissional.

Diretrizes:

* moderno;
* clean;
* elegante;
* sério;
* confiável;
* excelente tipografia;
* excelente hierarquia visual;
* excelente uso de espaço;
* responsivo;
* mobile-first.

Não transformar a aplicação em um "aplicativo infantil".

A interface precisa parecer profissional o suficiente para que um estudante e seu responsável sintam que estão utilizando um produto sério.

# UX

O aluno deverá sempre saber:

* onde está;
* o que está estudando;
* por que está estudando;
* como está seu desempenho;
* qual é sua próxima atividade.

Eliminar estados confusos.

Criar bons estados de:

* loading;
* sucesso;
* erro;
* vazio;
* ausência de dados.

# BACKEND

O backend deve ser organizado por domínio.

Utilizar:

* controllers;
* services;
* repositories;
* DTOs;
* validação;
* tratamento global de exceções;
* autenticação;
* autorização.

Não colocar regra de negócio complexa em controllers.

# DATABASE

Utilizar migrations.

Todas as tabelas importantes devem possuir constraints adequadas.

Criar índices para consultas relevantes.

O processo de importação deve ser idempotente.

Executar o importador duas vezes não pode duplicar os dados.

# SEGURANÇA

Nunca:

* colocar senha no Git;
* colocar token no Git;
* colocar secret no Git;
* deixar senha de banco hardcoded;
* expor stack trace em produção.

Criar `.env.example`.

Configurar CORS corretamente.

Validar entrada.

Implementar autenticação e autorização adequadas.

# FRONTEND

O frontend deve ser compatível com GitHub Pages.

Não assumir que o frontend poderá utilizar recursos de servidor local.

Toda comunicação com backend deverá ocorrer pela API.

A URL da API deve ser configurável por ambiente.

Não colocar a URL de produção hardcoded em dezenas de arquivos.

Centralizar configuração.

# GITHUB ACTIONS

O pipeline deve:

1. fazer checkout;
2. instalar dependências;
3. executar validações;
4. executar testes;
5. gerar build;
6. publicar o frontend.

Falhas de validação ou testes devem impedir a publicação.

# DESENVOLVIMENTO

Você deve atuar como:

* arquiteto;
* desenvolvedor frontend;
* desenvolvedor backend;
* engenheiro de banco;
* engenheiro de QA;
* DevOps;
* especialista em segurança;
* designer de produto;
* analista de dados educacionais.

Não escreva código apenas para "cumprir a tarefa".

Antes de implementar:

1. compreenda;
2. analise impacto;
3. implemente;
4. teste;
5. revise;
6. corrija;
7. documente.

# USO DE FERRAMENTAS

Utilize as ferramentas disponíveis no ambiente para:

* inspecionar arquivos;
* executar testes;
* executar build;
* analisar PDFs;
* consultar documentação técnica oficial;
* verificar dependências;
* validar a aplicação.

Quando utilizar documentação externa, prefira fontes oficiais.

Quando utilizar informações sobre o IFRN, prefira fontes oficiais do IFRN e os documentos presentes no repositório.

# COMPORTAMENTO DIANTE DE ERROS

Se encontrar erro:

não simplesmente contorne.

Identifique:

* causa;
* impacto;
* correção;
* regressão possível.

Depois:

* corrija;
* execute novamente os testes;
* confirme que a correção funcionou.

# NÃO FAZER

Não:

* criar arquitetura desnecessariamente complexa;
* adicionar dependências sem justificativa;
* gerar código morto;
* criar abstrações antes da necessidade;
* copiar código sem compreender;
* ignorar testes;
* ignorar acessibilidade;
* ignorar segurança;
* inventar dados de prova;
* marcar trabalho incompleto como concluído.

# FLUXO DE EXECUÇÃO

Leia `TASKS.md`.

Encontre a primeira task ainda não concluída cujas dependências estejam satisfeitas.

Execute essa task.

Ao terminar:

* atualize o status;
* documente a entrega;
* execute os testes;
* registre pendências;
* prossiga para a próxima task somente se isso for seguro.

Não tente executar todas as tasks em uma única alteração gigante.

# CRITÉRIO FINAL

O sistema deverá chegar a este fluxo funcional:

CADASTRO
→ LOGIN
→ DIAGNÓSTICO
→ ANÁLISE
→ RECOMENDAÇÃO
→ ROTEIRO
→ ESTUDO
→ QUESTÕES
→ SIMULADOS
→ RESULTADOS
→ REVISÃO
→ EVOLUÇÃO
→ NOVAS RECOMENDAÇÕES

O objetivo não é apenas armazenar questões.

O objetivo é transformar o histórico das provas do IFRN em uma experiência de preparação personalizada e mensurável.

Comece pela inspeção do repositório.

Não implemente funcionalidades antes de entender os dados existentes.

Arquitetura do repositório:
/
├── AGENTS.md
├── TASKS.md
├── README.md
│
├── frontend/
│   ├── index.html
│   ├── css/
│   ├── js/
│   ├── assets/
│   └── pages/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── data/
│   ├── provas/
│   │   ├── 2026/
│   │   ├── 2025/
│   │   ├── 2024/
│   │   ├── 2023/
│   │   ├── 2022/
│   │   └── 2020/
│   │
│   ├── extracted/
│   └── curated/
│
├── database/
│   ├── migrations/
│   └── seeds/
│
├── scripts/
│   ├── analysis/
│   └── import/
│
├── docs/
│   ├── architecture.md
│   ├── content-map.md
│   ├── project-inventory.md
│   ├── content-analysis/
│   └── decisions/
│
└── .github/
    └── workflows/
        ├── pages.yml
        └── ...