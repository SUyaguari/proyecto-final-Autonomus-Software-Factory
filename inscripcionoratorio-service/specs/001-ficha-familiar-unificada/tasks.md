---

description: "Task list for Ficha familiar unificada (US-01)"
---

# Tasks: Ficha familiar unificada

**Input**: Design documents from `/specs/001-ficha-familiar-unificada/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/ficha-familiar.openapi.yaml, quickstart.md

**Tests**: Incluidas — el Principio II de la constitución exige pruebas unitarias BDD, de
integración y funcionales para toda lógica de negocio nueva, y `plan.md`/`research.md` ya
definieron explícitamente esta estrategia de pruebas.

**Organization**: Esta feature tiene una única historia de usuario (US-01, prioridad P1), por lo
que Setup + Foundational + US1 conforman el 100% del alcance (no hay historias P2/P3).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Se puede ejecutar en paralelo (archivos distintos, sin dependencias pendientes)
- **[US1]**: Tareas de la historia de usuario 1 (única historia de esta feature)
- Todas las rutas de archivo son relativas a la raíz del repositorio

## Path Conventions

Proyecto único (monolito modular Gradle): `src/main/java/org/ups/inscripcionoratorio/`,
`src/test/java/org/ups/inscripcionoratorio/`, `src/main/resources/`, `src/test/resources/`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar el build para soportar API-First (OpenAPI Generator), cobertura (JaCoCo) y
pruebas funcionales (Cucumber), y crear la estructura de paquetes del módulo `inscripcion`.

- [X] T001 [P] Crear la estructura de paquetes vacíos para el módulo `inscripcion`
  (`domain/{model,validation,repository,exception}`, `application/usecase`,
  `infrastructure/persistence`, `presentation/rest`) y `shared/infrastructure/web`, bajo
  `src/main/java/org/ups/inscripcionoratorio/`, según `plan.md` → Project Structure
- [X] T002 Añadir el plugin Gradle `org.openapi.generator` en `build.gradle`. El contrato se
  copió de `specs/001-ficha-familiar-unificada/contracts/ficha-familiar.openapi.yaml` a
  `src/main/resources/openapi/ficha-familiar.openapi.yaml` (mismo patrón que
  `citasalud-service`), que es de donde lee `inputSpec`
  (`generatorName = "spring"`, `interfaceOnly = true`, modelos y interfaces en el paquete
  `org.ups.inscripcionoratorio.inscripcion.presentation.rest.generated`), según research.md §4
- [X] T003 Añadir el plugin Gradle `jacoco` en `build.gradle`, con `jacocoTestReport` ligado a
  `check` y una tarea `jacocoTestCoverageVerification` (mínimo 80% líneas, global y por clase,
  sobre `domain`, `application` e `infrastructure.persistence`; excluyendo la clase principal,
  el paquete `presentation.rest.generated`, las entidades JPA y clases de configuración), según
  research.md §5
- [X] T004 Añadir dependencias de prueba `io.cucumber:cucumber-java` y
  `io.cucumber:cucumber-junit-platform-engine` (`testImplementation`) en `build.gradle`, según
  research.md §6

**Checkpoint**: El build compila con los plugins nuevos (aún sin código de dominio); `./gradlew build` no debe fallar por configuración.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestructura compartida que debe existir antes de implementar la historia de
usuario: esquema de base de datos, datos de prueba y el contrato de respuesta de error.

**⚠️ CRITICAL**: Ninguna tarea de la Fase 3 puede completarse sin esta fase.

- [X] T005 [P] Configurar `spring.jpa.hibernate.ddl-auto: none` y `spring.sql.init.mode: always`
  en `src/main/resources/application.yaml`, según research.md §7
- [X] T006 [P] Crear `src/main/resources/schema.sql` con las tablas `ficha_familiar` y
  `nino_hijo` (DDL exacto en data-model.md → Esquema SQL)
- [X] T007 [P] Crear `src/main/resources/data.sql` con datos de prueba idempotentes
  (`MERGE INTO`): una ficha `COMPLETA` (representante + 2 hijos) y una ficha `INCOMPLETA` (solo
  representante), según data-model.md → Datos de prueba
- [X] T008 [P] Crear `ErrorResponse` (cuerpo de error consistente: `timestamp`, `status`,
  `error`, `message`, `path`, `camposFaltantes`) en
  `src/main/java/org/ups/inscripcionoratorio/shared/infrastructure/web/ErrorResponse.java`

**Checkpoint**: Esquema y datos de prueba disponibles; forma de error definida. Lista para
implementar la historia de usuario.

---

## Phase 3: User Story 1 - Registrar representante y vincular varios hijos en una sola ficha (Priority: P1) 🎯 MVP

**Goal**: El Receptor de Inscripciones registra al representante legal y agrega uno o más hijos
a la misma ficha sin repetir los datos del adulto (spec.md, Acceptance Scenarios 1-3), pudiendo
además editar el representante (FR-010) y cancelar una ficha sin hijos (FR-011).

**Independent Test**: Levantar el servicio, ejecutar los Escenarios 1-3 de `quickstart.md`
(crear ficha, agregar dos hijos, intentar agregar un hijo con representante incompleto) y
confirmar los códigos de estado y cuerpos de respuesta esperados.

### Tests for User Story 1 (escribir primero; deben fallar antes de implementar)

- [X] T009 [P] [US1] Prueba unitaria BDD `CedulaEcuatorianaValidatorTest` (cédulas válidas e
  inválidas, dígito verificador) en
  `src/test/java/org/ups/inscripcionoratorio/inscripcion/domain/validation/CedulaEcuatorianaValidatorTest.java`
- [X] T010 [P] [US1] Prueba unitaria BDD `FichaFamiliarTest` (invariantes de estado
  INCOMPLETA→COMPLETA, no permitir hijo sin representante completo) en
  `src/test/java/org/ups/inscripcionoratorio/inscripcion/domain/model/FichaFamiliarTest.java`
- [X] T011 [P] [US1] Prueba unitaria BDD `RegistrarRepresentanteUseCaseTest` (Mockito/BDDMockito;
  incluye el caso de representante con campos obligatorios incompletos, FR-007) en
  `src/test/java/org/ups/inscripcionoratorio/inscripcion/application/RegistrarRepresentanteUseCaseTest.java`
- [X] T012 [P] [US1] Prueba unitaria BDD `AgregarHijoUseCaseTest` (Mockito/BDDMockito; incluye el
  caso de datos propios del hijo incompletos, FR-013) en
  `src/test/java/org/ups/inscripcionoratorio/inscripcion/application/AgregarHijoUseCaseTest.java`
- [X] T013 [P] [US1] Prueba unitaria BDD `ActualizarRepresentanteUseCaseTest` en
  `src/test/java/org/ups/inscripcionoratorio/inscripcion/application/ActualizarRepresentanteUseCaseTest.java`
- [X] T014 [P] [US1] Prueba unitaria BDD `CancelarFichaUseCaseTest` en
  `src/test/java/org/ups/inscripcionoratorio/inscripcion/application/CancelarFichaUseCaseTest.java`
- [X] T015 [P] [US1] Prueba unitaria `FichaFamiliarMapperTest` (mapper manual dominio↔entidad
  JPA) en
  `src/test/java/org/ups/inscripcionoratorio/inscripcion/infrastructure/persistence/FichaFamiliarMapperTest.java`
- [X] T016 [P] [US1] Feature Cucumber `ficha-familiar-unificada.feature`, traduciendo
  literalmente los 3 Acceptance Scenarios de `spec.md`, en
  `src/test/resources/features/ficha-familiar-unificada.feature`
- [X] T017 [P] [US1] Prueba de integración `FichaFamiliarControllerIT` (`@SpringBootTest` +
  `MockMvc` + H2) cubriendo los 4 endpoints del contrato (crear, agregar hijo, editar
  representante, cancelar), incluyendo 400 por representante incompleto/cédula inválida al
  crear o editar la ficha (FR-007/FR-002a), 400 por datos del hijo incompletos al agregarlo
  (FR-013), 404 y 409, en
  `src/test/java/org/ups/inscripcionoratorio/inscripcion/presentation/rest/FichaFamiliarControllerIT.java`

### Implementation for User Story 1 — Dominio

- [X] T018 [P] [US1] Enum `EstadoFicha` (`INCOMPLETA`, `COMPLETA`) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/domain/model/EstadoFicha.java`
- [X] T019 [P] [US1] Value object `RepresentanteLegal` (nombreCompleto, cedula, celular,
  direccion; validación de obligatoriedad) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/domain/model/RepresentanteLegal.java`
- [X] T020 [P] [US1] Entidad de dominio `NinoHijo` (nombreCompleto, fechaNacimiento; validación
  de obligatoriedad) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/domain/model/NinoHijo.java`
