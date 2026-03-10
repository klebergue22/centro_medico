package ec.gob.igm.rrhh.consultorio.domain.model;





import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "FICHA_RIESGO", schema = "CONSULTORIO")
public class FichaRiesgo implements Serializable {

    private static final long serialVersionUID = 1L;

    // PK – generado por TRIGGER en Oracle
       @Id
    @SequenceGenerator(
            name = "FICHA_RIESGO_GEN",
            sequenceName = "CONSULTORIO.SQ_FICHA_RIESGO",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FICHA_RIESGO_GEN")
    @Column(name = "ID_FICHA_RIESGO", nullable = false)

    private Long idFichaRiesgo;

    // Relación 1–1 con FICHA_OCUPACIONAL
    // (UX_FR_FICHA lo garantiza)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_FICHA",
            referencedColumnName = "ID_FICHA",
            nullable = false
    )
    private FichaOcupacional ficha;

    // Datos del puesto / actividades
    @Column(name = "PUESTO_TRABAJO", length = 200)
    private String puestoTrabajo;

    @Column(name = "ACTIVIDAD_1", length = 500)
    private String actividad1;

    @Column(name = "ACTIVIDAD_2", length = 500)
    private String actividad2;

    @Column(name = "ACTIVIDAD_3", length = 500)
    private String actividad3;

    @Column(name = "ACTIVIDAD_4", length = 500)
    private String actividad4;

    @Column(name = "ACTIVIDAD_5", length = 500)
    private String actividad5;

    @Column(name = "ACTIVIDAD_6", length = 500)
    private String actividad6;

    @Column(name = "ACTIVIDAD_7", length = 500)
    private String actividad7;

    // Riesgos por categoría
    @Column(name = "RIESGOS_FISICOS", length = 2000)
    private String riesgosFisicos;

    @Column(name = "RIESGOS_SEGURIDAD", length = 2000)
    private String riesgosSeguridad;

    @Column(name = "RIESGOS_QUIMICOS", length = 2000)
    private String riesgosQuimicos;

    @Column(name = "RIESGOS_BIOLOGICOS", length = 2000)
    private String riesgosBiologicos;

    @Column(name = "RIESGOS_ERGONOMICOS", length = 2000)
    private String riesgosErgonomicos;

    @Column(name = "RIESGOS_PSICOSOCIALES", length = 2000)
    private String riesgosPsicosociales;

    @Column(name = "OBSERVACIONES", length = 2000)
    private String observaciones;

    @Column(name = "MEDIDAS_PREVENTIVAS", length = 2000)
    private String medidasPreventivas;

    @Column(name = "ESTADO", length = 20)
    private String estado;

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


