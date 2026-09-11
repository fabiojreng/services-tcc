# ADR-005: Sem Docker na Fase 0; preparado para migração

## Status

Aceito

## Contexto

A máquina de desenvolvimento não possui Docker Desktop nem WSL2. Docker é desejável para E1 (troca de store) e E2 (Toxiproxy), mas não bloqueia a fundação do código nem as métricas internas (ArchUnit).

## Decisão

- **Fase 0:** persistência H2 embutida; serviços como JARs Spring Boot; profiles Spring (`h2`, futuro `postgres`)
- **Fase 3:** instalar WSL2 + Docker Desktop; ativar `infra/docker-compose.yml`
- Manter o repositório **já estruturado** para os dois modos (sem reescrever o domínio)

## Consequências

- **Positivas:** progresso imediato na Fase 0; domínio independente do store
- **Negativas:** E1/E2 reais adiados; ambiente local menos próximo do experimental até a Fase 3
- **Alternativas rejeitadas:** bloquear todo o desenvolvimento até instalar Docker; usar só H2 também nos experimentos finais (menos realista para E1)
