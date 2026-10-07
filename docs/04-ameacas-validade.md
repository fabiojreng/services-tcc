# Ameaças à validade

Registro explícito das ameaças (Wohlin et al.; Runeson e Höst) e mitigações adotadas neste estudo.

## Validade de construção

| Ameaça | Descrição | Mitigação |
|--------|-----------|-----------|
| Métrica inadequada de “desacoplamento” | Usar só contagem de serviços ou pastas | Três dimensões com instrumentos objetivos (ArchUnit, Git diff, taxa sob falha) |
| Clean Architecture “de fachada” | Pastas sem Regra da Dependência | Submódulos Maven: `domain` sem Spring/JPA no classpath |
| Grupo de controle fraco | Layered propositalmente ruim (“espantalho”) | Implementação competente do estilo Spring idiomático; mesma suíte de aceitação |

## Validade interna

| Ameaça | Descrição | Mitigação |
|--------|-----------|-----------|
| Confusão de variáveis | Diferenças de feature entre variantes | Contrato REST idêntico; paridade funcional antes dos experimentos |
| Viés do implementador | Autor favorece a variante Clean | Protocolo fixo de intervenção; diffs objetivos; hipóteses pré-registradas incluindo resultado negativo em E2 |
| Aprendizado entre variantes | Segunda implementação “melhor” por experiência | Ordem: Clean primeiro (Fase 0/1); Layered depois (Fase 2) com checklist de paridade; não portar refatorações extras |

## Validade externa

| Ameaça | Descrição | Mitigação |
|--------|-----------|-----------|
| Domínio específico | Gestão de laboratórios pode não generalizar | Estudo de caso exploratório — generalização analítica, não estatística |
| Stack única | Só Java/Spring | Limitar conclusões ao ecossistema estudado; discutir na análise |
| Escala pequena | Poucos serviços e um caso de uso rico | Adequado ao TCC; limitações declaradas na conclusão |

## Validade de conclusão

| Ameaça | Descrição | Mitigação |
|--------|-----------|-----------|
| N=1 por célula | Uma implementação por estilo | Transparência: evidência qualitativa + quantitativa descritiva, sem inferência estatística forte |
| Cherry-picking de métricas | Reportar só o que favorece Clean | Bateria fixa de métricas em [02-criterios-desacoplamento.md](02-criterios-desacoplamento.md); E2 como teste de limitação |

## Ameaças específicas deste desenho

1. **Comparar um serviço, não o sistema inteiro**: decisão deliberada para reduzir o escopo. As conclusões sobre Clean × Layered referem-se ao Agendamento, e qualquer extrapolação é feita com cautela.
2. **E2 original baseado em stubs (Fase 4)**: os testes `*FailureSimulationTests` verificavam o mapeamento da exceção para 503, não o comportamento sob falha de rede, o que era quase tautológico. **Mitigação (Fase 6):** E2-v2 com Toxiproxy real (latência, recusa de conexão e conexão pendurada), com latência e taxa de erro medidas. Os testes com stub foram reclassificados como "teste de mapeamento de erro".
3. **Comunicação só síncrona**: o acoplamento temporal fica visível. Eventos assíncronos são discutidos como trabalho futuro, não como variável oculta.

## Reforço metodológico (Fase 6)

| Ameaça | Situação nas Fases 4–5 | Mitigação na Fase 6 |
|--------|------------------------|---------------------|
| Viés do implementador / ajuste *post hoc* | Hipóteses existiam, mas sem previsões numéricas | Pré-registro com previsões verificáveis, commitado antes da execução (`experiments/fase-6-preregistro.md`) |
| "Testes quebrados" contaminado | Produção e testes ajustados juntos | Protocolo de dois commits (P: produção, T: testes); quebras registradas entre P e T |
| E1 sem banco real | Repositório em memória / `@MockitoBean` | Testcontainers com `mongo:7`; o banco real revelou dois defeitos que o mock escondia |
| E3 pouco discriminante | Troca de constante | E3b (cota semanal), que exige consulta nova e invariante nova |
| ArchUnit só na baseline | Métricas não reavaliadas | Exportação com rótulo após cada experimento e variante |
| Viés do implementador (avaliação) | Só o autor avaliou | Protocolo de revisão cega por terceiro (`05-revisao-cega.md`); execução pendente |
| Aprendizado entre variantes | — | A Clean foi executada primeiro em cada experimento. No E1-v2, a correção descoberta na Clean (UUID) foi aplicada à Layered na mesma sequência P1 → P2, e isso foi declarado |
| Artefato do instrumento | — | Variação de CCD na Layered (E1-v2) sem dependência nova no bytecode, tratada como inconclusiva |
| Desvio de amostra no E2-v2 | — | S4 com N = 10 em vez de 30, por custo de tempo. O resultado foi determinístico (10/10 em todas as células) |
| Hipóteses pouco falsificáveis | — | Nenhuma previsão foi refutada frontalmente. Isso pode indicar hipóteses conservadoras; declarado em `experiments/fase-6-sintese.md` |
| Ambiente de E2 local | Stubs em processo | Proxy, serviços e cliente na mesma máquina: os valores absolutos de latência não representam rede real, mas a comparação entre variantes se mantém |

## O que **não** foi testado (limitações declaradas)

- **Replicação (N = 2):** o protocolo não foi repetido num segundo serviço, como uma variante Layered do Catalog. Os resultados descrevem **um** caso.
- **Mudança de contrato entre serviços (E4):** não se mediu quantos serviços e camadas uma mudança no contrato do Catalog atinge. O eixo "monolito distribuído" do título é avaliado só pelo acoplamento temporal (E2), não pelo acoplamento de contrato.
- **Erosão ao longo do tempo:** cada experimento é um único antes/depois. A promessa de Martin sobre o custo de mudança "ao longo do tempo" exigiria várias iterações sucessivas.
- **Carga e concorrência:** o E2-v2 usa requisições sequenciais. Esgotamento de *threads* sob conexões penduradas não foi medido.
- **Entrada de um novo serviço** no sistema já maduro.
- **Circuit breaker:** só o timeout foi introduzido na fase B do E2-v2.
- **Dependência de calendário na suíte de aceitação:** a suíte escolhe "a próxima quarta-feira com pelo menos 2 dias de distância". Com a antecedência de 48 h, ela quebraria se executada numa segunda-feira depois das 10:00. Esse defeito do instrumento é independente da arquitetura.

## Registro de decisões relacionadas

Ver ADRs em [`adr/`](adr/).
