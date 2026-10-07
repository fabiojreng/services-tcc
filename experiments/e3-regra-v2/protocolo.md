# E3-v2 — Antecedência mínima 24h → 48h (protocolo de dois commits)

Hipóteses pré-registradas: [../fase-6-preregistro.md](../fase-6-preregistro.md) (H3.1–H3.5).

## Branches

- `exp2/e3-regra-v2-clean` a partir de `baseline-v2`
- `exp2/e3-regra-v2-layered` a partir de `baseline-v2`

## Checklist

- [ ] Commit P (produção) — Clean
- [ ] `medir-experimento.ps1 -Experimento e3-regra-v2 -Variante clean -Etapa producao`
- [ ] Commit T (testes) — Clean
- [ ] `medir-experimento.ps1 -Experimento e3-regra-v2 -Variante clean -Etapa testes`
- [ ] Idem para Layered
- [ ] `clean/notas.md`, `layered/notas.md`
- [ ] Comparativo e confronto com as hipóteses (abaixo)

## Resultado

(preenchido após a execução)
