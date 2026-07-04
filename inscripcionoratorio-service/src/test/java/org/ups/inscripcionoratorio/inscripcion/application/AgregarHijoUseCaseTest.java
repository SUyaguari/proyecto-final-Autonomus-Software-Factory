package org.ups.inscripcionoratorio.inscripcion.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.inscripcionoratorio.inscripcion.application.usecase.AgregarHijoUseCase;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaNoEncontradaException;
import org.ups.inscripcionoratorio.inscripcion.domain.model.EstadoFicha;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.NinoHijo;
import org.ups.inscripcionoratorio.inscripcion.domain.model.RepresentanteLegal;
import org.ups.inscripcionoratorio.inscripcion.domain.repository.FichaFamiliarRepository;

@ExtendWith(MockitoExtension.class)
class AgregarHijoUseCaseTest {

    @Mock
    private FichaFamiliarRepository fichaFamiliarRepository;

    private RepresentanteLegal representanteValido() {
        return new RepresentanteLegal("Maria Perez", "1710034065", "0991234567", "Av. Siempre Viva 123");
    }

    @Test
    @DisplayName("Dada una ficha existente sin hijos, cuando se agrega un hijo, entonces queda vinculado y la ficha pasa a COMPLETA")
    void dadaFichaExistente_cuandoSeAgregaHijo_entoncesQuedaVinculado() {
        // given
        AgregarHijoUseCase useCase = new AgregarHijoUseCase(fichaFamiliarRepository);
        FichaFamiliar ficha = FichaFamiliar.crear(representanteValido()).conId(1L);
        given(fichaFamiliarRepository.buscarPorId(1L)).willReturn(Optional.of(ficha));
        given(fichaFamiliarRepository.guardar(any(FichaFamiliar.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        NinoHijo hijo = NinoHijo.nuevo("Juan Perez", LocalDate.of(2016, 5, 10));

        // when
        FichaFamiliar resultado = useCase.ejecutar(1L, hijo);

        // then
        assertThat(resultado.getHijos()).containsExactly(hijo);
        assertThat(resultado.getEstado()).isEqualTo(EstadoFicha.COMPLETA);
    }

    @Test
    @DisplayName("Dado un fichaId inexistente, cuando se agrega un hijo, entonces lanza FichaNoEncontradaException")
    void dadoFichaIdInexistente_cuandoSeAgregaHijo_entoncesLanzaExcepcion() {
        // given
        AgregarHijoUseCase useCase = new AgregarHijoUseCase(fichaFamiliarRepository);
        given(fichaFamiliarRepository.buscarPorId(99L)).willReturn(Optional.empty());
        NinoHijo hijo = NinoHijo.nuevo("Juan Perez", LocalDate.of(2016, 5, 10));

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar(99L, hijo))
                .isInstanceOf(FichaNoEncontradaException.class);
    }

    @Test
    @DisplayName("Dado datos propios del hijo incompletos, cuando se construye el hijo, entonces se rechaza (FR-013)")
    void dadoHijoConDatosIncompletos_cuandoSeConstruye_entoncesSeRechaza() {
        assertThatThrownBy(() -> NinoHijo.nuevo("", LocalDate.of(2016, 5, 10)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NinoHijo.nuevo("Juan Perez", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
