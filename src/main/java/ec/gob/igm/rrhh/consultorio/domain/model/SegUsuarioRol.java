package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "SEG_USUARIO_ROL", schema = "CONSULTORIO")
public class SegUsuarioRol implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "segUsuarioRolGen")
    @SequenceGenerator(name = "segUsuarioRolGen", sequenceName = "CONSULTORIO.SQ_SEG_USUARIO_ROL", allocationSize = 1)
    @Column(name = "ID_USUARIO_ROL", nullable = false)
    private Long idUsuarioRol;

    @Column(name = "ID_USUARIO", nullable = false)
    private Long idUsuario;

    @Column(name = "ID_ROL", nullable = false)
    private Long idRol;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_DESDE", nullable = false)
    private Date fechaDesde;

    @Column(name = "ACTIVO", nullable = false, length = 1)
    private String activo;

    public Long getIdUsuarioRol() { return idUsuarioRol; }
    public void setIdUsuarioRol(Long idUsuarioRol) { this.idUsuarioRol = idUsuarioRol; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }
    public Date getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(Date fechaDesde) { this.fechaDesde = fechaDesde; }
    public String getActivo() { return activo; }
    public void setActivo(String activo) { this.activo = activo; }
}
