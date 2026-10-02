-- V7__footer_pagenumbers.sql — curadoria de dados (limpeza de rodape).
-- Remove da alternativa D o boilerplate de rodape/cabecalho do caderno
-- ("Processo Seletivo ..." / "EDITAL Nº ... – PROEN/..." / nº de pagina)
-- que restaram colados apos a V6 (a primeira versao do strip
-- nao enxergava o nº solto quando o rodape vinha depois dele).
-- Fonte: extrator v1.1.0 (scripts/analysis/extract_questions.py),
-- diff auditado contra o estado pos-V6 (12/12 remocoes puras de nº de pagina,
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
  -- 2020 Q37
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2020 AND q.source_question_number = 37
    AND q.checksum = '3a92b795175aa551901fc96369beb0cc9d4f087a6e03fa054c0f8e30b6994031'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2020 Q37 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '. 50';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2020 Q37 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '25ce570b259a6f2338e6223f70765a2c96b9349c3092faf296a039562569b166', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2024 Q24 [TRECHO COMPARTILHADO MANTIDO EM D — ver docs]
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2024 AND q.source_question_number = 24
    AND q.checksum = '7bd836e2144dad7bbf4a46a89e63c74f06a2d0e99303e980c16d2a537940fec1'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2024 Q24 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '. Considere o trecho para resolver as questões 25, 26 e 27. O tamanho dos aparelhos de televisão, em geral, é dado em polegadas e corresponde à medida da diagonal da tela do aparelho. Uma polegada é equivalente a 2,5 cm. No Texto 2, é possível ver uma televisão de formato retangular, sobre uma mesa. Suponha que as dimensões dessa tela plana são 95 cm de largura e 53 cm de altura.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '. 7 Considere o trecho para resolver as questões 25, 26 e 27. O tamanho dos aparelhos de televisão, em geral, é dado em polegadas e corresponde à medida da diagonal da tela do aparelho. Uma polegada é equivalente a 2,5 cm. No Texto 2, é possível ver uma televisão de formato retangular, sobre uma mesa. Suponha que as dimensões dessa tela plana são 95 cm de largura e 53 cm de altura.';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2024 Q24 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '723436062887c3d5955867a978f74eef1504bb6f7f14ed3b9f4e42db15a7d4a8', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2025 Q36
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2025 AND q.source_question_number = 36
    AND q.checksum = '73681924586f3dabe223d9e4b4dcb0db08f71ad899768b6fc14eed58a2671545'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2025 Q36 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '. 30';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2025 Q36 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'b74631ff92d029b282db06cba3594677e8ceb937e840189c673ad1d050e04e26', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q2
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 2
    AND q.checksum = 'd03cbadacc09ff9bb36b7aa58a7e080f046e62825376630c71e8b3b3674525ee'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2026 Q2 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'implementar os compromissos globais de combate às mudanças climáticas.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'implementar os compromissos globais de combate às mudanças climáticas. 4';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2026 Q2 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'a9b48bbd7398609d429c511c825379d5592ce69f9453973abf126a415fe13047', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q7 [TRECHO COMPARTILHADO MANTIDO EM D — ver docs]
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 7
    AND q.checksum = '3145e8453269fe2cb33af98da0018357b04aa99a7c77709eec03ea329746f9b2'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2026 Q7 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'sintetizar, em linhas gerais, as metas traçadas para a edição anterior. O trecho a seguir deve ser utilizado para responder às questões de 8 a 10. A conferência é dividida em Zona Azul e Zona Verde. A Zona Azul, que(1) é gerenciada diretamente pela ONU, é onde(2) acontecem as negociações políticas e os encontros diplomáticos(3). Já a Zona Verde sedia painéis para o público geral, apresentação de ONG e outras atividades, inclusive as culturais.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'sintetizar, em linhas gerais, as metas traçadas para a edição anterior. 5 O trecho a seguir deve ser utilizado para responder às questões de 8 a 10. A conferência é dividida em Zona Azul e Zona Verde. A Zona Azul, que(1) é gerenciada diretamente pela ONU, é onde(2) acontecem as negociações políticas e os encontros diplomáticos(3). Já a Zona Verde sedia painéis para o público geral, apresentação de ONG e outras atividades, inclusive as culturais.';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2026 Q7 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '49cbf761d38d036353d40d1367d5a3843d8b94bb29721a7827985c3a14dd5d5b', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q12
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 12
    AND q.checksum = '39b0dd9481b0f585ccdbbf491e497e336daf8f8ab6fcd1d4f0c9a99ccaa09786'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2026 Q12 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'postergar.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'postergar. 6';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2026 Q12 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'd6a148eba616df6d5bba2d2a73dbab90c62247c1f2c2ff050e59fbc11e58f8f9', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q18
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 18
    AND q.checksum = '2ff5561f8d9a88298987a30dac9740c4bcbb28c3da376767272d4d430393bc5f'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2026 Q18 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = 'presença da linguagem verbal, materializada no diálogo entre os trabalhadores, em contraste com o título do texto.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = 'presença da linguagem verbal, materializada no diálogo entre os trabalhadores, em contraste com o título do texto. 7';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2026 Q18 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '05d604a1432bd4c5f76d1f00ce0e18dac74392988c79099d66f576228cc4f4e1', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q23
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 23
    AND q.checksum = 'dbbb727cd38f9c0f1db076839baff5870ee66dcbbb448c59277946c05904cf15'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2026 Q23 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '20%.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '20%. 8';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2026 Q23 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = 'e41f8b765796069eb2dc10975686dc14d08c21a5676655acdc6517beb320cb97', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q28
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 28
    AND q.checksum = 'e1950a8227f301952a610422fa7de47fae67717c638a93dbfd7039275d63646e'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2026 Q28 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '31%.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '31%. 9';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2026 Q28 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '1abf0329384bd6d0723219511e116b70c8150793efde53a7a4c4815a7e7bb1ce', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q31
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 31
    AND q.checksum = '09095331f562a494b9d9767e86de687815a291c12b0870bfbaf873eb3fda1697'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2026 Q31 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '75 · (32 − 2π).', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '75 · (32 − 2π). 10';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2026 Q31 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '99264dd1be21fe06bbb9473ccd8cad6f5855e4c6aefe8195a9a788c201f67f8f', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q36
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 36
    AND q.checksum = 'd67d1b9c41a2f51b0019f492f1894bae76eb565ed450db05d3cda24e3f602603'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2026 Q36 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '25.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '25. 11';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2026 Q36 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '5bd36e619bed8d5a8bbae1e380b29b9915fc277ca542a1e78ad9c180e6d1a0e6', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  -- 2026 Q40
  SELECT q.id INTO v_qid FROM questions q
  WHERE q.source_type = 'OFFICIAL'
    AND q.source_year = 2026 AND q.source_question_number = 40
    AND q.checksum = '48b90ea01b786ad16f093c6a9253c7adf8f38780b6ff60b73a8869a7d2a0d015'
  ;
  IF v_qid IS NULL THEN
    RAISE EXCEPTION 'V7: 2026 Q40 nao encontrado com checksum esperado (banco divergiu da fonte v1.0.0)';
  END IF;
  UPDATE question_options SET option_text = '300 e 500.', updated_at = now()
  WHERE question_id = v_qid AND label = 'D' AND option_text = '300 e 500. 12';
  IF NOT FOUND THEN
    RAISE EXCEPTION 'V7: 2026 Q40 opcao D divergiu da fonte v1.0.0';
  END IF;
  UPDATE questions SET checksum = '82317895031034a1d2eaff3563312a6da004d0c1357c9f0e9d089fdd86694984', updated_at = now() WHERE id = v_qid;
  v_n := v_n + 1;

  IF v_n <> 12 THEN
    RAISE EXCEPTION 'V7: esperado 12 questoes, processadas %', v_n;
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
    RAISE EXCEPTION 'V7: ainda ha % opcoes com rodape', v_left;
  END IF;
END $$;
