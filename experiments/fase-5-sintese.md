# Fase 5 — Resultados consolidados e discussão

Documento de síntese para alimentar as seções de **metodologia**, **resultados** e **discussão** do monográfico.

Questão de pesquisa (escopo):

> Em que medida a aplicação da Clean Architecture contribui para o desacoplamento em arquiteturas de microsserviços, e quais são suas limitações quando aplicada a sistemas distribuídos?

---

## 1. Desenho experimental (lembrete)

| Elemento | Valor |
|----------|-------|
| Tratamento | `scheduling-service-clean` |
| Controle | `scheduling-service-layered` |
| Linha de base | tag Git `baseline` (Fase 3) |
| Paridade | suíte `scheduling-acceptance` |
| Dimensões | internas (ArchUnit), entre serviços (E2), propagação (Git diff) |

Branches experimentais (código): `exp/e3-regra`, `exp/e1-infra`, `exp/e2-falhas`.  
Artefatos brutos: pastas `experiments/eN-*/`.

---

## 2. Dimensão 1 — Desacoplamento interno (linha de base)

Fonte: `experiments/baseline-metrics.csv` (export ArchUnit na Fase 3).

### Observações estruturais

| Aspecto | Clean | Layered |
|---------|-------|---------|
| Módulos Maven | `domain` / `application` / `presentation` / `infra` | módulo único |
| Domínio livre de Spring/JPA | Enforceado por ArchUnit | Entidade JPA/`@Document` no pacote `entity` |
| Regra da Dependência | Testes de camada verdes na baseline | Regras mínimas (web ↛ repository) |
| CCD (Lakos, pacotes do serviço) | 91 (mais componentes) | 18 (menos componentes) |

**Leitura:** a Clean apresenta **mais componentes** e CCD maior em valor absoluto (custo estrutural de fronteiras explícitas). Em contrapartida, o domínio permanece sem contaminação de framework — critério central da Dimensão 1. A Layered é mais “plana”: CCD menor, mas o pacote `service` concentra Ce em direção a clientes HTTP e persistência.

Não se recomenda comparar CCD bruto entre variantes como “quem é melhor”: a Clean **decompõe** o que a Layered **agrega**. O valor analítico está em **contaminação de domínio** e **direção das dependências**.

---

## 3. Dimensão 3 — Propagação de mudança (E3 e E1)

### 3.1 Tabela consolidada

| Experimento | Intervenção | Clean — produção | Layered — produção | Camadas Clean | Camadas Layered |
|-------------|-------------|------------------|--------------------|---------------|-----------------|
| **E3** | Antecedência 24h → 48h | 1 arquivo (`ReservationPolicy`) | 1 arquivo (`ReservationService`) | `domain` | `service` |
| **E1** | JPA → MongoDB | só `infra` (pom, document, adapters, config, props) | `entity` + `repository` + `service` (+ pom/props) | `infra` | `entity`, `repository`, `service` |

Detalhes numéricos: `experiments/e3-regra/*/diff-numstat.txt`, `experiments/e1-infra/*/diff-numstat.txt`.  
CSV resumido: [`propagacao-e1-e3.csv`](propagacao-e1-e3.csv).

### 3.2 E3 — regra de negócio

**Hipótese:** mudança concentra-se no núcleo na Clean; na Layered pode espalhar.

**Resultado:**

- Quantitativamente, **ambos** alteraram **um** arquivo de produção.
- Qualitativamente, o **locus** difere: política de domínio isolada (Clean) versus constante embutida no `@Service` que também orquestra HTTP e persistência (Layered).
- A Clean exigiu atualizar testes de domínio (`ReservationPolicyTest`); a Layered **não tinha** testes unitários da regra — sinal menor de quebra local, maior dependência da suíte compartilhada.

**Implicação para o TCC:** em mudanças pontuais de constante, o número de arquivos pode empatar; o benefício da Clean aparece como **fronteira estável** e **testabilidade do núcleo**, não necessariamente como “menos linhas” em E3.

### 3.3 E1 — troca de persistência

**Hipótese:** Clean altera principalmente `infra`; Layered propaga para camadas de negócio/API.

**Resultado:** **confirmado.**

- Clean: `domain`, `application` e `presentation` **intocados**; porta `ReservationRepository` absorveu a troca.
- Layered: entidade anêmica, repositório Spring Data e serviço precisaram ser alterados (anotações, tipos, remoção de `@Transactional`).

**Implicação:** este é o experimento em que a Clean Architecture mostra mais claramente a mitigação do **custo de mudança tecnológica** dentro do serviço.

---

## 4. Dimensão 2 — Desacoplamento entre serviços (E2)

### 4.1 Resultado

| Variante | Identity down | Catalog down | Taxa de sucesso |
|----------|---------------|--------------|-----------------|
| Clean | HTTP **503** | HTTP **503** | **0%** |
| Layered | HTTP **503** | HTTP **503** | **0%** |

Instrumento: testes `*FailureSimulationTests` (stubs equivalentes a dependência indisponível). Toxiproxy permanece como opção operacional (`infra/README.md`).

