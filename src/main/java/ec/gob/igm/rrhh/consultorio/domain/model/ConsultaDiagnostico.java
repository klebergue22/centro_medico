package ec.gob.igm.rrhh.consultorio.domain.model;



import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "CONSULTA_DIAGNOSTICO", schema = "CONSULTORIO",
        indexes = {
            @Index(name = "IX_CD_CONS_TIPO", columnList = "ID_CONSULTA, TIPO_DIAG"),
            @Index(name = "IX_CD_CIE", columnList = "COD_CIE10")
        }
// Nota: el UNIQUE (ID_CONSULTA, ES_PPAL) ya existe como índice único en BD (UX_CD_PPAL).
// JPA no “crea” eso si tienes schema-generation=none, así que está OK dejarlo solo en BD.
)
/**
 * Class ConsultaDiagnostico: representa una entidad del dominio de consultorio médico.
 */
public class ConsultaDiagnostico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
            name = "CONS_DIAG_SEQ_GEN",
            sequenceName = "CONSULTORIO.SQ_CONS_DIAG",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONS_DIAG_SEQ_GEN")
    @Column(name = "ID_CONS_DIAG", nullable = false, precision = 38, scale = 0)
    private Long idConsDiag;

    // FK_CD_CONS -> CONSULTA_MEDICA(ID_CONSULTA)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_CONSULTA",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_CD_CONS")
    )
    private ConsultaMedica consulta;

    // FK_CD_CIE10 -> CIE10(CODIGO)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "COD_CIE10",
            referencedColumnName = "CODIGO",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_CD_CIE10")
    )
    private Cie10 cie10;

    @Column(name = "TIPO_DIAG", length = 1, nullable = true)
    private String tipoDiag;

    @Column(name = "OBSERVACION", length = 1000)
    private String observacion;

    @Column(name = "ES_PPAL", insertable = false, updatable = false)
    private Long esPpal;

    @Column(name = "ESTADO", length = 20, nullable = false)
    private String estado;

    @Transient
    private String codigoUi;

    @Transient
    private String descripcionUi;

    public ConsultaDiagnostico() {
    }

    // Reemplaza @AllArgsConstructor
    public ConsultaDiagnostico(Long idConsDiag, ConsultaMedica consulta, Cie10 cie10, String tipoDiag, String observacion, Long esPpal, String estado) {
        this.idConsDiag = idConsDiag;
        this.consulta = consulta;
        this.cie10 = cie10;
        this.tipoDiag = tipoDiag;
        this.observacion = observacion;
        this.esPpal = esPpal;
        this.estado = estado;
    }

    // Lifecycle Callbacks
    @PrePersist
    public void prePersist() {
        if (estado == null || estado.trim().isEmpty()) {
            estado = "ACTIVO";
        }
        if (tipoDiag == null || tipoDiag.trim().isEmpty()) {
            tipoDiag = "P";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsultaDiagnostico that = (ConsultaDiagnostico) o;
        return Objects.equals(idConsDiag, that.idConsDiag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idConsDiag);
    }

    // Excluye consulta y cie10 (@ToString(exclude = ...))
    @Override
    public String toString() {
        return "ConsultaDiagnostico{" +
                "idConsDiag=" + idConsDiag +
                ", tipoDiag='" + tipoDiag + '\'' +
                ", observacion='" + observacion + '\'' +
                ", esPpal=" + esPpal +
                ", estado='" + estado + '\'' +
                '}';
    }

    public Long getIdConsDiag() {
        return idConsDiag;
    }

    public void setIdConsDiag(Long idConsDiag) {
        this.idConsDiag = idConsDiag;
    }

    public ConsultaMedica getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaMedica consulta) {
        this.consulta = consulta;
    }

    public Cie10 getCie10() {
        return cie10;
    }

    public void setCie10(Cie10 cie10) {
        this.cie10 = cie10;
        if (cie10 != null) {
            this.codigoUi = cie10.getCodigo();
            this.descripcionUi = cie10.getDescripcion();
        }
    }

    public String getTipoDiag() {
        return tipoDiag;
    }

    public void setTipoDiag(String tipoDiag) {
        this.tipoDiag = tipoDiag;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Long getEsPpal() {
        return esPpal;
    }

    public void setEsPpal(Long esPpal) {
        this.esPpal = esPpal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Compatibilidad con UI/Controller (CentroMedicoCtrl)

    public String getCodigo() {
        if (this.cie10 != null) {
            return this.cie10.getCodigo();
        }
        return codigoUi;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            this.cie10 = null;
            this.codigoUi = null;
            return;
        }

        this.codigoUi = codigo.trim().toUpperCase();
        if (this.cie10 != null && this.cie10.getCodigo() != null
                && !this.cie10.getCodigo().equalsIgnoreCase(this.codigoUi)) {
            this.cie10 = null;
            this.descripcionUi = null;
        }
    }

    public String getDescripcion() {
        if (this.cie10 != null) {
            return this.cie10.getDescripcion();
        }
        return descripcionUi;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            this.descripcionUi = null;
            if (this.cie10 == null) {
                return;
            }
            if (this.cie10.getDescripcion() == null || this.cie10.getDescripcion().trim().isEmpty()) {
                this.cie10 = null;
            }
            return;
        }

        this.descripcionUi = descripcion.trim();
        if (this.cie10 != null && this.cie10.getDescripcion() != null
                && !this.cie10.getDescripcion().equalsIgnoreCase(this.descripcionUi)) {
            this.cie10 = null;
        }
    }
}
