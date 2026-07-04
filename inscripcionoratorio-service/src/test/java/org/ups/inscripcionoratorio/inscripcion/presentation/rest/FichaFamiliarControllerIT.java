package org.ups.inscripcionoratorio.inscripcion.presentation.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class FichaFamiliarControllerIT {

    private static final String BASE_PATH = "/fichas-familiares";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private long crearFicha(String cedula, String celular) throws Exception {
        String body = """
                {"nombreCompleto":"Representante Test","cedula":"%s","celular":"%s","direccion":"Calle Test 1"}
                """.formatted(cedula, celular);
        MvcResult result = mockMvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    @Test
    @DisplayName("Dado un representante con datos completos y cédula válida, cuando creo la ficha, entonces responde 201 con estado INCOMPLETA (AC#1)")
    void dadoRepresentanteValido_cuandoCreoFicha_entoncesRetorna201Incompleta() throws Exception {
        String body = """
                {"nombreCompleto":"Pedro Soto","cedula":"1710034065","celular":"0991112222","direccion":"Calle 1"}
                """;

        mockMvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("INCOMPLETA"))
                .andExpect(jsonPath("$.hijos").isEmpty());
    }

    @Test
    @DisplayName("Dado un representante con campos obligatorios incompletos, cuando intento crear la ficha, entonces responde 400 con camposFaltantes (AC#3/FR-007)")
    void dadoRepresentanteIncompleto_cuandoCreoFicha_entoncesRetorna400() throws Exception {
        String body = """
                {"nombreCompleto":"Carlos Ruiz","cedula":"1710034065"}
                """;

        mockMvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposFaltantes").isArray());
    }

    @Test
    @DisplayName("Dada una cédula con dígito verificador inválido, cuando creo la ficha, entonces responde 400 (FR-002a)")
    void dadaCedulaInvalida_cuandoCreoFicha_entoncesRetorna400() throws Exception {
        String body = """
                {"nombreCompleto":"Carlos Ruiz","cedula":"1234567890","celular":"0987654321","direccion":"Calle Falsa 456"}
                """;

        mockMvc.perform(post(BASE_PATH).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Dada una ficha existente, cuando agrego dos hijos, entonces ambos quedan vinculados y la ficha pasa a COMPLETA (AC#2)")
    void dadaFichaExistente_cuandoAgregoDosHijos_entoncesQuedanVinculados() throws Exception {
        long fichaId = crearFicha("1710034065", "0991112233");

        String hijo1 = """
                {"nombreCompleto":"Hijo Uno","fechaNacimiento":"2016-05-10"}
                """;
        mockMvc.perform(post(BASE_PATH + "/" + fichaId + "/hijos").contentType(MediaType.APPLICATION_JSON).content(hijo1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("COMPLETA"))
                .andExpect(jsonPath("$.hijos.length()").value(1));

        String hijo2 = """
                {"nombreCompleto":"Hijo Dos","fechaNacimiento":"2018-09-02"}
                """;
        mockMvc.perform(post(BASE_PATH + "/" + fichaId + "/hijos").contentType(MediaType.APPLICATION_JSON).content(hijo2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hijos.length()").value(2))
                .andExpect(jsonPath("$.representanteLegal.cedula").value("1710034065"));
    }

    @Test
    @DisplayName("Dado un hijo con datos propios incompletos, cuando lo agrego, entonces responde 400 (FR-013)")
    void dadoHijoConDatosIncompletos_cuandoAgrego_entoncesRetorna400() throws Exception {
        long fichaId = crearFicha("1710034065", "0991112244");

        String hijoIncompleto = """
                {"nombreCompleto":""}
                """;
        mockMvc.perform(post(BASE_PATH + "/" + fichaId + "/hijos").contentType(MediaType.APPLICATION_JSON).content(hijoIncompleto))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Dado un fichaId inexistente, cuando agrego un hijo, entonces responde 404")
    void dadoFichaIdInexistente_cuandoAgregoHijo_entoncesRetorna404() throws Exception {
        String hijo = """
                {"nombreCompleto":"Hijo X","fechaNacimiento":"2020-01-01"}
                """;
        mockMvc.perform(post(BASE_PATH + "/999999/hijos").contentType(MediaType.APPLICATION_JSON).content(hijo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Dada la ficha COMPLETA precargada, cuando edito el representante, entonces responde 200 con datos actualizados (FR-010)")
    void dadaFichaConHijos_cuandoEditoRepresentante_entoncesRetorna200() throws Exception {
        String nuevoRepresentante = """
                {"nombreCompleto":"Maria Perez","cedula":"1710034065","celular":"0999999999","direccion":"Av. Siempre Viva 123"}
                """;
        mockMvc.perform(put(BASE_PATH + "/1/representante").contentType(MediaType.APPLICATION_JSON).content(nuevoRepresentante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.representanteLegal.celular").value("0999999999"));
    }

    @Test
    @DisplayName("Dada una ficha sin hijos, cuando la cancelo, entonces responde 200 con mensaje de confirmación (FR-011)")
    void dadaFichaSinHijos_cuandoLaCancelo_entoncesRetorna200ConMensaje() throws Exception {
        long fichaId = crearFicha("1710034065", "0991112255");

        mockMvc.perform(delete(BASE_PATH + "/" + fichaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Ficha familiar eliminada correctamente."));
    }

    @Test
    @DisplayName("Dada la ficha COMPLETA precargada, cuando intento cancelarla, entonces responde 409")
    void dadaFichaConHijos_cuandoIntentoCancelarla_entoncesRetorna409() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/1"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Dado un fichaId inexistente, cuando intento cancelarlo, entonces responde 404")
    void dadoFichaIdInexistente_cuandoLaCancelo_entoncesRetorna404() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/999999"))
                .andExpect(status().isNotFound());
    }
}
