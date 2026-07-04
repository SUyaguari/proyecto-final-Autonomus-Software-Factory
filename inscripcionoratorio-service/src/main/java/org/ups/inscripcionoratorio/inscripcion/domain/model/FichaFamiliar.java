package org.ups.inscripcionoratorio.inscripcion.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FichaFamiliar {

    private final Long id;
    private RepresentanteLegal representanteLegal;
    private final List<NinoHijo> hijos;
    private EstadoFicha estado;
    private final LocalDateTime fechaCreacion;

    private FichaFamiliar(Long id, RepresentanteLegal representanteLegal, List<NinoHijo> hijos,
                          EstadoFicha estado, LocalDateTime fechaCreacion) {
        if (representanteLegal == null) {
            throw new IllegalArgumentException("Una ficha familiar no puede existir sin representante legal");
        }
        this.id = id;
        this.representanteLegal = representanteLegal;
        this.hijos = new ArrayList<>(hijos);
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public static FichaFamiliar crear(RepresentanteLegal representanteLegal) {
        return new FichaFamiliar(null, representanteLegal, List.of(), EstadoFicha.INCOMPLETA, LocalDateTime.now());
    }

    public static FichaFamiliar reconstruir(Long id, RepresentanteLegal representanteLegal, List<NinoHijo> hijos,
                                             EstadoFicha estado, LocalDateTime fechaCreacion) {
        return new FichaFamiliar(id, representanteLegal, hijos, estado, fechaCreacion);
    }

    public FichaFamiliar conId(Long nuevoId) {
        return new FichaFamiliar(nuevoId, representanteLegal, hijos, estado, fechaCreacion);
    }

    public void agregarHijo(NinoHijo hijo) {
        hijos.add(hijo);
        estado = EstadoFicha.COMPLETA;
    }

    public void actualizarRepresentante(RepresentanteLegal nuevoRepresentante) {
        if (nuevoRepresentante == null) {
            throw new IllegalArgumentException("El nuevo representante legal no puede ser nulo");
        }
        this.representanteLegal = nuevoRepresentante;
    }

    public boolean puedeCancelarse() {
        return estado == EstadoFicha.INCOMPLETA;
    }

    public Long getId() {
        return id;
    }

    public RepresentanteLegal getRepresentanteLegal() {
        return representanteLegal;
    }

    public List<NinoHijo> getHijos() {
        return Collections.unmodifiableList(hijos);
    }

    public EstadoFicha getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
