# E4 — Pré-registro: mudança de contrato entre serviços

> Este documento foi commitado **antes** da execução do E4. A data do commit no histórico Git é a
> evidência do pré-registro. Resultados que contrariem as previsões serão reportados como estão.

## Motivação

O E4 constava como "não testado" no pré-registro da Fase 6 ([`fase-6-preregistro.md`](fase-6-preregistro.md))
e nas limitações ([`../docs/04-ameacas-validade.md`](../docs/04-ameacas-validade.md)). Sem ele, o eixo
"monolito distribuído" do título só é avaliado pelo acoplamento temporal (E2). Duas métricas da Dimensão 2
([`../docs/02-criterios-desacoplamento.md`](../docs/02-criterios-desacoplamento.md)) estavam definidas e não
medidas: **acoplamento de contrato** (campos consumidos ÷ campos expostos) e **acoplamento de implantação**
(serviços que precisam ser reimplantados por uma mudança).

O E4 **não** varia a forma do acoplamento entre serviços (síncrono × assíncrono). Ele mede o custo de uma
mudança de contrato com o acoplamento atual, que é igual nas duas variantes. Variar a forma do acoplamento
continua fora do escopo.

## Ponto de partida

- Tag `baseline-v2` (`9beb275`). A produção da `main` é idêntica à da `baseline-v2` em `services/` e `acceptance/`.
- Instrumento: `experiments/scripts/medir-experimento.ps1`, com o parâmetro opcional `-ModulosExtras`
  (acrescentado neste commit) para executar também os testes do `catalog-service`.

## Situação atual do contrato (lida no código antes da execução)

`GET /api/laboratories/{id}` do Catalog expõe 6 campos: `id`, `name`, `capacity`, `opensAt`, `closesAt`,
`openDays`. As duas variantes do Agendamento consomem a resposta da mesma forma: desserializam para `Map` e
leem `opensAt`, `closesAt` e `openDays` por chave textual, num único adaptador:

- Clean: `infra/http/HttpCatalogGateway` (implementa a porta `application/ports/CatalogGateway`).
- Layered: `client/CatalogClient` (devolve o record interno `LaboratoryHours`).

Os testes de integração das duas variantes substituem esse adaptador inteiro por um stub.

## Intervenções

### E4 principal — renomear um campo consumido

`opensAt` → `openingTime` no contrato REST do Catalog (request de cadastro e response). Domínio e persistência
do Catalog **não** mudam: o que muda é o JSON.

Branches, a partir de `baseline-v2`: `exp2/e4-contrato-clean` e `exp2/e4-contrato-layered`.

1. **Commit P1** — somente produção do **provedor** (Catalog). Idêntico nas duas branches.
   Medir: `-Etapa producao` com `-ModulosExtras` do Catalog; artefatos renomeados para `*-P1.txt`.
2. **Commit P2** — somente produção do **consumidor** (adaptador da variante).
   Medir: `-Etapa producao`.
3. **Commit T** — somente ajuste de testes.
   Medir: `-Etapa testes`.
4. Commit [A] com artefatos.

### E4-controle — renomear um campo não consumido

`capacity` → `seats` no contrato REST do Catalog. Branch `exp2/e4-controle` a partir de `baseline-v2`:
commit P (Catalog), medir com as duas variantes; commit T; medir; commit [A].

### Execução real (fora dos testes)

Harness `experiments/e4-contrato/run-e4.ps1`: sobe Identity, Catalog e as duas variantes do Agendamento com H2
a partir de jars construídos em cada estado, cadastra usuário e laboratório e envia N = 10 pedidos de reserva
válidos a cada variante. Registra o status HTTP e o corpo do erro.

| Estado | Catalog | Agendamento | Representa |
|--------|---------|-------------|------------|
| R0 | `baseline-v2` | `baseline-v2` | controle de sanidade |
| R1 | P1 | `baseline-v2` | provedor implantado antes do consumidor |
| R2 | `baseline-v2` | P2 | consumidor implantado antes do provedor |
| R3 | P1 | P2 | os dois implantados |
| RC | E4-controle P | `baseline-v2` | mudança em campo não consumido |

## Previsões

| ID | Previsão |
|----|----------|
| H4.1 | P1 (provedor): só `catalog-service/presentation` muda (2 schemas e o controller); 0 linhas em `domain`, `application` e `infra` do Catalog. |
| H4.2 | Após P1: **0** testes do Agendamento quebram nas duas variantes (os stubs substituem o adaptador). No Catalog, `CatalogApplicationTests` quebra. A quebra do contrato não é detectada por nenhum teste do consumidor. |
| H4.3 | R1: **0%** de sucesso nas duas variantes, com HTTP **500** (o campo ausente gera `DateTimeParseException`, que nenhum handler mapeia). Nenhuma das variantes devolve 503 ou 422. |
| H4.4 | P2 (consumidor): **1 arquivo**, 1 linha alterada em cada variante. Clean: só `infra/http/HttpCatalogGateway`; 0 linhas em `domain`, `application` e `presentation`. Layered: só `client/CatalogClient`; 0 linhas em `service`, `web`, `entity` e `repository`. |
| H4.5 | Após P2: 0 testes do Agendamento quebrados nas duas variantes. |
| H4.6 | R2: 0% de sucesso nas duas variantes, com HTTP 500. R3: 100% de sucesso (201) nas duas. Logo, as duas variantes exigem implantação **coordenada** de 2 serviços: nenhuma ordem de implantação evita a janela de falha. |
| H4.7 | Commit T: só `CatalogApplicationTests` muda; 0 arquivos de teste do Agendamento nas duas variantes. |
| H4.8 | ArchUnit: regras verdes e métricas iguais às da `baseline-v2` nas duas variantes. |
| H4.C1 | E4-controle: 0 linhas no Agendamento; 0 testes do Agendamento quebrados; RC com 100% de sucesso nas duas variantes. Só `CatalogApplicationTests` quebra. |
| H4.C2 | Acoplamento de contrato: 3 de 6 campos da resposta consumidos (`opensAt`, `closesAt`, `openDays`), igual nas duas variantes. O `id` vai na URL e não é lido do corpo. |

Leitura pretendida: se H4.2 a H4.6 se sustentarem, a Clean Architecture **não** altera o acoplamento de
contrato nem o de implantação. A contenção da mudança no consumidor vem, nas duas variantes, de existir um
adaptador HTTP isolado, e não das camadas da Clean.

## Regra de parada

A produção é a mínima idiomática: renomear o componente dos records e as referências diretas a ele, e trocar a
chave lida no adaptador. Não se introduz versionamento de API, *tolerant reader* nem *expand-contract*.

## O que não será testado

- Evolução compatível do contrato (*expand-contract*, versionamento de API, testes de contrato orientados pelo
  consumidor). Fica como trabalho futuro e é discutida apenas analiticamente.
- Mudança de tipo ou de semântica de um campo (só renomeação).
- Mudança no contrato do Identity.
