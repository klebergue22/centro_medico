package ec.gob.igm.rrhh.consultorio.domain.model;





import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "FICHA_ACT_LABORAL", schema = "CONSULTORIO")
@Access(AccessType.FIELD)
/**
 * Class FichaActLaboral: representa una entidad del dominio de consultorio médico.
 */
public class FichaActLaboral implements Serializable {

    private static final long serialVersionUID = 1L;

    // PK – generado por SEQUENCE (correcto)
    @Id
    @SequenceGenerator(
            name = "SQ_FICHA_ACT_LAB_GEN",
            sequenceName = "CONSULTORIO.SQ_FICHA_ACT_LAB",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_FICHA_ACT_LAB_GEN")
    @Column(name = "ID_FICHA_ACT_LAB", nullable = false)
    private Long idFichaActLab;

    // Relación con FICHA_OCUPACIONAL
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    // Datos de la actividad laboral
    @Column(name = "NRO_FILA", nullable = false)
    private Integer nroFila;

    @Column(name = "CENTRO_TRABAJO", length = 250)
    private String centroTrabajo;

    @Column(name = "ACTIVIDAD", length = 500)
    private String actividad;

    @Column(name = "ES_ANTERIOR", length = 1, nullable = false)
    private String esAnterior; // S/N

    @Column(name = "ES_ACTUAL", length = 1, nullable = false)
    private String esActual; // S/N

    @Column(name = "TIEMPO", length = 100)
    private String tiempo;

    @Column(name = "INCIDENTE", length = 1, nullable = false)
    private String incidente; // S/N

    @Column(name = "ACCIDENTE", length = 1, nullable = false)
    private String accidente; // S/N

    @Column(name = "ENF_OCUPACIONAL", length = 1, nullable = false)
    private String enfOcupacional; // S/N

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_EVENTO")
    private Date fechaEvento;

    @Column(name = "ESPECIFICAR", length = 300)
    private String especificar;

    @Column(name = "OBSERVACIONES", length = 2000)
    private String observaciones;

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
    public FichaActLaboral() {
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public FichaActLaboral(Long idFichaActLab, FichaOcupacional ficha, Integer nroFila,
                           String centroTrabajo, String actividad, String esAnterior,
                           String esActual, String tiempo, String incidente,
                           String accidente, String enfOcupacional, Date fechaEvento,
                           String especificar, String observaciones, Date fCreacion,
                           String usrCreacion, Date fActualizacion, String usrActualizacion) {
        this.idFichaActLab = idFichaActLab;
        this.ficha = ficha;
        this.nroFila = nroFila;
        this.centroTrabajo = centroTrabajo;
        this.actividad = actividad;
        this.esAnterior = esAnterior;
        this.esActual = esActual;
        this.tiempo = tiempo;
        this.incidente = incidente;
        this.accidente = accidente;
        this.enfOcupacional = enfOcupacional;
        this.fechaEvento = fechaEvento;
        this.especificar = especificar;
        this.observaciones = observaciones;
        this.fCreacion = fCreacion;
        this.usrCreacion = usrCreacion;
        this.fActualizacion = fActualizacion;
        this.usrActualizacion = usrActualizacion;
    }

    // Lifecycle
    @PrePersist
    public void prePersist() {
        if (fCreacion == null) fCreacion = new Date();
        if (esAnterior == null) esAnterior = "N";
        if (esActual == null) esActual = "N";
        if (incidente == null) incidente = "N";
        if (accidente == null) accidente = "N";
        if (enfOcupacional == null) enfOcupacional = "N";
    }

    @PreUpdate
    public void preUpdate() {
        fActualizacion = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FichaActLaboral that = (FichaActLaboral) o;
        return Objects.equals(idFichaActLab, that.idFichaActLab);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFichaActLab);
    }

    // Excluye 'ficha' (@ToString(exclude = ...))
    @Override
    public String toString() {
        return "FichaActLaboral{" +
                "idFichaActLab=" + idFichaActLab +
                ", nroFila=" + nroFila +
                ", centroTrabajo='" + centroTrabajo + '\'' +
                ", actividad='" + actividad + '\'' +
                ", esAnterior='" + esAnterior + '\'' +
                ", esActual='" + esActual + '\'' +
                ", tiempo='" + tiempo + '\'' +
                ", incidente='" + incidente + '\'' +
                ", accidente='" + accidente + '\'' +
                ", enfOcupacional='" + enfOcupacional + '\'' +
                ", fechaEvento=" + fechaEvento +
                ", especificar='" + especificar + '\'' +
                ", observaciones='" + observaciones + '\'' +
                ", fCreacion=" + fCreacion +
                ", usrCreacion='" + usrCreacion + '\'' +
                ", fActualizacion=" + fActualizacion +
                ", usrActualizacion='" + usrActualizacion + '\'' +
                '}';
    }

    public boolean isAnteriorBool() { return "S".equalsIgnoreCase(esAnterior); }
    public void setAnteriorBool(boolean v) { this.esAnterior = v ? "S" : "N"; }

    public boolean isActualBool() { return "S".equalsIgnoreCase(esActual); }
    public void setActualBool(boolean v) { this.esActual = v ? "S" : "N"; }

    public boolean isIncidenteBool() { return "S".equalsIgnoreCase(incidente); }
    public void setIncidenteBool(boolean v) { this.incidente = v ? "S" : "N"; }

    public boolean isAccidenteBool() { return "S".equalsIgnoreCase(accidente); }
    public void setAccidenteBool(boolean v) { this.accidente = v ? "S" : "N"; }

    public boolean isEnfOcupacionalBool() { return "S".equalsIgnoreCase(enfOcupacional); }
    public void setEnfOcupacionalBool(boolean v) { this.enfOcupacional = v ? "S" : "N"; }

    public Long getIdFichaActLab() {
        return idFichaActLab;
    }

    public void setIdFichaActLab(Long idFichaActLab) {
        this.idFichaActLab = idFichaActLab;
    }

    public FichaOcupacional getFicha() {
        return ficha;
    }

    public void setFicha(FichaOcupacional ficha) {
        this.ficha = ficha;
    }

    public Integer getNroFila() {
        return nroFila;
    }

    public void setNroFila(Integer nroFila) {
        this.nroFila = nroFila;
    }

    public String getCentroTrabajo() {
        return centroTrabajo;
    }

    public void setCentroTrabajo(String centroTrabajo) {
        this.centroTrabajo = centroTrabajo;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getEsAnterior() {
        return esAnterior;
    }

    public void setEsAnterior(String esAnterior) {
        this.esAnterior = esAnterior;
    }

    public String getEsActual() {
        return esActual;
    }

    public void setEsActual(String esActual) {
        this.esActual = esActual;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public String getIncidente() {
        return incidente;
    }

    public void setIncidente(String incidente) {
        this.incidente = incidente;
    }

    public String getAccidente() {
        return accidente;
    }

    public void setAccidente(String accidente) {
        this.accidente = accidente;
    }

    public String getEnfOcupacional() {
        return enfOcupacional;
    }

    public void setEnfOcupacional(String enfOcupacional) {
        this.enfOcupacional = enfOcupacional;
    }

    public Date getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(Date fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public String getEspecificar() {
        return especificar;
    }

    public void setEspecificar(String especificar) {
        this.especificar = especificar;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
