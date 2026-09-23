# E1 — Substituição de infraestrutura de persistência

## Intervenção

Trocar **PostgreSQL/JPA (e H2)** por **MongoDB** apenas em `scheduling-service-*`.

Branch: `exp/e1-infra` ← `baseline`.

## Checklist

- [x] Branch a partir de `baseline`
- [x] Novo adaptador Mongo nas duas variantes
- [x] Diff `git diff --numstat baseline...` por variante
- [x] Testes verdes (com stub de persistência nos testes; Mongo em runtime via Compose)
- [x] Notas em `clean/notas.md` e `layered/notas.md`
- [x] Container `mongo-scheduling` no `infra/docker-compose.yml`

## Comparativo

| Métrica | Clean | Layered |
|---------|-------|---------|
| `domain` / núcleo de regras | 0 arquivos | n/a (não existe) |
| Pacotes de produção tocados | só `infra` | `entity` + `repository` + `service` |
| Hipótese E1 | Confirmada (mudança no adaptador) | Confirmada (propagação para camadas de negócio/API idiomáticas) |

## Runtime

```powershell
docker compose -f infra/docker-compose.yml up -d mongo-scheduling
# URI padrão: mongodb://localhost:27017/scheduling_clean | scheduling_layered
```
