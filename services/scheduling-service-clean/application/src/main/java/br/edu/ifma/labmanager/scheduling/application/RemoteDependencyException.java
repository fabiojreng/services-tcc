package br.edu.ifma.labmanager.scheduling.application;

public class RemoteDependencyException extends RuntimeException {
    public RemoteDependencyException(String message) {
        super(message);
    }

    public RemoteDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
