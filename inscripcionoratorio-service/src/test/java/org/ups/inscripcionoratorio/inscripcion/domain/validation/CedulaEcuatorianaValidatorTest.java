package org.ups.inscripcionoratorio.inscripcion.domain.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.CedulaInvalidaException;

class CedulaEcuatorianaValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"1710034065", "1710034016"})
    @DisplayName("Dado una cédula ecuatoriana con dígito verificador correcto, cuando se valida, entonces se considera válida")
    void dadaCedulaValida_cuandoSeValida_entoncesEsValida(String cedula) {
        // when
        boolean resultado = CedulaEcuatorianaValidator.esValida(cedula);

        // then
        assertThat(resultado).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "171003406",   // menos de 10 dígitos
            "17100340655", // más de 10 dígitos
            "171003406A",  // no numérica
            "9910034065",  // provincia inválida (99)
            "1790034065",  // tercer dígito > 5 (no es persona natural)
            "1710034060"   // dígito verificador incorrecto
    })
    @DisplayName("Dado una cédula con formato, provincia, tercer dígito o dígito verificador inválido, cuando se valida, entonces no es válida")
    void dadaCedulaInvalida_cuandoSeValida_entoncesNoEsValida(String cedula) {
        // when
        boolean resultado = CedulaEcuatorianaValidator.esValida(cedula);

        // then
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Dado una cédula inválida, cuando se invoca validar(), entonces lanza CedulaInvalidaException")
    void dadaCedulaInvalida_cuandoSeInvocaValidar_entoncesLanzaExcepcion() {
        // given
        String cedulaInvalida = "1710034060";

        // when / then
        assertThatThrownBy(() -> CedulaEcuatorianaValidator.validar(cedulaInvalida))
                .isInstanceOf(CedulaInvalidaException.class);
    }

    @Test
    @DisplayName("Dado una cédula válida, cuando se invoca validar(), entonces no lanza excepción")
    void dadaCedulaValida_cuandoSeInvocaValidar_entoncesNoLanzaExcepcion() {
        // given
        String cedulaValida = "1710034065";

        // when / then
        CedulaEcuatorianaValidator.validar(cedulaValida);
    }

    @Test
    @DisplayName("Dado un valor nulo, cuando se valida, entonces no es válida")
    void dadoValorNulo_cuandoSeValida_entoncesNoEsValida() {
        assertThat(CedulaEcuatorianaValidator.esValida(null)).isFalse();
    }
}
