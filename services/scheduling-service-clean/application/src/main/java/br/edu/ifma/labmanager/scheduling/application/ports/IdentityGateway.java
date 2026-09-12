package br.edu.ifma.labmanager.scheduling.application.ports;

public interface IdentityGateway {
    boolean hasPermission(String userId, String permission);
}
