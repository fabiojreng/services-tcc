# Escopo do sistema de pesquisa

## Questão de pesquisa

Em que medida a aplicação da Clean Architecture contribui para o desacoplamento em arquiteturas de microsserviços, e quais são suas limitações quando aplicada a sistemas distribuídos?

## Objetivo geral

Avaliar o nível de desacoplamento em uma arquitetura de microsserviços estruturada com base nos princípios da Clean Architecture, por meio de um estudo de caso no domínio de gestão de laboratórios.

## Objetivos específicos (derivados do TCC)

1. Investigar os conceitos de Microsserviços, desacoplamento e Clean Architecture
2. Desenvolver um sistema de gestão de laboratórios baseado em Microsserviços
3. Aplicar os princípios da Clean Architecture na estrutura dos serviços
4. Definir critérios para avaliação do nível de desacoplamento
5. Realizar experimentos práticos e analisar os resultados, identificando benefícios e limitações

## Domínio

Sistema **LabManager** — gestão de laboratórios acadêmicos/institucionais, com:

- Controle de estoque (consumíveis e equipamentos)
- Alocação de recursos (reservas de laboratórios e equipamentos)
- Gerenciamento de permissões (usuários, papéis, autorização)

## O que está no escopo

- Quatro microsserviços com fronteiras de negócio delimitadas
- Duas variantes do serviço de **Agendamento** (Clean Architecture × estilo em camadas idiomático Spring)
- Instrumento automatizado de métricas de acoplamento (ArchUnit)
- Três experimentos controlados (E1, E2, E3) sobre as duas variantes
- Versionamento Git como instrumento de medição de propagação de mudança

## O que está fora do escopo (Fase 0 e pesquisa)

- Interface gráfica (UI) para usuários finais
- Autenticação OAuth/OIDC completa de produção
- Escalabilidade horizontal e balanceamento de carga sob carga real
- Comparação com arquitetura monolítica (não faz parte do desenho A/B escolhido)
- Heterogeneidade tecnológica entre serviços (todos em Java/Spring)
- Produção em nuvem / CI/CD avançado (pode ser adicionado, mas não é variável experimental)

## Desenho experimental resumido

| Elemento | Decisão |
|----------|---------|
| Tipo | Estudo de caso único + experimentos controlados |
| Tratamento | `scheduling-service-clean` |
| Controle | `scheduling-service-layered` |
| Unidade de comparação | Serviço de Agendamento (mesmo contrato REST) |
| Dimensões | Desacoplamento interno, entre serviços, propagação de mudança |

## Critério de sucesso da Fase 0

- Repositório Git inicializado e documentado
- Esqueleto Maven multi-módulo compilável
- Walking skeleton do Agendamento (Clean) com caso de uso **Solicitar Reserva**
- Módulo `architecture-metrics` exportando métricas
- Tag `fase-0` marcada após linha de base inicial
