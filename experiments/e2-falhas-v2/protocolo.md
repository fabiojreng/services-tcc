# E2-v2 — Falhas de rede reais com Toxiproxy

Hipóteses pré-registradas: [../fase-6-preregistro.md](../fase-6-preregistro.md) (H2.1–H2.8).

## Ambiente

- `docker compose -f infra/docker-compose.yml up -d toxiproxy`
- identity-service (8080) e catalog-service (8082) com H2
- scheduling-service-clean (8081) e scheduling-service-layered (8083) com perfil `e2`
  (Identity → `localhost:18080`, Catalog → `localhost:18082`, via Toxiproxy)
- Harness: `run-e2.ps1` (seed, toxics via API 8474, carga, CSV)

## Cenários

| Cenário | Toxic |
|---------|-------|
| S0 | nenhum |
| S1 | `latency` 500 ms no catalog |
| S2 | `latency` 5000 ms no catalog |
| S3 | proxy identity desabilitado |
| S4 | `timeout` (timeout=0) no catalog |

N = 30 requisições sequenciais por cenário e variante; timeout do cliente = 60 s.

## Fases

- **A** (código de `baseline-v2`): `resultados-fase-A.csv`, `resumo-fase-A.csv`
- **B** (timeout 1 s/2 s; branches `exp2/e2-falhas-v2-clean` e `exp2/e2-falhas-v2-layered`,
  protocolo de dois commits): `resultados-fase-B.csv`, `resumo-fase-B.csv`

## Resultado

(preenchido após a execução)
