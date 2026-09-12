package br.edu.ifma.labmanager.inventory.application.ports;

public interface IdentityGateway {
    boolean hasPermission(String userId, String permission);
}
