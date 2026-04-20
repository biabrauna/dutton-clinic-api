package br.com.clinicah.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Integer id) {
        super(resource + " não encontrado com id: " + id);
    }
}
