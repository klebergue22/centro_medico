package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "SEG_ROL", schema = "CONSULTORIO")
public class SegRol implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "segRolGen")
    @SequenceGenerator(name = "segRolGen", sequenceName = "CONSULTORIO.SQ_SEG_ROL", allocationSize = 1)
    @Column(name = "ID_ROL", nullable = false)
    private Long idRol;

    @Column(name = "CODIGO", nullable = false, length = 50)
    private String codigo;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ACTIVO", nullable = false, length = 1)
    private String activo;

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getActivo() { return activo; }
    public void setActivo(String activo) { this.activo = activo; }
}
