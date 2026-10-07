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

- [ ] Commit P (produção) — Clean
- [ ] `medir-experimento.ps1 -Experimento e1-infra-v2 -Variante clean -Etapa producao`
- [ ] Commit T (testes) — Clean
- [ ] `medir-experimento.ps1 -Experimento e1-infra-v2 -Variante clean -Etapa testes`
- [ ] Idem para Layered
- [ ] `clean/notas.md`, `layered/notas.md`
- [ ] Comparativo com o E1 original

## Resultado

(preenchido após a execução)
