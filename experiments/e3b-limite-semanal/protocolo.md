# E3b — Limite semanal de reservas por usuário (protocolo de dois commits)

Hipóteses pré-registradas: [../fase-6-preregistro.md](../fase-6-preregistro.md) (Hb.1–Hb.6).

## Regra

No máximo **3** reservas `REQUESTED` ou `CONFIRMED` por `requesterId` com início na mesma
semana ISO (segunda a domingo) do slot solicitado. Violação → 422.

## Branches

- `exp2/e3b-limite-semanal-clean` a partir de `baseline-v2`
- `exp2/e3b-limite-semanal-layered` a partir de `baseline-v2`

## Checklist

- [x] Commit P (produção) — Clean
- [x] `medir-experimento.ps1 -Experimento e3b-limite-semanal -Variante clean -Etapa producao`
- [x] Commit T (testes) — Clean
- [x] `medir-experimento.ps1 -Experimento e3b-limite-semanal -Variante clean -Etapa testes`
- [x] Idem para Layered
- [x] `clean/notas.md`, `layered/notas.md`
- [x] Comparativo e confronto com as hipóteses

## Resultado

| Medida | Clean | Layered |
|--------|-------|---------|
| Produção (P): arquivos / linhas | **5 / 71+ 0−** | **2 / 18+ 1−** |
| Módulos/pacotes atingidos | `domain` (2), `application` (1), `infra` (2) | `repository` (1), `service` (1) |
| Testes quebrados após P | 0 de 8 | 0 de 1 |
| Ajuste de testes (T): arquivos / linhas | 3 / 92+ (inclui 4 testes de unidade puros) | 2 / 29+ (só integração) |
| Testes finais | 13/13 | 2/2 |
| ArchUnit | Regras verdes; `domain.policy` Ca 1→3, I 0,75→0,50; sem dependência nova saindo do domínio | Regras verdes; métricas = `baseline-v2` |

| Hipótese | Situação |
|----------|----------|
| Hb.1 (Clean altera mais arquivos) | Consistente: 5 × 2 arquivos; 71 × 19 linhas |
| Hb.2 (invariante só em `domain`) | Consistente |
| Hb.3 (regra e consulta juntas na Layered) | Consistente |
| Hb.4 (0 quebrados, na ausência de dublês da porta) | Consistente |
| Hb.5 (teste de unidade só na Clean) | Parcialmente: a Layered *poderia* ter teste com Mockito (3 mocks), mas a prática idiomática de referência não o tinha |
| Hb.6 (sem nova dependência saindo do domínio) | Consistente |

Leitura: quando a regra exige **novo acesso a dados**, a Clean custa mais para modificar (2,5× os arquivos e 3,7× as linhas). Em troca, isola a regra e a torna testável sem infraestrutura. Esse custo é um *trade-off* real da Clean Architecture, e não uma falha da implementação.
