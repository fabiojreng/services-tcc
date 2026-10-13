# E3 — Custo de localização versus custo de modificação

No E3 as duas variantes alteram **o mesmo número de arquivos de produção** (1). Este documento
separa esse empate quantitativo em dois custos que o `git diff --numstat` mistura: **localizar** a
regra e **modificá-la**. Não há avaliador independente; a contagem de saltos é reproduzível a partir
do código e do protocolo abaixo. Fonte do código: tag `baseline` / HEAD atual (a estrutura é a mesma
em `exp/e3-regra`; só o literal 24→48 muda).

Hipótese de fundo ([`docs/03-protocolo-experimental.md`](../docs/03-protocolo-experimental.md)):
alterar a antecedência mínima deve concentrar mudanças no núcleo na Clean; na Layered a regra *pode*
estar espalhada. O espalhamento em *arquivos* não se materializou. A pergunta que resta é se um
desenvolvedor **sem contexto prévio** chega à constante `MINIMUM_LEAD_TIME` com o mesmo esforço.

---

## Protocolo de contagem (reproduzível)

Ponto de partida: o contrato REST `POST /api/reservations` (único endpoint de solicitação).

Um **salto** é uma navegação do tipo “ir para a declaração” (IDE: *Go to declaration* / Ctrl+clique)
que muda de **arquivo**, **classe** ou **método**. Saltos *dentro do mesmo arquivo* para outro método
ou para o campo são contados à parte, porque não exigem abrir outro arquivo.

Hipótese de trabalho do navegador: a pessoa não conhece o nome `MINIMUM_LEAD_TIME` de antemão; segue
a cadeia de chamadas a partir do controller. A busca textual é um atalho registrado na seção 4, não
o caminho principal.

---

## Variante Clean — cadeia a partir do endpoint

| Passo | Arquivo | Classe | Método / membro | O que se lê |
|-------|---------|--------|-----------------|-------------|
| 0 | `services/scheduling-service-clean/presentation/src/main/java/br/edu/ifma/labmanager/scheduling/presentation/routes/ReservationController.java:26-35` | `ReservationController` | `request` | O endpoint **não** contém a regra. Delega a `requestReservationUseCase.execute`. |
| 1 | `services/scheduling-service-clean/application/src/main/java/br/edu/ifma/labmanager/scheduling/application/use_cases/RequestReservationUseCase.java:36-51` | `RequestReservationUseCase` | `execute` | Orquestra Identity, Catalog e repositório. **Não menciona** `ReservationPolicy` nem `MINIMUM_LEAD_TIME`. A criação passa por `ReservationFactory.createRequest`. |
| 2 | `services/scheduling-service-clean/domain/src/main/java/br/edu/ifma/labmanager/scheduling/domain/entities/ReservationFactory.java:19-28` | `ReservationFactory` | `createRequest` | Uma linha: `ReservationPolicy.validate(...)`. Ainda não se vê a constante. |
| 3 | `services/scheduling-service-clean/domain/src/main/java/br/edu/ifma/labmanager/scheduling/domain/policy/ReservationPolicy.java:31-48` | `ReservationPolicy` | `validate` | Uso de `MINIMUM_LEAD_TIME` na linha 46. |
| 3b (intra-arquivo) | mesmo arquivo, linha 25 | `ReservationPolicy` | campo `MINIMUM_LEAD_TIME` | `Duration.ofHours(24)` (na `exp/e3-regra`: 48). |

**Saltos inter-arquivo:** 3 (`Controller` → `UseCase` → `Factory` → `Policy`).
**Saltos intra-arquivo até a constante:** 1 (de `validate` para o campo, ~20 linhas acima).
**Arquivos abertos até ver o valor numérico:** 4.
**Módulos Maven cruzados:** `presentation` → `application` → `domain`.

Trecho em que a cadeia *esconde* a política (o caso de uso não nomeia a regra):

```44:51:services/scheduling-service-clean/application/src/main/java/br/edu/ifma/labmanager/scheduling/application/use_cases/RequestReservationUseCase.java
        Reservation reservation = ReservationFactory.createRequest(
                command.laboratoryId(),
                command.requesterId(),
                command.toSlot(),
                operatingHours,
                clock.now(),
                existing
        );
```

```27:28:services/scheduling-service-clean/domain/src/main/java/br/edu/ifma/labmanager/scheduling/domain/entities/ReservationFactory.java
        ReservationPolicy.validate(slot, operatingHours, now, existingForLaboratory);
        return Reservation.request(laboratoryId, requesterId, slot);
```

```25:48:services/scheduling-service-clean/domain/src/main/java/br/edu/ifma/labmanager/scheduling/domain/policy/ReservationPolicy.java
    public static final Duration MINIMUM_LEAD_TIME = Duration.ofHours(24);
    // ...
        if (Duration.between(now, requested.start()).compareTo(MINIMUM_LEAD_TIME) < 0) {
            throw new DomainException("A reserva exige antecedência mínima de 24 horas");
        }
```

Observação: o javadoc da própria política (linhas 14–21) lista “Antecedência mínima de 24 horas”
como item 3. Quem **já está** neste arquivo encontra o valor imediatamente. O custo está em *chegar*
ao arquivo, não em reconhecê-lo.

---

## Variante Layered — cadeia a partir do endpoint

