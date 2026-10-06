#!/usr/bin/env python3
"""Integra as figuras colocadas pelo usuário (data/provas/<ano>/) ao sistema.

Ações:
- Copia/renomeia cada arquivo para frontend/assets/figures/<ano>/
- Comprime se > 300 KB (convert -resize 1600x1600> -quality 80)
- Atualiza manifest.json (adiciona novas chaves, atualiza files/alt/status)
"""
import json
import shutil
import subprocess
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
MANIFEST = REPO / "frontend" / "assets" / "figures" / "manifest.json"
ASSETS = REPO / "frontend" / "assets" / "figures"
SOURCE = REPO / "data" / "provas"

# Mapeamento manual dos arquivos colocados pelo usuário
MAPA = {
    # 2020
    "2020/2020_Q34.webp": {"key": "2020-34", "files": ["2020/Q34.webp"], "page": 8, "alt": "Figura da questão 34 da prova 2020 (página 8 do caderno)."},
    "2020/2020_Q35_Q36.webp": {"keys": ["2020-35", "2020-36"], "files": ["2020/Q35.webp"], "page": 12, "alt": "Figura compartilhada das questões 35 e 36 da prova 2020 (página 12 do caderno)."},
    "2020/2020_texto2.webp": {"keys": ["2020-16", "2020-17", "2020-18", "2020-19", "2020-20"], "files": ["2020/Q16.webp"], "page": 6, "alt": "Texto-base compartilhado (Texto 2 — charge) da prova 2020."},
    # 2022
    "2022/2022_Q32_figura1.webp": {"key": "2022-32", "files": ["2022/Q32.webp"], "page": 11, "alt": "Figura 1 da questão 32 da prova 2022 (página 11 do caderno)."},
    "2022/2022_Q34.png": {"key": "2022-34", "files": ["2022/Q34.webp"], "page": 10, "alt": "Figura da questão 34 da prova 2022."},
    "2022/2022_Q35.webp": {"key": "2022-35", "files": ["2022/Q35.webp"], "page": 11, "alt": "Figura da questão 35 da prova 2022 (página 11 do caderno)."},
    "2022/2022_Q37_figura3.webp": {"key": "2022-37", "files": ["2022/Q37.webp", "2022/Q37-2.webp", "2022/Q37-3.webp"], "page": 12, "alt": "Figura 3 (terceira figura) da questão 37 da prova 2022 (página 12 do caderno)."},
    "2022/2022_Q39_figura4.webp": {"key": "2022-39", "files": ["2022/Q39.webp", "2022/Q39-2.webp", "2022/Q39-3.webp", "2022/Q39-4.webp"], "page": 12, "alt": "Figura 4 (quarta figura) da questão 39 da prova 2022 (página 12 do caderno)."},
    "2022/2022_texto2.webp": {"keys": ["2022-15", "2022-16", "2022-19"], "files": ["2022/Q15.webp"], "page": 6, "alt": "Texto-base compartilhado (Texto 2 — charge) da prova 2022."},
    "2022/2022_texto3.webp": {"keys": ["2022-17", "2022-18", "2022-31"], "files": ["2022/Q17.webp"], "page": 7, "alt": "Texto-base compartilhado (Texto 3 — gráfico/charge) da prova 2022."},
    # 2023
    "2023/2023_figura2.webp": {"keys": ["2023-23", "2023-24"], "files": ["2023/Q23.webp", "2023/Q24.webp"], "page": 8, "alt": "Figura 2 (texto-base compartilhado — charge) referenciada nas questões 23 e 24 da prova 2023."},
    "2023/2023_Q23_Q24_figura1.webp": {"keys": ["2023-23", "2023-24"], "files": ["2023/Q23.webp", "2023/Q24.webp"], "page": 8, "alt": "Figura 1 compartilhada pelas questões 23 e 24 da prova 2023 (página 8 do caderno)."},
    "2023/2023_Q28_Q29_grafico1.webp": {"keys": ["2023-28", "2023-29"], "files": ["2023/Q28.webp", "2023/Q29.webp"], "page": 10, "alt": "Gráfico 1 compartilhado pelas questões 28 e 29 da prova 2023 (página 10 do caderno)."},
    "2023/2023_texto2.webp": {"keys": ["2023-28", "2023-29"], "files": ["2023/Q28.webp"], "page": 10, "alt": "Texto-base compartilhado (Texto 2 — gráfico) da prova 2023."},
    # 2024
    "2024/2024_texto2.webp": {"keys": ["2024-18", "2024-31", "2024-32", "2024-33"], "files": ["2024/Q18.webp"], "page": 7, "alt": "Texto-base compartilhado (Texto 2 — charge) da prova 2024."},
    "2024/2024_texto3.webp": {"keys": ["2024-18", "2024-31", "2024-32", "2024-33"], "files": ["2024/Q18.webp"], "page": 11, "alt": "Texto-base compartilhado (Texto 3 — gráfico) da prova 2024."},
    # 2025
    "2025/2025_Q26.webp": {"key": "2025-26", "files": ["2025/Q26.webp"], "page": 9, "alt": "Gráfico de barras (figura) da questão 26 da prova 2025 (página 9 do caderno)."},
    "2025/2025_Q27.webp": {"key": "2025-27", "files": ["2025/Q27.webp"], "page": 9, "alt": "Figura (planta da sala) da questão 27 da prova 2025 (página 9 do caderno)."},
    "2025/2025_Q40.webp": {"key": "2025-40", "files": ["2025/Q40.webp"], "page": 13, "alt": "Figura da questão 40 da prova 2025 (página 13 do caderno)."},
    "2025/2025_texto2.webp": {"keys": ["2025-15", "2025-16", "2025-26", "2025-27"], "files": ["2025/Q26.webp"], "page": 6, "alt": "Texto-base compartilhado (Texto 2 — charge) da prova 2025."},
    # 2026
    "2026/2026_Q30_figura1.webp": {"key": "2026-30", "files": ["2026/Q30.webp"], "page": 10, "alt": "Figura 1 (triângulo retângulo) da questão 30 da prova 2026 (página 10 do caderno)."},
    "2026/2026_texto2.webp": {"keys": ["2026-18", "2026-19", "2026-20", "2026-30", "2026-39"], "files": ["2026/Q18.webp"], "page": 8, "alt": "Texto-base compartilhado (Texto 2 — charge) da prova 2026."},
}

