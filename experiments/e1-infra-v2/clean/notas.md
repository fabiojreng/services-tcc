# E1-v2 — Clean — notas

Branch `exp2/e1-infra-v2-clean`: P1 `d32fe27`, P2 `72fe55c`, T `58513d0`.

## Produção final (P1 + P2) — 8 arquivos do serviço, 28+/71−, mais Compose

| Arquivo | + | − | Módulo |
|---------|---|---|--------|
| `infra/pom.xml` | 1 | 11 | `infra` |
| `SchedulingCleanApplication.java` | 0 | 4 | `infra` |
| `config/SchedulingConfig.java` | 2 | 2 | `infra` |
| `models/ReservationJpaEntity → ReservationDocument` | 6 | 17 | `infra` |
| `repositories/JpaReservationRepository → MongoReservationRepository` | 13 | 13 | `infra` |
| `repositories/SpringDataReservationRepository.java` | 4 | 4 | `infra` |
| `application-postgres.properties` (removido) | 0 | 10 | `infra` |
| `application.properties` | 2 | 10 | `infra` |
| `infra/docker-compose.yml` (compartilhado) | 9 | 0 | ambiente |

`domain`, `application` e `presentation`: **0 linhas**.

## P2: ajuste exigido pelo MongoDB real

Com o banco real, a primeira requisição falhou com
`CodecConfigurationException: The uuidRepresentation has not been specified`.
No Spring Boot 4, o padrão de `spring.mongodb.representation.uuid` passou a ser `unspecified` (no Boot 3 era `JAVA_LEGACY`).
A correção foi 1 linha em `application.properties`, dentro de `infra`.

Também se constatou que a propriedade usada no E1 original (`spring.data.mongodb.uri`) está depreciada com nível *error* desde o Boot 4.0 e é **ignorada**: aquele adaptador teria se conectado silenciosamente ao banco padrão `test`.

## Testes quebrados

- Após P1: **1 de 8**, o teste de integração (falha ao carregar o contexto). O `spring-boot-starter-data-jpa-test` em escopo de teste recoloca JPA/DataSource no classpath.
- Após P2: **1 de 8** (mesma causa).
- Os 7 testes de domínio permaneceram verdes em todas as etapas.

## Ajuste de testes (commit T) — 2 arquivos, 15+/1−

- `pom.xml` (escopo de teste): `data-jpa-test` → `data-mongodb-test`, mais `testcontainers-mongodb`.
- `SchedulingCleanApplicationTests`: `@Testcontainers` + `MongoDBContainer("mongo:7")` com `@ServiceConnection`.

Final: 8/8 verdes contra **MongoDB real**.

## ArchUnit

13/13 regras verdes. Métricas **idênticas** à `baseline-v2`.

## Comparação com o E1 original

O E1 original (9 arquivos, 70+/80−) incluía ruído: um `@ConditionalOnBean` só para permitir testes sem Mongo, remoção de comentários e uma linha de logging. Também usava uma propriedade ignorada pelo Boot 4. O v2 é menor e funcionou contra um banco real.
