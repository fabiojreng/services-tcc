package br.edu.ifma.labmanager.identity.presentation.schemas;

public record CheckPermissionResponse(String userId, String permission, boolean allowed) {
}
