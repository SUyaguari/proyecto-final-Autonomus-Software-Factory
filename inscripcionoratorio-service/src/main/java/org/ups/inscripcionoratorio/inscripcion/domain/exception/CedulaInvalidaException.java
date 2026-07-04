package org.ups.inscripcionoratorio.inscripcion.domain.exception;

public class CedulaInvalidaException extends RuntimeException {

    public CedulaInvalidaException(String cedula) {
        super("La cédula '" + cedula + "' no es una cédula ecuatoriana válida");
    }
}
