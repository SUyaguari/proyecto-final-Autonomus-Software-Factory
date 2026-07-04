package org.ups.inscripcionoratorio.inscripcion.domain.repository;

import java.util.Optional;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;

public interface FichaFamiliarRepository {

    FichaFamiliar guardar(FichaFamiliar fichaFamiliar);

    Optional<FichaFamiliar> buscarPorId(Long id);

    void eliminar(FichaFamiliar fichaFamiliar);
}
