# Roadmap das fases

## Fase 0 — Fundação

- [x] Repositório Git + README + `.gitignore`
- [x] Documentação de escopo, contextos, critérios, protocolo, ameaças
- [x] ADRs das decisões fechadas
- [x] Esqueleto Maven multi-módulo + wrapper
- [x] Walking skeleton: `scheduling-service-clean` (Solicitar Reserva)
- [x] Módulo `architecture-metrics`
- [x] Tag `fase-0`

## Fase 1 — Catálogo, identidade e rede

- [x] `identity-service` (Clean Architecture) — usuários, papéis, `POST /api/permissions/check`
- [x] `catalog-service` (Clean Architecture) — laboratórios + horário; consome Identity em cadastro
- [x] Clientes HTTP no Agendamento (`CatalogGateway`, `IdentityGateway`)
- [x] Request de reserva sem OperatingHours no body (obtido do Catalog)
- [x] Regras ArchUnit para os três serviços + métricas Fase 1
- [x] Tag `fase-1`

## Fase 2 — Controle e inventário (atual)

- [x] `scheduling-service-layered` com **paridade funcional** (suíte `scheduling-acceptance`)
- [x] `inventory-service` (Clean Architecture) — itens, movimentações IN/OUT, saldo
- [x] Permissão `INVENTORY_MANAGE` no Identity
- [x] Regras ArchUnit + métricas Fase 2 (Clean × Layered do Agendamento)
- [x] Tag `fase-2`

## Fase 3 — Ambiente experimental

- Instalar WSL2 + Docker Desktop
- `infra/docker-compose.yml` (PostgreSQL por serviço, opcional broker, Toxiproxy)
- Observabilidade mínima (logs estruturados; tracing opcional)
- Tag **`baseline`** — congelamento para E1–E3

## Fase 4 — Experimentos

- E3 — alteração de regra de negócio
- E1 — troca de infraestrutura de persistência
- E2 — falhas entre serviços
- Artefatos em `experiments/`

## Fase 5 — Análise e redação

- Consolidar CSVs e diffs
- Discutir benefícios e limitações (incluindo resultado esperado de E2)
- Alimentar seções de metodologia/resultados do monográfico

## Portas locais (Fase 2)

| Serviço | Porta |
|---------|-------|
| identity-service | 8080 |
| scheduling-service-clean | 8081 |
| catalog-service | 8082 |
| scheduling-service-layered | 8083 |
| inventory-service | 8084 |

## Dependências externas de ambiente

| Ferramenta | Fase 0–1 | Fase 3+ |
|------------|----------|---------|
| JDK 25 | Obrigatório | Obrigatório |
| Maven wrapper | Incluso | Incluso |
| Docker / WSL2 | Não | Obrigatório para E1/E2 reprodutíveis |
