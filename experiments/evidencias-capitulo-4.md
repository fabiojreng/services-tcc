# Evidências do Capítulo 4 — E1, E2 e E3 (rodada da Fase 4)

Consolidação das evidências dos três experimentos originais, a partir da tag `baseline` (`5304b47`) e das
branches `exp/e1-infra` (`a22b8e5`), `exp/e2-falhas` (`319af0e`) e `exp/e3-regra` (`26f259e`).

> **Relação com a Fase 6.** Esta consolidação cobre a rodada original. Os mesmos experimentos foram
> replicados na Fase 6 (`baseline-v2`, branches `exp2/*`, protocolo de dois commits, MongoDB real e
> Toxiproxy real) e [`fase-6-sintese.md`](fase-6-sintese.md) prevalece onde houver divergência. A seção 5
> deste documento põe os números das duas rodadas lado a lado. A seção 4 registra dois achados novos desta
> consolidação que afetam o texto já escrito.

---

## 0. Como estas evidências foram coletadas (12/10/2026)

Todos os números de testes abaixo foram obtidos **nesta data**, executando a suíte. Nenhum foi copiado das
notas da Fase 4. Nenhuma branch ou tag existente foi alterada. Os `git diff --numstat baseline..exp/eN-*`
foram reexecutados na mesma data e conferem com as saídas das seções 1.3, 2.3 e 3.3.

| Item | Valor |
|------|-------|
| Ambiente | Windows 10.0.26300, Java HotSpot 25.0.1+8-LTS-27, Maven Wrapper do repositório |
| Docker | **Indisponível** (`failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine`) |
| Comando | `mvnw -B clean test -fae -Dmaven.test.failure.ignore=true` no reator inteiro (todos os serviços + `architecture-metrics`) |
| Isolamento | Uma `git worktree --detach` por estado, em `C:\tmp\tcc-wt\` (fora do OneDrive) |
| Script | [`evidencias-capitulo-4/execucao-2026-10-12/run-state.ps1`](evidencias-capitulo-4/execucao-2026-10-12/run-state.ps1) |
| Saída bruta | `evidencias-capitulo-4/execucao-2026-10-12/<estado>/` (`summary.txt`, `mvn.log`, `prod.patch`, `applied-numstat.txt`) |

### Estados medidos

| Estado | Definição | Horário da execução |
|--------|-----------|---------------------|
| **B** — antes | `baseline` | 11:40:01 |
| **P** — só produção | `baseline` + `git diff baseline exp/eN -- 'services/*/src/main/*' 'services/*pom.xml' 'infra/*'` aplicado na worktree (**sem commit**; testes e suíte de aceitação permanecem os da `baseline`) | E1 11:43:17 · E3 11:43:56 |
| **T** — depois | ponta da branch `exp/eN-*` (produção + testes ajustados no mesmo commit, como na Fase 4) | E1 11:40:49 · E2 11:41:31 · E3 11:42:11 |

O estado **P** não existia na Fase 4: cada branch `exp/eN-*` é **um único commit** que mistura produção,
testes e artefatos. A contagem de "testes quebrados antes do ajuste" da Fase 4 nunca foi medida
(as notas do E3 Clean dizem "quebraria", no condicional). O estado P reconstitui essa medição sem tocar nas
branches. No E2 não há diff de produção, então P = B.

O arquivo `applied-numstat.txt` de cada P confirma que o patch aplicado é idêntico às linhas de produção do
`git diff --numstat` da branch.

### Linha de base (estado B) — vale para os três experimentos

`BUILD SUCCESS`, 28 testes, 0 falhas, 0 erros. Recorte do Agendamento:

| Suíte | Testes | Resultado |
|-------|--------|-----------|
| `scheduling-clean-domain` · `ReservationPolicyTest` | 7 | 7 ok |
| `scheduling-clean-infra` · `SchedulingCleanApplicationTests` (aceitação compartilhada) | 1 | 1 ok |
| `scheduling-service-layered` · `SchedulingLayeredApplicationTests` (aceitação compartilhada) | 1 | 1 ok |
| `architecture-metrics` · `SchedulingCleanArchitectureRulesTest` / `SchedulingLayeredArchitectureRulesTest` | 4 / 2 | ok |

Os demais 13 testes (Identity, Catalog, Inventory e as regras ArchUnit deles) passaram em **todos** os estados
medidos e não são repetidos abaixo.

---

## 1. E1 — Troca de persistência JPA → MongoDB (`exp/e1-infra`)

### 1.1 Hipótese registrada

[`docs/03-protocolo-experimental.md`](../docs/03-protocolo-experimental.md), linha 25:

> na variante Clean, trocar o mecanismo de persistência altera principalmente `infra` (e eventualmente
> `presentation`); na variante Layered, a mudança propaga para camadas de negócio/API com maior frequência.

### 1.2 Código relevante

**Clean — a porta que deveria absorver a troca (inalterada):**
`services/scheduling-service-clean/domain/src/main/java/br/edu/ifma/labmanager/scheduling/domain/repository/ReservationRepository.java:10-17`

```java
public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(ReservationId id);
    List<Reservation> findByLaboratoryId(LaboratoryId laboratoryId);
}
```

**Clean — modelo de persistência, baseline:**
`services/scheduling-service-clean/infra/src/main/java/br/edu/ifma/labmanager/scheduling/infra/models/ReservationJpaEntity.java:14-35`

```java
@Entity
@Table(name = "reservations")
public class ReservationJpaEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID laboratoryId;
    // ... demais campos com @Column(nullable = false); status com @Enumerated(EnumType.STRING)
