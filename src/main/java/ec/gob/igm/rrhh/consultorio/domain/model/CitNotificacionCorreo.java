package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "CIT_NOTIFICACION_CORREO", schema = "CONSULTORIO")
public class CitNotificacionCorreo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "CIT_NOTIF_GEN", sequenceName = "CONSULTORIO.SQ_CIT_NOTIF_CORREO", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CIT_NOTIF_GEN")
    @Column(name = "ID_NOTIFICACION", nullable = false)
    private Long idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CITA", nullable = false)
    private CitCita cita;

    @Column(name = "TIPO_EVENTO", nullable = false, length = 30)
    private String tipoEvento;

    @Column(name = "DESTINATARIO", nullable = false, length = 150)
    private String destinatario;

    @Column(name = "ASUNTO", nullable = false, length = 300)
    private String asunto;

    @Lob
    @Column(name = "CUERPO", nullable = false)
    private String cuerpo;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "INTENTOS", nullable = false)
    private Integer intentos;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_PROGRAMADA", nullable = false)
    private Date fechaProgramada;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_ENVIO")
    private Date fechaEnvio;

    @Column(name = "ERROR_ENVIO", length = 2000)
    private String errorEnvio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION", nullable = false)
    private Date fechaCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = "PENDIENTE";
        }
        if (intentos == null) {
            intentos = 0;
        }
        if (fechaProgramada == null) {
            fechaProgramada = new Date();
        }
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    public Long getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(Long idNotificacion) { this.idNotificacion = idNotificacion; }
    public CitCita getCita() { return cita; }
    public void setCita(CitCita cita) { this.cita = cita; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer intentos) { this.intentos = intentos; }
    public Date getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(Date fechaProgramada) { this.fechaProgramada = fechaProgramada; }
    public Date getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(Date fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public String getErrorEnvio() { return errorEnvio; }
    public void setErrorEnvio(String errorEnvio) { this.errorEnvio = errorEnvio; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getUsrCreacion() { return usrCreacion; }
    public void setUsrCreacion(String usrCreacion) { this.usrCreacion = usrCreacion; }
}
