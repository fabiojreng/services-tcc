# E4 — Layered — notas

Branch `exp2/e4-contrato-layered` (P1 `3504463`, P2 `79f1771`, T `75effef`, artefatos `24fb475`).

## Provedor (commit P1) — 3 arquivos, 3+/3−

Idêntico ao P1 da Clean (mesma árvore; ver `../clean/notas.md`).

## Testes quebrados após P1

**Agendamento: 0 de 1.** `SchedulingLayeredApplicationTests` substitui `CatalogClient` por uma subclasse
anônima que devolve `LaboratoryHours` fixo; o parse do JSON não é exercitado.

**Catalog: 1 de 1.** Mesmo erro da Clean (`testes-quebrados-P1.txt`).

## Consumidor (commit P2) — 1 arquivo, 1+/1−

`client/CatalogClient.java`, linha 39: `body.get("opensAt")` → `body.get("openingTime")`.

`service/ReservationService`, `web`, `entity` e `repository` não mudaram: o `service` recebe o record
`CatalogClient.LaboratoryHours`, cujos componentes não dependem do nome no JSON. Após P2: 0 de 1 quebrados no
Agendamento (`testes-quebrados.txt`).

## Ajuste de testes (commit T) — 1 arquivo, 2+/2−

Idêntico ao T da Clean (só `CatalogApplicationTests`). Final: 2/2.

## Execução real

| Estado | Resultado |
|--------|-----------|
| R1 (Catalog novo, Layered antiga) | 500 × 10; `DateTimeParseException: Text 'null'` |
| R2 (Catalog antigo, Layered nova) | 500 × 10; mesma exceção |
| R3 (os dois novos) | 201 × 10 |

`CatalogClient` só traduz `RestClientException` para `IllegalStateException` (que vira 503). A exceção de parse
escapa da tradução e o `GlobalExceptionHandler` não a trata: 500 genérico, como na Clean.

## ArchUnit

Regras verdes. Métricas iguais às da `baseline-v2` (63 linhas, 0 diferenças).

## Leitura

Sem portas nem módulos Maven separados, a Layered conteve a mudança de contrato no mesmo lugar e com o mesmo
custo da Clean. O que conteve foi o cliente HTTP isolado, que a Layered idiomática também tem.
