package br.edu.ifma.labmanager.inventory.application.use_cases;

public record RegisterItemCommand(String requesterId, String name, String unit) {
}
