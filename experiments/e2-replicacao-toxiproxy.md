# E2 — Stubs versus Toxiproxy

## Status

| Data | O que aconteceu |
|------|-----------------|
| 07/10/2026 | Fase 6 (E2-v2), Toxiproxy real. CSVs em `e2-falhas-v2/resumo-fase-{A,B}.csv`. Não foram alterados nesta sessão. |
| 12/10/2026 | Docker indisponível. A suíte de stubs de `exp/e2-falhas` foi reexecutada; o Toxiproxy não. |
| **13/10/2026** | Docker no ar. A fase A do harness foi **reexecutada**. Os números abaixo com data 13/10 vêm dessa execução. |

Branch de trabalho: `exp/e2-toxi-replica` (criada a partir de `675201a`, o mesmo commit de `main`).
Nenhuma tag e nenhuma branch `exp/e1-infra`, `exp/e2-falhas`, `exp/e3-regra` ou `exp2/*` foi reescrita.

Código executado: `675201a`. `git diff --stat baseline-v2^{}..HEAD -- services infra` não listou
arquivo nenhum. O `RestClient` das duas variantes continua `RestClient.builder()` sem timeout de
cliente. Esta réplica é a **fase A** (falha de rede, sem a intervenção de timeout da fase B).
A fase B **não** foi reexecutada em 13/10.

---

## Como a execução de 13/10 foi feita

- Harness: cópia de `experiments/e2-falhas-v2/run-e2.ps1` em
  `experiments/e2-replicacao-toxiproxy/execucao-2026-10-13/run-e2.ps1`. A única mudança funcional é
  o diretório de saída, para não sobrescrever os CSVs citados da Fase 6.
- Comando: `run-e2.ps1 -Fase A -JarDir $env:TEMP\e2-jars-replica-20261013`
- Defaults do harness, iguais aos da Fase 6: N = 30 (S0–S3), N = 10 (S4), timeout do cliente = 60 s,
  3 aquecimentos por variante antes de S0 (não entram no CSV).
- Jars empacotados neste commit com `mvnw -DskipTests package` (identity, catalog, scheduling-clean).
  A variante Layered sobe por classpath (`target/classes` + `dependency:build-classpath`), como o
  harness já fazia, porque o POM pai deixa `spring-boot-maven-plugin` com `skip=true` nesse módulo.
- Perfil `e2`: Identity em `http://localhost:18080`, Catalog em `http://localhost:18082`.
- Identity (8080) e Catalog (8082) com H2 em memória. Agendamento Clean em 8081, Layered em 8083, também H2.
- Toxiproxy: container `labmanager-toxiproxy`, imagem `ghcr.io/shopify/toxiproxy:2.12.0`
  (`docker inspect` em 13/10). Proxies do `infra/toxiproxy/toxiproxy.json`: identity `0.0.0.0:18080` →
  `host.docker.internal:8080`; catalog `0.0.0.0:18082` → `host.docker.internal:8082`.
- Ambiente: Windows, Java 25.0.1 (linha `Started SchedulingCleanApplication` do log).
- Relógio do harness: início 2026-10-13T14:18:17Z, fim 14:44:36Z, exit code 0, 1.578.458 ms.
- Saída bruta: `execucao-2026-10-13/resumo-fase-A.csv` e `resultados-fase-A.csv`.
- Os logs `logs-fase-A/*.out.log` só contêm a subida do Spring. Nenhuma stack de exceção foi
  escrita neles. Não são versionados (`*.log` no `.gitignore`, como na Fase 6). O corpo HTTP não é
  coluna do CSV; por isso há sondas à parte, com N = 1.

Cenários do harness (os mesmos do protocolo da Fase 6):

| Cenário | O que o harness aplica |
|---------|------------------------|
| S0 | nenhum toxic |
| S1 | `latency` 500 ms no proxy catalog |
| S2 | `latency` 5000 ms no proxy catalog |
| S3 | proxy identity `enabled=false` |
| S4 | toxic `timeout` com `timeout=0` no proxy catalog (conexão travada) |

---

## Resultados da carga (13/10/2026) — fase A

