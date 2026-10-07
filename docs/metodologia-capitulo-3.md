# Capítulo 3 — Metodologia

Texto preparado para continuidade do monográfico (`TCC2.docx`), seguindo os tópicos do sumário:

- 3.1 Classificação da pesquisa  
- 3.2 Contexto e objeto do estudo  
- 3.3 Desenvolvimento e configuração do sistema  
- 3.4 Procedimentos experimentais e avaliação do desacoplamento  

**Uso:** copiar/adaptar para o Word, mantendo a formatação ABNT do *Modelo Formatação IFMA*. As seções 3.1 e 3.2 abaixo **expandem** o texto já existente no documento; as seções 3.3 e 3.4 são **novas** (estavam apenas com título).

---

## 3 METODOLOGIA

### 3.1 Classificação da pesquisa

Quanto à natureza, esta pesquisa classifica-se como aplicada, uma vez que busca gerar conhecimento com aplicação prática direta na avaliação de decisões arquiteturais em sistemas distribuídos, sem a pretensão de produzir uma teoria de caráter geral (GIL, 2002). Quanto aos objetivos, a pesquisa possui caráter exploratório, pois busca investigar, por meio da implementação de um sistema e da realização de experimentos controlados, como a aplicação da Clean Architecture se relaciona ao desacoplamento em uma arquitetura de microsserviços, permitindo identificar seus efeitos e limitações no contexto estudado.

Com relação à abordagem, a pesquisa adota uma abordagem mista, combinando procedimentos qualitativos e quantitativos. A dimensão qualitativa está relacionada à definição e à interpretação das relações arquiteturais e dos limites de responsabilidade entre os componentes do sistema — por exemplo, a classificação manual das camadas atingidas por uma mudança e a análise de onde uma falha remota é tratada no código. A dimensão quantitativa está associada à mensuração dos efeitos produzidos pelas intervenções experimentais sobre o acoplamento e a independência dos componentes, por meio de métricas estruturais (ArchUnit), de propagação de mudança (`git diff`) e de taxa de sucesso sob falha. Para Creswell (2010), a definição desse tipo de pesquisa se trata de:

> “uma abordagem da investigação que combina ou associa as formas qualitativa e quantitativa. Envolve suposições filosóficas, o uso de abordagens qualitativas e quantitativas e a mistura das duas abordagens em um estudo. Por isso, é mais do que uma simples coleta e análise dos dois tipos de dados; envolve também o uso das duas abordagens em conjunto, de modo que a força geral de um estudo seja maior do que a da pesquisa qualitativa ou quantitativa isolada”. (CRESWELL, 2010, p. 26)

Quanto aos procedimentos técnicos, a pesquisa é conduzida como um **estudo de caso único**, seguindo as diretrizes propostas por Runeson e Höst (2009) para estudos de caso em Engenharia de Software. O estudo de caso tem como objeto um sistema de gestão de laboratórios (LabManager) desenvolvido com base em uma arquitetura de microsserviços. Esse contexto permite investigar o comportamento do acoplamento a partir de uma implementação concreta, na qual as dependências arquiteturais podem ser observadas e submetidas a intervenções previamente definidas.

No âmbito desse estudo de caso, são realizados **três experimentos controlados**, estruturados conforme princípios de experimentação em Engenharia de Software apresentados por Wohlin *et al.* (2012), contemplando a definição das variáveis de interesse, das condições de execução, do protocolo experimental e dos critérios de medição. Para viabilizar a comparação controlada do efeito da Clean Architecture, o serviço de Agendamento — unidade central de análise — foi implementado em **duas variantes** com o mesmo contrato funcional: uma organizada segundo a Clean Architecture (tratamento) e outra segundo o estilo em camadas idiomático Spring (controle). Dessa forma, o estudo de caso fornece o contexto sistêmico, enquanto os experimentos fornecem o contraste sistemático entre estilos internos de organização do código.

