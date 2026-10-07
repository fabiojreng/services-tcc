# Fase 6 — Síntese da replicação controlada

Versão vigente dos resultados. Ela substitui as conclusões da [`fase-5-sintese.md`](fase-5-sintese.md) onde houver divergência.

Questão de pesquisa:

> Em que medida a aplicação da Clean Architecture contribui para o desacoplamento em arquiteturas de microsserviços, e quais são suas limitações quando aplicada a sistemas distribuídos?

---

## 1. O que mudou em relação à Fase 5

| Fragilidade identificada | Tratamento na Fase 6 |
|--------------------------|----------------------|
| Hipóteses formuladas junto com a análise | Pré-registro commitado antes da execução ([`fase-6-preregistro.md`](fase-6-preregistro.md)) |
| Produção e testes ajustados no mesmo commit | Protocolo de dois commits (P → medir → T → medir), com uma branch por experimento e variante |
| E1 sem banco real | MongoDB 7 via Testcontainers |
| E2 com stubs | Toxiproxy real, com latência, indisponibilidade e conexão travada; fase A sem e fase B com timeout |
| E3 pouco discriminante | E3b: regra que exige novo acesso a dados |
| ArchUnit só na linha de base | Regras e métricas após cada experimento (`experiments/<exp>/<variante>/metricas.csv`) |

Ponto de partida comum: tag `baseline-v2` (`9beb275`). A produção é idêntica à `baseline`, exceto por uma constante HTTP equivalente (422).

---

## 2. Resultados consolidados

### 2.1 Propagação de mudança (Dimensão 3)

| Experimento | Variante | Produção: arquivos / linhas | Camadas de negócio tocadas | Testes quebrados após P | Ajuste de testes |
|-------------|----------|-----------------------------|----------------------------|-------------------------|------------------|
| E3-v2 (24 h → 48 h) | Clean | 1 / 3+ 3− | `domain` | **3 de 8** | 1 arquivo |
| | Layered | 1 / 2+ 2− | `service` | **0 de 1** | nenhum |
| E3b (limite semanal) | Clean | **5 / 71+ 0−** | `domain`, `application`, `infra` | 0 de 8 | 3 arquivos, 92+ |
| | Layered | **2 / 18+ 1−** | `service`, `repository` | 0 de 1 | 2 arquivos, 29+ |
| E1-v2 (JPA → Mongo) | Clean | 8 / 28+ 71− | **nenhuma** (só `infra`) | 1 de 8 | 2 arquivos, 15+ 1− |
| | Layered | 6 / 11+ 50− | `service` (só troca de tipo) | 1 de 1 | 2 arquivos, 15+ 1− |
| E2-v2 fase B (timeout) | Clean | 1 / 10+ 1− | nenhuma (`infra/config`) | 0 de 8 | nenhum |
| | Layered | 1 / 10+ 1− | nenhuma (`config`) | 0 de 1 | nenhum |

No E1-v2, os números de produção incluem o commit P2: uma linha de configuração de UUID que só o Mongo real revelou, necessária nas duas variantes. O `docker-compose` (+9 linhas, igual nas duas) não entra na contagem.

### 2.2 Desacoplamento interno após cada experimento (Dimensão 1)

| Experimento | Clean | Layered |
|-------------|-------|---------|
| E3-v2 | Regras verdes; métricas = `baseline-v2` | Regras verdes; métricas = `baseline-v2` |
| E3b | Regras verdes; `domain.policy` Ca 1→3, I 0,75→0,50, D 0,25→0,50; `application` Ce 5→6 | Regras verdes; métricas = `baseline-v2` |
| E1-v2 | Regras verdes; métricas = `baseline-v2` | Regras verdes; CCD 18→22, `repository` Ca 0→2 (**inconclusivo**: o bytecode não mostra dependência nova) |
| E2-v2 B | Regras verdes; métricas = `baseline-v2` | Regras verdes; métricas = `baseline-v2` |

Nenhum experimento introduziu dependência saindo do domínio da Clean. No E3b, o aumento de D em `domain.policy` reflete um pacote concreto que ficou mais usado (mais Ca). Isso é esperado para uma política nova e não indica violação da Regra da Dependência.

### 2.3 Falhas entre serviços (Dimensão 2)

| Cenário | Fase A (sem timeout): Clean / Layered | Fase B (timeout 1 s/2 s): Clean / Layered |
|---------|----------------------------------------|--------------------------------------------|
| S0 nenhum | 201 · 19 ms / 201 · 19 ms | 201 · 18 ms / 201 · 17 ms |
| S1 +500 ms | 201 · 518 ms / 201 · 517 ms | 201 · 516 ms / 201 · 516 ms |
| S2 +5000 ms | 201 · 5019 ms / 201 · 5018 ms | **503 · 2009 ms / 503 · 2010 ms** |
| S3 Identity off | 503 · 4 ms / 503 · 4 ms | 503 · 4 ms / 503 · 4 ms |
| S4 conexão travada | **sem resposta (60 s) / sem resposta (60 s)** | **503 · 2009 ms / 503 · 2010 ms** |

