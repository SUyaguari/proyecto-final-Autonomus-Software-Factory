# Proyecto final - Inscripcion Oratorio Vacacional

Este repositorio reúne tres agentes de apoyo al ciclo de Ingenieria de Software y un servicio Spring Boot que implementa la primera parte del MVP del sistema de inscripcion del Oratorio Vacacional. El flujo completo del trabajo queda asi:

1. `discovery-agent` transforma entrevistas en evidencia de negocio, requisitos e hipotesis.
2. `agile-delivery-team` convierte ese descubrimiento en epicas, historias, arquitectura y plan de sprint.
3. `inscripcionoratorio-service` implementa en codigo la historia priorizada del MVP.
4. `quality-agent` verifica pruebas, seguridad y cobertura de criterios antes de dar el trabajo por aprobado.

## Estructura del repositorio

```text
.
├── discovery-agent/
├── agile-delivery-team/
├── quality-agent/
└── inscripcionoratorio-service/
```

## 1. Discovery Agent

Ubicacion: `discovery-agent/`

Su objetivo es levantar el problema real antes de programar. En este caso analiza entrevistas con cuatro actores:

- Receptor de Inscripciones
- Coordinador del Oratorio
- Tesorera del Oratorio
- Representante de familia

### Hallazgos principales del discovery

- El proceso actual usa multiples formularios por rango de edad y eso genera errores de clasificacion.
- Los datos del representante se repiten cuando una familia registra varios hijos.
- El estado del pago no es claro para tesoreria, coordinacion ni familias.
- La comunicacion por WhatsApp y la revision de comprobantes dependen mucho de trabajo manual.

### Artefactos generados para el caso `inscripcion`

- `discoveries/inscripcion/outputs/personas.md`
- `discoveries/inscripcion/outputs/requisitos.md`
- `discoveries/inscripcion/outputs/user-stories.md`
- `discoveries/inscripcion/outputs/mvp-canvas.md`
- `discoveries/inscripcion/outputs/hypotheses.md`
- `discoveries/inscripcion/outputs/evidence-map.json`
- `discoveries/inscripcion/outputs/experiment-board.json`
- `discoveries/inscripcion/outputs/report.html`

### Resultado del discovery

El descubrimiento define como nucleo de valor:

- formulario unico por familia
- clasificacion automatica por edad
- estados de pago claros
- notificacion oportuna al representante
- visibilidad del estado para todos los roles

De ese conjunto, la historia que pasa primero a implementacion es `US-01`: ficha familiar unificada.

## 2. Agile Delivery Team

Ubicacion: `agile-delivery-team/`

Este agente toma los insumos del discovery y los convierte en trabajo listo para construir. El equipo se divide en cuatro roles:

- Product Owner
- Developer
- Scrum Master
- Architect

### Entregables del caso `inscripcion`

- `deliveries/inscripcion/outputs/epics.md`
- `deliveries/inscripcion/outputs/backlog.json`
- `deliveries/inscripcion/outputs/stories.md`
- `deliveries/inscripcion/outputs/architecture.md`
- `deliveries/inscripcion/outputs/sprint-plan.md`
- `deliveries/inscripcion/outputs/report.html`
- `deliveries/inscripcion/outputs/adr/`

### Definicion del trabajo

El backlog queda organizado en cinco epicas:

1. Inscripcion unificada por familia
2. Gestion de pagos con trazabilidad
3. Notificaciones al representante por WhatsApp
4. Visibilidad operativa y coordinacion de grupos
5. Autoservicio del representante

La arquitectura propuesta para el MVP es un monolito server-side con base de datos relacional y notificaciones de WhatsApp en modo semimanual mediante `wa.me`.

### Sprint priorizado

El sprint inicial compromete historias que construyen el flujo minimo operativo:

- `US-01` Ficha familiar unificada
- `US-05` Configuracion de grupos y rangos
- `US-02` Clasificacion automatica por edad
- `US-07` Estados de pago trazables
- `US-08` Pago en efectivo
- `US-09` Verificacion de comprobante de transferencia

De ese sprint, el servicio implementado en este repositorio cubre la parte de `US-01`.

## 3. inscripcionoratorio-service

Ubicacion: `inscripcionoratorio-service/`

Este es el codigo de produccion del caso implementado. Corresponde a la historia `US-01 - Ficha familiar unificada`, cuyo objetivo es registrar primero al representante legal y luego agregar uno o mas hijos a la misma ficha.

### Alcance funcional implementado

El servicio cubre los requisitos funcionales `FR-001` a `FR-013` del spec `specs/001-ficha-familiar-unificada/spec.md`, entre ellos:

