# ADR-006: Docker Compose + PostgreSQL na Fase 3; H2 permanece no default

## Status

Aceito

## Contexto

A Fase 0–2 usou H2 embutido (ADR-005) para não bloquear o desenvolvimento. Para E1 (troca de store) e E2 (falhas via Toxiproxy), o ambiente precisa de bancos reais e orquestração reprodutível. WSL2 + Docker Desktop estão disponíveis na máquina de desenvolvimento.

## Decisão

- Ativar `infra/docker-compose.yml` com **um PostgreSQL por serviço** (autonomia de dados) e **Toxiproxy** para E2
- Microsserviços continuam rodando no host (`spring-boot:run`); containers só para infra
- Profile Spring **`postgres`**: JDBC PostgreSQL + logs estruturados ECS
- Profile Spring **`e2`**: Agendamento aponta Identity/Catalog para portas do Toxiproxy
- Profile default (sem flag): permanece **H2 em memória** — testes e desenvolvimento rápido sem Docker
- Tag Git **`baseline`** congela este estado para branches dos experimentos E1–E3

## Consequências

- **Positivas:** E1/E2 reprodutíveis; domínio inalterado; H2 ainda funciona para CI/`mvnw verify`
- **Negativas:** exige Docker ligado para o modo experimental; cinco containers Postgres + Toxiproxy
- **Alternativas rejeitadas:** colocar todos os serviços em containers agora (fora do escopo da Fase 3); abandonar H2 (quebraria testes offline)
