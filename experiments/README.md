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

## Fase 6 — Replicação controlada e E4

Resultados vigentes em [`fase-6-sintese.md`](fase-6-sintese.md) (pré-registro em
[`fase-6-preregistro.md`](fase-6-preregistro.md)). Branches `exp2/*`, a partir da tag `baseline-v2`.

| Pasta | Branch | Intervenção |
|-------|--------|-------------|
| `e3-regra-v2/` | `exp2/e3-regra-v2-*` | Antecedência 24h → 48h (dois commits) |
| `e3b-limite-semanal/` | `exp2/e3b-limite-semanal-*` | Limite semanal de reservas |
| `e1-infra-v2/` | `exp2/e1-infra-v2-*` | JPA → MongoDB real (Testcontainers) |
| `e2-falhas-v2/` | `exp2/e2-falhas-v2-*` | Falhas reais (Toxiproxy) e timeout |
| `e4-contrato/` | `exp2/e4-contrato-*` | Catalog renomeia `opensAt` → `openingTime` (pré-registro em [`e4-preregistro.md`](e4-preregistro.md)) |
| `e4-controle/` | `exp2/e4-controle` | Catalog renomeia `capacity` → `seats` (campo não consumido) |

## Fase 5 — Síntese

- [`fase-5-resumo.md`](fase-5-resumo.md) — resposta curta
- [`fase-5-sintese.md`](fase-5-sintese.md) — discussão completa para o monográfico
- [`propagacao-e1-e3.csv`](propagacao-e1-e3.csv) — tabela consolidada de propagação
