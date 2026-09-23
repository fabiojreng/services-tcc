# E2 — Falhas entre serviços

Branch a partir da tag `baseline`. Infra: Toxiproxy em `infra/docker-compose.yml`.

## Checklist

- [ ] Identity e Catalog no ar; Agendamento com profiles `postgres,e2`
- [ ] Injetar toxics (latência / timeout / reset) via `http://localhost:8474`
- [ ] Executar cenários de reserva em Clean e Layered
- [ ] Registrar taxa de erro, tempo de falha e se a falha “vaza” para o domínio
- [ ] Notas em `clean/notas.md` e `layered/notas.md`
