# Gabarito da revisão cega (E3) — não distribuir junto com as variantes

Abrir **somente depois** de o revisor devolver as duas avaliações.

| Arquivo | Identidade | Serviço / caminho original |
|---------|------------|----------------------------|
| `revisao-cega-e3-variante-a.md` | **Layered** (controle) | `services/scheduling-service-layered/.../service/ReservationService.java` (HEAD / `baseline`; na `exp/e3-regra` o literal é 48 em vez de 24) |
| `revisao-cega-e3-variante-b.md` | **Clean** (tratamento) | `services/scheduling-service-clean/.../domain/policy/ReservationPolicy.java` |

## O que foi omitido ou renomeado (só na variante A)

Para não revelar o pacote `layered` nem anotações Spring/JPA:

| Original | No material cego |
|----------|------------------|
| pacote `br.edu.ifma.labmanager.scheduling.layered.service` | omitido |
| `@Service`, `@Transactional` | omitidos |
| `ReservationJpaRepository` | `ReservationStore` |
| `ReservationEntity` | `ReservationRecord` |
| javadoc “Estilo em camadas idiomático Spring…” | omitido |

Na variante B omitiram-se apenas pacote, *imports* e o javadoc da Fase 0. Nenhum identificador de
tipo foi trocado. Os textos entregues ao revisor não nomeiam JPA, Spring, Clean nem Layered; a
correspondência dos nomes trocados fica só nesta tabela.

Limitação: os nomes `ReservationService` e `ReservationPolicy` permanecem. Quem já leu o código do
TCC identifica a variante por esses nomes. Renomeá-los seria outra decisão de instrumento, e não
foi feita.

## O que o revisor *não* vê (e que entra na discussão, não no instrumento)

- Na Clean, chegar a `ReservationPolicy` a partir de `POST /api/reservations` exige três saltos
  inter-arquivo (`Controller` → `RequestReservationUseCase` → `ReservationFactory` → `Policy`). Ver
  [`e3-localizacao-vs-modificacao.md`](e3-localizacao-vs-modificacao.md).
- Na Layered, um salto inter-arquivo chega ao arquivo que contém a constante *e* as chamadas HTTP.
- Só a Clean tem testes de unidade da regra (`ReservationPolicyTest`, relógio fixo).

## Uso sugerido

Perguntas iguais para A e B, nesta ordem:

1. Em quantos minutos você apontaria a linha que precisa mudar para 48 h?
2. A regra está misturada com I/O (HTTP, banco, relógio de sistema)?
3. Você alteraria só essa constante com confiança, sem suíte de aceitação?

Não há avaliação independente coletada neste repositório. O commit `782c1de` retirou um protocolo
prévio de revisão cega e registrou a ausência como ameaça à validade. Estes três arquivos são
**material**, não resultado.
