package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PacienteFichaStateService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private EmpleadoService empleadoService;

    @EJB
    private FichaOcupacionalService fichaService;

    public PatientState ensureEmpleadoSelEnViewScope(boolean permitirIngresoManual, DatEmpleado empleadoSel,
            Integer noPersonaSel, FichaOcupacional ficha, PersonaAux personaAux) {
        if (permitirIngresoManual) {
            return new PatientState(ficha, empleadoSel, noPersonaSel, personaAux, true);
        }

        if (empleadoSel != null) {
            Integer noPersona = (noPersonaSel == null) ? empleadoSel.getNoPersona() : noPersonaSel;
            return new PatientState(ficha, empleadoSel, noPersona, personaAux, false);
        }

        if (ficha != null && ficha.getEmpleado() != null) {
            DatEmpleado empleadoFicha = resolveEmpleadoForView(ficha.getEmpleado());
            ficha.setEmpleado(empleadoFicha);
            Integer noPersona = (noPersonaSel == null) ? empleadoFicha.getNoPersona() : noPersonaSel;
            return new PatientState(ficha, empleadoFicha, noPersona, personaAux, false);
        }

        if (noPersonaSel != null) {
            DatEmpleado empleado = empleadoService.buscarPorId(noPersonaSel);
            if (ficha != null && empleado != null) {
                ficha.setEmpleado(empleado);
                ficha.setPersonaAux(null);
            }
            return new PatientState(ficha, empleado, noPersonaSel, personaAux, false);
        }

        return new PatientState(ficha, null, null, personaAux, false);
    }

    public PatientState syncPatientStateAfterStep1(boolean permitirIngresoManual, DatEmpleado empleadoSel,
            Integer noPersonaSel, PersonaAux personaAux, FichaOcupacional ficha) {
        if (ficha == null) {
            return new PatientState(null, empleadoSel, noPersonaSel, personaAux, permitirIngresoManual);
        }

        if (ficha.getEmpleado() != null) {
            DatEmpleado empleadoFicha = resolveEmpleadoForView(ficha.getEmpleado());
            ficha.setEmpleado(empleadoFicha);
            return new PatientState(ficha, empleadoFicha, empleadoFicha.getNoPersona(), null, false);
        }

        if (ficha.getPersonaAux() != null) {
            return new PatientState(ficha, null, null, ficha.getPersonaAux(), true);
        }

        return new PatientState(ficha, empleadoSel, noPersonaSel, personaAux, permitirIngresoManual);
    }

    public PatientState ensurePatientAssignedForFicha(boolean permitirIngresoManual, DatEmpleado empleadoSel,
            Integer noPersonaSel, PersonaAux personaAux, FichaOcupacional ficha) {
        if (ficha == null) {
            return new PatientState(null, empleadoSel, noPersonaSel, personaAux, permitirIngresoManual);
        }

        if (ficha.getEmpleado() != null && ficha.getPersonaAux() != null) {
            if (permitirIngresoManual && personaAux != null) {
                ficha.setEmpleado(null);
            } else {
                ficha.setPersonaAux(null);
            }
            return syncPatientStateAfterStep1(permitirIngresoManual, empleadoSel, noPersonaSel, personaAux, ficha);
        }

        if (ficha.getEmpleado() != null || ficha.getPersonaAux() != null) {
            return syncPatientStateAfterStep1(permitirIngresoManual, empleadoSel, noPersonaSel, personaAux, ficha);
        }

        DatEmpleado empleado = empleadoSel;
        if (empleado == null && noPersonaSel != null) {
            empleado = empleadoService.buscarPorId(noPersonaSel);
        }

        if (empleado != null) {
            ficha.setEmpleado(empleado);
            ficha.setPersonaAux(null);
            return new PatientState(ficha, empleado, empleado.getNoPersona(), null, false);
        }

        if (personaAux != null) {
            ficha.setPersonaAux(personaAux);
            ficha.setEmpleado(null);
            return new PatientState(ficha, null, null, personaAux, true);
        }

        if (ficha.getIdFicha() != null) {
            FichaOcupacional persisted = fichaService.findById(ficha.getIdFicha());
            if (persisted != null) {
                if (persisted.getEmpleado() != null) {
                    DatEmpleado empleadoPersistido = resolveEmpleadoForView(persisted.getEmpleado());
                    ficha.setEmpleado(empleadoPersistido);
                    ficha.setPersonaAux(null);
                    return new PatientState(ficha, empleadoPersistido, empleadoPersistido.getNoPersona(), null, false);
                }
                if (persisted.getPersonaAux() != null) {
                    ficha.setPersonaAux(persisted.getPersonaAux());
                    ficha.setEmpleado(null);
                    return new PatientState(ficha, null, null, persisted.getPersonaAux(), true);
                }
            }
        }

        throw new IllegalStateException(
                "Debe seleccionar un empleado o registrar una persona auxiliar antes de guardar el Step 3.");
    }

    private DatEmpleado resolveEmpleadoForView(DatEmpleado empleado) {
        if (empleado == null || empleado.getNoPersona() == null) {
            return empleado;
        }

        DatEmpleado completo = empleadoService.buscarPorId(empleado.getNoPersona());
        return completo != null ? completo : empleado;
    }

    public static class PatientState {

        private final FichaOcupacional ficha;
        private final DatEmpleado empleadoSel;
        private final Integer noPersonaSel;
        private final PersonaAux personaAux;
        private final boolean permitirIngresoManual;

        public PatientState(FichaOcupacional ficha, DatEmpleado empleadoSel, Integer noPersonaSel, PersonaAux personaAux,
                boolean permitirIngresoManual) {
            this.ficha = ficha;
            this.empleadoSel = empleadoSel;
            this.noPersonaSel = noPersonaSel;
            this.personaAux = personaAux;
            this.permitirIngresoManual = permitirIngresoManual;
        }

        public FichaOcupacional getFicha() {
            return ficha;
        }

        public DatEmpleado getEmpleadoSel() {
            return empleadoSel;
        }

        public Integer getNoPersonaSel() {
            return noPersonaSel;
        }

        public PersonaAux getPersonaAux() {
            return personaAux;
        }

        public boolean isPermitirIngresoManual() {
            return permitirIngresoManual;
        }
    }
}