A generalização pretendida é **analítica**, e não estatística: os resultados permitem discutir benefícios e limitações da abordagem no domínio e na *stack* estudados, reconhecendo as restrições de um único caso (N=1 por célula experimental), conforme discutido nas ameaças à validade (seção correspondente / Wohlin *et al.*, 2012; Runeson e Höst, 2009).

---

### 3.2 Contexto e objeto do estudo

O contexto desta pesquisa está relacionado ao desenvolvimento de um sistema de gestão de laboratórios, utilizado como objeto para investigar a relação entre a aplicação da Clean Architecture e o desacoplamento em uma arquitetura de microsserviços. O domínio foi selecionado por apresentar diferentes responsabilidades de negócio que podem ser organizadas em componentes distintos, incluindo aspectos relacionados ao controle de estoque, à alocação de recursos e ao gerenciamento de permissões. Essa escolha dialoga com a recomendação de organizar microsserviços em torno de capacidades de negócio (NEWMAN, 2015; RICHARDSON, 2019) e com a noção de contextos delimitados do Domain-Driven Design (EVANS, 2010).

O objeto do estudo consiste, portanto, no próprio sistema LabManager desenvolvido para a pesquisa. A aplicação foi estruturada segundo uma arquitetura de microsserviços, buscando estabelecer limites entre responsabilidades de negócio. Cada serviço possui **banco próprio** (autonomia de dados) e comunica-se com os demais apenas por **API/contrato**, sem compartilhamento de tabelas. O sistema não representa apenas o resultado da implementação: constitui também o ambiente no qual são observadas as dependências existentes entre os componentes e os efeitos produzidos por diferentes intervenções arquiteturais.

A escolha de um sistema construído especificamente para o estudo permite controlar as características da implementação e estabelecer condições previamente definidas para a realização das intervenções. Nesse contexto, as decisões relacionadas à divisão das responsabilidades, à comunicação entre serviços e à organização interna dos componentes são mantidas como elementos do contexto experimental, possibilitando observar como essas decisões se comportam diante das alterações propostas ao longo da pesquisa.

Os contextos delimitados implementados são:

1. **Identity & Access (`identity-service`)** — usuários, papéis e verificação de permissões; raiz de autorização do sistema.  
2. **Catálogo (`catalog-service`)** — laboratórios, capacidade e horários de funcionamento; consome Identity em operações administrativas.  
3. **Agendamento (`scheduling-service`)** — **serviço-alvo** da comparação experimental; reservas, conflitos e regras de antecedência/duração; consome Catalog e Identity.  
4. **Inventário (`inventory-service`)** — consumíveis, movimentações de entrada/saída e saldo; consome Identity.

O estudo concentra-se, especificamente, na análise do desacoplamento arquitetural. Para esse fim, são observadas dimensões complementares alinhadas à fundamentação teórica (seção 2.5.4):

- o **desacoplamento interno** aos serviços, relacionado à organização das responsabilidades e das dependências entre seus componentes;  
- o **desacoplamento entre serviços**, relacionado ao grau de independência existente entre os componentes distribuídos;  
- e, como instrumento de observação empírica do custo da mudança (MARTIN, 2019), a **propagação de alteração** ao longo das camadas e arquivos do serviço-alvo.

Assim, o sistema de gestão de laboratórios constitui o caso analisado, enquanto o desacoplamento produzido pelas decisões arquiteturais adotadas constitui o fenômeno de interesse da pesquisa. A unidade de comparação experimental é o **Agendamento**, nas variantes Clean e Layered, mantendo-se constante o contrato REST e as regras de negócio do caso de uso *Solicitar Reserva*.

---

### 3.3 Desenvolvimento e configuração do sistema

Esta seção descreve como o artefato experimental foi construído e configurado, de modo a tornar reproduzíveis as condições nas quais os experimentos foram executados.

#### 3.3.1 Estratégia de desenvolvimento e versionamento

