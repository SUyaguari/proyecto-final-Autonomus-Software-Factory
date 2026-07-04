package org.ups.inscripcionoratorio.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.spring.CucumberContextConfiguration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FichaFamiliarSteps {

    @LocalServerPort
    private int puerto;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpResponse<String> ultimaRespuesta;
    private Long fichaId;

    @Before
    public void reiniciarEstado() {
        ultimaRespuesta = null;
        fichaId = null;
    }

    private String urlBase() {
        return "http://localhost:" + puerto + "/api/v1/fichas-familiares";
    }

    private HttpResponse<String> post(String url, String cuerpo) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Dado("que inicio una ficha de inscripción nueva")
    public void queInicioUnaFichaDeInscripcionNueva() {
        // sin estado previo que preparar; la ficha se crea en el paso "Cuando"
    }

    @Cuando("ingreso los datos del representante legal con nombre {string}, cedula {string}, celular {string} y direccion {string}")
    public void ingresoLosDatosDelRepresentanteLegal(String nombre, String cedula, String celular, String direccion) throws Exception {
        String cuerpo = objectMapper.writeValueAsString(new RepresentanteLegalPayload(nombre, cedula, celular, direccion));
        ultimaRespuesta = post(urlBase(), cuerpo);
        if (ultimaRespuesta.statusCode() == 201) {
            JsonNode json = objectMapper.readTree(ultimaRespuesta.body());
            fichaId = json.get("id").asLong();
        }
    }

    @Entonces("el sistema guarda esos datos y la ficha queda en estado {string} sin hijos")
    public void elSistemaGuardaEsosDatosYLaFichaQuedaEnEstadoSinHijos(String estadoEsperado) throws Exception {
        assertThat(ultimaRespuesta.statusCode()).isEqualTo(201);
        JsonNode json = objectMapper.readTree(ultimaRespuesta.body());
        assertThat(json.get("estado").asText()).isEqualTo(estadoEsperado);
        assertThat(json.get("hijos")).isEmpty();
    }

    @Dado("que el representante legal ya fue registrado en la ficha actual")
    public void queElRepresentanteLegalYaFueRegistrado() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(
                new RepresentanteLegalPayload("Maria Perez", "1710034065", "0991234567", "Av. Siempre Viva 123"));
        ultimaRespuesta = post(urlBase(), cuerpo);
        assertThat(ultimaRespuesta.statusCode()).isEqualTo(201);
        JsonNode json = objectMapper.readTree(ultimaRespuesta.body());
        fichaId = json.get("id").asLong();
    }

    @Dado("que agregué un primer hijo llamado {string} nacido el {string}")
    public void queAgregueUnPrimerHijoLlamado(String nombre, String fechaNacimiento) throws Exception {
        String cuerpo = objectMapper.writeValueAsString(new NinoHijoPayload(nombre, fechaNacimiento));
        ultimaRespuesta = post(urlBase() + "/" + fichaId + "/hijos", cuerpo);
        assertThat(ultimaRespuesta.statusCode()).isEqualTo(201);
    }

    @Cuando("agrego un segundo hijo llamado {string} nacido el {string}")
    public void agregoUnSegundoHijoLlamado(String nombre, String fechaNacimiento) throws Exception {
        String cuerpo = objectMapper.writeValueAsString(new NinoHijoPayload(nombre, fechaNacimiento));
        ultimaRespuesta = post(urlBase() + "/" + fichaId + "/hijos", cuerpo);
    }

    @Entonces("ambos hijos quedan vinculados al mismo representante sin repetir sus datos")
    public void ambosHijosQuedanVinculadosAlMismoRepresentante() throws Exception {
        assertThat(ultimaRespuesta.statusCode()).isEqualTo(201);
        JsonNode json = objectMapper.readTree(ultimaRespuesta.body());
        assertThat(json.get("hijos")).hasSize(2);
        assertThat(json.get("representanteLegal").get("cedula").asText()).isEqualTo("1710034065");
    }

    @Entonces("la ficha queda en estado {string}")
    public void laFichaQuedaEnEstado(String estadoEsperado) throws Exception {
        JsonNode json = objectMapper.readTree(ultimaRespuesta.body());
        assertThat(json.get("estado").asText()).isEqualTo(estadoEsperado);
    }

    @Dado("que el formulario tiene datos obligatorios del representante incompletos")
    public void queElFormularioTieneDatosObligatoriosIncompletos() {
        // el payload incompleto se envía directamente en el paso "Cuando"
    }

    @Cuando("intento guardar o crear la ficha")
    public void intentoGuardarCrearLaFicha() throws Exception {
        String cuerpoIncompleto = "{\"nombreCompleto\":\"Carlos Ruiz\",\"cedula\":\"1710034065\"}";
        ultimaRespuesta = post(urlBase(), cuerpoIncompleto);
    }

    @Entonces("el sistema rechaza la operación indicando los campos obligatorios faltantes")
    public void elSistemaRechazaLaOperacionIndicandoCamposFaltantes() throws Exception {
        assertThat(ultimaRespuesta.statusCode()).isEqualTo(400);
        JsonNode json = objectMapper.readTree(ultimaRespuesta.body());
        assertThat(json.get("camposFaltantes")).isNotEmpty();
    }

    private record RepresentanteLegalPayload(String nombreCompleto, String cedula, String celular, String direccion) {
    }

    private record NinoHijoPayload(String nombreCompleto, String fechaNacimiento) {
    }
}
