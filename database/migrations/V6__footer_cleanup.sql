-- V6__footer_cleanup.sql — curadoria de dados (limpeza de rodape).
-- Remove da alternativa D o boilerplate de rodape/cabecalho do caderno
-- ("Processo Seletivo ..." / "EDITAL Nº ... – PROEN/..." / nº de pagina)
-- que o extrator v1.0.0 colava no texto exibido ao aluno.
-- Fonte: extrator v1.1.0 (scripts/analysis/extract_questions.py),
-- diff auditado linha a linha contra data/extracted v1.0.0 (60/60 em D,
-- enunciados intactos, raw_block preservado). Gabarito, disciplina,
-- pagina e classificacao NAO mudam: so texto de exibicao + checksum.
-- Casos com trecho compartilhado mantido em D (conteudo real, sem perda):
-- 2020 Q3; 2023 Q12/Q24/Q27; 2024 Q24; 2026 Q7. Ver docs/auditoria-questoes.md.
-- 2026 Q10 (trecho de Q11-14 em D, sem rodape no bloco) nao muda aqui.

DO $$
DECLARE
  v_qid BIGINT;
  v_n INT := 0;
BEGIN
  -- 2020 Q3 [TRECHO COMPARTILHADO MANTIDO EM D — ver docs]
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 3
    AND q.checksum = '6e8e2f214f07b2741cf8d252ea48aced2928d74399eb7a0f5049af6f440447c4'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q3 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'o sofrimento das vítimas do cyberbullying no Brasil pode ser amenizado por entidades protetivas. Considere o trecho a seguir para responder às questões de 4 a 7. Quando entrou em um colégio novo, na Zona Oeste do Rio, os problemas começaram para Laura, de 13 anos. “Ela é popular. Faz amizade fácil e é bonita. Aquilo provocou a ira de um grupo de colegas”, lembra Rita, de 46 anos, mãe da jovem.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'o sofrimento das vítimas do cyberbullying no Brasil pode ser amenizado por entidades protetivas. Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020                                    3 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN Considere o trecho a seguir para responder às questões de 4 a 7. Quando entrou em um colégio novo, na Zona Oeste do Rio, os problemas começaram para Laura, de 13 anos. “Ela é popular. Faz amizade fácil e é bonita. Aquilo provocou a ira de um grupo de colegas”, lembra Rita, de 46 anos, mãe da jovem.';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q3 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '9f0bb9cf205bdeff4eaa03caa50da2c792c7589d65a9da1d6d75ab17d8d07ef7', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q8
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 8
    AND q.checksum = '456038545d32e7b3874eb5dc79361193877c89c645fba9bd5f408f3326f78fbb'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q8 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'a maioria dos pais percebe, com antecedência, que os filhos estão vivendo alguma agressão por meio das redes sociais.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'a maioria dos pais percebe, com antecedência, que os filhos estão vivendo alguma agressão por meio das redes sociais. 4                                            Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q8 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'dbb89036c861bf6c9130d7f24dc70520027adc1e616cdba910441a579c2ae4f2', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q13
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 13
    AND q.checksum = '5351cc28cb157e25780a4d32fdfeab15768e22fdffb657eacfca00fba1d368da'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q13 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'substantivo e adjetivo.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'substantivo e adjetivo. Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020                                 5 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q13 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '1f280679412c01677a990da6a402af4cb8d19283e4bf95997fc842c593e14a4f', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q19
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 19
    AND q.checksum = '29c75b5bff4ae07c9d5a99aba7e946671d68bcd2de7914fc91a8c63e8d5dc7f0'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q19 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'narração caracterizada pela presença de personagem.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'narração caracterizada pela presença de personagem. 6                                                 Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q19 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '882ec1a42e24e77474ce508835720676c16987e65d831c7c15a2427b66aaf12d', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q20
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 20
    AND q.checksum = '78bdf1d4a2141e28c3804cf3acda4dd98b90495a72775b7eafc22691509f24d7'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q20 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'reportagem e charge.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'reportagem e charge. Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020               7 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q20 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'fe5ef61b4b3a763093aa82073de8e391d53df16d5b5adc2b89ae251b81235188', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q24
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 24
    AND q.checksum = '630267a50752de8b3cbff6bb7a39708b73a5201d8cde689f23c8ab3369d76ccd'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q24 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '13 cm.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '13 cm. 8                                               Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q24 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '254bb481c5f98d2f745defee38f07635a1075db1da2cfc0bdc548ddcb35ba9b4', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q28
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 28
    AND q.checksum = 'aea2c44cfd376e6f8e7c767472b82c2cb03c71ad326d740983461e38d1444c89'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q28 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '18.000 pessoas.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '18.000 pessoas. Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020                                 9 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q28 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '1a3ffe8645017de96f6d7016c97034a5f0a68b66b7d8159a28eebd7fa5a9207c', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q32
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 32
    AND q.checksum = '94d88f03f49a7bfd7d99f18ab62351fb10e900b6af87084104e3f7d21b4729cd'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q32 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '140,00.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '140,00. 10                                                 Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q32 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'b6206f49913abe6b53207077cde6a99c7f4e8423c98e0bb7483025b72e3da249', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q34
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 34
    AND q.checksum = '4858c633f34f69e9df4d97ee71a729900f3d41fc8f4cb678dc23b227e111d145'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q34 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '698. Considere o Gráfico 1 para responder às questões 35 e 36. Gráfico 1', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '698. Considere o Gráfico 1 para responder às questões 35 e 36. Gráfico 1 Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020                               11 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q34 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '9240ec639578e840f503caafade836ad02be79d3ca7367993c563aa3a12ce50e', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q37
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 37
    AND q.checksum = 'a5405ea66597213ea3b77df8dde1abd8650b7f177349bca532f0f8e1857e64af'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q37 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '. 50', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '. 50 12                                             Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q37 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '3a92b795175aa551901fc96369beb0cc9d4f087a6e03fa054c0f8e30b6994031', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2020 Q40
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 40
    AND q.checksum = 'e1cae3b66cd61277111fad4c2fc51bf1bbb4f5255bada03a6cb8cbc497f40a23'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2020 Q40 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'terça-feira, às 14 horas.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'terça-feira, às 14 horas. Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2020                                13 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2020 EDITAL Nº 29/2019-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2020 Q40 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '005d748d1c46b4cd6369b17a62a7ee675715a2835d60b514213057f5f0962179', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q6
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 6
    AND q.checksum = 'e055ed1a50388e712755aa4719b7382945a6f8ece690942fad211dfa75fc4b86'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q6 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'limita a três as atividades que provocaram o desmatamento da mata nativa.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'limita a três as atividades que provocaram o desmatamento da mata nativa. 4                                              Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q6 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'acccfe6bfa86c6ea966b1b42258aaef184936b35b3f20b9d656c823e435ec635', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q11
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 11
    AND q.checksum = '5b5344ef4668c2f3f785c8bf657aa83d73b02738f98ea5593a7fefbebb0170a0'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q11 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'adjunto adverbial e determina o local dos rios.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'adjunto adverbial e determina o local dos rios. Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022                                 5 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q11 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'a391c885fde0db5a73a6900a4e00754fe4238e71f82fdbc1880a80597199dca1', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q16
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 16
    AND q.checksum = '7b5b647d9f0b647f62d3843493ef7a813afe7046440676544368450e5ba62f05'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q16 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'indica a classe social e o gênero do Juiz.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'indica a classe social e o gênero do Juiz. 6                                                Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q16 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '3dc5d0a703c00f439f992eef7a5677123707f9be9cd8cc170f0a42cd8f2a6cb3', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q20
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 20
    AND q.checksum = '3af987a69dfe4c207efaf3ec2b5eac97d461a2fee0ff18b669729a6c0687ef84'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q20 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'a variação linguística.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'a variação linguística. Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022         7 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q20 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'dd1ce39d7071ff5dd23281c56a89bd6269ff9bfa37c427269e05fdd01583efea', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q24
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 24
    AND q.checksum = '83ea37ab3e430ae7d17732ab525ea50f1064128802707877fd200d27b65d918c'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q24 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '15.800.000.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '15.800.000. 8                                             Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q24 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'd3e07f15cb7cfae02b23278b961a5b4415a6bde2b6dc3bafd7fbb5d5f8f7c036', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q29
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 29
    AND q.checksum = 'b0e8c6532d04a4854e880e236c476dcc8f9b40d0141a2601b30bdbc7d634ee60'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q29 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '75.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '75. Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022                                   9 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q29 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '8ce9cdeb8d8330594a889c8f67e486263e83c8ac7f75bd0e94a44e2a6b15b8e5', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q33
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 33
    AND q.checksum = '3d6e72334b7a22caeb5f22468ba03ecb3b87570a6b08065b6c5e318890844ecd'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q33 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '0,02.10⁵.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '0,02.10⁵. 10                                             Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q33 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '9fc6c2c908195e3c4521bf412804d9d92abbab59c2322b2e2f8d577aa4867a75', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q36
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 36
    AND q.checksum = '8be5391d25cde335e4b692926b8abd00c6baf14aa8d660bbf2a70c8fe3cd006e'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q36 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '138.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '138. Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022                                             11 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q36 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '7fa3342fb1ade5f779e0c7eb3d4ade9e16aafae5f607e6a851c888d1b8ff0f03', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q39
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 39
    AND q.checksum = '6dd31b24be8337262561d1597ccc55b92610d138c50343bfd6419ef4557cc28d'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q39 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '. ̅̅̅̅ 𝐴𝐶                                                  https://congressoemfoco.uol.com.br/tema/meio- ambiente/governo-estuda-medidas-para-regularizar- garimpo-clandestino/', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '. ̅̅̅̅ 𝐴𝐶                                                  https://congressoemfoco.uol.com.br/tema/meio- ambiente/governo-estuda-medidas-para-regularizar- garimpo-clandestino/ 12                                             Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q39 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'd965636881786618538e9f57a5c92c995263b4d0333b12037b0c30845745b6be', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2022 Q40
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2022 AND q.source_question_number = 40
    AND q.checksum = '6a8009e347373b8f80f651aa33d11fcea057186f0595a90368d543a4ff4dcf45'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2022 Q40 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '(111100110)2 .', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '(111100110)2 . Processo Seletivo – Curso Técnico de Nível Médio na Forma Integrada 2022                                 13 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2022 EDITAL Nº 041/2021 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2022 Q40 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '6e934646d4d683ce75de7bdcd61116e0ba36ba51a9157e59ef00e32bcd7383b9', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q3
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 3
    AND q.checksum = '990b43aeec7131f851ab678699505e1916ca678ee29202471299256eb0cbb657'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q3 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'reconfigurou as feições das universidades brasileiras, tornando-as menos elitizadas em dez anos.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'reconfigurou as feições das universidades brasileiras, tornando-as menos elitizadas em dez anos. Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023                                      3 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q3 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '835c8155fd2f98ba28de20704f20292a81fd281de0789d6202f20bad999cf4ad', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q7
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 7
    AND q.checksum = 'c0afba7b1d793a192784399874a6a9c0140aa74e40b5ff85dbc71cb388182dc1'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q7 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'Haviam entre os argumentos a alegação de que a regra, privilegia pretos e pardos, e prejudica os direitos dos brancos, tornando desigual a disputa por uma vaga nas universidades.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'Haviam entre os argumentos a alegação de que a regra, privilegia pretos e pardos, e prejudica os direitos dos brancos, tornando desigual a disputa por uma vaga nas universidades. 4                             Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q7 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'a24947e86c60c167e51c28929299b647a768854a06f946a6258b7fdd7e92ff6b', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q12 [TRECHO COMPARTILHADO MANTIDO EM D — ver docs]
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 12
    AND q.checksum = '1936a7f46191b20865a8f7056bb824498dc72dd5567449fe1ed4b0caa41d37be'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q12 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'completará. Considere o trecho para responder às questões 13 a 15. Mesmo com todos os efeitos positivos, (1) a Lei das Cotas tem sido rechaçada por grupos conservadores e neonazistas (2), inconformados com a presença dos negros nas universidades (3) ou em postos de mando em algumas empresas (4).', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'completará. Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023                     5 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN Considere o trecho para responder às questões 13 a 15. Mesmo com todos os efeitos positivos, (1) a Lei das Cotas tem sido rechaçada por grupos conservadores e neonazistas (2), inconformados com a presença dos negros nas universidades (3) ou em postos de mando em algumas empresas (4).';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q12 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '2dc2b324117c62ace5ff31522b8ee589aa24ccb769fd132890989eb804d0e589', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q17
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 17
    AND q.checksum = 'f077763748d233fda039e6362faeb55075c8a47b679037c763c6a481446d5533'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q17 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'apresenta uma descrição de como se configura o racismo estrutural no cenário brasileiro.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'apresenta uma descrição de como se configura o racismo estrutural no cenário brasileiro. 6                           Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q17 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '24a8ecb20fc7a562d2ec0b14d043e47e6bb92f5e3e46923c33a5565e8521fe8d', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q20
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 20
    AND q.checksum = '00131af0dbb9255e210312ecaab36cec96d43299768c3188b064abddf73f4e32'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q20 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'a variante linguística do registro informal.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'a variante linguística do registro informal. Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023     7 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q20 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '53d7facca4b3efb01c3446a2fbebc6c81cfc33752eeef10f94b1de03ca4401bf', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q24 [TRECHO COMPARTILHADO MANTIDO EM D — ver docs]
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 24
    AND q.checksum = '9e11749b263dc4420a2b9454efae8fe32490638af5706275adc3ae381639757e'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q24 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '5√3⁄2. Observe, na Figura 2, o organograma de distribuição de vagas de uma universidade. Figura 2 PPI: Pretos, pardos ou indígenas PcD: Pessoas com deficiência', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '5√3⁄2. 8                           Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN Observe, na Figura 2, o organograma de distribuição de vagas de uma universidade. Figura 2 PPI: Pretos, pardos ou indígenas PcD: Pessoas com deficiência';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q24 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '68ec41ddd743fb494145bc333bd4b5c915ab1b9c67be6dfa9b54bbd69fc37914', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q27 [TRECHO COMPARTILHADO MANTIDO EM D — ver docs]
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 27
    AND q.checksum = '79b7115979ea598cfd1d9e298b44c4edfa0f6cdc255939d7cf392d88975db370'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q27 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '𝑦 = 2500𝑥 − 830. Considere o Gráfico 1 para responder às questões 28 e 29. Gráfico 1: Posicionamento em relação à reserva de vagas para negros, por faixa etária, em percentual.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '𝑦 = 2500𝑥 − 830. Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023                                 9 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN Considere o Gráfico 1 para responder às questões 28 e 29. Gráfico 1: Posicionamento em relação à reserva de vagas para negros, por faixa etária, em percentual.';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q27 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'dcc5d3b087578bca2d3a15e53fa2fb08593eeab122654f1cc36cfd4ee29d34a6', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q30
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 30
    AND q.checksum = '00a1734fbfb8abe10b6b4b253e81ca9fd0fdb6363b3097561d54ac99435b8a60'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q30 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '9898.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '9898. 10                          Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q30 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'e455b83ce187878eacf4a3cc9955de1da5076182ec2cc3090a9e336c48821403', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q35
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 35
    AND q.checksum = '11a065960b97f8bcf9c9feef158c758cf7242b7ac89c6903b5ca4654888a8775'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q35 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '200.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '200. Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023                                11 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q35 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '8211c80f502a84ffe4cecbc3e8f781898a9e6974a9f1ce4a4a4561771f4ac28b', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2023 Q40
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2023 AND q.source_question_number = 40
    AND q.checksum = 'fc8ac25115e9f464c926da62d71c1f8a6474400338ab698400e8acbfd98b3458'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2023 Q40 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '2054.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '2054. 12                           Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2023 PROCESSO SELETIVO – CURSO TÉCNICO DE NÍVEL MÉDIO NA FORMA INTEGRADA 2023 EDITAL Nº 40/2022 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2023 Q40 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '5cf6c089b94a1a8c199cf772447c1d4690c134ae50352697088ec5c78cabc2ef', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q3
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 3
    AND q.checksum = '3d131637461aff1c0bea447c2416e62f223c19d901b64ce400abaf7504646ab6'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q3 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'dar destaque às informações.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'dar destaque às informações. 4                                                     Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q3 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '3deea9f940ccafcdc7017daaf5d48ff062327a092eaaa726d6167b8a526a6b9f', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q7
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 7
    AND q.checksum = '74c5782a2c0bc061203f0460e8aaa1c50d3ae589990c8506b3bb03625f7445fd'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q7 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'dois períodos: um simples e um composto por coordenação e três orações.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'dois períodos: um simples e um composto por coordenação e três orações. Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024                            5 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q7 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'ed43f947746aa7dfddd9fdd290837efc64d878c74b45ba02d919bdd3a42079df', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q13
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 13
    AND q.checksum = '693822441715360bda3f16abf9421ab6221d1efe0b3cc177037be6135c5c3607'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q13 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'à COP28.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'à COP28. 6                                            Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q13 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'f36d6a49750e183d2209e3c90eceb9900fb41ec8da29cbed85ff21cac7e30fdf', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q18
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 18
    AND q.checksum = 'd050a9b67afebd691e56669f3e7764c35e953c06dbd9cf39f0607de0d770f828'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q18 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'decrescente em 2022 em relação a 2021.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'decrescente em 2022 em relação a 2021. Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024                                7 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q18 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'aa2980a43ccd25da812c2bbd1b4187290f83a64810c3bf3a0bdca1675eb5aef1', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q20
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 20
    AND q.checksum = '7e1a4a493925d617eb5a23f5a536ec4b769191258af36ce439425e7c0f1ae708'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q20 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'Texto 3 configura-se, predominantemente, como um texto argumentativo.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'Texto 3 configura-se, predominantemente, como um texto argumentativo. 8                                            Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q20 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'f8fde32528919d22a24632b7faaf13e17d7c5c3a9324db5876c8b152732244ec', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q24 [TRECHO COMPARTILHADO MANTIDO EM D — ver docs]
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 24
    AND q.checksum = '34b24b2e5f5dc99105488cb202a00e8e8ce03458ad9885878e07c2ee1791ef15'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q24 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '. 7 Considere o trecho para resolver as questões 25, 26 e 27. O tamanho dos aparelhos de televisão, em geral, é dado em polegadas e corresponde à medida da diagonal da tela do aparelho. Uma polegada é equivalente a 2,5 cm. No Texto 2, é possível ver uma televisão de formato retangular, sobre uma mesa. Suponha que as dimensões dessa tela plana são 95 cm de largura e 53 cm de altura.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '. 7 Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024                              9 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN Considere o trecho para resolver as questões 25, 26 e 27. O tamanho dos aparelhos de televisão, em geral, é dado em polegadas e corresponde à medida da diagonal da tela do aparelho. Uma polegada é equivalente a 2,5 cm. No Texto 2, é possível ver uma televisão de formato retangular, sobre uma mesa. Suponha que as dimensões dessa tela plana são 95 cm de largura e 53 cm de altura.';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q24 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '7bd836e2144dad7bbf4a46a89e63c74f06a2d0e99303e980c16d2a537940fec1', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q29
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 29
    AND q.checksum = '44904378152221db3b3decc2cd72ddcd760a73aa337007b4dd3abc8c70ed9788'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q29 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '11 mulheres e 08 homens.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '11 mulheres e 08 homens. 10                                         Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q29 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'e9f2ce48abf64332adcbbce7e279d904ca6e5e208d1b09dc46586f08d3515ff4', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q34
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 34
    AND q.checksum = '8857f73a7e80408b259d76315183c616c59912f00c485e77961b01e0ea054943'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q34 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '160.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '160. Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024                           11 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q34 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '7c69d7de2a2704019d0c9de82593229dc9d8b0c87c1af41bdff3af2555c15ffb', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q39
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 39
    AND q.checksum = '4328a8f62a6c4b651a95425ab334f23aaa8398e7a985869dcd2622b0a7e82dc8'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q39 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '8250 km.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '8250 km. 12                                           Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q39 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '95dddb65ef42ad8340e5548f3626c7e8f9bd905ced5b4ecb511bd69ba097c557', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q40
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 40
    AND q.checksum = 'ca2832ce96ecd49168c6f8c0c678fdb8006549dcbb780fe29569b859ef26a3f9'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2024 Q40 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '35.000.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '35.000. Processo Seletivo – Cursos Técnico de Nível Médio na Forma Integrada – 2024                       13 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA 2024 EDITAL Nº 078/2023 – PROEN/RN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2024 Q40 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '7f362951655b4b2be4f6c6c08e502b5e839027e6c4583657c5cf9437aa472c37', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q2
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 2
    AND q.checksum = 'c64c2ebbf2b2eb6ce8199ba64715d1af58bcda61690623a3668cbb3b9bd468c8'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q2 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'a um acordo para cumprir o protocolo do Programa Acesso Mais Seguro.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'a um acordo para cumprir o protocolo do Programa Acesso Mais Seguro. IFRN – Exame de Seleção – 2025                                                                                        3 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q2 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'df7c126444b328d56a8799f96db14cbd4c47752fa3654458286268bd576f6f9e', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q7
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 7
    AND q.checksum = '0ab662b53d6d1b944880afe095a82549d8cf9a86b4c5728533266e52d6ce88df'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q7 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'em ambas as ocorrências, eles antecedem uma enumeração.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'em ambas as ocorrências, eles antecedem uma enumeração. IFRN – Exame de Seleção – 2025                                                                                  4 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q7 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'd02eecb9fbb6b553fc31984f30934d9063b9aa2ee214821c5e5a72fc7e9014e8', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q12
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 12
    AND q.checksum = 'e5e8578c55dbda176e0a59fe34eeb34c30316ac4c0336e55955c744e462b7a8b'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q12 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'a interação entre as crianças é estimulada pelo uso dos celulares.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'a interação entre as crianças é estimulada pelo uso dos celulares. IFRN – Exame de Seleção – 2025                                                                              5 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q12 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'c5b9d9b5e25a96d37382439beaca9e5d21df9bb79701a0e8dfab33a87506705f', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q18
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 18
    AND q.checksum = '9f1470495986f979dd66f8f514810d87dc818a79dfa52c075d99c30f3e63aa1a'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q18 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'seu registro de linguagem é o mesmo que o do Texto 1.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'seu registro de linguagem é o mesmo que o do Texto 1. IFRN – Exame de Seleção – 2025                                                                  6 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q18 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'bb5b5131bf02d0c72dd2008bc6b4bbde72d206eea986a699d05edf76ab746f57', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q20
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 20
    AND q.checksum = '14707b65083ea7c01b485494f56829c608cdd12e84dfd70ee1b72f278db7f1d7'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q20 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'tipo textual.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'tipo textual. IFRN – Exame de Seleção – 2025                                                              7 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q20 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '74d7d26df5d5552cdf672f65b1a06a5b22b15837224829652487f6e04871f635', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q25
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 25
    AND q.checksum = 'afa7937c76c74c465be3e9d3c5599b63bdefd50dc688d59b0f42e78482bb6c3b'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q25 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '400.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '400. IFRN – Exame de Seleção – 2025                                                                              8 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q25 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'ecc134e418e814d16804634bc0f43f158f1f464de67768805a3ca9f81d46dbd5', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q27
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 27
    AND q.checksum = '067a96db5661cb27d9e500ac3ad8515f28925550129b5a6448ae35a30a34b993'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q27 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '4,0.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '4,0. IFRN – Exame de Seleção – 2025                                                                         9 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q27 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '743fdf35c3d7f6ba0cbcea2fb6dc8450381139459be70124a2e6438593ce9d37', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q32
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 32
    AND q.checksum = 'cae9ff7bb09adc1a8f1bb539de0f28bb9d3095841e22b2c51b6f5b8578eb8e1a'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q32 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'R$ 6000,00.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'R$ 6000,00. IFRN – Exame de Seleção – 2025                                                                             10 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q32 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '9dc0fd53aa54246e439b59e28885d847c562367ce02b4ca61a72d584032663fe', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q36
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 36
    AND q.checksum = 'b7a5564b5a2a46cea1898cedc1f0b382e98a77f59a2f0bc34d4d32ecf6a515fd'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q36 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '. 30', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '. 30 IFRN – Exame de Seleção – 2025                                                                            11 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q36 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '73681924586f3dabe223d9e4b4dcb0db08f71ad899768b6fc14eed58a2671545', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q40
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 40
    AND q.checksum = '38f1e1555b676c8eac739e103d4b0f5c0f96a65b107cec4269a09d7587b67dba'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2025 Q40 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '65.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '65. IFRN – Exame de Seleção – 2025                                                                           12 IFRN – PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 23/2024 – PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2025 Q40 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'd57077bd6dbc77fabb83c64e1dbfff64a8dd319b4a3b9061214ea4fe9a9adb0f', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q2
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 2
    AND q.checksum = '48fbaac45f8699c37e4838906e862d160f3649082b2ade65d9f4cc031ac58707'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2026 Q2 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'implementar os compromissos globais de combate às mudanças climáticas. 4', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'implementar os compromissos globais de combate às mudanças climáticas. 4 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 48/2025-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2026 Q2 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'd03cbadacc09ff9bb36b7aa58a7e080f046e62825376630c71e8b3b3674525ee', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q7 [TRECHO COMPARTILHADO MANTIDO EM D — ver docs]
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 7
    AND q.checksum = '97bcacd44e6fdc98dbe8ecc95bfd55b7ab29d5afb532fdeffbb04e8a85aec299'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2026 Q7 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'sintetizar, em linhas gerais, as metas traçadas para a edição anterior. 5 O trecho a seguir deve ser utilizado para responder às questões de 8 a 10. A conferência é dividida em Zona Azul e Zona Verde. A Zona Azul, que(1) é gerenciada diretamente pela ONU, é onde(2) acontecem as negociações políticas e os encontros diplomáticos(3). Já a Zona Verde sedia painéis para o público geral, apresentação de ONG e outras atividades, inclusive as culturais.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'sintetizar, em linhas gerais, as metas traçadas para a edição anterior. 5 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 48/2025-PROEN/IFRN O trecho a seguir deve ser utilizado para responder às questões de 8 a 10. A conferência é dividida em Zona Azul e Zona Verde. A Zona Azul, que(1) é gerenciada diretamente pela ONU, é onde(2) acontecem as negociações políticas e os encontros diplomáticos(3). Já a Zona Verde sedia painéis para o público geral, apresentação de ONG e outras atividades, inclusive as culturais.';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2026 Q7 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '3145e8453269fe2cb33af98da0018357b04aa99a7c77709eec03ea329746f9b2', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q12
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 12
    AND q.checksum = '99c0b28645dbbb24c47f3d86ce009808028a3390c774d778882bb3b29d2280d0'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2026 Q12 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'postergar. 6', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'postergar. 6 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 48/2025-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2026 Q12 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '39b0dd9481b0f585ccdbbf491e497e336daf8f8ab6fcd1d4f0c9a99ccaa09786', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q18
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 18
    AND q.checksum = 'faa18d99b83f27b67bfe2d50a8eaa0f540bef1cf431487c222cd031382d23c39'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2026 Q18 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'presença da linguagem verbal, materializada no diálogo entre os trabalhadores, em contraste com o título do texto. 7', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'presença da linguagem verbal, materializada no diálogo entre os trabalhadores, em contraste com o título do texto. 7 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 48/2025-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2026 Q18 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '2ff5561f8d9a88298987a30dac9740c4bcbb28c3da376767272d4d430393bc5f', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q23
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 23
    AND q.checksum = '83630052a78f5e04bc595c3b8351d508a3a1a24bb8e9b51c8cafe77e5fb34cfd'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2026 Q23 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '20%. 8', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '20%. 8 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 48/2025-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2026 Q23 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'dbbb727cd38f9c0f1db076839baff5870ee66dcbbb448c59277946c05904cf15', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q28
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 28
    AND q.checksum = 'b1f7fd08b79b2f32af68c8d7f09f29c8f7c0da8bd218ac15665c464b79f76c9c'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2026 Q28 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '31%. 9', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '31%. 9 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 48/2025-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2026 Q28 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'e1950a8227f301952a610422fa7de47fae67717c638a93dbfd7039275d63646e', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q31
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 31
    AND q.checksum = 'dda7944e7b62518affd4d7bea243a0ff58dc8e66880c8b763c7a114eace6a40c'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2026 Q31 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '75 · (32 − 2π). 10', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '75 · (32 − 2π). 10 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 48/2025-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2026 Q31 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '09095331f562a494b9d9767e86de687815a291c12b0870bfbaf873eb3fda1697', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q36
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 36
    AND q.checksum = '653bfe4ce16bf1eb623211a2e25121c1168159144a74d9775788423c35c6c428'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2026 Q36 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '25. 11', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '25. 11 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 48/2025-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2026 Q36 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'd67d1b9c41a2f51b0019f492f1894bae76eb565ed450db05d3cda24e3f602603', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q40
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 40
    AND q.checksum = '5a039d6a93cc45e89131f8e6466397adefcb1f97a806bea24cac801fa9c9047e'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V6: 2026 Q40 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '300 e 500. 12', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '300 e 500. 12 PROCESSO SELETIVO PARA OS CURSOS TÉCNICOS DE NÍVEL MÉDIO NA FORMA INTEGRADA EDITAL Nº 48/2025-PROEN/IFRN';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V6: 2026 Q40 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '48b90ea01b786ad16f093c6a9253c7adf8f38780b6ff60b73a8869a7d2a0d015', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  IF v_n <> 60 THEN
    RAISE EXCEPTION 'V6: esperado 60 questoes, processadas %', v_n;
  END IF;
END $$;

-- Verificacao pos-migracao: nenhum texto de opcao pode reter boilerplate.
DO $$
DECLARE
  v_left INT;
BEGIN
  SELECT count(*) INTO v_left FROM question_options
  WHERE option_text ILIKE '%processo seletivo%forma integrada%'
     OR option_text ILIKE '%PROEN/IFRN' OR option_text ILIKE '%PROEN/RN';
  IF v_left <> 0 THEN
    RAISE EXCEPTION 'V6: ainda ha % opcoes com rodape', v_left;
  END IF;
END $$;
