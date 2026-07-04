package org.ups.inscripcionoratorio.shared.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.CedulaInvalidaException;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaConHijosNoCancelableException;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaNoEncontradaException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> camposFaltantes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .toList();
        ErrorResponse cuerpo = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud inválida",
                "Uno o más campos obligatorios son inválidos o faltan",
                request.getRequestURI(),
                camposFaltantes);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(CedulaInvalidaException.class)
    public ResponseEntity<ErrorResponse> manejarCedulaInvalida(CedulaInvalidaException ex, HttpServletRequest request) {
        ErrorResponse cuerpo = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "Cédula inválida", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(FichaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> manejarFichaNoEncontrada(FichaNoEncontradaException ex, HttpServletRequest request) {
        ErrorResponse cuerpo = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(), "Ficha no encontrada", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }

    @ExceptionHandler(FichaConHijosNoCancelableException.class)
    public ResponseEntity<ErrorResponse> manejarFichaNoCancelable(FichaConHijosNoCancelableException ex, HttpServletRequest request) {
        ErrorResponse cuerpo = ErrorResponse.of(
                HttpStatus.CONFLICT.value(), "Ficha no cancelable", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
    }
}
