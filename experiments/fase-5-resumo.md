# Resultados — guia rápido para o monográfico

## Resposta curta

Os resultados são consistentes com as hipóteses a seguir. A Clean Architecture **ajudou** no desacoplamento **interno** e na **contenção** de mudanças de infraestrutura (E1), mas não reduziu o volume alterado. **Não ajudou** contra falhas síncronas entre serviços (E2). Em mudança de regra simples (E3), o ganho foi **qualitativo** (locus no domínio e detecção por testes), não quantitativo (mesmo nº de arquivos).

A Fase 6 acrescenta uma distinção. A Clean facilita **localizar** e **testar** uma regra. Quando a regra exige novo acesso a dados, porém, ela custa mais para **modificar** (E3b: 5 contra 2 arquivos).

## Onde está o texto completo

→ [`fase-6-sintese.md`](fase-6-sintese.md): versão vigente, com replicação controlada.
→ [`fase-5-sintese.md`](fase-5-sintese.md): primeira rodada, com as correções marcadas.

## Tabelas prontas para colar

- Propagação (Fase 5): [`propagacao-e1-e3.csv`](propagacao-e1-e3.csv)
- Propagação e falhas (Fase 6): tabelas em [`fase-6-sintese.md`](fase-6-sintese.md)
- ArchUnit baseline: [`baseline-metrics.csv`](baseline-metrics.csv) e [`baseline-v2/metricas.csv`](baseline-v2/metricas.csv)
