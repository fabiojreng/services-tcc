# LabManager — artefato experimental do TCC

Repositório de suporte ao Trabalho de Conclusão de Curso:

> **Uma avaliação do papel da Clean Architecture no desacoplamento de microsserviços e seus limites frente ao monolito distribuído: um estudo de caso em gestão de laboratórios.**

Autor: Fábio da Silva Eloi Júnior  
Instituição: IFMA — Campus Santa Inês, Bacharelado em Engenharia da Computação  
Orientador: Prof. Dr. Ernesto Franklin Marçal Ferreira

**Versão de entrega:** tag [`entrega-tcc`](https://github.com/fabiojreng/services-tcc/tree/entrega-tcc). Os resultados citados na monografia estão em
[`experiments/fase-6-sintese.md`](experiments/fase-6-sintese.md), que inclui o adendo do E4.

## Objetivo

Não é um produto comercial. É o **artefato experimental** da pesquisa: um sistema de gestão de laboratórios com
quatro microsserviços, no qual o serviço de Agendamento foi implementado em duas variantes, **Clean Architecture**
(tratamento) e **arquitetura em camadas** convencional do Spring (controle), com o mesmo contrato REST e as mesmas
regras. Sobre as duas variantes foram aplicadas intervenções controladas, medindo o desacoplamento interno ao
serviço e o desacoplamento entre serviços.

Questão de pesquisa:

> Em que medida a aplicação da Clean Architecture contribui para o desacoplamento em arquiteturas de microsserviços, e quais são suas limitações quando aplicada a sistemas distribuídos?

## Resultados em resumo

| Experimento | Intervenção | Clean | Layered |
|-------------|-------------|-------|---------|
| E1-v2 | PostgreSQL/JPA → MongoDB 7 real | Mudança só em `infra`; 8 arquivos, 28+/71− | `service` tocado só por troca de tipo; 6 arquivos, 11+/50− |
| E2-v2 | Falhas de rede reais (Toxiproxy), sem e com timeout | Indistinguível da Layered (p50 com diferença ≤ 1 ms) | Indistinguível da Clean |
| E3-v2 | Antecedência mínima 24 h → 48 h | 1 arquivo; 3 de 8 testes detectam | 1 arquivo; 0 de 1 detecta |
| E3b | Limite semanal de reservas (regra com dado novo) | 5 arquivos, 71 linhas | 2 arquivos, 19 linhas |
| E4 | Catalog renomeia um campo consumido | 1 arquivo, 1 linha no consumidor; nenhum teste detecta; implantação coordenada | Idem |

Síntese: a Clean Architecture **conteve** a troca de infraestrutura e tornou as regras **reconhecíveis e
testáveis**, com **mais custo** para modificar regras que exigem novos dados. Ela **não alterou** o comportamento
diante de falhas de rede nem diante de mudanças de contrato de outro serviço. O caso é único, com um executor:
a generalização é analítica, não estatística.

## Onde está cada parte da monografia

| Monografia | Repositório |
|------------|-------------|
| 3.3 Sistema e variantes | [`services/`](services/), [`docs/01-contextos-delimitados.md`](docs/01-contextos-delimitados.md), [`docs/adr/`](docs/adr/) |
| 3.3.4 Linha de base e instrumentação | Tag `baseline-v2`, [`experiments/baseline-v2/metricas.csv`](experiments/baseline-v2/metricas.csv), [`experiments/scripts/medir-experimento.ps1`](experiments/scripts/medir-experimento.ps1), [`architecture-metrics/`](architecture-metrics/) |
| 3.4.1 Dimensões e critérios | [`docs/02-criterios-desacoplamento.md`](docs/02-criterios-desacoplamento.md) |
| 3.4.5 Registro prévio das hipóteses | [`experiments/fase-6-preregistro.md`](experiments/fase-6-preregistro.md) (E1–E3) e [`experiments/e4-preregistro.md`](experiments/e4-preregistro.md) (E4) |
| 3.4.7 Rodada exploratória | Branches `exp/*`, [`experiments/fase-5-sintese.md`](experiments/fase-5-sintese.md), [`experiments/evidencias-capitulo-4.md`](experiments/evidencias-capitulo-4.md) |
| 4.2 Experimento 1 | [`experiments/e1-infra-v2/`](experiments/e1-infra-v2/) |
| 4.3 Experimento 2 | [`experiments/e2-falhas-v2/`](experiments/e2-falhas-v2/) (CSVs por requisição e harness `run-e2.ps1`) |
| 4.4 Experimento 3 | [`experiments/e3-regra-v2/`](experiments/e3-regra-v2/), [`experiments/e3b-limite-semanal/`](experiments/e3b-limite-semanal/), [`experiments/e3-localizacao-vs-modificacao.md`](experiments/e3-localizacao-vs-modificacao.md) |
| 4.5 Experimento 4 | [`experiments/e4-contrato/`](experiments/e4-contrato/) (protocolo, execução real e harness `run-e4.ps1`), [`experiments/e4-controle/`](experiments/e4-controle/) |
| 4.6 Síntese | [`experiments/fase-6-sintese.md`](experiments/fase-6-sintese.md) |
| 4.8 Limitações e ameaças | [`docs/04-ameacas-validade.md`](docs/04-ameacas-validade.md), [`docs/05-fora-de-escopo.md`](docs/05-fora-de-escopo.md) |

Cada pasta de experimento tem `protocolo.md` e, por variante, `diff-producao.txt`, `testes-quebrados.txt`,
`diff-testes.txt`, `testes-final.txt`, `archunit.txt`, `metricas.csv` e `notas.md`.

## Tags e branches

| Referência | Conteúdo |
|------------|----------|
| `fase-0`, `fase-1`, `fase-2` | Estrutura Maven, serviços e variantes |
| `baseline` | Linha de base da rodada exploratória (Fase 3) |
| `baseline-v2` | Linha de base da rodada que fundamenta os resultados (Fase 6) |
| `entrega-tcc` | Versão citada na monografia |
| `exp/*` | Rodada exploratória (Fase 4); usada só como lição metodológica |
| `exp2/<experimento>-<variante>` | Rodada final: commits P (produção), T (testes) e artefatos, a partir de `baseline-v2` |
| `exp2/e4-contrato-clean`, `exp2/e4-contrato-layered`, `exp2/e4-controle` | E4: P1 (provedor), P2 (consumidor), T e artefatos |

O código alterado por cada intervenção vive nas branches; a `main` guarda a linha de base, a documentação e os
artefatos de medição.

## Pilha

| Item | Escolha |
|------|---------|
| Linguagem | Java 25 LTS |
| Framework | Spring Boot 4.1.1 |
| Build | Maven (wrapper incluso) |
| Persistência padrão (testes, E2 e E4) | H2 em memória |
| Persistência de origem do E1 | PostgreSQL 16 via Docker Compose (perfil `postgres`) |
| Persistência de destino do E1 | MongoDB 7 via Testcontainers (nas branches `exp2/e1-infra-v2-*`) |
| Falhas de rede (E2) | Toxiproxy 2.12.0 via Docker Compose (perfil `e2`) |
| Regras e métricas arquiteturais | ArchUnit 1.4.1 (módulo `architecture-metrics`) |

## Serviços

| Serviço | Papel | Organização |
|---------|-------|-------------|
| `identity-service` | Usuários, papéis e permissões | Clean Architecture |
| `catalog-service` | Laboratórios, capacidade e horários | Clean Architecture |
| `scheduling-service-clean` / `scheduling-service-layered` | Reservas, conflitos e regras (**serviço-alvo**) | Clean (tratamento) e Layered (controle) |
| `inventory-service` | Consumíveis e movimentações | Clean Architecture (fora do caso de uso analisado) |

O caso de uso analisado é *Solicitar Reserva* (`POST /api/reservations`), que consulta Identity e Catalog de
forma síncrona (fan-out de 2).

```
docs/                     Escopo, critérios, protocolo, ameaças, ADRs
services/                 Microsserviços
acceptance/               Suíte de aceitação compartilhada pelas duas variantes do Agendamento
architecture-metrics/     Regras e métricas ArchUnit
experiments/              Pré-registros, protocolos, artefatos e sínteses
infra/                    Docker Compose (PostgreSQL + Toxiproxy)
scripts/                  Subir e parar os serviços localmente
web-console/              Console web para operar os serviços (não participa dos experimentos)
```

## Fases

| Fase | Conteúdo |
|------|----------|
| 0–2 | Estrutura Maven, serviços, comunicação REST e as duas variantes do Agendamento |
| 3 | Docker, PostgreSQL, Toxiproxy e tag `baseline` |
| 4–5 | Rodada exploratória (E1, E2, E3) e sua avaliação crítica |
| 6 | Rodada final: pré-registro, protocolo de dois commits, MongoDB real, Toxiproxy real, E3b; adendo E4 (23/10/2026) com pré-registro próprio |

Detalhes em [`docs/roadmap-fases.md`](docs/roadmap-fases.md).

## Como construir

Pré-requisito: **JDK 25**. Maven não precisa estar instalado.

```powershell
.\mvnw.cmd clean verify
```

## Como reproduzir um experimento

Cada intervenção segue o protocolo de dois commits (seção 3.4.2 da monografia), automatizado por
`experiments/scripts/medir-experimento.ps1`:

```powershell
git log --oneline baseline-v2..exp2/e3-regra-v2-clean   # commits [P], [T] e [A] (artefatos)
git switch --detach 4473c1a                             # E3-v2 Clean [P]: somente produção
.\experiments\scripts\medir-experimento.ps1 -Experimento e3-regra-v2 -Variante clean -Etapa producao
```

O script grava os resultados em `experiments/<experimento>/<variante>/`, para comparação com os artefatos
versionados no commit [A] da mesma branch. Harnesses de execução real:

- E2: [`experiments/e2-falhas-v2/run-e2.ps1`](experiments/e2-falhas-v2/run-e2.ps1) (requer Docker para o Toxiproxy).
- E4: [`experiments/e4-contrato/construir-artefatos.ps1`](experiments/e4-contrato/construir-artefatos.ps1) e
  [`experiments/e4-contrato/run-e4.ps1`](experiments/e4-contrato/run-e4.ps1) (H2, sem Docker).

## Como subir os serviços

Modo rápido (H2, sem Docker):

```powershell
.\scripts\subir-servicos.ps1          # cinco serviços e o console web, em segundo plano (logs em logs/)
.\scripts\subir-servicos.ps1 -Parar
```

Ou um a um:

```powershell
.\mvnw.cmd -pl services/identity-service/infra -am spring-boot:run
.\mvnw.cmd -pl services/catalog-service/infra -am spring-boot:run
.\mvnw.cmd -pl services/scheduling-service-clean/infra -am spring-boot:run
.\mvnw.cmd -pl services/scheduling-service-layered -am spring-boot:run
.\mvnw.cmd -pl services/inventory-service/infra -am spring-boot:run
```

Com PostgreSQL, suba a infraestrutura (`docker compose -f infra/docker-compose.yml up -d`) e acrescente
`"-Dspring-boot.run.profiles=postgres"`, ou use `.\scripts\subir-servicos.ps1 -Perfil postgres`. No PowerShell,
cite `-D...` entre aspas. Detalhes em [`infra/README.md`](infra/README.md).

Portas: identity `8080`, scheduling-clean `8081`, catalog `8082`, scheduling-layered `8083`, inventory `8084`,
console web `8090`.

O console web ([http://localhost:8090](http://localhost:8090)) mostra o status de cada serviço, o diagrama de
dependências e a comparação lado a lado das variantes do Agendamento. Ele fica fora de `services/` e não entra nas
métricas nem nos diffs dos experimentos.

## Licença

Uso acadêmico — IFMA / TCC Engenharia da Computação.
