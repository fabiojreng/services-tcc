# Resumo da sessão — evidências para o Capítulo 4

Data: **12/10/2026**. Branch de trabalho: `exp/consolidacao-cap4` (mesmo commit que `main`:
`782c1de`). Nenhuma tag (`baseline`, `baseline-v2`) e nenhuma branch `exp/e1-infra`,
`exp/e2-falhas`, `exp/e3-regra` foi reescrita.

Ambiente: Windows 10.0.26300, Java 25, Maven Wrapper. Docker Desktop **sem daemon**
(`npipe:////./pipe/dockerDesktopLinuxEngine` inexistente) — bloqueia Mongo ao vivo e Toxiproxy.

---

## O que foi concluído

### A1 — Evidências do Capítulo 4 — **concluído**

Arquivo: [`evidencias-capitulo-4.md`](evidencias-capitulo-4.md).
Saída bruta: [`evidencias-capitulo-4/execucao-2026-10-12/`](evidencias-capitulo-4/execucao-2026-10-12/).

Suíte executada de fato (não copiada das notas da Fase 4), com worktrees destacadas:

| Estado | Ref | Resultado Agendamento |
|--------|-----|------------------------|
| B (`baseline`) | `5304b47` | 8/8 Clean + 1/1 Layered, 0 falhas (11:40) |
| T E1 | `exp/e1-infra` `a22b8e5` | verde (stubs; Mongo excluído da autoconfiguração) |
| P E1 | `baseline` + só produção E1 | Clean: contexto não sobe (`NoSuchBeanDefinitionException` de `ReservationRepository`); Layered: timeout em `localhost:27017` |
| T E2 | `exp/e2-falhas` `319af0e` | verde; 503 nos quatro testes de simulação |
| T E3 | `exp/e3-regra` `26f259e` | verde |
| P E3 | `baseline` + só produção E3 | Clean 4/8 quebrados; Layered 1/1 quebrado (422 em vez de 201) |

`git diff --numstat baseline..exp/eN-*` reexecutado nesta sessão; confere com o documento.

### A2 — Fora de escopo — **concluído**

[`docs/05-fora-de-escopo.md`](../docs/05-fora-de-escopo.md): erosão em múltiplas iterações, quebra
de contrato, novo serviço em sistema maduro, carga, comunicação assíncrona — cada um com motivo e
se é trabalho futuro.

### A3 — Localização vs. modificação no E3 — **concluído**

[`e3-localizacao-vs-modificacao.md`](e3-localizacao-vs-modificacao.md). Contagem reproduzível a
partir do REST:

| | Clean | Layered |
|--|-------|---------|
| Saltos inter-arquivo até `MINIMUM_LEAD_TIME` | 3 | 1 |
| Arquivos de produção alterados no E3 | 1 | 1 |

O empate do diff **não** é empate de localização. Na Clean o *use case* nem nomeia a política
(hop extra pela factory). Na Layered a constante vive no mesmo arquivo que HTTP e JPA.

### B3 — Material para revisão cega — **concluído (material, não resultado)**

- [`revisao-cega-e3-variante-a.md`](revisao-cega-e3-variante-a.md) — Layered, anonimizado
- [`revisao-cega-e3-variante-b.md`](revisao-cega-e3-variante-b.md) — Clean, anonimizado
- [`revisao-cega-gabarito.md`](revisao-cega-gabarito.md) — não distribuir com as variantes

Não houve revisor terceiro. O commit `782c1de` já havia retirado um protocolo de revisão cega e
declarado a ausência como limitação. Estes arquivos só preparam o instrumento, caso a banca ou o
orientador queiram usá-lo.

### B2 — documento de comparação stub × Toxiproxy — **parcial**

[`e2-replicacao-toxiproxy.md`](e2-replicacao-toxiproxy.md) compara os stubs da Fase 4 (reexecutados
hoje) com os CSVs reais da Fase 6 (07/10/2026). **Esta sessão não repetiu o Toxiproxy** (Docker
indisponível). Os números de latência/taxa da rede não foram gerados hoje.

---

## O que falhou, ficou inconclusivo ou não foi feito — e por quê

### B1 — Réplica do protocolo no Catalog — **não feito (decisão de design)**

Criar `catalog-service-layered` competente, com paridade REST, e repetir E1 (JPA→Mongo) **não foi
improvisado**. Motivos, todos já no repositório:

1. **ADR-003** (`docs/adr/003-variantes-apenas-agendamento.md`): as duas variantes existem *apenas*
   no Agendamento. Uma variante Layered do Catalog contradiz uma decisão aceita.
2. O Catalog tem persistência JPA com `@ElementCollection` de dias da semana
   (`LaboratoryJpaEntity`) — o mapeamento Mongo equivalente não é mecânico; escolher documento
   embutido vs. coleção paralela é decisão de desenho.
3. Não há suíte de aceitação compartilhada do Catalog análoga a `scheduling-acceptance`. Sem ela, a
   “paridade funcional” da nova variante não tem o mesmo instrumento.
4. ArchUnit, POM do reator e métricas teriam de ganhar um segundo alvo. Isso altera a linha de
   base, não só um experimento.

Para o orientador: B1 é o caminho honesto para N=2, mas é um mini-projeto com ADR nova, não um
apêndice desta sessão. A validade externa continua N=1 por célula, já declarada em
`docs/04-ameacas-validade.md`.

