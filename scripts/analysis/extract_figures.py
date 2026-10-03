#!/usr/bin/env python3
"""Gera recortes iniciais (.webp) a partir das páginas dos PDFs das edições.

Estratégia: para cada entrada no manifest com PENDENTE_REVISAO,
renderiza a página mencionada (`page`) via pdftoppm, converte para .webp
com redimensionamento (lado maior <= 1600 px, < 300 KB).

Não faz recorte preciso da figura — isso exige curadoria visual manual.
O arquivo gerado é a página completa, servindo como base para recorte
ou como referência visual imediata.
"""
import json
import subprocess
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
MANIFEST = REPO / "frontend" / "assets" / "figures" / "manifest.json"
ASSETS = REPO / "frontend" / "assets" / "figures"
PDF_DIR = REPO / "data" / "provas"

# Mapeia (ano, número) -> arquivo PDF (caderno/prova) daquela edição
PDF_MAP = {
    "2020": "2020/Prova.pdf",
    "2022": "2022/Prova_Exame_Tecnico_Integrado_2022_XNn8mg4.pdf",
    "2023": "2023/Caderno_de_provas.pdf",
    "2024": "2024/Cadernos_de_provas_-_Cursos_Tecnicos_Integrados_2024_.pdf",
    "2025": "2025/PROVA_IFRN_-_EXAME_DE_SELEÇÃO_2025.pdf",
    "2026": "2026/Caderno_de_Provas_-Técnico_Integrado_2026.pdf",
}


def run(cmd, **kwargs):
    result = subprocess.run(cmd, capture_output=True, text=True, **kwargs)
    if result.returncode != 0:
        print(f"COMANDO FALHOU: {' '.join(str(c) for c in cmd)}")
        print(f"STDERR: {result.stderr[:500]}")
        return False
    return True


def generate():
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    figures = manifest.get("figures", {})
    generated = 0
    skipped = 0

    for key, entry in figures.items():
        files = entry.get("files", [])
        if not files:
            skipped += 1
            continue
        page = entry.get("page")
        if not page or not isinstance(page, int):
            skipped += 1
            print(f"SKIP {key}: sem page válida ({page})")
            continue

        # Ex.: "2020-16" -> ano=2020, num=16
        parts = key.split("-")
        if len(parts) != 2:
            skipped += 1
            print(f"SKIP {key}: chave malformada")
            continue
        yr_str, num_str = parts
        pdf_rel = PDF_MAP.get(yr_str)
        if not pdf_rel:
            skipped += 1
            print(f"SKIP {key}: PDF da edição {yr_str} não mapeado")
            continue

        pdf_path = PDF_DIR / pdf_rel
        if not pdf_path.exists():
            skipped += 1
            print(f"SKIP {key}: PDF não encontrado: {pdf_path}")
            continue

        target_dir = ASSETS / yr_str
        target_dir.mkdir(parents=True, exist_ok=True)
        target_path = target_dir / Path(files[0]).name

        # Se já existe, pula (não sobrescreve)
        if target_path.exists():
            print(f"OK {key}: arquivo já existe ({target_path.name})")
            generated += 1
            continue

        # Renderiza a página como PNG (resolução 150 dpi para qualidade)
        png_path = target_path.with_suffix(".png")
        print(f"RENDERIZANDO {key}: página {page} de {pdf_path.name} ...")
        ok = run([
            "pdftoppm", "-png", "-r", "150",
            "-f", str(page), "-l", str(page),
            str(pdf_path), str(png_path.with_suffix(""))
        ])
        if not ok:
            skipped += 1
            continue
        # O arquivo gerado pelo pdftoppm é `<prefix>-<page>.png`
        # Prefixo = caminho sem extensão (ex.: .../2020/Q16)
        # Resultado = .../2020/Q16-6.png
        candidates = sorted(png_path.parent.glob(f"{png_path.stem}-*.png"))
        if not candidates:
            # Fallback: se o prefixo incluía o ano, pode ser .../2020/Q16-6.png
            # Mas se o nome do arquivo tem traço, pode ser .../Q16-6.png (já coberto)
            print(f"AVISO: arquivo PNG renderizado não encontrado para {key}")
            skipped += 1
            continue
        src_png = candidates[0]

        # Converte PNG para WEBP, redimensiona para lado maior <= 1600 px, < 300 KB
        # Usa ImageMagick
        print(f"CONVERTENDO {key}: {src_png.name} -> {target_path.name}")
        ok = run([
            "convert", str(src_png),
            "-resize", "1600x1600>",
            "-quality", "85",
            "-define", "webp:target-size=250KB",
            str(target_path)
        ])
        # Se falhou, tenta sem target-size específico
        if not ok:
            ok = run([
                "convert", str(src_png),
                "-resize", "1600x1600>",
                "-quality", "80",
                str(target_path)
            ])
        if ok and target_path.exists():
            size_kb = target_path.stat().st_size / 1024
            print(f"GERADO {key}: {target_path.name} ({size_kb:.0f} KB)")
            generated += 1
        else:
            print(f"FALHA na conversão para {key}")
            skipped += 1

        # Limpa PNG intermediário
        if src_png.exists():
            src_png.unlink()

    print(f"\nRESUMO: {generated} gerados, {skipped} pulados/falhos (de {len(figures)})")
    return generated, skipped


if __name__ == "__main__":
    generate()
