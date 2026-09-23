# E3 — Alteração de regra de negócio

## Intervenção escolhida

Antecedência mínima: **24 horas → 48 horas** (exemplo do protocolo).

Aplicada às duas variantes na mesma branch `exp/e3-regra` a partir de `baseline`.

## Checklist

- [x] Escolher uma regra (antecedência 24h → 48h)
- [x] Aplicar a **mesma** mudança em Clean e Layered
- [x] Coletar diff por camada / pacote (`clean/diff-numstat.txt`, `layered/diff-numstat.txt`)
- [x] Verificar quais testes quebraram (domínio vs. integração) — ver notas
- [x] Notas em `clean/notas.md` e `layered/notas.md`

## Diff compartilhado

`acceptance/scheduling-acceptance` (suíte de paridade) também exigiu ajuste de calendário (≥ 3 dias civis) para não violar a nova antecedência. Conta para **ambas** as variantes:

Ver `acceptance-diff-numstat.txt` (2 inserções / 1 remoção).

## Comparativo resumido

| Métrica | Clean | Layered |
|---------|-------|---------|
| Arquivos de produção no serviço | 1 | 1 |
| Camada principal atingida | `domain` | `service` |
| Testes de unidade da regra atualizados | Sim (`ReservationPolicyTest`) | Não havia |
| Contágio em presentation/infra/web | Não | Não (neste caso) |

## Conclusão preliminar

Para esta intervenção pontual, a **propagação quantitativa** foi semelhante (1 arquivo de produção). A diferença qualitativa está no **locus**: Clean isolou a regra no domínio; Layered alterou a lógica embutida no serviço idiomático. A hipótese de “espalhamento maior na layered” não se materializou em número de arquivos neste E3, mas a fronteira de responsabilidade favorece a Clean para evoluções futuras da mesma política.
