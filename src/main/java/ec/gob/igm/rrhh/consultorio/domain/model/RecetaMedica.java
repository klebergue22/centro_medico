package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encabezado de la receta médica emitida en una consulta.
 */
@Entity
@Table(name = "RECETA_MEDICA", schema = "CONSULTORIO",
        indexes = {
                @Index(name = "IX_REC_CONS", columnList = "ID_CONSULTA")
        })
public class RecetaMedica implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
            name = "RECETA_MEDICA_SEQ_GEN",
            sequenceName = "CONSULTORIO.SQ_RECETA_MEDICA",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RECETA_MEDICA_SEQ_GEN")
    @Column(name = "ID_RECETA", nullable = false)
    private Long idReceta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_CONSULTA",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_REC_CONS")
    )
    private ConsultaMedica consulta;

    @Column(name = "INDICACIONES", length = 2000)
    private String indicaciones;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EMISION")
    private Date fechaEmision;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecetaItem> items;

    public RecetaMedica() {
        this.items = new ArrayList<>();
    }

    public RecetaMedica(Long idReceta, ConsultaMedica consulta, String indicaciones,
                        Date fechaEmision, String estado, List<RecetaItem> items) {
        this.idReceta = idReceta;
        this.consulta = consulta;
        this.indicaciones = indicaciones;
        this.fechaEmision = fechaEmision;
        this.estado = estado;
        this.items = items == null ? new ArrayList<>() : items;
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaEmision == null) {
            this.fechaEmision = new Date();
        }
        if (this.estado == null || this.estado.trim().isEmpty()) {
            this.estado = "ACTIVA";
        }
    }

    public Long getIdReceta() {
        return idReceta;
    }

    public void setIdReceta(Long idReceta) {
        this.idReceta = idReceta;
    }

    public ConsultaMedica getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaMedica consulta) {
        this.consulta = consulta;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<RecetaItem> getItems() {
        return items;
    }

    public void setItems(List<RecetaItem> items) {
        this.items = items;
    }
}
