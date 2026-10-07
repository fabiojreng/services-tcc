# E1-v2 — Layered — notas

Branch `exp2/e1-infra-v2-layered`: P1 `d5b09f1`, P2 `afffdc8`, T `b547a59`.

## Produção final (P1 + P2) — 6 arquivos do serviço, 11+/50−, mais Compose

| Arquivo | + | − | Pacote |
|---------|---|---|--------|
| `pom.xml` | 1 | 11 | build |
| `entity/ReservationEntity.java` | 3 | 14 | `entity` |
| `repository/ReservationJpaRepository → ReservationMongoRepository` | 2 | 2 | `repository` |
| `service/ReservationService.java` | 3 | 3 | `service` |
| `application-postgres.properties` (removido) | 0 | 10 | config |
| `application.properties` | 2 | 10 | config |
| `infra/docker-compose.yml` (compartilhado) | 9 | 0 | ambiente |

A mudança no `service` é **apenas a troca do tipo** do repositório (import, campo e construtor). A lógica de negócio não foi tocada.

## P2: ajuste exigido pelo MongoDB real

O mesmo defeito da Clean (`uuidRepresentation` não especificada), corrigido com a mesma linha em `application.properties`.
Viés de aprendizado declarado: a Clean foi executada primeiro, mas manteve-se a mesma sequência (P1 sem a correção, P2 com ela) para que as medições fossem comparáveis.

## `@Transactional` silenciosamente inativo

O `ReservationService` continua anotado com `@Transactional`, e os testes passam. Com MongoDB e sem `TransactionManager`, o Spring Boot não habilita o gerenciamento de transações, e a anotação vira um **no-op silencioso**. O E1 original removeu a anotação; o v2 não precisou removê-la para funcionar. Em ambos os casos, a semântica transacional que o `service` declarava deixou de existir sem nenhum aviso. Esse é um vazamento da tecnologia de persistência para a camada de negócio que não aparece no diff.

## Testes quebrados

- Após P1: **1 de 1**, o único teste da variante (mesma causa da Clean: `data-jpa-test` no classpath de teste).
- Após P2: **1 de 1**.
- A Layered não tem nenhum teste independente da persistência.

## Ajuste de testes (commit T) — 2 arquivos, 15+/1−

O mesmo ajuste da Clean: `pom.xml` em escopo de teste e Testcontainers no teste de integração. Final: 1/1 verde contra MongoDB real.

## ArchUnit

Regras verdes. Métricas: o CCD (Lakos) passou de **18 para 22**, e o Ca de `repository`, de 0 para 2.
O bytecode do `ReservationService` referencia o repositório tanto na `baseline-v2` quanto no v2, então a variação **não corresponde** a uma dependência nova no código-fonte. Ela parece um artefato do importador do ArchUnit, que não registrou `service → repository` quando o repositório estendia `JpaRepository`. **Resultado tratado como inconclusivo.**

## Comparação com o E1 original

O E1 original (7 arquivos, 50+/60−) removia o `@Transactional` e usava `@MockitoBean` do repositório nos testes, ou seja, nunca tocava um Mongo real. O v2 é menor e funcionou contra um banco real.
