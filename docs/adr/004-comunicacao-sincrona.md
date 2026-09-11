# ADR-004: Comunicação síncrona na fase inicial

## Status

Aceito

## Contexto

Microsserviços podem comunicar-se de forma síncrona (HTTP) ou assíncrona (mensageria). O experimento E2 investiga acoplamento temporal, que se manifesta claramente em chamadas síncronas.

## Decisão

Adotar **HTTP/REST síncrono** como mecanismo inicial de integração entre serviços (Agendamento → Catalog, Agendamento → Identity, etc.).

Mensageria (eventos) fica como **trabalho futuro / discussão**, não como variável oculta dos experimentos principais.

## Consequências

- **Positivas:** implementação simples na Fase 1; E2 fica metodologicamente limpo; contratos REST fáceis de versionar
- **Negativas:** maior acoplamento temporal por construção — intencional para evidenciar limitações
- **Alternativas rejeitadas:** event-driven desde o início (misturaria padrão de resiliência com o fator Clean Architecture)
