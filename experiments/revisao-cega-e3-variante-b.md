# Material para revisão — Variante B

Instruções ao revisor: avalie quão fácil é **localizar** a regra de antecedência mínima de reserva
e **alterá-la** (por exemplo, de 24 para 48 horas). Não tente identificar o estilo arquitetural.
Pacotes, *imports* e comentários de autoria foram omitidos de propósito.

O trecho é o código de produção do serviço de Agendamento envolvido nessa regra. Este arquivo não
é o controller HTTP.

---

```java
public final class ReservationPolicy {

    public static final Duration MINIMUM_LEAD_TIME = Duration.ofHours(24);
    public static final Duration MAXIMUM_DURATION = Duration.ofHours(4);

    private ReservationPolicy() {
    }

    public static void validate(
            TimeSlot requested,
            OperatingHours operatingHours,
            LocalDateTime now,
            List<Reservation> existingForLaboratory
    ) {
        Objects.requireNonNull(requested);
        Objects.requireNonNull(operatingHours);
        Objects.requireNonNull(now);
        Objects.requireNonNull(existingForLaboratory);

        if (requested.duration().compareTo(MAXIMUM_DURATION) > 0) {
            throw new DomainException("A duração máxima da reserva é de 4 horas");
        }

        if (Duration.between(now, requested.start()).compareTo(MINIMUM_LEAD_TIME) < 0) {
            throw new DomainException("A reserva exige antecedência mínima de 24 horas");
        }

        if (!operatingHours.covers(requested)) {
            throw new DomainException("A reserva está fora do horário de funcionamento do laboratório");
        }

        boolean overlapsConfirmed = existingForLaboratory.stream()
                .filter(r -> r.status() == ReservationStatus.CONFIRMED)
                .anyMatch(r -> r.slot().overlaps(requested));

        if (overlapsConfirmed) {
            throw new DomainException("Já existe reserva confirmada sobreposta neste laboratório");
        }
    }
}
```

Nenhuma substituição estrutural além da omissão de pacote, *imports* e javadoc. O relógio entra como
parâmetro `now` (não há `LocalDateTime.now()` neste arquivo). Não há cliente HTTP nem persistência
nesta classe.
