package org.ups.inscripcionoratorio.inscripcion.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaNoEncontradaException;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.RepresentanteLegal;
import org.ups.inscripcionoratorio.inscripcion.domain.repository.FichaFamiliarRepository;
import org.ups.inscripcionoratorio.inscripcion.domain.validation.CedulaEcuatorianaValidator;

@Service
public class ActualizarRepresentanteUseCase {

    private final FichaFamiliarRepository fichaFamiliarRepository;

    public ActualizarRepresentanteUseCase(FichaFamiliarRepository fichaFamiliarRepository) {
        this.fichaFamiliarRepository = fichaFamiliarRepository;
    }

    public FichaFamiliar ejecutar(Long fichaId, RepresentanteLegal nuevoRepresentante) {
        CedulaEcuatorianaValidator.validar(nuevoRepresentante.cedula());
        FichaFamiliar fichaFamiliar = fichaFamiliarRepository.buscarPorId(fichaId)
                .orElseThrow(() -> new FichaNoEncontradaException(fichaId));
        fichaFamiliar.actualizarRepresentante(nuevoRepresentante);
        return fichaFamiliarRepository.guardar(fichaFamiliar);
    }
}
