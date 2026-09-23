# Experimentos

Artefatos das linhas de base por fase e dos experimentos E1–E3 (a partir da tag `baseline`).

## Linhas de base

| Arquivo | Fase |
|---------|------|
| `baseline-fase-0-metrics.csv` | Fundação (walking skeleton) |
| `baseline-fase-1-metrics.csv` | Identity + Catalog + Scheduling Clean |
| `baseline-fase-2-metrics.csv` | + Layered + Inventory |
| `baseline-fase-3-metrics.csv` / `baseline-metrics.csv` | Ambiente Docker congelado (`baseline`) |

## Experimentos (Fase 4)

| Pasta | Branch | Intervenção |
|-------|--------|-------------|
| `e3-regra/` | `exp/e3-regra` | Antecedência 24h → 48h |
| `e1-infra/` | `exp/e1-infra` | JPA/PostgreSQL → MongoDB |
| `e2-falhas/` | `exp/e2-falhas` | Identity/Catalog indisponíveis |

Cada pasta contém `protocolo.md` e subpastas `clean/` / `layered/` com `diff-numstat.txt`, `notas.md` e `metricas.csv`.

O código alterado vive nas branches `exp/*`; em `main` ficam a tag `baseline` e os artefatos documentais.

## Fase 5 — Síntese

- [`fase-5-resumo.md`](fase-5-resumo.md) — resposta curta
- [`fase-5-sintese.md`](fase-5-sintese.md) — discussão completa para o monográfico
- [`propagacao-e1-e3.csv`](propagacao-e1-e3.csv) — tabela consolidada de propagação
