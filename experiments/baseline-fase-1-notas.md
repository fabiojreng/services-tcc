# Linha de base — Fase 1

Gerada por `MetricsExportTest` em `./mvnw verify`.

## O que mudou em relação à Fase 0

- `identity-service` e `catalog-service` em Clean Architecture
- Agendamento consulta Identity (`RESERVATION_REQUEST`) e Catalog (horário) via HTTP
- Body de `POST /api/reservations` sem `opensAt`/`closesAt`/`openDays`
- Fan-out síncrono do caso de uso Solicitar Reserva: **2** (Identity + Catalog)

## Artefato

CSV: [baseline-fase-1-metrics.csv](baseline-fase-1-metrics.csv)

A tag experimental definitiva para E1–E3 continua sendo `baseline` (Fase 3).
