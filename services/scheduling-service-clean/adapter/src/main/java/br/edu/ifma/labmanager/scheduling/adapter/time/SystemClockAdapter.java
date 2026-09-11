package br.edu.ifma.labmanager.scheduling.adapter.time;

import br.edu.ifma.labmanager.scheduling.application.port.out.ClockPort;

import java.time.Clock;
import java.time.LocalDateTime;

public class SystemClockAdapter implements ClockPort {

    private final Clock clock;

    public SystemClockAdapter(Clock clock) {
        this.clock = clock;
    }

    public SystemClockAdapter() {
        this(Clock.systemDefaultZone());
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
