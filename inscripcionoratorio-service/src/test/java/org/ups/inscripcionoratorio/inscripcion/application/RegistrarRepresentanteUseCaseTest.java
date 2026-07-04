package org.ups.inscripcionoratorio.inscripcion.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.inscripcionoratorio.inscripcion.application.usecase.RegistrarRepresentanteUseCase;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.CedulaInvalidaException;
import org.ups.inscripcionoratorio.inscripcion.domain.model.EstadoFicha;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.RepresentanteLegal;
import org.ups.inscripcionoratorio.inscripcion.domain.repository.FichaFamiliarRepository;

@ExtendWith(MockitoExtension.class)
class RegistrarRepresentanteUseCaseTest {

    @Mock
    private FichaFamiliarRepository fichaFamiliarRepository;

    @Test
    @DisplayName("Dado un representante con datos completos y cédula válida, cuando se registra, entonces se persiste una ficha INCOMPLETA")
    void dadoRepresentanteValido_cuandoSeRegistra_entoncesSePersisteFichaIncompleta() {
        // given
        RegistrarRepresentanteUseCase useCase = new RegistrarRepresentanteUseCase(fichaFamiliarRepository);
        RepresentanteLegal representante = new RepresentanteLegal(
                "Maria Perez", "1710034065", "0991234567", "Av. Siempre Viva 123");
        given(fichaFamiliarRepository.guardar(any(FichaFamiliar.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        FichaFamiliar resultado = useCase.ejecutar(representante);

        // then
        assertThat(resultado.getEstado()).isEqualTo(EstadoFicha.INCOMPLETA);
        assertThat(resultado.getRepresentanteLegal()).isEqualTo(representante);
        verify(fichaFamiliarRepository).guardar(any(FichaFamiliar.class));
    }

    @Test
    @DisplayName("Dado un representante con cédula inválida, cuando se registra, entonces se rechaza sin persistir")
    void dadoRepresentanteConCedulaInvalida_cuandoSeRegistra_entoncesSeRechaza() {
        // given
        RegistrarRepresentanteUseCase useCase = new RegistrarRepresentanteUseCase(fichaFamiliarRepository);
        RepresentanteLegal representante = new RepresentanteLegal(
                "Carlos Ruiz", "1234567890", "0987654321", "Calle Falsa 456");

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar(representante))
                .isInstanceOf(CedulaInvalidaException.class);
        verify(fichaFamiliarRepository, never()).guardar(any());
    }
}
