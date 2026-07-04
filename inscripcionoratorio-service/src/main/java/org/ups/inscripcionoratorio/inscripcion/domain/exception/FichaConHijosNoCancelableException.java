package org.ups.inscripcionoratorio.inscripcion.domain.exception;

public class FichaConHijosNoCancelableException extends RuntimeException {

    public FichaConHijosNoCancelableException(Long fichaId) {
        super("La ficha familiar " + fichaId + " ya tiene hijos registrados y no puede cancelarse");
    }
}
