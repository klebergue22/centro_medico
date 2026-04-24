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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_LOGOUT")
    private Date fechaLogout;

    @Column(name = "ACTIVA", nullable = false, length = 1)
    private String activa;

    @Column(name = "IP_ORIGEN", length = 45)
    private String ipOrigen;

    @Column(name = "NAVEGADOR", length = 300)
    private String navegador;

    @Column(name = "SISTEMA_OPERATIVO", length = 120)
    private String sistemaOperativo;

    @Column(name = "ES_MOVIL", length = 1)
    private String esMovil;

    @Column(name = "MOTIVO_CIERRE", length = 200)
    private String motivoCierre;

    @Column(name = "OBSERVACION", length = 1000)
    private String observacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION")
    private Date fCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_ACTUALIZACION")
    private Date fActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;

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
    public Date getFechaLogout() { return fechaLogout; }
    public void setFechaLogout(Date fechaLogout) { this.fechaLogout = fechaLogout; }
    public String getActiva() { return activa; }
    public void setActiva(String activa) { this.activa = activa; }
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
    public String getNavegador() { return navegador; }
    public void setNavegador(String navegador) { this.navegador = navegador; }
    public String getSistemaOperativo() { return sistemaOperativo; }
    public void setSistemaOperativo(String sistemaOperativo) { this.sistemaOperativo = sistemaOperativo; }
    public String getEsMovil() { return esMovil; }
    public void setEsMovil(String esMovil) { this.esMovil = esMovil; }
    public String getMotivoCierre() { return motivoCierre; }
    public void setMotivoCierre(String motivoCierre) { this.motivoCierre = motivoCierre; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Date getFCreacion() { return fCreacion; }
    public void setFCreacion(Date fCreacion) { this.fCreacion = fCreacion; }
    public String getUsrCreacion() { return usrCreacion; }
    public void setUsrCreacion(String usrCreacion) { this.usrCreacion = usrCreacion; }
    public Date getFActualizacion() { return fActualizacion; }
    public void setFActualizacion(Date fActualizacion) { this.fActualizacion = fActualizacion; }
    public String getUsrActualizacion() { return usrActualizacion; }
    public void setUsrActualizacion(String usrActualizacion) { this.usrActualizacion = usrActualizacion; }
}
