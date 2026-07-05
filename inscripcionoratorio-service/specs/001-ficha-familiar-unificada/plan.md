# Implementation Plan: Ficha familiar unificada

**Branch**: `001-ficha-familiar-unificada` | **Date**: 2026-07-03 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-ficha-familiar-unificada/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

El Receptor de Inscripciones registra primero al representante legal de una familia y luego
agrega uno o más hijos a la misma ficha, sin repetir los datos del adulto. Se implementa como un
nuevo módulo `inscripcion` dentro del monolito modular (Arquitectura Limpia: dominio, aplicación,
infraestructura, presentación), expuesto mediante una API REST definida primero en un contrato
OpenAPI (API-First) con tres operaciones: crear ficha con representante, agregar hijo, editar
representante, y cancelar una ficha sin hijos. El agregado `FichaFamiliar` persiste al
representante legal (embebido) y a los hijos vinculados (colección); el estado de la ficha
(`INCOMPLETA`/`COMPLETA`) se deriva automáticamente de si tiene al menos un hijo registrado.

## Technical Context

**Language/Version**: Java 25 (toolchain definido en `build.gradle`)

**Primary Dependencies**: Spring Boot 4.1.0 (`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-h2console`, `spring-boot-starter-validation`), Lombok. Se añaden en este plan: plugin Gradle `org.openapi.generator` (contrato-primero, Principio IV) y plugin Gradle `jacoco` (cobertura, Principio V), `org.springdoc:springdoc-openapi-starter-webmvc-ui` (UI de Swagger en runtime sobre el contrato generado). Para pruebas: JUnit 5, Mockito, BDDMockito (ya presentes vía `spring-boot-starter-webmvc-test`/`spring-boot-starter-data-jpa-test`), y se añade `io.cucumber:cucumber-java` + `io.cucumber:cucumber-junit-platform-engine` para las pruebas funcionales Gherkin.

**Storage**: H2 (en memoria) vía Spring Data JPA. Siguiendo el mismo patrón ya usado en otros
servicios del equipo (p. ej. `citasalud-service`), el esquema se define explícitamente en
`src/main/resources/schema.sql` (`spring.jpa.hibernate.ddl-auto: none`, `spring.sql.init.mode:
always`) en vez de dejar que Hibernate lo autogenere, y se precargan datos de prueba
idempotentes (`MERGE INTO`) en `src/main/resources/data.sql` para desarrollo local y como
fixture de las pruebas de integración/funcionales (ver research.md §7).

**Testing**: JUnit 5 + Mockito + BDDMockito (unitarias, estilo BDD Given/When/Then) para validadores, casos de uso y mappers; `@SpringBootTest` + `MockMvc` con H2 para pruebas de integración (persistencia + endpoints); Cucumber (Gherkin) para las pruebas funcionales, reutilizando literalmente los escenarios de aceptación de `spec.md`. JaCoCo para cobertura (≥80% global y por clase en servicios/casos de uso/validadores/mappers, según Principio V de la constitución).

**Target Platform**: Servicio backend Spring Boot (monolito modular), desplegable como JAR ejecutable en Linux server/contenedor; sin componente de interfaz de usuario en esta historia (solo API REST).

**Project Type**: Web service (backend único, monolito modular con módulos internos por bounded context).

**Performance Goals**: No se definieron metas de rendimiento específicas en la especificación (herramienta interna de back-office, volumen bajo/moderado). Se asume el comportamiento estándar de una API CRUD interna, sin metas de throughput/latencia particulares para esta historia.

**Constraints**: Debe respetar la Arquitectura Limpia y el enfoque API-First de la constitución del proyecto (contrato OpenAPI como fuente de verdad, capas dominio/aplicación/infraestructura/presentación con dependencias apuntando hacia adentro). Esta historia no define roles ni permisos (la especificación no lo requiere), por lo que no se añade Spring Security en esta iteración; queda pendiente para una historia futura de autenticación/autorización (ver Constitution Check).

