package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "CIT_SLOT_AGENDA", schema = "CONSULTORIO")
public class CitSlotAgenda implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "CIT_SLOT_GEN", sequenceName = "CONSULTORIO.SQ_CIT_SLOT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CIT_SLOT_GEN")
    @Column(name = "ID_SLOT", nullable = false)
    private Long idSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_HORARIO")
    private CitHorarioProfesional horario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PROFESIONAL", nullable = false)
    private CitProfesional profesional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ESPECIALIDAD", nullable = false)
    private CitEspecialidad especialidad;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_INICIO", nullable = false)
    private Date fechaInicio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_FIN", nullable = false)
    private Date fechaFin;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "OBSERVACION", length = 500)
    private String observacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION", nullable = false)
    private Date fechaCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_ACTUALIZACION")
    private Date fechaActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = "DISPONIBLE";
        }
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = new Date();
    }

    public Long getIdSlot() { return idSlot; }
    public void setIdSlot(Long idSlot) { this.idSlot = idSlot; }
    public CitHorarioProfesional getHorario() { return horario; }
    public void setHorario(CitHorarioProfesional horario) { this.horario = horario; }
    public CitProfesional getProfesional() { return profesional; }
    public void setProfesional(CitProfesional profesional) { this.profesional = profesional; }
    public CitEspecialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(CitEspecialidad especialidad) { this.especialidad = especialidad; }
    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }
    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getUsrCreacion() { return usrCreacion; }
    public void setUsrCreacion(String usrCreacion) { this.usrCreacion = usrCreacion; }
    public Date getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Date fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public String getUsrActualizacion() { return usrActualizacion; }
    public void setUsrActualizacion(String usrActualizacion) { this.usrActualizacion = usrActualizacion; }
}
