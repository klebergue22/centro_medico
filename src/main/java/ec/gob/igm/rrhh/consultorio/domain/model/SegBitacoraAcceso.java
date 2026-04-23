package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "SEG_BITACORA_ACCESO", schema = "CONSULTORIO")
public class SegBitacoraAcceso implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "segBitacoraGen")
    @SequenceGenerator(name = "segBitacoraGen", sequenceName = "CONSULTORIO.SQ_SEG_BITACORA_ACCESO", allocationSize = 1)
    @Column(name = "ID_BITACORA", nullable = false)
    private Long idBitacora;

    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "USERNAME_INTENTADO", length = 50)
    private String usernameIntentado;

    @Column(name = "EVENTO", nullable = false, length = 30)
    private String evento;

    @Column(name = "EXITOSO", nullable = false, length = 1)
    private String exitoso;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_EVENTO", nullable = false)
    private Date fechaEvento;

    @Column(name = "DETALLE", length = 1000)
    private String detalle;

    @Column(name = "IP_ORIGEN", length = 45)
    private String ipOrigen;

    @Column(name = "NAVEGADOR", length = 300)
    private String navegador;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    public Long getIdBitacora() { return idBitacora; }
    public void setIdBitacora(Long idBitacora) { this.idBitacora = idBitacora; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getUsernameIntentado() { return usernameIntentado; }
    public void setUsernameIntentado(String usernameIntentado) { this.usernameIntentado = usernameIntentado; }
    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    public String getExitoso() { return exitoso; }
    public void setExitoso(String exitoso) { this.exitoso = exitoso; }
    public Date getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(Date fechaEvento) { this.fechaEvento = fechaEvento; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }
    public String getNavegador() { return navegador; }
    public void setNavegador(String navegador) { this.navegador = navegador; }
    public String getUsrCreacion() { return usrCreacion; }
    public void setUsrCreacion(String usrCreacion) { this.usrCreacion = usrCreacion; }
}
