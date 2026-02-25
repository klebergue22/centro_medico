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
    name = "FICHA_RIESGO_DET",
    schema = "CONSULTORIO",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_FRD_FICHA_GRP_ITEM_ACT",
            columnNames = {"ID_FICHA", "GRUPO", "ITEM", "ACTIVIDAD_NRO"}
        )
    }
)
@Access(AccessType.FIELD)
public class FichaRiesgoDet implements Serializable {

    private static final long serialVersionUID = 1L;

    // =========================
    // PK
    // =========================
    @Id
    @SequenceGenerator(
        name = "SQ_FICHA_RIESGO_DET_GEN",
        sequenceName = "CONSULTORIO.SQ_FICHA_RIESGO_DET",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_FICHA_RIESGO_DET_GEN")
    @Column(name = "ID_FICHA_RIESGO_DET", nullable = false)
    private Long idFichaRiesgoDet;

    // =========================
    // Relaciones
    // =========================

    /** Ficha ocupacional (padre) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    /** Encabezado de riesgos (opcional pero recomendado) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FICHA_RIESGO")
    private FichaRiesgo fichaRiesgo;

    // =========================
    // Datos del detalle
    // =========================
    @Column(name = "GRUPO", length = 30, nullable = false)
    private String grupo; 

    @Column(name = "ITEM", length = 300, nullable = false)
    private String item;

    @Column(name = "ACTIVIDAD_NRO", nullable = false)
    private Integer actividadNro; 

    @Column(name = "MARCADO", length = 1, nullable = false)
    private String marcado; 

    @Column(name = "ORDEN")
    private Integer orden;

    // =========================
    // Auditoría
    // =========================
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

    // ==========================
    // Constructores
    // ==========================

    // Constructor vacío (requerido por JPA)
    public FichaRiesgoDet() {
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public FichaRiesgoDet(Long idFichaRiesgoDet, FichaOcupacional ficha, FichaRiesgo fichaRiesgo, 
                           String grupo, String item, Integer actividadNro, String marcado, Integer orden, 
                           Date fCreacion, String usrCreacion, Date fActualizacion, String usrActualizacion) {
        this.idFichaRiesgoDet = idFichaRiesgoDet;
        this.ficha = ficha;
        this.fichaRiesgo = fichaRiesgo;
        this.grupo = grupo;
        this.item = item;
        this.actividadNro = actividadNro;
        this.marcado = marcado;
        this.orden = orden;
        this.fCreacion = fCreacion;
        this.usrCreacion = usrCreacion;
        this.fActualizacion = fActualizacion;
        this.usrActualizacion = usrActualizacion;
    }

    // =========================
    // Ciclo de vida JPA
    // =========================
    @PrePersist
    public void prePersist() {
        if (marcado == null) marcado = "N";
        if (fCreacion == null) fCreacion = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        fActualizacion = new Date();
    }

    // ==========================
    // Equals y HashCode
    // Basado en idFichaRiesgoDet (@EqualsAndHashCode.Include)
    // ==========================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FichaRiesgoDet that = (FichaRiesgoDet) o;
        return Objects.equals(idFichaRiesgoDet, that.idFichaRiesgoDet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFichaRiesgoDet);
    }

    // ==========================
    // ToString
    // Excluye 'ficha' y 'fichaRiesgo' (@ToString(exclude = ...))
    // ==========================
    @Override
    public String toString() {
        return "FichaRiesgoDet{" +
                "idFichaRiesgoDet=" + idFichaRiesgoDet +
                ", grupo='" + grupo + '\'' +
                ", item='" + item + '\'' +
                ", actividadNro=" + actividadNro +
                ", marcado='" + marcado + '\'' +
                ", orden=" + orden +
                ", fCreacion=" + fCreacion +
                ", usrCreacion='" + usrCreacion + '\'' +
                ", fActualizacion=" + fActualizacion +
                ", usrActualizacion='" + usrActualizacion + '\'' +
                '}';
    }

    // =========================
    // Helpers para JSF
    // =========================
    public boolean isMarcadoBool() {
        return "S".equalsIgnoreCase(marcado);
    }

    public void setMarcadoBool(boolean value) {
        this.marcado = value ? "S" : "N";
    }

    // ==========================
    // Getters y Setters
    // ==========================
    public Long getIdFichaRiesgoDet() {
        return idFichaRiesgoDet;
    }

    public void setIdFichaRiesgoDet(Long idFichaRiesgoDet) {
        this.idFichaRiesgoDet = idFichaRiesgoDet;
    }

    public FichaOcupacional getFicha() {
        return ficha;
    }

    public void setFicha(FichaOcupacional ficha) {
        this.ficha = ficha;
    }

    public FichaRiesgo getFichaRiesgo() {
        return fichaRiesgo;
    }

    public void setFichaRiesgo(FichaRiesgo fichaRiesgo) {
        this.fichaRiesgo = fichaRiesgo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Integer getActividadNro() {
        return actividadNro;
    }

    public void setActividadNro(Integer actividadNro) {
        this.actividadNro = actividadNro;
    }

    public String getMarcado() {
        return marcado;
    }

    public void setMarcado(String marcado) {
        this.marcado = marcado;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Date getFCreacion() {
        return fCreacion;
    }

    public void setFCreacion(Date fCreacion) {
        this.fCreacion = fCreacion;
    }

    public String getUsrCreacion() {
        return usrCreacion;
    }

    public void setUsrCreacion(String usrCreacion) {
        this.usrCreacion = usrCreacion;
    }

    public Date getFActualizacion() {
        return fActualizacion;
    }

    public void setFActualizacion(Date fActualizacion) {
        this.fActualizacion = fActualizacion;
    }

    public String getUsrActualizacion() {
        return usrActualizacion;
    }

    public void setUsrActualizacion(String usrActualizacion) {
        this.usrActualizacion = usrActualizacion;
    }
}
