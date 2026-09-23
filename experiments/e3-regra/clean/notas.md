# E3 — Clean — notas

## Intervenção

Antecedência mínima de reserva: **24h → 48h**.

Branch: `exp/e3-regra` (a partir de `baseline`).

## Diff (produção + testes do serviço)

Ver `diff-numstat.txt`.

| Arquivo | Camada | +/- |
|---------|--------|-----|
| `domain/.../ReservationPolicy.java` | **domain** | 3 / 3 |
| `domain/.../ReservationPolicyTest.java` | **domain (teste)** | 8 / 8 |

**Arquivos de produção alterados no serviço:** 1 (`ReservationPolicy`).  
**Camadas atingidas no serviço:** apenas `domain`.  
**application / presentation / infra:** 0.

## Testes

| Tipo | Antes do ajuste | Após ajuste |
|------|-----------------|-------------|
| `ReservationPolicyTest` (domínio) | Quebraria (mensagens/`aceitaReservaValida` com janela < 48h) | 7/7 OK |
| `SchedulingCleanApplicationTests` | Dependia da suíte compartilhada | OK após ajuste do acceptance |

Não houve violação ArchUnit nova (mudança local no domínio).

## Interpretação (Dimensão 3)

A regra de negócio estava encapsulada em `ReservationPolicy`. A mudança de produção concentrou-se no núcleo; o ajuste de testes ficou no mesmo módulo `domain`. Isso é consistente com a hipótese do E3 para a variante Clean.
