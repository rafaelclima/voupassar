-- V10__fix_fraction_options.sql — frações conferidas no PDF oficial.
-- Corrige alternativas extraídas com numerador/denominador invertidos
-- (commits 3f6703f/c29c814) pelos valores lidos no caderno: 2020 Q26,
-- 2022 Q23, 2024 Q21/Q24, 2025 Q33/Q36. 2020 Q37 mantida (idêntica).
-- Atualiza question_options + checksum (mesma regra do importador:
-- SHA-256 de statement + A-D normalizados NFC/whitespace), para o
-- importador continuar idempotente após a migração.
-- Condicional ao valor antigo: roda 2x sem efeito colateral.

-- 2020 Q26
UPDATE question_options SET option_text = '23/11', updated_at = now() WHERE label = 'A' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2020 AND source_question_number = 26);
UPDATE question_options SET option_text = '23/12', updated_at = now() WHERE label = 'B' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2020 AND source_question_number = 26);
UPDATE question_options SET option_text = '11/23', updated_at = now() WHERE label = 'C' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2020 AND source_question_number = 26);
UPDATE question_options SET option_text = '12/23', updated_at = now() WHERE label = 'D' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2020 AND source_question_number = 26);
UPDATE questions SET checksum = '95b3f5d3339538e17db6874f0ef602590da96f13583a12215c507557c878d450', updated_at = now() WHERE source_type = 'OFFICIAL' AND source_year = 2020 AND source_question_number = 26 AND checksum <> '95b3f5d3339538e17db6874f0ef602590da96f13583a12215c507557c878d450';

-- 2022 Q23
UPDATE question_options SET option_text = '5/24', updated_at = now() WHERE label = 'A' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2022 AND source_question_number = 23);
UPDATE question_options SET option_text = '5/2400', updated_at = now() WHERE label = 'B' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2022 AND source_question_number = 23);
UPDATE question_options SET option_text = '5/24000', updated_at = now() WHERE label = 'C' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2022 AND source_question_number = 23);
UPDATE question_options SET option_text = '5/240', updated_at = now() WHERE label = 'D' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2022 AND source_question_number = 23);
UPDATE questions SET checksum = 'd538111529c359479fb7805529e918a51d31b00855108df66b100db39ee0b4fa', updated_at = now() WHERE source_type = 'OFFICIAL' AND source_year = 2022 AND source_question_number = 23 AND checksum <> 'd538111529c359479fb7805529e918a51d31b00855108df66b100db39ee0b4fa';

-- 2024 Q21
UPDATE question_options SET option_text = '1/4', updated_at = now() WHERE label = 'A' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 21);
UPDATE question_options SET option_text = '1/8', updated_at = now() WHERE label = 'B' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 21);
UPDATE question_options SET option_text = '2/3', updated_at = now() WHERE label = 'C' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 21);
UPDATE question_options SET option_text = '3/5', updated_at = now() WHERE label = 'D' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 21);
UPDATE questions SET checksum = '501cbbe5b3fe7af4681753d7a565bb84626a3c02abc1fcf79e821d8cc08c4527', updated_at = now() WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 21 AND checksum <> '501cbbe5b3fe7af4681753d7a565bb84626a3c02abc1fcf79e821d8cc08c4527';

-- 2024 Q24
UPDATE question_options SET option_text = '2/5', updated_at = now() WHERE label = 'A' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 24);
UPDATE question_options SET option_text = '4/5', updated_at = now() WHERE label = 'B' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 24);
UPDATE question_options SET option_text = '2/7', updated_at = now() WHERE label = 'C' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 24);
UPDATE question_options SET option_text = '4/7', updated_at = now() WHERE label = 'D' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 24);
UPDATE questions SET checksum = 'bf694ae4d54eea271b8acddc7c7d88c9e80aa49ef322b029e0ba1cd3f3a62da3', updated_at = now() WHERE source_type = 'OFFICIAL' AND source_year = 2024 AND source_question_number = 24 AND checksum <> 'bf694ae4d54eea271b8acddc7c7d88c9e80aa49ef322b029e0ba1cd3f3a62da3';

-- 2025 Q33
UPDATE question_options SET option_text = '15/28', updated_at = now() WHERE label = 'A' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 33);
UPDATE question_options SET option_text = '13/56', updated_at = now() WHERE label = 'B' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 33);
UPDATE question_options SET option_text = '13/14', updated_at = now() WHERE label = 'C' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 33);
UPDATE question_options SET option_text = '13/28', updated_at = now() WHERE label = 'D' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 33);
UPDATE questions SET checksum = '98bcd46b0d429440686dc445e2ba6ad40f2c2d45f1e9a4093e5fa007f181a1ea', updated_at = now() WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 33 AND checksum <> '98bcd46b0d429440686dc445e2ba6ad40f2c2d45f1e9a4093e5fa007f181a1ea';

-- 2025 Q36
UPDATE question_options SET option_text = '25/30', updated_at = now() WHERE label = 'A' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 36);
UPDATE question_options SET option_text = '732/900', updated_at = now() WHERE label = 'B' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 36);
UPDATE question_options SET option_text = '741/900', updated_at = now() WHERE label = 'C' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 36);
UPDATE question_options SET option_text = '24/30', updated_at = now() WHERE label = 'D' AND question_id IN (SELECT id FROM questions WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 36);
UPDATE questions SET checksum = '4af34c9875316bca5236b1e875097c9b3bb16440c91aa5e6963a9bdf603596da', updated_at = now() WHERE source_type = 'OFFICIAL' AND source_year = 2025 AND source_question_number = 36 AND checksum <> '4af34c9875316bca5236b1e875097c9b3bb16440c91aa5e6963a9bdf603596da';