Fonte: `execucao-2026-10-13/resumo-fase-A.csv`. Conferido contra a contagem do CSV bruto:
30×201 em S0–S2, 30×503 em S3, 10×`CLIENT_TIMEOUT` em S4, nas duas variantes. Nenhum outro status.

| Cenário | Clean | Layered |
|---------|-------|---------|
| S0 | 100 % 201; p50 18 ms; p95 21; máx 24; n=30 | 100 % 201; p50 18 ms; p95 20; máx 22; n=30 |
| S1 +500 ms | 100 % 201; p50 516 ms; p95 520; máx 520; n=30 | 100 % 201; p50 517 ms; p95 519; máx 524; n=30 |
| S2 +5000 ms | 100 % 201; p50 5017 ms; p95 5022; máx 5041; n=30 | 100 % 201; p50 5017 ms; p95 5021; máx 5029; n=30 |
| S3 identity off | 0 % 201, 100 % 503; p50 4 ms; p95 5; máx 15; n=30 | 0 % 201, 100 % 503; p50 4 ms; p95 4; máx 8; n=30 |
| S4 conexão travada | 100 % timeout do cliente; p50 60013 ms; p95 60018; máx 60023; n=10 | 100 % timeout do cliente; p50 60007 ms; p95 60014; máx 60016; n=10 |

Taxa de sucesso do caso de uso (HTTP 201): 100 % em S0, S1 e S2; 0 % em S3 e S4. Nas duas variantes.

---

## Mesma fase A em 07/10/2026 (não reprocessada; CSV original)

Fonte: `e2-falhas-v2/resumo-fase-A.csv`. p50 em ms.

| Cenário | Clean 07/10 | Clean 13/10 | Layered 07/10 | Layered 13/10 |
|---------|-------------|-------------|---------------|---------------|
| S0 | 19 | 18 | 19 | 18 |
| S1 | 518 | 516 | 517 | 517 |
| S2 | 5019 | 5017 | 5018 | 5017 |
| S3 | 4 | 4 | 4 | 4 |
| S4 | 60011 | 60013 | 60010 | 60007 |

Os status percentuais são os mesmos nas duas datas (100 % 201, 100 % 201, 100 % 201, 100 % 503,
100 % timeout do cliente). A diferença de p50 entre as datas fica entre 0 e 2 ms, exceto S4 Layered
(3 ms). A diferença de p50 entre Clean e Layered em 13/10 é 0 ms (S0, S2, S3), 1 ms (S1) e 6 ms (S4).

---

## O que os stubs mediram (Fase 4, suíte reexecutada em 12/10/2026)

Produção inalterada entre `baseline` e `exp/e2-falhas` (`git diff --numstat` só cria testes; reexecutado
em 13/10 e idêntico ao registrado em `evidencias-capitulo-4.md` §2.3). Os testes substituem o
gateway/cliente inteiro e lançam a exceção que o handler mapeia para 503. O `catch (RestClientException)`
do adaptador real não corre.

Reexecução `e2-tip` (12/10, `BUILD SUCCESS`, `execucao-2026-10-12/e2-tip/summary.txt`): os quatro
testes de simulação passaram (Identity e Catalog, Clean e Layered). Taxa de sucesso do caso de uso
sob o stub: 0 %. Latência não foi medida.

---

## Comparação explícita

| Aspecto | Stub (`exp/e2-falhas`) | Toxiproxy, fase A, 13/10 |
|---------|------------------------|---------------------------|
| O que falha | o bean do teste | o caminho de rede (proxy) |
| Identity indisponível (S3: proxy desabilitado) | 503 imediato, texto do stub contém `connection refused` | 100 % 503, p50 4 ms, n=30, nas duas variantes. O corpo **não** foi gravado pelo harness |
| Latência de 500 ms e 5 s no Catalog | não observável | 100 % **201**; p50 ≈ 516 ms e 5017 ms. A reserva completa |
| Conexão travada no Catalog (S4) | não observável | nenhuma resposta HTTP; o cliente desiste aos ~60 s (n=10) |
| Código de produção alterado | 0 linhas | 0 linhas (fase A) |
| Diferença Clean × Layered | as duas devolvem 503 | p50 difere em 0–6 ms; o status é o mesmo em toda célula |

