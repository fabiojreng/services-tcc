# Linha de base — Fase 0

Gerada automaticamente por `MetricsExportTest` ao executar `./mvnw verify`.

## Resumo Lakos (sistema Agendamento Clean)

| Métrica | Valor |
|---------|-------|
| CCD | 71 |
| ACD | 5.0714 |
| RACD | 0.3622 |
| NCCD | 1.5778 |

## Observações

- Domínio (`domain.entities` / `value_objects` / `policy` / `repository`) sem dependências de framework (validado por ArchUnit).
- Portas (`application.ports`, `domain.repository`) com abstratividade alta — esperado (interfaces).
- Camadas Presentation/Infra com instabilidade alta (I≈1) — esperado para detalhes externos.
- O aumento de CCD/ACD em relação à baseline anterior reflete o particionamento em mais pacotes (entities, value_objects, routes, schemas, etc.), não mudança de comportamento.
- CSV completo: [baseline-fase-0-metrics.csv](baseline-fase-0-metrics.csv)

Esta linha de base é **preliminar** (apenas o walking skeleton). A tag experimental definitiva para E1–E3 será `baseline` na Fase 3.
