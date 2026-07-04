<!--
Sync Impact Report
==================
Version change: [TEMPLATE] → 1.0.0 (initial ratification)
Modified principles: N/A (first concrete adoption; template placeholders replaced)
Added sections:
  - Core Principles I–VIII (Modular Monolith & Clean Architecture, Testing Discipline,
    Design & Maintainability Principles, API-First with OpenAPI, Test Coverage Standards,
    Centralized Error Handling & Secure Logging, Externalized Configuration, Security by Design)
  - Additional Constraints (technology stack)
  - Development Workflow (quality gates, review process)
  - Governance (amendment procedure, versioning policy, compliance review)
Removed sections: none (template placeholders only)
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ (generic "Constitution Check" gate remains compatible)
  - .specify/templates/spec-template.md ✅ (no agent-specific or conflicting references found)
  - .specify/templates/tasks-template.md ✅ (generic task phases remain compatible; task authors
    should map Foundational/Polish phases to principles II, V, VI, VII, VIII where applicable)
  - .specify/templates/commands/*.md ⚠ N/A (directory does not exist in this project)
Follow-up TODOs: none
-->

# inscripcionoratorio-service Constitution

## Core Principles

### I. Modular Monolith con Arquitectura Limpia
El sistema se construye como un monolito modular sobre Spring Boot. Cada módulo DEBE
organizarse en capas independientes siguiendo la Arquitectura Limpia de Robert C. Martin:
dominio, aplicación, infraestructura y presentación. La capa de dominio NO DEBE depender de
frameworks, persistencia ni detalles de infraestructura; las dependencias DEBEN apuntar
siempre hacia adentro (de infraestructura/presentación hacia aplicación/dominio). Los módulos
DEBEN exponer límites claros y comunicarse mediante contratos explícitos, evitando
acoplamiento directo entre las capas internas de módulos distintos.

**Rationale**: Separar responsabilidades por capas y módulos permite evolucionar,
probar y reemplazar tecnología de infraestructura (persistencia, mensajería, web) sin
reescribir las reglas de negocio, y mantiene el monolito manejable a medida que crece.

### II. Disciplina de Pruebas Multinivel
El proyecto DEBE contar con pruebas unitarias, de integración y funcionales. Las pruebas
unitarias DEBEN seguir un enfoque BDD (Given/When/Then) implementado con JUnit 5, Mockito y
BDDMockito. Las pruebas funcionales basadas en escenarios de negocio PUEDEN implementarse con
Cucumber y sintaxis Gherkin cuando el escenario lo amerite. Toda nueva lógica de negocio DEBE
incluir su prueba correspondiente antes de considerarse completa.

**Rationale**: Un enfoque BDD hace explícito el comportamiento esperado en términos de
negocio, mejora la legibilidad de las pruebas y facilita la trazabilidad entre requisitos y
verificación automatizada.

### III. Principios de Diseño y Mantenibilidad (SOLID, YAGNI, DRY, KISS)
Todo el código DEBE aplicar SOLID, YAGNI, DRY y KISS. Las clases DEBEN tener
responsabilidades claras y únicas, bajo acoplamiento y alta cohesión. La lógica común DEBE
reutilizarse sin caer en sobreabstracción prematura. Las soluciones DEBEN ser lo más simples
posible y las funcionalidades DEBEN limitarse a lo definido en los requerimientos, sin
anticipar necesidades futuras no solicitadas.

**Rationale**: Estos principios previenen deuda técnica temprana, reducen el costo de
cambio y evitan complejidad innecesaria que no aporta valor al requerimiento actual.

### IV. API-First con Contrato OpenAPI
Todo endpoint DEBE diseñarse primero como contrato OpenAPI antes de implementarse; ese
contrato es la única fuente de verdad de la API. Las interfaces, modelos y stubs de Spring
Boot DEBEN generarse a partir del contrato mediante OpenAPI Generator. La implementación de
la lógica de negocio DEBE respetar fielmente el contrato generado; cualquier cambio de
comportamiento observable DEBE reflejarse primero en el contrato OpenAPI y regenerarse antes
de modificar el código de implementación.

**Rationale**: Definir el contrato antes que el código garantiza consistencia entre
documentación y comportamiento real, habilita el desarrollo paralelo entre consumidores y
proveedores de la API, y reduce ambigüedad en la integración.

### V. Cobertura de Pruebas Verificable (JaCoCo)
El proyecto DEBE usar JaCoCo para reportar cobertura de pruebas. La cobertura global DEBE
ser igual o superior al 80%, y la cobertura por clase DEBE ser igual o superior al 80% en
servicios, casos de uso, validadores, mappers manuales y demás componentes con lógica de
negocio. Se EXCLUYEN de esta métrica las clases generadas automáticamente (incluyendo las
generadas por OpenAPI Generator), los DTOs simples, las clases de configuración, las
entidades sin lógica y la clase principal de arranque de Spring Boot.

**Rationale**: Un umbral de cobertura medible y con exclusiones explícitas enfoca el
esfuerzo de pruebas en el código con lógica real, evitando cobertura artificial sobre código
trivial o generado.

### VI. Manejo Centralizado de Errores y Logging Seguro
El manejo de errores DEBE centralizarse mediante `@RestControllerAdvice`, produciendo
respuestas de error consistentes en toda la API. Los datos de entrada DEBEN validarse antes
de procesarse. El sistema DEBE registrar logs adecuados para diagnóstico y auditoría, pero
NUNCA DEBE registrar credenciales, tokens, contraseñas ni información sensible en los logs.

**Rationale**: Respuestas de error consistentes mejoran la experiencia de los
consumidores de la API y facilitan el diagnóstico, mientras que la exclusión de datos
sensibles de los logs previene fugas de información y cumple requisitos básicos de
seguridad.

### VII. Configuración Externalizada
La configuración del sistema DEBE externalizarse mediante `application.yml`, perfiles de
Spring (por ejemplo, `dev`, `test`, `prod`) y variables de entorno. El código fuente NUNCA
DEBE incluir credenciales, claves secretas, URLs sensibles ni configuraciones específicas de
producción de forma embebida.

**Rationale**: Externalizar la configuración permite promover el mismo artefacto entre
entornos sin recompilar, y evita la exposición accidental de secretos en el control de
versiones.

### VIII. Seguridad desde el Diseño
La seguridad DEBE considerarse desde el inicio del desarrollo, no como una etapa
posterior. Los endpoints DEBEN protegerse según el rol o permiso correspondiente, utilizando
Spring Security cuando aplique. Las contraseñas DEBEN almacenarse únicamente mediante
algoritmos de hash seguros (por ejemplo, BCrypt o equivalente) y NUNCA en texto plano.

**Rationale**: Incorporar seguridad desde el diseño reduce el riesgo de vulnerabilidades
estructurales que son costosas de corregir una vez que el sistema está en producción.

## Additional Constraints

- **Stack tecnológico**: Java (toolchain vigente en `build.gradle`), Spring Boot, Gradle
  como herramienta de build, Spring Data JPA para persistencia, H2 para desarrollo/pruebas
  locales cuando aplique.
- **Generación de código**: OpenAPI Generator es la herramienta oficial para producir
  interfaces, modelos y stubs a partir de los contratos OpenAPI; no se debe escribir a mano
  código que el generador ya produce a partir del contrato.
- **Cobertura de calidad**: JaCoCo DEBE integrarse al build y sus reportes DEBEN poder
  consultarse localmente y en el pipeline de integración continua.

## Development Workflow

- Toda funcionalidad nueva DEBE partir de un contrato OpenAPI aprobado antes de escribir
  código de implementación (Principio IV).
- Toda Pull Request DEBE incluir las pruebas correspondientes (unitarias BDD y, cuando
  aplique, integración o funcionales) y DEBE pasar el build con los umbrales de cobertura de
  JaCoCo definidos en el Principio V antes de poder integrarse.
- Las revisiones de código DEBEN verificar cumplimiento de SOLID, YAGNI, DRY y KISS
  (Principio III), separación de capas de Arquitectura Limpia (Principio I), manejo
  centralizado de errores (Principio VI) y ausencia de secretos en código o logs (Principios
  VI y VII).
- Cambios que afecten endpoints DEBEN actualizar primero el contrato OpenAPI y regenerar los
  artefactos correspondientes antes de modificar la implementación.

## Governance

Esta constitución prevalece sobre cualquier otra práctica, guía o convención del proyecto
que la contradiga. Toda Pull Request y revisión de código DEBE verificar cumplimiento de los
principios aquí establecidos; cualquier desviación DEBE justificarse explícitamente en la
descripción del cambio (por ejemplo, en una sección de complejidad/justificación del plan
correspondiente).

**Procedimiento de enmienda**: Cambios a esta constitución DEBEN proponerse documentando el
principio o sección afectada, la motivación del cambio y su impacto en artefactos
dependientes (plantillas de spec, plan y tareas). Las enmiendas se consideran adoptadas al
actualizarse este archivo con su reporte de sincronización (Sync Impact Report) y el
incremento de versión correspondiente.

**Política de versionado**: Esta constitución sigue versionado semántico:
- **MAYOR**: eliminación o redefinición incompatible de principios o de reglas de
  gobernanza existentes.
- **MENOR**: adición de un nuevo principio o sección, o expansión material de una guía
  existente.
- **PATCH**: aclaraciones de redacción, correcciones tipográficas o refinamientos que no
  cambian el significado normativo.

**Revisión de cumplimiento**: Toda planificación de features (`/speckit-plan`) DEBE
incluir una verificación contra los principios de esta constitución ("Constitution Check")
antes de la Fase 0 y nuevamente después del diseño de la Fase 1. Cualquier violación no
justificable de forma razonable DEBE resolverse simplificando el diseño antes de continuar.

**Version**: 1.0.0 | **Ratified**: 2026-07-03 | **Last Amended**: 2026-07-03
