package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.function.Consumer;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.PacienteRegistrationFacade;
import ec.gob.igm.rrhh.consultorio.web.jsf.CentroMedicoMessageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
/**
 * Class PacienteControllerSupport: orquesta la lógica de presentación y flujo web.
 */
public class PacienteControllerSupport implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient PacienteRegistrationFacade pacienteRegistrationFacade;
    @Inject
    private transient CedulaDialogUiCoordinator cedulaDialogUiCoordinator;
    @Inject
    private transient PersonaAuxDialogUiCoordinator personaAuxDialogUiCoordinator;
    @Inject
    private transient CentroMedicoMessageService messageService;

    public void onBuscarPorCedulaRh(BuscarCedulaInput input) {
        try {
            buscarCedula(input);
        } catch (RuntimeException ex) {
            messageService.handleUnexpected(input.getLogger(), "onBuscarPorCedulaRh", ex,
                    input.getActiveStep(), input.getNoPersonaSel(), input.getCedulaBusqueda());
            cedulaDialogUiCoordinator.onRhError();
        }
    }

    public void buscarCedula(BuscarCedulaInput input) {
        try {
            PacienteRegistrationFacade.UiResult uiResult = pacienteRegistrationFacade.buscarPorCedula(
                    input.getCedulaBusqueda(),
                    input.getFicha(),
                    input.getPersonaAux(),
                    input.isPermitirIngresoManual());
            input.getApplyUiResult().accept(uiResult);

            PacienteUiFlowCoordinator.UiFlowResult flowResult = uiResult.getFlowResult();
            if (flowResult != null && flowResult.isFound()) {
                cedulaDialogUiCoordinator.onFound(CedulaSearchService.CedulaSearchResult.found(
                        flowResult.getCedulaBusqueda(),
                        flowResult.getFicha(),
                        flowResult.getPersonaAux(),
                        flowResult.getEmpleadoSel(),
                        flowResult.getNoPersonaSel(),
                        flowResult.getApellido1(),
                        flowResult.getApellido2(),
                        flowResult.getNombre1(),
                        flowResult.getNombre2(),
                        flowResult.getSexo(),
                        flowResult.getFechaNacimiento(),
                        flowResult.getEdad()));
                if (flowResult.isCargoNoEncontrado()) {
                    cedulaDialogUiCoordinator.showCargoMissing();
                }
            } else if (flowResult != null && flowResult.isShowManual()) {
                cedulaDialogUiCoordinator.onManualEnabled(CedulaSearchService.CedulaSearchResult.manual(
                        flowResult.getCedulaBusqueda(),
                        flowResult.getFicha(),
                        flowResult.getPersonaAux()));
            }

            cedulaDialogUiCoordinator.refreshMainViews();
        } catch (CedulaSearchService.CedulaValidationException ex) {
            cedulaDialogUiCoordinator.onValidationWarning(ex.getMessage());
        } catch (RuntimeException ex) {
            messageService.handleUnexpected(input.getLogger(), "buscarCedula", ex,
                    input.getActiveStep(), input.getNoPersonaSel(), input.getCedulaBusqueda());
            cedulaDialogUiCoordinator.onSearchError();
        }
    }

    public void prepararIngresoManual(String cedulaBusqueda, PersonaAux personaAux,
            Consumer<PacienteRegistrationFacade.UiResult> applyUiResult) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            applyUiResult.accept(pacienteRegistrationFacade.habilitarIngresoManual(cedulaBusqueda, personaAux));
        } catch (PersonaAuxFlowService.PersonaAuxValidationException ex) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Cédula requerida", ex.getMessage()));
        }
    }

    public void abrirPersonaAuxManual(String cedulaBusqueda, PersonaAux personaAux, FichaOcupacional ficha,
            DatEmpleado empleadoSel, Integer noPersonaSel, boolean permitirIngresoManual, boolean mostrarDlgCedula,
            Consumer<PacienteRegistrationFacade.UiResult> applyUiResult) {
        PacienteRegistrationFacade.UiResult uiResult = pacienteRegistrationFacade.abrirPersonaAuxManual(
                cedulaBusqueda,
                personaAux,
                ficha,
                empleadoSel,
                noPersonaSel,
                permitirIngresoManual,
                mostrarDlgCedula);
        applyUiResult.accept(uiResult);
    }

    public void guardarPersonaAuxYUsar(PersonaAux personaAux, FichaOcupacional ficha,
            DatEmpleado empleadoSel, Integer noPersonaSel,
            Consumer<PacienteRegistrationFacade.UiResult> applyUiResult,
            Logger logger) {
        logger.info(String.valueOf("INGRESA AL METODO DE GUARDAR "));
        logger.info(String.valueOf("PERSONA AUXILIAR ANTES VALIDAR: " + personaAux));

        try {
            PacienteRegistrationFacade.UiResult uiResult = pacienteRegistrationFacade.guardarPersonaAux(
                    personaAux,
                    ficha,
                    empleadoSel,
                    noPersonaSel);

            applyUiResult.accept(uiResult);

            personaAuxDialogUiCoordinator.onGuardarSuccess(uiResult.getFlowResult());

            logger.info("PersonaAux guardada manualmente: {} {} / {} {} (cedula={})",
                    personaAux.getApellido1(),
                    personaAux.getApellido2(),
                    personaAux.getNombre1(),
                    personaAux.getNombre2(),
                    personaAux.getCedula());

        } catch (PersonaAuxFlowService.PersonaAuxValidationException e) {
            logger.warn("Validación PersonaAux en flujo manual: {}", e.getMessage());
            personaAuxDialogUiCoordinator.onValidationFailure(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error guardando datos manuales", e);
            personaAuxDialogUiCoordinator.onTechnicalFailure();
        }
    }

    public static final class BuscarCedulaInput {
        private final String cedulaBusqueda;
        private final FichaOcupacional ficha;
        private final PersonaAux personaAux;
        private final boolean permitirIngresoManual;
        private final String activeStep;
        private final Integer noPersonaSel;
        private final Logger logger;
        private final Consumer<PacienteRegistrationFacade.UiResult> applyUiResult;

        private BuscarCedulaInput(Builder builder) {
            this.cedulaBusqueda = builder.cedulaBusqueda;
            this.ficha = builder.ficha;
            this.personaAux = builder.personaAux;
            this.permitirIngresoManual = builder.permitirIngresoManual;
            this.activeStep = builder.activeStep;
            this.noPersonaSel = builder.noPersonaSel;
            this.logger = builder.logger;
            this.applyUiResult = builder.applyUiResult;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getCedulaBusqueda() { return cedulaBusqueda; }
        public FichaOcupacional getFicha() { return ficha; }
        public PersonaAux getPersonaAux() { return personaAux; }
        public boolean isPermitirIngresoManual() { return permitirIngresoManual; }
        public String getActiveStep() { return activeStep; }
        public Integer getNoPersonaSel() { return noPersonaSel; }
        public Logger getLogger() { return logger; }
        public Consumer<PacienteRegistrationFacade.UiResult> getApplyUiResult() { return applyUiResult; }

        public static final class Builder {
            private String cedulaBusqueda;
            private FichaOcupacional ficha;
            private PersonaAux personaAux;
            private boolean permitirIngresoManual;
            private String activeStep;
            private Integer noPersonaSel;
            private Logger logger;
            private Consumer<PacienteRegistrationFacade.UiResult> applyUiResult;

            public Builder cedulaBusqueda(String cedulaBusqueda) { this.cedulaBusqueda = cedulaBusqueda; return this; }
            public Builder ficha(FichaOcupacional ficha) { this.ficha = ficha; return this; }
            public Builder personaAux(PersonaAux personaAux) { this.personaAux = personaAux; return this; }
            public Builder permitirIngresoManual(boolean permitirIngresoManual) { this.permitirIngresoManual = permitirIngresoManual; return this; }
            public Builder activeStep(String activeStep) { this.activeStep = activeStep; return this; }
            public Builder noPersonaSel(Integer noPersonaSel) { this.noPersonaSel = noPersonaSel; return this; }
            public Builder logger(Logger logger) { this.logger = logger; return this; }
            public Builder applyUiResult(Consumer<PacienteRegistrationFacade.UiResult> applyUiResult) { this.applyUiResult = applyUiResult; return this; }

            public BuscarCedulaInput build() { return new BuscarCedulaInput(this); }
        }
    }
}