O desenvolvimento foi organizado em **fases incrementais**, versionadas em repositório Git monorepo Maven, com *tags* de congelamento (`fase-0` a `fase-2`, e `baseline` ao final do ambiente experimental). Essa estratégia atende a dois propósitos: (i) documentar a evolução do sistema de pesquisa; e (ii) fornecer pontos de referência estáveis para medição de propagação de mudança (`git diff` a partir de `baseline`).

As decisões estruturantes foram registradas em *Architecture Decision Records* (ADRs), incluindo a adoção do monorepo Maven, a *stack* Java 25 / Spring Boot 4.1, a restrição das duas variantes arquiteturais apenas ao Agendamento, a comunicação síncrona HTTP na fase inicial e a ativação de Docker/PostgreSQL a partir da Fase 3.

#### 3.3.2 Decomposição em microsserviços

A decomposição seguiu fronteiras de capacidade de negócio. Apenas o Agendamento possui duas implementações; Identity, Catalog e Inventory adotam Clean Architecture, reduzindo o escopo da comparação A/B ao serviço em que as regras de reserva e as dependências síncronas se concentram (ADR de variantes apenas no Agendamento). Essa escolha evita confundir a variável independente (estilo interno do Agendamento) com diferenças de feature entre serviços.

O caso de uso principal analisado é **Solicitar Reserva** (`POST /api/reservations`), com o corpo `{ laboratoryId, requesterId, start, end }`. As regras de domínio controladas incluem: ausência de sobreposição com reservas confirmadas; conformidade com o horário de funcionamento obtido do Catalog; antecedência mínima de 24 horas (alterada no experimento E3); duração máxima de 4 horas; e verificação da permissão `RESERVATION_REQUEST` no Identity.

#### 3.3.3 Organização interna das variantes do Agendamento

**Variante Clean (`scheduling-service-clean`).** Organizada em submódulos Maven `domain`, `application`, `presentation` e `infra`, com direção de dependências orientada ao núcleo. O domínio concentra entidades, *value objects*, políticas (`ReservationPolicy`) e portas de repositório, sem dependência de Spring ou JPA. A aplicação orquestra casos de uso e declara portas para Catalog, Identity e relógio. A presentation expõe REST e trata exceções. A infra implementa adaptadores HTTP, persistência e o *composition root* Spring Boot. A conformidade com a Regra da Dependência e a ausência de contaminação de domínio por *frameworks* são verificadas automaticamente por testes ArchUnit.

**Variante Layered (`scheduling-service-layered`).** Implementada como módulo único no estilo idiomático Spring: `@RestController` → `@Service` → Spring Data, com entidade de persistência anêmica e clientes HTTP invocados a partir da camada de serviço. A implementação foi conduzida de forma competente (não como “espantalho”), preservando o mesmo contrato REST e as mesmas regras de negócio, a fim de fortalecer a validade de construção da comparação (Wohlin *et al.*, 2012).

A **paridade funcional** entre as variantes é assegurada por uma suíte de aceitação compartilhada (`scheduling-acceptance`), executada nos testes de ambas as implementações.

#### 3.3.4 Stack tecnológica e ambiente

| Elemento | Escolha |
|----------|---------|
| Linguagem / plataforma | Java 25 LTS |
| Framework | Spring Boot 4.1.x |
| Build | Maven (wrapper incluso no repositório) |
| Persistência (desenvolvimento/testes offline) | H2 em memória (perfil *default*) |
| Persistência (ambiente experimental) | PostgreSQL 16 por serviço, via Docker Compose (perfil `postgres`) |
| Instrumento de métricas estruturais | ArchUnit 1.4.x (`architecture-metrics`) |
| Orquestração de falhas (E2) | Toxiproxy (Compose) e/ou stubs de falha nos testes |
| Observabilidade mínima | *logs* estruturados ECS no perfil experimental |

