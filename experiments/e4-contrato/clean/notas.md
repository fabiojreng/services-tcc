# E4 — Clean — notas

Branch `exp2/e4-contrato-clean` (P1 `0988dba`, P2 `d926515`, T `61e9b8e`, artefatos `9237a6a`).

## Provedor (commit P1) — 3 arquivos, 3+/3−, comum às duas variantes

| Arquivo | + | − | Módulo |
|---------|---|---|--------|
| `catalog-service/presentation/schemas/RegisterLaboratoryRequest.java` | 1 | 1 | `presentation` |
| `catalog-service/presentation/schemas/LaboratoryResponse.java` | 1 | 1 | `presentation` |
| `catalog-service/presentation/routes/LaboratoryController.java` | 1 | 1 | `presentation` |

O domínio do Catalog (`OperatingHours.opensAt()`) e a entidade JPA não mudaram: o nome no JSON e o nome no
domínio passaram a ser diferentes, o que a separação entre `schemas` e `domain` do Catalog permite sem custo.

## Testes quebrados após P1

**Agendamento: 0 de 8.** `SchedulingCleanApplicationTests` substitui `CatalogGateway` por um stub, então o
adaptador HTTP não é exercitado. `ReservationPolicyTest` não depende do Catalog.

**Catalog: 1 de 1.** `CatalogApplicationTests.registraEConsultaLaboratorio` com `NullPointerException` no POST:
o teste ainda envia `opensAt`, o campo `openingTime` chega nulo e `OperatingHours.of` falha. Saída:
`testes-quebrados-P1.txt`.

## Consumidor (commit P2) — 1 arquivo, 1+/1−

`infra/http/HttpCatalogGateway.java`, linha 46: `body.get("opensAt")` → `body.get("openingTime")`.

A porta `application/ports/CatalogGateway`, o value object `domain/value_objects/OperatingHours`, o caso de uso
e a `presentation` não mudaram. Após P2: 0 de 8 quebrados no Agendamento; o teste do Catalog continua quebrado
(`testes-quebrados.txt`).

## Ajuste de testes (commit T) — 1 arquivo, 2+/2−

`CatalogApplicationTests`: `opensAt` → `openingTime` no corpo do POST e no `jsonPath`. Nenhum teste do
Agendamento mudou. Final: 9/9 (8 do Agendamento e 1 do Catalog).

## Execução real

| Estado | Resultado |
|--------|-----------|
| R1 (Catalog novo, Clean antigo) | 500 × 10; `DateTimeParseException: Text 'null'` |
| R2 (Catalog antigo, Clean novo) | 500 × 10; mesma exceção |
| R3 (os dois novos) | 201 × 10 |

A exceção nasce dentro do adaptador, mas não é `RestClientException`, então não vira `RemoteDependencyException`
nem 503. O `DomainExceptionHandler` não a trata e a resposta é o 500 genérico.

## ArchUnit

Regras verdes. Métricas iguais às da `baseline-v2` (63 linhas, 0 diferenças).

## Leitura

A fronteira da Clean conteve a mudança em `infra`, como no E1-v2. Aqui, porém, a Layered conteve a mesma
mudança num único arquivo também (ver `../layered/notas.md`). A porta não reduziu o custo nem trouxe detecção:
o stub de teste fica atrás da porta, e a quebra está na frente dela, no adaptador.
