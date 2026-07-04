package org.ups.inscripcionoratorio.inscripcion.presentation.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ups.inscripcionoratorio.inscripcion.application.usecase.ActualizarRepresentanteUseCase;
import org.ups.inscripcionoratorio.inscripcion.application.usecase.AgregarHijoUseCase;
import org.ups.inscripcionoratorio.inscripcion.application.usecase.CancelarFichaUseCase;
import org.ups.inscripcionoratorio.inscripcion.application.usecase.RegistrarRepresentanteUseCase;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated.api.FichasFamiliaresApi;
import org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated.model.FichaFamiliarResponse;
import org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated.model.MensajeResponse;
import org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated.model.NinoHijoRequest;
import org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated.model.RepresentanteLegalRequest;

@RestController
public class FichaFamiliarController implements FichasFamiliaresApi {

    private final RegistrarRepresentanteUseCase registrarRepresentanteUseCase;
    private final AgregarHijoUseCase agregarHijoUseCase;
    private final ActualizarRepresentanteUseCase actualizarRepresentanteUseCase;
    private final CancelarFichaUseCase cancelarFichaUseCase;
    private final FichaFamiliarDtoMapper dtoMapper;

    public FichaFamiliarController(RegistrarRepresentanteUseCase registrarRepresentanteUseCase,
                                    AgregarHijoUseCase agregarHijoUseCase,
                                    ActualizarRepresentanteUseCase actualizarRepresentanteUseCase,
                                    CancelarFichaUseCase cancelarFichaUseCase,
                                    FichaFamiliarDtoMapper dtoMapper) {
        this.registrarRepresentanteUseCase = registrarRepresentanteUseCase;
        this.agregarHijoUseCase = agregarHijoUseCase;
        this.actualizarRepresentanteUseCase = actualizarRepresentanteUseCase;
        this.cancelarFichaUseCase = cancelarFichaUseCase;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public ResponseEntity<FichaFamiliarResponse> crearFichaFamiliar(RepresentanteLegalRequest representanteLegalRequest) {
        FichaFamiliar fichaFamiliar = registrarRepresentanteUseCase.ejecutar(dtoMapper.aDominio(representanteLegalRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.aResponse(fichaFamiliar));
    }

    @Override
    public ResponseEntity<FichaFamiliarResponse> agregarHijo(Long fichaId, NinoHijoRequest ninoHijoRequest) {
        FichaFamiliar fichaFamiliar = agregarHijoUseCase.ejecutar(fichaId, dtoMapper.aDominio(ninoHijoRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.aResponse(fichaFamiliar));
    }

    @Override
    public ResponseEntity<FichaFamiliarResponse> actualizarRepresentanteLegal(Long fichaId, RepresentanteLegalRequest representanteLegalRequest) {
        FichaFamiliar fichaFamiliar = actualizarRepresentanteUseCase.ejecutar(fichaId, dtoMapper.aDominio(representanteLegalRequest));
        return ResponseEntity.ok(dtoMapper.aResponse(fichaFamiliar));
    }

    @Override
    public ResponseEntity<MensajeResponse> cancelarFichaFamiliar(Long fichaId) {
        cancelarFichaUseCase.ejecutar(fichaId);
        MensajeResponse cuerpo = new MensajeResponse();
        cuerpo.setMensaje("Ficha familiar eliminada correctamente.");
        return ResponseEntity.ok(cuerpo);
    }
}
