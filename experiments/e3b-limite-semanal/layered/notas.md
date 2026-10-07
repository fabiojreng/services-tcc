# E3b — Layered — notas

Branch `exp2/e3b-limite-semanal-layered` (P `1d87cd0`, T `a86b353`).

## Produção (commit P) — 2 arquivos, 18+/1−

| Arquivo | + | − | Pacote |
|---------|---|---|--------|
| `repository/ReservationJpaRepository.java` | 6 | 0 | `repository` |
| `service/ReservationService.java` | 12 | 1 | `service` |

Regra e consulta ficam juntas: o `service` calcula a semana, passa os status ativos para o método derivado do Spring Data e compara com o limite.

## Testes quebrados após P

**0 de 1.**

## Ajuste de testes (commit T) — 2 arquivos, 29+/0−

- `ReservationAcceptanceSupport` (+24): o mesmo cenário compartilhado da Clean.
- `SchedulingLayeredApplicationTests` (+5).

Final: 2/2 verdes. A regra é verificada **apenas** por teste de integração, com contexto Spring e H2.

## ArchUnit

Regras verdes. Métricas de pacote **idênticas** à `baseline-v2`. As dependências `service → repository` e `repository → entity` já existiam.

## Leitura

- Mudança mais barata, com 2 arquivos num único pacote de negócio mais o repositório.
- A regra de "quais status contam" passou a ser um **argumento da consulta** (`EnumSet` enviado ao repositório). O conhecimento de negócio fica distribuído entre o `service` e a assinatura do método de dados.
- Um teste de unidade com Mockito seria possível, mas exigiria mockar o repositório e os dois clientes HTTP (Identity e Catalog) que o mesmo `@Service` usa. Não foi feito, para manter a prática idiomática de referência.
