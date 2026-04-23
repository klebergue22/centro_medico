/*
  Patch Oracle 11g
  Amplía CK_SEG_BA_EVENTO para permitir el evento CONSULTA_REPORTE
  usado por EmpleadosController al consultar historial por cédula.
*/

ALTER TABLE CONSULTORIO.SEG_BITACORA_ACCESO DROP CONSTRAINT CK_SEG_BA_EVENTO;

ALTER TABLE CONSULTORIO.SEG_BITACORA_ACCESO
    ADD CONSTRAINT CK_SEG_BA_EVENTO
    CHECK (
        EVENTO IN (
            'LOGIN',
            'LOGOUT',
            'INTENTO_FALLIDO',
            'CAMBIO_CLAVE',
            'RESET_CLAVE',
            'REGISTRO_USUARIO',
            'CONSULTA_REPORTE'
        )
    );
