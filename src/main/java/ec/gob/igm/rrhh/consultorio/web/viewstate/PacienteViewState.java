package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;

/**
 * Class PacienteViewState: contiene la lógica de la aplicación.
 */
public class PacienteViewState implements Serializable {

    private static final long serialVersionUID = 1L;

    private String activeStep = "step1";
    private boolean cedulaDlgAutoOpened;
    private boolean mostrarDlgCedula = true;
    private boolean preRenderDone;
    private boolean mostrarDiaLOGoAux;
    private boolean permitirIngresoManual;
    private String cedulaBusqueda;
    private Integer noPersonaSel;
    private DatEmpleado empleadoSel;
    private PersonaAux personaAux;

    public String getActiveStep() { return activeStep; }
    public void setActiveStep(String activeStep) { this.activeStep = activeStep; }
    public boolean isCedulaDlgAutoOpened() { return cedulaDlgAutoOpened; }
    public void setCedulaDlgAutoOpened(boolean cedulaDlgAutoOpened) { this.cedulaDlgAutoOpened = cedulaDlgAutoOpened; }
    public boolean isMostrarDlgCedula() { return mostrarDlgCedula; }
    public void setMostrarDlgCedula(boolean mostrarDlgCedula) { this.mostrarDlgCedula = mostrarDlgCedula; }
    public boolean isPreRenderDone() { return preRenderDone; }
    public void setPreRenderDone(boolean preRenderDone) { this.preRenderDone = preRenderDone; }
    public boolean isMostrarDiaLOGoAux() { return mostrarDiaLOGoAux; }
    public void setMostrarDiaLOGoAux(boolean mostrarDiaLOGoAux) { this.mostrarDiaLOGoAux = mostrarDiaLOGoAux; }
    public boolean isPermitirIngresoManual() { return permitirIngresoManual; }
    public void setPermitirIngresoManual(boolean permitirIngresoManual) { this.permitirIngresoManual = permitirIngresoManual; }
    public String getCedulaBusqueda() { return cedulaBusqueda; }
    public void setCedulaBusqueda(String cedulaBusqueda) { this.cedulaBusqueda = cedulaBusqueda; }
    public Integer getNoPersonaSel() { return noPersonaSel; }
    public void setNoPersonaSel(Integer noPersonaSel) { this.noPersonaSel = noPersonaSel; }
    public DatEmpleado getEmpleadoSel() { return empleadoSel; }
    public void setEmpleadoSel(DatEmpleado empleadoSel) { this.empleadoSel = empleadoSel; }
    public PersonaAux getPersonaAux() { return personaAux; }
    public void setPersonaAux(PersonaAux personaAux) { this.personaAux = personaAux; }
}
