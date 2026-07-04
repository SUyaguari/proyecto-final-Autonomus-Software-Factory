# Feature Specification: Ficha familiar unificada

**Feature Branch**: `001-ficha-familiar-unificada`

**Created**: 2026-07-03

**Status**: Draft

**Input**: User description: "US-01 · Ficha familiar unificada · épica E-01 · 5 pts — Como Receptor de
Inscripciones, quiero registrar primero al representante legal y luego agregar uno o más niños en la
misma ficha, para no tener que pedir los datos del representante más de una vez por familia."

## Clarifications

### Session 2026-07-03

- Q: ¿Qué debe ocurrir si el Receptor de Inscripciones abandona la ficha antes de agregar ningún
  hijo (solo representante guardado)? → A: La ficha (incluidos los datos del representante) se
  descarta automáticamente si la sesión finaliza o se cancela sin haber registrado al menos un
  hijo.
- Q: ¿Se permite editar los datos del representante legal después de haber agregado uno o más
  hijos a la misma ficha? → A: Sí, en cualquier momento antes de finalizar la ficha, sin
  restricciones adicionales.
- Q: ¿Qué regla de validación debe aplicar el sistema al campo cédula del representante legal? →
  A: Se valida como cédula ecuatoriana: 10 dígitos numéricos y dígito verificador correcto según
  el algoritmo oficial.
- Q: ¿Cuándo se considera "completa" una ficha familiar? → A: Automáticamente, en cuanto tiene el
  representante legal guardado y al menos un hijo vinculado; no existe un paso explícito de
  "finalizar".

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registrar representante y vincular varios hijos en una sola ficha (Priority: P1)

Como Receptor de Inscripciones, al iniciar una inscripción nueva registro primero los datos del
representante legal de la familia. Una vez guardados esos datos, el sistema habilita la sección
para agregar hijos, y puedo registrar uno o más niños dentro de la misma ficha sin que se me
vuelvan a pedir los datos del adulto responsable.

**Why this priority**: Es la funcionalidad base de la épica de inscripción familiar: sin ella, cada
hijo requeriría reingresar los datos del representante, duplicando trabajo y aumentando el riesgo de
inconsistencias entre hermanos de una misma familia. Es la historia mínima que entrega valor
independiente (MVP de la épica E-01).

**Independent Test**: Puede probarse de forma completa iniciando una ficha nueva, registrando un
representante legal con sus datos obligatorios, y agregando dos o más hijos a esa misma ficha,
verificando que los datos del representante no se vuelven a solicitar y que ambos hijos quedan
vinculados al mismo representante.

**Acceptance Scenarios**:

1. **Given** que el Receptor de Inscripciones inicia una ficha de inscripción nueva, **When**
   ingresa los datos del representante legal (nombre, cédula, celular, dirección), **Then** el
   sistema guarda esos datos y habilita la sección de hijos sin volver a solicitarlos.
2. **Given** que el representante legal ya fue registrado en la ficha actual, **When** el Receptor
   de Inscripciones agrega un segundo hijo de la misma familia, **Then** el sistema vincula
   automáticamente ese hijo al representante ya ingresado, sin requerir que se repitan los datos del
   adulto.
3. **Given** que el formulario tiene datos obligatorios del representante incompletos, **When** el
   Receptor de Inscripciones intenta guardar/crear la ficha, **Then** el sistema rechaza la
   operación y señala qué campos obligatorios faltan por completar, por lo que la sección de
   hijos nunca llega a habilitarse.

---

### Edge Cases

- ¿Qué ocurre si el Receptor de Inscripciones intenta agregar un hijo antes de haber ingresado
  cualquier dato del representante (sección de hijos aún no habilitada)?
- Si, luego de agregar uno o más hijos, el Receptor de Inscripciones necesita corregir un dato
  del representante (por ejemplo, un número de celular mal digitado), puede editarlo en
  cualquier momento antes de finalizar la ficha (ver Clarifications).
- Si el Receptor de Inscripciones abandona o cancela la ficha antes de registrar al menos un
  hijo, la ficha (incluidos los datos del representante ya guardados) se descarta
  automáticamente (ver Clarifications).
- ¿Qué ocurre si se intenta agregar un hijo con datos propios incompletos (por ejemplo, sin nombre)
  aun cuando los datos del representante sí están completos?
- ¿Qué ocurre si la cédula ingresada para el representante ya existe en el sistema por haber sido
  registrada previamente en otra ficha (posible familia ya inscrita en una gestión anterior)?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir al Receptor de Inscripciones iniciar una nueva ficha de
  inscripción familiar.
- **FR-002**: El sistema DEBE solicitar y capturar los datos obligatorios del representante legal
  (nombre, cédula, celular, dirección) como primer paso de la ficha.
- **FR-002a**: El sistema DEBE validar que la cédula del representante legal corresponda a una
  cédula ecuatoriana válida (10 dígitos numéricos con dígito verificador correcto según el
  algoritmo oficial).
- **FR-003**: El sistema DEBE persistir los datos del representante legal al guardarlos y mantenerlos
  asociados a la ficha durante todo el proceso de inscripción de esa familia.
- **FR-004**: El sistema DEBE habilitar la sección de registro de hijos únicamente después de que
  los datos obligatorios del representante legal hayan sido guardados.
