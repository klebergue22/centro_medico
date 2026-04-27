package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "CIT_PROFESIONAL", schema = "CONSULTORIO")
public class CitProfesional implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "CIT_PROFESIONAL_GEN", sequenceName = "CONSULTORIO.SQ_CIT_PROFESIONAL", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CIT_PROFESIONAL_GEN")
    @Column(name = "ID_PROFESIONAL", nullable = false)
    private Long idProfesional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USUARIO")
    private UsuarioAuth usuario;

    @Column(name = "NO_PERSONA")
    private Integer noPersona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ESPECIALIDAD", nullable = false)
    private CitEspecialidad especialidad;

    @Column(name = "NOMBRE_PROFESIONAL", nullable = false, length = 150)
    private String nombreProfesional;

    @Column(name = "CODIGO_PROFESIONAL", length = 50)
    private String codigoProfesional;

    @Column(name = "EMAIL", length = 150)
    private String email;

    @Column(name = "ACTIVO", nullable = false, length = 1)
    private String activo;

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
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = new Date();
    }

    public Long getIdProfesional() { return idProfesional; }
    public void setIdProfesional(Long idProfesional) { this.idProfesional = idProfesional; }
    public UsuarioAuth getUsuario() { return usuario; }
    public void setUsuario(UsuarioAuth usuario) { this.usuario = usuario; }
    public Integer getNoPersona() { return noPersona; }
    public void setNoPersona(Integer noPersona) { this.noPersona = noPersona; }
    public CitEspecialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(CitEspecialidad especialidad) { this.especialidad = especialidad; }
    public String getNombreProfesional() { return nombreProfesional; }
    public void setNombreProfesional(String nombreProfesional) { this.nombreProfesional = nombreProfesional; }
    public String getCodigoProfesional() { return codigoProfesional; }
    public void setCodigoProfesional(String codigoProfesional) { this.codigoProfesional = codigoProfesional; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getActivo() { return activo; }
    public void setActivo(String activo) { this.activo = activo; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getUsrCreacion() { return usrCreacion; }
    public void setUsrCreacion(String usrCreacion) { this.usrCreacion = usrCreacion; }
    public Date getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Date fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public String getUsrActualizacion() { return usrActualizacion; }
    public void setUsrActualizacion(String usrActualizacion) { this.usrActualizacion = usrActualizacion; }
}
