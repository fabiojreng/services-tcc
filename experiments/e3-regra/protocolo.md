# E3 — Alteração de regra de negócio

Branch a partir da tag `baseline`. Ver protocolo em [`docs/03-protocolo-experimental.md`](../../docs/03-protocolo-experimental.md).

## Checklist

- [ ] Escolher uma regra (ex.: antecedência 24h → 48h, ou duração máx. 4h → 2h)
- [ ] Aplicar a **mesma** mudança em Clean e Layered
- [ ] Coletar diff por camada / pacote
- [ ] Verificar quais testes quebraram (domínio vs. integração)
- [ ] Notas em `clean/notas.md` e `layered/notas.md`
