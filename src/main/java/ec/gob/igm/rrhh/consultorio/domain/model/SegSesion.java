package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "SEG_SESION", schema = "CONSULTORIO")
public class SegSesion implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "segSesionGen")
    @SequenceGenerator(name = "segSesionGen", sequenceName = "CONSULTORIO.SQ_SEG_SESION", allocationSize = 1)
    @Column(name = "ID_SESION", nullable = false)
    private Long idSesion;

    @Column(name = "ID_USUARIO", nullable = false)
    private Long idUsuario;

    @Column(name = "TOKEN_SESION", nullable = false, length = 200)
    private String tokenSesion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_LOGIN", nullable = false)
    private Date fechaLogin;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_ULTIMA_ACT", nullable = false)
    private Date fechaUltimaAct;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_EXPIRACION", nullable = false)
    private Date fechaExpiracion;

    @Column(name = "ACTIVA", nullable = false, length = 1)
    private String activa;

    public Long getIdSesion() { return idSesion; }
    public void setIdSesion(Long idSesion) { this.idSesion = idSesion; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getTokenSesion() { return tokenSesion; }
    public void setTokenSesion(String tokenSesion) { this.tokenSesion = tokenSesion; }
    public Date getFechaLogin() { return fechaLogin; }
    public void setFechaLogin(Date fechaLogin) { this.fechaLogin = fechaLogin; }
    public Date getFechaUltimaAct() { return fechaUltimaAct; }
    public void setFechaUltimaAct(Date fechaUltimaAct) { this.fechaUltimaAct = fechaUltimaAct; }
    public Date getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(Date fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public String getActiva() { return activa; }
    public void setActiva(String activa) { this.activa = activa; }
}
