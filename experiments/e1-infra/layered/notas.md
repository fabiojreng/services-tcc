# E1 — Layered — notas

## Intervenção

Mesma troca **JPA → MongoDB** na variante layered.

Branch: `exp/e1-infra` (a partir de `baseline`).

## Camadas atingidas

| Pacote idiomático | Alterada? |
|-------------------|-----------|
| `entity` | **Sim** (`@Entity` → `@Document`) |
| `repository` | **Sim** (`JpaRepository` → `MongoRepository`, rename) |
| `service` | **Sim** (tipo do repositório, remoção de `@Transactional`) |
| `web` / `client` | Não |
| `pom` / properties / testes | Sim |

Ver `diff-numstat.txt`.

## Interpretação

A entidade anêmica e o repositório Spring Data atravessam o estilo em camadas: a troca de tecnologia **obrigou** alterar `entity` + `repository` + `service`. Não há porta de domínio que absorva o impacto. Mesmo com contrato REST intacto, a propagação interna é maior que na Clean (mais pacotes de “negócio/persistência misturados”).

## Testes

`@MockitoBean` do `ReservationMongoRepository` + exclusão do auto-config Mongo nos testes (sem Docker obrigatório no `mvnw test`).
