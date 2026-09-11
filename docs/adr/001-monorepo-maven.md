# ADR-001: Monorepo Maven multi-módulo

## Status

Aceito

## Contexto

O sistema de pesquisa possui vários microsserviços e um módulo de métricas. É preciso versionar tudo junto para que diffs experimentais e linhas de base sejam comparáveis em um único histórico Git.

## Decisão

Usar **um único repositório Git** (monorepo) com **Maven multi-módulo**: um POM pai agregador e módulos sob `services/` e `architecture-metrics/`.

## Consequências

- **Positivas:** um `git diff` cobre o sistema inteiro; tags `fase-0` / `baseline` são globais; build unificado via `./mvnw`
- **Negativas:** repositório cresce com todos os serviços; disciplina necessária para não misturar mudanças de serviços não relacionados no mesmo commit experimental
- **Alternativas rejeitadas:** um repositório por serviço (dificulta medição transversal e onboarding do TCC)
