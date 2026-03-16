package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.service.PacienteViewFlowDelegate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PacienteSectionFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PacienteViewFlowDelegate pacienteViewFlowDelegate;

    public void applyPacienteUiResult(CentroMedicoCtrl ctrl, PacienteRegistrationFacade.UiResult uiResult) {
        pacienteViewFlowDelegate.applyPacienteUiResult(ctrl, uiResult);
    }

    public void asegurarPacienteAsignado(CentroMedicoCtrl ctrl,
                                         boolean permitirIngresoManual,
                                         DatEmpleado empleadoSel,
                                         Integer noPersonaSel,
                                         PersonaAux personaAux,
                                         FichaOcupacional ficha) {
        pacienteViewFlowDelegate.asegurarPacienteAsignado(ctrl, permitirIngresoManual, empleadoSel, noPersonaSel, personaAux, ficha);
    }

    public void asegurarPersonaAuxPersistida(CentroMedicoCtrl ctrl) {
        pacienteViewFlowDelegate.asegurarPersonaAuxPersistida(ctrl);
    }

    public void onBuscarPorCedulaRh(CentroMedicoCtrl ctrl, Logger log) {
        pacienteViewFlowDelegate.onBuscarPorCedulaRh(ctrl, log);
    }

    public void buscarCedula(CentroMedicoCtrl ctrl, Logger log) {
        pacienteViewFlowDelegate.buscarCedula(ctrl, log);
    }

    public void prepararIngresoManual(CentroMedicoCtrl ctrl) {
        pacienteViewFlowDelegate.prepararIngresoManual(ctrl);
    }

    public void abrirPersonaAuxManual(CentroMedicoCtrl ctrl) {
        pacienteViewFlowDelegate.abrirPersonaAuxManual(ctrl);
    }

    public void guardarPersonaAuxYUsar(CentroMedicoCtrl ctrl, Logger log) {
        pacienteViewFlowDelegate.guardarPersonaAuxYUsar(ctrl, log);
    }
}
