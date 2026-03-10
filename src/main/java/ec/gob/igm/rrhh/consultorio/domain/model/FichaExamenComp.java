package ec.gob.igm.rrhh.consultorio.domain.model;




import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "FICHA_EXAMEN_COMP", schema = "CONSULTORIO")
public class FichaExamenComp implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
            name = "SQ_FICHA_EXAMEN_COMP_GEN",
            sequenceName = "CONSULTORIO.SQ_FICHA_EXAMEN_COMP",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_FICHA_EXAMEN_COMP_GEN")
    @Column(name = "ID_FICHA_EXAMEN", nullable = false)
    private Long idFichaExamen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    @Column(name = "NRO_FILA")
    private Integer nroFila;

    @Column(name = "NOMBRE_EXAMEN", length = 200)
    private String nombreExamen;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EXAMEN")
    private Date fechaExamen;

    @Column(name = "RESULTADO", length = 2000)
    private String resultado;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_CREACION")
    private Date fCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_ACTUALIZACION")
    private Date fActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;


    // Constructor vacío (requerido por JPA)
    public FichaExamenComp() {
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public FichaExamenComp(Long idFichaExamen, FichaOcupacional ficha, Integer nroFila,
                           String nombreExamen, Date fechaExamen, String resultado,
                           Date fCreacion, String usrCreacion, Date fActualizacion,
                           String usrActualizacion) {
        this.idFichaExamen = idFichaExamen;
        this.ficha = ficha;
        this.nroFila = nroFila;
        this.nombreExamen = nombreExamen;
        this.fechaExamen = fechaExamen;
        this.resultado = resultado;
        this.fCreacion = fCreacion;
        this.usrCreacion = usrCreacion;
        this.fActualizacion = fActualizacion;
        this.usrActualizacion = usrActualizacion;
    }

    // Lifecycle
    @PrePersist
    public void prePersist() {
        if (fCreacion == null) fCreacion = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        fActualizacion = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FichaExamenComp that = (FichaExamenComp) o;
        return Objects.equals(idFichaExamen, that.idFichaExamen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFichaExamen);
    }

    // Excluye 'ficha' (@ToString(exclude = ...))
    @Override
    public String toString() {
        return "FichaExamenComp{" +
                "idFichaExamen=" + idFichaExamen +
                ", nroFila=" + nroFila +
                ", nombreExamen='" + nombreExamen + '\'' +
                ", fechaExamen=" + fechaExamen +
                ", resultado='" + resultado + '\'' +
                ", fCreacion=" + fCreacion +
                ", usrCreacion='" + usrCreacion + '\'' +
                ", fActualizacion=" + fActualizacion +
                ", usrActualizacion='" + usrActualizacion + '\'' +
                '}';
    }

    public Long getIdFichaExamen() {
        return idFichaExamen;
    }

    public void setIdFichaExamen(Long idFichaExamen) {
        this.idFichaExamen = idFichaExamen;
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

    public String getNombreExamen() {
        return nombreExamen;
    }

    public void setNombreExamen(String nombreExamen) {
        this.nombreExamen = nombreExamen;
    }

    public Date getFechaExamen() {
        return fechaExamen;
    }

    public void setFechaExamen(Date fechaExamen) {
        this.fechaExamen = fechaExamen;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public Date getfCreacion() {
        return fCreacion;
    }

    public void setfCreacion(Date fCreacion) {
        this.fCreacion = fCreacion;
    }

    public String getUsrCreacion() {
        return usrCreacion;
    }

    public void setUsrCreacion(String usrCreacion) {
        this.usrCreacion = usrCreacion;
    }

    public Date getfActualizacion() {
        return fActualizacion;
    }

    public void setfActualizacion(Date fActualizacion) {
        this.fActualizacion = fActualizacion;
    }

    public String getUsrActualizacion() {
        return usrActualizacion;
    }

    public void setUsrActualizacion(String usrActualizacion) {
        this.usrActualizacion = usrActualizacion;
    }
}