```

**Clean — modelo de persistência, branch (arquivo renomeado):**
`services/scheduling-service-clean/infra/src/main/java/br/edu/ifma/labmanager/scheduling/infra/models/ReservationDocument.java:10-19`

```java
@Document(collection = "reservations")
public class ReservationDocument {
    @Id
    private UUID id;
    private UUID laboratoryId;
    private String requesterId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;
```

**Clean — composition root, branch (linha introduzida pela intervenção):**
`services/scheduling-service-clean/infra/src/main/java/br/edu/ifma/labmanager/scheduling/infra/config/SchedulingConfig.java:29-33`

```java
@Bean
@ConditionalOnBean(SpringDataReservationRepository.class)
ReservationRepository reservationRepository(SpringDataReservationRepository springData) {
    return new MongoReservationRepository(springData);
}
```

**Layered — serviço de negócio, baseline:**
`services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/service/ReservationService.java:7-10,28,42-43`

```java
import br.edu.ifma.labmanager.scheduling.layered.repository.ReservationJpaRepository;
// ...
import org.springframework.transaction.annotation.Transactional;
// ...
private final ReservationJpaRepository repository;
// ...
@Transactional
public ReservationResponse requestReservation(
```

**Layered — serviço de negócio, branch:**
`services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/service/ReservationService.java:7,17-20,27,41`

```java
import br.edu.ifma.labmanager.scheduling.layered.repository.ReservationMongoRepository;
// ...
/**
 * Estilo em camadas idiomático Spring: regras de negócio e chamadas HTTP no @Service,
 * documento Mongo anêmico atravessando camadas (E1: JPA → MongoDB).
 */
// ...
private final ReservationMongoRepository repository;
// ...
public ReservationResponse requestReservation(      // @Transactional removido
```

A alteração do javadoc (linhas 17-20) é cosmética e não era necessária para a intervenção. Isso viola a
regra "não misturar refatoração cosmética com a intervenção" e entra na contagem de linhas da Layered
(1+/1−). No pom da Clean houve remoção de comentário (2 linhas), também cosmética. A Fase 6 já registrou esse
ruído (`e1-infra-v2/clean/notas.md`, linha 49).

**Testes ajustados na branch — o Mongo é excluído dos testes nas duas variantes:**
`services/scheduling-service-clean/infra/src/test/java/br/edu/ifma/labmanager/scheduling/infra/SchedulingCleanApplicationTests.java:26-31,52-75`
e
`services/scheduling-service-layered/src/test/java/br/edu/ifma/labmanager/scheduling/layered/SchedulingLayeredApplicationTests.java:30-35,42-61`

```java
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
                + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
                + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration"
})
// Clean: @Bean @Primary ReservationRepository em memória (ConcurrentHashMap)
// Layered: @MockitoBean ReservationMongoRepository com save/findByLaboratoryId respondidos por um Map
```

### 1.3 `git diff --numstat` (saída real)

```
$ git diff --numstat baseline..exp/e1-infra -- services/scheduling-service-clean
2	14	services/scheduling-service-clean/infra/pom.xml
0	4	services/scheduling-service-clean/infra/src/main/java/br/edu/ifma/labmanager/scheduling/infra/SchedulingCleanApplication.java
4	2	services/scheduling-service-clean/infra/src/main/java/br/edu/ifma/labmanager/scheduling/infra/config/SchedulingConfig.java
6	22	services/scheduling-service-clean/infra/src/main/java/br/edu/ifma/labmanager/scheduling/infra/models/{ReservationJpaEntity.java => ReservationDocument.java}
12	13	services/scheduling-service-clean/infra/src/main/java/br/edu/ifma/labmanager/scheduling/infra/repositories/{JpaReservationRepository.java => MongoReservationRepository.java}
4	4	services/scheduling-service-clean/infra/src/main/java/br/edu/ifma/labmanager/scheduling/infra/repositories/SpringDataReservationRepository.java
0	10	services/scheduling-service-clean/infra/src/main/resources/application-postgres.properties
3	10	services/scheduling-service-clean/infra/src/main/resources/application.properties
39	1	services/scheduling-service-clean/infra/src/test/java/br/edu/ifma/labmanager/scheduling/infra/SchedulingCleanApplicationTests.java

$ git diff --numstat baseline..exp/e1-infra -- services/scheduling-service-layered
2	12	services/scheduling-service-layered/pom.xml
3	19	services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/entity/ReservationEntity.java
2	2	services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/repository/{ReservationJpaRepository.java => ReservationMongoRepository.java}
4	6	services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/service/ReservationService.java
0	10	services/scheduling-service-layered/src/main/resources/application-postgres.properties
3	10	services/scheduling-service-layered/src/main/resources/application.properties
36	1	services/scheduling-service-layered/src/test/java/br/edu/ifma/labmanager/scheduling/layered/SchedulingLayeredApplicationTests.java

$ git diff --numstat baseline..exp/e1-infra -- infra
10	0	infra/docker-compose.yml
```

Nenhuma linha em `services/identity-service`, `services/catalog-service`, `services/inventory-service` ou
`acceptance`.

### 1.4 Testes antes e depois

| Estado | Clean (8 testes) | Layered (1 teste) | ArchUnit |
|--------|------------------|-------------------|----------|
| B | 8/8 ok | 1/1 ok | ok |
| **P** (só produção) | **7/8** — `SchedulingCleanApplicationTests.solicitaReservaComSucesso` **ERROR** | **0/1** — `SchedulingLayeredApplicationTests.solicitaReservaComSucessoParidadeComClean` **ERROR** | ok |
| T (branch) | 8/8 ok | 1/1 ok | ok |

Mensagens relevantes do estado P (`execucao-2026-10-12/e1-prod/summary.txt` e `mvn.log`):

- **Clean:** `java.lang.IllegalStateException: Failed to load ApplicationContext` ← causa raiz
  `NoSuchBeanDefinitionException: No qualifying bean of type
  'br.edu.ifma.labmanager.scheduling.domain.repository.ReservationRepository' available`.
- **Layered:** `jakarta.servlet.ServletException: Request processing failed:
  org.springframework.dao.DataAccessResourceFailureException: Timed out while waiting for a server that matches
  ReadPreferenceServerSelector{readPreference=primary} ... servers=[{address=localhost:27017, ... MongoSocketOpenException}]`.

Os dois testes quebram por **motivos de natureza diferente**:

- Na Layered, a falha é **de ambiente**: o contexto sobe e só a consulta falha, porque não há Mongo em
  `localhost:27017` (Docker indisponível).
- Na Clean, a falha ocorre **na montagem do contexto**, antes de qualquer conexão. Ela é um defeito de
  produção introduzido pela intervenção (ver 4.1).

Verificação adicional, executando os apps do estado P fora dos testes (`e1-prod/run-*.out.log`):

| App (estado P, sem Mongo) | Resultado |
|---------------------------|-----------|
| `scheduling-clean-infra-0.1.0-SNAPSHOT-boot.jar` | `APPLICATION FAILED TO START` — *Parameter 0 of method requestReservationUseCase in ...SchedulingConfig required a bean of type '...ReservationRepository' that could not be found.* |
| `SchedulingLayeredApplication` (classpath do Maven) | `Started SchedulingLayeredApplication in 1.935 seconds`; erros de conexão só no monitor do driver Mongo, em segundo plano |

No estado T, os dois testes passam porque a autoconfiguração do Mongo é excluída e o repositório é
substituído por um stub em memória (1.2). Ou seja, **a suíte da branch `exp/e1-infra` não exercita o
código Mongo de produção** em nenhuma das variantes.

### 1.5 Tabela resumo — E1

| | Clean | Layered |
|--|-------|---------|
| Arquivos de produção | 8 | 6 |
| Linhas de produção (+/−) | 31 / 79 | 14 / 59 |
| Arquivos de teste | 1 (39 / 1) | 1 (36 / 1) |
| Camadas de produção atingidas | `infra` (código, pom, properties) | `entity`, `repository`, `service` (+ pom, properties) |
| Camadas de negócio atingidas | nenhuma (`domain`, `application`, `presentation` intactos) | `service` (tipo do repositório, `@Transactional`, javadoc) |
| Serviços atingidos | 1 (`scheduling-service-clean`) | 1 (`scheduling-service-layered`) |
| Compartilhado | `infra/docker-compose.yml` 10 / 0 | idem |
| Testes quebrados em P | 1 de 8 (defeito de produção) | 1 de 1 (ambiente: sem Mongo) |
| App sobe em P (sem Mongo) | **Não** | Sim |

Os valores "aproximados" de [`propagacao-e1-e3.csv`](propagacao-e1-e3.csv) para o E1 (70/80 e 50/60) somam
produção e testes: 31+39 = 70 e 79+1 = 80; 14+36 = 50 e 59+1 = 60. Eles batem com o git, mas não separam produção de teste.

---

## 2. E2 — Indisponibilidade de Identity e Catalog (`exp/e2-falhas`)

### 2.1 Hipótese registrada

[`docs/03-protocolo-experimental.md`](../docs/03-protocolo-experimental.md), linha 40:

> a Clean Architecture **não** mitiga acoplamento temporal síncrono; ambas as variantes sofrem degradação
> semelhante quando Catalog ou Identity ficam indisponíveis, a menos que padrões de resiliência (timeout,
> circuit breaker, fallback) sejam introduzidos — e esses padrões são ortogonais à Clean Architecture.

### 2.2 Código relevante (baseline — produção não muda no E2)

**Clean — tradução da falha no adaptador HTTP:**
`services/scheduling-service-clean/infra/src/main/java/br/edu/ifma/labmanager/scheduling/infra/http/HttpIdentityGateway.java:27-43`

```java
public boolean hasPermission(String userId, String permission) {
    try {
        Map<String, Object> body = restClient.post()
                .uri("/api/permissions/check")
                // ...
                .retrieve()
                .body(Map.class);
        // ...
    } catch (RestClientException ex) {
        throw new RemoteDependencyException("Falha ao consultar identity-service: " + ex.getMessage(), ex);
    }
}
```

**Clean — mapeamento para HTTP na presentation:**
`services/scheduling-service-clean/presentation/src/main/java/br/edu/ifma/labmanager/scheduling/presentation/routes/DomainExceptionHandler.java:22-27`

```java
@ExceptionHandler(RemoteDependencyException.class)
public ResponseEntity<Map<String, String>> handleRemote(RemoteDependencyException ex) {
    return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", ex.getMessage()));
}
```

**Layered — tradução da falha no client:**
`services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/client/IdentityClient.java:24-36`

```java
public boolean hasPermission(String userId, String permission) {
    try {
        // ... restClient.post() ... .retrieve().body(Map.class);
        return body != null && Boolean.TRUE.equals(body.get("allowed"));
    } catch (RestClientException ex) {
        throw new IllegalStateException("Falha ao consultar identity-service: " + ex.getMessage(), ex);
    }
}
```

**Layered — mapeamento para HTTP:**
`services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/web/GlobalExceptionHandler.java:20-24`

```java
@ExceptionHandler(IllegalStateException.class)
public ResponseEntity<Map<String, String>> handleRemote(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", ex.getMessage()));
}
```

**Testes adicionados na branch (instrumento do E2):**
`services/scheduling-service-clean/infra/src/test/java/br/edu/ifma/labmanager/scheduling/infra/SchedulingCleanFailureSimulationTests.java:34-55`

```java
@Bean
@Primary
IdentityGateway identityGatewayDown() {
    return (userId, permission) -> {
        throw new RemoteDependencyException("Falha ao consultar identity-service: connection refused");
    };
}
// ...
@Test
void reservaFalhaCom503QuandoIdentityIndisponivel() throws Exception {
    ReservationAcceptanceSupport.solicitaReservaValida(mockMvc)
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("identity-service")));
}
```

`services/scheduling-service-layered/src/test/java/br/edu/ifma/labmanager/scheduling/layered/SchedulingLayeredFailureSimulationTests.java:34-61`

```java
@Bean
@Primary
IdentityClient identityClientDown() {
    return new IdentityClient(RestClient.builder(), "http://localhost:8080") {
        @Override
        public boolean hasPermission(String userId, String permission) {
            throw new IllegalStateException("Falha ao consultar identity-service: connection refused");
        }
    };
}
```

Os stubs **substituem o adaptador inteiro** e lançam diretamente a exceção que o handler espera. O bloco
`catch (RestClientException ...)` dos adaptadores reais não é executado. Por isso o teste verifica o
mapeamento exceção → 503, e não o comportamento sob falha de rede. Essa limitação já está declarada em
[`docs/04-ameacas-validade.md`](../docs/04-ameacas-validade.md) (item 2) e foi tratada no E2-v2 com Toxiproxy.

### 2.3 `git diff --numstat` (saída real)

```
$ git diff --numstat baseline..exp/e2-falhas -- services/scheduling-service-clean
49	0	services/scheduling-service-clean/infra/src/test/java/br/edu/ifma/labmanager/scheduling/infra/SchedulingCleanCatalogFailureSimulationTests.java
56	0	services/scheduling-service-clean/infra/src/test/java/br/edu/ifma/labmanager/scheduling/infra/SchedulingCleanFailureSimulationTests.java

