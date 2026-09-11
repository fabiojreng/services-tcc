package br.edu.ifma.labmanager.scheduling.application.port.out;

import java.time.LocalDateTime;

public interface ClockPort {

    LocalDateTime now();
}
