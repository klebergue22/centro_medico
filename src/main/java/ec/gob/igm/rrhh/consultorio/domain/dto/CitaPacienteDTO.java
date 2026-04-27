package ec.gob.igm.rrhh.consultorio.domain.dto;

import java.io.Serializable;

/**
 * Resultado de búsqueda de paciente para agendamiento de citas.
 */
public class CitaPacienteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String cedula;
    private final Integer noPersona;
    private final Long idPersonaAux;
    private final Long idFichaActiva;
    private final String nombreCompleto;
    private final boolean requiereCrearFicha;

    private CitaPacienteDTO(String cedula, Integer noPersona, Long idPersonaAux, Long idFichaActiva,
            String nombreCompleto, boolean requiereCrearFicha) {
        this.cedula = cedula;
        this.noPersona = noPersona;
        this.idPersonaAux = idPersonaAux;
        this.idFichaActiva = idFichaActiva;
        this.nombreCompleto = nombreCompleto;
        this.requiereCrearFicha = requiereCrearFicha;
    }

    public static CitaPacienteDTO encontradoConFicha(String cedula, Integer noPersona, Long idPersonaAux,
            Long idFichaActiva, String nombreCompleto) {
        return new CitaPacienteDTO(cedula, noPersona, idPersonaAux, idFichaActiva, nombreCompleto, false);
    }

    public static CitaPacienteDTO requiereFicha(String cedula, Integer noPersona, Long idPersonaAux, String nombreCompleto) {
        return new CitaPacienteDTO(cedula, noPersona, idPersonaAux, null, nombreCompleto, true);
    }

    public String getCedula() {
        return cedula;
    }

    public Integer getNoPersona() {
        return noPersona;
    }

    public Long getIdPersonaAux() {
        return idPersonaAux;
    }

    public Long getIdFichaActiva() {
        return idFichaActiva;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public boolean isRequiereCrearFicha() {
        return requiereCrearFicha;
    }
}
