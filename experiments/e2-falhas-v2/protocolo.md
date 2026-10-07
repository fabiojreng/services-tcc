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

## Resultado (execução em 2026-10-07)

Docker Desktop 29.8, Toxiproxy 2.12, mesma máquina para clientes, serviços e proxy. Antes de cada variante e cenário houve 3 requisições de aquecimento.

### Fase A — sem timeout (`resumo-fase-A.csv`)

| Cenário | Clean | Layered |
|---------|-------|---------|
| S0 | 100% 201; p50 19 ms | 100% 201; p50 19 ms |
| S1 (500 ms) | 100% 201; p50 518 ms | 100% 201; p50 517 ms |
| S2 (5000 ms) | 100% 201; p50 5019 ms | 100% 201; p50 5018 ms |
| S3 (Identity off) | 100% 503; p50 4 ms | 100% 503; p50 4 ms |
| S4 (travado) | 100% timeout do cliente (60 s) | 100% timeout do cliente (60 s) |

### Fase B — timeout 1 s/2 s (`resumo-fase-B.csv`)

| Cenário | Clean | Layered |
|---------|-------|---------|
| S0 | 100% 201; p50 18 ms | 100% 201; p50 17 ms |
| S1 | 100% 201; p50 516 ms | 100% 201; p50 516 ms |
| S2 | 100% 503; p50 2009 ms | 100% 503; p50 2010 ms |
| S3 | 100% 503; p50 4 ms | 100% 503; p50 4 ms |
| S4 | 100% 503; p50 2009 ms | 100% 503; p50 2010 ms |

### Custo da mudança (fase B, dois commits)

| Medida | Clean | Layered |
|--------|-------|---------|
| Produção | 1 arquivo, 10+ 1− (`infra/config`) | 1 arquivo, 10+ 1− (`config`) |
| Camadas de negócio tocadas | nenhuma | nenhuma |
| Testes quebrados após P | 0 de 8 | 0 de 1 |
| ArchUnit / métricas | verdes / = `baseline-v2` | verdes / = `baseline-v2` |

### Confronto com as hipóteses

| Hipótese | Situação |
|----------|----------|
| H2.1 | Consistente |
| H2.2 | Consistente (diferença de p50 entre variantes < 1%) |
| H2.3 | Consistente (≈ 4 ms) |
| H2.4 | Consistente: nenhuma das variantes responde; a Clean não protege |
| H2.5 | Consistente (diffs idênticos em tamanho) |
| H2.6 | Consistente |
| H2.7 | Consistente (≈ 2,01 s); S0/S1 inalterados |
| H2.8 | Consistente |

### Desvios do pré-registro

- **S4 com N = 10** em vez de 30, nas duas fases e variantes. Na fase A, cada requisição dura 60 s; 30 por variante dariam 1 hora só nesse cenário. O resultado é determinístico (10/10 em todas as células), o que reduz o impacto do N menor.
- Não é desvio, mas convém lembrar: o circuit breaker já estava fora do escopo no pré-registro. Só o timeout foi medido.