- crear una ficha familiar con representante legal
- validar cedula ecuatoriana
- agregar uno o varios hijos a la misma ficha
- cambiar el estado de la ficha de `INCOMPLETA` a `COMPLETA`
- actualizar datos del representante aun despues de agregar hijos
- cancelar una ficha solo si aun no tiene hijos
- rechazar datos incompletos del representante o del hijo

### Arquitectura del codigo

El servicio sigue una separacion por capas y responsabilidades:

- `domain/`: modelo y reglas de negocio (`FichaFamiliar`, `RepresentanteLegal`, `NinoHijo`, validaciones y excepciones)
- `application/usecase/`: casos de uso (`RegistrarRepresentanteUseCase`, `AgregarHijoUseCase`, `ActualizarRepresentanteUseCase`, `CancelarFichaUseCase`)
- `infrastructure/persistence/`: entidades JPA, mapper y repositorio de persistencia
- `presentation/rest/`: controlador REST y mapper DTO
- `shared/infrastructure/`: manejo global de errores y configuracion de seguridad

### API expuesta

El contrato se define en `src/main/resources/openapi/ficha-familiar.openapi.yaml` y expone cuatro operaciones principales:

- `POST /api/v1/fichas-familiares`
- `POST /api/v1/fichas-familiares/{fichaId}/hijos`
- `PUT /api/v1/fichas-familiares/{fichaId}/representante`
- `DELETE /api/v1/fichas-familiares/{fichaId}`

### Persistencia y configuracion

- Base de datos: H2 en memoria para el entorno actual
- Esquema: `src/main/resources/schema.sql`
- Datos de apoyo: `src/main/resources/data.sql`
- Context path: `/api/v1`
- Seguridad: HTTP Basic con rol `RECEPTOR_INSCRIPCIONES`

Las credenciales del usuario principal se leen desde variables de entorno:

```text
APP_USER
APP_PASSWORD
```

### Trazabilidad entre analisis y codigo

- Discovery identifica el dolor de repetir datos del representante.
- Delivery lo convierte en la historia `US-01`.
- El spec formaliza esa historia en `FR-001` a `FR-013`.
- El servicio implementa esos requisitos con casos de uso, API REST y pruebas automatizadas.

## 4. Quality Agent

Ubicacion: `quality-agent/`

Este agente evalua el codigo terminado con tres pilares:

1. Pruebas
2. Seguridad
3. Cobertura de criterios del spec

### Evidencia generada para `inscripcionoratorio-service`

- `inscripcionoratorio-service/quality-output/verification.json`
- `inscripcionoratorio-service/quality-output/report.html`

### Estado actual de calidad

Segun `quality-output/verification.json` y la verificacion local ejecutada sobre el proyecto:

- pruebas: `45/45` exitosas
- cobertura de lineas: `99.07%`
- gate de cobertura: aprobado (`jacocoTestCoverageVerification`)
- seguridad: `0` hallazgos criticos, `0` secretos
- criterios del spec: `FR-001` a `FR-013` marcados como `cumple`

Ademas, el servicio ya incorpora una configuracion de seguridad Spring Security en `src/main/java/org/ups/inscripcionoratorio/shared/infrastructure/security/SecurityConfig.java` para proteger `/fichas-familiares/**`.

## 5. Pruebas del servicio

El proyecto implementa tres niveles de prueba:

- unitarias de dominio y casos de uso
- integracion REST con `MockMvc`
- BDD con Cucumber

Archivos representativos:

- `src/test/java/.../FichaFamiliarTest.java`
- `src/test/java/.../RegistrarRepresentanteUseCaseTest.java`
- `src/test/java/.../FichaFamiliarControllerIT.java`
- `src/test/resources/features/ficha-familiar-unificada.feature`

Comandos verificados localmente:

```bash
.\gradlew.bat test
.\gradlew.bat jacocoTestCoverageVerification
```

Ambos finalizaron con `BUILD SUCCESSFUL`.

## 6. Conclusiones

El repositorio muestra una cadena completa de trabajo:

- el `discovery-agent` identifica necesidades reales del proceso de inscripcion
- el `agile-delivery-team` las convierte en un backlog y una arquitectura de MVP
- `inscripcionoratorio-service` materializa la primera historia prioritaria del flujo
- el `quality-agent` comprueba que la implementacion cumple pruebas, seguridad y criterios del spec

En su estado actual, el proyecto no implementa todo el backlog del sistema de inscripcion, pero si deja resuelta y validada la primera capacidad central: la ficha familiar unificada para registrar representante e hijos sin repetir informacion.
