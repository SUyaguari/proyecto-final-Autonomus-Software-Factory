package org.ups.inscripcionoratorio.inscripcion.domain.validation;

import org.ups.inscripcionoratorio.inscripcion.domain.exception.CedulaInvalidaException;

public final class CedulaEcuatorianaValidator {

    private static final int[] COEFICIENTES = {2, 1, 2, 1, 2, 1, 2, 1, 2};

    private CedulaEcuatorianaValidator() {
    }

    public static void validar(String cedula) {
        if (!esValida(cedula)) {
            throw new CedulaInvalidaException(cedula);
        }
    }

    public static boolean esValida(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) {
            return false;
        }
        int[] digitos = cedula.chars().map(c -> c - '0').toArray();

        int provincia = digitos[0] * 10 + digitos[1];
        if (provincia < 1 || provincia > 24) {
            return false;
        }
        if (digitos[2] > 5) {
            return false;
        }

        int suma = 0;
        for (int i = 0; i < COEFICIENTES.length; i++) {
            int producto = digitos[i] * COEFICIENTES[i];
            if (producto >= 10) {
                producto -= 9;
            }
            suma += producto;
        }
        int verificador = (10 - (suma % 10)) % 10;
        return verificador == digitos[9];
    }
}
