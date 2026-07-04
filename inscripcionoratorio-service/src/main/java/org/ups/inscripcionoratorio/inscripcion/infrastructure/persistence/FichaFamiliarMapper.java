package org.ups.inscripcionoratorio.inscripcion.infrastructure.persistence;

import java.util.List;
import org.springframework.stereotype.Component;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.NinoHijo;
import org.ups.inscripcionoratorio.inscripcion.domain.model.RepresentanteLegal;

@Component
public class FichaFamiliarMapper {

    public FichaFamiliarEntity aNuevaEntidad(FichaFamiliar dominio) {
        FichaFamiliarEntity entity = new FichaFamiliarEntity();
        aplicarDatosBasicos(entity, dominio);
        entity.setFechaCreacion(dominio.getFechaCreacion());
        sincronizarHijos(entity, dominio);
        return entity;
    }

    public void actualizarEntidad(FichaFamiliarEntity entity, FichaFamiliar dominio) {
        aplicarDatosBasicos(entity, dominio);
        sincronizarHijos(entity, dominio);
    }

    public FichaFamiliar aDominio(FichaFamiliarEntity entity) {
        RepresentanteLegal representanteLegal = new RepresentanteLegal(
                entity.getNombreCompleto(), entity.getCedula(), entity.getCelular(), entity.getDireccion());
        List<NinoHijo> hijos = entity.getHijos().stream()
                .map(hijoEntity -> new NinoHijo(hijoEntity.getId(), hijoEntity.getNombreCompleto(), hijoEntity.getFechaNacimiento()))
                .toList();
        return FichaFamiliar.reconstruir(
                entity.getId(), representanteLegal, hijos, entity.getEstado(), entity.getFechaCreacion());
    }

    private void aplicarDatosBasicos(FichaFamiliarEntity entity, FichaFamiliar dominio) {
        RepresentanteLegal representanteLegal = dominio.getRepresentanteLegal();
        entity.setNombreCompleto(representanteLegal.nombreCompleto());
        entity.setCedula(representanteLegal.cedula());
        entity.setCelular(representanteLegal.celular());
        entity.setDireccion(representanteLegal.direccion());
        entity.setEstado(dominio.getEstado());
    }

    private void sincronizarHijos(FichaFamiliarEntity entity, FichaFamiliar dominio) {
        for (NinoHijo hijo : dominio.getHijos()) {
            if (hijo.id() == null) {
                NinoHijoEntity nuevoHijo = new NinoHijoEntity();
                nuevoHijo.setNombreCompleto(hijo.nombreCompleto());
                nuevoHijo.setFechaNacimiento(hijo.fechaNacimiento());
                nuevoHijo.setFichaFamiliar(entity);
                entity.getHijos().add(nuevoHijo);
            }
        }
    }
}
