# Protocolo experimental

Estudo de caso único (Runeson e Höst, 2009) com três experimentos controlados (Wohlin et al., 2012).

## Variáveis

| Tipo | Variável |
|------|----------|
| Independente | Estilo arquitetural interno do Agendamento: Clean × Layered |
| Dependentes | Métricas das dimensões 1, 2 e 3 ([02-criterios-desacoplamento.md](02-criterios-desacoplamento.md)) |
| Controladas | Mesmo contrato REST, mesmas regras de negócio, mesma stack (Java/Spring), mesmos cenários de teste de aceitação |

## Pré-condições (a partir da Fase 3)

- Tag `baseline` congelada
- Paridade funcional das duas variantes verificada pela mesma suíte de aceitação
- Ambiente reprodutível (Docker) para E1 e E2

Na Fase 0 apenas se estabelece o instrumento e a linha de base preliminar (`fase-0`).

---

## E1 — Substituição de tecnologia de infraestrutura

**Hipótese:** na variante Clean, trocar o mecanismo de persistência altera principalmente `infra` (e eventualmente `presentation`); na variante Layered, a mudança propaga para camadas de negócio/API com maior frequência.

| Item | Detalhe |
|------|---------|
| Intervenção | Substituir PostgreSQL/JPA por MongoDB (ou outro store) no Agendamento |
| Escopo | Apenas `scheduling-service-*` |
| Medidas | Diff Git (arquivos, linhas, camadas); testes quebrados; violações ArchUnit após o ajuste |
| Procedimento | Branch a partir de `baseline`; implementar o novo adaptador; ajustar apenas o necessário; coletar métricas **antes** de qualquer limpeza estética |

**Resultado esperado (orientação, não viés):** menor propagação na variante Clean se a Regra da Dependência estiver preservada.

---

## E2 — Simulação de falhas entre serviços

**Hipótese:** a Clean Architecture **não** mitiga acoplamento temporal síncrono; ambas as variantes sofrem degradação semelhante quando Catalog ou Identity ficam indisponíveis, a menos que padrões de resiliência (timeout, circuit breaker, fallback) sejam introduzidos — e esses padrões são ortogonais à Clean Architecture.

| Item | Detalhe |
|------|---------|
| Intervenção | Indisponibilizar dependência (Catalog e/ou Identity) via Toxiproxy, WireMock ou profile de falha |
| Escopo | Caso de uso Solicitar Reserva (e correlatos) |
| Medidas | Taxa de sucesso/erro; tempo de falha; necessidade de alterar código para degradar graciosamente |
| Procedimento | Executar suíte sob falha em cada variante; registrar se a falha “vaza” para o domínio ou fica nos adaptadores |

**Resultado esperado:** evidência das **limitações** da Clean Architecture quanto ao desacoplamento entre serviços.

---

## E3 — Alteração de regra de negócio

**Hipótese:** alterar uma regra de domínio (ex.: antecedência mínima de 24h → 48h, ou duração máxima 4h → 2h) deve concentrar mudanças no núcleo na variante Clean; na Layered, a regra pode estar espalhada em services/controllers/entidades JPA.

| Item | Detalhe |
|------|---------|
| Intervenção | Mudança única e documentada de regra de negócio no Agendamento |
| Escopo | Mesma regra aplicada às duas variantes |
| Medidas | Diff Git; camadas atingidas; testes de domínio vs. testes de integração quebrados |

**Resultado esperado:** na Clean, alteração predominantemente em `domain` (e testes de domínio); na Layered, possível espalhamento.

---

## Registro de execução

Para cada par (experimento × variante), criar em `experiments/`:

```
experiments/e1-infra/
  protocolo.md          # cópia operacional / checklist
  clean/
    diff-numstat.txt
    metricas.csv
    notas.md
  layered/
    diff-numstat.txt
    metricas.csv
    notas.md
```

## Ordem sugerida de execução

1. Congelar `baseline` (Fase 3)
2. E3 (menor dependência de infra distribuída)
3. E1 (exige segundo mecanismo de persistência)
4. E2 (exige orquestração de falhas)

A ordem pode ser ajustada; o importante é não contaminar branches entre experimentos.
