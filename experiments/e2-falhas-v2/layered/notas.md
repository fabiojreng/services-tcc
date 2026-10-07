# E2-v2 — Layered

## Fase A (código de `baseline-v2`, sem timeout)

| Cenário | Resultado (N) | p50 / p95 (ms) |
|---------|---------------|----------------|
| S0 nenhum | 100% 201 (30) | 19 / 21 |
| S1 latência 500 ms | 100% 201 (30) | 517 / 522 |
| S2 latência 5000 ms | 100% 201 (30) | 5018 / 5021 |
| S3 Identity indisponível | 100% 503 (30) | 4 / 5 |
| S4 conexão travada | 100% timeout do cliente (10) | 60010 / 60015 |

- Comportamento **indistinguível** da Clean em todos os cenários: as diferenças de p50 ficam abaixo de 1%.
- No S3, a falha sai do client chamado pelo `@Service` como `IllegalStateException` e o `GlobalExceptionHandler` a mapeia para 503.

## Fase B (timeout de conexão 1 s e de leitura 2 s)

Branch `exp2/e2-falhas-v2-layered`: commit P `83cbf69`, commit T vazio.

| Medida | Valor |
|--------|-------|
| Arquivos de produção | 1 (`config/LayeredConfig.java`) |
| Linhas | 10+ / 1− |
| `service` / `web` / `entity` | 0 linhas |
| Testes quebrados após P | 0 de 1 |
| Ajuste de testes | nenhum |
| ArchUnit | 13/13 verdes; métricas idênticas a `baseline-v2` |

| Cenário | Resultado (N) | p50 / p95 (ms) |
|---------|---------------|----------------|
| S0 | 100% 201 (30) | 17 / 19 |
| S1 | 100% 201 (30) | 516 / 518 |
| S2 | 100% 503 (30) | 2010 / 2012 |
| S3 | 100% 503 (30) | 4 / 5 |
| S4 | 100% 503 (10) | 2010 / 2013 |

- A mudança foi **textualmente equivalente** à da Clean: o mesmo bean de `RestClient.Builder`, num pacote de configuração.
- A Layered também tinha um ponto único de montagem do cliente HTTP. Por isso a preocupação transversal (timeout) ficou localizada, mesmo sem portas e adaptadores.
