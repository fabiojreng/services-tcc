# ADR-003: Duas variantes apenas no serviço de Agendamento

## Status

Aceito

## Contexto

A questão de pesquisa exige comparação controlada (Clean Architecture × alternativa). Implementar o A/B em todos os serviços dobraria o esforço sem necessidade metodológica proporcional.

## Decisão

- Quatro serviços no sistema
- Apenas **`scheduling-service`** possui duas implementações:
  - `scheduling-service-clean` (tratamento)
  - `scheduling-service-layered` (controle)
- Ambas expõem o **mesmo contrato REST** e são intercambiáveis do ponto de vista dos consumidores
- Os demais serviços usam Clean Architecture (consistência estrutural, sem papel de controle)

## Consequências

- **Positivas:** escopo viável para TCC; serviço rico em regras + dependências de rede + persistência própria — adequado a E1, E2 e E3
- **Negativas:** conclusões sobre Clean × Layered referem-se formalmente ao Agendamento; extrapolação para o sistema deve ser cautelosa (ver ameaças à validade)
- **Alternativas rejeitadas:** A/B em todos os serviços; três variantes incluindo monolito