Os microsserviços executam no *host* (`spring-boot:run`); os *containers* concentram infraestrutura (PostgreSQL por serviço, MongoDB para o experimento E1, Toxiproxy). Essa separação mantém o foco experimental no código dos serviços e facilita a troca de *stores* sem reescrever o domínio.

#### 3.3.5 Instrumentação para medição

Antes das intervenções, foi estabelecido o instrumento de medição:

1. **ArchUnit** — regras de camada e métricas de Martin/Lakos (acoplamento aferente/eferente, instabilidade, abstratividade, distância da sequência principal, CCD e derivados), exportadas em CSV (`baseline-metrics.csv`).  
2. **Git** — `git diff --numstat` a partir da *tag* `baseline`, classificando arquivos por camada/pacote.  
3. **Testes automatizados** — testes de domínio, de integração HTTP (`MockMvc`) e simulações de falha remota; a suíte de aceitação compartilhada ancora a paridade Clean × Layered.

A linha de base experimental (`baseline`) congela o estado do sistema após a configuração do ambiente Docker, a partir da qual as *branches* `exp/e3-regra`, `exp/e1-infra` e `exp/e2-falhas` foram derivadas sem contaminar umas às outras.

---

### 3.4 Procedimentos experimentais e avaliação do desacoplamento

Esta seção operacionaliza as variáveis, os critérios de desacoplamento, o protocolo dos três experimentos e as ameaças à validade consideradas no desenho.

#### 3.4.1 Variáveis experimentais

| Tipo | Variável | Operacionalização |
|------|----------|-------------------|
| Independente | Estilo arquitetural interno do Agendamento | Clean Architecture × estilo em camadas Spring |
| Dependentes | Desacoplamento interno, entre serviços e propagação de mudança | Métricas das seções 3.4.2 a 3.4.4 |
| Controladas | Contrato REST, regras de negócio, *stack*, cenários de aceitação | Mesmo *endpoint*, mesmas regras, Java/Spring, `scheduling-acceptance` |

#### 3.4.2 Critérios de avaliação do desacoplamento

Os critérios foram definidos *a priori* (objetivo específico de definir critérios) e organizados em três dimensões instrumentadas.

**Dimensão 1 — Desacoplamento interno.** Mede a organização das dependências *dentro* do serviço. Instrumento: ArchUnit. Métricas: Ca, Ce, instabilidade (I), abstratividade (A), distância da sequência principal (D), métricas de Lakos (CCD, ACD, RACD, NCCD), violações da Regra da Dependência e contaminação de domínio por tipos de *framework*/infraestrutura. Expectativa teórica: a variante Clean deve apresentar domínio livre de Spring/JPA e zero (ou próximo de zero) violações de direção de dependência; a Layered pode ser funcionalmente equivalente, porém com maior acoplamento das camadas de negócio a *frameworks*.

**Dimensão 2 — Desacoplamento entre serviços.** Mede a autonomia entre microsserviços. Indicadores: fan-out síncrono do caso de uso (número de chamadas HTTP a Identity e Catalog); acoplamento temporal (taxa de sucesso quando dependências falham — experimento E2); inspeção de contrato (campos consumidos); e checklist de implantação (quais serviços precisam ser alterados/reimplantados). Expectativa teórica relevante: a Clean Architecture **não** elimina, por si só, o acoplamento temporal síncrono; um resultado em que E2 afeta igualmente as duas variantes é evidência válida das **limitações** da abordagem em sistemas distribuídos.

**Dimensão 3 — Propagação de mudança.** Mede o custo da mudança (MARTIN, 2019). Instrumento: Git (`diff --numstat`) e classificação manual de camadas. Métricas: arquivos alterados, linhas inseridas/removidas, camadas/pacotes atingidos, serviços atingidos e testes quebrados após a intervenção (antes de ajustes cosméticos). Protocolo: partir de `baseline`; criar *branch* experimental; aplicar apenas a intervenção documentada; registrar o *diff* e o resultado dos testes; não misturar refatorações estéticas à medição.

