package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "VW_CIT_PACIENTE_BUSQUEDA", schema = "CONSULTORIO")
public class VwCitPacienteBusqueda implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CEDULA", nullable = false, length = 20)
    private String cedula;

    @Column(name = "NO_PERSONA")
    private Integer noPersona;

    @Column(name = "ID_PERSONA_AUX")
    private Long idPersonaAux;

    @Column(name = "NOMBRE_COMPLETO", length = 200)
    private String nombreCompleto;

    @Column(name = "ORIGEN", length = 30)
    private String origen;

    public String getCedula() { return cedula; }
    public Integer getNoPersona() { return noPersona; }
    public Long getIdPersonaAux() { return idPersonaAux; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getOrigen() { return origen; }
}