**Scale/Scope**: Un módulo nuevo (`inscripcion`), 3 conceptos de dominio (Representante Legal, Niño/Hijo, Ficha Familiar), 4 operaciones REST (crear ficha, agregar hijo, editar representante, cancelar ficha sin hijos).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación | Estado |
|---|---|---|
| I. Monolito Modular con Arquitectura Limpia | Se crea el módulo `inscripcion` con paquetes `domain`, `application`, `infrastructure`, `presentation`; el dominio no depende de Spring/JPA. | PASS |
| II. Disciplina de Pruebas Multinivel | Se planifican pruebas unitarias BDD (JUnit5/Mockito/BDDMockito), de integración (`@SpringBootTest`+MockMvc+H2) y funcionales (Cucumber/Gherkin, reutilizando los escenarios de la spec). | PASS |
| III. SOLID/YAGNI/DRY/KISS | Alcance limitado a lo pedido en la historia (4 endpoints, sin funcionalidades especulativas); representante embebido evita una tabla/relación innecesaria dado que es 1:1 con la ficha. | PASS |
| IV. API-First con OpenAPI | El contrato OpenAPI (`contracts/ficha-familiar.openapi.yaml`) se define antes de la implementación; se añade el plugin `org.openapi.generator` para generar interfaces/modelos que el controlador implementa. | PASS (requiere añadir el plugin al build, ver research.md) |
| V. Cobertura JaCoCo ≥80% | Se añade el plugin `jacoco` con reglas de verificación (global y por clase) sobre servicios/casos de uso/validadores/mappers, excluyendo clases generadas por OpenAPI Generator, DTOs, configuración, entidades JPA sin lógica y la clase principal. | PASS (requiere configurar el plugin, ver research.md) |
| VI. Manejo Centralizado de Errores y Logging Seguro | Se introduce el primer `@RestControllerAdvice` del proyecto (paquete compartido) mapeando excepciones de dominio a respuestas de error consistentes; no se registran datos sensibles (cédula/celular no se loguean en texto claro en niveles INFO). | PASS |
| VII. Configuración Externalizada | Credenciales del único usuario (`APP_USER`/`APP_PASSWORD`, hash BCrypt) se leen por variable de entorno en `application.yaml`, sin valores por defecto ni secretos en el código fuente. | PASS |
| VIII. Seguridad desde el Diseño | `spring-boot-starter-security` protege los 4 endpoints de `FichaFamiliarController` con HTTP Basic y un único rol `RECEPTOR_INSCRIPCIONES` (usuario en memoria, contraseña con hash BCrypt, ver research.md). Swagger UI y `/v3/api-docs` quedan públicos (documentación, no datos). | PASS |

Actualización posterior al MVP inicial: la desviación del Principio VIII que estaba registrada
en Complexity Tracking (endpoints sin autenticación) se resolvió agregando Spring Security con
HTTP Basic, a raíz de un hallazgo `high` de un agente de calidad externo (`quality-output/`).
Ver research.md para el detalle de la decisión.

**Re-check post-Fase 1 (tras data-model.md, contracts/ y quickstart.md)**: El diseño final
(representante embebido, un único agregado `FichaFamiliar`, puerto `FichaFamiliarRepository`,
contrato OpenAPI con 4 operaciones) no introduce entidades, proyectos ni patrones adicionales a
los ya evaluados arriba. Todos los principios se mantienen en PASS; no se requieren cambios al
Constitution Check inicial.

## Project Structure

### Documentation (this feature)

