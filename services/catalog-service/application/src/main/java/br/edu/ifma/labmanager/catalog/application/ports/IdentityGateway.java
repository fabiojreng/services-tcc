package br.edu.ifma.labmanager.catalog.application.ports;

public interface IdentityGateway {
    boolean hasPermission(String userId, String permission);
}
