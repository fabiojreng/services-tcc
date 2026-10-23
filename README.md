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
web-console/              Interface web (console) para operar os serviços
architecture-metrics/     Instrumento de medição (ArchUnit)
experiments/              Protocolos e resultados de E1, E2, E3, E3b e E4
infra/                    Docker Compose (PostgreSQL + Toxiproxy)
```

## Fases

| Fase | Conteúdo |
|------|----------|
| 0 | Fundação: docs, esqueleto Maven, walking skeleton do Agendamento (Clean), métricas |
| 1 | Catalog + Identity + comunicação REST |
| 2 | Variante layered do Agendamento + Inventory |
| 3 | Docker, PostgreSQL, Toxiproxy, tag `baseline` |
| 4 | Experimentos E1, E2, E3 |
| 5 | Análise e redação (síntese em `experiments/fase-5-sintese.md`) |
| **6** (atual) | Reforço metodológico: pré-registro, dois commits, Mongo real, Toxiproxy real, E3b (síntese em `experiments/fase-6-sintese.md`); adendo E4, mudança de contrato do Catalog (`experiments/e4-contrato/protocolo.md`) |

Portas da aplicação: identity `8080`, scheduling-clean `8081`, catalog `8082`, scheduling-layered `8083`, inventory `8084`, console web `8090`.

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

### Console web

Interface visual para operar os serviços (usuários, laboratórios, reservas, inventário), com status de cada serviço, diagrama de dependências, comparação lado a lado das variantes Clean/Layered do Agendamento e log das requisições.

```powershell
.\mvnw.cmd -pl web-console -am spring-boot:run
```

Para subir os cinco serviços e o console de uma vez, em segundo plano (logs em `logs/`):

```powershell
.\scripts\subir-servicos.ps1                    # H2
.\scripts\subir-servicos.ps1 -Perfil postgres   # com a infra Docker no ar
.\scripts\subir-servicos.ps1 -Parar
```

Abra [http://localhost:8090](http://localhost:8090). O console encaminha as chamadas para os serviços via `/proxy/{serviço}/...` (endereços em `web-console/src/main/resources/application.properties`), então os serviços não precisam de CORS. Ele fica fora de `services/` e não entra nas métricas nem nos diffs dos experimentos.

## Documentação da pesquisa

- [`docs/00-escopo.md`](docs/00-escopo.md)
- [`docs/01-contextos-delimitados.md`](docs/01-contextos-delimitados.md)
- [`docs/02-criterios-desacoplamento.md`](docs/02-criterios-desacoplamento.md)
- [`docs/03-protocolo-experimental.md`](docs/03-protocolo-experimental.md)
- [`docs/04-ameacas-validade.md`](docs/04-ameacas-validade.md)
- [`docs/adr/`](docs/adr/)
- [`experiments/fase-5-sintese.md`](experiments/fase-5-sintese.md) — consolidação da primeira rodada de experimentos
- [`experiments/fase-6-preregistro.md`](experiments/fase-6-preregistro.md) — hipóteses pré-registradas da replicação
- [`experiments/fase-6-sintese.md`](experiments/fase-6-sintese.md) — resultados vigentes (replicação controlada)
- [`experiments/e4-preregistro.md`](experiments/e4-preregistro.md) e [`experiments/e4-contrato/protocolo.md`](experiments/e4-contrato/protocolo.md) — E4: mudança de contrato entre serviços

## Licença

Uso acadêmico — IFMA / TCC Engenharia da Computação.
