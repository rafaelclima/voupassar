# One-shots arquivados (não executar)

Scripts de operação única já executados contra o banco/arquivos, preservados
como trilha de auditoria. **Não rodar novamente**: referenciam colunas e
endpoints do contrato de curadoria humana removido em 2026-10-06
(`docs/plano-remocao-curadoria.md`, V12).

| Script | O que fez | Quando |
|---|---|---|
| `curate_classifications.py` | Pré-curadoria LLM das 240 classificações via PATCH admin (200 APPROVED / 39 REVIEWED / 1 REJECTED) | 2026-10-02 |
| `backfill_classification_observations.py` | Preencheu `observation` em 105 classificações (`data/import/backfill-observations.json`) | 2026-10-02 |
| `integrate_figures_final.py` | Copiou recortes e escreveu as 41 entradas de `frontend/assets/figures/manifest.json` | fase de figuras |
