# E4 — Mudança de contrato entre serviços (protocolo de dois commits)

Hipóteses pré-registradas: [../e4-preregistro.md](../e4-preregistro.md) (H4.1–H4.8, H4.C1–H4.C2), commit
`8cfa095` na `main`, feito antes de qualquer alteração de código do E4.

## Intervenção

`opensAt` → `openingTime` no contrato REST do Catalog (`POST /api/laboratories` e
`GET /api/laboratories/{id}`). Domínio e persistência do Catalog não mudam. Controle: `capacity` → `seats`,
campo que o Agendamento não lê (ver [`../e4-controle/notas.md`](../e4-controle/notas.md)).

## Branches

| Branch | P1 (provedor) | P2 (consumidor) | T (testes) | Artefatos |
|--------|---------------|-----------------|------------|-----------|
| `exp2/e4-contrato-clean` | `0988dba` | `d926515` | `61e9b8e` | `9237a6a` |
| `exp2/e4-contrato-layered` | `3504463` | `79f1771` | `75effef` | `24fb475` |
| `exp2/e4-controle` | `e065acd` (P) | — | `f06de89` | `b0d391e` |

O P1 é idêntico nas duas branches (mesma árvore). Todas partem de `baseline-v2` (`9beb275`).

## Checklist

- [x] Pré-registro commitado antes da execução
- [x] Commit P1 (Catalog) nas duas branches
- [x] `medir-experimento.ps1 -Etapa producao -ModulosExtras <catalog>`; saída renomeada para `*-P1.txt`
- [x] Commit P2 (adaptador do consumidor)
- [x] `medir-experimento.ps1 -Etapa producao -ModulosExtras <catalog>`
- [x] Execução real R0–R3 e RC (`run-e4.ps1`)
- [x] Commit T (testes)
- [x] `medir-experimento.ps1 -Etapa testes -ModulosExtras <catalog>`
- [x] Controle: P, medir (as duas variantes), T, medir
- [x] Notas e confronto com as hipóteses

Execução em 23/10/2026, 02:31–02:38 (sexta-feira), Windows 10.0.26300, Java 25.0.1, H2, sem Docker.

## Resultado — propagação (Dimensão 3)

| Medida | Clean | Layered |
|--------|-------|---------|
| P1 (provedor, comum): arquivos / linhas | 3 / 3+ 3− (`catalog-service/presentation`) | idem |
| P2 (consumidor): arquivos / linhas | **1 / 1+ 1−** | **1 / 1+ 1−** |
| Onde mudou o consumidor | `infra/http/HttpCatalogGateway` | `client/CatalogClient` |
| Camadas de negócio tocadas no consumidor | nenhuma (`domain`, `application`, `presentation` intactos) | nenhuma (`service`, `web`, `entity`, `repository` intactos) |
| Testes do Agendamento quebrados após P1 | **0 de 8** | **0 de 1** |
| Testes do Agendamento quebrados após P2 | 0 de 8 | 0 de 1 |
| Testes do Catalog quebrados após P1 e P2 | 1 de 1 (`CatalogApplicationTests`, `NullPointerException` no POST) | idem |
| Ajuste de testes (T) | 1 arquivo do Catalog, 2+ 2−; 0 no Agendamento | idem |
| Testes finais | 9/9 | 2/2 |
| ArchUnit | Regras verdes; métricas = `baseline-v2` (63 linhas, 0 diferenças) | idem |

## Resultado — execução real (Dimensão 2)

N = 10 pedidos por célula, sequenciais. Dados: `execucao/<estado>/resultados.csv` e `excecoes.txt`.

| Estado | Catalog | Agendamento | Clean | Layered |
|--------|---------|-------------|-------|---------|
| R0 | `baseline-v2` | `baseline-v2` | 201 × 10 | 201 × 10 |
| R1 | P1 (`openingTime`) | `baseline-v2` (lê `opensAt`) | **500 × 10** | **500 × 10** |
| R2 | `baseline-v2` (`opensAt`) | P2 (lê `openingTime`) | **500 × 10** | **500 × 10** |
| R3 | P1 | P2 | 201 × 10 | 201 × 10 |
| RC | controle (`seats`) | `baseline-v2` | 201 × 10 | 201 × 10 |

Em R1 e R2, a causa registrada no log é a mesma nas duas variantes:
`java.time.format.DateTimeParseException: Text 'null' could not be parsed at index 0`. O corpo da resposta é o
erro genérico do Spring Boot (`{"status":500,"error":"Internal Server Error"}`), sem indicar qual dependência
falhou. No E2-v2, a falha de rede gerava 503 com `catalog-service` na mensagem. A quebra de contrato não passa
pelo caminho de tradução de erros que as duas variantes têm para falhas remotas.

