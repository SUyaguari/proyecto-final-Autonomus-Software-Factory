# Research: Ficha familiar unificada

No quedaron marcadores `NEEDS CLARIFICATION` en el Technical Context: todas las decisiones
técnicas se derivan del stack ya establecido en `build.gradle` y de la constitución del proyecto.
Este documento resuelve las decisiones de diseño necesarias para pasar de la especificación
(WHAT/WHY) a un plan implementable (HOW).

## 1. Separación de aggregate: representante embebido vs. tabla independiente

- **Decision**: `RepresentanteLegal` se modela como un value object embebido dentro del
  agregado `FichaFamiliar` (sin tabla ni identidad propia). `NinoHijo` se modela como entidad de
  dominio con tabla propia y relación many-to-one hacia `FichaFamiliar`.
- **Rationale**: La especificación fija una relación 1:1 entre ficha y representante y excluye
  explícitamente (Assumptions) la reutilización de un representante entre fichas distintas. Una
  tabla independiente para el representante añadiría una relación y una clave foránea sin
  aportar valor en el alcance actual (Principio III: YAGNI/KISS).
- **Alternatives considered**: Tabla `representante_legal` independiente con relación 1:1 hacia
  `ficha_familiar` — rechazada por ahora; si una historia futura habilita la reutilización de
  representantes entre fichas (fuera de alcance de esta historia), se puede migrar a una tabla
  independiente sin romper el contrato público (los DTOs de la API no exponen la separación
  interna).
- **Nota (post `/speckit-analyze`)**: dado que el representante se valida completo en el momento
  de crear/editar la ficha (FR-007), no existe un estado "ficha con representante incompleto"
  persistido. Por eso se retiró `DatosRepresentanteIncompletosException` del diseño (ver
  data-model.md → NinoHijo): esa excepción habría sido código inalcanzable en
  `AgregarHijoUseCase`, ya que ninguna ficha persistida puede tener un representante incompleto.

## 2. Política de descarte de fichas sin hijos (FR-011)

- **Decision**: Se expone una operación explícita `DELETE /fichas-familiares/{fichaId}`
  (cancelar ficha) que el cliente invoca cuando el Receptor de Inscripciones abandona o cancela
  el flujo antes de registrar algún hijo. El backend permite el borrado únicamente si la ficha
  tiene cero hijos vinculados (`EstadoFicha.INCOMPLETA`); si ya tiene al menos un hijo, se
  rechaza con `FichaConHijosNoCancelableException`.
- **Rationale**: Un backend REST no tiene noción nativa de "sesión de UI". La redacción de la
  spec ("abandona o cancela") sugiere una acción determinista y testable, no un mecanismo
  implícito. Esto evita introducir un valor de expiración/timeout arbitrario que la spec no
  especifica.
- **Nota sobre el código de respuesta**: El `DELETE` exitoso responde `200 OK` con un cuerpo
  `MensajeResponse` (`{"mensaje": "Ficha familiar eliminada correctamente."}`) en vez de
  `204 No Content` sin cuerpo. Se ajustó así a pedido del usuario, ya que al probar la API
  manualmente (Swagger UI/curl) un `204` sin cuerpo se percibía como "no dio respuesta" aunque
  la operación sí se completó; un mensaje explícito de confirmación mejora la experiencia de
  quien consume la API manualmente, sin cambiar la semántica (sigue siendo una eliminación
  exitosa e idempotente en su efecto).
- **Alternatives considered**: (a) Job programado que purga fichas incompletas tras N minutos —
  rechazada: introduce un parámetro de tiempo sin un valor por defecto razonable especificado
  por el negocio, y complejidad operativa (scheduler) no solicitada. (b) No persistir el
  representante hasta que se agregue el primer hijo (borrador en memoria) — rechazada:
  contradice FR-003, que exige persistir los datos del representante al guardarlos,
  independientemente de si ya hay hijos.

## 3. Validación de cédula ecuatoriana (FR-002a)

