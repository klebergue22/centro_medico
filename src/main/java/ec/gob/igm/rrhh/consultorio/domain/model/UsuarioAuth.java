package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "SEG_USUARIO", schema = "CONSULTORIO")
public class UsuarioAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "segUsuarioGen")
    @SequenceGenerator(name = "segUsuarioGen", sequenceName = "CONSULTORIO.SQ_SEG_USUARIO", allocationSize = 1)
    @Column(name = "ID_USUARIO", nullable = false)
    private Long idUsuario;

    @Column(name = "USERNAME", nullable = false, length = 50)
    private String username;

    @Column(name = "CLAVE_HASH", nullable = false, length = 255)
    private String claveHash;

    @Column(name = "ALGORITMO_HASH", nullable = false, length = 30)
    private String algoritmoHash;

    @Column(name = "NOMBRE_VISIBLE", nullable = false, length = 150)
    private String nombreVisible;

    @Column(name = "EMAIL", length = 150)
    private String email;

    @Column(name = "NO_PERSONA")
    private Integer noPersona;

    @Column(name = "NO_CEDULA", length = 20)
    private String noCedula;

    @Column(name = "ACTIVO", nullable = false, length = 1)
    private String activo;

    @Column(name = "BLOQUEADO", nullable = false, length = 1)
    private String bloqueado;

    @Column(name = "INTENTOS_FALLIDOS", nullable = false)
    private Integer intentosFallidos;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_ULTIMO_LOGIN")
    private Date fechaUltimoLogin;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_ULTIMO_CAMBIO_CLAVE")
    private Date fechaUltimoCambioClave;

    @Column(name = "REQUIERE_CAMBIO_CLAVE", nullable = false, length = 1)
    private String requiereCambioClave;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION", nullable = false)
    private Date fechaCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @PrePersist
    public void prePersist() {
        if (algoritmoHash == null) algoritmoHash = "SHA-256";
        if (activo == null) activo = "S";
        if (bloqueado == null) bloqueado = "N";
        if (intentosFallidos == null) intentosFallidos = 0;
        if (requiereCambioClave == null) requiereCambioClave = "S";
        if (fechaCreacion == null) fechaCreacion = new Date();
    }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getClaveHash() { return claveHash; }
    public void setClaveHash(String claveHash) { this.claveHash = claveHash; }
    public String getAlgoritmoHash() { return algoritmoHash; }
    public void setAlgoritmoHash(String algoritmoHash) { this.algoritmoHash = algoritmoHash; }
    public String getNombreVisible() { return nombreVisible; }
    public void setNombreVisible(String nombreVisible) { this.nombreVisible = nombreVisible; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getNoPersona() { return noPersona; }
    public void setNoPersona(Integer noPersona) { this.noPersona = noPersona; }
    public String getNoCedula() { return noCedula; }
    public void setNoCedula(String noCedula) { this.noCedula = noCedula; }
    public String getActivo() { return activo; }
    public void setActivo(String activo) { this.activo = activo; }
    public String getBloqueado() { return bloqueado; }
    public void setBloqueado(String bloqueado) { this.bloqueado = bloqueado; }
    public Integer getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(Integer intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public Date getFechaUltimoLogin() { return fechaUltimoLogin; }
    public void setFechaUltimoLogin(Date fechaUltimoLogin) { this.fechaUltimoLogin = fechaUltimoLogin; }
    public Date getFechaUltimoCambioClave() { return fechaUltimoCambioClave; }
    public void setFechaUltimoCambioClave(Date fechaUltimoCambioClave) { this.fechaUltimoCambioClave = fechaUltimoCambioClave; }
    public String getRequiereCambioClave() { return requiereCambioClave; }
    public void setRequiereCambioClave(String requiereCambioClave) { this.requiereCambioClave = requiereCambioClave; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getUsrCreacion() { return usrCreacion; }
    public void setUsrCreacion(String usrCreacion) { this.usrCreacion = usrCreacion; }
}
