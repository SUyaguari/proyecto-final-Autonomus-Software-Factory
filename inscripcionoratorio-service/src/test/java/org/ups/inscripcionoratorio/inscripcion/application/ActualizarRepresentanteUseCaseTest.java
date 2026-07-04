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
import org.ups.inscripcionoratorio.inscripcion.application.usecase.ActualizarRepresentanteUseCase;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.CedulaInvalidaException;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaNoEncontradaException;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.NinoHijo;
import org.ups.inscripcionoratorio.inscripcion.domain.model.RepresentanteLegal;
import org.ups.inscripcionoratorio.inscripcion.domain.repository.FichaFamiliarRepository;

@ExtendWith(MockitoExtension.class)
class ActualizarRepresentanteUseCaseTest {

    @Mock
    private FichaFamiliarRepository fichaFamiliarRepository;

    private RepresentanteLegal representanteValido() {
        return new RepresentanteLegal("Maria Perez", "1710034065", "0991234567", "Av. Siempre Viva 123");
    }

    @Test
    @DisplayName("Dada una ficha con hijos, cuando se edita el representante, entonces se actualiza sin afectar a los hijos")
    void dadaFichaConHijos_cuandoSeEditaRepresentante_entoncesSeActualiza() {
        // given
        ActualizarRepresentanteUseCase useCase = new ActualizarRepresentanteUseCase(fichaFamiliarRepository);
        FichaFamiliar ficha = FichaFamiliar.crear(representanteValido()).conId(1L);
        ficha.agregarHijo(NinoHijo.nuevo("Juan Perez", LocalDate.of(2016, 5, 10)));
        given(fichaFamiliarRepository.buscarPorId(1L)).willReturn(Optional.of(ficha));
        given(fichaFamiliarRepository.guardar(any(FichaFamiliar.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        RepresentanteLegal nuevoRepresentante = new RepresentanteLegal(
                "Maria Perez", "1710034065", "0987654321", "Av. Siempre Viva 123");

        // when
        FichaFamiliar resultado = useCase.ejecutar(1L, nuevoRepresentante);

        // then
        assertThat(resultado.getRepresentanteLegal()).isEqualTo(nuevoRepresentante);
        assertThat(resultado.getHijos()).hasSize(1);
    }

    @Test
    @DisplayName("Dado un fichaId inexistente, cuando se edita el representante, entonces lanza FichaNoEncontradaException")
    void dadoFichaIdInexistente_cuandoSeEdita_entoncesLanzaExcepcion() {
        ActualizarRepresentanteUseCase useCase = new ActualizarRepresentanteUseCase(fichaFamiliarRepository);
        given(fichaFamiliarRepository.buscarPorId(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(99L, representanteValido()))
                .isInstanceOf(FichaNoEncontradaException.class);
    }

    @Test
    @DisplayName("Dada una cédula inválida, cuando se edita el representante, entonces se rechaza")
    void dadaCedulaInvalida_cuandoSeEdita_entoncesSeRechaza() {
        ActualizarRepresentanteUseCase useCase = new ActualizarRepresentanteUseCase(fichaFamiliarRepository);
        RepresentanteLegal representanteInvalido = new RepresentanteLegal(
                "Carlos Ruiz", "1234567890", "0987654321", "Calle Falsa 456");

        assertThatThrownBy(() -> useCase.ejecutar(1L, representanteInvalido))
                .isInstanceOf(CedulaInvalidaException.class);
    }
}
