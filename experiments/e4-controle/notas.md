# E4-controle — renomear um campo não consumido

Hipóteses pré-registradas: [../e4-preregistro.md](../e4-preregistro.md) (H4.C1, H4.C2). Protocolo e leitura
conjunta com o E4 principal: [../e4-contrato/protocolo.md](../e4-contrato/protocolo.md).

Branch `exp2/e4-controle` (P `e065acd`, T `f06de89`, artefatos `b0d391e`), a partir de `baseline-v2`.
Como o Agendamento não muda, a mesma branch foi medida com as duas variantes (`clean/` e `layered/`).

## Intervenção

`capacity` → `seats` no contrato REST do Catalog (request, response e a referência no controller).

## Resultado

| Medida | Clean | Layered |
|--------|-------|---------|
| Produção (P): arquivos / linhas | 3 / 3+ 3−, só `catalog-service/presentation` | idem |
| Linhas no Agendamento | **0** | **0** |
| Testes do Agendamento quebrados após P | 0 de 8 | 0 de 1 |
| Testes do Catalog quebrados após P | 1 de 1 (`FAILURE`: o POST com `capacity` chega com `seats` = 0 e o Catalog responde 400) | idem |
| Ajuste de testes (T) | 1 arquivo do Catalog, 2+ 2− | idem |
| Testes finais | 9/9 | 2/2 |
| Execução real (RC: Catalog controle, Agendamento `baseline-v2`) | 201 × 10 | 201 × 10 |
| ArchUnit | Regras verdes | Regras verdes |

## Leitura

As duas variantes leem a resposta do Catalog como `Map` e só acessam as chaves de que precisam
(`opensAt`, `closesAt`, `openDays`): 3 de 6 campos. Mudanças nos outros 3 não se propagam. Esse comportamento de
*tolerant reader* vem da forma de desserializar, que é igual nas duas variantes, e não do estilo arquitetural.
Se qualquer uma delas desserializasse para um DTO estrito (falhando em campo desconhecido ou ausente), o controle
teria propagado nas duas.
