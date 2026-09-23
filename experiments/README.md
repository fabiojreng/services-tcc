# Experimentos

Artefatos das linhas de base por fase e dos experimentos E1–E3 (a partir da tag `baseline`).

## Linhas de base

| Arquivo | Fase |
|---------|------|
| `baseline-fase-0-metrics.csv` | Fundação (walking skeleton) |
| `baseline-fase-1-metrics.csv` | Identity + Catalog + Scheduling Clean |
| `baseline-fase-2-metrics.csv` | + Layered + Inventory |
| `baseline-fase-3-metrics.csv` / `baseline-metrics.csv` | Ambiente Docker congelado (`baseline`) |

## Pastas dos experimentos (Fase 4)

```
e1-infra/     # troca de persistência
e2-falhas/    # Toxiproxy / indisponibilidade
e3-regra/     # alteração de regra de negócio
```

Cada experimento deve conter subpastas `clean/` e `layered/` com diffs, métricas e notas, conforme [`docs/03-protocolo-experimental.md`](../docs/03-protocolo-experimental.md).
