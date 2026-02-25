/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.domain.model;

/**
 *
 * @author GUERRA_KLEBER
 */


 

import ec.gob.igm.rrhh.consultorio.domain.enums.EstadoContrato;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "T_CONTRATACIONES", schema = "RH")
public class Contratacion implements Serializable {

    @EmbeddedId
    private ContratacionId id;

    @MapsId("noPersona")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NO_PERSONA", referencedColumnName = "NO_PERSONA", nullable = false)
    private DatEmpleado empleado;

    @Column(name = "ESTADO", length = 1)
    private EstadoContrato estado;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_CONTRATO")
    private Date fContrato;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_SALIDA")
    private Date fSalida;

    // ====== GETTERS/SETTERS MANUALES (sin Lombok) ======

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
}
