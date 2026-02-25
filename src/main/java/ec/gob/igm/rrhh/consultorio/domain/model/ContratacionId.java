/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.domain.model;

/**
 *
 * @author GUERRA_KLEBER
 */


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ContratacionId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "NO_PERSONA", nullable = false, precision = 10, scale = 0)
    private Long noPersona;

    @Column(name = "NO_CONT", nullable = false, precision = 10, scale = 0)
    private Long noCont;

    public ContratacionId() {
    }

    public ContratacionId(Long noPersona, Long noCont) {
        this.noPersona = noPersona;
        this.noCont = noCont;
    }

    public Long getNoPersona() {
        return noPersona;
    }

    public void setNoPersona(Long noPersona) {
        this.noPersona = noPersona;
    }

    public Long getNoCont() {
        return noCont;
    }

    public void setNoCont(Long noCont) {
        this.noCont = noCont;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContratacionId)) return false;
        ContratacionId that = (ContratacionId) o;
        return Objects.equals(noPersona, that.noPersona) &&
               Objects.equals(noCont, that.noCont);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noPersona, noCont);
    }
}
