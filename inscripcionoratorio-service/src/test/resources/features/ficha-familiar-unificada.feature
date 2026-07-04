# language: es
Característica: Ficha familiar unificada
  Como Receptor de Inscripciones
  Quiero registrar primero al representante legal y luego agregar uno o más niños en la misma ficha
  Para no tener que pedir los datos del representante más de una vez por familia

  Escenario: Registrar representante y habilitar la sección de hijos
    Dado que inicio una ficha de inscripción nueva
    Cuando ingreso los datos del representante legal con nombre "Maria Perez", cedula "1710034065", celular "0991234567" y direccion "Av. Siempre Viva 123"
    Entonces el sistema guarda esos datos y la ficha queda en estado "INCOMPLETA" sin hijos

  Escenario: Agregar un segundo hijo vinculado automáticamente al representante ya ingresado
    Dado que el representante legal ya fue registrado en la ficha actual
    Y que agregué un primer hijo llamado "Juan Perez" nacido el "2016-05-10"
    Cuando agrego un segundo hijo llamado "Ana Perez" nacido el "2018-09-02"
    Entonces ambos hijos quedan vinculados al mismo representante sin repetir sus datos
    Y la ficha queda en estado "COMPLETA"

  Escenario: Bloquear la ficha cuando los datos obligatorios del representante están incompletos
    Dado que el formulario tiene datos obligatorios del representante incompletos
    Cuando intento guardar o crear la ficha
    Entonces el sistema rechaza la operación indicando los campos obligatorios faltantes
