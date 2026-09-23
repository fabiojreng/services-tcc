# E2 — Clean — notas

## Intervenção

Simular indisponibilidade de **Identity** e **Catalog** no caso de uso Solicitar Reserva.

Instrumento principal neste artefato: stubs que lançam `RemoteDependencyException` (equivalente lógico ao Toxiproxy derrubando a dependência). Profile `e2` + Toxiproxy permanece disponível quando Docker Desktop estiver ativo (`infra/README.md`).

## Resultado

| Dependência | HTTP | Onde a falha é tratada |
|-------------|------|------------------------|
| Identity down | **503** | `DomainExceptionHandler` ← `RemoteDependencyException` (application/infra HTTP) |
| Catalog down | **503** | idem |

Taxa de sucesso do caso de uso sob falha: **0%** (esperado).

## Interpretação

A Clean Architecture **contém** a falha fora do domínio (`ReservationPolicy` não é invocada quando Identity falha antes), mas **não elimina** o acoplamento temporal: o request ainda falha. Padrões de resiliência (timeout, circuit breaker, fallback) seriam ortogonais à Clean e necessários nas duas variantes.
