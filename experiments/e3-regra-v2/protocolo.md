# E3-v2 — Antecedência mínima 24h → 48h (protocolo de dois commits)

Hipóteses pré-registradas: [../fase-6-preregistro.md](../fase-6-preregistro.md) (H3.1–H3.5).

## Branches

- `exp2/e3-regra-v2-clean` a partir de `baseline-v2`
- `exp2/e3-regra-v2-layered` a partir de `baseline-v2`

## Checklist

- [x] Commit P (produção) — Clean
- [x] `medir-experimento.ps1 -Experimento e3-regra-v2 -Variante clean -Etapa producao`
- [x] Commit T (testes) — Clean
- [x] `medir-experimento.ps1 -Experimento e3-regra-v2 -Variante clean -Etapa testes`
- [x] Idem para Layered
- [x] `clean/notas.md`, `layered/notas.md`
- [x] Comparativo e confronto com as hipóteses (abaixo)

## Resultado (execução em 2026-10-07)

| Medida | Clean | Layered |
|--------|-------|---------|
| Produção (P): arquivos / linhas | 1 / 3+ 3− (`domain`) | 1 / 2+ 2− (`service`) |
| Testes quebrados após P | **3 de 8** (todos de unidade do domínio) | **0 de 1** |
| Ajuste de testes (T): arquivos / linhas | 1 / 6+ 6− | 0 |
| Testes finais | 8/8 | 1/1 |
| ArchUnit | 13/13; métricas = `baseline-v2` | 13/13; métricas = `baseline-v2` |

| Hipótese | Situação |
|----------|----------|
| H3.1 (1 arquivo cada) | Consistente |
| H3.2 (Clean quebra ≥ 1 teste de unidade) | Consistente (3) |
| H3.3 (Layered quebra 0) | Consistente: a mudança passou silenciosa |
| H3.4 (aceitação dependente da data) | Passou na data de execução; fragilidade confirmada por análise (segunda-feira depois das 10:00) |
| H3.5 (métricas inalteradas) | Consistente |

Leitura: a troca de constante empata em custo de produção. A diferença está na **detecção**: a Clean tem testes que fixam a regra; na Layered, nenhum teste percebeu a mudança.