Valores: resultado predominante (100% em todas as células) e p50. N = 30 por célula, exceto S4 (N = 10). Dados brutos: `e2-falhas-v2/resultados-fase-{A,B}.csv`.

---

## 3. Confronto com as hipóteses pré-registradas

| Experimento | Consistentes | Parciais / não confirmadas |
|-------------|--------------|----------------------------|
| E3-v2 | H3.1, H3.2, H3.3, H3.5 | H3.4: a aceitação passou na data de execução, mas a fragilidade de calendário (segunda-feira após as 10:00) foi confirmada por análise |
| E3b | Hb.1, Hb.2, Hb.3, Hb.4, Hb.6 | Hb.5 parcial: a Layered *poderia* ter teste de unidade (Mockito, 3 mocks); a referência idiomática não o tinha |
| E1-v2 | H1.1, H1.3, H1.4, H1.5, H1.6 | H1.2: o `service` foi tocado, mas apenas por troca de tipo |
| E2-v2 | H2.1 a H2.8 | — |

Nenhum resultado contrariou frontalmente uma previsão. Isso tem duas leituras possíveis: as hipóteses eram bem calibradas, ou eram conservadoras demais para serem refutadas. A segunda leitura é uma ameaça à validade e está registrada na seção 7.

---

## 4. Localizar, testar e modificar

Contar arquivos alterados mistura três custos diferentes. Separá-los foi o principal ganho analítico da Fase 6.

| Custo | Clean | Layered | Evidência |
|-------|-------|---------|-----------|
| **Localizar** a regra | Baixo: a regra tem nome e lugar (`ReservationPolicy`, `WeeklyReservationQuota`) | Médio: a regra é uma constante ou um `if` dentro de `ReservationService`, junto com HTTP e persistência | E3-v2, E3b |
| **Detectar** a mudança por testes | Alta: 3 testes de unidade falharam no E3-v2 | Nula: a mudança de regra passou sem nenhum teste falhar | E3-v2 |
| **Testar** sem infraestrutura | Possível: 4 testes puros no E3b | Só via integração (Spring + banco) | E3b |
| **Modificar** quando a regra precisa de dados novos | Alto: porta + adaptador + repositório + caso de uso + política (5 arquivos, 71 linhas) | Baixo: um método de repositório + um `if` (2 arquivos, 19 linhas) | E3b |
| **Conter** troca de tecnologia | Alta: zero linhas fora de `infra` | Média: o `service` muda o tipo do repositório | E1-v2 |
| **Reduzir volume** na troca de tecnologia | Não reduziu: mais linhas que a Layered | — | E1-v2 |

A Clean Architecture compra **localização, detecção e contenção** e paga em **custo de modificação** quando a mudança atravessa a fronteira de uma porta. É um *trade-off*, não uma vantagem absoluta.

---

## 5. Resposta revista à questão de pesquisa

### Em que medida contribui?

1. **Desacoplamento interno:** contribui. O domínio permaneceu livre de framework em todos os experimentos, e as regras ArchUnit tornaram isso verificável a cada mudança.
2. **Mudança tecnológica:** contribui para a **contenção** (E1-v2: só `infra`), sem reduzir o volume alterado.
3. **Mudança de regra:** contribui para **localizar e testar**. Quando a regra exige novo acesso a dados, porém, aumenta o custo de **modificar** (E3b).
4. **Desacoplamento entre serviços:** **não contribui**. Sob falhas de rede reais, as duas variantes têm comportamento indistinguível (diferença de p50 < 1%), com e sem timeout.

### Limitações em sistemas distribuídos

- O acoplamento temporal síncrono não é afetado pela organização interna. Sem timeout, ambas as variantes ficam presas indefinidamente a uma dependência travada (S4).
- A correção de resiliência (timeout) teve custo **idêntico** nas duas variantes: 1 arquivo, 10+/1−, num ponto de configuração. A Layered também tinha um ponto único de montagem do cliente HTTP. A localidade dessa preocupação transversal veio da **injeção de dependências do framework**, não das camadas da Clean.
- O timeout melhora a latência sob falha (60 s → 2 s), mas não a disponibilidade (0% de sucesso em S2 e S4). Disponibilidade sob falha exige outros mecanismos (cache, fallback, mensageria), que não foram testados.

### Síntese em uma frase

> No LabManager, a Clean Architecture **conteve** mudanças de infraestrutura e **tornou as regras localizáveis e testáveis**, ao custo de **mais trabalho para modificar** regras que exigem novos dados. Ela **não alterou em nada** o comportamento do serviço diante de falhas de rede, cuja mitigação teve o mesmo custo nas duas arquiteturas.

---

