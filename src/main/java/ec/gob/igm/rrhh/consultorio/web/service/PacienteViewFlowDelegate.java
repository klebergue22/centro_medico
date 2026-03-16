package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.facade.PacienteRegistrationFacade;
import ec.gob.igm.rrhh.consultorio.web.mapper.PacienteSearchInputAssembler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
/**
 * Class PacienteViewFlowDelegate: centraliza el flujo de paciente/cédula/persona auxiliar para el controller.
 */
public class PacienteViewFlowDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient PacienteControllerSupport pacienteControllerSupport;
    @Inject
    private transient PacienteSearchInputAssembler pacienteSearchInputAssembler;
    @Inject
    private transient PacienteRegistrationFacade pacienteRegistrationFacade;
    @Inject
    private transient PacienteUiStateApplier pacienteUiStateApplier;

    public void onBuscarPorCedulaRh(CentroMedicoCtrl ctrl, Logger logger) {
        pacienteControllerSupport.onBuscarPorCedulaRh(buildBuscarCedulaInput(ctrl, logger));
    }

    public void buscarCedula(CentroMedicoCtrl ctrl, Logger logger) {
        pacienteControllerSupport.buscarCedula(buildBuscarCedulaInput(ctrl, logger));
    }

    public void prepararIngresoManual(CentroMedicoCtrl ctrl) {
        pacienteControllerSupport.prepararIngresoManual(
                ctrl.getCedulaBusqueda(),
                ctrl.getPersonaAux(),
                uiResult -> applyPacienteUiResult(ctrl, uiResult));
    }

    public void abrirPersonaAuxManual(CentroMedicoCtrl ctrl) {
        pacienteControllerSupport.abrirPersonaAuxManual(
                ctrl.getCedulaBusqueda(),
                ctrl.getPersonaAux(),
                ctrl.getFicha(),
                ctrl.getEmpleadoSel(),
                ctrl.getNoPersonaSel(),
                ctrl.isPermitirIngresoManual(),
                ctrl.isMostrarDlgCedula(),
                uiResult -> applyPacienteUiResult(ctrl, uiResult));
    }

    public void guardarPersonaAuxYUsar(CentroMedicoCtrl ctrl, Logger logger) {
        pacienteControllerSupport.guardarPersonaAuxYUsar(
                ctrl.getPersonaAux(),
                ctrl.getFicha(),
                ctrl.getEmpleadoSel(),
                ctrl.getNoPersonaSel(),
                uiResult -> applyPacienteUiResult(ctrl, uiResult),
                logger);
    }

    public void asegurarPersonaAuxPersistida(CentroMedicoCtrl ctrl) {
        applyPacienteUiResult(ctrl, pacienteRegistrationFacade.asegurarPersonaAuxPersistida(
                ctrl.isPermitirIngresoManual(),
                ctrl.getFicha(),
                ctrl.getPersonaAux()));
    }

    public void asegurarPacienteAsignado(CentroMedicoCtrl ctrl, boolean permitirIngresoManual,
            DatEmpleado empleadoSel, Integer noPersonaSel, PersonaAux personaAux, FichaOcupacional ficha) {
        applyPacienteUiResult(ctrl, pacienteRegistrationFacade.asegurarPacienteAsignado(
                permitirIngresoManual,
                empleadoSel,
                noPersonaSel,
                personaAux,
                ficha));
    }

    public void applyPacienteUiResult(CentroMedicoCtrl ctrl, PacienteRegistrationFacade.UiResult uiResult) {
        if (uiResult == null || ctrl == null) {
            return;
        }

        PacienteViewBinder.PacienteUiPatch patch = uiResult.getPatch();
        if (patch != null) {
            pacienteUiStateApplier.apply(patch, ctrl);
        }

        for (String script : uiResult.getScripts()) {
            PrimeFaces.current().executeScript(script);
        }
    }

    private PacienteControllerSupport.BuscarCedulaInput buildBuscarCedulaInput(CentroMedicoCtrl ctrl, Logger logger) {
        return pacienteSearchInputAssembler.buildBuscarCedulaInput(
                ctrl,
                logger,
                uiResult -> applyPacienteUiResult(ctrl, uiResult));
    }
}