**O que o stub acertou, nesta réplica:** com a dependência síncrona indisponível (S3) ou muda (S4),
as duas variantes falham juntas. A Clean não produz 201 quando a Layered produz 503, nem o inverso.

**O que o stub não mostrou, e a rede mostrou de novo em 13/10:** latência alta ainda termina em 201
enquanto o cliente espera; conexão travada não vira 503 — vira silêncio até o timeout do cliente.

---

## Sondas de corpo (N = 1, fora da carga)

O harness não grava o corpo. Duas sondas, cada uma com os quatro processos recém-subidos e **uma**
requisição por variante, foram gravadas à parte. Não substituem o p50 da carga. A primeira requisição
depois da subida levou ~150–170 ms; o p50 de 4 ms do S3 é o da carga, depois do aquecimento e de S0–S2.

### Proxy identity desabilitado (o S3), corpo

Arquivo: `execucao-2026-10-13/logs-sonda-s3/sonda-s3.txt`.

| Variante | Status | ms | Corpo |
|----------|--------|----|-------|
| Clean | 503 | 173 | `{"error":"Falha ao consultar identity-service: I/O error on POST request for \"http://localhost:18080/api/permissions/check\": HTTP/1.1 header parser received no bytes"}` |
| Layered | 503 | 149 | o mesmo texto |

O texto `connection refused` **não** aparece. O proxy aceita o TCP e não devolve bytes HTTP.

### Porta 18080 sem processo (Toxiproxy parado)

`docker stop labmanager-toxiproxy`. `Test-Port 18080` devolveu falso. Uma reserva por variante.
Arquivo: `execucao-2026-10-13/logs-sonda-recusada/sonda-conexao-recusada.txt`. O container foi
religado ao final (`docker start`); `docker ps` mostrou `Up` depois disso.

| Variante | Status | ms | Corpo |
|----------|--------|----|-------|
| Clean | 503 | 167 | `{"error":"Falha ao consultar identity-service: I/O error on POST request for \"http://localhost:18080/api/permissions/check\": null"}` |
| Layered | 503 | 163 | o mesmo texto |

De novo as duas variantes iguais, e de novo o corpo **não** é o `connection refused` que o stub
escreve à mão. N = 1: isto descreve o texto observado, não uma distribuição de latência.

Não foi feita sonda equivalente com a porta do Catalog fechada. No harness, Catalog entra como
latência (S1, S2) e conexão travada (S4), não como porta fechada.

---

## Fase B (timeout de cliente) — não reexecutada em 13/10

Os números de 07/10 continuam sendo a medição dessa intervenção. Não foram refeitos hoje.
Registro, para a comparação ficar no mesmo arquivo (`e2-falhas-v2/resumo-fase-B.csv`):

| Cenário | Clean 07/10 | Layered 07/10 |
|---------|-------------|---------------|
| S0 | 100 % 201; p50 18 ms; n=30 | 100 % 201; p50 17 ms; n=30 |
| S1 | 100 % 201; p50 516 ms; n=30 | 100 % 201; p50 516 ms; n=30 |
| S2 | 100 % 503; p50 2009 ms; n=30 | 100 % 503; p50 2010 ms; n=30 |
| S3 | 100 % 503; p50 4 ms; n=30 | 100 % 503; p50 4 ms; n=30 |
| S4 | 100 % 503; p50 2009 ms; n=10 | 100 % 503; p50 2010 ms; n=10 |

Custo de produção dessa intervenção, já medido na Fase 6: 1 arquivo, 10+/1−, nas duas variantes
(`SchedulingConfig.java` e `LayeredConfig.java`). Não entra no diff de 13/10.

---

## Leitura para o Capítulo 4

A réplica de 13/10 repete o padrão da fase A de 07/10: a Clean e a Layered degradam juntas.
O stub da Fase 4 acerta o 503 quando a dependência some e erra o mecanismo — o texto
`connection refused` é do teste, não da rede. Na rede, proxy desabilitado e porta fechada
produziram dois corpos diferentes, ambos 503, nenhum com essa frase. Latência no Catalog
não derruba a reserva. Conexão travada, sem timeout de cliente, não devolve 503.
