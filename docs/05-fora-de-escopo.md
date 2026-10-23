# O que os experimentos não testaram

Este documento declara, de forma explícita, fenômenos que **não** foram objeto de E1, E2 ou E3
(nem das replicações da Fase 6). O item 2 foi executado depois, como E4, e está marcado como tal. A lista atende ao Capítulo 4: o que se pode afirmar a partir da
evidência coletada, e o que permanece como limitação ou trabalho futuro.

Itens já mencionados em [`00-escopo.md`](00-escopo.md) (UI, OAuth/OIDC, comparação com monolito,
heterogeneidade de *stack*, nuvem/CI) não são repetidos. Ameaças de validade correlatas estão em
[`04-ameacas-validade.md`](04-ameacas-validade.md).

---

## 1. Erosão arquitetural ao longo de múltiplas iterações

**O que seria.** Observar se, depois de várias mudanças sucessivas no mesmo serviço (não um único
antes/depois), as fronteiras da Clean se mantêm e as da Layered se degradam — a promessa de Martin
sobre o custo da mudança *ao longo do tempo*.

**Por que ficou fora.** Cada experimento (E1, E2, E3, e os pares da Fase 6) é um único par
antes/depois a partir de uma tag congelada. O protocolo de
[`03-protocolo-experimental.md`](03-protocolo-experimental.md) exige aplicar *só* a intervenção
pretendida, sem misturar refatoração. Uma sequência de iterações no mesmo código confundiria a
variável independente (estilo) com aprendizado do implementador e com acúmulo de decisões. O N
temporal seria 1 cadeia por variante, ainda sem poder estatístico.

**Candidato a trabalho futuro?** Sim, com desenho próprio: um *backlog* pré-registrado de N ≥ 5
mudanças heterogêneas (regra, persistência, contrato, resiliência), métrica de violações ArchUnit e
de camadas atingidas *após cada passo*, e regra de parada. Não cabe improvisar isso sobre as
branches `exp/e1-infra`, `exp/e2-falhas` e `exp/e3-regra`, que já são evidência citada.

---

## 2. Quebra de contrato entre serviços

**O que seria.** Remover ou renomear um campo na API do Catalog (ou do Identity) e medir quantos
serviços e quais camadas de cada variante do Agendamento precisam mudar — o eixo de *acoplamento de
contrato* da Dimensão 2 em [`02-criterios-desacoplamento.md`](02-criterios-desacoplamento.md).

**Por que ficou fora.** O E2 mede acoplamento *temporal* (falha síncrona), não acoplamento de
contrato. Catalog e Identity não têm variante Layered; uma mudança de contrato atingiria o
Agendamento nas duas variantes *e* os consumidores reais, misturando a comparação Clean × Layered
com o desenho do sistema. O título do trabalho evoca o “monolito distribuído”; na evidência atual
esse eixo aparece só via fan-out síncrono e taxa sob falha.

**Candidato a trabalho futuro?** Sim (já listado como “E4” em `experiments/fase-5-sintese.md` e em
`04-ameacas-validade.md`). Exigiria um contrato versionado, um campo de fato consumido pelo
Agendamento, e o mesmo protocolo de dois commits nas duas variantes.

**Atualização (23/10/2026): executado.** A renomeação incompatível de um campo consumido (`opensAt` →
`openingTime`) e o controle com um campo não consumido (`capacity` → `seats`) foram executados com
pré-registro próprio ([`../experiments/e4-preregistro.md`](../experiments/e4-preregistro.md)) e protocolo
de dois commits, sem versionamento de contrato. Resultado: custo idêntico no consumidor (1 arquivo, 1 linha),
nenhuma detecção por testes do Agendamento e implantação coordenada obrigatória nas duas variantes
([`../experiments/e4-contrato/protocolo.md`](../experiments/e4-contrato/protocolo.md)). Continua fora a
evolução *compatível* do contrato (*expand-contract*, versionamento, testes de contrato orientados pelo
consumidor), que é o próximo passo natural.

