/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.domain.model;

/**
 *
 * @author GUERRA_KLEBER
 */



import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(
    name = "FICHA_DIAGNOSTICO",
    schema = "CONSULTORIO",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_FD_FICHA_CIE10",
        columnNames = {"ID_FICHA", "COD_CIE10"}
    )
)
public class FichaDiagnostico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
        name = "FD_GEN",
        sequenceName = "CONSULTORIO.SQ_FICHA_DIAG",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FD_GEN")
    @Column(name = "ID_FICHA_DIAG", nullable = false)
    private Long idFichaDiag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    @Column(name = "COD_CIE10", length = 10, nullable = false)
    private String codCie10;

    @Column(name = "DESCRIPCION", length = 500)
    private String descripcion;

    @Column(name = "TIPO_DIAG", length = 1, nullable = false)
    private String tipoDiag; // P / S

    @Column(name = "ORDEN")
    private Integer orden;

    @Column(name = "ESTADO", length = 20, nullable = false)
    private String estado;

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

    // ==========================
    // Constructores
    // ==========================

    // Constructor vacío (requerido por JPA)
    public FichaDiagnostico() {
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public FichaDiagnostico(Long idFichaDiag, FichaOcupacional ficha, String codCie10, 
                           String descripcion, String tipoDiag, Integer orden, String estado, 
                           Date fechaCreacion, String usrCreacion, Date fechaActualizacion, 
                           String usrActualizacion) {
        this.idFichaDiag = idFichaDiag;
        this.ficha = ficha;
        this.codCie10 = codCie10;
        this.descripcion = descripcion;
        this.tipoDiag = tipoDiag;
        this.orden = orden;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.usrCreacion = usrCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.usrActualizacion = usrActualizacion;
    }

    // ==========================
    // Lifecycle
    // ==========================
    @PrePersist
    public void prePersist() {
        if (estado == null || estado.trim().isEmpty()) {
            estado = "A";
        }
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    // ==========================
    // Equals y HashCode
    // Basado en idFichaDiag (@EqualsAndHashCode.Include)
    // ==========================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FichaDiagnostico that = (FichaDiagnostico) o;
        return Objects.equals(idFichaDiag, that.idFichaDiag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFichaDiag);
    }

    // ==========================
    // ToString
    // Excluye 'ficha' (@ToString(exclude = "ficha"))
    // ==========================
    @Override
    public String toString() {
        return "FichaDiagnostico{" +
                "idFichaDiag=" + idFichaDiag +
                ", codCie10='" + codCie10 + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tipoDiag='" + tipoDiag + '\'' +
                ", orden=" + orden +
                ", estado='" + estado + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", usrCreacion='" + usrCreacion + '\'' +
                ", fechaActualizacion=" + fechaActualizacion +
                ", usrActualizacion='" + usrActualizacion + '\'' +
                '}';
    }

    // ==========================
    // Getters y Setters
    // ==========================
    public Long getIdFichaDiag() {
        return idFichaDiag;
    }

    public void setIdFichaDiag(Long idFichaDiag) {
        this.idFichaDiag = idFichaDiag;
    }

    public FichaOcupacional getFicha() {
        return ficha;
    }

    public void setFicha(FichaOcupacional ficha) {
        this.ficha = ficha;
    }

    public String getCodCie10() {
        return codCie10;
    }

    public void setCodCie10(String codCie10) {
        this.codCie10 = codCie10;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoDiag() {
        return tipoDiag;
    }

    public void setTipoDiag(String tipoDiag) {
        this.tipoDiag = tipoDiag;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getUsrCreacion() {
        return usrCreacion;
    }

    public void setUsrCreacion(String usrCreacion) {
        this.usrCreacion = usrCreacion;
    }

    public Date getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Date fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getUsrActualizacion() {
        return usrActualizacion;
    }

    public void setUsrActualizacion(String usrActualizacion) {
        this.usrActualizacion = usrActualizacion;
    }
}