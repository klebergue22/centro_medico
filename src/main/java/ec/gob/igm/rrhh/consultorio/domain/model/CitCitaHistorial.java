package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "CIT_CITA_HISTORIAL", schema = "CONSULTORIO")
public class CitCitaHistorial implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "CIT_HIST_GEN", sequenceName = "CONSULTORIO.SQ_CIT_HISTORIAL", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CIT_HIST_GEN")
    @Column(name = "ID_HISTORIAL", nullable = false)
    private Long idHistorial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CITA", nullable = false)
    private CitCita cita;

    @Column(name = "ACCION", nullable = false, length = 30)
    private String accion;

    @Column(name = "ESTADO_ANTERIOR", length = 20)
    private String estadoAnterior;

    @Column(name = "ESTADO_NUEVO", length = 20)
    private String estadoNuevo;

    @Column(name = "ID_SLOT_ANTERIOR")
    private Long idSlotAnterior;

    @Column(name = "ID_SLOT_NUEVO")
    private Long idSlotNuevo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_INICIO_ANT")
    private Date fechaInicioAnt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_FIN_ANT")
    private Date fechaFinAnt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_INICIO_NUEVA")
    private Date fechaInicioNueva;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_FIN_NUEVA")
    private Date fechaFinNueva;

    @Column(name = "OBSERVACION", length = 1000)
    private String observacion;

    @Column(name = "USUARIO_ACCION", length = 50)
    private String usuarioAccion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_ACCION", nullable = false)
    private Date fechaAccion;

    @PrePersist
    public void prePersist() {
        if (fechaAccion == null) {
            fechaAccion = new Date();
        }
    }

    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }
    public CitCita getCita() { return cita; }
    public void setCita(CitCita cita) { this.cita = cita; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public Long getIdSlotAnterior() { return idSlotAnterior; }
    public void setIdSlotAnterior(Long idSlotAnterior) { this.idSlotAnterior = idSlotAnterior; }
    public Long getIdSlotNuevo() { return idSlotNuevo; }
    public void setIdSlotNuevo(Long idSlotNuevo) { this.idSlotNuevo = idSlotNuevo; }
    public Date getFechaInicioAnt() { return fechaInicioAnt; }
    public void setFechaInicioAnt(Date fechaInicioAnt) { this.fechaInicioAnt = fechaInicioAnt; }
    public Date getFechaFinAnt() { return fechaFinAnt; }
    public void setFechaFinAnt(Date fechaFinAnt) { this.fechaFinAnt = fechaFinAnt; }
    public Date getFechaInicioNueva() { return fechaInicioNueva; }
    public void setFechaInicioNueva(Date fechaInicioNueva) { this.fechaInicioNueva = fechaInicioNueva; }
    public Date getFechaFinNueva() { return fechaFinNueva; }
    public void setFechaFinNueva(Date fechaFinNueva) { this.fechaFinNueva = fechaFinNueva; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getUsuarioAccion() { return usuarioAccion; }
    public void setUsuarioAccion(String usuarioAccion) { this.usuarioAccion = usuarioAccion; }
    public Date getFechaAccion() { return fechaAccion; }
    public void setFechaAccion(Date fechaAccion) { this.fechaAccion = fechaAccion; }
}
