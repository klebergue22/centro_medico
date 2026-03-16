package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.service.PacienteUiFlowCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.PacienteViewBinder;
import ec.gob.igm.rrhh.consultorio.web.service.PersonaAuxFlowService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
/**
 * Class PacienteRegistrationFacade: expone una fachada para simplificar operaciones del módulo web.
 */
public class PacienteRegistrationFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient PacienteUiFlowCoordinator pacienteUiFlowCoordinator;

    @Inject
    private transient PersonaAuxFlowService personaAuxFlowService;

    @Inject
    private transient PacienteViewBinder pacienteViewBinder;

    public UiResult buscarPorCedula(String cedulaBusqueda, FichaOcupacional ficha, PersonaAux personaAux,
            boolean permitirIngresoManual) {
        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.buscarCedula(
                cedulaBusqueda,
                ficha,
                personaAux,
                permitirIngresoManual);

        return UiResult.builder()
                .patch(pacienteViewBinder.forCedulaSearch(result))
                .flowResult(result)
                .build();
    }

    public UiResult habilitarIngresoManual(String cedulaBusqueda, PersonaAux personaAux)
            throws PersonaAuxFlowService.PersonaAuxValidationException {
        PersonaAuxFlowService.ManualPreparationResult result = personaAuxFlowService.prepararIngresoManual(
                cedulaBusqueda,
                personaAux);

        return UiResult.builder()
                .patch(pacienteViewBinder.forManualPreparation(result))
                .build();
    }

    public UiResult abrirPersonaAuxManual(String cedulaBusqueda, PersonaAux personaAux, FichaOcupacional ficha,
            DatEmpleado empleadoSel, Integer noPersonaSel, boolean permitirIngresoManual, boolean mostrarDlgCedula) {
        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.abrirPersonaAuxManual(
                cedulaBusqueda,
                personaAux,
                ficha,
                empleadoSel,
                noPersonaSel,
                permitirIngresoManual,
                mostrarDlgCedula);

        return UiResult.builder()
                .patch(pacienteViewBinder.forAbrirPersonaAuxManual(result))
                .flowResult(result)
                .scripts(result.getScripts())
                .build();
    }

    public UiResult guardarPersonaAux(PersonaAux personaAux, FichaOcupacional ficha, DatEmpleado empleadoSel,
            Integer noPersonaSel) {
        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.guardarPersonaAuxYUsar(
                personaAux,
                ficha,
                empleadoSel,
                noPersonaSel);

        return UiResult.builder()
                .patch(pacienteViewBinder.forGuardarPersonaAux(result))
                .flowResult(result)
                .scripts(result.getScripts())
                .build();
    }

    public UiResult asegurarPacienteAsignado(boolean permitirIngresoManual, DatEmpleado empleadoSel,
            Integer noPersonaSel, PersonaAux personaAux, FichaOcupacional ficha) {
        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.ensurePatientAssignedForFicha(
                permitirIngresoManual,
                empleadoSel,
                noPersonaSel,
                personaAux,
                ficha);

        return fromGeneralFlow(result);
    }

    public UiResult asegurarEmpleadoEnViewScope(boolean permitirIngresoManual, DatEmpleado empleadoSel,
            Integer noPersonaSel, FichaOcupacional ficha, PersonaAux personaAux) {
        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.ensureEmpleadoSelEnViewScope(
                permitirIngresoManual,
                empleadoSel,
                noPersonaSel,
                ficha,
                personaAux);

        return fromGeneralFlow(result);
    }

    public UiResult syncPatientStateAfterStep1(boolean permitirIngresoManual, DatEmpleado empleadoSel,
            Integer noPersonaSel, PersonaAux personaAux, FichaOcupacional ficha) {
        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.syncPatientStateAfterStep1(
                permitirIngresoManual,
                empleadoSel,
                noPersonaSel,
                personaAux,
                ficha);

        return fromGeneralFlow(result);
    }

    public UiResult asegurarPersonaAuxPersistida(boolean permitirIngresoManual, FichaOcupacional ficha,
            PersonaAux personaAux) {
        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.asegurarPersonaAuxPersistida(
                permitirIngresoManual,
                ficha,
                personaAux);

        return fromGeneralFlow(result);
    }

    private UiResult fromGeneralFlow(PacienteUiFlowCoordinator.UiFlowResult result) {
        return UiResult.builder()
                .patch(pacienteViewBinder.forGeneralFlow(result))
                .flowResult(result)
                .build();
    }

    public static final class UiResult {
        private final PacienteViewBinder.PacienteUiPatch patch;
        private final PacienteUiFlowCoordinator.UiFlowResult flowResult;
        private final List<String> scripts;

        private UiResult(Builder builder) {
            this.patch = builder.patch;
            this.flowResult = builder.flowResult;
            this.scripts = Collections.unmodifiableList(new ArrayList<>(builder.scripts));
        }

        public static Builder builder() {
            return new Builder();
        }

        public PacienteViewBinder.PacienteUiPatch getPatch() {
            return patch;
        }

        public PacienteUiFlowCoordinator.UiFlowResult getFlowResult() {
            return flowResult;
        }

        public List<String> getScripts() {
            return scripts;
        }

        public static final class Builder {
            private PacienteViewBinder.PacienteUiPatch patch;
            private PacienteUiFlowCoordinator.UiFlowResult flowResult;
            private final List<String> scripts = new ArrayList<>();

            public Builder patch(PacienteViewBinder.PacienteUiPatch patch) {
                this.patch = patch;
                return this;
            }

            public Builder flowResult(PacienteUiFlowCoordinator.UiFlowResult flowResult) {
                this.flowResult = flowResult;
                return this;
            }

            public Builder scripts(List<String> scripts) {
                if (scripts != null) {
                    this.scripts.addAll(scripts);
                }
                return this;
            }

            public UiResult build() {
                return new UiResult(this);
            }
        }
    }
}
