package br.edu.ifma.labmanager.scheduling.layered.service;

import br.edu.ifma.labmanager.scheduling.layered.client.CatalogClient;
import br.edu.ifma.labmanager.scheduling.layered.client.IdentityClient;
import br.edu.ifma.labmanager.scheduling.layered.entity.ReservationEntity;
import br.edu.ifma.labmanager.scheduling.layered.entity.ReservationStatus;
import br.edu.ifma.labmanager.scheduling.layered.repository.ReservationJpaRepository;
import br.edu.ifma.labmanager.scheduling.layered.web.ReservationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Estilo em camadas idiomático Spring: regras de negócio e chamadas HTTP no @Service,
 * entidade JPA anêmica atravessando camadas.
 */
@Service
public class ReservationService {

    private static final Duration MINIMUM_LEAD_TIME = Duration.ofHours(24);
    private static final Duration MAXIMUM_DURATION = Duration.ofHours(4);

    private final ReservationJpaRepository repository;
    private final IdentityClient identityClient;
    private final CatalogClient catalogClient;

    public ReservationService(
            ReservationJpaRepository repository,
            IdentityClient identityClient,
            CatalogClient catalogClient
    ) {
        this.repository = repository;
        this.identityClient = identityClient;
        this.catalogClient = catalogClient;
    }

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

        ReservationEntity entity = new ReservationEntity(
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

        List<ReservationEntity> existing = repository.findByLaboratoryId(laboratoryId);
        boolean overlaps = existing.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .anyMatch(r -> overlaps(start, end, r.getStartAt(), r.getEndAt()));

        if (overlaps) {
            throw new ReservationBusinessException("Já existe reserva confirmada sobreposta neste laboratório");
        }
    }

    private boolean isWithinOperatingHours(
            LocalDateTime start,
            LocalDateTime end,
            CatalogClient.LaboratoryHours hours
    ) {
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            return false;
        }
        DayOfWeek day = start.getDayOfWeek();
        if (!hours.openDays().contains(day)) {
            return false;
        }
        return !start.toLocalTime().isBefore(hours.opensAt())
                && !end.toLocalTime().isAfter(hours.closesAt());
    }

    private boolean overlaps(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    public static class ReservationBusinessException extends RuntimeException {
        public ReservationBusinessException(String message) {
            super(message);
        }
    }
}