---

## 3. Adição de um novo serviço a um sistema já maduro

**O que seria.** Introduzir um quinto contexto (por exemplo, notificações ou faturamento) no LabManager
já congelado e medir o custo de integração — quantos serviços mudam, se o contrato REST do
Agendamento se mantém, se a Clean facilita ou encarece o *composition root*.

**Por que ficou fora.** O escopo fixou quatro serviços ([`00-escopo.md`](00-escopo.md);
[`01-contextos-delimitados.md`](01-contextos-delimitados.md)). A ADR-003 restringe as duas variantes
ao Agendamento; os demais serviços existem para dar rede e autonomia de dados ao caso de uso
*Solicitar Reserva*, não para variar o estilo interno. Acrescentar um serviço agora alteraria o
sistema-base da evidência já citada (tag `baseline` / `baseline-v2`).

**Candidato a trabalho futuro?** Sim, como estudo de caso de *evolução da decomposição*, distinto da
comparação Clean × Layered. Não deve ser fundido com B1 (variante Layered do Catalog): são perguntas
diferentes (novo contexto × segundo N na mesma célula).

---

## 4. Desempenho sob carga

**O que seria.** Latência, vazão e esgotamento de *threads* com concorrência real — em especial sob
conexão pendurada (cenário S4 do E2-v2), onde o E2 sequencial já sugere ocupação de *thread* até o
timeout do cliente.

**Por que ficou fora.** Critério explícito em [`02-criterios-desacoplamento.md`](02-criterios-desacoplamento.md)
(“O que NÃO é critério neste trabalho”: performance absoluta) e em [`00-escopo.md`](00-escopo.md)
(escalabilidade horizontal / carga real). O E2-v2 usa requisições *sequenciais*; os valores de p50
são comparáveis entre variantes, não representativos de rede ou de produção. Medir carga exigiria
ferramenta de *load test*, orçamento de *threads* do servlet e ambiente isolado — variáveis que o
protocolo atual não controla.

**Candidato a trabalho futuro?** Parcialmente. Carga sob S4 (conexão travada) seria o experimento
mais informativo *para o desacoplamento temporal*, não um *benchmark* de *framework*. Um teste de
vazão “quem é mais rápido, Clean ou Layered?” permanece fora do objetivo da pesquisa.

---

## 5. Comunicação assíncrona

**O que seria.** Substituir (ou complementar) o fan-out HTTP síncrono Identity+Catalog por eventos
(fila/tópico), e medir se a taxa de sucesso do *Solicitar Reserva* sob falha da dependência muda, e
se o custo dessa mudança difere entre as variantes.

**Por que ficou fora.** A ADR de comunicação síncrona fixou HTTP como canal do walking skeleton e dos
experimentos. O E2 existe precisamente para tornar visível o acoplamento temporal *desse* desenho.
Introduzir mensageria no meio da rodada misturaria a variável independente (estilo interno) com um
padrão de integração. A hipótese do E2 já prevê que resiliência e assincronismo são *ortogonais* à
Clean Architecture; testar assincronismo seria um experimento novo, não uma correção do E2.

**Candidato a trabalho futuro?** Sim. É o complemento natural do E2: se a Clean não mitiga o
acoplamento temporal síncrono, a pergunta seguinte é qual padrão de integração mitiga, e se o custo
de adotá-lo difere entre as variantes. Circuit breaker (declarado fora na Fase 6 por suporte ao
Spring Boot 4) cabe no mesmo pacote de resiliência, não no de estilo interno.

---

## Relação com o texto do Capítulo 4

Estes cinco itens **não** enfraquecem os achados já medidos (contenção da troca de persistência,
localidade da regra, indiferença à falha síncrona). Eles delimitam a *generalização analítica*: o
que se observou vale para um serviço de Agendamento, em Java/Spring, com três intervenções pontuais
e comunicação síncrona. Qualquer frase do tipo “a Clean desacopla microsserviços” que ignore esta
lista extrapola além da evidência.
