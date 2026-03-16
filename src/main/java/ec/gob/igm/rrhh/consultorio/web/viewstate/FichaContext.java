package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;

public class FichaContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer noPersonaSel;
    private DatEmpleado empleadoSel;
    private PersonaAux personaAux;
    private FichaOcupacional ficha;
    private SignosVitales signos;
    private ConsultaMedica consulta;

    public Integer getNoPersonaSel() { return noPersonaSel; }
    public void setNoPersonaSel(Integer noPersonaSel) { this.noPersonaSel = noPersonaSel; }
    public DatEmpleado getEmpleadoSel() { return empleadoSel; }
    public void setEmpleadoSel(DatEmpleado empleadoSel) { this.empleadoSel = empleadoSel; }
    public PersonaAux getPersonaAux() { return personaAux; }
    public void setPersonaAux(PersonaAux personaAux) { this.personaAux = personaAux; }
    public FichaOcupacional getFicha() { return ficha; }
    public void setFicha(FichaOcupacional ficha) { this.ficha = ficha; }
    public SignosVitales getSignos() { return signos; }
    public void setSignos(SignosVitales signos) { this.signos = signos; }
    public ConsultaMedica getConsulta() { return consulta; }
    public void setConsulta(ConsultaMedica consulta) { this.consulta = consulta; }
}
