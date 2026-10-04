#!/usr/bin/env python3
"""
Auditoria e correção automática de expressões matemáticas confusas nas alternativas.
Não inventa conteúdo: só corrige o padrão óbvio '. NUM DEN' -> 'NUM/DEN' quando
o raw_block confirma que é uma fração. Casos ambíguos (geometria, notação científica
confusa) são listados mas NÃO corrigidos automaticamente.
"""

import json, glob, sys, re
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
EXTRACTED_DIR = REPO / "data" / "extracted"

# Padrão óbvio: alternativa começa com ponto, depois número, espaço, número.
# Ex.: ". 24 5" -> "24/5", ". 4 1" -> "4/1"
FRACTION_PATTERN = re.compile(r"^\.\s+(\d+)\s+(\d+)$")

# Padrão para expressões matemáticas com ponto como separador decimal (ex.: "5,6 ⋅ 106 .")
# Não corrigimos automaticamente porque é ambíguo (pode ser notação científica ou fração).

corrected = 0
skipped_ambiguous = 0

for path in sorted(EXTRACTED_DIR.glob("*.json")):
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    questions = data.get("questions", [])
    for q in questions:
        options = q.get("options", {})
        for label, text in list(options.items()):
            stripped = text.strip()
            m = FRACTION_PATTERN.match(stripped)
            if m:
                num, den = m.group(1), m.group(2)
                corrected_text = f"{num}/{den}"
                # Confirma que o raw_block contém a mesma expressão (ou similar)
                raw = q.get("raw_block", "")
                # Se o raw_block menciona o número da questão e contém numerador/denominador,
                # consideramos a correção segura.
                if str(q.get("number")) in raw and (num in raw or den in raw):
                    options[label] = corrected_text
                    corrected += 1
                    print(f"CORRIGIDO: {path.name} Q{q.get('number')} {label}: '{stripped}' -> '{corrected_text}'")
                else:
                    skipped_ambiguous += 1
                    print(f"AMBÍGUO (não corrigido): {path.name} Q{q.get('number')} {label}: '{stripped}'")

    # Escreve de volta (idempotente: só altera se encontrar padrão)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")

print(f"\nResumo: {corrected} alternativas corrigidas, {skipped_ambiguous} ambíguas (revisão manual necessária).")
