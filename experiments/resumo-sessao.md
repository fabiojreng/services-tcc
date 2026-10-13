# Resumo da sessão — evidências para o Capítulo 4

Data desta sessão: **13/10/2026**. Branch de trabalho: `exp/e2-toxi-replica`, criada a partir de
`675201a` (`main`). Nada commitado nesta sessão. Nenhuma tag (`baseline`, `baseline-v2`) e nenhuma
branch `exp/e1-infra`, `exp/e2-falhas`, `exp/e3-regra` ou `exp2/*` foi reescrita.

Os arquivos da Fase A e o material de revisão cega já estavam em `675201a` (sessão de 12/10).
Hoje eles foram **reconferidos** contra o git e contra os logs, não reescritos por cima. O que é
novo é a execução do Toxiproxy e um ajuste no texto entregue ao revisor.

Ambiente de 13/10: Windows 10.0.26300, Java 25.0.1, Docker Desktop 4.91.0 / Engine 29.8.0
(o daemon respondeu; em 12/10 não respondia).

---

## O que foi concluído

### A1 — Evidências do Capítulo 4 — já estava feito; conferido de novo

Arquivo: [`evidencias-capitulo-4.md`](evidencias-capitulo-4.md).
Logs: [`evidencias-capitulo-4/execucao-2026-10-12/`](evidencias-capitulo-4/execucao-2026-10-12/).

Em 13/10 reexecutei `git diff --numstat baseline..exp/e1-infra`, `..exp/e2-falhas` e `..exp/e3-regra`.
A saída é a mesma que está nas seções 1.3, 2.3 e 3.3 (incluindo `infra/docker-compose.yml` 10/0 no E1,
e zero linhas fora de teste no E2). Os `summary.txt` de 12/10 batem com as tabelas de testes
(baseline 8/8 + 1/1; E1 estado P com `NoSuchBeanDefinitionException` na Clean e timeout Mongo na
Layered; E3 estado P com as três quebras de domínio e os dois 422). As linhas citadas de
`SchedulingConfig` (E1, `@ConditionalOnBean` nas linhas 29–33 da branch) e de `ReservationPolicy` /
`ReservationService` (E3) foram relidas com número de linha no git. Conferem.

A suíte **não** foi reexecutada em 13/10. Os números de teste do Capítulo 4 continuam sendo os de
12/10, com log versionado. Não há número novo de teste Maven nesta data.

### A2 — Fora de escopo — já estava feito; não precisou de mudança

[`docs/05-fora-de-escopo.md`](../docs/05-fora-de-escopo.md) cobre os cinco itens pedidos (erosão em
várias iterações, quebra de contrato, serviço novo, carga, comunicação assíncrona), cada um com
motivo e se é trabalho futuro.

### A3 — Localização versus modificação no E3 — já estava feito; saltos conferidos

[`e3-localizacao-vs-modificacao.md`](e3-localizacao-vs-modificacao.md). Na `baseline`, a cadeia
bate com o texto: Clean, 3 saltos inter-arquivo (`ReservationController.request` →
`RequestReservationUseCase.execute` → `ReservationFactory.createRequest` →
`ReservationPolicy.validate` / campo na linha 25). Layered, 1 salto inter-arquivo
(`ReservationController.request` linhas 21–30 → `ReservationService.requestReservation` linha 42,
`validateRules` linha 69, uso do campo na linha 84). Arquivos de produção no E3 continuam 1 e 1.

### B2 — Toxiproxy — **executado em 13/10**

[`e2-replicacao-toxiproxy.md`](e2-replicacao-toxiproxy.md).
CSV: [`e2-replicacao-toxiproxy/execucao-2026-10-13/resumo-fase-A.csv`](e2-replicacao-toxiproxy/execucao-2026-10-13/resumo-fase-A.csv).

Fase A do harness (sem timeout de cliente), N=30 e S4 com N=10, exit code 0, ~26 min.
Código idêntico a `baseline-v2` em `services/` e `infra/` (`git diff --stat` vazio).
Os CSVs de `e2-falhas-v2/` não foram sobrescritos.

| Cenário | Clean | Layered |
|---------|-------|---------|
| S0 | 100 % 201, p50 18 ms | 100 % 201, p50 18 ms |
| S1 +500 ms | 100 % 201, p50 516 ms | 100 % 201, p50 517 ms |
| S2 +5000 ms | 100 % 201, p50 5017 ms | 100 % 201, p50 5017 ms |
| S3 proxy identity off | 100 % 503, p50 4 ms | 100 % 503, p50 4 ms |
| S4 conexão travada | 100 % timeout do cliente, p50 60013 ms, n=10 | 100 % timeout do cliente, p50 60007 ms, n=10 |

O padrão de status é o mesmo da fase A de 07/10. O p50 difere em 0–3 ms entre as datas.
A fase B (timeout 1 s/2 s) **não** foi reexecutada hoje; os números de 07/10 ficam como estão.

### B3 — Revisão cega — material já existia; o texto ao revisor foi enxugado

