#!/usr/bin/env python3
"""TASK B.3 — PNGs livres EAJ → frontend/assets/figures/eaj/<ano>/Q<NN>.webp + manifest.

Fonte de extração: data/provas/EAJ/<ano>/*.png (curadoria; nomes livres).
Os PNGs agrupados multi-questão são UM texto-base/figura compartilhado
(verificação visual B.3) e desmembram-se por questão via DUPLICAÇÃO do
mesmo recorte (padrão IFRN: docs/figuras-estrategia.md §2 — "Duplicar é
preferível"; md5 idênticos entre Q16–Q20/2020 comprovam o padrão).

  2021 (13 PNGs → 17 arquivos, 17 chaves):
    Q11_Q12_Q13_tirinha.png → Q11,Q12,Q13 (tirinha Calvin, Texto 02)
    Q14_Q15_censo.png       → Q14,Q15 (infográfico IBGE Censo Agro, Texto 03)
    Q18/Q21/Q22/Q27/Q28/Q34/Q40/Q43/Q44/Q46 → 1:1
    Q47_Q48_mapa.png        → Q47,Q48 (Mapa 1 rebanho bovino)
  2022 (12 PNGs → 25 arquivos, 23 chaves):
    Q08_Q09.png                       → Q08,Q09 (fragmento gêneros musicais)
    Q10_.._Q38_Q39.png (Texto 02)     → Q10–Q14,Q39 + Q38-2 (2ª figura da Q38)
    Q15_.._Q40.png (Texto 03)         → Q15–Q20,Q40
    Q21/Q23/Q24/Q28/Q33/Q34/Q38 → 1:1 ; Q27.png → Q27, Q27_02.png → Q27-2
  2025 (8 PNGs → 18 arquivos, 18 chaves):
    Q09_Q10.png            → Q09,Q10 (trecho Texto 1)
    Q14_.._Q20.png (charge IA, Texto 2) → Q14–Q20
    Q21_Q22_Q23.png (Texto 3 queimadas) → Q21,Q22,Q23
    Q30_Q31.png            → Q30,Q31 (fragmento desmatamento)
    Q34/Q36/Q37/Q38 → 1:1

Regras (TASKS.md B.3): lado maior ≤1600px, <300KB, chave "EAJ-<ano>-<n>",
page = "DESCONHECIDA" (extracted B.1 marca pageStatus DESCONHECIDO; nunca
inventar página), alt obrigatório, credit EAJ/Comperve. Questão sem recorte
= aviso do check (nunca imagem inventada).

Uso:
    python3 scripts/analysis/build_eaj_figures.py [--rebuild]
Idempotente: pula arquivos existentes (a menos de --rebuild); manifest é
reescrito de forma determinística preservando as chaves IFRN intactas.
"""

from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
SRC_DIR = REPO / "data" / "provas" / "EAJ"
ASSETS = REPO / "frontend" / "assets" / "figures"
MANIFEST = ASSETS / "manifest.json"

MAX_SIDE = 1600
MAX_KB = 300

CREDIT = ("Fonte: EAJ/UFRN (Comperve) — Caderno EAJ-{ano}, "
          "página DESCONHECIDA (recorte para estudo).")

