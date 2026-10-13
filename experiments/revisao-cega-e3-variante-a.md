# Material para revisão — Variante A

Instruções ao revisor: avalie quão fácil é **localizar** a regra de antecedência mínima de reserva
e **alterá-la** (por exemplo, de 24 para 48 horas). Não tente identificar o estilo arquitetural.
Pacotes, *imports* e comentários de autoria foram omitidos de propósito.

O trecho é o código de produção do serviço de Agendamento envolvido nessa regra. O endpoint HTTP
de solicitação delega para o método `requestReservation` abaixo.

---

```java
public class ReservationService {

    private static final Duration MINIMUM_LEAD_TIME = Duration.ofHours(24);
    private static final Duration MAXIMUM_DURATION = Duration.ofHours(4);

    private final ReservationStore repository;
    private final IdentityClient identityClient;
    private final CatalogClient catalogClient;

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

        ReservationRecord entity = new ReservationRecord(
                UUID.randomUUID(),
                laboratoryId,
                requesterId,
                start,
                end,
                ReservationStatus.REQUESTED
        );
        repository.save(entity);

        return new ReservationResponse(entity.getId(), entity.getStatus(), entity.getStartAt(), entity.getEndAt());
    }

    private void validateRules(
            UUID laboratoryId,
            LocalDateTime start,
            LocalDateTime end,
            CatalogClient.LaboratoryHours hours
    ) {
        if (!end.isAfter(start)) {
            throw new ReservationBusinessException("O fim da reserva deve ser posterior ao início");
        }

        Duration duration = Duration.between(start, end);
        if (duration.compareTo(MAXIMUM_DURATION) > 0) {
            throw new ReservationBusinessException("A duração máxima da reserva é de 4 horas");
        }

        if (Duration.between(LocalDateTime.now(), start).compareTo(MINIMUM_LEAD_TIME) < 0) {
            throw new ReservationBusinessException("A reserva exige antecedência mínima de 24 horas");
        }

        if (!isWithinOperatingHours(start, end, hours)) {
            throw new ReservationBusinessException("A reserva está fora do horário de funcionamento do laboratório");
        }

        List<ReservationRecord> existing = repository.findByLaboratoryId(laboratoryId);
        boolean overlaps = existing.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .anyMatch(r -> overlaps(start, end, r.getStartAt(), r.getEndAt()));

        if (overlaps) {
            throw new ReservationBusinessException("Já existe reserva confirmada sobreposta neste laboratório");
        }
    }

    // isWithinOperatingHours e overlaps omitidos: não usam MINIMUM_LEAD_TIME
}
```

Pacote e *imports* foram omitidos. Dois nomes de tipo neste trecho (`ReservationStore` e
`ReservationRecord`) não são os originais; o gabarito registra a correspondência. A sequência de
chamadas e as condições permanecem as do código de produção.
