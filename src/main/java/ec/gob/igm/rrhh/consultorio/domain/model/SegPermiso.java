package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "SEG_PERMISO", schema = "CONSULTORIO")
public class SegPermiso implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "segPermisoGen")
    @SequenceGenerator(name = "segPermisoGen", sequenceName = "CONSULTORIO.SQ_SEG_PERMISO", allocationSize = 1)
    @Column(name = "ID_PERMISO", nullable = false)
    private Long idPermiso;

    @Column(name = "CODIGO", nullable = false, length = 80)
    private String codigo;

    @Column(name = "MODULO", nullable = false, length = 50)
    private String modulo;

    @Column(name = "RECURSO", nullable = false, length = 80)
    private String recurso;

    @Column(name = "ACCION", nullable = false, length = 50)
    private String accion;

    public Long getIdPermiso() { return idPermiso; }
    public void setIdPermiso(Long idPermiso) { this.idPermiso = idPermiso; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
}