    // Constructor vacío (requerido por JPA)
    public FichaRiesgo() {
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public FichaRiesgo(Long idFichaRiesgo, FichaOcupacional ficha, String puestoTrabajo,
                       String actividad1, String actividad2, String actividad3,
                       String actividad4, String actividad5, String actividad6, String actividad7,
                       String riesgosFisicos, String riesgosSeguridad, String riesgosQuimicos,
                       String riesgosBiologicos, String riesgosErgonomicos, String riesgosPsicosociales,
                       String observaciones, String medidasPreventivas, String estado,
                       Date fCreacion, String usrCreacion, Date fActualizacion, String usrActualizacion) {
        this.idFichaRiesgo = idFichaRiesgo;
        this.ficha = ficha;
        this.puestoTrabajo = puestoTrabajo;
        this.actividad1 = actividad1;
        this.actividad2 = actividad2;
        this.actividad3 = actividad3;
        this.actividad4 = actividad4;
        this.actividad5 = actividad5;
        this.actividad6 = actividad6;
        this.actividad7 = actividad7;
        this.riesgosFisicos = riesgosFisicos;
        this.riesgosSeguridad = riesgosSeguridad;
        this.riesgosQuimicos = riesgosQuimicos;
        this.riesgosBiologicos = riesgosBiologicos;
        this.riesgosErgonomicos = riesgosErgonomicos;
        this.riesgosPsicosociales = riesgosPsicosociales;
        this.observaciones = observaciones;
        this.medidasPreventivas = medidasPreventivas;
        this.estado = estado;
        this.fCreacion = fCreacion;
        this.usrCreacion = usrCreacion;
        this.fActualizacion = fActualizacion;
        this.usrActualizacion = usrActualizacion;
    }

    // Lifecycle
    @PrePersist
    public void prePersist() {
        if (estado == null || estado.trim().isEmpty()) {
            estado = "ACTIVO";
        }
        if (fCreacion == null) {
            fCreacion = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fActualizacion = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FichaRiesgo that = (FichaRiesgo) o;
        return Objects.equals(idFichaRiesgo, that.idFichaRiesgo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFichaRiesgo);
    }

    // Excluye 'ficha' (@ToString(exclude = ...))
    @Override
    public String toString() {
        return "FichaRiesgo{" +
                "idFichaRiesgo=" + idFichaRiesgo +
                ", puestoTrabajo='" + puestoTrabajo + '\'' +
                ", actividad1='" + actividad1 + '\'' +
                ", actividad2='" + actividad2 + '\'' +
                ", actividad3='" + actividad3 + '\'' +
                ", actividad4='" + actividad4 + '\'' +
                ", actividad5='" + actividad5 + '\'' +
                ", actividad6='" + actividad6 + '\'' +
                ", actividad7='" + actividad7 + '\'' +
                ", riesgosFisicos='" + riesgosFisicos + '\'' +
                ", riesgosSeguridad='" + riesgosSeguridad + '\'' +
                ", riesgosQuimicos='" + riesgosQuimicos + '\'' +
                ", riesgosBiologicos='" + riesgosBiologicos + '\'' +
                ", riesgosErgonomicos='" + riesgosErgonomicos + '\'' +
                ", riesgosPsicosociales='" + riesgosPsicosociales + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", medidasPreventivas='" + medidasPreventivas + '\'' +
                ", estado='" + estado + '\'' +
                ", fCreacion=" + fCreacion +
                ", usrCreacion='" + usrCreacion + '\'' +
                ", fActualizacion=" + fActualizacion +
                ", usrActualizacion='" + usrActualizacion + '\'' +
                '}';
    }

    public Long getIdFichaRiesgo() {
        return idFichaRiesgo;
    }

    public void setIdFichaRiesgo(Long idFichaRiesgo) {
        this.idFichaRiesgo = idFichaRiesgo;
    }

    public FichaOcupacional getFicha() {
        return ficha;
    }

    public void setFicha(FichaOcupacional ficha) {
        this.ficha = ficha;
    }

    public String getPuestoTrabajo() {
        return puestoTrabajo;
    }

    public void setPuestoTrabajo(String puestoTrabajo) {
        this.puestoTrabajo = puestoTrabajo;
    }

    public String getActividad1() {
        return actividad1;
    }

    public void setActividad1(String actividad1) {
        this.actividad1 = actividad1;
    }

    public String getActividad2() {
        return actividad2;
    }

    public void setActividad2(String actividad2) {
        this.actividad2 = actividad2;
    }

    public String getActividad3() {
        return actividad3;
    }

    public void setActividad3(String actividad3) {
        this.actividad3 = actividad3;
    }

    public String getActividad4() {
        return actividad4;
    }

    public void setActividad4(String actividad4) {
        this.actividad4 = actividad4;
    }

    public String getActividad5() {
        return actividad5;
    }

    public void setActividad5(String actividad5) {
        this.actividad5 = actividad5;
    }

    public String getActividad6() {
        return actividad6;
    }

    public void setActividad6(String actividad6) {
        this.actividad6 = actividad6;
    }

    public String getActividad7() {
        return actividad7;
    }

    public void setActividad7(String actividad7) {
        this.actividad7 = actividad7;
    }

    public String getRiesgosFisicos() {
        return riesgosFisicos;
    }

    public void setRiesgosFisicos(String riesgosFisicos) {
        this.riesgosFisicos = riesgosFisicos;
    }

    public String getRiesgosSeguridad() {
        return riesgosSeguridad;
    }

    public void setRiesgosSeguridad(String riesgosSeguridad) {
        this.riesgosSeguridad = riesgosSeguridad;
    }

    public String getRiesgosQuimicos() {
        return riesgosQuimicos;
    }

    public void setRiesgosQuimicos(String riesgosQuimicos) {
        this.riesgosQuimicos = riesgosQuimicos;
    }

    public String getRiesgosBiologicos() {
        return riesgosBiologicos;
    }

    public void setRiesgosBiologicos(String riesgosBiologicos) {
        this.riesgosBiologicos = riesgosBiologicos;
    }

    public String getRiesgosErgonomicos() {
        return riesgosErgonomicos;
    }

    public void setRiesgosErgonomicos(String riesgosErgonomicos) {
        this.riesgosErgonomicos = riesgosErgonomicos;
    }

    public String getRiesgosPsicosociales() {
        return riesgosPsicosociales;
    }

    public void setRiesgosPsicosociales(String riesgosPsicosociales) {
        this.riesgosPsicosociales = riesgosPsicosociales;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getMedidasPreventivas() {
        return medidasPreventivas;
    }

    public void setMedidasPreventivas(String medidasPreventivas) {
        this.medidasPreventivas = medidasPreventivas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
