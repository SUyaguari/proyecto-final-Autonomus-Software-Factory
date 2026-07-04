package org.ups.inscripcionoratorio.inscripcion.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaConHijosNoCancelableException;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaNoEncontradaException;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.repository.FichaFamiliarRepository;

@Service
public class CancelarFichaUseCase {

    private final FichaFamiliarRepository fichaFamiliarRepository;

    public CancelarFichaUseCase(FichaFamiliarRepository fichaFamiliarRepository) {
        this.fichaFamiliarRepository = fichaFamiliarRepository;
    }

    public void ejecutar(Long fichaId) {
        FichaFamiliar fichaFamiliar = fichaFamiliarRepository.buscarPorId(fichaId)
                .orElseThrow(() -> new FichaNoEncontradaException(fichaId));
        if (!fichaFamiliar.puedeCancelarse()) {
            throw new FichaConHijosNoCancelableException(fichaId);
        }
        fichaFamiliarRepository.eliminar(fichaFamiliar);
    }
}
