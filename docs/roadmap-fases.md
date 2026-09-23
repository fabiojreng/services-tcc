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

## Fase 2 — Controle e inventário

- [x] `scheduling-service-layered` com **paridade funcional** (suíte `scheduling-acceptance`)
- [x] `inventory-service` (Clean Architecture) — itens, movimentações IN/OUT, saldo
- [x] Permissão `INVENTORY_MANAGE` no Identity
- [x] Regras ArchUnit + métricas Fase 2 (Clean × Layered do Agendamento)
- [x] Tag `fase-2`

## Fase 3 — Ambiente experimental

- [x] WSL2 + Docker Desktop disponíveis
- [x] `infra/docker-compose.yml` — PostgreSQL por serviço + Toxiproxy
- [x] Profiles Spring `postgres` e `e2`; driver PostgreSQL; H2 permanece no default
- [x] Observabilidade mínima — logs estruturados ECS no profile `postgres`
- [x] Tag **`baseline`** — congelamento para E1–E3

## Fase 4 — Experimentos

- [x] E3 — alteração de regra de negócio (branch `exp/e3-regra`)
- [x] E1 — troca de infraestrutura de persistência (branch `exp/e1-infra`)
- [x] E2 — falhas entre serviços (branch `exp/e2-falhas`)
- Artefatos em `experiments/e1-infra/`, `experiments/e2-falhas/`, `experiments/e3-regra/`

Código experimental permanece nas branches `exp/*` (main fica em `baseline` + docs/artefatos).

## Fase 5 — Análise e redação (atual)

- [x] Consolidar CSVs e diffs (`experiments/propagacao-e1-e3.csv`, `fase-5-sintese.md`)
- [x] Discutir benefícios e limitações (incluindo E2 como limitação)
- [x] Texto-base para metodologia/resultados/discussão do monográfico
- [ ] Incorporar trechos ao documento final do TCC (fora do repositório de código, se aplicável)

## Portas locais (aplicação)

| Serviço | Porta |
|---------|-------|
| identity-service | 8080 |
| scheduling-service-clean | 8081 |
| catalog-service | 8082 |
| scheduling-service-layered | 8083 |
| inventory-service | 8084 |

## Portas Docker (Fase 3+)

| Recurso | Porta host |
|---------|------------|
| Postgres Identity | 5432 |
| Postgres Catalog | 5433 |
| Postgres Scheduling Clean | 5434 |
| Postgres Scheduling Layered | 5435 |
| Postgres Inventory | 5436 |
| Toxiproxy API | 8474 |
| Toxiproxy → Identity | 18080 |
| Toxiproxy → Catalog | 18082 |

Detalhes operacionais: [`infra/README.md`](../infra/README.md).

## Dependências externas de ambiente

| Ferramenta | Fase 0–2 | Fase 3+ |
|------------|----------|---------|
| JDK 25 | Obrigatório | Obrigatório |
| Maven wrapper | Incluso | Incluso |
| Docker / WSL2 | Não | Obrigatório para modo `postgres` e E1/E2 |
