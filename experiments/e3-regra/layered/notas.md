# E3 — Layered — notas

## Intervenção

Antecedência mínima de reserva: **24h → 48h** (mesma regra da variante Clean).

Branch: `exp/e3-regra` (a partir de `baseline`).

## Diff (produção do serviço)

Ver `diff-numstat.txt`.

| Arquivo | Camada idiomática | +/- |
|---------|-------------------|-----|
| `layered/service/ReservationService.java` | **service** (negócio + HTTP + orquestração JPA) | 2 / 2 |

**Arquivos de produção alterados no serviço:** 1 (`ReservationService`).  
**web / entity / repository / client:** 0.

Não há módulo `domain` separado; a constante e a mensagem vivem no `@Service` que também chama Identity/Catalog e persiste a entidade JPA.

## Testes

| Tipo | Observação |
|------|------------|
| Teste unitário da regra | **Inexistente** na variante layered — nada quebrou localmente além do que a suíte compartilhada cobre |
| `SchedulingLayeredApplicationTests` | OK após ajuste do `scheduling-acceptance` |

## Interpretação (Dimensão 3)

O tamanho do diff de produção foi similar ao Clean (1 arquivo). Porém:

1. O ponto de mudança **não** é um núcleo de domínio isolado — é a camada de serviço acoplada a frameworks e clientes HTTP.
2. A ausência de testes de unidade da regra reduz o “sinal” de quebra local; a paridade depende do módulo de aceitação compartilhado.
3. Em uma evolução futura da mesma regra (ex.: extrair política, mensagens i18n, validação em outro service), o risco de espalhamento na layered permanece maior porque não há fronteira explícita de domínio.