#### 3.4.3 Protocolo geral de execução

1. Congelar a *tag* `baseline` (ambiente experimental pronto; paridade Clean × Layered verificada).  
2. Para cada experimento, criar *branch* isolada a partir de `baseline` (evitar contaminação entre E1, E2 e E3).  
3. Aplicar a intervenção conforme o protocolo específico.  
4. Coletar artefatos em `experiments/eN-*/{clean,layered}/` (`diff-numstat.txt`, `notas.md`, métricas quando aplicável).  
5. Analisar quantitativamente (tabelas) e qualitativamente (locus da mudança / da falha).

Ordem adotada: **E3 → E1 → E2** (da intervenção com menor dependência de infraestrutura distribuída para a de maior orquestração).

#### 3.4.4 Experimento E3 — Alteração de regra de negócio

**Objetivo.** Avaliar onde se concentra a mudança de uma regra de domínio nas duas variantes.

**Hipótese.** Na Clean, a alteração concentra-se no núcleo de domínio (e testes de domínio); na Layered, a regra pode estar embutida em *services*/controladores/entidades, com risco de espalhamento.

**Intervenção.** Alterar a antecedência mínima de reserva de **24 horas para 48 horas**, aplicando a mesma mudança nas duas variantes.

**Medidas.** `git diff --numstat`; camadas atingidas; necessidade de ajuste em testes de domínio versus testes de integração/aceitação.

**Procedimento.** *Branch* `exp/e3-regra`; alterar constantes/mensagens e testes necessários à regra; atualizar a suíte compartilhada apenas o indispensável à nova antecedência; registrar *diffs* e notas por variante.

#### 3.4.5 Experimento E1 — Substituição de tecnologia de infraestrutura

**Objetivo.** Avaliar a propagação da troca do mecanismo de persistência.

**Hipótese.** Na Clean, a mudança altera principalmente `infra` (adaptadores); na Layered, propaga-se com maior frequência para camadas que misturam negócio e persistência (`entity`, `service`, repositórios).

**Intervenção.** Substituir JPA/PostgreSQL (e o modelo H2 de desenvolvimento) por **MongoDB** apenas em `scheduling-service-*`.

**Medidas.** Diff Git por camada; eventuais violações ArchUnit após o ajuste; testes quebrados e ajustes estritamente necessários.

**Procedimento.** *Branch* `exp/e1-infra`; implementar adaptador Mongo na Clean e equivalentes na Layered; coletar métricas de propagação **antes** de limpezas estéticas; documentar arquivos e pacotes tocados.

#### 3.4.6 Experimento E2 — Simulação de falhas entre serviços

**Objetivo.** Avaliar o acoplamento temporal síncrono e se a Clean mitiga a degradação quando dependências externas falham.

**Hipótese.** A Clean Architecture **não** mitiga, por si só, o acoplamento temporal; ambas as variantes sofrem degradação semelhante quando Identity e/ou Catalog ficam indisponíveis, salvo introdução de padrões de resiliência (timeout, *circuit breaker*, *fallback*), ortogonais ao estilo Clean.

**Intervenção.** Indisponibilizar Identity e/ou Catalog durante `POST /api/reservations`, por meio de stubs de falha nos testes automatizados e, opcionalmente, Toxiproxy (perfil `e2` apontando o Agendamento às portas do *proxy*).

**Medidas.** Taxa de sucesso/erro; status HTTP observado; locus da falha (domínio × adaptadores/clientes × *handlers*); necessidade de alterar código de produção para degradar.

**Procedimento.** *Branch* `exp/e2-falhas`; executar cenários de Identity *down* e Catalog *down* em cada variante; registrar se a falha “vaza” para o domínio ou permanece nas bordas de integração.

#### 3.4.7 Reforço metodológico: replicação controlada dos experimentos

Após a primeira rodada (E1–E3), uma avaliação crítica identificou cinco fragilidades:

