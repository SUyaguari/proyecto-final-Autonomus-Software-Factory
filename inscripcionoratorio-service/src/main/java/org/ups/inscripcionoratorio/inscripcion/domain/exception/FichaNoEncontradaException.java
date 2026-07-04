package org.ups.inscripcionoratorio.inscripcion.domain.exception;

public class FichaNoEncontradaException extends RuntimeException {

    public FichaNoEncontradaException(Long fichaId) {
        super("No existe una ficha familiar con id " + fichaId);
    }
}
