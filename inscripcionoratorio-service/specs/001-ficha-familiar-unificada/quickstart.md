# Quickstart: Ficha familiar unificada

Guía para validar de extremo a extremo que US-01 funciona, una vez implementada según
`plan.md`, `data-model.md` y el contrato en `contracts/ficha-familiar.openapi.yaml`.

## Prerrequisitos

- JDK 25 instalado (toolchain del proyecto).
- Plugin `org.openapi.generator` y `jacoco` configurados en `build.gradle` (ver research.md §4-5).
- Base de datos H2 en memoria (perfil por defecto), sin configuración adicional.

## Levantar el servicio

```bash
./gradlew bootRun
```

El servicio queda disponible en `http://localhost:8080/api/v1`.

**Datos precargados**: al iniciar, `data.sql` ya inserta una ficha `COMPLETA` (representante +
2 hijos) y una ficha `INCOMPLETA` (solo representante), siguiendo el mismo patrón de
`citasalud-service` (ver `data-model.md` → Datos de prueba). Se pueden usar sus IDs directamente
en los Escenarios 4 y 5 sin repetir el Escenario 1.

## Escenario 1 — Registrar representante y habilitar hijos (AC #1 de spec.md)

```bash
curl -i -X POST http://localhost:8080/api/v1/fichas-familiares \
  -H "Content-Type: application/json" \
  -d '{
        "nombreCompleto": "Maria Perez",
        "cedula": "1710034065",
        "celular": "0991234567",
        "direccion": "Av. Siempre Viva 123"
      }'
```

**Resultado esperado**: `201 Created`, cuerpo con `estado: "INCOMPLETA"`, `representanteLegal`
con los datos enviados y `hijos: []`. Anotar el `id` retornado como `FICHA_ID`.

## Escenario 2 — Agregar dos hijos sin repetir datos del representante (AC #2 de spec.md)

```bash
curl -i -X POST http://localhost:8080/api/v1/fichas-familiares/FICHA_ID/hijos \
  -H "Content-Type: application/json" \
  -d '{"nombreCompleto": "Juan Perez", "fechaNacimiento": "2016-05-10"}'

curl -i -X POST http://localhost:8080/api/v1/fichas-familiares/FICHA_ID/hijos \
  -H "Content-Type: application/json" \
  -d '{"nombreCompleto": "Ana Perez", "fechaNacimiento": "2018-09-02"}'
```

**Resultado esperado**: Ambas respuestas `201 Created`; la segunda respuesta muestra
`estado: "COMPLETA"` y `hijos` con **ambos** niños, cada uno vinculado a la misma
`representanteLegal` (sin que se haya vuelto a enviar ningún dato del adulto).

## Escenario 3 — Bloqueo por datos incompletos del representante (AC #3 de spec.md)

```bash
curl -i -X POST http://localhost:8080/api/v1/fichas-familiares \
  -H "Content-Type: application/json" \
  -d '{"nombreCompleto": "Carlos Ruiz", "cedula": "1710034065"}'
```

**Resultado esperado**: `400 Bad Request` (faltan `celular` y `direccion`); el cuerpo de error
incluye `camposFaltantes: ["celular", "direccion"]`. No se crea ninguna ficha.

## Escenario 4 — Cancelar una ficha sin hijos (FR-011)

```bash
curl -i -X DELETE http://localhost:8080/api/v1/fichas-familiares/FICHA_ID_SIN_HIJOS
```

**Resultado esperado**: `200 OK` con cuerpo `{"mensaje": "Ficha familiar eliminada correctamente."}`
y la ficha deja de existir. Si se repite el `DELETE` sobre una ficha que ya tiene hijos (por
ejemplo, `FICHA_ID` del Escenario 2), se espera `409 Conflict`.

## Escenario 5 — Editar datos del representante (FR-010)

```bash
curl -i -X PUT http://localhost:8080/api/v1/fichas-familiares/FICHA_ID/representante \
  -H "Content-Type: application/json" \
  -d '{
        "nombreCompleto": "Maria Perez",
        "cedula": "1710034065",
        "celular": "0987654321",
        "direccion": "Av. Siempre Viva 123"
      }'
```

**Resultado esperado**: `200 OK`, `celular` actualizado en la respuesta, aunque la ficha ya
tenga hijos registrados.

## Validación automatizada equivalente

- Los Escenarios 1-3 corresponden exactamente a los 3 Acceptance Scenarios de
  `spec.md` y están cubiertos por el feature file Cucumber
  `src/test/resources/features/ficha-familiar-unificada.feature`.
- Todos los escenarios están además cubiertos por `FichaFamiliarControllerIT`
  (`@SpringBootTest` + `MockMvc` + H2).
- Ejecutar `./gradlew test jacocoTestReport jacocoTestCoverageVerification` para confirmar que
  las pruebas pasan y que se cumplen los umbrales de cobertura del Principio V.