### 4.2 Interpretação

**Hipótese confirmada:** a Clean **não** mitiga, por si só, o **acoplamento temporal síncrono**.

- Na Clean, a falha é traduzida em `RemoteDependencyException` nos adapters HTTP e mapeada na presentation — o domínio (`ReservationPolicy`) não é contaminado.
- Na Layered, a falha explode no client chamado pelo `@Service` e é mapeada no `GlobalExceptionHandler`.
- Em ambos os casos o **caso de uso falha** da mesma forma para o cliente HTTP.

**Implicação para a questão de pesquisa:** evidência direta das **limitações** da Clean Architecture em sistemas distribuídos. Resiliência (timeout, circuit breaker, fallback, mensageria) é **ortogonal** ao estilo interno e deveria ser tratada como preocupação transversal.

Fan-out síncrono do caso Solicitar Reserva (ambas as variantes): **2** dependências (Identity + Catalog).

---

## 5. Resposta à questão de pesquisa

### Em que medida a Clean contribui para o desacoplamento?

1. **Desacoplamento interno (Dimensão 1):** contribui de forma clara ao isolar o domínio de frameworks e ao tornar verificável a Regra da Dependência (ArchUnit). Há custo estrutural (mais módulos/pacotes).
2. **Propagação de mudança tecnológica (Dimensão 3 / E1):** contribui de forma **forte** — a troca de store ficou restrita a `infra`.
3. **Propagação de mudança de regra (Dimensão 3 / E3):** contribui de forma **moderada/qualitativa** — mesmo número de arquivos de produção neste caso, porém locus no núcleo testável; favorece evolução da política sem misturar com HTTP/JPA.
4. **Desacoplamento entre serviços (Dimensão 2 / E2):** **não contribui** de forma mensurável neste desenho — ambas as variantes degradam igualmente sob falha síncrona.

### Limitações quando aplicada a sistemas distribuídos

- Não reduz fan-out síncrono nem acoplamento temporal.
- Não substitui contratos estáveis, versionamento de API, resiliência ou autonomia de dados (já presentes por desenho de microsserviços, não pela Clean).
- Pode aumentar complexidade acidental (módulos, mapeamentos, composition root) se aplicada de forma cerimonial.

### Síntese em uma frase

> No LabManager, a Clean Architecture **melhorou o desacoplamento interno e a localidade de mudanças de infraestrutura** no Agendamento, mas **não protegeu** o serviço contra falhas das dependências síncronas — limitação que deve ser declarada ao avaliar Clean em microsserviços.

---

## 6. Benefícios e limitações (quadro para o monográfico)

| | Benefícios observados | Limitações observadas |
|--|----------------------|------------------------|
| **Clean** | Domínio puro; E1 contido em `infra`; política testável; falhas remotas tipadas fora do domínio | Mais estrutura; E3 não “ganhou” em contagem de arquivos; E2 idêntico à Layered |
| **Layered** | Menos cerimônia; CCD menor; entrega rápida do mesmo contrato | Entidade/store vazam para o estilo de camadas; E1 toca service; regra sem teste unitário dedicado |
| **Sistema (ambos)** | Fronteiras de serviço + H2/Postgres/Mongo/Toxiproxy permitem experimentos reprodutíveis | Comunicação só síncrona; N=1 por célula; stack Java/Spring única |

---

## 7. Ameaças à validade (pós-experimentos)

Além de [`docs/04-ameacas-validade.md`](../docs/04-ameacas-validade.md):

- **E2 sem Toxiproxy na execução principal:** stubs reproduzem o efeito lógico (exceção na borda remota); repetição com Toxiproxy reforçaria validade de construção de “falha de rede”.
- **Testes E1 com persistência em memória:** medem propagação de código (objetivo do E1), não a fidelidade operacional do Mongo em CI.
- **E3 com mudança mínima:** uma alteração maior de regra (vários invariantes) poderia ampliar o contraste de espalhamento.

---

## 8. Trabalho futuro sugerido

1. Repetir E2 com Toxiproxy + métricas de latência/timeout.
2. Introduzir circuit breaker **nas duas** variantes e medir se o esforço de adaptação difere (Clean vs Layered).
3. Experimento de contrato (campo removido no Catalog) para Dimensão 2.
4. Mensageria assíncrona como alternativa ao fan-out síncrono.

---

## 9. Mapa de artefatos

| Artefato | Uso no texto |
|----------|--------------|
| `experiments/baseline-metrics.csv` | Tabelas Dimensão 1 |
| `experiments/propagacao-e1-e3.csv` | Tabelas Dimensão 3 |
| `experiments/e3-regra/**` | Evidência E3 |
| `experiments/e1-infra/**` | Evidência E1 |
| `experiments/e2-falhas/**` | Evidência E2 |
| `docs/02-criterios-desacoplamento.md` | Operacionalização das métricas |
| `docs/03-protocolo-experimental.md` | Protocolo |
| Branches `exp/*` | Rastreabilidade Git das intervenções |
