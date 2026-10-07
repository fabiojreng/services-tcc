# E3b — Limite semanal de reservas por usuário (protocolo de dois commits)

Hipóteses pré-registradas: [../fase-6-preregistro.md](../fase-6-preregistro.md) (Hb.1–Hb.6).

## Regra

No máximo **3** reservas `REQUESTED` ou `CONFIRMED` por `requesterId` com início na mesma
semana ISO (segunda a domingo) do slot solicitado. Violação → 422.

## Branches

- `exp2/e3b-limite-semanal-clean` a partir de `baseline-v2`
- `exp2/e3b-limite-semanal-layered` a partir de `baseline-v2`

## Checklist

- [ ] Commit P (produção) — Clean
- [ ] `medir-experimento.ps1 -Experimento e3b-limite-semanal -Variante clean -Etapa producao`
- [ ] Commit T (testes) — Clean
- [ ] `medir-experimento.ps1 -Experimento e3b-limite-semanal -Variante clean -Etapa testes`
- [ ] Idem para Layered
- [ ] `clean/notas.md`, `layered/notas.md`
- [ ] Comparativo e confronto com as hipóteses

## Resultado

(preenchido após a execução)
