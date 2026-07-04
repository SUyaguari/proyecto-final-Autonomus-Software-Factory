package org.ups.inscripcionoratorio.inscripcion.domain.model;

public record RepresentanteLegal(String nombreCompleto, String cedula, String celular, String direccion) {

    public RepresentanteLegal {
        requireNonBlank(nombreCompleto, "nombreCompleto");
        requireNonBlank(cedula, "cedula");
        requireNonBlank(celular, "celular");
        requireNonBlank(direccion, "direccion");
    }

    private static void requireNonBlank(String value, String campo) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' del representante legal es obligatorio");
        }
    }
}
