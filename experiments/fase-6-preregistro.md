# Fase 6 — Pré-registro das hipóteses (reforço metodológico)

> Este documento foi commitado **antes** da execução de qualquer experimento da Fase 6.
> A data do commit no histórico Git é a evidência do pré-registro. Resultados que
> contrariem as previsões serão reportados como estão.

## Motivação

A avaliação crítica dos resultados das Fases 4–5 identificou fragilidades:

1. E2 baseado em stubs: media o próprio *handler* de exceção, não o comportamento sob falha de rede.
2. E1 nunca executou o adaptador MongoDB contra um banco real.
3. "Testes quebrados" não foi medido de forma independente (produção e testes eram ajustados juntos).
4. E3 era uma troca de constante — pouco discriminante entre as variantes.
5. ArchUnit não foi reexecutado após cada experimento.

## Ponto de partida

- Tag `baseline-v2` (commit `9beb275`).
- Produção idêntica à tag `baseline`, exceto pela troca cosmética de `HttpStatus.UNPROCESSABLE_ENTITY`
  por `UNPROCESSABLE_CONTENT` (mesmo código 422; apenas evita API depreciada).
- Adições exclusivamente de instrumentação: `MetricsExportTest` com rótulo (`-Dmetrics.label`),
  script `experiments/scripts/medir-experimento.ps1`, dependências Testcontainers em escopo de teste.
- Métricas de referência: `experiments/baseline-v2/metricas.csv`.

## Protocolo de dois commits (aplicado a todos os experimentos da Fase 6)

Uma branch por experimento **e por variante**, a partir de `baseline-v2`:
`exp2/<experimento>-clean` e `exp2/<experimento>-layered`.

1. **Commit P** — somente código de produção (inclui `pom.xml` e `application*.properties`).
2. Executar `medir-experimento.ps1 -Etapa producao`:
   `diff-producao.txt`, `mvn-producao.log`, `testes-quebrados.txt`.
   Falhas de **compilação** de testes contam como quebra.
3. **Commit T** — somente ajuste de testes (incluindo a suíte compartilhada `acceptance/`).
4. Executar `medir-experimento.ps1 -Etapa testes`:
   `diff-testes.txt`, `testes-final.txt` (deve estar verde), `archunit.txt`, `metricas.csv`.
5. `notas.md` com interpretação.

Regra de parada: a implementação de produção é a **mínima idiomática** para cada estilo,
descrita abaixo antes da execução; não se faz limpeza estética antes de medir.

---

## E3-v2 — Antecedência mínima 24h → 48h (refeito)

Mesma intervenção do E3 original, agora com o protocolo de dois commits.

| ID | Previsão |
|----|----------|
| H3.1 | Produção: 1 arquivo em cada variante (Clean: `domain/policy/ReservationPolicy`; Layered: `service/ReservationService`). |
| H3.2 | Commit P quebra ao menos 1 teste de unidade na Clean (`ReservationPolicyTest`, limiar de 24h). |
| H3.3 | Commit P quebra **0** testes na Layered (não há teste de unidade da regra) — a mudança passa "silenciosa". |
| H3.4 | Suíte de aceitação: resultado dependente da data de execução (usa "próxima quarta-feira ≥ 2 dias"); pode passar ou falhar sem relação com a arquitetura. Registrar a data. |
| H3.5 | ArchUnit: nenhuma violação nova; métricas idênticas à `baseline-v2`. |

## E3b — Limite semanal de reservas por usuário (novo)

Regra: um solicitante pode ter no máximo **3** reservas com status `REQUESTED` ou `CONFIRMED`
cuja data de início esteja na mesma **semana ISO** (segunda a domingo) do slot solicitado.
Violação → erro de negócio (HTTP 422).

Implementação mínima idiomática pré-definida:

- **Clean:** novo método na porta `ReservationRepository` (domínio) para contar reservas ativas
  do solicitante em um intervalo; nova política de domínio para a invariante; caso de uso
  consulta a contagem e aplica a política; adaptador JPA + Spring Data implementam a consulta.
- **Layered:** método derivado no `ReservationJpaRepository` e verificação em `ReservationService.validateRules`.