```text
specs/001-ficha-familiar-unificada/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/
│   └── ficha-familiar.openapi.yaml   # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/org/ups/inscripcionoratorio/
├── InscripcionoratorioServiceApplication.java
├── shared/
│   └── infrastructure/
│       └── web/
│           ├── GlobalExceptionHandler.java      # @RestControllerAdvice centralizado
│           └── ErrorResponse.java               # Cuerpo de error consistente
└── inscripcion/
    ├── domain/
    │   ├── model/
    │   │   ├── FichaFamiliar.java                # Agregado raíz (representante + hijos + estado)
    │   │   ├── RepresentanteLegal.java           # Value object embebido
    │   │   ├── NinoHijo.java                     # Entidad de dominio
    │   │   └── EstadoFicha.java                  # Enum: INCOMPLETA, COMPLETA
    │   ├── validation/
    │   │   └── CedulaEcuatorianaValidator.java   # Validador (algoritmo módulo 10)
    │   ├── repository/
    │   │   └── FichaFamiliarRepository.java      # Puerto (interfaz) del dominio
    │   └── exception/
    │       ├── CedulaInvalidaException.java
    │       ├── DatosHijoIncompletosException.java
    │       ├── FichaNoEncontradaException.java
    │       └── FichaConHijosNoCancelableException.java
    ├── application/
    │   └── usecase/
    │       ├── RegistrarRepresentanteUseCase.java
    │       ├── AgregarHijoUseCase.java
    │       ├── ActualizarRepresentanteUseCase.java
    │       └── CancelarFichaUseCase.java
    ├── infrastructure/
    │   └── persistence/
    │       ├── FichaFamiliarEntity.java          # @Entity JPA
    │       ├── NinoHijoEntity.java                # @Entity JPA
    │       ├── FichaFamiliarJpaRepository.java    # Spring Data JPA
    │       ├── FichaFamiliarRepositoryImpl.java   # Adaptador del puerto de dominio
    │       └── FichaFamiliarMapper.java           # Mapper manual dominio <-> entidad JPA
    └── presentation/
        └── rest/
            ├── FichaFamiliarController.java       # Implementa la interfaz generada por OpenAPI
            └── FichaFamiliarDtoMapper.java         # Mapper manual dominio <-> DTOs generados

src/test/java/org/ups/inscripcionoratorio/
├── inscripcion/
│   ├── domain/
│   │   ├── validation/CedulaEcuatorianaValidatorTest.java     # unitaria BDD
│   │   └── model/FichaFamiliarTest.java                       # unitaria BDD (reglas de estado)
│   ├── application/
│   │   ├── RegistrarRepresentanteUseCaseTest.java              # unitaria BDD (Mockito/BDDMockito)
│   │   ├── AgregarHijoUseCaseTest.java
│   │   ├── ActualizarRepresentanteUseCaseTest.java
│   │   └── CancelarFichaUseCaseTest.java
│   ├── infrastructure/persistence/FichaFamiliarMapperTest.java # unitaria (mapper manual)
│   └── presentation/rest/FichaFamiliarControllerIT.java        # integración (@SpringBootTest + MockMvc + H2)
└── bdd/
    ├── RunCucumberTest.java                                    # runner JUnit Platform Suite
    └── resources/features/ficha-familiar-unificada.feature      # escenarios Gherkin (US-01)

src/main/resources/
├── application.yaml           # ddl-auto: none, sql.init.mode: always, context-path (ver research.md §7)
├── schema.sql                 # DDL explícito: tablas ficha_familiar, nino_hijo
├── data.sql                   # Datos de prueba (MERGE INTO, idempotente) para desarrollo/pruebas
└── openapi/
    └── ficha-familiar.openapi.yaml   # Copia del contrato (fuente real de inputSpec en build.gradle, ver research.md §4)
```

**Structure Decision**: Monolito modular de un solo proyecto Gradle (sin separación
frontend/backend, no aplica). Se introduce el primer módulo funcional (`inscripcion`) y el
paquete compartido (`shared`) para infraestructura transversal (manejo de errores), siguiendo la
Arquitectura Limpia exigida por el Principio I: `domain` sin dependencias de Spring/JPA,
`application` orquesta casos de uso sobre el puerto `FichaFamiliarRepository`, `infrastructure`
implementa persistencia JPA y mapeo manual, `presentation` expone la API REST generada a partir
del contrato OpenAPI.

## Complexity Tracking

> Sin desviaciones activas. La única entrada previa (Principio VIII, endpoints sin
> autenticación) se resolvió: se agregó `spring-boot-starter-security` con HTTP Basic y un
> único rol `RECEPTOR_INSCRIPCIONES` (ver Constitution Check y research.md). No se implementó
> un modelo de roles múltiples ni JWT por no haber otro requisito que lo justifique (YAGNI,
> Principio III); si una historia futura necesita más de un rol o un flujo de login propio,
> se ampliará entonces.