- [`revisao-cega-e3-variante-a.md`](revisao-cega-e3-variante-a.md) — Layered
- [`revisao-cega-e3-variante-b.md`](revisao-cega-e3-variante-b.md) — Clean
- [`revisao-cega-gabarito.md`](revisao-cega-gabarito.md)

Saíram do texto ao revisor as frases que diziam “repositório JPA” e “não há cliente HTTP”.
Os nomes `ReservationService` e `ReservationPolicy` **permanecem**. Quem já leu o código identifica
a variante por eles. Não houve revisor. Isto é instrumento, não dado.

---

## O que falhou, ficou inconclusivo ou não foi feito — e por quê

### B1 — Variante Layered do Catalog e réplica do E1 — **não feito**

Não improvisei. A ADR-003 (`docs/adr/003-variantes-apenas-agendamento.md`) está aceita: as duas
variantes existem só no Agendamento. Além disso, o Catalog persiste dias da semana com JPA
(`@ElementCollection`); o documento Mongo equivalente é decisão de desenho, e não há suíte de
aceitação compartilhada do Catalog análoga a `scheduling-acceptance`. Isso é um mini-projeto com
ADR nova, não um apêndice. A validade externa continua N=1 por célula.

### Fase B do E2-v2 — não reexecutada

A tarefa pedia falha de rede real contra o E2 de stubs. A fase A é esse contraste (produção sem
timeout, igual à linha de base). A fase B é outra intervenção (timeout no `RestClient`), já medida
em 07/10. Repeti-la hoje exigiria build a partir de `exp2/e2-falhas-v2-*`. Não foi feita.

### Corpo HTTP da carga — o harness não grava

Os logs dos quatro processos na carga só têm a subida do Spring. O corpo do 503 foi obtido em
sondas com **N = 1**, não nas 30 requisições do S3. A latência dessas sondas (149–173 ms) não é o
p50 de 4 ms da carga: eram a primeira requisição depois da subida.

### E1 Clean da Fase 4 e o calendário do E3 — achados de 12/10, não reabertos hoje

Continuam valendo, com a ressalva de que os testes não rodaram de novo em 13/10:

- Na branch `exp/e1-infra`, a Clean com só o diff de produção não sobe
  (`@ConditionalOnBean` em `SchedulingConfig`). A suíte da ponta da branch passa porque exclui o
  Mongo. Docker em 12/10 estava parado, então o jar **com** Mongo não foi testado. O E1-v2 segue
  sendo a evidência de persistência que sobe de verdade.
- O “0 testes quebrados” da Layered no E3-v2 (07/10, quarta-feira) virou 1/1 em 12/10 (segunda-feira,
  aceitação 422). A Layered não tem detecção determinística da regra; a Clean tem 3, com relógio fixo.

---

## Descobertas desta sessão (13/10)

1. **A réplica de rede repete a fase A.** Status idênticos aos de 07/10; p50 dentro de poucos
   milissegundos. Não apareceu divergência entre Clean e Layered.
2. **O stub não reproduz o texto da falha real.** O teste escreve `connection refused`. Com o proxy
   do Identity desabilitado, as duas variantes devolveram 503 com
   `HTTP/1.1 header parser received no bytes` (N=1). Com o Toxiproxy parado e a porta 18080 sem
   processo, as duas devolveram 503 com o sufixo `null` (N=1). A frase do stub não apareceu.
3. **Latência no Catalog continua sendo sucesso.** S2, 5 s, 100 % 201 nas duas, p50 5017 ms.
   Conexão travada continua sendo silêncio de ~60 s, não 503. Isso só existe porque a fase A não
   tem timeout de cliente.

---

## Como isso se encaixa no Capítulo 4

| Seção | Usar |
|-------|------|
| Resultados E1 | [`evidencias-capitulo-4.md`](evidencias-capitulo-4.md) §1 e §4.1, com a ressalva de que a Clean da Fase 4 não sobe. Números vigentes da troca que funciona: E1-v2 em [`fase-6-sintese.md`](fase-6-sintese.md) |
| Resultados E2 | Stub = mapeamento para 503. Rede = [`e2-replicacao-toxiproxy.md`](e2-replicacao-toxiproxy.md), carga de 13/10 (fase A) mais a fase B de 07/10. Não citar `connection refused` como texto observado na rede |
| Resultados E3 | Empate de 1 arquivo + saltos em [`e3-localizacao-vs-modificacao.md`](e3-localizacao-vs-modificacao.md) + quebra dependente de calendário (§4.2 das evidências, medida em 12/10) |
| Limitações | [`docs/05-fora-de-escopo.md`](../docs/05-fora-de-escopo.md). N=1 por célula: B1 não foi feito |
| Revisão cega | Instrumento pronto, com a limitação dos nomes de classe. Sem avaliador |

Nada disto muda a frase-síntese da Fase 6. Refina o que a banca pode citar sem ressalva: a fase A
de rede agora tem uma segunda execução, no mesmo desenho, com o mesmo desfecho.