| ID | Previsão |
|----|----------|
| Hb.1 | Clean altera **mais arquivos** de produção que a Layered (≈4–5 vs. 2), distribuídos em 3 módulos (`domain`, `application`, `infra`). |
| Hb.2 | Na Clean, a lógica da invariante (o "se > 3 então erro") fica exclusivamente em `domain`; `application`/`infra` recebem apenas código de orquestração/consulta. |
| Hb.3 | Na Layered, regra e consulta ficam juntas no `service` + `repository`. |
| Hb.4 | Commit P: Clean quebra testes que implementam `ReservationRepository` (se houver dublês) por compilação; caso contrário 0. Layered: 0 quebrados. |
| Hb.5 | Commit T: Clean ganha teste de unidade da nova política sem Spring; Layered só consegue testar a regra via teste de integração (contexto Spring). |
| Hb.6 | ArchUnit: nenhuma violação; Clean ganha 1 classe em `domain.policy` sem novas dependências para fora do domínio. |

Esta é a intervenção em que a Clean tende a "perder" em volume de mudança. Ela foi escolhida justamente por isso.

## E1-v2 — JPA → MongoDB com banco real (refeito)

Testes de integração passam a usar MongoDB real via Testcontainers (`mongo:7`), sem mocks de repositório.

| ID | Previsão |
|----|----------|
| H1.1 | Clean: produção alterada apenas em `infra` (pom, modelo, adaptador, Spring Data, config, properties); **0** linhas em `domain`, `application`, `presentation`. |
| H1.2 | Layered: produção alterada em `entity`, `repository`, `pom`/properties e **também** em `service` (ao menos 1 linha). |
| H1.3 | **Volume** (linhas): sem previsão de vantagem para a Clean (o E1 original mostrou Clean com mais linhas). O que se compara é a localização. |
| H1.4 | Commit P: o teste de integração de cada variante quebra (sem Mongo disponível no contexto de teste); testes de domínio da Clean permanecem verdes. |
| H1.5 | Ajustes exigidos pelo Mongo real (ex.: tipo de id, mapeamento de datas, índices) ficam em `infra` na Clean e em `entity`/`repository` na Layered. |
| H1.6 | ArchUnit: regras da Clean continuam verdes (domínio sem dependência de Mongo/Spring Data). |

## E2-v2 — Falhas de rede reais com Toxiproxy (refeito)

Ambiente: Toxiproxy (Docker Compose) entre Agendamento e Identity/Catalog; serviços com H2;
variantes de Agendamento com perfil `e2`. Harness: `experiments/e2-falhas-v2/run-e2.ps1`.
N = 30 requisições sequenciais por cenário e variante; timeout do cliente de carga = 60 s.

| Cenário | Toxic |
|---------|-------|
| S0 | nenhum (controle) |
| S1 | `latency` 500 ms no catalog (downstream) |
| S2 | `latency` 5000 ms no catalog |
| S3 | proxy identity desabilitado (conexão recusada) |
| S4 | `timeout` (timeout=0) no catalog — aceita conexão e nunca responde |

**Fase A — código atual (sem timeout configurado):**

| ID | Previsão |
|----|----------|
| H2.1 | S0: 100% de sucesso (201) nas duas variantes. |
| H2.2 | S1/S2: 100% de sucesso, latência acrescida ≈ do valor do toxic, **igual nas duas variantes** (diferença de p50 < 10%). |
| H2.3 | S3: falha rápida (< 1 s) com 503 nas duas variantes. |
| H2.4 | S4: **as duas variantes travam** até o timeout do cliente (60 s); 0% de resposta do servidor. A Clean não oferece proteção. |

**Fase B — intervenção de resiliência (protocolo de dois commits):**
timeout de conexão 1 s e de leitura 2 s nos clientes HTTP de saída, com falha mapeada para 503.
Circuit breaker fica fora do escopo (trabalho futuro), para não depender de bibliotecas ainda sem suporte declarado ao Spring Boot 4.

| ID | Previsão |
|----|----------|
| H2.5 | Diff de produção de tamanho semelhante (±1 arquivo) nas duas variantes. |
| H2.6 | Clean: mudança apenas em `infra`; 0 linhas em `domain`/`application`/`presentation`. Layered: mudança em `config`/`client`; 0 linhas em `service`. |
| H2.7 | Após B: S2 e S4 passam a responder 503 em ≈ 2 s nas duas variantes. S0/S1 inalterados. |
| H2.8 | Commit P: 0 testes quebrados nas duas variantes. |

Leitura pretendida: se H2.4–H2.7 se sustentarem, o resultado é consistente com a tese de que
resiliência entre serviços é **ortogonal** à Clean Architecture.

---

## O que **não** será testado nesta fase (declarado antecipadamente)

- Replicação do protocolo em um segundo serviço (N = 2).
- Mudança de contrato entre serviços (E4 — renomear campo do Catalog).
- Erosão arquitetural ao longo de múltiplas iterações.
- Comportamento sob carga concorrente.
- Entrada de um novo serviço no sistema maduro.
