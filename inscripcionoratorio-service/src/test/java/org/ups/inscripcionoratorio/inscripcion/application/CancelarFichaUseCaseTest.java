package org.ups.inscripcionoratorio.inscripcion.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.inscripcionoratorio.inscripcion.application.usecase.CancelarFichaUseCase;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaConHijosNoCancelableException;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaNoEncontradaException;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.NinoHijo;
import org.ups.inscripcionoratorio.inscripcion.domain.model.RepresentanteLegal;
import org.ups.inscripcionoratorio.inscripcion.domain.repository.FichaFamiliarRepository;

@ExtendWith(MockitoExtension.class)
class CancelarFichaUseCaseTest {

    @Mock
    private FichaFamiliarRepository fichaFamiliarRepository;

    private RepresentanteLegal representanteValido() {
        return new RepresentanteLegal("Maria Perez", "1710034065", "0991234567", "Av. Siempre Viva 123");
    }

    @Test
    @DisplayName("Dada una ficha INCOMPLETA (sin hijos), cuando se cancela, entonces se elimina")
    void dadaFichaIncompleta_cuandoSeCancela_entoncesSeElimina() {
        // given
        CancelarFichaUseCase useCase = new CancelarFichaUseCase(fichaFamiliarRepository);
        FichaFamiliar ficha = FichaFamiliar.crear(representanteValido()).conId(1L);
        given(fichaFamiliarRepository.buscarPorId(1L)).willReturn(Optional.of(ficha));

        // when
        useCase.ejecutar(1L);

        // then
        verify(fichaFamiliarRepository).eliminar(ficha);
    }

    @Test
    @DisplayName("Dada una ficha COMPLETA (con hijos), cuando se intenta cancelar, entonces lanza FichaConHijosNoCancelableException")
    void dadaFichaCompleta_cuandoSeIntentaCancelar_entoncesLanzaExcepcion() {
        // given
        CancelarFichaUseCase useCase = new CancelarFichaUseCase(fichaFamiliarRepository);
        FichaFamiliar ficha = FichaFamiliar.crear(representanteValido()).conId(1L);
        ficha.agregarHijo(NinoHijo.nuevo("Juan Perez", LocalDate.of(2016, 5, 10)));
        given(fichaFamiliarRepository.buscarPorId(1L)).willReturn(Optional.of(ficha));

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar(1L))
                .isInstanceOf(FichaConHijosNoCancelableException.class);
        verify(fichaFamiliarRepository, never()).eliminar(ficha);
    }

    @Test
    @DisplayName("Dado un fichaId inexistente, cuando se cancela, entonces lanza FichaNoEncontradaException")
    void dadoFichaIdInexistente_cuandoSeCancela_entoncesLanzaExcepcion() {
        CancelarFichaUseCase useCase = new CancelarFichaUseCase(fichaFamiliarRepository);
        given(fichaFamiliarRepository.buscarPorId(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(99L))
                .isInstanceOf(FichaNoEncontradaException.class);
    }
}
