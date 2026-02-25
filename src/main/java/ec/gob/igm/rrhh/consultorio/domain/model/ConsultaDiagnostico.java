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

    /**
     * 'P' = Principal, 'S' = Secundario En BD tiene DEFAULT 'P' y CHECK
     * (TIPO_DIAG IN ('P','S')). Recomendación: setear siempre en Java para no
     * depender del DEFAULT si JPA manda null.
     */
    @Column(name = "TIPO_DIAG", length = 1, nullable = true)
    private String tipoDiag;

    @Column(name = "OBSERVACION", length = 1000)
    private String observacion;

    /**
     * Columna virtual en Oracle: ES_PPAL GENERATED ALWAYS AS (CASE WHEN
     * TIPO_DIAG='P' THEN 1 ELSE NULL END) VIRTUAL
     */
    @Column(name = "ES_PPAL", insertable = false, updatable = false)
    private Long esPpal;

    @Column(name = "ESTADO", length = 20, nullable = false)
    private String estado;

    // ==========================
    // Constructores
    // ==========================
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

    // ==========================
    // Lifecycle Callbacks
    // ==========================
    @PrePersist
    public void prePersist() {
        if (estado == null || estado.trim().isEmpty()) {
            estado = "ACTIVO";
        }
        if (tipoDiag == null || tipoDiag.trim().isEmpty()) {
            tipoDiag = "P";
        }
    }

    // ==========================
    // Equals y HashCode
    // Basado en idConsDiag (@EqualsAndHashCode(of = "idConsDiag"))
    // ==========================
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

    // ==========================
    // ToString
    // Excluye consulta y cie10 (@ToString(exclude = ...))
    // ==========================
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

    // ==========================
    // Getters y Setters
    // ==========================
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

    // =========================================================
    // Compatibilidad con UI/Controller (CentroMedicoCtrl)
    // =========================================================

    public String getCodigo() {
        // si está asociado a CIE10 real, úsalo
        if (this.cie10 != null) {
            return this.cie10.getCodigo();
        }
        return null;
    }

    public void setCodigo(String codigo) {
        // para filas vacías de UI se permite null
        if (codigo == null || codigo.trim().isEmpty()) {
            // NO crees un Cie10 nuevo aquí (evita TransientPropertyValueException)
            // solo “desengancha” si venía algo
            this.cie10 = null;
            return;
        }

        // Si aquí tú ya manejas la selección de CIE10 en otro lado, este setter
        // no debe inventarse un CIE10 nuevo.
        // Deja que el controller/service asigne el cie10 real.
        // Opcional: si ya hay un cie10 asignado, no lo cambies aquí.
    }

    public String getDescripcion() {
        if (this.cie10 != null) {
            return this.cie10.getDescripcion();
        }
        return null;
    }

    public void setDescripcion(String descripcion) {
        // Solo por compatibilidad: en tu caso descripción viene de CIE10.
        // Para filas vacías, no hace nada.
    }
}