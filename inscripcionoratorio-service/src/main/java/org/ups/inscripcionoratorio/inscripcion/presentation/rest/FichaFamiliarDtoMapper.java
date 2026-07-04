package org.ups.inscripcionoratorio.inscripcion.presentation.rest;

import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.NinoHijo;
import org.ups.inscripcionoratorio.inscripcion.domain.model.RepresentanteLegal;
import org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated.model.FichaFamiliarResponse;
import org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated.model.NinoHijoRequest;
import org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated.model.NinoHijoResponse;
import org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated.model.RepresentanteLegalRequest;

@Component
public class FichaFamiliarDtoMapper {

    public RepresentanteLegal aDominio(RepresentanteLegalRequest request) {
        return new RepresentanteLegal(
                request.getNombreCompleto(), request.getCedula(), request.getCelular(), request.getDireccion());
    }

    public NinoHijo aDominio(NinoHijoRequest request) {
        return NinoHijo.nuevo(request.getNombreCompleto(), request.getFechaNacimiento());
    }

    public FichaFamiliarResponse aResponse(FichaFamiliar dominio) {
        FichaFamiliarResponse response = new FichaFamiliarResponse();
        response.setId(dominio.getId());
        response.setEstado(FichaFamiliarResponse.EstadoEnum.fromValue(dominio.getEstado().name()));
        response.setRepresentanteLegal(aRepresentanteLegalRequest(dominio.getRepresentanteLegal()));
        response.setHijos(dominio.getHijos().stream().map(this::aNinoHijoResponse).toList());
        response.setFechaCreacion(dominio.getFechaCreacion().atOffset(ZoneOffset.UTC));
        return response;
    }

    private RepresentanteLegalRequest aRepresentanteLegalRequest(RepresentanteLegal representanteLegal) {
        RepresentanteLegalRequest dto = new RepresentanteLegalRequest();
        dto.setNombreCompleto(representanteLegal.nombreCompleto());
        dto.setCedula(representanteLegal.cedula());
        dto.setCelular(representanteLegal.celular());
        dto.setDireccion(representanteLegal.direccion());
        return dto;
    }

    private NinoHijoResponse aNinoHijoResponse(NinoHijo hijo) {
        NinoHijoResponse dto = new NinoHijoResponse();
        dto.setId(hijo.id());
        dto.setNombreCompleto(hijo.nombreCompleto());
        dto.setFechaNacimiento(hijo.fechaNacimiento());
        return dto;
    }
}
