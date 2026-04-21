# Pruebas funcionales

## Salud ocupacional

- `PF-SO-01` Validar Step 2 incompleto. Dado un registro sin puesto de trabajo, sin actividades y sin medidas preventivas, al intentar continuar el sistema debe bloquear el avance y mostrar los tres mensajes obligatorios.
- `PF-SO-02` Validar Step 2 con datos m\u00ednimos. Dado un registro con puesto tomado desde `CIIU`, al menos una actividad y una medida preventiva, el sistema debe aceptar el guardado del bloque de riesgos laborales.
- `PF-SO-03` Validar cierre m\u00e9dico ocupacional incompleto. Dado un Step 3 sin diagn\u00f3stico, sin aptitud m\u00e9dica, sin recomendaci\u00f3n y sin datos del profesional, el sistema debe impedir el cierre y detallar todos los campos faltantes.
- `PF-SO-04` Validar cierre m\u00e9dico ocupacional correcto. Dado un Step 3 con diagn\u00f3stico CIE10, aptitud, recomendaci\u00f3n y datos del m\u00e9dico, el sistema debe aceptar la finalizaci\u00f3n de la ficha.

## Citas m\u00e9dicas

- `PF-CM-01` B\u00fasqueda por c\u00e9dula. Dada una c\u00e9dula existente, el sistema debe cargar el paciente, su alergia registrada y las consultas previas, mostrando el mensaje `Paciente cargado`.
- `PF-CM-02` Guardado sin paciente. Si se intenta guardar una consulta sin haber buscado un paciente, el sistema debe rechazar la acci\u00f3n con el mensaje `Paciente requerido`.
- `PF-CM-03` Guardado de consulta con normalizaci\u00f3n. Dada una consulta con diagn\u00f3sticos repetidos, signos vitales y receta, el sistema debe omitir duplicados por c\u00f3digo CIE10, persistir la presi\u00f3n arterial parseada, asociar la receta a la consulta y confirmar con `Consulta guardada`.
- `PF-CM-04` Preparaci\u00f3n de certificado m\u00e9dico. Dado un paciente cargado con direcci\u00f3n y cargo, al abrir el certificado el sistema debe completar autom\u00e1ticamente fechas, cargo del m\u00e9dico, domicilio y cargo del paciente.

## Automatizaci\u00f3n disponible

- Las validaciones anteriores quedaron automatizadas en `mvn test` con las clases `SaludOcupacionalFunctionalTest` y `ConsultaMedicaCtrlFunctionalTest`.