- o E2 verificava o mapeamento de exceções por meio de *stubs*, e não falhas de rede;
- o adaptador MongoDB do E1 nunca havia sido executado contra um banco real;
- a medida "testes quebrados" ficava contaminada, porque produção e testes eram ajustados juntos;
- o E3 consistia na troca de uma constante, sendo pouco discriminante;
- as métricas do ArchUnit não eram reavaliadas após as intervenções.

Para tratá-las, os experimentos foram **refeitos** a partir de uma nova *tag*, `baseline-v2`, cujo código de produção é idêntico ao da `baseline`. O procedimento passou a incluir quatro elementos:

1. **Pré-registro de hipóteses com previsões verificáveis.** Antes de qualquer execução, um documento com previsões numéricas e direcionais por experimento e variante foi versionado no repositório; a data do *commit* serve de evidência. As previsões incluíram explicitamente resultados desfavoráveis à variante Clean.
2. **Protocolo de dois *commits*.** Para cada experimento e variante, numa *branch* própria (`exp2/<experimento>-<variante>`):
   - um *commit* P altera **apenas** código de produção;
   - em seguida, a suíte de testes é executada e as falhas são registradas (falhas de compilação contam como quebra);
   - por fim, um *commit* T altera **apenas** testes.

   Quando a execução contra infraestrutura real revela defeitos de produção, eles são corrigidos num *commit* P2 e registrados separadamente. O procedimento é automatizado pelo *script* `experiments/scripts/medir-experimento.ps1`.
3. **Infraestrutura real nos experimentos tecnológicos.** O E1-v2 executa os testes de integração contra MongoDB real, provisionado por Testcontainers (`mongo:7`). O E2-v2 injeta falhas de rede reais com Toxiproxy entre o Agendamento e os serviços Identity e Catalog, com cinco cenários:
   - S0: controle, sem falha;
   - S1 e S2: latência de 500 ms e de 5 s;
   - S3: recusa de conexão;
   - S4: conexão aceita que nunca responde.

   As medidas são latência (p50, p95, máximo) e distribuição de respostas (201, 503, timeout do cliente). O E2-v2 tem duas fases: A, com o código original, e B, após introduzir timeouts nos clientes HTTP (1 s para conexão e 2 s para leitura) pelo protocolo de dois *commits*. Cada célula (fase × variante × cenário) recebeu 30 requisições sequenciais, após 3 de aquecimento, com timeout de 60 s no cliente de carga. A exceção foi o S4, com 10 requisições por célula: na fase A cada requisição dura 60 s. Esse desvio do pré-registro está documentado.
4. **Reavaliação arquitetural após cada intervenção.** As regras do ArchUnit e as métricas de Martin e Lakos são recalculadas ao fim de cada experimento e variante e publicadas com rótulo próprio, para comparação com `baseline-v2`.

Acrescentou-se o experimento **E3b**, que introduz uma regra nova (no máximo três reservas ativas por solicitante numa mesma semana ISO). Diferentemente do E3, ela exige uma nova consulta ao repositório e uma nova invariante, situação em que a Clean tende a exigir mais alterações. O E3b foi escolhido justamente para testar a hipótese num cenário potencialmente desfavorável à Clean.

O "custo de localizar" uma regra no código, que os *diffs* não capturam, é discutido de forma analítica, com base na posição da regra em cada variante e no comportamento dos testes. Não houve avaliação por terceiros, e essa limitação é declarada na seção 3.4.8.

**Triangulação.** Os resultados são interpretados em três frentes:

- entre instrumentos (diff, testes, ArchUnit e medidas de execução);
- entre experimentos (E1, E2, E3 e E3b);
- com a literatura empírica sobre Clean/Hexagonal Architecture e acoplamento em microsserviços [CITAÇÃO: estudos empíricos sobre arquitetura hexagonal/Clean e manutenibilidade] [CITAÇÃO: estudos sobre acoplamento temporal e resiliência em microsserviços].

