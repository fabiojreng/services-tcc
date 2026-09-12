# Contextos delimitados (Bounded Contexts)

A decomposição segue capacidades de negócio (Newman, Richardson) alinhadas ao domínio de gestão de laboratórios citado no TCC.

## Visão geral

```
┌─────────────────┐     ┌─────────────────┐
│ identity        │◄────│ catalog         │
│ Usuários,       │     │ Laboratórios,   │
│ papéis,         │◄────│ equipamentos,   │
│ permissões      │     │ horários        │
└────────▲────────┘     └────────▲────────┘
         │                       │
         │              ┌────────┴────────┐
         └──────────────│ scheduling      │  ← serviço-alvo (2 variantes)
                        │ Reservas,       │
                        │ conflitos,      │
         ┌──────────────│ aprovação       │
         │              └─────────────────┘
┌────────┴────────┐
│ inventory       │
│ Consumíveis,    │
│ movimentações   │
└─────────────────┘
```

## 1. Identity & Access (`identity-service`)

| Aspecto | Definição |
|---------|-----------|
| Responsabilidade | Cadastro de usuários, papéis (aluno, professor, técnico, admin) e verificação de permissões |
| Linguagem ubíqua | Usuário, Papel, Permissão, Autorização |
| Dados próprios | Usuários, papéis, mapeamento usuário↔papel |
| Expõe | API de verificação de permissão; CRUD básico de usuários/papéis |
| Consome | Nada (raiz de autorização) |
| Variante | Clean Architecture |

## 2. Catálogo de Laboratórios (`catalog-service`)

| Aspecto | Definição |
|---------|-----------|
| Responsabilidade | Cadastro de laboratórios, equipamentos associados, capacidade e janelas de funcionamento |
| Linguagem ubíqua | Laboratório, Equipamento, Capacidade, Horário de Funcionamento, Disponibilidade |
| Dados próprios | Laboratórios, equipamentos, calendários de funcionamento |
| Expõe | Consulta de laboratório, horário de funcionamento, equipamentos |
| Consome | Identity (validação de permissão em operações administrativas) |
| Variante | Clean Architecture |

## 3. Agendamento (`scheduling-service`) — **serviço-alvo**

| Aspecto | Definição |
|---------|-----------|
| Responsabilidade | Solicitar, confirmar, cancelar e consultar reservas; regras de conflito e aprovação |
| Linguagem ubíqua | Reserva, Solicitação, Conflito, Janela, Antecedência, Duração, Status |
| Dados próprios | Reservas e seus estados |
| Expõe | API REST de reservas (contrato idêntico nas duas variantes) |
| Consome | Catalog (disponibilidade/horário); Identity (permissão) — a partir da Fase 1 |
| Variantes | `scheduling-service-clean` e `scheduling-service-layered` |

### Regras de domínio

1. Não pode haver sobreposição com reservas **confirmadas** no mesmo laboratório
2. A reserva deve estar dentro do horário de funcionamento do laboratório (obtido do Catalog)
3. Antecedência mínima de **24 horas** em relação ao início
4. Duração máxima de **4 horas**
5. Solicitante deve ter permissão `RESERVATION_REQUEST` (validada no Identity)

### APIs relevantes (Fase 1)

| Serviço | Endpoint | Uso |
|---------|----------|-----|
| Identity | `POST /api/users` | Cadastro de usuário |
| Identity | `POST /api/permissions/check` | Verificação de permissão |
| Catalog | `POST /api/laboratories` | Cadastro (exige `LABORATORY_MANAGE`) |
| Catalog | `GET /api/laboratories/{id}` | Consulta + horário de funcionamento |
| Scheduling | `POST /api/reservations` | Solicitar reserva (`laboratoryId`, `requesterId`, `start`, `end`) |

## 4. Inventário (`inventory-service`)

| Aspecto | Definição |
|---------|-----------|
| Responsabilidade | Controle de consumíveis, saldos, entradas/saídas e empréstimos de itens |
| Linguagem ubíqua | Item, Saldo, Movimentação, Empréstimo, Baixa |
| Dados próprios | Itens, movimentações, saldos |
| Expõe | Consulta de saldo, registro de movimentação |
| Consome | Identity (permissão) |
| Variante | Clean Architecture |
| Fase | Implementação na Fase 2 |

## Princípios de fronteira

- Cada serviço possui **banco próprio** (autonomia de dados)
- Comunicação apenas por **API/contrato** — sem compartilhar tabelas
- Elementos que mudam juntos permanecem no mesmo serviço (Fowler / coesão)
- Chamadas frequentes entre serviços são sinal de fronteira inadequada (revisar se aparecerem)