- [X] T021 [US1] Agregado raíz `FichaFamiliar` (representanteLegal, hijos, estado derivado,
  fechaCreacion; invariantes de data-model.md) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/domain/model/FichaFamiliar.java`
  (depende de T018, T019, T020)
- [X] T022 [P] [US1] `CedulaEcuatorianaValidator` (algoritmo módulo 10, research.md §3) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/domain/validation/CedulaEcuatorianaValidator.java`
- [X] T023 [P] [US1] Excepciones de dominio `CedulaInvalidaException`,
  `FichaNoEncontradaException`, `FichaConHijosNoCancelableException` en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/domain/exception/` (se retiró
  `DatosHijoIncompletosException`: FR-013 ya lo cubre Bean Validation sobre `NinoHijoRequest`
  generado; sería código inalcanzable, mismo motivo que `DatosRepresentanteIncompletosException`
  en I1)
- [X] T024 [US1] Puerto `FichaFamiliarRepository` (interfaz del dominio) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/domain/repository/FichaFamiliarRepository.java`
  (depende de T021)

### Implementation for User Story 1 — Aplicación

- [X] T025 [P] [US1] `RegistrarRepresentanteUseCase` (crea la ficha, valida que el representante
  esté completo antes de persistir, FR-001/002/002a/003/004/007) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/application/usecase/RegistrarRepresentanteUseCase.java`
  (depende de T021, T022, T024)
- [X] T026 [P] [US1] `AgregarHijoUseCase` (agrega hijo, valida datos propios del hijo, actualiza
  estado; el representante ya está garantizado completo por construcción,
  FR-005/006/008/009/012/013) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/application/usecase/AgregarHijoUseCase.java`
  (depende de T021, T024)
