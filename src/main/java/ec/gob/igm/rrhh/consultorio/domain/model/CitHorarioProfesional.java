package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "CIT_HORARIO_PROFESIONAL", schema = "CONSULTORIO")
public class CitHorarioProfesional implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "CIT_HORARIO_GEN", sequenceName = "CONSULTORIO.SQ_CIT_HORARIO", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CIT_HORARIO_GEN")
    @Column(name = "ID_HORARIO", nullable = false)
    private Long idHorario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PROFESIONAL", nullable = false)
    private CitProfesional profesional;

    @Column(name = "DIA_SEMANA", nullable = false)
    private Integer diaSemana;

    @Column(name = "HORA_INICIO_MIN", nullable = false)
    private Integer horaInicioMin;

    @Column(name = "HORA_FIN_MIN", nullable = false)
    private Integer horaFinMin;

    @Column(name = "DURACION_SLOT_MIN", nullable = false)
    private Integer duracionSlotMin;

    @Column(name = "ACTIVO", nullable = false, length = 1)
    private String activo;

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
        if (activo == null) {
            activo = "S";
        }
        if (duracionSlotMin == null) {
            duracionSlotMin = 30;
        }
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = new Date();
    }

    public Long getIdHorario() { return idHorario; }
    public void setIdHorario(Long idHorario) { this.idHorario = idHorario; }
    public CitProfesional getProfesional() { return profesional; }
    public void setProfesional(CitProfesional profesional) { this.profesional = profesional; }
    public Integer getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }
    public Integer getHoraInicioMin() { return horaInicioMin; }
    public void setHoraInicioMin(Integer horaInicioMin) { this.horaInicioMin = horaInicioMin; }
    public Integer getHoraFinMin() { return horaFinMin; }
    public void setHoraFinMin(Integer horaFinMin) { this.horaFinMin = horaFinMin; }
    public Integer getDuracionSlotMin() { return duracionSlotMin; }
    public void setDuracionSlotMin(Integer duracionSlotMin) { this.duracionSlotMin = duracionSlotMin; }
    public String getActivo() { return activo; }
    public void setActivo(String activo) { this.activo = activo; }
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