A comparação com a literatura não generaliza o caso. Ela posiciona o achado como convergente ou divergente em relação ao que outros trabalhos já observaram (RUNESON; HÖST, 2009).

#### 3.4.8 Registro, análise e ameaças à validade

Para cada par (experimento × variante) foram produzidos artefatos padronizados (*diff*, notas interpretativas e referências às métricas de linha de base). A análise combina tabelas quantitativas com interpretação qualitativa das fronteiras, conforme recomendado para estudos de caso em Engenharia de Software (RUNESON; HÖST, 2009).

As principais ameaças e mitigações consideradas no desenho incluem:

| Validade | Ameaça | Mitigação |
|----------|-------|-----------|
| Construção | Métrica inadequada de “desacoplamento” | Três dimensões com instrumentos objetivos (ArchUnit, Git, taxa sob falha) |
| Construção | Clean “de fachada” / Layered “espantalho” | Submódulos Maven sem Spring no domínio; Layered idiomática competente; mesma suíte de aceitação |
| Interna | Confusão de variáveis / viés do implementador | Contrato REST idêntico; hipóteses pré-registradas (incluindo resultado negativo em E2); protocolo fixo de intervenção |
| Interna | Aprendizado entre variantes | Clean implementada primeiro; Layered depois, com checklist de paridade |
| Externa | Domínio e *stack* únicos; escala pequena | Generalização analítica; limitações declaradas |
| Conclusão | N=1 por célula; *cherry-picking* | Bateria fixa de métricas; E2 como teste explícito de limitação |
| Interna | Ajuste *post hoc* das hipóteses | Pré-registro versionado antes da execução (Fase 6) |
| Construção | "Testes quebrados" contaminado por ajustes simultâneos | Protocolo de dois *commits* (P/T) |
| Construção | Falha de rede simulada por *stub*; banco simulado | Toxiproxy (E2-v2) e Testcontainers/MongoDB (E1-v2) |
| Interna | Mesmo autor implementa, mede e interpreta | Pré-registro e artefatos objetivos (*diffs*, testes, métricas) versionados; sem avaliação independente (limitação declarada) |

Ficaram fora do escopo, e são declarados como limitações e trabalho futuro:

- replicação do protocolo num segundo serviço;
- mudança de contrato entre serviços;
- erosão arquitetural ao longo de várias iterações;
- comportamento sob carga concorrente;
- introdução de *circuit breaker*;
- avaliação independente, por terceiros, da facilidade de localizar e alterar regras em cada variante.

---

## Notas de redação (para o autor)

1. **Onde colar no Word:** substituir/estender o texto atual das seções 3.1–3.2; preencher 3.3 e 3.4 que estavam só com título.  
2. **Figuras sugeridas:** (a) diagrama dos quatro contextos; (b) comparação esquemática Clean × Layered no Agendamento; (c) fluxo do caso *Solicitar Reserva* com fan-out Identity+Catalog; (d) pipeline *baseline* → *branches* E3/E1/E2.  
3. **Tabelas:** as tabelas deste arquivo podem virar “Quadro X” / “Tabela Y” no padrão IFMA.  
4. **Capítulos seguintes (não são metodologia):** “Tecnologias e desenvolvimento” pode detalhar APIs e módulos; “Experimentos e resultados” deve **apresentar** os números (usar `experiments/fase-6-sintese.md`, que substitui a síntese da Fase 5 como fonte principal), evitando misturar resultados longos dentro da metodologia.  
4a. **Marcadores `[CITAÇÃO: ...]`** na seção 3.4.7 devem ser substituídos por referências reais consultadas pelo autor; não foram preenchidos para evitar referências não verificadas.  
5. **Citação Creswell:** manter como no documento original (bloco citado). Conferir páginas no exemplar físico/PDF utilizado.  
6. Após colar, atualizar sumário automático e numeração de figuras/tabelas.
