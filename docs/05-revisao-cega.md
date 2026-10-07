# Protocolo de revisão cega por terceiro

Instrumento complementar para reduzir o **viés do implementador** (validade interna).
Um avaliador externo (orientador ou colega com experiência em Java/Spring) compara as duas
variantes **sem saber qual é a Clean e qual é a Layered**.

## Preparação (pelo autor)

1. Gerar dois pacotes anonimizados a partir da tag `baseline-v2`:
   - **Versão A** e **Versão B**: cópia de `services/scheduling-service-clean/` e de
     `services/scheduling-service-layered/`, atribuídas a A/B por sorteio (moeda), com o resultado
     anotado em envelope lacrado ou em arquivo com *hash* registrado antes da avaliação.
   - Renomear pastas e o pacote raiz para nomes neutros (`servico-a`, `servico-b`) e remover
     READMEs, comentários de cabeçalho e javadoc que citem "Clean", "Layered", "camadas" ou "hexagonal".
   - Manter o mesmo contrato REST e a mesma suíte de aceitação nas duas versões.
2. Entregar ao avaliador as duas pastas, a descrição funcional do caso de uso "Solicitar Reserva"
   (regras de negócio, sem menção a arquitetura) e o questionário abaixo.
3. Não responder a perguntas sobre a estrutura durante a avaliação.

## Tarefas do avaliador (tempo sugerido: 40 a 60 minutos)

Para **cada** versão, nesta ordem:

| # | Tarefa | Registro |
|---|--------|----------|
| T1 | Localizar onde está a regra "antecedência mínima de 24 h" | Tempo até localizar (min) e arquivos abertos |
| T2 | Descrever, sem alterar código, o que seria preciso para trocar o banco relacional por MongoDB | Lista de arquivos que tocaria |
| T3 | Descrever o que seria preciso para limitar cada usuário a 3 reservas por semana | Lista de arquivos que tocaria |
| T4 | Descrever como o serviço se comporta se o Catalog parar de responder | Texto livre |

A ordem das versões é alternada entre avaliadores (A→B e B→A) quando houver mais de um.

## Questionário (escala de 1 a 5, de "discordo totalmente" a "concordo totalmente")

1. Foi fácil entender a organização do código desta versão.
2. Foi fácil localizar onde as regras de negócio estão implementadas.
3. Eu me sentiria seguro alterando uma regra de negócio nesta versão.
4. Eu me sentiria seguro trocando a tecnologia de persistência nesta versão.
5. A quantidade de arquivos e pacotes é adequada ao tamanho do problema.

Pergunta aberta final: *"Qual versão você manteria num projeto real e por quê?"*

## Análise

- Comparar as respostas de T2 e T3 com os diffs reais de E1-v2 e E3b (acerto de previsão).
- Comparar o tempo de T1 entre as versões. É o "custo de localizar", que os diffs não medem
  (ver nuance em `experiments/fase-6-sintese.md`).
- Reportar de forma descritiva (com N de 1 a 3 avaliadores, não cabe inferência estatística).
- Revelar o sorteio só depois de registradas todas as respostas.

## Situação

Protocolo definido; **execução pendente** e dependente da disponibilidade de avaliadores.
Se não for executado antes da defesa, entra como trabalho futuro nas ameaças à validade.
