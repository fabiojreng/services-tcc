# E2 — Stubs versus Toxiproxy (evidência já existente; não reexecutado nesta sessão)

## Status desta sessão (12/10/2026)

A tarefa B2 pedia **repetir** o E2 com Toxiproxy real (latência, timeout, conexão recusada) e
registrar taxa de sucesso, latência e comparação com os stubs.

**Não foi reexecutado nesta sessão.** Motivo factual: o daemon do Docker Desktop não está
acessível.

```
docker version
failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine
```

O harness `experiments/e2-falhas-v2/run-e2.ps1` exige `docker compose -f infra/docker-compose.yml
up -d toxiproxy` mais Identity, Catalog e as duas variantes do Agendamento. Sem o motor, qualquer
número novo seria invenção. Números abaixo vêm de execuções **já realizadas e versionadas**.

| Rodada | Data | Instrumento | Artefato |
|--------|------|-------------|----------|
| Fase 4 (`exp/e2-falhas`) | commit `319af0e`; suíte reexecutada em 12/10/2026 | stubs em processo | `evidencias-capitulo-4.md` §2; `execucao-2026-10-12/e2-tip/summary.txt` |
| Fase 6 E2-v2 | 07/10/2026 | Toxiproxy 2.12, Docker Desktop 29.8 | `e2-falhas-v2/resumo-fase-{A,B}.csv`, `resultados-fase-{A,B}.csv`, `protocolo.md` |

Nenhuma tag `baseline` / `baseline-v2` e nenhuma branch `exp/e2-falhas` / `exp2/e2-falhas-v2-*`
foi alterada para produzir este arquivo.

---

## Hipótese (a mesma do protocolo)

[`docs/03-protocolo-experimental.md`](../docs/03-protocolo-experimental.md): a Clean **não** mitiga
acoplamento temporal síncrono; ambas as variantes degradam de forma semelhante, a menos que se
introduzam padrões de resiliência — ortogonais ao estilo interno.

---

## O que os stubs mediram (Fase 4, confirmado em 12/10/2026)

Produção **inalterada** (`git diff --numstat baseline..exp/e2-falhas` só cria testes). Os testes
substituem o adaptador/cliente inteiro e lançam a exceção que o handler já mapeia para 503.

Reexecução `e2-tip` (ponta `exp/e2-falhas`, 11:41:31, `BUILD SUCCESS`):

| Teste | Variante | Resultado |
|-------|----------|-----------|
| `SchedulingCleanFailureSimulationTests.reservaFalhaCom503QuandoIdentityIndisponivel` | Clean | passou (espera 503 + `identity-service` no corpo) |
| `SchedulingCleanCatalogFailureSimulationTests` | Clean | passou (503 + `catalog-service`) |
| `SchedulingLayeredFailureSimulationTests` | Layered | passou |
| `SchedulingLayeredCatalogFailureSimulationTests` | Layered | passou |

Taxa de sucesso do caso de uso sob “dependência down”: **0 %** nas duas variantes. Latência **não
foi medida** (stubs são instantâneos). O `catch (RestClientException)` dos adaptadores reais **não
executa**.

---

## O que o Toxiproxy mediu (Fase 6, 07/10/2026) — dados brutos versionados

Cenários (`e2-falhas-v2/protocolo.md`): S0 nenhum toxic; S1 `latency` 500 ms no Catalog; S2
`latency` 5000 ms no Catalog; S3 proxy Identity desabilitado; S4 `timeout=0` (conexão travada) no
Catalog. N = 30 sequenciais por célula, exceto S4 (N = 10, desvio declarado). Timeout do cliente de
carga = 60 s. Mesma máquina para proxy, serviços e cliente.

### Fase A — sem timeout no `RestClient` (`resumo-fase-A.csv`)

| Cenário | Clean: HTTP / p50 ms / n | Layered: HTTP / p50 ms / n |
|---------|--------------------------|----------------------------|
| S0 | 100 % 201 / 19 / 30 | 100 % 201 / 19 / 30 |
| S1 +500 ms | 100 % 201 / 518 / 30 | 100 % 201 / 517 / 30 |
| S2 +5000 ms | 100 % 201 / 5019 / 30 | 100 % 201 / 5018 / 30 |
| S3 Identity off | 100 % 503 / 4 / 30 | 100 % 503 / 4 / 30 |
| S4 conexão travada | 100 % timeout cliente / 60011 / 10 | 100 % timeout cliente / 60010 / 10 |

### Fase B — timeout conexão 1 s / leitura 2 s (`resumo-fase-B.csv`)

| Cenário | Clean: HTTP / p50 ms / n | Layered: HTTP / p50 ms / n |
|---------|--------------------------|----------------------------|
| S0 | 100 % 201 / 18 / 30 | 100 % 201 / 17 / 30 |
| S1 | 100 % 201 / 516 / 30 | 100 % 201 / 516 / 30 |
| S2 | 100 % 503 / 2009 / 30 | 100 % 503 / 2010 / 30 |
| S3 | 100 % 503 / 4 / 30 | 100 % 503 / 4 / 30 |
| S4 | 100 % 503 / 2009 / 10 | 100 % 503 / 2010 / 10 |

Custo da introdução do timeout (produção, protocolo de dois commits): **1 arquivo, 10+/1−** nas duas
variantes (`infra/config/SchedulingConfig.java` vs `config/LayeredConfig.java`). Testes quebrados
após P: 0 de 8 (Clean), 0 de 1 (Layered).

---

## Comparação explícita stub × Toxiproxy

| Aspecto | Stub (Fase 4) | Toxiproxy (Fase 6) |
|---------|---------------|---------------------|
| O que falha | o *bean* do gateway/cliente, substituído no teste | o caminho de rede até Identity/Catalog |
| Identity/Catalog “down” | 503 imediato, 0 % sucesso, **igual** nas duas | S3: 503 em ~4 ms, 0 % sucesso, **igual** nas duas |
| Latência da dependência | não observável | S1/S2: latência **repassada** ao cliente (p50 ≈ 500+base / 5000+base) enquanto não há timeout |
| Conexão pendurada | não observável (o stub lança na hora) | S4 fase A: **nenhuma** variante responde; o cliente desiste aos 60 s |
| Timeout como mitigação | não faz parte do E2 original | fase B: S2 e S4 passam a 503 em ~2 s; disponibilidade continua 0 % |
| Código de produção | zero linhas | zero na fase A; 1 arquivo idêntico em tamanho na fase B |
| Hipótese E2 | consistente | consistente (diferença de p50 entre variantes < 1 %) |

**O que o stub acertou:** o mapeamento exceção → 503 e a conclusão de que a Clean não salva o caso
de uso quando a dependência síncrona some.

**O que o stub não podia mostrar:** (1) latência alta *ainda retorna 201* se o `RestClient` espera;
(2) conexão travada *não devolve 503* — devolve silêncio até o timeout do cliente; (3) a mitigação
(timeout) tem o mesmo custo e o mesmo efeito nas duas arquiteturas, porque as duas já tinham um
único ponto de montagem do `RestClient.Builder`.

Para o Capítulo 4: usar só o E2-com-stub é quase tautológico (o teste instala a exceção que o handler
conhece). O E2-v2 é a evidência de Dimensão 2 que deve ser citada. Esta sessão não acrescenta
medida nova; só torna a comparação stub × rede **explícita** e registra a impossibilidade de
replicar o Toxiproxy hoje.
