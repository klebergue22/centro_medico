package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "SEG_ROL_PERMISO", schema = "CONSULTORIO")
public class SegRolPermiso implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "segRolPermisoGen")
    @SequenceGenerator(name = "segRolPermisoGen", sequenceName = "CONSULTORIO.SQ_SEG_ROL_PERMISO", allocationSize = 1)
    @Column(name = "ID_ROL_PERMISO", nullable = false)
    private Long idRolPermiso;

    @Column(name = "ID_ROL", nullable = false)
    private Long idRol;

    @Column(name = "ID_PERMISO", nullable = false)
    private Long idPermiso;

    @Column(name = "ACTIVO", nullable = false, length = 1)
    private String activo;

    public Long getIdRolPermiso() { return idRolPermiso; }
    public void setIdRolPermiso(Long idRolPermiso) { this.idRolPermiso = idRolPermiso; }
    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }
    public Long getIdPermiso() { return idPermiso; }
    public void setIdPermiso(Long idPermiso) { this.idPermiso = idPermiso; }
    public String getActivo() { return activo; }
    public void setActivo(String activo) { this.activo = activo; }
}