$ git diff --numstat baseline..exp/e2-falhas -- services/scheduling-service-layered
59	0	services/scheduling-service-layered/src/test/java/br/edu/ifma/labmanager/scheduling/layered/SchedulingLayeredCatalogFailureSimulationTests.java
62	0	services/scheduling-service-layered/src/test/java/br/edu/ifma/labmanager/scheduling/layered/SchedulingLayeredFailureSimulationTests.java
```

Nenhuma linha fora de `src/test` nos serviços; nada em `acceptance` ou `infra`.

### 2.4 Testes antes e depois

| Estado | Clean | Layered | ArchUnit |
|--------|-------|---------|----------|
| B (= P, sem diff de produção) | 8/8 ok | 1/1 ok | ok |
| T (branch) | 10/10 ok (+ `...CleanFailureSimulationTests`, `...CleanCatalogFailureSimulationTests`) | 3/3 ok (+ `...LayeredFailureSimulationTests`, `...LayeredCatalogFailureSimulationTests`) | ok |

Os testes novos passaram, ou seja, as respostas foram HTTP 503 com `$.error` contendo `identity-service` ou
`catalog-service`, nas duas variantes. Taxa de sucesso do caso de uso sob falha simulada: 0 % nas duas
variantes (2 cenários × 1 execução cada).

### 2.5 Tabela resumo — E2

| | Clean | Layered |
|--|-------|---------|
| Arquivos de produção | 0 | 0 |
| Linhas de produção (+/−) | 0 / 0 | 0 / 0 |
| Arquivos de teste | 2 (105 / 0) | 2 (121 / 0) |
| Camadas atingidas | nenhuma em produção; testes em `infra` | nenhuma em produção |
| Serviços atingidos | 1 (somente testes) | 1 (somente testes) |
| Resposta sob Identity / Catalog indisponível | 503 / 503 | 503 / 503 |
| Onde a falha é traduzida | adaptador `infra/http` → `RemoteDependencyException` (`application`) → `presentation` | `client` → `IllegalStateException` → `web` |

---

## 3. E3 — Antecedência mínima 24 h → 48 h (`exp/e3-regra`)

### 3.1 Hipótese registrada

[`docs/03-protocolo-experimental.md`](../docs/03-protocolo-experimental.md), linha 55:

> alterar uma regra de domínio (ex.: antecedência mínima de 24h → 48h, ou duração máxima 4h → 2h) deve
> concentrar mudanças no núcleo na variante Clean; na Layered, a regra pode estar espalhada em
> services/controllers/entidades JPA.

### 3.2 Código relevante

**Clean, baseline:**
`services/scheduling-service-clean/domain/src/main/java/br/edu/ifma/labmanager/scheduling/domain/policy/ReservationPolicy.java:25,46-48`

```java
public static final Duration MINIMUM_LEAD_TIME = Duration.ofHours(24);
// ...
if (Duration.between(now, requested.start()).compareTo(MINIMUM_LEAD_TIME) < 0) {
    throw new DomainException("A reserva exige antecedência mínima de 24 horas");
}
```

**Clean, branch** (mesmas linhas; a linha 19 do javadoc também muda de "24" para "48"):
`services/scheduling-service-clean/domain/src/main/java/br/edu/ifma/labmanager/scheduling/domain/policy/ReservationPolicy.java:25,46-48`

```java
public static final Duration MINIMUM_LEAD_TIME = Duration.ofHours(48);
// ...
if (Duration.between(now, requested.start()).compareTo(MINIMUM_LEAD_TIME) < 0) {
    throw new DomainException("A reserva exige antecedência mínima de 48 horas");
}
```

**Layered, baseline:**
`services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/service/ReservationService.java:25,84-86`

```java
private static final Duration MINIMUM_LEAD_TIME = Duration.ofHours(24);
// ...
if (Duration.between(LocalDateTime.now(), start).compareTo(MINIMUM_LEAD_TIME) < 0) {
    throw new ReservationBusinessException("A reserva exige antecedência mínima de 24 horas");
}
```

**Layered, branch** (mesmas linhas):
`services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/service/ReservationService.java:25,84-86`

```java
private static final Duration MINIMUM_LEAD_TIME = Duration.ofHours(48);
// ...
if (Duration.between(LocalDateTime.now(), start).compareTo(MINIMUM_LEAD_TIME) < 0) {
    throw new ReservationBusinessException("A reserva exige antecedência mínima de 48 horas");
}
```

Diferença relevante para testes: a Clean recebe `now` como parâmetro (vindo de `ClockPort`). A Layered
chama `LocalDateTime.now()` dentro do `@Service` (linha 84).

**Suíte de aceitação compartilhada, baseline:**
`acceptance/scheduling-acceptance/src/main/java/br/edu/ifma/labmanager/acceptance/scheduling/ReservationAcceptanceSupport.java:26-31`

```java
LocalDate nextWednesday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
if (nextWednesday.isBefore(LocalDate.now().plusDays(2))) {
    nextWednesday = nextWednesday.plusWeeks(1);
}
LocalDateTime start = nextWednesday.atTime(10, 0);
```

**Suíte de aceitação, branch:**
`acceptance/scheduling-acceptance/src/main/java/br/edu/ifma/labmanager/acceptance/scheduling/ReservationAcceptanceSupport.java:26-30`

```java
// Antecedência mínima experimental: 48h (E3). Garante ≥ 3 dias civis.
LocalDate nextWednesday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
if (!nextWednesday.isAfter(LocalDate.now().plusDays(2))) {
    nextWednesday = nextWednesday.plusWeeks(1);
}
```

### 3.3 `git diff --numstat` (saída real)

```
$ git diff --numstat baseline..exp/e3-regra -- services/scheduling-service-clean
3	3	services/scheduling-service-clean/domain/src/main/java/br/edu/ifma/labmanager/scheduling/domain/policy/ReservationPolicy.java
8	8	services/scheduling-service-clean/domain/src/test/java/br/edu/ifma/labmanager/scheduling/domain/policy/ReservationPolicyTest.java