### B2 — Reexecução Toxiproxy — **não reexecutado**

Docker parado. Reexecução honesta é impossível. A evidência de rede **já existe** na Fase 6
(`e2-falhas-v2/resumo-fase-{A,B}.csv`). O documento B2 desta sessão só confronta stub × essa
evidência prévia.

### E1 Clean da Fase 4 — intervenção de produção **não sobe**

Achado da consolidação (estado P + jar, sem Mongo). Causa: `@ConditionalOnBean(SpringDataReservationRepository.class)`
em `SchedulingConfig` (branch `exp/e1-infra`), avaliado antes da autoconfiguração registrar o
repositório. A suíte da ponta da branch passa porque **exclui** Mongo e injeta stub.

**Não verificado:** o mesmo binário *com* MongoDB no ar (Docker indisponível). A falha é de
definição de beans, anterior a qualquer conexão; não há motivo forte para esperar outro resultado,
mas o teste com banco **não foi feito**. O E1-v2 (Fase 6, Testcontainers) não tem esse defeito e
deve ser a evidência principal de troca de persistência no Capítulo 4.

### E3 Layered “0 testes quebrados” — **depende do calendário**

Reexecução dos commits P do E3-v2 no mesmo dia (segunda-feira 12/10, após 10:00):

| Commit | Registro original (07/10, quarta) | Reexecução 12/10 (segunda 11:45–11:46) |
|--------|-----------------------------------|----------------------------------------|
| `4473c1a` Clean P | 3/8 (domínio) | **4/8** (3 domínio + aceitação 422) |
| `0f46387` Layered P | **0/1** | **1/1** (aceitação 422) |

A ameaça H3.4, que a Fase 6 tinha só por análise, passou a ter **confirmação empírica**. A
formulação robusta para o texto: a Layered **não tem detecção determinística** da mudança de regra;
a única quebra possível vem da aceitação, e só em certas janelas. A Clean tem 3 detecções com
relógio fixo.

### E1 P Layered — **inconclusivo quanto a “teste quebrado pela intervenção”**

O 1/1 ERROR no estado P é `MongoSocketOpenException` em `localhost:27017`. Sem Docker, não se
separa “o teste da baseline é incompatível com Mongo” de “falta o processo na porta”. Na ponta da
branch o teste passa *porque o Mongo é mockado*, não porque o store novo foi exercitado.

---

## Descobertas inesperadas (úteis para a conversa com o orientador)

1. **O Capítulo 4 deve preferir a Fase 6 à Fase 4 no E1.** A rodada original da Clean não entregava
   uma aplicação que sobe. Os números de propagação descrevem um diff que, em produção, estava
   quebrado. Isso não invalida a *direção* da mudança (só `infra`), mas invalida qualquer ênfase em
   “a troca funcionou com menos alarde”.
2. **Localizar ≠ modificar ≠ detectar.** E3 empata em arquivos; a Clean perde em saltos a partir do
   REST (3 vs 1) e ganha em detecção por teste. A frase da síntese da Fase 6 (“a Clean compra
   localização”) precisa do qualificativo: localização *pelo nome da política*, não *pela cadeia do
   endpoint*.
3. **Stubs do E2 são evidência de mapeamento HTTP, não de rede.** O Toxiproxy (já feito) mostra o
   que os stubs escondem: 201 com 5 s de latência; silêncio de 60 s com conexão travada; timeout com
   custo idêntico nas duas variantes.
4. **Instrumento de aceitação frágil à segunda-feira.** Independente da arquitetura. Vale uma nota
   de rodapé no Capítulo 4 e, se houver tempo *depois* da defesa, um ajuste da suíte — em branch
   nova, sem tocar `exp/e3-regra`.
5. Artefatos `experiments/e3-regra/*/diff-numstat.txt` estão em UTF-16 LE; o git os trata como
   binários. Conteúdo bate com o diff, mas polui `git diff`. Não foram “corrigidos” (seriam
   reescrita de evidência citada).

---

## Como isso se encaixa no Capítulo 4

Sugestão de uso, sem redigir o capítulo aqui:

| Seção do capítulo | Usar |
|-------------------|------|
| Resultados E1 | Tabela e ressalva de [`evidencias-capitulo-4.md`](evidencias-capitulo-4.md) §1 e §4.1; **números vigentes = E1-v2** (`fase-6-sintese.md`) |
| Resultados E2 | Stub como mapeamento; Toxiproxy como Dimensão 2 ([`e2-replicacao-toxiproxy.md`](e2-replicacao-toxiproxy.md) + CSVs da Fase 6) |
| Resultados E3 | Empate quantitativo + cadeia de saltos ([`e3-localizacao-vs-modificacao.md`](e3-localizacao-vs-modificacao.md)) + quebra de calendário (§4.2 das evidências) |
| Limitações | [`docs/05-fora-de-escopo.md`](../docs/05-fora-de-escopo.md) |
| Validade externa (N=1) | B1 não feito; texto já em `04-ameacas-validade.md` |
| Avaliação independente | B3 é instrumento, não dado; `782c1de` já declara a lacuna |

Nada nesta sessão altera a frase-síntese da Fase 6; refina *onde* citá-la e *quais* números da Fase 4
não devem ir para a banca sem ressalva.
