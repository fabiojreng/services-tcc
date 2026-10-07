# E3-v2 — Clean — notas

Branch `exp2/e3-regra-v2-clean` (P `4473c1a`, T `d8259ae`). Execução em 2026-10-07 (quarta-feira).

## Produção (commit P)

| Arquivo | + | − | Módulo |
|---------|---|---|--------|
| `domain/policy/ReservationPolicy.java` | 3 | 3 | `domain` |

`application`, `presentation` e `infra`: 0 linhas.

## Testes quebrados após P (antes de qualquer ajuste)

3 de 8 testes, todos de unidade do domínio (`ReservationPolicyTest`):

- `aceitaReservaValida` (FAILURE): o slot de exemplo ficava a 47 h do "agora" fixo.
- `factoryCriaComStatusRequested` (ERROR): mesmo slot.
- `rejeitaAntecedenciaMenorQue24Horas` (FAILURE): a mensagem esperada citava "24 horas".

Teste de integração (suíte de aceitação): **verde**. O slot escolhido pela suíte ficou 7 dias à frente na data de execução.

## Ajuste de testes (commit T)

1 arquivo, 6+/6−: datas dos dois testes movidas de quarta para quinta-feira, e o teste de antecedência renomeado para 48 h.

## ArchUnit

13/13 regras verdes. Métricas de Martin e Lakos **idênticas** à `baseline-v2`.

## Leitura

- Os 3 testes quebrados são exatamente os que fixam a regra. A quebra é o **sinal esperado** de uma rede de segurança no nível da regra, e não custo acidental.
- Um custo acidental real foi o teste `factoryCriaComStatusRequested`, que testa outra coisa (o status inicial) mas usava um slot próximo do limiar.