$ git diff --numstat baseline..exp/e3-regra -- services/scheduling-service-layered
2	2	services/scheduling-service-layered/src/main/java/br/edu/ifma/labmanager/scheduling/layered/service/ReservationService.java

$ git diff --numstat baseline..exp/e3-regra -- acceptance
2	1	acceptance/scheduling-acceptance/src/main/java/br/edu/ifma/labmanager/acceptance/scheduling/ReservationAcceptanceSupport.java
```

A Clean tem 1 linha a mais (3/3 contra 2/2) por causa do javadoc da linha 19, que enumera as regras.

### 3.4 Testes antes e depois

| Estado | Clean (8 testes) | Layered (1 teste) | ArchUnit |
|--------|------------------|-------------------|----------|
| B | 8/8 ok | 1/1 ok | ok |
| **P** (só produção; segunda-feira 12/10/2026, 11:43) | **4/8** — 3 testes de domínio + 1 de aceitação | **0/1** — aceitação | ok |
| T (branch) | 8/8 ok | 1/1 ok | ok |

Testes quebrados no estado P (`execucao-2026-10-12/e3-prod/summary.txt`):

| Teste | Tipo | Mensagem |
|-------|------|----------|
| `ReservationPolicyTest.aceitaReservaValida` | FAILURE | `Unexpected exception thrown: ...DomainException: A reserva exige antecedência mínima de 48 horas` |
| `ReservationPolicyTest.factoryCriaComStatusRequested` | ERROR | `DomainException: A reserva exige antecedência mínima de 48 horas` |
| `ReservationPolicyTest.rejeitaAntecedenciaMenorQue24Horas` | FAILURE | `expected: <true> but was: <false>` (asserção sobre o texto "24 horas") |
| `SchedulingCleanApplicationTests.solicitaReservaComSucesso` | FAILURE | `Status expected:<201> but was:<422>` |
| `SchedulingLayeredApplicationTests.solicitaReservaComSucessoParidadeComClean` | FAILURE | `Status expected:<201> but was:<422>` |

As três quebras de domínio são **determinísticas**: `ReservationPolicyTest` usa relógio fixo
(`now = 2026-09-07T10:00`). As duas quebras de aceitação **dependem do dia e da hora** da execução (ver 4.2).

### 3.5 Tabela resumo — E3

| | Clean | Layered |
|--|-------|---------|
| Arquivos de produção | 1 | 1 |
| Linhas de produção (+/−) | 3 / 3 | 2 / 2 |
| Arquivos de teste do serviço | 1 (8 / 8) | 0 |
| Camadas atingidas | `domain` (`policy`) | `service` |
| Serviços atingidos | 1 | 1 |
| Compartilhado | `acceptance` 2 / 1 | idem |
| Testes quebrados em P — determinísticos | 3 (domínio) | 0 |
| Testes quebrados em P — dependentes de calendário | 1 (aceitação) | 1 (aceitação) |

---

## 4. Achados novos desta consolidação

### 4.1 O E1 Clean da Fase 4 tem um defeito de produção que os testes escondiam

A intervenção adicionou `@ConditionalOnBean(SpringDataReservationRepository.class)` a um `@Bean` de uma
`@Configuration` comum (`SchedulingConfig.java:30`). Em configuração de usuário, essa condição é avaliada
antes de a autoconfiguração registrar os repositórios Spring Data. A condição resulta falsa, o bean
`ReservationRepository` não é criado e a aplicação não sobe. Isso foi observado de duas formas
independentes: no teste do estado P e na execução direta do jar (1.4).

- As notas da Fase 4 (`e1-infra/clean/notas.md`) registram "Integração HTTP continua verde". Isso é
  verdade **apenas** com o stub de teste, que exclui o Mongo e injeta o repositório manualmente.
- A Layered, na mesma condição, sobe normalmente.
- **Não verificado:** a execução com um MongoDB acessível. O Docker estava indisponível. A falha ocorre na
  fase de definição de beans, antes de qualquer conexão, então não há motivo para esperar outro resultado com o
  banco ativo. Mesmo assim, essa execução não foi feita.
- O E1-v2 (Fase 6) não tem esse problema: não usa `@ConditionalOnBean` e foi testado contra MongoDB real via
  Testcontainers.

**Impacto:** os números de propagação do E1 da Fase 4 descrevem uma intervenção que **não funcionava** na
variante Clean. Isso reforça a decisão de usar o E1-v2 como evidência principal. Qualquer menção ao E1 da
Fase 4 no texto deveria citar essa ressalva ou ser substituída pelo E1-v2.

### 4.2 O resultado "0 testes quebrados" na Layered em E3 depende do dia da semana

Com a regra de 48 h e a suíte de aceitação da `baseline` (ou da `baseline-v2`, que é idêntica nesse arquivo),
o cenário agenda para "a próxima quarta-feira com pelo menos 2 dias civis de distância", às 10:00. Numa
segunda-feira após as 10:00, essa quarta-feira fica a menos de 48 h, e a reserva é rejeitada com 422.

Esta consolidação rodou numa segunda-feira (12/10/2026, 11:43). Para confirmar, os **commits P do E3-v2**
foram reexecutados sem alteração, no mesmo dia:

| Commit (E3-v2, só produção) | Registrado em 07/10/2026 (quarta, ~10:12) | Reexecutado em 12/10/2026 (segunda, 11:45-11:46) |
|-----------------------------|--------------------------------------------|--------------------------------------------------|
| `4473c1a` Clean [P] | 3 de 8 quebrados (domínio) | **4 de 8** (3 domínio + `solicitaReservaComSucesso`: 201 esperado, 422 obtido) |
| `0f46387` Layered [P] | **0 de 1** quebrados | **1 de 1** (`solicitaReservaComSucessoParidadeComClean`: 201 esperado, 422 obtido) |

Saída bruta: `execucao-2026-10-12/e3v2-clean-P/summary.txt` e `e3v2-layered-P/summary.txt`.

Consequências:

1. A ameaça H3.4 ("fragilidade de calendário"), que a Fase 6 confirmou só por análise, agora tem
   **confirmação empírica**.
2. O "0 de 1" da Layered no E3-v2 e a frase "a mudança de regra passou sem nenhum teste falhar"
   (`fase-6-sintese.md`, seção 4, linha "Detectar") são verdadeiros para a data em que foram medidos, mas não são
   reprodutíveis em qualquer data. A formulação robusta é: **a Layered não tem detecção determinística** da
   mudança de regra. A única quebra que pode aparecer vem da suíte de aceitação, e só em certas janelas do
   calendário. A Clean tem 3 detecções determinísticas (testes de domínio com relógio fixo).
3. Pela leitura do código (não medido), a janela em que a aceitação quebra com 48 h é segunda-feira das 10:00
   às 23:59 (cerca de 14 h de 168 h por semana). Com 24 h (baseline), o cenário nunca fica abaixo do mínimo
   (o menor intervalo possível é de cerca de 34 h, de segunda 23:59 a quarta 10:00).

### 4.3 Codificação dos `diff-numstat.txt` do E3

`experiments/e3-regra/clean/diff-numstat.txt`, `layered/diff-numstat.txt` e `acceptance-diff-numstat.txt`
estão em UTF-16 LE (BOM `FF FE`), enquanto os do E1 e E2 estão em UTF-8. O conteúdo confere com o git, mas o
git os trata como binários (`-	-` no `--numstat`), e eles não aparecem legíveis em `git diff`. Os arquivos não
foram alterados aqui.

---

## 5. Fase 4 × Fase 6 (produção apenas)

| Experimento | Variante | Fase 4: arquivos / linhas | Fase 6: arquivos / linhas | Fase 4: quebrados em P (12/10) | Fase 6: quebrados após P (registro) |
|-------------|----------|---------------------------|---------------------------|--------------------------------|-------------------------------------|
| E1 | Clean | 8 / 31+ 79− | 8 / 28+ 71− | 1 de 8 (defeito de produção) | 1 de 8 |
| E1 | Layered | 6 / 14+ 59− | 6 / 11+ 50− | 1 de 1 (ambiente) | 1 de 1 |
| E2 | Clean | 0 | 1 / 10+ 1− (timeout, fase B) | 0 | 0 de 8 |
| E2 | Layered | 0 | 1 / 10+ 1− (timeout, fase B) | 0 | 0 de 1 |
| E3 | Clean | 1 / 3+ 3− | 1 / 3+ 3− | 4 de 8 (3 determinísticos) | 3 de 8 (07/10); **4 de 8 em 12/10** |
| E3 | Layered | 1 / 2+ 2− | 1 / 2+ 2− | 1 de 1 (calendário) | 0 de 1 (07/10); **1 de 1 em 12/10** |

Os números da Fase 6 vêm de [`fase-6-sintese.md`](fase-6-sintese.md), seção 2.1. Os do E2 na Fase 4 e na Fase 6
medem intervenções diferentes: na Fase 4, só a introdução de testes; na Fase 6, a introdução de timeout.
