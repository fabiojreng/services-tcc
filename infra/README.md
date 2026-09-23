# Infraestrutura experimental (Fase 3+)

Ambiente Docker para os experimentos E1–E3. Os microsserviços **não** rodam em containers — apenas PostgreSQL (um banco por serviço) e Toxiproxy.

## Pré-requisitos

- Docker Desktop (com WSL2 no Windows)
- JDK 25 + Maven wrapper na raiz do repositório

## Subir a infraestrutura

Na raiz do repositório:

```powershell
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml ps
```

## Portas

| Recurso | Host |
|---------|------|
| PostgreSQL Identity | `localhost:5432` / DB `identity` |
| PostgreSQL Catalog | `localhost:5433` / DB `catalog` |
| PostgreSQL Scheduling Clean | `localhost:5434` / DB `scheduling` |
| PostgreSQL Scheduling Layered | `localhost:5435` / DB `scheduling_layered` |
| PostgreSQL Inventory | `localhost:5436` / DB `inventory` |
| Toxiproxy API | `localhost:8474` |
| Toxiproxy → Identity | `localhost:18080` |
| Toxiproxy → Catalog | `localhost:18082` |

Credenciais Postgres: usuário/senha `labmanager`.

## Rodar serviços com PostgreSQL

Ative o profile `postgres` (e opcionalmente `e2` para trafegar via Toxiproxy):

```powershell
# Infra já no ar
docker compose -f infra/docker-compose.yml up -d

# Identity
.\mvnw.cmd -pl services/identity-service/infra -am spring-boot:run "-Dspring-boot.run.profiles=postgres"

# Catalog
.\mvnw.cmd -pl services/catalog-service/infra -am spring-boot:run "-Dspring-boot.run.profiles=postgres"

# Scheduling Clean
.\mvnw.cmd -pl services/scheduling-service-clean/infra -am spring-boot:run "-Dspring-boot.run.profiles=postgres"

# Scheduling Layered
.\mvnw.cmd -pl services/scheduling-service-layered -am spring-boot:run "-Dspring-boot.run.profiles=postgres"

# Inventory
.\mvnw.cmd -pl services/inventory-service/infra -am spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Modo desenvolvimento rápido (sem Docker): omita o profile — default continua H2 em memória.

## Experimento E2 (falhas)

1. Suba Identity e Catalog normalmente (portas 8080 / 8082).
2. Suba o Agendamento com profiles `postgres,e2` para passar pelas portas do Toxiproxy.
3. Injete toxicidade via API, por exemplo:

```powershell
# Latência de 5s no Identity
curl -X POST http://localhost:8474/proxies/identity/toxics `
  -H "Content-Type: application/json" `
  -d '{"name":"identity-latency","type":"latency","attributes":{"latency":5000}}'

# Derrubar o Catalog (timeout)
curl -X POST http://localhost:8474/proxies/catalog/toxics `
  -H "Content-Type: application/json" `
  -d '{"name":"catalog-timeout","type":"timeout","attributes":{"timeout":1000}}'
```

## Parar e limpar

```powershell
docker compose -f infra/docker-compose.yml down -v
```