- [X] T027 [P] [US1] `ActualizarRepresentanteUseCase` (edita representante en cualquier momento,
  validando que los campos obligatorios sigan completos, FR-007/FR-010) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/application/usecase/ActualizarRepresentanteUseCase.java`
  (depende de T022, T024)
- [X] T028 [P] [US1] `CancelarFichaUseCase` (elimina ficha solo si `INCOMPLETA`, FR-011) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/application/usecase/CancelarFichaUseCase.java`
  (depende de T024)

### Implementation for User Story 1 — Infraestructura

- [X] T029 [P] [US1] `FichaFamiliarEntity` (`@Entity` JPA, mapea `ficha_familiar`) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/infrastructure/persistence/FichaFamiliarEntity.java`
- [X] T030 [P] [US1] `NinoHijoEntity` (`@Entity` JPA, mapea `nino_hijo`) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/infrastructure/persistence/NinoHijoEntity.java`
- [X] T031 [P] [US1] `FichaFamiliarJpaRepository` (Spring Data JPA) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/infrastructure/persistence/FichaFamiliarJpaRepository.java`
  (depende de T029, T030)
- [X] T032 [P] [US1] `FichaFamiliarMapper` (mapper manual dominio↔entidad JPA) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/infrastructure/persistence/FichaFamiliarMapper.java`
  (depende de T021, T029, T030)
- [X] T033 [US1] `FichaFamiliarRepositoryImpl` (adaptador del puerto de dominio) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/infrastructure/persistence/FichaFamiliarRepositoryImpl.java`
  (depende de T024, T031, T032)

### Implementation for User Story 1 — Presentación

- [X] T034 [P] [US1] `FichaFamiliarDtoMapper` (mapper manual dominio↔DTOs generados por OpenAPI
  Generator) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/presentation/rest/FichaFamiliarDtoMapper.java`
  (depende de T002, T021)
- [X] T035 [US1] `FichaFamiliarController` implementando la interfaz generada por OpenAPI,
  delegando en los casos de uso (4 endpoints del contrato) en
  `src/main/java/org/ups/inscripcionoratorio/inscripcion/presentation/rest/FichaFamiliarController.java`
  (depende de T025, T026, T027, T028, T034)
- [X] T036 [P] [US1] `GlobalExceptionHandler` (`@RestControllerAdvice`) mapeando
  `MethodArgumentNotValidException` (Bean Validation de los DTOs generados, FR-002/FR-007/FR-013,
  con `camposFaltantes`) y las excepciones de dominio (T023) a `ErrorResponse` (T008) con los
  códigos HTTP del contrato (400/404/409) en
  `src/main/java/org/ups/inscripcionoratorio/shared/infrastructure/web/GlobalExceptionHandler.java`
  (depende de T008, T023)

### Cierre funcional (Cucumber)

- [X] T037 [US1] Definiciones de pasos Cucumber implementando el feature de T016 contra el
  contexto Spring Boot (`@SpringBootTest`) en
  `src/test/java/org/ups/inscripcionoratorio/bdd/steps/FichaFamiliarSteps.java` (depende de
  T016, T035)
- [X] T038 [US1] Runner `RunCucumberTest` (JUnit Platform Suite) en
  `src/test/java/org/ups/inscripcionoratorio/bdd/RunCucumberTest.java` (depende de T037)

**Checkpoint**: Los 3 Acceptance Scenarios de spec.md y los FR-010/FR-011 pasan tanto en
`FichaFamiliarControllerIT` como en el escenario Cucumber; la historia es demostrable de
extremo a extremo vía `quickstart.md`.

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Verificar cobertura, comportamiento observable y ausencia de datos sensibles en
logs antes de dar por cerrada la historia.