- **Decision**: `CedulaEcuatorianaValidator` en la capa de dominio implementa el algoritmo
  oficial: 10 dígitos numéricos; los dos primeros dígitos representan la provincia (01-24, o 30
  para casos especiales/extranjeros según el registro civil); el tercer dígito debe ser menor a
  6 para cédulas de persona natural; el décimo dígito es un dígito verificador calculado con
  coeficientes alternados (2,1,2,1,2,1,2,1,2) sobre los primeros 9 dígitos, módulo 10.
- **Rationale**: Resuelve la clarificación aceptada ("cédula ecuatoriana con dígito
  verificador") con un algoritmo estándar, determinista y unitariamente testable (alineado con
  el Principio V: los validadores requieren ≥80% de cobertura).
- **Alternatives considered**: Validación de solo longitud (10 dígitos) sin dígito verificador —
  descartada porque la clarificación fue explícita en pedir el dígito verificador.

## 4. Integración de OpenAPI Generator en el build

- **Decision**: Añadir el plugin Gradle `org.openapi.generator` (última versión estable
  compatible con Gradle/Spring Boot 4.1). El contrato se escribe primero en
  `specs/001-ficha-familiar-unificada/contracts/ficha-familiar.openapi.yaml` (fuente de verdad
  para el proceso de spec-kit) y se copia a
  `src/main/resources/openapi/ficha-familiar.openapi.yaml`, que es de donde realmente lee
  `inputSpec` en `build.gradle` — replicando el mismo patrón de `citasalud-service` (contrato
  empaquetado dentro de los recursos del artefacto, no solo en `specs/`). Ambas copias deben
  mantenerse idénticas; si el contrato cambia, se edita en `specs/.../contracts/` y se vuelve a
  copiar a `src/main/resources/openapi/`. El plugin genera
  interfaces de controlador (`interfaceOnly: true`, generador `spring`) y modelos (DTOs) en un
  paquete generado (`org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated`). El
  controlador de `presentation/rest` implementa la interfaz generada.
- **Rationale**: Cumple el Principio IV (API-First): el contrato es la fuente de verdad y el
  código de la API (interfaces/modelos) se genera de él, no al revés. Las anotaciones Swagger
  (`@Operation`, `@ApiResponse`, etc.) del código generado se mantienen habilitadas, siguiendo
  el mismo patrón ya usado en `citasalud-service`, en vez de desactivarlas con
  `documentationProvider: "none"` / `annotationLibrary: "none"`.
- **Nota sobre `servers` en el contrato**: se añadió una entrada absoluta
  `http://localhost:8080/api/v1` (además de la relativa `/api/v1`) en el bloque `servers:` del
  contrato. Motivo: al abrir el `.yaml` con el editor de OpenAPI de IntelliJ y usar su función
  de "Run"/probar directamente desde el archivo, IntelliJ renderiza el preview en su propio
  servidor web interno (`localhost:63342`) y resuelve una URL relativa (`/api/v1`) contra ese
  servidor interno en vez de contra la app real — resultando en un 404 servido por IntelliJ
  (`Server: IntelliJ IDEA`), no por la aplicación. Una URL de servidor absoluta evita esa
  ambigüedad sin importar desde qué herramienta (IntelliJ, Postman, Swagger UI embebido) se
  pruebe el contrato.
- **Nota sobre versión de swagger-annotations**: no se fija una versión propia de
  `io.swagger.core.v3:swagger-annotations` en `build.gradle`. `springdoc-openapi-starter-webmvc-ui`
  (ver más abajo) ya trae transitivamente `swagger-annotations-jakarta` en la versión correcta;
  fijar una versión propia de `swagger-annotations` (no-jakarta) causó un choque de clases en
  runtime (`NoSuchMethodError: Schema.$dynamicRef()`) porque ambos artefactos declaran el mismo
  paquete `io.swagger.v3.oas.annotations.*` con versiones distintas.
- **UI de Swagger en runtime**: se añadió `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3`
  para exponer `/api/v1/v3/api-docs` (JSON del contrato generado en runtime a partir de las
  anotaciones) y `/api/v1/swagger-ui/index.html` (UI interactiva), a pedido explícito del
  usuario.
