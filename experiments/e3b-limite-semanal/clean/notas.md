# E3b — Clean — notas

Branch `exp2/e3b-limite-semanal-clean` (P `f197d16`, T `2b37d20`).

## Produção (commit P) — 5 arquivos, 71+/0−

| Arquivo | + | − | Módulo |
|---------|---|---|--------|
| `domain/policy/WeeklyReservationQuota.java` (novo) | 47 | 0 | `domain` |
| `domain/repository/ReservationRepository.java` (porta) | 3 | 0 | `domain` |
| `application/use_cases/RequestReservationUseCase.java` | 9 | 0 | `application` |
| `infra/repositories/JpaReservationRepository.java` | 8 | 0 | `infra` |
| `infra/repositories/SpringDataReservationRepository.java` | 4 | 0 | `infra` |

`presentation`: 0 linhas. A invariante (contar reservas ativas e comparar com 3) está só no domínio. `application` apenas orquestra (calcula a semana, consulta e chama a política). `infra` apenas implementa a consulta por intervalo.

## Testes quebrados após P

**0 de 8.** Nenhum teste implementa a porta `ReservationRepository`, então acrescentar um método não quebrou compilação.

## Ajuste de testes (commit T) — 3 arquivos, 92+/0−

- `WeeklyReservationQuotaTest` (novo, 63 linhas, 4 testes): teste de unidade puro, sem Spring e sem dublês.
- `ReservationAcceptanceSupport` (+24): cenário compartilhado em que a 4ª reserva da semana recebe 422.
- `SchedulingCleanApplicationTests` (+5): chama o cenário compartilhado.

Final: 13/13 verdes.

## ArchUnit

Regras verdes. Métricas alteradas em relação à `baseline-v2`:

| Pacote | Ca | Ce | I | D |
|--------|----|----|---|---|
| `domain.policy` | 1 → 3 | 3 → 3 | 0,75 → 0,50 | 0,25 → 0,50 |
| `application` (árvore) | 5 → 5 | 5 → 6 | 0,50 → 0,55 | 0,07 → 0,03 |
| `application.use_cases` | 0 → 0 | 6 → 7 | 1,00 → 1,00 | 0 → 0 |

Nenhuma dependência nova saindo do domínio (Ce de `domain.policy` inalterado).

## Leitura

- Para uma regra que exige **novo acesso a dados**, a Clean custou mais: 2,5× os arquivos e 3,7× as linhas da Layered.
- Contrapartida: a regra ficou testável em isolamento (4 testes em milissegundos), e o que mudou em `infra` é mecânico.
- **Trade-off de eficiência:** para manter o conhecimento "quais status contam" no domínio, a porta devolve as reservas do intervalo, e a filtragem por status acontece em memória. A Layered conta direto no banco (`countBy...StatusIn`). A pureza do domínio custou uma consulta menos eficiente. Isso é irrelevante nesta escala, mas é real.