# (png_relativo, ano, [(numero, sufixo)]) — sufixo "" = Q<NN>.webp
PLAN: list[tuple[str, str, list[tuple[int, str]]]] = [
    ("2021/Q11_Q12_Q13_tirinha.png", "2021", [(11, ""), (12, ""), (13, "")]),
    ("2021/Q14_Q15_censo.png", "2021", [(14, ""), (15, "")]),
    ("2021/Q18_tipobanana.png", "2021", [(18, "")]),
    ("2021/Q21_nutricaoatleta.png", "2021", [(21, "")]),
    ("2021/Q22_grafico.png", "2021", [(22, "")]),
    ("2021/Q27_consumoalimento.png", "2021", [(27, "")]),
    ("2021/Q28_triangulo.png", "2021", [(28, "")]),
    ("2021/Q34_afirmativas.png", "2021", [(34, "")]),
    ("2021/Q40_esquematransporte.png", "2021", [(40, "")]),
    ("2021/Q43_figura.png", "2021", [(43, "")]),
    ("2021/Q44_fragmentotextual.png", "2021", [(44, "")]),
    ("2021/Q46_imagem.png", "2021", [(46, "")]),
    ("2021/Q47_Q48_mapa.png", "2021", [(47, ""), (48, "")]),
    ("2022/Q08_Q09.png", "2022", [(8, ""), (9, "")]),
    ("2022/Q10_Q11_Q12_Q13_Q14_Q38_Q39.png", "2022",
     [(10, ""), (11, ""), (12, ""), (13, ""), (14, ""), (39, ""), (38, "-2")]),
    ("2022/Q15_Q16_Q17_Q18_Q19_Q20_Q40.png", "2022",
     [(15, ""), (16, ""), (17, ""), (18, ""), (19, ""), (20, ""), (40, "")]),
    ("2022/Q21.png", "2022", [(21, "")]),
    ("2022/Q23.png", "2022", [(23, "")]),
    ("2022/Q24.png", "2022", [(24, "")]),
    ("2022/Q27.png", "2022", [(27, "")]),
    ("2022/Q27_02.png", "2022", [(27, "-2")]),
    ("2022/Q28.png", "2022", [(28, "")]),
    ("2022/Q33.png", "2022", [(33, "")]),
    ("2022/Q34.png", "2022", [(34, "")]),
    ("2022/Q38.png", "2022", [(38, "")]),
    ("2025/Q09_Q10.png", "2025", [(9, ""), (10, "")]),
    ("2025/Q14_Q15_Q16_Q17_Q18_Q19_Q20.png", "2025",
     [(14, ""), (15, ""), (16, ""), (17, ""), (18, ""), (19, ""), (20, "")]),
    ("2025/Q21_Q22_Q23.png", "2025", [(21, ""), (22, ""), (23, "")]),
    ("2025/Q30_Q31.png", "2025", [(30, ""), (31, "")]),
    ("2025/Q34.png", "2025", [(34, "")]),
    ("2025/Q36.png", "2025", [(36, "")]),
    ("2025/Q37.png", "2025", [(37, "")]),
    ("2025/Q38.png", "2025", [(38, "")]),
]

