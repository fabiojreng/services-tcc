package br.edu.ifma.labmanager.catalog.infra.models;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "laboratories")
public class LaboratoryJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private LocalTime opensAt;

    @Column(nullable = false)
    private LocalTime closesAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "laboratory_open_days", joinColumns = @JoinColumn(name = "laboratory_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "open_day", nullable = false)
    private Set<DayOfWeek> openDays = new HashSet<>();

    protected LaboratoryJpaEntity() {
    }

    public LaboratoryJpaEntity(
            UUID id,
            String name,
            int capacity,
            LocalTime opensAt,
            LocalTime closesAt,
            Set<DayOfWeek> openDays
    ) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.openDays = openDays;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public LocalTime getOpensAt() {
        return opensAt;
    }

    public LocalTime getClosesAt() {
        return closesAt;
    }

    public Set<DayOfWeek> getOpenDays() {
        return openDays;
    }
}
