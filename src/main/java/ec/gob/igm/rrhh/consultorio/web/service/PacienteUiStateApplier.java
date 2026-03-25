package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PacienteViewState;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
/**
 * Class PacienteUiStateApplier: orquesta la logica de presentacion y flujo web.
 */
public class PacienteUiStateApplier implements Serializable {

    private static final long serialVersionUID = 1L;

    public void apply(PacienteViewBinder.PacienteUiPatch patch, PacienteUiStateTarget target) {
        if (patch == null || target == null) {
            return;
        }

        PacienteViewState pacienteViewState = target.getPacienteViewState();
        applyPatientSelectionState(patch, target, pacienteViewState);
        applyPatientIdentityState(patch, target);
        applyDialogState(patch, target);
    }

    public interface PacienteUiStateTarget {
        PacienteViewState getPacienteViewState();

        void setFicha(FichaOcupacional ficha);

        void setEmpleadoSel(DatEmpleado empleadoSel);

        void setNoPersonaSel(Integer noPersonaSel);

        void setPersonaAux(PersonaAux personaAux);

        void setPermitirIngresoManual(boolean permitirIngresoManual);

        void setCedulaBusqueda(String cedulaBusqueda);

        void setApellido1(String apellido1);

        void setApellido2(String apellido2);

        void setNombre1(String nombre1);

        void setNombre2(String nombre2);

        void setSexo(String sexo);

        void setFechaNacimiento(java.util.Date fechaNacimiento);

        void setEdad(Integer edad);

        void setNoHistoria(String noHistoria);

        void setMostrarDlgCedula(boolean mostrarDlgCedula);

        void setMostrarDialogoAux(boolean mostrarDialogoAux);
    }

    private void applyPatientSelectionState(PacienteViewBinder.PacienteUiPatch patch, PacienteUiStateTarget target,
            PacienteViewState pacienteViewState) {
        if (patch.appliesFicha()) target.setFicha(patch.getFicha());
        if (patch.appliesEmpleadoSel()) {
            target.setEmpleadoSel(patch.getEmpleadoSel());
            pacienteViewState.setEmpleadoSel(patch.getEmpleadoSel());
        }
        if (patch.appliesNoPersonaSel()) {
            target.setNoPersonaSel(patch.getNoPersonaSel());
            pacienteViewState.setNoPersonaSel(patch.getNoPersonaSel());
        }
        if (patch.appliesPersonaAux()) {
            target.setPersonaAux(patch.getPersonaAux());
            pacienteViewState.setPersonaAux(patch.getPersonaAux());
        }
        if (patch.appliesPermitirIngresoManual()) {
            boolean permitirIngresoManual = Boolean.TRUE.equals(patch.getPermitirIngresoManual());
            target.setPermitirIngresoManual(permitirIngresoManual);
            pacienteViewState.setPermitirIngresoManual(permitirIngresoManual);
        }
        if (patch.appliesCedulaBusqueda()) target.setCedulaBusqueda(patch.getCedulaBusqueda());
    }

    private void applyPatientIdentityState(PacienteViewBinder.PacienteUiPatch patch, PacienteUiStateTarget target) {
        if (patch.appliesApellido1()) target.setApellido1(patch.getApellido1());
        if (patch.appliesApellido2()) target.setApellido2(patch.getApellido2());
        if (patch.appliesNombre1()) target.setNombre1(patch.getNombre1());
        if (patch.appliesNombre2()) target.setNombre2(patch.getNombre2());
        if (patch.appliesSexo()) target.setSexo(patch.getSexo());
        if (patch.appliesFechaNacimiento()) target.setFechaNacimiento(patch.getFechaNacimiento());
        if (patch.appliesEdad()) target.setEdad(patch.getEdad());
        if (patch.appliesNoHistoria()) target.setNoHistoria(patch.getNoHistoria());
    }

    private void applyDialogState(PacienteViewBinder.PacienteUiPatch patch, PacienteUiStateTarget target) {
        if (patch.appliesMostrarDlgCedula()) {
            target.setMostrarDlgCedula(Boolean.TRUE.equals(patch.getMostrarDlgCedula()));
        }
        if (patch.appliesMostrarDialogoAux()) {
            target.setMostrarDialogoAux(Boolean.TRUE.equals(patch.getMostrarDialogoAux()));
        }
    }
}
