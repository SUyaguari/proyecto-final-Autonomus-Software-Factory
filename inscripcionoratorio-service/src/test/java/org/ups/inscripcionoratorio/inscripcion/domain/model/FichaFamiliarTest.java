package org.ups.inscripcionoratorio.inscripcion.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FichaFamiliarTest {

    private RepresentanteLegal representanteValido() {
        return new RepresentanteLegal("Maria Perez", "1710034065", "0991234567", "Av. Siempre Viva 123");
    }

    @Test
    @DisplayName("Dado un representante legal, cuando se crea la ficha, entonces queda INCOMPLETA y sin hijos")
    void dadoRepresentante_cuandoSeCrea_entoncesQuedaIncompletaSinHijos() {
        // given
        RepresentanteLegal representante = representanteValido();

        // when
        FichaFamiliar ficha = FichaFamiliar.crear(representante);

        // then
        assertThat(ficha.getEstado()).isEqualTo(EstadoFicha.INCOMPLETA);
        assertThat(ficha.getHijos()).isEmpty();
        assertThat(ficha.getRepresentanteLegal()).isEqualTo(representante);
        assertThat(ficha.puedeCancelarse()).isTrue();
    }

    @Test
    @DisplayName("Dada una ficha INCOMPLETA, cuando se agrega el primer hijo, entonces pasa a COMPLETA y ya no es cancelable")
    void dadaFichaIncompleta_cuandoSeAgregaPrimerHijo_entoncesQuedaCompleta() {
        // given
        FichaFamiliar ficha = FichaFamiliar.crear(representanteValido());
        NinoHijo hijo = NinoHijo.nuevo("Juan Perez", LocalDate.of(2016, 5, 10));

        // when
        ficha.agregarHijo(hijo);

        // then
        assertThat(ficha.getEstado()).isEqualTo(EstadoFicha.COMPLETA);
        assertThat(ficha.getHijos()).containsExactly(hijo);
        assertThat(ficha.puedeCancelarse()).isFalse();
    }

    @Test
    @DisplayName("Dada una ficha con un hijo, cuando se agrega un segundo hijo, entonces ambos quedan vinculados")
    void dadaFichaConUnHijo_cuandoSeAgregaSegundoHijo_entoncesAmbosQuedanVinculados() {
        // given
        FichaFamiliar ficha = FichaFamiliar.crear(representanteValido());
        NinoHijo primero = NinoHijo.nuevo("Juan Perez", LocalDate.of(2016, 5, 10));
        NinoHijo segundo = NinoHijo.nuevo("Ana Perez", LocalDate.of(2018, 9, 2));

        // when
        ficha.agregarHijo(primero);
        ficha.agregarHijo(segundo);

        // then
        assertThat(ficha.getHijos()).containsExactly(primero, segundo);
        assertThat(ficha.getEstado()).isEqualTo(EstadoFicha.COMPLETA);
    }

    @Test
    @DisplayName("Dada una ficha, cuando se actualiza el representante, entonces se reemplazan sus datos sin importar los hijos existentes")
    void dadaFicha_cuandoSeActualizaRepresentante_entoncesSeReemplazanDatos() {
        // given
        FichaFamiliar ficha = FichaFamiliar.crear(representanteValido());
        ficha.agregarHijo(NinoHijo.nuevo("Juan Perez", LocalDate.of(2016, 5, 10)));
        RepresentanteLegal nuevoRepresentante = new RepresentanteLegal(
                "Maria Perez", "1710034065", "0987654321", "Av. Siempre Viva 123");

        // when
        ficha.actualizarRepresentante(nuevoRepresentante);

        // then
        assertThat(ficha.getRepresentanteLegal()).isEqualTo(nuevoRepresentante);
        assertThat(ficha.getHijos()).hasSize(1);
    }

    @Test
    @DisplayName("Dado un representante nulo, cuando se crea la ficha, entonces lanza IllegalArgumentException")
    void dadoRepresentanteNulo_cuandoSeCrea_entoncesLanzaExcepcion() {
        assertThatThrownBy(() -> FichaFamiliar.crear(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
