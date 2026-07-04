package org.ups.inscripcionoratorio.inscripcion.domain.model;

import java.time.LocalDate;

public record NinoHijo(Long id, String nombreCompleto, LocalDate fechaNacimiento) {

    public NinoHijo {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre completo del hijo es obligatorio");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento del hijo es obligatoria");
        }
    }

    public static NinoHijo nuevo(String nombreCompleto, LocalDate fechaNacimiento) {
        return new NinoHijo(null, nombreCompleto, fechaNacimiento);
    }
}
