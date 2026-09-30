-- V2__seed.sql — TASK 2.2 (seed inicial)
-- Só fatos da Fase 1: roles, disciplinas, taxonomia v1.1 observada
-- (docs/content-map.md), edições/documentos (hashes auditados em
-- docs/gabaritos-validation.md §0) e prompts da discursiva
-- (docs/provas-inventario.md). Questões: só via importador (TASK 2.3).
-- Idempotente (ON CONFLICT DO NOTHING) para reexecução local segura.

-- ---- roles ----
INSERT INTO roles (code, description) VALUES
  ('STUDENT', 'Estudante: seus próprios attempts/planos'),
  ('CURATOR', 'Curadoria: revisar classificações e publicação'),
  ('ADMIN', 'Administração: métricas e gestão')
ON CONFLICT (code) DO NOTHING;

-- ---- disciplinas ----
INSERT INTO disciplines (code, name) VALUES
  ('LINGUA_PORTUGUESA', 'Língua Portuguesa'),
  ('MATEMATICA', 'Matemática')
ON CONFLICT (code) DO NOTHING;

-- ---- tópicos v1.1 (observados nas 240 questões) ----
INSERT INTO topics (discipline_id, code, name)
SELECT d.id, v.code, v.name FROM disciplines d
JOIN (VALUES
  ('LINGUA_PORTUGUESA','GRAMATICA_NORMA','Gramática e norma'),
  ('LINGUA_PORTUGUESA','INTERPRETACAO_TEXTUAL','Interpretação textual'),
  ('MATEMATICA','RAZAO_PROPORCAO','Razão e proporção'),
  ('MATEMATICA','ARITMETICA','Aritmética'),
  ('MATEMATICA','ALGEBRA','Álgebra'),
  ('MATEMATICA','GEOMETRIA','Geometria'),
  ('MATEMATICA','ESTATISTICA_DADOS','Estatística e dados'),
  ('MATEMATICA','PORCENTAGEM','Porcentagem'),
  ('MATEMATICA','MATEMATICA_FINANCEIRA','Matemática financeira'),
  ('MATEMATICA','GRANDEZAS_MEDIDAS','Grandezas e medidas')
) AS v(dcode, code, name) ON d.code = v.dcode
ON CONFLICT DO NOTHING;

-- ---- subtópicos v1.1 (só os observados; doc/content-map.md §3) ----
-- Códigos da taxonomia v1 sem ocorrência (ex. MEDIANA_MODA,
-- CONTAGEM_COMBINATORIA, JUROS_COMPOSTOS, DESCONTO, MASSA_COMPRIMENTO)
-- entram por migração futura quando (e se) observados — nada inventado.
INSERT INTO subtopics (topic_id, code, name)
SELECT t.id, v.code, v.name FROM topics t
JOIN (VALUES
  ('GRAMATICA_NORMA','MORFOLOGIA','Morfologia'),
  ('GRAMATICA_NORMA','PONTUACAO','Pontuação'),
  ('GRAMATICA_NORMA','SEMANTICA_VOCABULARIO','Semântica e vocabulário'),
  ('GRAMATICA_NORMA','COESAO_REFERENCIA','Coesão e referência'),
  ('GRAMATICA_NORMA','SINTAXE_PERIODO','Sintaxe do período'),
  ('GRAMATICA_NORMA','SINTAXE_FUNCAO','Funções sintáticas'),
  ('GRAMATICA_NORMA','NORMA_PADRAO','Norma-padrão'),
  ('GRAMATICA_NORMA','ACENTUACAO_GRAFICA','Acentuação gráfica'),
  ('INTERPRETACAO_TEXTUAL','INTENCAO_COMUNICATIVA','Intenção comunicativa'),
  ('INTERPRETACAO_TEXTUAL','INFORMACAO_EXPLICITA','Informação explícita'),
  ('INTERPRETACAO_TEXTUAL','INFERENCIA','Inferência'),
  ('INTERPRETACAO_TEXTUAL','FUNCAO_ELEMENTO_PARAGRAFO','Função de elemento/parágrafo'),
  ('INTERPRETACAO_TEXTUAL','TIPO_TEXTUAL','Tipo textual'),
  ('INTERPRETACAO_TEXTUAL','GENERO_TEXTUAL','Gênero textual'),
  ('RAZAO_PROPORCAO','RAZAO','Razão'),
  ('RAZAO_PROPORCAO','REGRA_DE_TRES','Regra de três'),
  ('RAZAO_PROPORCAO','ESCALA','Escala'),
  ('RAZAO_PROPORCAO','PROPORCIONALIDADE','Proporcionalidade'),
  ('ARITMETICA','OPERACOES','Operações'),
  ('ARITMETICA','FRACOES','Frações'),
  ('ARITMETICA','NOTACAO_CIENTIFICA','Notação científica'),
  ('ARITMETICA','DIVISIBILIDADE_MMC_MDC','Divisibilidade, MMC e MDC'),
  ('ARITMETICA','SISTEMAS_NUMERACAO','Sistemas de numeração'),
  ('ALGEBRA','FUNCAO_AFIM','Função afim'),
  ('ALGEBRA','EQUACOES','Equações'),
  ('ALGEBRA','SISTEMAS','Sistemas'),
  ('ALGEBRA','SEQUENCIAS','Sequências'),
  ('GEOMETRIA','AREA_PLANA','Área plana'),
  ('GEOMETRIA','PERIMETRO_COMPRIMENTO','Perímetro e comprimento'),
  ('GEOMETRIA','POLIGONOS','Polígonos'),
  ('GEOMETRIA','PITAGORAS_DISTANCIA','Pitágoras e distância'),
  ('GEOMETRIA','VOLUME','Volume'),
  ('GEOMETRIA','UNIDADES_MEDIDA','Unidades de medida'),
  ('ESTATISTICA_DADOS','MEDIA','Média'),
  ('ESTATISTICA_DADOS','LEITURA_GRAFICO_TABELA','Leitura de gráfico/tabela'),
  ('ESTATISTICA_DADOS','PROBABILIDADE','Probabilidade'),
  ('PORCENTAGEM','CALCULO_DIRETO','Cálculo direto'),
  ('PORCENTAGEM','VARIACAO','Variação percentual'),
  ('PORCENTAGEM','REPRESENTACAO_FRACAO','Representação fracionária'),
  ('MATEMATICA_FINANCEIRA','JUROS_SIMPLES','Juros simples'),
  ('GRANDEZAS_MEDIDAS','TEMPO','Tempo'),
  ('GRANDEZAS_MEDIDAS','VOLUME_CAPACIDADE','Volume e capacidade')
) AS v(tcode, code, name) ON t.code = v.tcode
ON CONFLICT DO NOTHING;

