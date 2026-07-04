package org.ups.inscripcionoratorio.inscripcion.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ups.inscripcionoratorio.inscripcion.domain.model.EstadoFicha;
import org.ups.inscripcionoratorio.inscripcion.domain.model.FichaFamiliar;
import org.ups.inscripcionoratorio.inscripcion.domain.model.NinoHijo;
import org.ups.inscripcionoratorio.inscripcion.domain.model.RepresentanteLegal;

class FichaFamiliarMapperTest {

    private final FichaFamiliarMapper mapper = new FichaFamiliarMapper();

    @Test
    @DisplayName("Dada una ficha nueva de dominio, cuando se mapea a entidad, entonces copia representante y no crea hijos")
    void dadaFichaNueva_cuandoSeMapeaAEntidad_entoncesCopiaRepresentante() {
        // given
        RepresentanteLegal representante = new RepresentanteLegal(
                "Maria Perez", "1710034065", "0991234567", "Av. Siempre Viva 123");
        FichaFamiliar ficha = FichaFamiliar.crear(representante);

        // when
        FichaFamiliarEntity entity = mapper.aNuevaEntidad(ficha);

        // then
        assertThat(entity.getNombreCompleto()).isEqualTo("Maria Perez");
        assertThat(entity.getCedula()).isEqualTo("1710034065");
        assertThat(entity.getCelular()).isEqualTo("0991234567");
        assertThat(entity.getDireccion()).isEqualTo("Av. Siempre Viva 123");
        assertThat(entity.getEstado()).isEqualTo(EstadoFicha.INCOMPLETA);
        assertThat(entity.getHijos()).isEmpty();
    }

    @Test
    @DisplayName("Dada una ficha de dominio con un hijo nuevo, cuando se sincroniza sobre una entidad existente, entonces solo se agrega el hijo nuevo")
    void dadaFichaConHijoNuevo_cuandoSeActualizaEntidad_entoncesAgregaSoloElNuevo() {
        // given
        RepresentanteLegal representante = new RepresentanteLegal(
                "Maria Perez", "1710034065", "0991234567", "Av. Siempre Viva 123");
        FichaFamiliarEntity entity = new FichaFamiliarEntity();
        entity.setId(1L);
        entity.setNombreCompleto(representante.nombreCompleto());
        entity.setCedula(representante.cedula());
        entity.setCelular(representante.celular());
        entity.setDireccion(representante.direccion());
        entity.setEstado(EstadoFicha.COMPLETA);
        entity.setFechaCreacion(LocalDateTime.now());
        NinoHijoEntity hijoExistente = new NinoHijoEntity();
        hijoExistente.setId(10L);
        hijoExistente.setNombreCompleto("Juan Perez");
        hijoExistente.setFechaNacimiento(LocalDate.of(2016, 5, 10));
        hijoExistente.setFichaFamiliar(entity);
        entity.getHijos().add(hijoExistente);

        FichaFamiliar dominio = mapper.aDominio(entity);
        dominio.agregarHijo(NinoHijo.nuevo("Ana Perez", LocalDate.of(2018, 9, 2)));

        // when
        mapper.actualizarEntidad(entity, dominio);

        // then
        assertThat(entity.getHijos()).hasSize(2);
        assertThat(entity.getHijos().get(0).getId()).isEqualTo(10L);
        assertThat(entity.getHijos().get(1).getId()).isNull();
        assertThat(entity.getHijos().get(1).getNombreCompleto()).isEqualTo("Ana Perez");
        assertThat(entity.getHijos().get(1).getFichaFamiliar()).isSameAs(entity);
    }

    @Test
    @DisplayName("Dada una entidad persistida, cuando se mapea a dominio, entonces reconstruye la ficha con sus hijos")
    void dadaEntidadPersistida_cuandoSeMapeaADominio_entoncesReconstruyeFicha() {
        // given
        FichaFamiliarEntity entity = new FichaFamiliarEntity();
        entity.setId(1L);
        entity.setNombreCompleto("Maria Perez");
        entity.setCedula("1710034065");
        entity.setCelular("0991234567");
        entity.setDireccion("Av. Siempre Viva 123");
        entity.setEstado(EstadoFicha.COMPLETA);
        entity.setFechaCreacion(LocalDateTime.of(2026, 7, 1, 9, 0));
        NinoHijoEntity hijoEntity = new NinoHijoEntity();
        hijoEntity.setId(10L);
        hijoEntity.setNombreCompleto("Juan Perez");
        hijoEntity.setFechaNacimiento(LocalDate.of(2016, 5, 10));
        entity.getHijos().add(hijoEntity);

        // when
        FichaFamiliar dominio = mapper.aDominio(entity);

        // then
        assertThat(dominio.getId()).isEqualTo(1L);
        assertThat(dominio.getEstado()).isEqualTo(EstadoFicha.COMPLETA);
        assertThat(dominio.getHijos()).hasSize(1);
        assertThat(dominio.getHijos().get(0).id()).isEqualTo(10L);
        assertThat(dominio.getRepresentanteLegal().cedula()).isEqualTo("1710034065");
    }
}
