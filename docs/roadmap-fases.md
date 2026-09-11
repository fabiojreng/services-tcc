# Roadmap das fases

## Fase 0 — Fundação (atual)

- [x] Repositório Git + README + `.gitignore`
- [x] Documentação de escopo, contextos, critérios, protocolo, ameaças
- [x] ADRs das decisões fechadas
- [x] Esqueleto Maven multi-módulo + wrapper
- [x] Walking skeleton: `scheduling-service-clean` (Solicitar Reserva)
- [x] Módulo `architecture-metrics`
- [x] Tag `fase-0`

## Fase 1 — Catálogo, identidade e rede

- `catalog-service` e `identity-service` (Clean Architecture)
- Clientes HTTP no Agendamento para Catalog e Identity
- Testes de integração (ainda com H2 / processos locais ou Testcontainers se Docker já estiver disponível)

## Fase 2 — Controle e inventário

- `scheduling-service-layered` com **paridade funcional** (mesma suíte de aceitação)
- `inventory-service`
- Revisão das métricas internas nas duas variantes do Agendamento

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

## Dependências externas de ambiente

| Ferramenta | Fase 0 | Fase 3+ |
|------------|--------|---------|
| JDK 25 | Obrigatório | Obrigatório |
| Maven wrapper | Incluso | Incluso |
| Docker / WSL2 | Não | Obrigatório para E1/E2 reprodutíveis |
