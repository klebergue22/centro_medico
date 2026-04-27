package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "VW_CIT_FICHA_ACTIVA", schema = "CONSULTORIO")
public class VwCitFichaActiva implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_FICHA", nullable = false)
    private Long idFicha;

    @Column(name = "NO_PERSONA")
    private Integer noPersona;

    @Column(name = "ID_PERSONA_AUX")
    private Long idPersonaAux;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_EVALUACION")
    private Date fechaEvaluacion;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION")
    private Date fechaCreacion;

    public Long getIdFicha() { return idFicha; }
    public Integer getNoPersona() { return noPersona; }
    public Long getIdPersonaAux() { return idPersonaAux; }
    public Date getFechaEvaluacion() { return fechaEvaluacion; }
    public String getEstado() { return estado; }
    public Date getFechaCreacion() { return fechaCreacion; }
}
