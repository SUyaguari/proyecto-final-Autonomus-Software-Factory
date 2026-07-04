package org.ups.inscripcionoratorio.inscripcion.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.RepresentanteLegal;
import org.ups.inscripcionoratorio.inscripcion.domain.repository.FichaFamiliarRepository;
import org.ups.inscripcionoratorio.inscripcion.domain.validation.CedulaEcuatorianaValidator;

@Service
public class RegistrarRepresentanteUseCase {

    private final FichaFamiliarRepository fichaFamiliarRepository;

    public RegistrarRepresentanteUseCase(FichaFamiliarRepository fichaFamiliarRepository) {
        this.fichaFamiliarRepository = fichaFamiliarRepository;
    }

    public FichaFamiliar ejecutar(RepresentanteLegal representanteLegal) {
        CedulaEcuatorianaValidator.validar(representanteLegal.cedula());
        FichaFamiliar fichaFamiliar = FichaFamiliar.crear(representanteLegal);
        return fichaFamiliarRepository.guardar(fichaFamiliar);
    }
}