| Passo | Arquivo | Classe | Método / membro | O que se lê |
|-------|---------|--------|-----------------|-------------|
| 0 | `services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/web/ReservationController.java:21-28` | `ReservationController` | `request` | Delega a `reservationService.requestReservation`. Sem regra. |
| 1 | `services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/service/ReservationService.java:42-54` | `ReservationService` | `requestReservation` | No **mesmo** método: chamada HTTP a Identity, chamada HTTP a Catalog, depois `validateRules`. Persistência JPA nas linhas seguintes. |
| 2 (intra-arquivo, outro método) | mesmo arquivo, linhas 69–86 | `ReservationService` | `validateRules` | Uso de `MINIMUM_LEAD_TIME` na linha 84. |
| 2b (intra-arquivo, campo) | mesmo arquivo, linha 25 | `ReservationService` | campo `MINIMUM_LEAD_TIME` | `Duration.ofHours(24)` (na `exp/e3-regra`: 48). |

**Saltos inter-arquivo:** 1 (`Controller` → `Service`).
**Saltos intra-arquivo até a constante:** 2 (`requestReservation` → `validateRules` → campo).
**Arquivos abertos até ver o valor numérico:** 2.
**Pacotes cruzados:** `web` → `service`.

```21:29:services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/web/ReservationController.java
    @PostMapping
    public ResponseEntity<ReservationResponse> request(@RequestBody RequestReservationRequest body) {
        ReservationResponse response = reservationService.requestReservation(
                body.laboratoryId(),
                body.requesterId(),
                body.start(),
                body.end()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
```

```42:54:services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/service/ReservationService.java
    @Transactional
    public ReservationResponse requestReservation(
            UUID laboratoryId,
            String requesterId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (!identityClient.hasPermission(requesterId, "RESERVATION_REQUEST")) {
            throw new ReservationBusinessException("Usuário sem permissão RESERVATION_REQUEST");
        }

        CatalogClient.LaboratoryHours hours = catalogClient.getLaboratory(laboratoryId);
        validateRules(laboratoryId, start, end, hours);
```

```84:86:services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/service/ReservationService.java
        if (Duration.between(LocalDateTime.now(), start).compareTo(MINIMUM_LEAD_TIME) < 0) {
            throw new ReservationBusinessException("A reserva exige antecedência mínima de 24 horas");
        }
```

Quem abre `ReservationService` vê, no mesmo arquivo, clientes HTTP, `@Transactional`, entidade JPA e
a constante. Localizar a regra exige **menos arquivos**; isolá-la visualmente exige **filtrar** o
resto do `@Service`.

---

## Comparativo

| Medida | Clean | Layered |
|--------|-------|---------|
| Saltos inter-arquivo (endpoint → constante) | **3** | **1** |
| Arquivos abertos | **4** | **2** |
| Métodos distintos na cadeia | 4 (`request`, `execute`, `createRequest`, `validate`) | 3 (`request`, `requestReservation`, `validateRules`) |
| A constante está no arquivo que orquestra HTTP/persistência? | Não | Sim |
| Relógio | injetado (`ClockPort.now()`), testável sem `now()` de sistema | `LocalDateTime.now()` dentro de `validateRules` (linha 84) |
| Arquivos de produção alterados no E3 | 1 (`ReservationPolicy.java`, 3+/3−) | 1 (`ReservationService.java`, 2+/2−) |
| Testes de unidade da regra | 7 em `ReservationPolicyTest` (relógio fixo `2026-09-07T10:00`) | nenhum |

**Leitura.** “Localizar” a partir do REST, seguindo chamadas, é **mais longo** na Clean (um hop extra
pelo *use case* e outro pela *factory*, e o *use case* não nomeia a política). “Localizar” no sentido
de *a regra tem um lugar com nome e sem HTTP/JPA* é **mais curto** na Clean: o arquivo
`ReservationPolicy` contém só política. A Layered ganha no caminho REST→arquivo e perde no
reconhecimento: a constante convive com orquestração.

O empate em arquivos alterados (E3) mede o **custo de modificação desta intervenção pontual**. Não
mede o custo de chegar à linha. São grandezas diferentes; reportá-las juntas como “a Clean não
ganhou no E3” omite a nuance.

---

## Atalho por busca textual (não é o caminho pedido, mas é o que muitos desenvolvedores fazem)

Busca por `MINIMUM_LEAD_TIME` e por `antecedência mínima` no diretório `services/` (HEAD atual):

| Termo | Clean | Layered |
|-------|-------|---------|
| `MINIMUM_LEAD_TIME` | 1 arquivo de produção (`ReservationPolicy.java`, linhas 25 e 46) | 1 arquivo de produção (`ReservationService.java`, linhas 25 e 84) |
| `antecedência mínima` | o mesmo arquivo (mensagem + javadoc) | o mesmo arquivo (mensagem) |

Com busca, as duas variantes empatam em **zero saltos de cadeia**: um *grep* abre o arquivo certo.
A diferença reaparece *depois* de abrir: na Clean o arquivo tem 62 linhas e só regras; na Layered
tem 127 linhas, misturando HTTP, JPA e regras. Esse atalho **depende de conhecer o identificador ou
o texto da mensagem**. O protocolo desta nota assume que o desenvolvedor não os conhece e parte do
endpoint.

---

## Relação com testes (detecção, não localização)

A consolidação em [`evidencias-capitulo-4.md`](evidencias-capitulo-4.md) (execução 12/10/2026,
segunda-feira) mediu o estado P (só produção, testes da `baseline`):

- Clean: 3 quebras determinísticas em `ReservationPolicyTest` + 1 quebra de aceitação dependente de calendário.
- Layered: 0 quebra de unidade (não há teste da regra) + 1 quebra de aceitação dependente de calendário.

Localizar mais rápido (Layered, caminho REST) não implica **detectar** a mudança. Detectar, neste
desenho, exige teste de unidade com relógio controlado — presente só na Clean.
