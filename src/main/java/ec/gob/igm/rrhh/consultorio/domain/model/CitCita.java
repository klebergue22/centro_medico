package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "CIT_CITA", schema = "CONSULTORIO")
public class CitCita implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "CIT_CITA_GEN", sequenceName = "CONSULTORIO.SQ_CIT_CITA", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CIT_CITA_GEN")
    @Column(name = "ID_CITA", nullable = false)
    private Long idCita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SLOT", nullable = false)
    private CitSlotAgenda slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO_PACIENTE", nullable = false)
    private UsuarioAuth usuarioPaciente;

    @Column(name = "NO_PERSONA")
    private Integer noPersona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PERSONA_AUX")
    private PersonaAux personaAux;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ESPECIALIDAD", nullable = false)
    private CitEspecialidad especialidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PROFESIONAL", nullable = false)
    private CitProfesional profesional;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_INICIO", nullable = false)
    private Date fechaInicio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_FIN", nullable = false)
    private Date fechaFin;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "MOTIVO_ATENCION", length = 1000)
    private String motivoAtencion;

    @Column(name = "MOTIVO_CANCELACION", length = 1000)
    private String motivoCancelacion;

    @Column(name = "OBSERVACION", length = 1000)
    private String observacion;

    @Column(name = "CORREO_PACIENTE", nullable = false, length = 150)
    private String correoPaciente;

    @Column(name = "TELEFONO_PACIENTE", length = 30)
    private String telefonoPaciente;

    @Column(name = "ORIGEN", nullable = false, length = 20)
    private String origen;

    @Column(name = "REQ_NOTIFICAR", nullable = false, length = 1)
    private String reqNotificar;

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

    @Column(name = "ID_SLOT_ACTIVO", insertable = false, updatable = false)
    private Long idSlotActivo;

    @Column(name = "PACIENTE_CLAVE_ACTIVO", insertable = false, updatable = false, length = 50)
    private String pacienteClaveActivo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_INICIO_ACTIVA", insertable = false, updatable = false)
    private Date fechaInicioActiva;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = "PROGRAMADA";
        }
        if (origen == null) {
            origen = "WEB";
        }
        if (reqNotificar == null) {
            reqNotificar = "S";
        }
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = new Date();
    }

    public Long getIdCita() { return idCita; }
    public void setIdCita(Long idCita) { this.idCita = idCita; }
    public CitSlotAgenda getSlot() { return slot; }
    public void setSlot(CitSlotAgenda slot) { this.slot = slot; }
    public UsuarioAuth getUsuarioPaciente() { return usuarioPaciente; }
    public void setUsuarioPaciente(UsuarioAuth usuarioPaciente) { this.usuarioPaciente = usuarioPaciente; }
    public Integer getNoPersona() { return noPersona; }
    public void setNoPersona(Integer noPersona) { this.noPersona = noPersona; }
    public PersonaAux getPersonaAux() { return personaAux; }
    public void setPersonaAux(PersonaAux personaAux) { this.personaAux = personaAux; }
    public FichaOcupacional getFicha() { return ficha; }
    public void setFicha(FichaOcupacional ficha) { this.ficha = ficha; }
    public CitEspecialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(CitEspecialidad especialidad) { this.especialidad = especialidad; }
    public CitProfesional getProfesional() { return profesional; }
    public void setProfesional(CitProfesional profesional) { this.profesional = profesional; }
    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }
    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivoAtencion() { return motivoAtencion; }
    public void setMotivoAtencion(String motivoAtencion) { this.motivoAtencion = motivoAtencion; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getCorreoPaciente() { return correoPaciente; }
    public void setCorreoPaciente(String correoPaciente) { this.correoPaciente = correoPaciente; }
    public String getTelefonoPaciente() { return telefonoPaciente; }
    public void setTelefonoPaciente(String telefonoPaciente) { this.telefonoPaciente = telefonoPaciente; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public String getReqNotificar() { return reqNotificar; }
    public void setReqNotificar(String reqNotificar) { this.reqNotificar = reqNotificar; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getUsrCreacion() { return usrCreacion; }
    public void setUsrCreacion(String usrCreacion) { this.usrCreacion = usrCreacion; }
    public Date getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Date fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public String getUsrActualizacion() { return usrActualizacion; }
    public void setUsrActualizacion(String usrActualizacion) { this.usrActualizacion = usrActualizacion; }
    public Long getIdSlotActivo() { return idSlotActivo; }
    public String getPacienteClaveActivo() { return pacienteClaveActivo; }
    public Date getFechaInicioActiva() { return fechaInicioActiva; }
}
