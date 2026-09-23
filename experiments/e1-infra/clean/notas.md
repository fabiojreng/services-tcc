# E1 — Clean — notas

## Intervenção

Substituir **JPA/H2/PostgreSQL** por **MongoDB** no Agendamento Clean.

Branch: `exp/e1-infra` (a partir de `baseline`).

## Camadas atingidas

| Área | Alterada? |
|------|-----------|
| `domain` | **Não** |
| `application` | **Não** |
| `presentation` | **Não** |
| `infra` | **Sim** (pom, document, adapters, config, properties, testes de infra) |

Ver `diff-numstat.txt`.

## Interpretação

A porta `ReservationRepository` no domínio isolou a troca de store: domínio e casos de uso permaneceram intactos. Toda a mudança ficou em `infra` (+ ajuste de teste de bootstrap). Isso confirma a hipótese do E1 para a variante Clean quanto à **propagação limitada ao adaptador**.

## Testes

Integração HTTP continua verde com repositório em memória no `@TestConfiguration` (Mongo real fica para runtime/`docker compose` com `mongo-scheduling`). Domínio: 7/7 sem alteração.
