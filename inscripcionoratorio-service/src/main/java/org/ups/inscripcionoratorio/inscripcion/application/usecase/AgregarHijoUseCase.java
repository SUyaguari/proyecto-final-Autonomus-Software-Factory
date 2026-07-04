package org.ups.inscripcionoratorio.inscripcion.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaNoEncontradaException;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.NinoHijo;
import org.ups.inscripcionoratorio.inscripcion.domain.repository.FichaFamiliarRepository;

@Service
public class AgregarHijoUseCase {

    private final FichaFamiliarRepository fichaFamiliarRepository;

    public AgregarHijoUseCase(FichaFamiliarRepository fichaFamiliarRepository) {
        this.fichaFamiliarRepository = fichaFamiliarRepository;
    }

    public FichaFamiliar ejecutar(Long fichaId, NinoHijo hijo) {
        FichaFamiliar fichaFamiliar = fichaFamiliarRepository.buscarPorId(fichaId)
                .orElseThrow(() -> new FichaNoEncontradaException(fichaId));
        fichaFamiliar.agregarHijo(hijo);
        return fichaFamiliarRepository.guardar(fichaFamiliar);
    }
}
