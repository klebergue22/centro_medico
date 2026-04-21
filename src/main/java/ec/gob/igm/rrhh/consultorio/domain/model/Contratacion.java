package ec.gob.igm.rrhh.consultorio.domain.model;

import ec.gob.igm.rrhh.consultorio.domain.enums.EstadoContrato;
import ec.gob.igm.rrhh.consultorio.domain.enums.TipoContrato;
import ec.gob.igm.rrhh.consultorio.persistence.converter.EstadoContratoConverter;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "T_CONTRATACIONES", schema = "RH")
/**
 * Class Contratacion: representa una entidad del dominio de consultorio médico.
 */
public class Contratacion implements Serializable {

    @EmbeddedId
    private ContratacionId id;

    @MapsId("noPersona")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NO_PERSONA", referencedColumnName = "NO_PERSONA", nullable = false)
    private DatEmpleado empleado;

    @Column(name = "ESTADO", length = 1)
    @Convert(converter = EstadoContratoConverter.class)
    private EstadoContrato estado;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_CONTRATO")
    private Date fContrato;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_SALIDA")
    private Date fSalida;

    @Transient
    private TipoContrato tipoContrato;

    @Transient
    private String noDoc;

    @Transient
    private String responsable;

    @Transient
    private String nivel;

    @Transient
    private String categoria;

    @Transient
    private String obs;

    public ContratacionId getId() {
        return id;
    }

    public void setId(ContratacionId id) {
        this.id = id;
    }

    public DatEmpleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(DatEmpleado empleado) {
        this.empleado = empleado;
    }

    public EstadoContrato getEstado() {
        return estado;
    }

    public void setEstado(EstadoContrato estado) {
        this.estado = estado;
    }

    public Date getFContrato() {
        return fContrato;
    }

    public void setFContrato(Date fContrato) {
        this.fContrato = fContrato;
    }

    public Date getFSalida() {
        return fSalida;
    }

    public void setFSalida(Date fSalida) {
        this.fSalida = fSalida;
    }

    public Date getFeContrato() {
        return fContrato;
    }

    public void setFeContrato(Date feContrato) {
        this.fContrato = feContrato;
    }

    public Date getFeSalida() {
        return fSalida;
    }

    public void setFeSalida(Date feSalida) {
        this.fSalida = feSalida;
    }

    public TipoContrato getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(TipoContrato tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public String getNoDoc() {
        return noDoc;
    }

    public void setNoDoc(String noDoc) {
        this.noDoc = noDoc;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }
}
