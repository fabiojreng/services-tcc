# E2 — Falhas entre serviços

## Intervenção

Indisponibilizar Identity e/ou Catalog durante `POST /api/reservations`.

Branch: `exp/e2-falhas` ← `baseline`.

## Checklist

- [x] Simulação automatizada (stubs de falha) nas duas variantes — testes `*FailureSimulationTests`
- [x] Registrar status HTTP e locus da falha (`clean/notas.md`, `layered/notas.md`)
- [x] Confirmar hipótese: degradação semelhante (503 / sucesso 0%)
- [ ] Opcional com Docker: repetir via Toxiproxy (`profiles=postgres,e2` + toxics na API `:8474`)

## Diff de produção

Nenhuma mudança de código de produção foi necessária — apenas testes de evidência. Ver `diff-numstat.txt` (só testes).

## Comparativo

| Métrica | Clean | Layered |
|---------|-------|---------|
| Sucesso sob Identity down | 0% | 0% |
| Status HTTP | 503 | 503 |
| Domínio/policy afetado? | Não (falha antes) | N/A (regra no service; falha no client) |
| Mitigação pela Clean? | Não para acoplamento temporal | — |

## Toxiproxy (opcional)

```powershell
docker compose -f infra/docker-compose.yml up -d toxiproxy
# Agendamento: -Dspring-boot.run.profiles=postgres,e2
curl -X POST http://localhost:8474/proxies/identity/toxics -H "Content-Type: application/json" -d "{\"name\":\"down\",\"type\":\"timeout\",\"attributes\":{\"timeout\":1}}"
```