- **FR-005**: El sistema DEBE permitir agregar uno o más hijos a la misma ficha sin volver a
  solicitar los datos del representante legal.
- **FR-006**: El sistema DEBE vincular automáticamente cada hijo agregado al representante legal ya
  registrado en esa misma ficha.
- **FR-007**: El sistema DEBE exigir que todos los campos obligatorios del representante legal
  estén completos y validados en el momento de crear o editar la ficha; si algún campo
  obligatorio falta, el sistema DEBE rechazar la operación e indicar qué campos deben
  completarse. Como consecuencia, nunca es posible alcanzar el paso de agregar un hijo con datos
  del representante incompletos, porque la ficha (y su representante) solo existen una vez que
  esos datos están completos.
- **FR-008**: El sistema DEBE requerir, como mínimo, el nombre completo y la fecha de nacimiento de
  cada hijo para poder agregarlo a la ficha.
- **FR-009**: El sistema DEBE permitir registrar múltiples hijos (dos o más) dentro de una misma
  ficha durante la misma sesión de inscripción.
- **FR-010**: El sistema DEBE permitir editar los datos del representante legal en cualquier
  momento antes de finalizar la ficha, incluso después de haber agregado uno o más hijos.
- **FR-011**: El sistema DEBE descartar la ficha, incluidos los datos del representante legal, en
  respuesta a una cancelación explícita del Receptor de Inscripciones antes de haber registrado
  algún hijo (el sistema no infiere el abandono de forma implícita; requiere una acción explícita
  de cancelación).
- **FR-012**: El sistema DEBE considerar una ficha como completa automáticamente en cuanto el
  representante legal está guardado y al menos un hijo ha sido vinculado, sin requerir una acción
  explícita de finalización.
- **FR-013**: El sistema DEBE rechazar el agregado de un hijo si sus propios campos obligatorios
  (nombre completo, fecha de nacimiento) están incompletos, indicando el motivo del rechazo.

### Key Entities

- **Representante Legal**: Persona adulta responsable de la familia que se inscribe. Atributos
  clave: nombre completo, cédula (validada como cédula ecuatoriana con dígito verificador),
  celular, dirección. Existe uno por ficha familiar. Sus datos pueden editarse en cualquier
  momento antes de finalizar la ficha.
- **Niño/Hijo**: Menor que se inscribe. Atributos clave: nombre completo, fecha de nacimiento.
  Queda vinculado a exactamente un Representante Legal dentro de la ficha. Una ficha puede tener uno
  o más hijos.
- **Ficha Familiar**: Registro de inscripción que agrupa a un Representante Legal con uno o más
  Niños/Hijos registrados en la misma sesión de inscripción. Se considera completa
  automáticamente al tener representante y al menos un hijo; se descarta cuando el Receptor de
  Inscripciones la cancela explícitamente antes de registrar algún hijo.
- **Estado de la Ficha**: Atributo derivado de la Ficha Familiar con dos valores posibles,
  `INCOMPLETA` (representante guardado, cero hijos) y `COMPLETA` (representante guardado y al
  menos un hijo); no se edita directamente, se recalcula al agregar el primer hijo (FR-012).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un Receptor de Inscripciones puede registrar al representante legal y agregar al
  menos dos hijos a la misma ficha sin repetir ningún dato del representante, en una sola sesión
  continua.
- **SC-002**: El 100% de los hijos agregados dentro de una misma ficha quedan correctamente
  vinculados al representante legal de esa ficha (cero hijos huérfanos).
- **SC-003**: El 100% de los intentos de agregar un hijo con datos obligatorios del representante
  incompletos son bloqueados por el sistema, mostrando de forma clara los campos faltantes.
- **SC-004**: El Receptor de Inscripciones nunca necesita reingresar los datos del representante
  legal más de una vez por familia dentro de la misma ficha.
- **SC-005**: El 100% de las cancelaciones explícitas sobre fichas sin ningún hijo registrado
  descartan la ficha correctamente, sin dejar representantes registrados sin ningún hijo
  vinculado.
- **SC-006**: El 100% de las cédulas de representante ingresadas son validadas contra el formato
  de cédula ecuatoriana (10 dígitos y dígito verificador) antes de aceptar el registro.

## Assumptions

- Esta historia cubre el flujo dentro de una misma sesión/ficha de inscripción (representante +
  sus hijos capturados de forma continua por el Receptor de Inscripciones). La búsqueda o
  reutilización de un representante ya registrado en una ficha anterior (por ejemplo, en una
  gestión o período distinto) queda fuera del alcance de esta historia y podría abordarse en una
  historia futura de la épica E-01.
- La detección de cédulas duplicadas entre fichas distintas (para evitar representantes
  duplicados a nivel de todo el sistema) no está descrita en los criterios de aceptación de esta
  historia y se considera fuera de alcance; se asume que el sistema simplemente exige el campo
  cédula como obligatorio dentro de la ficha actual.
- Los datos mínimos obligatorios de cada hijo (nombre completo y fecha de nacimiento) son un valor
  por defecto razonable ante la ausencia de detalle explícito en la historia; otros atributos del
  hijo podrían añadirse en historias posteriores.
