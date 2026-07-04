# Data Model: Ficha familiar unificada

## FichaFamiliar (agregado raíz)

Representa la ficha de inscripción de una familia: un representante legal y los hijos que se le
vinculan.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | identificador (Long/UUID) | Generado por el sistema al crear la ficha. |
| `representanteLegal` | `RepresentanteLegal` (embebido) | Obligatorio; ver reglas de `RepresentanteLegal`. |
| `hijos` | `List<NinoHijo>` | Puede estar vacía solo mientras la ficha está `INCOMPLETA`. |
| `estado` | `EstadoFicha` | `INCOMPLETA` al crearse; pasa a `COMPLETA` automáticamente al agregar el primer hijo (FR-012). No existe transición manual/explícita de "finalizar". |
| `fechaCreacion` | fecha/hora | Asignada por el sistema al crear la ficha. |

**Invariantes de dominio**:
- No puede existir una ficha sin `representanteLegal`; el `representanteLegal` embebido siempre
  está completo y validado (incluyendo cédula) desde el momento en que la ficha se crea o edita.
  No existe un estado intermedio persistido de "representante incompleto" (FR-007).
- Una ficha `INCOMPLETA` (cero hijos) puede eliminarse (cancelarse); una ficha `COMPLETA` (uno o
  más hijos) no puede eliminarse mediante la operación de cancelación (FR-011, ver research.md
  §2).
- `estado` es un campo derivado (no editable directamente): se recalcula al agregar un hijo.

## RepresentanteLegal (value object embebido en FichaFamiliar)

| Campo | Tipo | Reglas |
|---|---|---|
| `nombreCompleto` | texto | Obligatorio, no vacío. |
| `cedula` | texto (10 dígitos) | Obligatorio; debe ser una cédula ecuatoriana válida (formato + dígito verificador, ver research.md §3). |
| `celular` | texto | Obligatorio, no vacío. |
| `direccion` | texto | Obligatorio, no vacío. |

**Reglas**:
- Los cuatro campos son obligatorios para crear o editar la ficha (FR-002/FR-007). La
  obligatoriedad de `nombreCompleto`/`celular`/`direccion` y el formato de 10 dígitos de `cedula`
  los aplica la validación Bean Validation (`@Valid`) del contrato OpenAPI sobre
  `RepresentanteLegalRequest`; el dígito verificador de la cédula (FR-002a) lo aplica
  adicionalmente `CedulaEcuatorianaValidator` en la capa de dominio/aplicación. Si algo falla, la
  operación de creación/edición se rechaza y la ficha nunca llega a existir (o a actualizarse) con
  datos incompletos o con una cédula inválida.
- Editable en cualquier momento mientras la ficha exista, sin importar cuántos hijos tenga
  (FR-010).

## NinoHijo (entidad, dentro del agregado FichaFamiliar)

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | identificador (Long/UUID) | Generado por el sistema al agregar el hijo. |
| `nombreCompleto` | texto | Obligatorio, no vacío (FR-008). |
| `fechaNacimiento` | fecha | Obligatorio (FR-008). |
| `fichaFamiliarId` | referencia a `FichaFamiliar` | Asignada automáticamente al agregar el hijo a una ficha (FR-006); un hijo pertenece exactamente a una ficha/representante. |

**Reglas**:
- Por construcción, toda `FichaFamiliar` persistida ya tiene un `representanteLegal` completo
  (FR-007 se aplica al crear/editar la ficha, no al agregar el hijo — ver research.md §1); por lo
  tanto, agregar un hijo nunca puede fallar por representante incompleto.
- Si los campos obligatorios propios del hijo (`nombreCompleto`, `fechaNacimiento`) están
  incompletos, la operación se rechaza (FR-013). En la API esto lo aplica la validación Bean
  Validation (`@Valid`) del contrato OpenAPI sobre `NinoHijoRequest`, que ya declara ambos campos
  obligatorios; el constructor de `NinoHijo` conserva una verificación defensiva propia (lanza
  `IllegalArgumentException`) como invariante de dominio para cuando se construye directamente,
  sin una excepción de dominio dedicada (evita el mismo código inalcanzable señalado en I1 para
  `DatosRepresentanteIncompletosException`).

## EstadoFicha (enum)

| Valor | Significado |
|---|---|
| `INCOMPLETA` | Representante guardado, cero hijos vinculados. Cancelable. |
| `COMPLETA` | Representante guardado y al menos un hijo vinculado. No cancelable mediante `DELETE`. |

## Relaciones

```text
FichaFamiliar 1 ── (embebido) ── 1 RepresentanteLegal
FichaFamiliar 1 ── (colección) ── 0..N NinoHijo
```

No hay relación entre `RepresentanteLegal` de fichas distintas en el alcance de esta historia
(ver Assumptions en spec.md: reutilización de representante entre fichas queda fuera de
alcance).

## Esquema SQL (schema.sql)

Siguiendo el patrón de `research.md` §7 (esquema explícito, sin autogeneración de Hibernate):

```sql
CREATE TABLE IF NOT EXISTS ficha_familiar (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    cedula VARCHAR(10) NOT NULL,
    celular VARCHAR(30) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS nino_hijo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ficha_familiar_id BIGINT NOT NULL,
    nombre_completo VARCHAR(255) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    CONSTRAINT fk_nino_ficha FOREIGN KEY (ficha_familiar_id) REFERENCES ficha_familiar (id)
);
```

Los datos de `RepresentanteLegal` se persisten como columnas propias de `ficha_familiar` (value
object embebido, sin tabla independiente, ver research.md §1).

## Datos de prueba (data.sql)

Registros precargados (idempotentes, vía `MERGE INTO`) para desarrollo local y como fixture de
las pruebas de integración/funcionales:

- Una ficha `COMPLETA` con representante y 2 hijos (para probar consultas/edición sobre una
  familia ya completa sin tener que crearla en cada corrida).
- Una ficha `INCOMPLETA` sin hijos (para probar el escenario de cancelación, FR-011).
