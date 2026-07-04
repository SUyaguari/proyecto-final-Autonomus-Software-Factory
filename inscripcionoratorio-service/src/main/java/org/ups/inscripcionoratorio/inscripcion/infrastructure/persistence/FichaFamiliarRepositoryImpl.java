package org.ups.inscripcionoratorio.inscripcion.infrastructure.persistence;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.ups.inscripcionoratorio.inscripcion.domain.exception.FichaNoEncontradaException;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.repository.FichaFamiliarRepository;

@Repository
public class FichaFamiliarRepositoryImpl implements FichaFamiliarRepository {

    private final FichaFamiliarJpaRepository jpaRepository;
    private final FichaFamiliarMapper mapper;

    public FichaFamiliarRepositoryImpl(FichaFamiliarJpaRepository jpaRepository, FichaFamiliarMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public FichaFamiliar guardar(FichaFamiliar fichaFamiliar) {
        FichaFamiliarEntity entity;
        if (fichaFamiliar.getId() == null) {
            entity = mapper.aNuevaEntidad(fichaFamiliar);
        } else {
            entity = jpaRepository.findById(fichaFamiliar.getId())
                    .orElseThrow(() -> new FichaNoEncontradaException(fichaFamiliar.getId()));
            mapper.actualizarEntidad(entity, fichaFamiliar);
        }
        FichaFamiliarEntity guardada = jpaRepository.save(entity);
        return mapper.aDominio(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FichaFamiliar> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::aDominio);
    }

    @Override
    @Transactional
    public void eliminar(FichaFamiliar fichaFamiliar) {
        jpaRepository.deleteById(fichaFamiliar.getId());
    }
}