## Acoplamento de contrato e de implantação

| Métrica | Clean | Layered |
|---------|-------|---------|
| Campos da resposta do Catalog consumidos | 3 de 6 (`opensAt`, `closesAt`, `openDays`) | 3 de 6 |
| Serviços a reimplantar por renomear um campo consumido | 2 (Catalog e Agendamento) | 2 |
| Ordem de implantação sem janela de falha | nenhuma (R1 e R2 falham) | nenhuma |
| Serviços a reimplantar por renomear um campo não consumido | 1 (Catalog) | 1 |

## Confronto com as hipóteses

| Hipótese | Situação |
|----------|----------|
| H4.1 (P1 só em `catalog-service/presentation`) | Consistente: 3 arquivos, 3+ 3− |
| H4.2 (0 testes do Agendamento quebrados após P1; só o do Catalog) | Consistente: 0 de 8 e 0 de 1; `CatalogApplicationTests` com erro |
| H4.3 (R1: 0% de sucesso, HTTP 500, nenhum 503/422) | Consistente: 500 × 10 nas duas, `DateTimeParseException` |
| H4.4 (P2: 1 arquivo, 1 linha, só no adaptador) | Consistente nas duas variantes |
| H4.5 (0 testes do Agendamento quebrados após P2) | Consistente |
| H4.6 (R2 falha, R3 passa; implantação coordenada) | Consistente: R2 500 × 10, R3 201 × 10 nas duas |
| H4.7 (T só no teste do Catalog) | Consistente |
| H4.8 (ArchUnit verde, métricas iguais à `baseline-v2`) | Consistente |
| H4.C1 (controle sem efeito no Agendamento) | Consistente: 0 linhas, 0 quebrados, RC 201 × 10 nas duas |
| H4.C2 (3 de 6 campos consumidos) | Consistente, por inspeção do código e confirmado pelo controle |

Nenhuma previsão foi contrariada. Como no restante da Fase 6, isso tem duas leituras: as previsões foram feitas
depois de ler os adaptadores, e o resultado era quase determinado pelo código. O E4 é, assim, uma **confirmação
empírica** de uma propriedade estrutural, e não um teste com chance real de favorecer uma das variantes.

## Leitura

1. **A Clean Architecture não alterou o acoplamento de contrato nem o de implantação.** Os números do
   consumidor são idênticos (1 arquivo, 1 linha, 0 testes quebrados) e as duas variantes exigem implantação
   coordenada de dois serviços. É o comportamento que o termo "monolito distribuído" descreve, e ele aparece
   igual nos dois estilos.
2. **A contenção no consumidor não é mérito da Clean.** Nas duas variantes a mudança ficou num único arquivo
   porque a leitura do JSON do Catalog está isolada num adaptador HTTP. Na Clean esse adaptador implementa uma
   porta; na Layered é um `@Component` cliente. É o mesmo padrão observado no timeout do E2-v2 (localidade vinda
   do ponto único de integração, não das camadas).
3. **Nenhuma das variantes detecta a quebra por testes.** Os testes do Agendamento substituem o adaptador
   inteiro por um stub, nas duas variantes. A única detecção veio do teste do provedor, e só porque ele usa o
   nome antigo no próprio corpo do POST. Detectar a quebra do lado do consumidor exigiria teste de contrato,
   que nenhuma das variantes tem. A vantagem de detecção da Clean no E3-v2 (testes de domínio) não se estende a
   mudanças que chegam pela fronteira entre serviços.
4. **A leitura por `Map` funciona como *tolerant reader*.** Renomear um campo não consumido (controle) não teve
   efeito. Isso também é igual nas duas variantes e vem da forma de desserializar, não do estilo arquitetural.

## Limitações específicas do E4

- Previsões feitas depois de ler o código dos adaptadores (o desenho tem baixo poder de refutação).
- Só renomeação; mudança de tipo ou de semântica não foi testada.
- Mesmo executor implementa, mede e interpreta, como na Fase 6.
- Os artefatos de execução real foram construídos com `construir-artefatos.ps1` a partir de quatro commits:
  `baseline-v2` (`9beb275`), P2 Clean (`d926515`), P2 Layered (`79f1771`) e P do controle (`e065acd`). Em R1
  e R3, o Catalog vem do P2 Clean, cujo código do Catalog é idêntico ao P1. O caminho do jar de cada serviço
  está no cabeçalho de `excecoes.txt`.
- Evolução compatível do contrato (*expand-contract*, versionamento, testes de contrato orientados pelo
  consumidor) ficou fora, como declarado no pré-registro.
