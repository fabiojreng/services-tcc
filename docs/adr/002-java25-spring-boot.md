# ADR-002: Java 25 LTS + Spring Boot 4.1

## Status

Aceito

## Contexto

O ambiente de desenvolvimento já possui JDK 25 LTS. É necessário um ecossistema com bom suporte a testes arquiteturais (ArchUnit) e a APIs REST típicas de microsserviços.

## Decisão

- **Java 25** como linguagem e bytecode alvo
- **Spring Boot 4.1.x** como framework de aplicação
- **Maven Wrapper** para builds sem instalação global de Maven
- **ArchUnit 1.5.x** para métricas e regras de fronteira (suporte a class file do Java 25)

## Consequências

- **Positivas:** alinhamento com o JDK instalado; métricas de Martin/Lakos nativas no ArchUnit; stack familiar na literatura de microsserviços
- **Negativas:** Spring Boot 4 é relativamente recente — possíveis ajustes de dependências ao longo do TCC
- **Alternativas rejeitadas:** Node/TypeScript, Python/FastAPI, Go (ferramental de métricas arquiteturais menos alinhado às métricas de Martin usadas na fundamentação)