# alt por (ano, numero, sufixo) — tipo do visual + vínculo, sem transcrever
# valores não conferidos além do já transcrito nos questoes.md (fonte).
ALT: dict[tuple[str, int, str], str] = {
    ("2021", 11, ""): "Tirinha de Calvin e Haroldo (4 quadrinhos) — Texto 02 da prova EAJ-2021, base das questões 11 a 13.",
    ("2021", 12, ""): "Tirinha de Calvin e Haroldo (4 quadrinhos) — Texto 02 da prova EAJ-2021, base das questões 11 a 13.",
    ("2021", 13, ""): "Tirinha de Calvin e Haroldo (4 quadrinhos) — Texto 02 da prova EAJ-2021, base das questões 11 a 13.",
    ("2021", 14, ""): "Infográfico do IBGE (Censo Agro 2017 — Rio Grande do Norte) — Texto 03 da prova EAJ-2021, base das questões 14 e 15.",
    ("2021", 15, ""): "Infográfico do IBGE (Censo Agro 2017 — Rio Grande do Norte) — Texto 03 da prova EAJ-2021, base das questões 14 e 15.",
    ("2021", 18, ""): "Tabela de tipos de banana e calorias por 100 g (questão 18 da prova EAJ-2021).",
    ("2021", 21, ""): "Tabela da dieta semanal de raízes em gramas (questão 21 da prova EAJ-2021).",
    ("2021", 22, ""): "Gráfico de câmbio mensal (jun–dez/2020) em R$ (questão 22 da prova EAJ-2021).",
    ("2021", 27, ""): "Quadro de parâmetro × consumo mensal de alimentos em kg (questão 27 da prova EAJ-2021).",
    ("2021", 28, ""): "Triângulo retângulo ABC com altura h e projeções m = 2 cm e n = 8 cm (questão 28 da prova EAJ-2021).",
    ("2021", 34, ""): "Afirmativas sobre o Texto 01 para análise (questão 34 da prova EAJ-2021).",
    ("2021", 40, ""): "Esquema de transporte de caixas (plano horizontal + rampa até a altura h) da questão 40 da prova EAJ-2021.",
    ("2021", 43, ""): "Reprodução do quadro Retirantes (1944), de Candido Portinari (questão 43 da prova EAJ-2021).",
    ("2021", 44, ""): "Fragmento textual para leitura (questão 44 da prova EAJ-2021).",
    ("2021", 46, ""): "Gravura de Theodor de Bry (1593) sobre o Brasil (questão 46 da prova EAJ-2021).",
    ("2021", 47, ""): "Mapa 1 — principais regiões com rebanho bovino no Brasil em 2016 (questões 47 e 48 da prova EAJ-2021).",
    ("2021", 48, ""): "Mapa 1 — principais regiões com rebanho bovino no Brasil em 2016 (questões 47 e 48 da prova EAJ-2021).",
    ("2022", 8, ""): "Fragmento textual sobre os gêneros musicais preferidos (questões 8 e 9 da prova EAJ-2022).",
    ("2022", 9, ""): "Fragmento textual sobre os gêneros musicais preferidos (questões 8 e 9 da prova EAJ-2022).",
    ("2022", 10, ""): "Charge-diálogo sobre falar outros idiomas ('Aí varêia') — Texto 02 da prova EAJ-2022.",
    ("2022", 11, ""): "Charge-diálogo sobre falar outros idiomas ('Aí varêia') — Texto 02 da prova EAJ-2022.",
    ("2022", 12, ""): "Charge-diálogo sobre falar outros idiomas ('Aí varêia') — Texto 02 da prova EAJ-2022.",
    ("2022", 13, ""): "Charge-diálogo sobre falar outros idiomas ('Aí varêia') — Texto 02 da prova EAJ-2022.",
    ("2022", 14, ""): "Charge-diálogo sobre falar outros idiomas ('Aí varêia') — Texto 02 da prova EAJ-2022.",
    ("2022", 39, ""): "Charge-diálogo sobre falar outros idiomas ('Aí varêia') — Texto 02 da prova EAJ-2022, base da questão 39.",
    ("2022", 38, "-2"): "Charge-diálogo sobre falar outros idiomas ('Aí varêia') — Texto 02 da prova EAJ-2022, contexto da questão 38.",
    ("2022", 15, ""): "Tirinha de 4 quadrinhos ('Bom dia, galera') — Texto 03 da prova EAJ-2022.",
    ("2022", 16, ""): "Tirinha de 4 quadrinhos ('Bom dia, galera') — Texto 03 da prova EAJ-2022.",
    ("2022", 17, ""): "Tirinha de 4 quadrinhos ('Bom dia, galera') — Texto 03 da prova EAJ-2022.",
    ("2022", 18, ""): "Tirinha de 4 quadrinhos ('Bom dia, galera') — Texto 03 da prova EAJ-2022.",
    ("2022", 19, ""): "Tirinha de 4 quadrinhos ('Bom dia, galera') — Texto 03 da prova EAJ-2022.",
    ("2022", 20, ""): "Tirinha de 4 quadrinhos ('Bom dia, galera') — Texto 03 da prova EAJ-2022.",
    ("2022", 40, ""): "Tirinha de 4 quadrinhos ('Bom dia, galera') — Texto 03 da prova EAJ-2022, base da questão 40.",
    ("2022", 21, ""): "Esquema da dança do pau de fitas (mastro m, fita f, distância d) da questão 21 da prova EAJ-2022.",
    ("2022", 23, ""): "Trapézio retângulo (área do espaço do evento) da questão 23 da prova EAJ-2022.",
    ("2022", 24, ""): "Triângulo retângulo ABC com regiões BAD/BCD (questão 24 da prova EAJ-2022).",
    ("2022", 27, ""): "Obra 'A Árvore', do artista potiguar Novenil Barros (questão 27 da prova EAJ-2022).",
    ("2022", 27, "-2"): "Losango com ângulos x/2 e x + 30 (questão 27 da prova EAJ-2022).",
    ("2022", 28, ""): "Mosaico do piso do salão (triângulos, quadrados e losangos) da questão 28 da prova EAJ-2022.",
    ("2022", 33, ""): "Preços de ingressos por setor do teatro (questão 33 da prova EAJ-2022).",
    ("2022", 34, ""): "Planta da Plateia A em formato de 'T' (questão 34 da prova EAJ-2022).",
    ("2022", 38, ""): "Retângulo do estacionamento sombreado (lados x e y) da questão 38 da prova EAJ-2022.",
    ("2025", 9, ""): "Trecho do Texto 1 sobre IA (custo de processamento e memória) — questões 9 e 10 da prova EAJ-2025.",
    ("2025", 10, ""): "Trecho do Texto 1 sobre IA (custo de processamento e memória) — questões 9 e 10 da prova EAJ-2025.",
    ("2025", 14, ""): "Charge sobre IA ('avanço da inteligência artificial' × 'retrocesso da inteligência natural') — Texto 2 da prova EAJ-2025.",
    ("2025", 15, ""): "Charge sobre IA ('avanço da inteligência artificial' × 'retrocesso da inteligência natural') — Texto 2 da prova EAJ-2025.",
    ("2025", 16, ""): "Charge sobre IA ('avanço da inteligência artificial' × 'retrocesso da inteligência natural') — Texto 2 da prova EAJ-2025.",
    ("2025", 17, ""): "Charge sobre IA ('avanço da inteligência artificial' × 'retrocesso da inteligência natural') — Texto 2 da prova EAJ-2025.",
    ("2025", 18, ""): "Charge sobre IA ('avanço da inteligência artificial' × 'retrocesso da inteligência natural') — Texto 2 da prova EAJ-2025.",
    ("2025", 19, ""): "Charge sobre IA ('avanço da inteligência artificial' × 'retrocesso da inteligência natural') — Texto 2 da prova EAJ-2025.",
    ("2025", 20, ""): "Charge sobre IA ('avanço da inteligência artificial' × 'retrocesso da inteligência natural') — Texto 2 da prova EAJ-2025.",
    ("2025", 21, ""): "Texto 3 — 'Queimadas no Brasil' (MapBiomas 1985–2020) — questões 21 a 23 da prova EAJ-2025.",
    ("2025", 22, ""): "Texto 3 — 'Queimadas no Brasil' (MapBiomas 1985–2020) — questões 21 a 23 da prova EAJ-2025.",
    ("2025", 23, ""): "Texto 3 — 'Queimadas no Brasil' (MapBiomas 1985–2020) — questões 21 a 23 da prova EAJ-2025.",
    ("2025", 30, ""): "Fragmento sobre desmatamento (+22,3% / 2,05 Mha, MapBiomas RAD2022) — questões 30 e 31 da prova EAJ-2025.",
    ("2025", 31, ""): "Fragmento sobre desmatamento (+22,3% / 2,05 Mha, MapBiomas RAD2022) — questões 30 e 31 da prova EAJ-2025.",
    ("2025", 34, ""): "Gráfico de barras de classificados OBM 2019–2022 (meninas × meninos) da questão 34 da prova EAJ-2025.",
    ("2025", 36, ""): "Barra de doce de leite (paralelepípedo 20 × 8 × 5 cm) da questão 36 da prova EAJ-2025.",
    ("2025", 37, ""): "Caixa d'água cilíndrica (40 cm de diâmetro × 50 cm de altura) da questão 37 da prova EAJ-2025.",
    ("2025", 38, ""): "Três quadrados adjacentes da questão 38 da prova EAJ-2025.",
}