## 6. Triangulação com a literatura (esqueleto)

Os marcadores `[CITAÇÃO: ...]` indicam onde inserir referências na redação final.

| Achado | Converge com | Diverge de / tensiona |
|--------|--------------|-----------------------|
| Contenção da troca de persistência (E1-v2) | Proposta da Regra da Dependência e de portas e adaptadores [CITAÇÃO: Martin, 2017; Cockburn, 2005] | Afirmações de que a Clean "facilita trocar o banco" sem custo: o volume não caiu |
| Custo de modificação no E3b | Críticas de complexidade acidental e cerimônia em arquiteturas em camadas concêntricas [CITAÇÃO: estudo empírico ou crítica de prática] | Recomendações de aplicar a Clean indistintamente a todo serviço |
| Detecção de mudança de regra por testes (E3-v2) | Testabilidade do núcleo como benefício central [CITAÇÃO: Martin, 2017; Freeman e Pryce, 2009] | — |
| Indiferença à falha de rede (E2-v2) | Falácias da computação distribuída; resiliência como preocupação de integração [CITAÇÃO: Deutsch; Nygard, 2018] | Textos que apresentam a Clean como solução de desacoplamento entre serviços |
| Localidade do timeout em ambas as variantes | Injeção de dependências e *composition root* como mecanismo de localidade [CITAÇÃO: Seemann, 2011] | Atribuição dessa localidade exclusivamente à Clean |
| Métricas de Martin estáveis em quase todos os experimentos | Uso de I/A/D como indicador de estrutura [CITAÇÃO: Martin, 1994] | Baixa sensibilidade das métricas de pacote a mudanças de comportamento [CITAÇÃO: estudo de validade de métricas] |

---

## 7. Desvios do pré-registro e ameaças específicas da Fase 6

| Item | Descrição | Impacto |
|------|-----------|---------|
| S4 com N = 10 | Em vez de 30, por custo de tempo (60 s por requisição na fase A) | Baixo: 10/10 em todas as células, resultado determinístico |
| Commits P2 no E1-v2 | O Mongo real revelou falta de configuração de UUID; houve um segundo commit de produção nas duas variantes | Registrado separadamente (`*-P1.txt`). A contagem de testes quebrados não mudou entre P1 e P2 |
| Ordem de execução | Clean antes da Layered em todos os experimentos; o executor aprendeu com a primeira variante | Pode favorecer a Layered em tempo/qualidade da solução. Mitigado pelas implementações mínimas descritas antes da execução |
| Mesmo executor e autor | Quem implementa também mede e interpreta | Mitigado só em parte, pelo pré-registro e por artefatos objetivos. A leitura de "localizar" e "detectar" (seção 4) é do autor e não teve validação independente |
| Hipóteses possivelmente conservadoras | Nenhuma refutação frontal | Limita o poder de falsificação do desenho |
| Anomalia de CCD no E1-v2 Layered | Mudança de métrica sem dependência nova no bytecode | Tratada como artefato da ferramenta; não usada como evidência |
| Ambiente de E2 local | Clientes, serviços e proxy na mesma máquina; latências de base não representam rede real | Afeta valores absolutos, não a comparação entre variantes |
| Circuit breaker | Fora do escopo já no pré-registro (bibliotecas sem suporte declarado ao Spring Boot 4) | Resiliência medida só com timeout |

## 8. O que não foi testado (declarado antes da execução)

- Replicação do protocolo em um segundo serviço (N = 2).
- Mudança de contrato entre serviços (E4).
- Erosão arquitetural ao longo de múltiplas iterações.
- Comportamento sob carga concorrente (o S4 da fase A sugere esgotamento de threads, mas isso não foi medido).
- Entrada de um novo serviço no sistema maduro.
- Avaliação independente da facilidade de localizar e alterar regras (decidido após a execução e declarado aqui como limitação).

---

## 9. Mapa de artefatos

| Artefato | Conteúdo |
|----------|----------|
| `experiments/fase-6-preregistro.md` | Hipóteses e protocolo, commitados antes da execução |
| `experiments/baseline-v2/metricas.csv` | Métricas de referência |
| `experiments/<exp>/protocolo.md` | Checklist, resultado e confronto por experimento |
| `experiments/<exp>/<variante>/` | `diff-producao.txt`, `testes-quebrados.txt`, `diff-testes.txt`, `testes-final.txt`, `archunit.txt`, `metricas.csv`, `notas.md` |
| `experiments/e2-falhas-v2/resultados-fase-{A,B}.csv` | Uma linha por requisição |
| `experiments/e2-falhas-v2/run-e2.ps1` | Harness Toxiproxy |
| `experiments/scripts/medir-experimento.ps1` | Instrumento de medição |
| Branches `exp2/<exp>-<variante>` | Commits P (e P2), T e artefatos |
| Tag `baseline-v2` | Ponto de partida comum |
