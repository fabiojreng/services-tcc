# Critérios de avaliação do desacoplamento

Atende ao objetivo específico: *definir critérios para avaliação do nível de desacoplamento*.

O desacoplamento é observado em **três dimensões complementares**, alinhadas à seção 2.5.4 do TCC (interno × entre serviços) e à métrica de propagação usada nos experimentos.

---

## Dimensão 1 — Desacoplamento interno

Mede a organização das dependências **dentro** de um serviço.

| Métrica | Definição | Instrumento |
|---------|-----------|-------------|
| Ca (acoplamento aferente) | Quantidade de componentes que dependem do componente sob análise | ArchUnit — métricas de Martin |
| Ce (acoplamento eferente) | Quantidade de componentes dos quais o componente depende | ArchUnit |
| Instabilidade (I) | `Ce / (Ca + Ce)` | ArchUnit |
| Abstratividade (A) | Proporção de classes abstratas/interfaces (públicas) | ArchUnit |
| Distância da Sequência Principal (D) | `\|A + I − 1\|` | ArchUnit |
| CCD, ACD, RACD, NCCD | Métricas de dependência cumulativa (Lakos) | ArchUnit |
| Violações da Regra da Dependência | Dependências do núcleo para camadas externas | ArchUnit (regras de camada) |
| Contaminação de domínio | Classes de domínio que referenciam tipos de framework/infra | ArchUnit + inspeção |

**Expectativa teórica:** a variante Clean deve apresentar menor contaminação de domínio, D mais próximo de 0 nas camadas centrais e zero (ou perto de zero) violações da Regra da Dependência. A variante layered pode ser funcionalmente equivalente, porém com maior Ce das camadas de negócio em direção a frameworks.

---

## Dimensão 2 — Desacoplamento entre serviços

Mede a autonomia entre microsserviços.

| Métrica | Definição | Como coletar |
|---------|-----------|--------------|
| Fan-out síncrono | Número de chamadas HTTP síncronas por caso de uso | Contagem no código / tracing |
| Acoplamento temporal | Taxa de sucesso do caso de uso quando dependências falham | Experimento E2 |
| Acoplamento de contrato | Campos consumidos ÷ campos expostos do provedor | Inspeção de DTOs/clientes |
| Acoplamento de implantação | Serviços que precisam ser reimplantados por uma mudança | Checklist por experimento |

**Expectativa teórica (importante para a pesquisa):** a Clean Architecture **não** elimina, por si só, acoplamento temporal nem fan-out excessivo. Um resultado em que E2 afeta igualmente as duas variantes é evidência válida das **limitações** da abordagem em sistemas distribuídos.

---

## Dimensão 3 — Propagação de mudança

Métrica central da comparação experimental (Martin: o custo da mudança).

| Métrica | Definição | Instrumento |
|---------|-----------|-------------|
| Arquivos alterados | Quantidade de arquivos no diff | `git diff --numstat` |
| Linhas +/- | Inserções e remoções | `git diff --numstat` |
| Camadas atingidas | Pacotes/módulos tocados (domain, application, adapter, …) | Classificação manual do diff |
| Serviços atingidos | Quantos microsserviços precisaram de alteração | Diff por diretório `services/` |
| Testes quebrados | Testes que falharam após a intervenção, antes do ajuste | Execução `mvnw test` |

### Protocolo de medição via Git

1. Partir da tag de linha de base (`baseline` a partir da Fase 3; na Fase 0 usa-se `fase-0`)
2. Criar branch `experiment/eN-<variante>`
3. Aplicar a intervenção conforme o protocolo
4. Registrar `git diff --numstat <tag>...HEAD` e resultado dos testes
5. Não misturar refatorações cosméticas com a intervenção

---

## Comparação entre variantes

Para cada experimento E1–E3, as mesmas métricas são coletadas em:

- `scheduling-service-clean` (tratamento)
- `scheduling-service-layered` (controle)

A análise combina:

- **Quantitativo:** tabelas de métricas e diffs
- **Qualitativo:** interpretação das fronteiras e das dependências (estudo de caso)

---

## O que NÃO é critério neste trabalho

- Performance absoluta (latência/throughput sob carga)
- Popularidade ou ergonomia subjetiva do estilo arquitetural
- Número de microsserviços (fixo; a qualidade da decomposição é o foco)