- [X] T039 [P] Ejecutar `./gradlew test jacocoTestReport jacocoTestCoverageVerification` y
  ajustar pruebas si algún paquete de `domain`/`application`/`infrastructure.persistence` queda
  bajo el 80% (Principio V)
- [X] T040 [P] Validar manualmente los 5 escenarios de `quickstart.md` contra el servicio
  levantado con `./gradlew bootRun`
- [X] T041 [P] Revisar los logs generados durante T040 y confirmar que no se registran cédula,
  celular ni otros datos sensibles en texto claro (Principio VI)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Fase 1)**: sin dependencias — puede iniciar de inmediato.
- **Foundational (Fase 2)**: depende de que Setup haya terminado (build.gradle configurado) —
  bloquea toda la Fase 3.
- **User Story 1 (Fase 3)**: depende de Foundational. Es la única historia; no hay fases P2/P3.
- **Polish (Fase 4)**: depende de que la Fase 3 esté completa.

### Dentro de la Fase 3

- Tests (T009-T017) se escriben antes que la implementación y deben fallar hasta que exista el
  código correspondiente.
- Dominio (T018-T024) antes que Aplicación (T025-T028).
- Aplicación antes que Infraestructura (T029-T033) e Presentación (T034-T036), ya que ambas
  dependen del puerto `FichaFamiliarRepository` (T024) y/o de los casos de uso.
- Presentación antes que el cierre funcional Cucumber (T037-T038), que requiere el controlador
  funcionando.

### Parallel Opportunities

- Setup: T001 en paralelo con T002 (T002-T004 son secuenciales entre sí por editar el mismo
  `build.gradle`).
- Foundational: T005, T006, T007, T008 — todas en paralelo (archivos distintos, sin
  dependencias entre sí).
- Tests de US1: T009-T017 — todas en paralelo (archivos de prueba distintos, ninguna depende de
  otra).
- Dominio de US1: T018, T019, T020, T022, T023 en paralelo; T021 y T024 son secuenciales
  (dependen de las anteriores).
- Aplicación de US1: T025, T026, T027, T028 — en paralelo entre sí una vez completas T021-T024.
- Infraestructura de US1: T029, T030 en paralelo; luego T031 y T032 en paralelo; T033 al final.
- Presentación de US1: T034 y T036 en paralelo; T035 depende de T034.
- Polish: T039, T040, T041 — en paralelo.

---

## Parallel Example: User Story 1 (tests)

```bash
# Lanzar juntas todas las pruebas de US1 (todas deben fallar antes de implementar):
Task: "CedulaEcuatorianaValidatorTest en src/test/java/.../domain/validation/CedulaEcuatorianaValidatorTest.java"
Task: "FichaFamiliarTest en src/test/java/.../domain/model/FichaFamiliarTest.java"
Task: "RegistrarRepresentanteUseCaseTest en src/test/java/.../application/RegistrarRepresentanteUseCaseTest.java"
Task: "AgregarHijoUseCaseTest en src/test/java/.../application/AgregarHijoUseCaseTest.java"
Task: "ActualizarRepresentanteUseCaseTest en src/test/java/.../application/ActualizarRepresentanteUseCaseTest.java"
Task: "CancelarFichaUseCaseTest en src/test/java/.../application/CancelarFichaUseCaseTest.java"
Task: "FichaFamiliarMapperTest en src/test/java/.../infrastructure/persistence/FichaFamiliarMapperTest.java"
Task: "Feature Cucumber en src/test/resources/features/ficha-familiar-unificada.feature"
Task: "FichaFamiliarControllerIT en src/test/java/.../presentation/rest/FichaFamiliarControllerIT.java"
```

---

## Implementation Strategy

### MVP First (única historia)

1. Completar Fase 1: Setup.
2. Completar Fase 2: Foundational (bloqueante).
3. Completar Fase 3: User Story 1 — tests → dominio → aplicación → infraestructura →
   presentación → Cucumber.
4. **STOP and VALIDATE**: ejecutar `quickstart.md` completo contra el servicio levantado.
5. Completar Fase 4: Polish (cobertura, validación manual, revisión de logs).

Como esta feature tiene una sola historia de usuario, completar la Fase 3 equivale a entregar el
MVP completo de la épica E-01 hasta este punto (US-01).

## Notes

- [P] = archivos distintos, sin dependencias pendientes.
- [US1] = tarea de la única historia de usuario de esta feature.
- Verificar que las pruebas fallan antes de implementar (T009-T017 antes que T018 en adelante).
- Commit después de cada tarea o grupo lógico de tareas.
- Evitar: tareas vagas, conflictos de mismo archivo marcados como [P], dependencias cruzadas que
  rompan la prueba independiente de la historia.
