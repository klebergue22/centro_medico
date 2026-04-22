package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "SEG_AUDITORIA_DET", schema = "CONSULTORIO")
public class SegAuditoriaDet implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "segAudDetGen")
    @SequenceGenerator(name = "segAudDetGen", sequenceName = "CONSULTORIO.SQ_SEG_AUDITORIA_DET", allocationSize = 1)
    @Column(name = "ID_AUDITORIA", nullable = false)
    private Long idAuditoria;

    @Column(name = "ID_USUARIO")
    private Long idUsuario;

    @Column(name = "ID_SESION")
    private Long idSesion;

    @Column(name = "MODULO", nullable = false, length = 60)
    private String modulo;

    @Column(name = "PROCESO", nullable = false, length = 120)
    private String proceso;

    @Column(name = "ENTIDAD", nullable = false, length = 80)
    private String entidad;

    @Column(name = "ID_REGISTRO", length = 80)
    private String idRegistro;

    @Column(name = "OPERACION", nullable = false, length = 1)
    private String operacion;

    @Lob
    @Column(name = "VALOR_ANTERIOR")
    private String valorAnterior;

    @Lob
    @Column(name = "VALOR_NUEVO")
    private String valorNuevo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_EVENTO", nullable = false)
    private Date fechaEvento;

    public Long getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(Long idAuditoria) { this.idAuditoria = idAuditoria; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdSesion() { return idSesion; }
    public void setIdSesion(Long idSesion) { this.idSesion = idSesion; }
    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    public String getProceso() { return proceso; }
    public void setProceso(String proceso) { this.proceso = proceso; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public String getIdRegistro() { return idRegistro; }
    public void setIdRegistro(String idRegistro) { this.idRegistro = idRegistro; }
    public String getOperacion() { return operacion; }
    public void setOperacion(String operacion) { this.operacion = operacion; }
    public String getValorAnterior() { return valorAnterior; }
    public void setValorAnterior(String valorAnterior) { this.valorAnterior = valorAnterior; }
    public String getValorNuevo() { return valorNuevo; }
    public void setValorNuevo(String valorNuevo) { this.valorNuevo = valorNuevo; }
    public Date getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(Date fechaEvento) { this.fechaEvento = fechaEvento; }
}
