package ec.gob.igm.rrhh.consultorio.web.ctrl;

/**
 * Excepción de validación de negocio reutilizable por controladores y fachadas.
 */
public class BusinessValidationException extends RuntimeException {

    private static final long serialVersionUID = 2L;

    public BusinessValidationException(String message) {
        super(message);
    }
}
