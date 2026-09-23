# E2 — Layered — notas

## Intervenção

Mesma simulação: Identity/Catalog indisponíveis.

Instrumento: clients HTTP stubados lançando `IllegalStateException` → `GlobalExceptionHandler` (503).

## Resultado

| Dependência | HTTP | Onde a falha é tratada |
|-------------|------|------------------------|
| Identity down | **503** | `GlobalExceptionHandler` ← exceção do `IdentityClient` no `@Service` |
| Catalog down | **503** | idem via `CatalogClient` |

Taxa de sucesso sob falha: **0%** (igual à Clean).

## Interpretação

Comportamento observável **equivalente** ao Clean sob falha síncrona. A falha “vive” no service/client (não há porta nomeada de dependência remota), mas o usuário final vê o mesmo tipo de degradação. Evidência das **limitações** da Clean quanto ao desacoplamento entre serviços (Dimensão 2).
