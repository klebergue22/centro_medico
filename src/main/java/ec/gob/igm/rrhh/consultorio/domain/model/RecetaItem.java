package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Detalle de los medicamentos prescritos en una receta.
 */
@Entity
@Table(name = "RECETA_ITEM", schema = "CONSULTORIO",
        indexes = {
                @Index(name = "IX_RI_REC", columnList = "ID_RECETA")
        })
public class RecetaItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
            name = "RECETA_ITEM_SEQ_GEN",
            sequenceName = "CONSULTORIO.SQ_RECETA_ITEM",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RECETA_ITEM_SEQ_GEN")
    @Column(name = "ID_ITEM", nullable = false)
    private Long idItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_RECETA",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_RI_REC")
    )
    private RecetaMedica receta;

    @Column(name = "MEDICAMENTO", nullable = false, length = 200)
    private String medicamento;

    @Column(name = "DOSIS", length = 100)
    private String dosis;

    @Column(name = "FRECUENCIA", length = 100)
    private String frecuencia;

    @Column(name = "VIA", length = 50)
    private String via;

    @Column(name = "DURACION_DIAS", precision = 4, scale = 0)
    private Integer duracionDias;

    @Column(name = "INDICACIONES", length = 1000)
    private String indicaciones;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    public RecetaItem() {
    }

    public RecetaItem(Long idItem, RecetaMedica receta, String medicamento, String dosis,
                      String frecuencia, String via, Integer duracionDias,
                      String indicaciones, String estado) {
        this.idItem = idItem;
        this.receta = receta;
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.via = via;
        this.duracionDias = duracionDias;
        this.indicaciones = indicaciones;
        this.estado = estado;
    }

    @PrePersist
    public void prePersist() {
        if (this.estado == null || this.estado.trim().isEmpty()) {
            this.estado = "ACTIVO";
        }
    }

    public Long getIdItem() {
        return idItem;
    }

    public void setIdItem(Long idItem) {
        this.idItem = idItem;
    }

    public RecetaMedica getReceta() {
        return receta;
    }

    public void setReceta(RecetaMedica receta) {
        this.receta = receta;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