-- ---- edições (2021 ausente: jamais inserir sem fonte) ----
INSERT INTO exams (year, edital, duration_minutes, objective_count, lp_count, mat_count, has_essay, scoring_rule) VALUES
  (2020, '29/2019', 240, 40, 20, 20, TRUE, NULL),
  (2022, '041/2021', 240, 40, 20, 20, TRUE, NULL),
  (2023, '040/2022', 240, 40, 20, 20, TRUE, NULL),
  (2024, '078/2023', 240, 40, 20, 20, TRUE, NULL),
  (2025, '23/2024', 240, 40, 20, 20, TRUE, NULL),
  (2026, '48/2025', 240, 40, 20, 20, TRUE, NULL)
ON CONFLICT (year) DO NOTHING;
-- scoring_rule NULL em todas: pontuação de anuladas/discursiva DESCONHECIDA.

-- ---- versões (uma por edição; 2022: preliminar+definitivo idênticos no mesmo PDF) ----
INSERT INTO exam_versions (exam_id, version_code, published_at, note)
SELECT e.id, v.code, v.pub::date, v.note FROM exams e
JOIN (VALUES
  (2020, 'FINAL', NULL, 'Só o final no repo; preliminar NÃO CONFIRMADA'),
  (2022, 'DEFINITIVO', NULL, 'Preliminar + definitivo idênticos 40/40 no mesmo PDF via FUNCERN; retificação posterior NÃO CONFIRMADA'),
  (2023, 'DEFINITIVO', NULL, 'Só o definitivo no repo; págs. 1–2 listam ofertas, não respostas'),
  (2024, 'DEFINITIVO', NULL, 'Só o definitivo no repo'),
  (2025, 'DEFINITIVO', NULL, 'Arquivo nomeado 2024 com conteúdo de 2025; mapeamento canônico pendente'),
  (2026, 'DEFINITIVO', '2025-11-04', 'Data interna do gabarito; X = questão anulada')
) AS v(yr, code, pub, note) ON e.year = v.yr
ON CONFLICT DO NOTHING;

