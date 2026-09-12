package br.edu.ifma.labmanager.identity.application.use_cases;

public record CheckPermissionResult(String userId, String permission, boolean allowed) {
}
