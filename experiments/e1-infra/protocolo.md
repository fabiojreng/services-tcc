# E1 — Substituição de infraestrutura de persistência

Branch a partir da tag `baseline`. Ver protocolo em [`docs/03-protocolo-experimental.md`](../../docs/03-protocolo-experimental.md).

## Checklist

- [ ] Branch `exp/e1-infra-clean` e `exp/e1-infra-layered` (ou equivalente)
- [ ] Trocar PostgreSQL/JPA por MongoDB (ou outro store) **apenas** no Agendamento
- [ ] Coletar diff (`git diff --numstat baseline...HEAD`)
- [ ] Reexportar métricas ArchUnit
- [ ] Registrar notas em `clean/notas.md` e `layered/notas.md`