def comprime(path: Path):
    size = path.stat().st_size
    kb = size / 1024
    if kb > 300:
        print(f"COMPRIMINDO {path.name}: {kb:.0f} KB -> ", end="")
        resultado = subprocess.run([
            "convert", str(path),
            "-resize", "1600x1600>",
            "-quality", "80",
            "-define", "webp:target-size=250KB",
            str(path)
        ], capture_output=True, text=True)
        if resultado.returncode == 0:
            new_kb = path.stat().st_size / 1024
            print(f"{new_kb:.0f} KB")
        else:
            print(f"FALHA ({resultado.stderr[:100]})")
    else:
        # Mesmo abaixo de 300 KB, garantir que o lado maior <= 1600 px
        resultado = subprocess.run([
            "convert", str(path),
            "-resize", "1600x1600>",
            str(path)
        ], capture_output=True, text=True)
        if resultado.returncode != 0:
            print(f"AVISO: redimensionamento falhou para {path.name}")


def main():
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    figures = manifest.get("figures", {})
    for rel, meta in MAPA.items():
        src_path = SOURCE / rel
        if not src_path.exists():
            print(f"SKIP (arquivo não encontrado): {rel}")
            continue
        # Copiar para assets/figures com nome correto
        # Se há uma chave única
        if "key" in meta:
            keys = [meta["key"]]
        else:
            keys = meta.get("keys", [])
        files = meta.get("files", [])
        # Copiar o arquivo para cada destino previsto no files
        # Se o arquivo original é único e precisa ser duplicado, duplicamos
        for i, file_name in enumerate(files):
            target_path = ASSETS / file_name
            target_path.parent.mkdir(parents=True, exist_ok=True)
            # Se é a primeira ocorrência e o arquivo já existe, não sobrescrever
            # Se é duplicação (ex.: Q35 e Q36), duplicamos o arquivo físico
            if i == 0:
                shutil.copy2(str(src_path), str(target_path))
            else:
                # Duplicar o arquivo físico com outro nome
                shutil.copy2(str(src_path), str(target_path))
            print(f"COPIADO: {rel} -> {file_name}")
            # Comprimir
            comprime(target_path)
        # Atualizar manifest para cada chave
        for k in keys:
            entry = figures.get(k)
            if entry is None:
                # Criar nova entrada
                entry = {
                    "files": files,
                    "page": meta.get("page"),
                    "alt": meta.get("alt"),
                    "credit": f"Fonte: IFRN — Caderno {k.split('-')[0]}, p. {meta.get('page')} (recorte para estudo).",
                    "status": "PUBLICAVEL"
                }
                figures[k] = entry
            else:
                # Atualizar files (adicionar se não estiver presente)
                # Se o arquivo é compartilhado, atualizar para apontar ao arquivo copiado
                # Mas se o arquivo original já está no manifest com outro nome, atualizar para o novo
                current_files = entry.get("files", [])
                # Substituir o primeiro arquivo pelo arquivo copiado (se for o mesmo conteúdo)
                # Ou adicionar o arquivo novo se ainda não estiver
                for f in files:
                    if f not in current_files:
                        current_files.append(f)
                entry["files"] = current_files
                entry["alt"] = meta.get("alt")
                entry["status"] = "PUBLICAVEL"
                # Atualizar credit se ainda for template
                current_credit = entry.get("credit", "")
                if "transcrição pendente" in current_credit.lower() or "recorte para estudo" not in current_credit:
                    entry["credit"] = meta.get("alt", entry.get("credit", ""))
        # Se há uma chave única e o arquivo foi copiado como duplicação, não fazer nada extra
    # Escrever manifest atualizado
    MANIFEST.parent.mkdir(parents=True, exist_ok=True)
    MANIFEST.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"MANIFEST ATUALIZADO: {MANIFEST}")
    print(f"Total de entradas no manifest: {len(figures)}")

if __name__ == "__main__":
    main()
