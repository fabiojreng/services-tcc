# E1-v2 — JPA → MongoDB com banco real (protocolo de dois commits)

Hipóteses pré-registradas: [../fase-6-preregistro.md](../fase-6-preregistro.md) (H1.1–H1.6).

## Diferença em relação ao E1 original

- Testes de integração executam contra **MongoDB real** (Testcontainers, imagem `mongo:7`, `@ServiceConnection`).
- Sem repositório em memória (Clean) nem `@MockitoBean` do repositório (Layered).
- Produção e testes medidos em commits separados.

## Branches

- `exp2/e1-infra-v2-clean` a partir de `baseline-v2`
- `exp2/e1-infra-v2-layered` a partir de `baseline-v2`

## Checklist

- [x] Commit P (produção) — Clean (P1 e P2)
- [x] `medir-experimento.ps1 -Experimento e1-infra-v2 -Variante clean -Etapa producao` (P1 e P2)
- [x] Commit T (testes) — Clean
- [x] `medir-experimento.ps1 -Experimento e1-infra-v2 -Variante clean -Etapa testes`
- [x] Idem para Layered
- [x] `clean/notas.md`, `layered/notas.md`
- [x] Comparativo com o E1 original

## Resultado

| Medida | Clean | Layered |
|--------|-------|---------|
| Produção final (P1+P2): arquivos do serviço / linhas | 8 / 28+ 71− | 6 / 11+ 50− |
| Camadas de negócio tocadas | **nenhuma** (`domain`, `application` e `presentation` com 0 linhas) | `service` (3+ 3−, só a troca de tipo) |
| Defeito revelado pelo Mongo real (P2) | UUID sem representação (1 linha em `infra`) | Idem (1 linha em properties) |
| Testes quebrados após P1 / P2 | 1 de 8 / 1 de 8 (integração) | 1 de 1 / 1 de 1 |
| Testes isolados da persistência | 7 (domínio), verdes em todas as etapas | 0 |
| Ajuste de testes (T) | 2 arquivos, 15+ 1− | 2 arquivos, 15+ 1− |
| Testes finais (Mongo real) | 8/8 | 1/1 |
| ArchUnit | Regras verdes; métricas = `baseline-v2` | Regras verdes; CCD 18→22 (**inconclusivo**: sem dependência nova no bytecode) |

| Hipótese | Situação |
|----------|----------|
| H1.1 (Clean só em `infra`) | Consistente |
| H1.2 (Layered toca `service`) | Consistente, mas a mudança foi apenas de tipo; a lógica não mudou |
| H1.3 (sem vantagem de volume para a Clean) | Consistente: a Clean mudou **mais** linhas |
| H1.4 (integração quebra; domínio da Clean verde) | Consistente |
| H1.5 (ajustes do Mongo ficam na borda) | Consistente nas duas variantes |
| H1.6 (regras da Clean verdes) | Consistente |

Achados adicionais:

1. A propriedade `spring.data.mongodb.uri`, usada no E1 original, é ignorada pelo Boot 4.
2. O `@Transactional` da Layered virou um no-op silencioso com Mongo.
3. Nas duas variantes, a quebra do teste de integração veio do **classpath de teste** (`data-jpa-test`), um acoplamento da infraestrutura de testes à tecnologia de persistência.
