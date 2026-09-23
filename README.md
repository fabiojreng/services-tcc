# LabManager — Sistema de pesquisa (TCC)

Repositório de suporte ao Trabalho de Conclusão de Curso:

> **Uma avaliação do papel da Clean Architecture na mitigação do monolito distribuído em arquiteturas de microsserviços: um estudo de caso em gestão de laboratórios.**

Autor: Fábio da Silva Eloi Júnior  
Instituição: IFMA — Campus Santa Inês  
Orientador: Prof. Dr. Ernesto Franklin Marçal Ferreira

## Objetivo deste repositório

Não é um produto comercial. É o **artefato experimental** da pesquisa: um sistema de gestão de laboratórios em microsserviços, usado para medir desacoplamento interno e entre serviços, com e sem Clean Architecture.

A questão de pesquisa:

> Em que medida a aplicação da Clean Architecture contribui para o desacoplamento em arquiteturas de microsserviços, e quais são suas limitações quando aplicada a sistemas distribuídos?

## Stack

| Item | Escolha |
|------|---------|
| Linguagem | Java 25 LTS |
| Framework | Spring Boot 4.1.x |
| Build | Maven (wrapper incluso) |
| Persistência (default) | H2 embutido |
| Persistência (experimental) | PostgreSQL 16 via Docker (profile `postgres`) |
| Falhas (E2) | Toxiproxy |
| Métricas arquiteturais | ArchUnit 1.4.x |

## Serviços

| Serviço | Papel | Variantes |
|---------|-------|-----------|
| `identity-service` | Usuários, papéis, permissões | Clean Architecture |
| `catalog-service` | Laboratórios, equipamentos, horários | Clean Architecture |
| `scheduling-service` | Reservas, conflitos, aprovação (**serviço-alvo**) | Clean + Layered |
| `inventory-service` | Consumíveis, movimentações | Clean Architecture |

Apenas o **Agendamento** possui duas implementações com o mesmo contrato REST, para comparação experimental controlada.

## Estrutura

```
docs/                     Escopo, critérios, protocolo, ADRs
services/                 Microsserviços
architecture-metrics/     Instrumento de medição (ArchUnit)
experiments/              Protocolos e resultados de E1, E2, E3
infra/                    Docker Compose (PostgreSQL + Toxiproxy)
```

## Fases

| Fase | Conteúdo |
|------|----------|
| 0 | Fundação: docs, esqueleto Maven, walking skeleton do Agendamento (Clean), métricas |
| 1 | Catalog + Identity + comunicação REST |
| 2 | Variante layered do Agendamento + Inventory |
| **4** (atual) | Experimentos E1, E2, E3 |
| 5 | Análise e redação |

Portas da aplicação: identity `8080`, scheduling-clean `8081`, catalog `8082`, scheduling-layered `8083`, inventory `8084`.

Detalhes em [`docs/roadmap-fases.md`](docs/roadmap-fases.md) e [`infra/README.md`](infra/README.md).

## Como construir

Pré-requisitos: **JDK 25**. Maven não precisa estar instalado (use o wrapper).

```bash
./mvnw clean verify
```

No Windows (PowerShell):

```powershell
.\mvnw.cmd clean verify
```

## Infraestrutura Docker (Fase 3)

```powershell
docker compose -f infra/docker-compose.yml up -d
```

## Como subir os serviços

### Modo rápido (H2, sem Docker)

```powershell
.\mvnw.cmd -pl services/identity-service/infra -am spring-boot:run
.\mvnw.cmd -pl services/scheduling-service-clean/infra -am spring-boot:run
.\mvnw.cmd -pl services/catalog-service/infra -am spring-boot:run
.\mvnw.cmd -pl services/scheduling-service-layered -am spring-boot:run
.\mvnw.cmd -pl services/inventory-service/infra -am spring-boot:run
```

### Modo experimental (PostgreSQL)

Com a infra Docker no ar, acrescente o profile `postgres`:

```powershell
.\mvnw.cmd -pl services/identity-service/infra -am spring-boot:run "-Dspring-boot.run.profiles=postgres"
.\mvnw.cmd -pl services/catalog-service/infra -am spring-boot:run "-Dspring-boot.run.profiles=postgres"
.\mvnw.cmd -pl services/scheduling-service-clean/infra -am spring-boot:run "-Dspring-boot.run.profiles=postgres"
.\mvnw.cmd -pl services/scheduling-service-layered -am spring-boot:run "-Dspring-boot.run.profiles=postgres"
.\mvnw.cmd -pl services/inventory-service/infra -am spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Para o experimento E2 (Agendamento via Toxiproxy):

```powershell
.\mvnw.cmd -pl services/scheduling-service-clean/infra -am spring-boot:run "-Dspring-boot.run.profiles=postgres,e2"
```

No PowerShell, sempre cite `-D...` entre aspas.

## Documentação da pesquisa

- [`docs/00-escopo.md`](docs/00-escopo.md)
- [`docs/01-contextos-delimitados.md`](docs/01-contextos-delimitados.md)
- [`docs/02-criterios-desacoplamento.md`](docs/02-criterios-desacoplamento.md)
- [`docs/03-protocolo-experimental.md`](docs/03-protocolo-experimental.md)
- [`docs/04-ameacas-validade.md`](docs/04-ameacas-validade.md)
- [`docs/adr/`](docs/adr/)

## Licença

Uso acadêmico — IFMA / TCC Engenharia da Computação.
