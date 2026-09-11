package br.edu.ifma.labmanager.scheduling.application.ports;

import java.time.LocalDateTime;

public interface ClockPort {

    LocalDateTime now();
}