- **Alternatives considered**: Escribir manualmente los DTOs/interfaces de controlador —
  rechazada porque contradice explícitamente el Principio IV de la constitución.

## 5. Cobertura de pruebas (JaCoCo)

- **Decision**: Añadir el plugin `jacoco`, con `jacocoTestReport` ligado a `check`, y una regla
  de verificación (`jacocoTestCoverageVerification`) con mínimo 80% de cobertura de líneas
  global y por clase para paquetes `domain`, `application` e `infrastructure.persistence`
  (mappers). Se excluyen del cálculo: la clase principal
  (`InscripcionoratorioServiceApplication`), el paquete de modelos/interfaces generados por
  OpenAPI Generator, las entidades JPA (`infrastructure.persistence.*Entity`, sin lógica de
  negocio) y clases de configuración.
- **Rationale**: Cumple literalmente el Principio V, incluyendo sus exclusiones explícitas.
- **Alternatives considered**: Cobertura solo global sin regla por clase — rechazada porque la
  constitución exige explícitamente ambas métricas.

## 6. Estrategia de pruebas por nivel

- **Decision**:
  - Unitarias (BDD, JUnit 5 + Mockito + BDDMockito): `CedulaEcuatorianaValidatorTest`,
    `FichaFamiliarTest` (reglas de estado/invariantes del dominio), los `UseCaseTest` de
    aplicación (con el repositorio de dominio mockeado) y `FichaFamiliarMapperTest`.
  - Integración (`@SpringBootTest` + `MockMvc` + H2 en memoria): `FichaFamiliarControllerIT`,
    cubriendo los 4 endpoints contra una base de datos real (H2), incluyendo persistencia y
    manejo de errores vía `GlobalExceptionHandler`.
  - Funcionales (Cucumber/Gherkin): un feature file que traduce literalmente los 3 escenarios
    Given/When/Then de `spec.md` (registrar representante y habilitar hijos; agregar segundo
    hijo vinculado automáticamente; bloqueo por datos incompletos), ejecutado sobre el mismo
    contexto Spring Boot + H2.
- **Rationale**: Cumple el Principio II (disciplina de pruebas multinivel) usando exactamente
  las herramientas mandatadas por la constitución.
- **Alternatives considered**: Omitir Cucumber y cubrir los escenarios solo con MockMvc —
  descartada; la constitución habilita Cucumber quando el escenario lo amerite, y aquí los ACs
  ya están en Gherkin, lo que hace directa la trazabilidad requisito↔prueba.

## 7. Esquema de base de datos y datos de prueba

- **Decision**: Reutilizar el mismo patrón ya aplicado en otros servicios del equipo (p. ej.
  `citasalud-service`): definir el esquema explícitamente en `src/main/resources/schema.sql`
  (`CREATE TABLE IF NOT EXISTS ...` para `ficha_familiar` y `nino_hijo`), desactivar la
  generación automática de Hibernate (`spring.jpa.hibernate.ddl-auto: none`) y precargar datos
  de prueba idempotentes en `src/main/resources/data.sql` (sentencias `MERGE INTO`, con IDs
  fijos para que sean reproducibles entre reinicios y sirvan de fixture a las pruebas de
  integración/funcionales). Se activa con `spring.sql.init.mode: always` en `application.yaml`.
- **Rationale**: Consistencia con el resto de servicios del equipo (mismo patrón, misma curva de
  aprendizaje); un esquema explícito es más fácil de auditar y de mantener alineado con
  `data-model.md` que depender de la inferencia automática de Hibernate; los datos precargados
  permiten validar manualmente el `quickstart.md` sin tener que crear una ficha desde cero cada
  vez.
- **Alternatives considered**: Dejar que Hibernate genere el esquema (`ddl-auto: update`/`create`)
  — rechazada por ser menos predecible y no seguir el patrón ya adoptado por el equipo; no
  precargar datos y depender solo de los `curl` del quickstart — rechazada porque dificulta
  probar rápidamente escenarios con una ficha ya `COMPLETA` sin repetir los pasos de creación.