def webp_name(num: int, suffix: str) -> str:
    return f"Q{num:02d}{suffix}.webp"


def convert(src: Path, dst: Path) -> None:
    for quality in (82, 70, 60):
        r = subprocess.run(
            ["magick", str(src), "-resize", f"{MAX_SIDE}x{MAX_SIDE}>",
             "-quality", str(quality), str(dst)],
            capture_output=True, text=True)
        if r.returncode != 0:
            raise SystemExit(f"magick falhou em {src.name}: {r.stderr[:300]}")
        if dst.stat().st_size / 1024 < MAX_KB:
            return
    raise SystemExit(f"{dst.name} segue > {MAX_KB} KB após q60 — curadoria manual")


def main() -> None:
    rebuild = "--rebuild" in sys.argv
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    figures = manifest.setdefault("figures", {})

    # Checagem de sanidade do plano antes de tocar em disco.
    plan_keys = {(ano, n, s) for _, ano, l in PLAN for (n, s) in l}
    if set(ALT.keys()) != plan_keys:
        raise SystemExit("ALT fora de sincronia com o plano: "
                         f"{sorted(set(ALT) ^ plan_keys)}")
    target_source: dict[str, str] = {}
    for png_rel, ano, items in PLAN:
        if not (SRC_DIR / png_rel).exists():
            raise SystemExit(f"PNG-fonte ausente: {png_rel}")
        for num, suffix in items:
            key = f"eaj/{ano}/{webp_name(num, suffix)}"
            if key in target_source:
                raise SystemExit(f"alvo duplicado no plano: {key}")
            target_source[key] = f"data/provas/EAJ/{png_rel}"

    made, reused = 0, 0
    # Agrupa arquivos por questão para o manifest.
    per_question: dict[tuple[str, int], list[str]] = {}
    for png_rel, ano, items in PLAN:
        src = SRC_DIR / png_rel
        outdir = ASSETS / "eaj" / ano
        outdir.mkdir(parents=True, exist_ok=True)
        for num, suffix in items:
            dst = outdir / webp_name(num, suffix)
            if dst.exists() and not rebuild:
                reused += 1
            else:
                convert(src, dst)
                made += 1
            per_question.setdefault((ano, num), []).append(
                f"eaj/{ano}/{webp_name(num, suffix)}")

    for (ano, num), files in sorted(per_question.items()):
        files.sort(key=lambda f: (len(f), f))  # Q<NN>.webp antes de Q<NN>-2
        key = f"EAJ-{ano}-{num}"
        if key in figures and not key.startswith("EAJ-"):
            raise SystemExit(f"colisão com chave IFRN: {key}")
        # Alts das figuras da questão, na ordem dos arquivos.
        suffixes = ["-2" if f.endswith("-2.webp") else "" for f in files]
        alt = " | ".join(ALT[(ano, num, s)] for s in suffixes)
        sources = sorted({target_source[f] for f in files})
        entry = figures.get(key, {})
        if not isinstance(entry, dict):
            raise SystemExit(f"[{key}] entrada existente não é objeto")
        entry.update({
            "files": files,
            "page": "DESCONHECIDA",
            "alt": alt,
            "credit": CREDIT.format(ano=ano),
            "source_png": sources[0] if len(sources) == 1 else sources,
        })
        figures[key] = entry

    MANIFEST.write_text(json.dumps(manifest, ensure_ascii=False, indent=2)
                        + "\n", encoding="utf-8")
    print(f"webp: {made} convertidos, {reused} reaproveitados "
          f"({len(per_question)} chaves EAJ)")


if __name__ == "__main__":
    main()
