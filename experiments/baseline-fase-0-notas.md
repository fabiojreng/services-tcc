# Linha de base — Fase 0

Gerada automaticamente por `MetricsExportTest` ao executar `./mvnw verify`.

## Resumo Lakos (sistema Agendamento Clean)

| Métrica | Valor |
|---------|-------|
| CCD | 37 |
| ACD | 4.1111 |
| RACD | 0.4568 |
| NCCD | 1.4800 |

## Observações

- Domínio (`domain.model`) com Ce baixo e sem dependências de framework (validado por ArchUnit).
- Portas de saída (`application.port.out`) com abstratividade 1.0 — esperado (interfaces).
- Camadas Adapter/Infrastructure com instabilidade alta (I≈1) — esperado para detalhes externos.
- CSV completo: [baseline-fase-0-metrics.csv](baseline-fase-0-metrics.csv)

Esta linha de base é **preliminar** (apenas o walking skeleton). A tag experimental definitiva para E1–E3 será `baseline` na Fase 3.
