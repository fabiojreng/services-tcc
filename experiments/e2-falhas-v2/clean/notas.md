# E2-v2 — Clean

## Fase A (código de `baseline-v2`, sem timeout)

| Cenário | Resultado (N) | p50 / p95 (ms) |
|---------|---------------|----------------|
| S0 nenhum | 100% 201 (30) | 19 / 21 |
| S1 latência 500 ms | 100% 201 (30) | 518 / 520 |
| S2 latência 5000 ms | 100% 201 (30) | 5019 / 5024 |
| S3 Identity indisponível | 100% 503 (30) | 4 / 4 |
| S4 conexão travada | 100% timeout do cliente (10) | 60011 / 60019 |

- A latência da dependência é **repassada integralmente** ao cliente. O serviço não tem nenhum limite próprio.
- Com dependência recusando conexão (S3), a falha é rápida e tipada: `RemoteDependencyException` → 503 na `presentation`. O domínio não é tocado.
- Com a conexão travada (S4), o serviço **nunca responde**: o `RestClient` padrão (JDK `HttpClient`) não tem timeout. A requisição ocupa uma thread até o cliente desistir. As fronteiras internas da Clean não oferecem nenhuma proteção aqui.

## Fase B (timeout de conexão 1 s e de leitura 2 s)

Branch `exp2/e2-falhas-v2-clean`: commit P `e38e584`, commit T vazio.

| Medida | Valor |
|--------|-------|
| Arquivos de produção | 1 (`infra/config/SchedulingConfig.java`) |
| Linhas | 10+ / 1− |
| `domain` / `application` / `presentation` | 0 linhas |
| Testes quebrados após P | 0 de 8 |
| Ajuste de testes | nenhum |
| ArchUnit | 13/13 verdes; métricas idênticas a `baseline-v2` |

| Cenário | Resultado (N) | p50 / p95 (ms) |
|---------|---------------|----------------|
| S0 | 100% 201 (30) | 18 / 21 |
| S1 | 100% 201 (30) | 516 / 517 |
| S2 | 100% 503 (30) | 2009 / 2011 |
| S3 | 100% 503 (30) | 4 / 5 |
| S4 | 100% 503 (10) | 2009 / 2011 |

- O timeout converteu "espera ilimitada" em "falha rápida e tipada". O mapeamento de erro que já existia (`RestClientException` → `RemoteDependencyException` → 503) absorveu a nova exceção sem mudança.
- O ponto de alteração foi o *composition root* (`SchedulingConfig`), onde o `RestClient.Builder` é montado. Os adaptadores HTTP não mudaram.
- A disponibilidade **não melhorou**: S2 e S4 continuam com 0% de sucesso. O ganho está em latência limitada e em liberar recursos, não em atender o pedido.
