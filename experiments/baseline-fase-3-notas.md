# Baseline — Fase 3

Congelamento do ambiente experimental (tag Git `baseline`).

- Docker Compose: PostgreSQL por serviço + Toxiproxy
- Profiles: `postgres` (JDBC + logs ECS), `e2` (URLs via Toxiproxy)
- H2 permanece como default para `mvnw verify` / desenvolvimento offline
- Métricas ArchUnit: `baseline-fase-3-metrics.csv` e `baseline-metrics.csv`

Próximo passo: Fase 4 — executar E3, E1 e E2 a partir desta tag.