-- ---- documentos (nomes literais + SHA-256 auditados) ----
INSERT INTO exam_documents (exam_version_id, kind, file_name, sha256, pages, generator, note)
SELECT ev.id, v.kind, v.fname, v.sha, v.pages, v.gen, v.note
FROM exam_versions ev JOIN exams e ON e.id = ev.exam_id
JOIN (VALUES
  (2020, 'CADERNO', 'data/provas/2020/Prova.pdf',
   'f53d78a34d1e669108bef0051783093a7ebfec20e87a94aa9b9f7f370154ba22', 15, 'Microsoft Word', NULL),
  (2020, 'GABARITO_FINAL', 'data/provas/2020/Gabarito_Final.pdf',
   'd12f799b5fc63c631a90d22eeb1c87ec1461cfef02e7399ebf4ed493d6a065c8', 1, NULL, 'Anuladas Q7 e Q26'),
  (2022, 'CADERNO', 'data/provas/2022/Prova_Exame_Tecnico_Integrado_2022_XNn8mg4.pdf',
   'ffcfa54064c148c026d99c2a73ada3fdfee4a5c78d664ba4db06c7a8ffdf1bfe', 15, 'Microsoft Word', NULL),
  (2022, 'GABARITO_DEFINITIVO', 'data/provas/2022/GABARITO_Prova_Exame_Tecnico_Integrado_2022.pdf',
   '35cd549542acd3c2c0fe02b35f447caacf61e74d86c07c83019cb6fa4faa403c', 4, 'FUNCERN', 'Contém preliminar (págs. 1–2) + definitivo (págs. 2–4), idênticos'),
  (2023, 'CADERNO', 'data/provas/2023/Caderno_de_provas.pdf',
   '99caba7636afdc10163c5e0bfddc0e1adbcf62443173e0196f08672b485772d8', 14, 'Microsoft Word', NULL),
  (2023, 'GABARITO_DEFINITIVO', 'data/provas/2023/Gabarito_Final_-_Exame_de_Selecao_2023.pdf',
   '57d989e8df89a59f819237e8bbd4b0e0975db341a250bfb91b9f48fecca180ac', 3, NULL, 'Grade na pág. 3; págs. 1–2 = ofertas; anulada Q21'),
  (2024, 'CADERNO', 'data/provas/2024/Cadernos_de_provas_-_Cursos_Tecnicos_Integrados_2024_.pdf',
   '8bf3f5931abdfb0e07868d061d9c30b877e3dd4869d5e4481f2cf5044b770020', 15, 'PDFium', NULL),
  (2024, 'GABARITO_DEFINITIVO', 'data/provas/2024/Gabarito_Oficial_Definitivo_IFRN_78.pdf',
   '6a1dfb5a8123641a9da011a0c80cbdfbabd8d390f69eece83463fc2fb551e8b6', 1, NULL, 'Anulada Q17'),
  (2025, 'CADERNO', 'data/provas/2025/PROVA_IFRN_-_EXAME_DE_SELEÇÃO_2025.pdf',
   '3f35c8024ea875051f99cdf82c2271ceb9ce92d1de9e7f9222f6fe4ddd24cdc2', 14, 'Microsoft Word', 'Vedado levar o caderno (novidade 2025–2026)'),
  (2025, 'GABARITO_DEFINITIVO', 'data/provas/2025/Exame_de_Seleção_2024_-_Gabarito_Final.pdf',
   '878446a04760fd5f70d341f57ad307ceb08c3e73d574e698b93fcbb101efa756', 1, NULL, 'Nome 2024 × conteúdo 2025; sem anuladas'),
  (2026, 'CADERNO', 'data/provas/2026/Caderno_de_Provas_-Técnico_Integrado_2026.pdf',
   '6a535a0b3a9207cc481ac373c4da9f38940c3558c6e1efac5e46443ce22aa3d1', 14, 'PDF24', 'Numeração com U+200B; normalizar na extração'),
  (2026, 'GABARITO_DEFINITIVO', 'data/provas/2026/GABARITO_EXAME_DE_SELEÇÃO_2026_-_DEFINITIVO.pdf',
   '5cf06f9de02e03077ca592860c60a09db8ce1539df43dc1c881c37cde8354ece', 1, NULL, 'Anulada Q37')
) AS v(yr, kind, fname, sha, pages, gen, note) ON e.year = v.yr
ON CONFLICT (sha256) DO NOTHING;

-- ---- prompts da discursiva (trechos mínimos de identificação) ----
INSERT INTO exam_essay_prompts (exam_id, genre, theme, pseudonym, proposal_excerpt, criteria_text, page)
SELECT e.id, v.genre, v.theme, v.pseudo, v.excerpt, v.crit, v.pg FROM exams e
JOIN (VALUES
  (2020, 'Artigo de opinião', 'Cyberbullying: a responsabilidade de resolver o problema é da escola?',
   'Ariel da Net', 'Escreva um artigo de opinião: a responsabilidade de resolver o problema do cyberbullying é da escola?', NULL, 14),
  (2022, 'Artigo de opinião', 'As terras indígenas devem ser protegidas pelo governo brasileiro?',
   'Potiguar da Silva', 'Escreva um artigo de opinião sobre a posse das terras indígenas.', 'Orientações e critérios impressos no caderno (ver PDF-fonte)', 14),
  (2023, 'Artigo de opinião', 'A Lei de Cotas e a disputa por vagas nas universidades',
   'Negritude Iorubá', 'Escreva um artigo de opinião sobre a Lei das Cotas.', NULL, 13),
  (2024, 'Artigo de opinião', 'A responsabilidade pela preservação da Amazônia é de todos os países?',
   'Amazonense da Silva', 'Escreva um artigo de opinião sobre a preservação da Amazônia.', NULL, 14),
  (2025, 'Artigo de opinião', 'O uso de celulares deve ser proibido nas escolas brasileiras?',
   'O REI DA NET', 'Escreva um artigo de opinião sobre o uso de celulares nas escolas.', NULL, 13),
  (2026, 'Artigo de opinião', 'O Brasil deve assumir um papel central no combate às mudanças climáticas?',
   'Amazonino Belém', 'Escreva um artigo de opinião sobre o papel do Brasil no combate às mudanças climáticas.', NULL, 13)
) AS v(yr, genre, theme, pseudo, excerpt, crit, pg) ON e.year = v.yr
ON CONFLICT (exam_id) DO NOTHING;
