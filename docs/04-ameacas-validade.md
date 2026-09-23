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

1. **Comparar um serviço, não o sistema inteiro** — deliberado para reduzir escopo; conclusões sobre Clean × Layered referem-se ao Agendamento, com extrapolação cuidadosa.
2. **Ausência de Docker na Fase 0** — não afetou a linha de base de métricas internas; E1/E2 foram executados a partir da Fase 3 (`baseline`). A execução principal do E2 usou stubs equivalentes; Toxiproxy permanece opcional para reforço.
3. **Comunicação só síncrona no início** — acoplamento temporal fica visível; eventos assíncronos podem ser discutidos como trabalho futuro, não como variável oculta.

## Registro de decisões relacionadas

Ver ADRs em [`adr/`](adr/).
