package ec.gob.igm.rrhh.consultorio.web.ctrl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoWizardCoordinator;
import ec.gob.igm.rrhh.consultorio.web.jsf.CentroMedicoMessageService;
import ec.gob.igm.rrhh.consultorio.web.mapper.Step3CommandAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.StepValidationInputAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.Step3ViewDataAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.PdfFichaInputAssembler;
import ec.gob.igm.rrhh.consultorio.web.facade.DiagnosticoSectionFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.PacienteSectionFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.PdfPreviewCommandFactory;
import ec.gob.igm.rrhh.consultorio.web.facade.PdfCommandContextBuilder;
import ec.gob.igm.rrhh.consultorio.web.facade.PdfPreviewFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.PdfSectionFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.Step1Facade;
import ec.gob.igm.rrhh.consultorio.web.facade.WizardSectionFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfContextAssembler;
import ec.gob.igm.rrhh.consultorio.web.service.CedulaDialogControllerSupport;
import ec.gob.igm.rrhh.consultorio.web.service.CedulaDialogStateService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoFormInitializer;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoFormStateService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoReactiveUiService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardNavigationCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoValidationCoordinator.FichaCompletaValidationInput;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoValidationCoordinator.Step1ValidationInput;
import ec.gob.igm.rrhh.consultorio.web.service.ValidationUiResult;
import ec.gob.igm.rrhh.consultorio.web.service.WizardStepActionService;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfDataMapper;
import ec.gob.igm.rrhh.consultorio.web.service.PacienteUiStateApplier;
import ec.gob.igm.rrhh.consultorio.web.service.Step2OrchestratorService.Step2RiskCommand;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoCalcUtil;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PacienteViewState;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PacienteFormData;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfPreviewState;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step1FormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step2FormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.AtencionPrioritariaModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.ActividadLaboralFormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.AntecedentesFormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.DiagnosticoFormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.ExamenFisicoFormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.ExamenesComplementariosFormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.FichaContext;
import ec.gob.igm.rrhh.consultorio.web.viewstate.GinecoObstetricoFormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.HabitosConsumoFormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.HistoriaLaboralFormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.SignosVitalesFormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step3FormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.WizardViewState;

/**
 * Controlador principal de la vista del Centro Médico: orquesta el flujo del
 * wizard, mantiene el estado de pantalla y delega validaciones/persistencia a
 * servicios de aplicación.
 */
@Named("centroMedicoCtrl")
@ViewScoped
public class CentroMedicoCtrl implements Serializable, PacienteUiStateApplier.PacienteUiStateTarget {

    // =========================
    // CONSTANTES ESTÁTICAS
    // =========================
    private static final Logger LOG = LoggerFactory.getLogger(CentroMedicoCtrl.class);
    private static final long serialVersionUID = 2L;
    private static final List<String> STATIC_RISK_COLS = new ArrayList<>();
    private static final int H_ROWS = 4;
    private static final int DIAG_ROWS = 6;

    static {
        for (int i = 1; i <= 7; i++) {
            STATIC_RISK_COLS.add(String.valueOf(i));
        }
    }

    // =========================
    // INYECCIONES DE DEPENDENCIAS - INJECT
    // =========================
    @Inject
    private transient PdfSessionStore pdfSessionStore;
    @Inject
    private transient WizardSectionFacade wizardSectionFacade;
    @Inject
    private transient CentroMedicoMessageService messageService;
    @Inject
    private transient ControllerActionTemplate controllerActionTemplate;
    @Inject
    private transient WizardStepActionService wizardStepActionService;
    @Inject
    private transient CentroMedicoCalcUtil calcUtil;
    @Inject
    private transient CedulaDialogStateService cedulaDialogStateService;
    @Inject
    private transient CedulaDialogControllerSupport cedulaDialogControllerSupport;
    @Inject
    private transient CentroMedicoFormInitializer centroMedicoFormInitializer;
    @Inject
    private transient CentroMedicoFormStateService centroMedicoFormStateService;
    @Inject
    private transient CentroMedicoReactiveUiService reactiveUiService;

    @Inject
    private transient Step3CommandAssembler step3CommandAssembler;
    @Inject
    private transient StepValidationInputAssembler stepValidationInputAssembler;
    @Inject
    private transient Step3ViewDataAssembler step3ViewDataAssembler;
    @Inject
    private transient PdfFichaInputAssembler pdfFichaInputAssembler;
    @Inject
    private transient FichaPdfContextAssembler fichaPdfContextAssembler;
    @Inject
    private transient FichaPdfDataMapper fichaPdfDataMapper;
    @Inject
    private transient DiagnosticoSectionFacade diagnosticoSectionFacade;
    @Inject
    private transient PacienteSectionFacade pacienteSectionFacade;
    @Inject
    private transient PdfSectionFacade pdfSectionFacade;
    @Inject
    private transient PdfPreviewFacade pdfPreviewFacade;
    @Inject
    private transient PdfPreviewCommandFactory pdfPreviewCommandFactory;
    @Inject
    private transient PdfCommandContextBuilder pdfCommandContextBuilder;
    @Inject
    private transient CentroMedicoWizardCoordinator wizardCoordinator;

    // =========================
    // MODELOS DE FORMULARIO
    // =========================
    private final Step1FormModel step1FormModel = new Step1FormModel();
    private final Step2FormModel step2FormModel = new Step2FormModel();
    private final Step3FormModel step3FormModel = new Step3FormModel();
    private final AtencionPrioritariaModel atencionPrioritariaModel = new AtencionPrioritariaModel();
    private final SignosVitalesFormModel signosVitalesFormModel = new SignosVitalesFormModel();
    private final DiagnosticoFormModel diagnosticoFormModel = new DiagnosticoFormModel();
    private AntecedentesFormModel antecedentesFormModel = new AntecedentesFormModel();
    private final GinecoObstetricoFormModel ginecoObstetricoFormModel = new GinecoObstetricoFormModel();
    private final ExamenFisicoFormModel examenFisicoFormModel = new ExamenFisicoFormModel();
    private final HabitosConsumoFormModel habitosConsumoFormModel = new HabitosConsumoFormModel();
    private final ActividadLaboralFormModel actividadLaboralFormModel = new ActividadLaboralFormModel();
    private final HistoriaLaboralFormModel historiaLaboralFormModel = new HistoriaLaboralFormModel();
    private final ExamenesComplementariosFormModel examenesComplementariosFormModel = new ExamenesComplementariosFormModel();
    private final PdfPreviewState pdfPreviewState = new PdfPreviewState();
    private final PacienteViewState pacienteViewState = new PacienteViewState();
    private final WizardViewState wizardViewState = new WizardViewState();
    private final FichaContext fichaContext = new FichaContext();


    // VARIABLES DE DATOS PERSONALES
    // =========================
    private final PacienteFormData pacienteFormData = step1FormModel.getPaciente();

    // =========================
    // MÉTODOS DE EXCEPCIÓN Y VALIDACIÓN PRIVADOS
    // =========================
    private void fail(String message) {
        throw new BusinessValidationException(message);
    }

    // =========================
    // CICLO DE VIDA
    // =========================
    @PostConstruct
    public void init() {
        centroMedicoFormInitializer.initUiDefaults(this);

        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getViewRoot() != null) {
            fc.getViewRoot().setLocale(new Locale("es", "EC"));
        }

        centroMedicoFormInitializer.initDomainDefaults(this);
        centroMedicoFormInitializer.initStep2Defaults(this, STATIC_RISK_COLS);
        centroMedicoFormInitializer.initStep3Defaults(this, H_ROWS, DIAG_ROWS);
        centroMedicoFormStateService.prepareStep3Collections(this, H_ROWS, DIAG_ROWS, 5);
    }

    // =========================
    // NAVEGACIÓN DEL WIZARD
    // =========================
    public String onFlow(FlowEvent event) {
        final String nextStep = event.getNewStep();
        this.wizardViewState.setActiveStep(nextStep);
        if ("step1".equals(nextStep)) {
            wizardViewState.setCedulaDlgAutoOpened(false);
        }
        return nextStep;
    }

    public void retrocederStep() {
        wizardViewState.setActiveStep(wizardSectionFacade.retrocederStep(wizardViewState.getActiveStep()));
    }

    public void guardarStepActual() {
        LOG.info(">>> ENTRO A guardarStepActual, step={}", wizardViewState.getActiveStep());
        controllerActionTemplate.executeWithResult(
                "guardarStepActual",
                () -> {
                    wizardCoordinator.guardarStepActual(
                            new CentroMedicoWizardCoordinator.GuardarStepActualCoordinatorCommand(
                                    wizardViewState.getActiveStep(),
                                    this::guardarStep1,
                                    this::guardarStep2,
                                    this::guardarStep3,
                                    wizardViewState::setActiveStep,
                                    this::applyStep4State,
                                    getFicha(),
                                    pdfPreviewState,
                                    this::setFicha,
                                    this::setMostrarDlgCedula,
                                    () -> pdfPreviewCommandFactory
                                            .buildBasePrepareCommandForCoordinator(buildPdfCommandContext())));
                    return wizardViewState.getActiveStep();
                },
                ignored -> {
                },
                LOG,
                wizardViewState.getActiveStep(),
                getNoPersonaSel(),
                getCedulaBusqueda());
    }

    // =========================
    // VALIDACIÓN DE STEPS
    // =========================
    private boolean validarStep1() {
        FichaRiesgo fichaRiesgo = step2FormModel.getFichaRiesgo();
        Step1ValidationInput input = stepValidationInputAssembler.buildStep1Input(
                pacienteFormData.getApellido1(),
                pacienteFormData.getApellido2(),
                pacienteFormData.getNombre1(),
                pacienteFormData.getNombre2(),
                step1FormModel.getFechaAtencion(),
                pacienteFormData.getSexo(),
                step1FormModel.getTipoEval(),
                signosVitalesFormModel.getPaStr(),
                signosVitalesFormModel.getFc(),
                signosVitalesFormModel.getPeso(),
                signosVitalesFormModel.getTallaCm(),
                getSignos(),
                fichaRiesgo,
                getEmpleadoSel(),
                getNoPersonaSel(),
                getPersonaAux());

        ValidationUiResult uiResult = wizardSectionFacade.validarStep1(input);
        uiResult.applyUi(messageService);
        return uiResult.isValid();
    }

    private boolean validarStep2() {
        ValidationUiResult uiResult = wizardSectionFacade.validarStep2(
                step2FormModel.getFichaRiesgo(),
                step2FormModel.getActividadesLab(),
                step2FormModel.getMedidasPreventivas(),
                true);
        uiResult.applyUi(messageService);
        return uiResult.isValid();
    }

    private boolean validarStep3() {
        LOG.info("[STEP3] validarStep3() INICIO");
        ValidationUiResult uiResult = wizardSectionFacade.validarStep3(
                step3FormModel.getListaDiag(),
                diagnosticoFormModel.getAptitudSel(),
                diagnosticoFormModel.getRecomendaciones(),
                diagnosticoFormModel.getMedicoNombre(),
                diagnosticoFormModel.getMedicoCodigo());
        if (!uiResult.isValid()) {
            for (String error : uiResult.getValidationResult().getErrors()) {
                LOG.info("[STEP3] validarStep3() FAIL: {}", error);
            }
        }
        uiResult.applyUi(messageService);
        LOG.info("[STEP3] validarStep3() FIN -> {}", uiResult.isValid());
        return uiResult.isValid();
    }

    private boolean verificarFichaCompleta() {
        FichaCompletaValidationInput input = stepValidationInputAssembler.buildFichaCompletaInput(
                getFicha(),
                isPermitirIngresoManual(),
                getPersonaAux(),
                getEmpleadoSel(),
                diagnosticoFormModel.getAptitudSel(),
                diagnosticoFormModel.getFechaEmision(),
                this::inferCie10PrincipalFromListaK);

        ValidationUiResult uiResult = wizardSectionFacade.verificarFichaCompleta(input);
        uiResult.applyUi(messageService);
        return uiResult.isValid();
    }

    // =========================
    // GUARDADO DE STEPS
    // =========================
    public void guardarStep1() {
        wizardStepActionService.execute(new WizardStepActionService.ExecuteStepActionCommand(
                "guardarStep1",
                this::validarStep1,
                this::saveStep1,
                null,
                () -> messageService.info("Step 1 guardado correctamente (BORRADOR)."),
                LOG,
                wizardViewState.getActiveStep(),
                getNoPersonaSel(),
                getCedulaBusqueda()));
    }

    private void saveStep1() {
        Step1Facade.SaveStep1Result result = wizardSectionFacade.guardarStep1(new Step1Facade.SaveStep1Command(
                isPermitirIngresoManual(),
                getEmpleadoSel(),
                getNoPersonaSel(),
                getFicha(),
                getPersonaAux(),
                this));

        pacienteSectionFacade.applyPacienteUiResult(this, result.preUiResult);
        setFicha(result.ficha);
        setEmpleadoSel(result.empleadoSel);
        setPersonaAux(result.personaAux);
        setSignos(result.signos);
        pacienteSectionFacade.applyPacienteUiResult(this, result.postUiResult);
    }

    public void guardarStep2() {
        wizardStepActionService.execute(new WizardStepActionService.ExecuteStepActionCommand(
                "guardarStep2",
                this::validarStep2,
                this::saveStep2,
                null,
                () -> messageService.addMsg(FacesMessage.SEVERITY_INFO, "Step 2",
                        "Riesgos laborales guardados correctamente (encabezado + detalle)."),
                LOG,
                wizardViewState.getActiveStep(),
                getNoPersonaSel(),
                getCedulaBusqueda()));
    }

    private void saveStep2() {
        final Date now = new Date();

        try {
            step2FormModel.setFichaRiesgo(wizardSectionFacade.guardarStep2(new Step2RiskCommand(
                    getFicha(),
                    step2FormModel.getFichaRiesgo(),
                    step2FormModel.getActividadesLab(),
                    step2FormModel.getMedidasPreventivas(),
                    step2FormModel.getRiesgos(),
                    step2FormModel.getOtrosRiesgos(),
                    now
            )));
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException(ex.getMessage());
        }

        wizardSectionFacade.registrarAuditoria("GUARDAR_STEP2", "FICHA_RIESGO / FICHA_RIESGO_DET", "*",
                "Step 2 guardado. ID_FICHA=" + getFicha().getIdFicha());
    }

    public void guardarStep3() {
        wizardStepActionService.execute(new WizardStepActionService.ExecuteStepActionCommand(
                "guardarStep3",
                this::validarStep3,
                this::saveStep3,
                null,
                () -> messageService.addMsg(FacesMessage.SEVERITY_INFO, "OK", "Step 3 guardado correctamente."),
                LOG,
                wizardViewState.getActiveStep(),
                getNoPersonaSel(),
                getCedulaBusqueda()));
    }

    private void saveStep3() {
        ensureFichaSavedOrThrow();
        try {
            pacienteSectionFacade.asegurarPacienteAsignado(this,
                    isPermitirIngresoManual(),
                    getEmpleadoSel(),
                    getNoPersonaSel(),
                    getPersonaAux(),
                    getFicha());
        } catch (IllegalStateException ex) {
            fail(ex.getMessage());
        }

        final Date now = new Date();

        try {
            var step3Data = step3ViewDataAssembler.capture(
                    this,
                    now,
                    () -> pacienteSectionFacade.asegurarPersonaAuxPersistida(this),
                    () -> centroMedicoFormStateService.ensureActLabSize(this, H_ROWS));
            setFicha(wizardSectionFacade.guardarStep3(step3CommandAssembler.toCommand(step3Data)));
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        wizardSectionFacade.registrarAuditoria("GUARDAR_STEP3", "FICHA_OCUPACIONAL / H / I / J / K", "*",
                "Step 3 guardado. ID_FICHA=" + getFicha().getIdFicha());
    }

    private void ensureFichaSavedOrThrow() {
        if (getFicha() == null || getFicha().getIdFicha() == null) {
            throw new BusinessValidationException("Primero debe existir y estar guardada la ficha (ID_FICHA).");
        }
    }

    // =========================
    // PDF - FICHA OCUPACIONAL
    // =========================
    public void prepararVistaPreviaFicha() {
        pdfPreviewFacade.prepararVistaPreviaFicha(
                pdfPreviewCommandFactory.buildPrepareVistaPreviaFichaCommand(buildPdfCommandContext()));
    }

    // =========================
    // PDF - CERTIFICADO MÉDICO
    // =========================
    public void prepararVistaPreviaCertificado() {
        pdfPreviewFacade.prepararVistaPreviaCertificado(
                pdfPreviewCommandFactory.buildPrepareVistaPreviaCertificadoCommand(buildPdfCommandContext()));
    }

    // =========================
    // PDF - VISTA PREVIA GENERAL
    // =========================
    public void prepararVistaPrevia() {
        pdfPreviewFacade.prepararVistaPrevia(
                pdfPreviewCommandFactory.buildPrepareVistaPreviaCommand(buildPdfCommandContext()));
    }

    public void limpiarVistaPrevia() {
        pdfPreviewFacade.limpiarVistaPrevia(new PdfPreviewFacade.LimpiarVistaPreviaCommand(
                pdfSessionStore,
                pdfPreviewState,
                this::setFicha,
                wizardViewState::setActiveStep,
                this::setMostrarDlgCedula));
    }

    private PdfPreviewCommandFactory.PdfCommandContext buildPdfCommandContext() {
        return pdfCommandContextBuilder.build(
                controllerActionTemplate,
                pdfPreviewState,
                pdfSessionStore,
                this,
                LOG,
                wizardViewState.getActiveStep(),
                getNoPersonaSel(),
                getCedulaBusqueda(),
                () -> pacienteSectionFacade.asegurarPersonaAuxPersistida(this),
                this::syncCamposDesdeObjetosInternal,
                this::recalcularIMC,
                this::verificarFichaCompleta,
                fecha -> diagnosticoFormModel.setFechaEmision(fecha),
                H_ROWS,
                this::setFicha,
                wizardViewState::setActiveStep,
                this::setMostrarDlgCedula);
    }

    private void applyStep4State(CentroMedicoWizardNavigationCoordinator.Step4UiState state) {
        pdfSectionFacade.applyStep4State(
                state,
                pdfPreviewState,
                this::setFicha,
                wizardViewState::setActiveStep,
                this::setMostrarDlgCedula);
    }

    // =========================
    // CIE10 Y DIAGNÓSTICO
    // =========================
    public void syncTipoEvaluacion() {
        setTipoEvaluacion(step1FormModel.getTipoEval());
    }

    private void syncCie10PrincipalFromK() {
        diagnosticoSectionFacade.syncCie10PrincipalFromK(this);
    }

    public void onCie10BlurCodigo(int index) {
        diagnosticoSectionFacade.onCie10BlurCodigo(this, index);
    }

    public void onCie10FilaSelect(int idx) {
        diagnosticoSectionFacade.onCie10FilaSelect(this, idx);
    }

    public List<Cie10> completarCie10(String query) {
        return diagnosticoSectionFacade.completarCie10(query);
    }

    public List<Cie10> completarCie10PorCodigo(String query) {
        return diagnosticoSectionFacade.completarCie10PorCodigo(query);
    }

    public List<Cie10> completarCie10PorDescripcion(String query) {
        return diagnosticoSectionFacade.completarCie10PorDescripcion(query);
    }

    public void onCie10CodigoSelect(SelectEvent event) {
        diagnosticoSectionFacade.onCie10CodigoSelect(this, event);
    }

    public void onCie10CodigoBlur() {
        diagnosticoSectionFacade.onCie10CodigoBlur(this);
    }

    public void onCie10DescripcionSelect(SelectEvent event) {
        diagnosticoSectionFacade.onCie10DescripcionSelect(this, event);
    }

    public void onCie10DescripcionBlur() {
        diagnosticoSectionFacade.onCie10DescripcionBlur(this);
    }

    private Cie10 inferCie10PrincipalFromListaK() {
        return diagnosticoSectionFacade.inferCie10PrincipalFromListaK(this);
    }

    public List<String> completarCie10FilaPorCodigo(String query) {
        return diagnosticoSectionFacade.completarCie10FilaPorCodigo(query);
    }

    public List<String> completarCie10FilaPorDescripcion(String query) {
        return diagnosticoSectionFacade.completarCie10FilaPorDescripcion(query);
    }

    public void onKCieCodigoSelect(SelectEvent<String> event) {
        diagnosticoSectionFacade.onKCieCodigoSelect(this, event);
    }

    public void onKCieCodigoBlur(AjaxBehaviorEvent event) {
        diagnosticoSectionFacade.onKCieCodigoBlur(this, event);
    }

    public void onKDescSelect(SelectEvent<String> event) {
        diagnosticoSectionFacade.onKDescSelect(this, event);
    }

    public void onKDescBlur(AjaxBehaviorEvent event) {
        diagnosticoSectionFacade.onKDescBlur(this, event);
    }

    public void onKTipoChange(AjaxBehaviorEvent event) {
        diagnosticoSectionFacade.onKTipoChange(this, event);
    }

    public void abrirDialogoDiagnostico(AjaxBehaviorEvent event) {
        diagnosticoSectionFacade.abrirDialogoDiagnostico(this, event);
    }

    public void aceptarDialogoDiagnostico() {
        diagnosticoSectionFacade.aceptarDialogoDiagnostico(this);
    }

    public void cerrarDialogoDiagnostico() {
        diagnosticoSectionFacade.cerrarDialogoDiagnostico();
    }

    // =========================
    // GESTIÓN DE PACIENTE / CÉDULA
    // =========================
    public void onBuscarPorCedulaRh() {
        pacienteSectionFacade.onBuscarPorCedulaRh(this, LOG);
    }

    public void buscarCedula() {
        pacienteSectionFacade.buscarCedula(this, LOG);
    }

    public void prepararIngresoManual() {
        pacienteSectionFacade.prepararIngresoManual(this);
    }

    public void abrirPersonaAuxManual() {
        pacienteSectionFacade.abrirPersonaAuxManual(this);
    }

    public void guardarPersonaAuxYUsar() {
        pacienteSectionFacade.guardarPersonaAuxYUsar(this, LOG);
    }

    // =========================
    // DIÁLOGO DE CÉDULA
    // =========================
    public void onDlgCedulaShown() {
        setMostrarDlgCedula(cedulaDialogControllerSupport.resetDialogVisibility());
    }

    public void onDlgCedulaHide() {
        setMostrarDlgCedula(cedulaDialogControllerSupport.resetDialogVisibility());
    }

    public void onDlgCedulaClose() {
        setMostrarDlgCedula(cedulaDialogControllerSupport.resetDialogVisibility());
    }

    public void autoOpenCedulaIfNeeded() {
        CedulaDialogStateService.AutoOpenState state = cedulaDialogControllerSupport.autoOpenIfNeeded(
                cedulaDialogStateService,
                wizardViewState.getActiveStep(),
                isMostrarDlgCedula(),
                wizardViewState.isCedulaDlgAutoOpened());
        wizardViewState.setCedulaDlgAutoOpened(state.isAutoOpened());
    }

    public void consumirAutoOpenCedulaDlg() {
        CedulaDialogStateService.AutoOpenState state = cedulaDialogControllerSupport.consumeAutoOpen(
                cedulaDialogStateService,
                wizardViewState.getActiveStep(),
                getEmpleadoSel() == null,
                wizardViewState.isCedulaDlgAutoOpened());
        wizardViewState.setCedulaDlgAutoOpened(state.isAutoOpened());
    }

    // =========================
    // CÁLCULOS Y UTILIDADES
    // =========================
    public void onFechaNacimientoSelect(SelectEvent e) {
        pacienteFormData.setFechaNacimiento((java.util.Date) e.getObject());
        recalculateEdadAndNotify();
    }

    public void onFechaNacimientoChange() {
        recalculateEdadAndNotify();
    }

    private void recalculateEdadAndNotify() {
        pacienteFormData.setEdad(reactiveUiService.recalculateEdad(pacienteFormData.getFechaNacimiento(), calcUtil));
        messageService.addMsg(FacesMessage.SEVERITY_INFO, "Cálculo de edad",
                reactiveUiService.buildEdadCalculationMessage(pacienteFormData.getEdad()));
    }

    public void calcularEdad() {
        pacienteFormData.setEdad(calcUtil.calcularEdad(pacienteFormData.getFechaNacimiento()));
    }

    public Date getFechaMaximaNacimiento() {
        return calcUtil.getFechaMaximaNacimiento();
    }

    public void validarEdadMinima() {
        if (!reactiveUiService.validarEdadMinima(pacienteFormData.getEdad(), calcUtil)) {
            messageService.error("La edad debe ser ≥ 18 años");
            pacienteFormData.setFechaNacimiento(null);
            pacienteFormData.setEdad(null);
        }
    }

    public void recalcularIMC() {
        signosVitalesFormModel.setImc(reactiveUiService.recalculateImc(signosVitalesFormModel.getPeso(), signosVitalesFormModel.getTallaCm(), calcUtil));
    }

    // =========================
    // CONSUMO, HÁBITOS Y ESTRUCTURAS AUXILIARES
    // =========================
    public void onNoConsumeChange(int idx) {
        reactiveUiService.onNoConsumeChange(
                habitosConsumoFormModel.getConsNoConsume(),
                habitosConsumoFormModel.getConsExConsumidor(),
                habitosConsumoFormModel.getConsTiempoConsumoMeses(),
                habitosConsumoFormModel.getConsTiempoAbstinenciaMeses(),
                idx);
    }

    // =========================
    // ATENCIÓN PRIORITARIA - TOGGLES
    // =========================
    public void onToggleDiscapacidad() {
        CentroMedicoReactiveUiService.AttentionPriorityState state = reactiveUiService.onToggleDiscapacidad(
                atencionPrioritariaModel.isDiscapacidad(),
                atencionPrioritariaModel.getDiscapTipo(),
                atencionPrioritariaModel.getDiscapDesc(),
                atencionPrioritariaModel.getDiscapPorc());
        if (!atencionPrioritariaModel.isDiscapacidad()) {
            atencionPrioritariaModel.setDiscapTipo(state.getDiscapTipo());
            atencionPrioritariaModel.setDiscapDesc(state.getDiscapDesc());
            atencionPrioritariaModel.setDiscapPorc(state.getDiscapPorc());
        }
    }

    public void onToggleCatastrofica() {
        CentroMedicoReactiveUiService.AttentionPriorityState state = reactiveUiService.onToggleCatastrofica(
                atencionPrioritariaModel.isCatastrofica(),
                atencionPrioritariaModel.getCatasDiagnostico(),
                atencionPrioritariaModel.getCatasCalificada());
        if (!atencionPrioritariaModel.isCatastrofica()) {
            atencionPrioritariaModel.setCatasDiagnostico(state.getCatasDiagnostico());
            atencionPrioritariaModel.setCatasCalificada(state.getCatasCalificada());
        }
    }

    // =========================
    // RECARGA DE FICHA
    // =========================
    public void reloadFichaDesdeBd() {
        if (getFicha() == null || getFicha().getIdFicha() == null) {
            return;
        }
        setFicha(wizardSectionFacade.recargarFicha(getFicha().getIdFicha()));
    }

    public String getStepProcessId() {
        return "@([id$=" + wizardViewState.getActiveStep() + "])";
    }

    public String getProcessStepId() {
        return ":wiz:" + wizardViewState.getActiveStep();
    }

    public long getTs() {
        return System.currentTimeMillis();
    }

    // =========================
    // UTILIDADES DE SINCRONIZACIÓN PDF
    // =========================
    private String obtenerTipoEvaluacionPdf() {
        return pdfSectionFacade.obtenerTipoEvaluacionPdf(step1FormModel.getTipoEval(), getTipoEvaluacion());
    }

    private void syncCamposDesdeObjetosInternal() {
        pdfSectionFacade.syncCamposDesdeObjetosInternal(
                pdfFichaInputAssembler.buildSyncCamposDesdeObjetosInput(this, fichaPdfContextAssembler, fichaPdfDataMapper));
    }

    // =========================
    // UTILIDADES DE CARGA DE DATOS PDF
    // =========================
    void cargarAtencionPrioritaria(Map<String, String> rep) {
        pdfSectionFacade.cargarAtencionPrioritaria(
                getFicha(),
                atencionPrioritariaModel.isDiscapacidad(),
                atencionPrioritariaModel.isCatastrofica(),
                atencionPrioritariaModel.isEmbarazada(),
                atencionPrioritariaModel.isLactancia(),
                atencionPrioritariaModel.isAdultoMayor(),
                rep,
                LOG);
    }

    private void cargarActividadLaboralArrays(Map<String, String> rep) {
        pdfSectionFacade.cargarActividadLaboralArrays(
                H_ROWS,
                actividadLaboralFormModel.getActLabCentroTrabajo(),
                actividadLaboralFormModel.getActLabActividad(),
                actividadLaboralFormModel.getActLabTiempo(),
                actividadLaboralFormModel.getActLabTrabajoAnterior(),
                actividadLaboralFormModel.getActLabTrabajoActual(),
                actividadLaboralFormModel.getActLabIncidenteChk(),
                actividadLaboralFormModel.getActLabAccidenteChk(),
                actividadLaboralFormModel.getActLabEnfermedadChk(),
                actividadLaboralFormModel.getIessSi(),
                actividadLaboralFormModel.getIessNo(),
                actividadLaboralFormModel.getIessFecha(),
                actividadLaboralFormModel.getIessEspecificar(),
                actividadLaboralFormModel.getActLabObservaciones(),
                rep);
    }

    public List<String> completarKCieStrings(String query) {
    return diagnosticoSectionFacade.completarKCieStrings(query);
}

public List<String> completarKDescStrings(String query) {
    return diagnosticoSectionFacade.completarKDescStrings(query);
}

    // =========================
    // GETTERS Y SETTERS
    // =========================
    public Date getFechaNacimiento() {
        return pacienteFormData.getFechaNacimiento();
    }

    public void setFechaNacimiento(Date f) {
        pacienteFormData.setFechaNacimiento(f);
        pacienteFormData.setEdad(calcUtil.calcularEdad(f));
    }

    public boolean isMostrarDlgCedula() {
        return pacienteViewState.isMostrarDlgCedula();
    }

    public void setMostrarDlgCedula(boolean mostrarDlgCedula) {
        pacienteViewState.setMostrarDlgCedula(mostrarDlgCedula);
    }

    public PersonaAux getPersonaAux() {
        if (fichaContext.getPersonaAux() == null) {
            fichaContext.setPersonaAux(new PersonaAux());
        }
        return fichaContext.getPersonaAux();
    }

    public void setPersonaAux(PersonaAux personaAux) {
        fichaContext.setPersonaAux(personaAux);
    }

    public String getStepActual() {
        return wizardViewState.getActiveStep();
    }

    public void setStepActual(String stepActual) {
        wizardViewState.setActiveStep(stepActual);
    }

    public String getActiveStep() {
        return wizardViewState.getActiveStep();
    }

    public void setActiveStep(String activeStep) {
        wizardViewState.setActiveStep(activeStep);
    }



    public List<String> getTipoAct() {
        if (actividadLaboralFormModel.getTipoAct() == null) {
            actividadLaboralFormModel.setTipoAct(new ArrayList<>());
        }
        return actividadLaboralFormModel.getTipoAct();
    }

    public void setTipoAct(List<String> tipoAct) {
        actividadLaboralFormModel.setTipoAct(tipoAct);
    }

    public List<Date> getFechaAct() {
        if (actividadLaboralFormModel.getFechaAct() == null) {
            actividadLaboralFormModel.setFechaAct(new ArrayList<>());
        }
        return actividadLaboralFormModel.getFechaAct();
    }

    public void setFechaAct(List<Date> fechaAct) {
        actividadLaboralFormModel.setFechaAct(fechaAct);
    }

    public List<String> getDescAct() {
        if (actividadLaboralFormModel.getDescAct() == null) {
            actividadLaboralFormModel.setDescAct(new ArrayList<>());
        }
        return actividadLaboralFormModel.getDescAct();
    }

    public void setDescAct(List<String> descAct) {
        actividadLaboralFormModel.setDescAct(descAct);
    }

    public int getStepIndex() {
        return wizardViewState.getStepIndex();
    }

    public void setStepIndex(int stepIndex) {
        wizardViewState.setStepIndex(stepIndex);
    }

    public Integer[] getConsTiempoConsumo() {
        return habitosConsumoFormModel.getConsTiempoConsumoMeses();
    }

    public void setConsTiempoConsumo(Integer[] v) {
        habitosConsumoFormModel.setConsTiempoConsumoMeses(v);
    }

    public Integer[] getConsTiempoAbstinencia() {
        return habitosConsumoFormModel.getConsTiempoAbstinenciaMeses();
    }

    public void setConsTiempoAbstinencia(Integer[] v) {
        habitosConsumoFormModel.setConsTiempoAbstinenciaMeses(v);
    }

    public String getObsExamenFisico() {
        return examenFisicoFormModel.getObsExamenFisico();
    }

    public void setObsExamenFisico(String obsExamenFisico) {
        examenFisicoFormModel.setObsExamenFisico(obsExamenFisico);
    }

    public String getConsObservacion() {
        return habitosConsumoFormModel.getConsumoVidaCondObs();
    }

    public void setConsObservacion(String v) {
        habitosConsumoFormModel.setConsumoVidaCondObs(v);
    }

    public String getNRealizaEvaluacion() {
        return diagnosticoFormModel.getnRealizaEvaluacion();
    }

    public void setNRealizaEvaluacion(String nRealizaEvaluacion) {
        diagnosticoFormModel.setnRealizaEvaluacion(nRealizaEvaluacion);
    }

    public String getNRelacionTrabajo() {
        return diagnosticoFormModel.getnRelacionTrabajo();
    }

    public void setNRelacionTrabajo(String nRelacionTrabajo) {
        diagnosticoFormModel.setnRelacionTrabajo(nRelacionTrabajo);
    }

    public String getNObsRetiro() {
        return diagnosticoFormModel.getnObsRetiro();
    }

    public void setNObsRetiro(String nObsRetiro) {
        diagnosticoFormModel.setnObsRetiro(nObsRetiro);
    }





    public void setEnfermedadActual(String enfermedadActual) {
        examenFisicoFormModel.setEnfermedadActual(enfermedadActual);
    }

    public void setExfPielCicatrices(String exfPielCicatrices) {
        examenFisicoFormModel.setExfPielCicatrices(exfPielCicatrices);
    }

    public void setExfOjosParpados(String exfOjosParpados) {
        examenFisicoFormModel.setExfOjosParpados(exfOjosParpados);
    }

    public void setExfOjosConjuntivas(String exfOjosConjuntivas) {
        examenFisicoFormModel.setExfOjosConjuntivas(exfOjosConjuntivas);
    }

    public void setExfOjosPupilas(String exfOjosPupilas) {
        examenFisicoFormModel.setExfOjosPupilas(exfOjosPupilas);
    }

    public void setExfOjosCornea(String exfOjosCornea) {
        examenFisicoFormModel.setExfOjosCornea(exfOjosCornea);
    }

    public void setExfOjosMotilidad(String exfOjosMotilidad) {
        examenFisicoFormModel.setExfOjosMotilidad(exfOjosMotilidad);
    }

    public void setExfOidoConducto(String exfOidoConducto) {
        examenFisicoFormModel.setExfOidoConducto(exfOidoConducto);
    }

    public void setExfOidoPabellon(String exfOidoPabellon) {
        examenFisicoFormModel.setExfOidoPabellon(exfOidoPabellon);
    }

    public void setExfOidoTimpanos(String exfOidoTimpanos) {
        examenFisicoFormModel.setExfOidoTimpanos(exfOidoTimpanos);
    }

    public void setExfOroLabios(String exfOroLabios) {
        examenFisicoFormModel.setExfOroLabios(exfOroLabios);
    }

    public void setExfOroLengua(String exfOroLengua) {
        examenFisicoFormModel.setExfOroLengua(exfOroLengua);
    }

    public void setExfOroFaringe(String exfOroFaringe) {
        examenFisicoFormModel.setExfOroFaringe(exfOroFaringe);
    }

    public void setExfOroAmigdalas(String exfOroAmigdalas) {
        examenFisicoFormModel.setExfOroAmigdalas(exfOroAmigdalas);
    }

    public void setExfOroDentadura(String exfOroDentadura) {
        examenFisicoFormModel.setExfOroDentadura(exfOroDentadura);
    }

    public void setExfNarizTabique(String exfNarizTabique) {
        examenFisicoFormModel.setExfNarizTabique(exfNarizTabique);
    }

    public void setExfNarizCornetes(String exfNarizCornetes) {
        examenFisicoFormModel.setExfNarizCornetes(exfNarizCornetes);
    }

    public void setExfNarizMucosas(String exfNarizMucosas) {
        examenFisicoFormModel.setExfNarizMucosas(exfNarizMucosas);
    }

    public void setExfNarizSenos(String exfNarizSenos) {
        examenFisicoFormModel.setExfNarizSenos(exfNarizSenos);
    }

    public void setExfCuelloTiroides(String exfCuelloTiroides) {
        examenFisicoFormModel.setExfCuelloTiroides(exfCuelloTiroides);
    }

    public void setExfCuelloMovilidad(String exfCuelloMovilidad) {
        examenFisicoFormModel.setExfCuelloMovilidad(exfCuelloMovilidad);
    }

    public void setExfToraxMamas(String exfToraxMamas) {
        examenFisicoFormModel.setExfToraxMamas(exfToraxMamas);
    }

    public void setExfToraxPulmones(String exfToraxPulmones) {
        examenFisicoFormModel.setExfToraxPulmones(exfToraxPulmones);
    }

    public void setExfToraxCorazon(String exfToraxCorazon) {
        examenFisicoFormModel.setExfToraxCorazon(exfToraxCorazon);
    }

    public void setExfToraxParrilla(String exfToraxParrilla) {
        examenFisicoFormModel.setExfToraxParrilla(exfToraxParrilla);
    }

    public void setExfAbdomenVisceras(String exfAbdomenVisceras) {
        examenFisicoFormModel.setExfAbdomenVisceras(exfAbdomenVisceras);
    }

    public void setExfAbdomenPared(String exfAbdomenPared) {
        examenFisicoFormModel.setExfAbdomenPared(exfAbdomenPared);
    }

    public void setExfColumnaFlexibilidad(String exfColumnaFlexibilidad) {
        examenFisicoFormModel.setExfColumnaFlexibilidad(exfColumnaFlexibilidad);
    }

    public void setExfColumnaDesviacion(String exfColumnaDesviacion) {
        examenFisicoFormModel.setExfColumnaDesviacion(exfColumnaDesviacion);
    }

    public void setExfColumnaDolor(String exfColumnaDolor) {
        examenFisicoFormModel.setExfColumnaDolor(exfColumnaDolor);
    }

    public void setExfPelvisPelvis(String exfPelvisPelvis) {
        examenFisicoFormModel.setExfPelvisPelvis(exfPelvisPelvis);
    }

    public void setExfPelvisGenitales(String exfPelvisGenitales) {
        examenFisicoFormModel.setExfPelvisGenitales(exfPelvisGenitales);
    }

    public void setExfExtVascular(String exfExtVascular) {
        examenFisicoFormModel.setExfExtVascular(exfExtVascular);
    }

    public void setExfExtSup(String exfExtSup) {
        examenFisicoFormModel.setExfExtSup(exfExtSup);
    }

    public void setExfExtInf(String exfExtInf) {
        examenFisicoFormModel.setExfExtInf(exfExtInf);
    }

    public void setExfNeuroFuerza(String exfNeuroFuerza) {
        examenFisicoFormModel.setExfNeuroFuerza(exfNeuroFuerza);
    }

    public void setExfNeuroSensibilidad(String exfNeuroSensibilidad) {
        examenFisicoFormModel.setExfNeuroSensibilidad(exfNeuroSensibilidad);
    }

    public void setExfNeuroMarcha(String exfNeuroMarcha) {
        examenFisicoFormModel.setExfNeuroMarcha(exfNeuroMarcha);
    }

    public void setExfNeuroReflejos(String exfNeuroReflejos) {
        examenFisicoFormModel.setExfNeuroReflejos(exfNeuroReflejos);
    }

    public Integer getAbortos() {
        return ginecoObstetricoFormModel.getAbortos();
    }

    public void setAbortos(Integer abortos) {
        ginecoObstetricoFormModel.setAbortos(abortos);
    }

    public List<Boolean> getActLabAccidenteChk() {
        return actividadLaboralFormModel.getActLabAccidenteChk();
    }

    public void setActLabAccidenteChk(List<Boolean> actLabAccidenteChk) {
        actividadLaboralFormModel.setActLabAccidenteChk(actLabAccidenteChk);
    }

    public List<String> getActLabActividad() {
        return actividadLaboralFormModel.getActLabActividad();
    }

    public void setActLabActividad(List<String> actLabActividad) {
        actividadLaboralFormModel.setActLabActividad(actLabActividad);
    }

    public List<String> getActLabCentroTrabajo() {
        return actividadLaboralFormModel.getActLabCentroTrabajo();
    }

    public void setActLabCentroTrabajo(List<String> actLabCentroTrabajo) {
        actividadLaboralFormModel.setActLabCentroTrabajo(actLabCentroTrabajo);
    }

    public List<Boolean> getActLabEnfermedadChk() {
        return actividadLaboralFormModel.getActLabEnfermedadChk();
    }

    public void setActLabEnfermedadChk(List<Boolean> actLabEnfermedadChk) {
        actividadLaboralFormModel.setActLabEnfermedadChk(actLabEnfermedadChk);
    }

    public List<Boolean> getActLabIncidenteChk() {
        return actividadLaboralFormModel.getActLabIncidenteChk();
    }

    public void setActLabIncidenteChk(List<Boolean> actLabIncidenteChk) {
        actividadLaboralFormModel.setActLabIncidenteChk(actLabIncidenteChk);
    }

    public List<String> getActLabObservaciones() {
        return actividadLaboralFormModel.getActLabObservaciones();
    }

    public void setActLabObservaciones(List<String> actLabObservaciones) {
        actividadLaboralFormModel.setActLabObservaciones(actLabObservaciones);
    }

    public List<String> getActLabRows() {
        return actividadLaboralFormModel.getActLabRows();
    }

    public void setActLabRows(List<String> actLabRows) {
        actividadLaboralFormModel.setActLabRows(actLabRows);
    }

    public List<String> getActLabTiempo() {
        return actividadLaboralFormModel.getActLabTiempo();
    }

    public void setActLabTiempo(List<String> actLabTiempo) {
        actividadLaboralFormModel.setActLabTiempo(actLabTiempo);
    }

    public List<Boolean> getActLabTrabajoActual() {
        return actividadLaboralFormModel.getActLabTrabajoActual();
    }

    public void setActLabTrabajoActual(List<Boolean> actLabTrabajoActual) {
        actividadLaboralFormModel.setActLabTrabajoActual(actLabTrabajoActual);
    }

    public List<Boolean> getActLabTrabajoAnterior() {
        return actividadLaboralFormModel.getActLabTrabajoAnterior();
    }

    public void setActLabTrabajoAnterior(List<Boolean> actLabTrabajoAnterior) {
        actividadLaboralFormModel.setActLabTrabajoAnterior(actLabTrabajoAnterior);
    }



    public String[] getAfCual() {
        return habitosConsumoFormModel.getAfCual();
    }

    public void setAfCual(String[] afCual) {
        habitosConsumoFormModel.setAfCual(afCual);
    }

    public String[] getAfTiempo() {
        return habitosConsumoFormModel.getAfTiempo();
    }

    public void setAfTiempo(String[] afTiempo) {
        habitosConsumoFormModel.setAfTiempo(afTiempo);
    }















    public String getApellido1() {
        return pacienteFormData.getApellido1();
    }

    public void setApellido1(String apellido1) {
        pacienteFormData.setApellido1(apellido1);
    }

    public String getApellido2() {
        return pacienteFormData.getApellido2();
    }

    public void setApellido2(String apellido2) {
        pacienteFormData.setApellido2(apellido2);
    }





    public String getCedulaBusqueda() {
        return pacienteViewState.getCedulaBusqueda();
    }

    public void setCedulaBusqueda(String cedulaBusqueda) {
        pacienteViewState.setCedulaBusqueda(cedulaBusqueda);
    }

    public boolean isCertificadoListo() {
        return pdfPreviewState.isCertificadoListo();
    }

    public void setCertificadoListo(boolean certificadoListo) {
        pdfPreviewState.setCertificadoListo(certificadoListo);
    }

    public Integer getCesareas() {
        return ginecoObstetricoFormModel.getCesareas();
    }

    public void setCesareas(Integer cesareas) {
        ginecoObstetricoFormModel.setCesareas(cesareas);
    }



    public Boolean[] getConsExConsumidor() {
        return habitosConsumoFormModel.getConsExConsumidor();
    }

    public void setConsExConsumidor(Boolean[] consExConsumidor) {
        habitosConsumoFormModel.setConsExConsumidor(consExConsumidor);
    }

    public Boolean[] getConsNoConsume() {
        return habitosConsumoFormModel.getConsNoConsume();
    }

    public void setConsNoConsume(Boolean[] consNoConsume) {
        habitosConsumoFormModel.setConsNoConsume(consNoConsume);
    }

    public String getDetalleObservaciones() {
        return diagnosticoFormModel.getDetalleObservaciones();
    }

    public void setDetalleObservaciones(String detalleObservaciones) {
        diagnosticoFormModel.setDetalleObservaciones(detalleObservaciones);
    }

    public Integer getEdad() {
        return pacienteFormData.getEdad();
    }

    public void setEdad(Integer edad) {
        pacienteFormData.setEdad(edad);
    }

    public List<Date> getExamFecha() {
        return examenesComplementariosFormModel.getExamFecha();
    }

    public void setExamFecha(List<Date> examFecha) {
        examenesComplementariosFormModel.setExamFecha(examFecha);
    }

    public List<String> getExamNombre() {
        return examenesComplementariosFormModel.getExamNombre();
    }

    public void setExamNombre(List<String> examNombre) {
        examenesComplementariosFormModel.setExamNombre(examNombre);
    }

    public List<String> getExamResultado() {
        return examenesComplementariosFormModel.getExamResultado();
    }

    public void setExamResultado(List<String> examResultado) {
        examenesComplementariosFormModel.setExamResultado(examResultado);
    }

    public String getExamenReproMasculino() {
        return ginecoObstetricoFormModel.getExamenReproMasculino();
    }

    public void setExamenReproMasculino(String examenReproMasculino) {
        ginecoObstetricoFormModel.setExamenReproMasculino(examenReproMasculino);
    }









    public FichaOcupacional getFicha() {
        return fichaContext.getFicha();
    }

    public void setFicha(FichaOcupacional ficha) {
        fichaContext.setFicha(ficha);
    }





    public Integer getGestas() {
        return ginecoObstetricoFormModel.getGestas();
    }

    public void setGestas(Integer gestas) {
        ginecoObstetricoFormModel.setGestas(gestas);
    }



    public List<String> getIessEspecificar() {
        return actividadLaboralFormModel.getIessEspecificar();
    }

    public void setIessEspecificar(List<String> iessEspecificar) {
        actividadLaboralFormModel.setIessEspecificar(iessEspecificar);
    }

    public List<Date> getIessFecha() {
        return actividadLaboralFormModel.getIessFecha();
    }

    public void setIessFecha(List<Date> iessFecha) {
        actividadLaboralFormModel.setIessFecha(iessFecha);
    }

    public List<Boolean> getIessNo() {
        return actividadLaboralFormModel.getIessNo();
    }

    public void setIessNo(List<Boolean> iessNo) {
        actividadLaboralFormModel.setIessNo(iessNo);
    }

    public List<Boolean> getIessSi() {
        return actividadLaboralFormModel.getIessSi();
    }

    public void setIessSi(List<Boolean> iessSi) {
        actividadLaboralFormModel.setIessSi(iessSi);
    }





    public List<ConsultaDiagnostico> getListaDiag() {
        return step3FormModel.getListaDiag();
    }

    public void setListaDiag(List<ConsultaDiagnostico> listaDiag) {
        step3FormModel.setListaDiag(listaDiag);
    }

    public Integer[] getMedCant() {
        return habitosConsumoFormModel.getMedCant();
    }

    public void setMedCant(Integer[] medCant) {
        habitosConsumoFormModel.setMedCant(medCant);
    }

    public String[] getMedCual() {
        return habitosConsumoFormModel.getMedCual();
    }

    public void setMedCual(String[] medCual) {
        habitosConsumoFormModel.setMedCual(medCual);
    }

    public String getMedicoCodigo() {
        return diagnosticoFormModel.getMedicoCodigo();
    }

    public void setMedicoCodigo(String medicoCodigo) {
        diagnosticoFormModel.setMedicoCodigo(medicoCodigo);
    }

    public String getMedicoNombre() {
        return diagnosticoFormModel.getMedicoNombre();
    }

    public void setMedicoNombre(String medicoNombre) {
        diagnosticoFormModel.setMedicoNombre(medicoNombre);
    }



    public String getNombre1() {
        return pacienteFormData.getNombre1();
    }

    public void setNombre1(String nombre1) {
        pacienteFormData.setNombre1(nombre1);
    }

    public String getNombre2() {
        return pacienteFormData.getNombre2();
    }

    public void setNombre2(String nombre2) {
        pacienteFormData.setNombre2(nombre2);
    }

    public String getObsJ() {
        return habitosConsumoFormModel.getObsJ();
    }

    public void setObsJ(String obsJ) {
        habitosConsumoFormModel.setObsJ(obsJ);
    }




    public Integer getPartos() {
        return ginecoObstetricoFormModel.getPartos();
    }

    public void setPartos(Integer partos) {
        ginecoObstetricoFormModel.setPartos(partos);
    }

    public String getPdfToken() {
        return pdfPreviewState.getPdfTokenCertificado();
    }

    public void setPdfToken(String pdfTokenCertificado) {
        pdfPreviewState.setPdfTokenCertificado(pdfTokenCertificado);
    }



    public boolean isPermitirIngresoManual() {
        return pacienteViewState.isPermitirIngresoManual();
    }

    public void setPermitirIngresoManual(boolean permitirIngresoManual) {
        pacienteViewState.setPermitirIngresoManual(permitirIngresoManual);
    }



    public String getRecomendaciones() {
        return diagnosticoFormModel.getRecomendaciones();
    }

    public void setRecomendaciones(String recomendaciones) {
        diagnosticoFormModel.setRecomendaciones(recomendaciones);
    }




    public String getSexo() {
        return pacienteFormData.getSexo();
    }

    public void setSexo(String sexo) {
        pacienteFormData.setSexo(sexo);
    }

    public String getGrupoSanguineo() {
        return step1FormModel.getGrupoSanguineo();
    }

    public void setGrupoSanguineo(String grupoSanguineo) {
        step1FormModel.setGrupoSanguineo(grupoSanguineo);
    }





    public Integer getTiempoReproMasculino() {
        return ginecoObstetricoFormModel.getTiempoReproMasculino();
    }

    public void setTiempoReproMasculino(Integer tiempoReproMasculino) {
        ginecoObstetricoFormModel.setTiempoReproMasculino(tiempoReproMasculino);
    }







    public String getDialogDiagnosticoCodigo() {
        return step3FormModel.getDialogDiagnosticoCodigo();
    }

    public void setDialogDiagnosticoCodigo(String dialogDiagnosticoCodigo) {
        step3FormModel.setDialogDiagnosticoCodigo(dialogDiagnosticoCodigo);
    }

    public String getDialogDiagnosticoDescripcion() {
        return step3FormModel.getDialogDiagnosticoDescripcion();
    }

    public void setDialogDiagnosticoDescripcion(String dialogDiagnosticoDescripcion) {
        step3FormModel.setDialogDiagnosticoDescripcion(dialogDiagnosticoDescripcion);
    }

    public Integer getDialogDiagnosticoIdx() {
        return step3FormModel.getDialogDiagnosticoIdx();
    }

    public void setDialogDiagnosticoIdx(Integer dialogDiagnosticoIdx) {
        step3FormModel.setDialogDiagnosticoIdx(dialogDiagnosticoIdx);
    }

    public boolean isCedulaDlgAutoOpened() {
        return wizardViewState.isCedulaDlgAutoOpened();
    }

    public void setCedulaDlgAutoOpened(boolean cedulaDlgAutoOpened) {
        wizardViewState.setCedulaDlgAutoOpened(cedulaDlgAutoOpened);
    }

    public boolean isPreRenderDone() {
        return wizardViewState.isPreRenderDone();
    }

    public void setPreRenderDone(boolean preRenderDone) {
        wizardViewState.setPreRenderDone(preRenderDone);
    }

    public boolean isMostrarDialogoAux() {
        return pacienteViewState.isMostrarDialogoAux();
    }

    public void setMostrarDialogoAux(boolean mostrarDialogoAux) {
        pacienteViewState.setMostrarDialogoAux(mostrarDialogoAux);
    }

    public Integer getNoPersonaSel() {
        return fichaContext.getNoPersonaSel();
    }

    public void setNoPersonaSel(Integer noPersonaSel) {
        fichaContext.setNoPersonaSel(noPersonaSel);
    }

    public DatEmpleado getEmpleadoSel() {
        return fichaContext.getEmpleadoSel();
    }

    public void setEmpleadoSel(DatEmpleado empleadoSel) {
        fichaContext.setEmpleadoSel(empleadoSel);
    }

    public String getInstitucion() {
        return step1FormModel.getInstitucion();
    }

    public void setInstitucion(String institucion) {
        step1FormModel.setInstitucion(institucion);
    }

    public String getRuc() {
        return step1FormModel.getRuc();
    }

    public void setRuc(String ruc) {
        step1FormModel.setRuc(ruc);
    }

    public String getCiiu() {
        return step1FormModel.getCiiu();
    }

    public void setCiiu(String ciiu) {
        step1FormModel.setCiiu(ciiu);
    }

    public String getCentroTrabajo() {
        return step1FormModel.getCentroTrabajo();
    }

    public void setCentroTrabajo(String centroTrabajo) {
        step1FormModel.setCentroTrabajo(centroTrabajo);
    }

    public String getNoHistoria() {
        return step1FormModel.getNoHistoria();
    }

    public void setNoHistoria(String noHistoria) {
        step1FormModel.setNoHistoria(noHistoria);
    }

    public String getNoArchivo() {
        return step1FormModel.getNoArchivo();
    }

    public void setNoArchivo(String noArchivo) {
        step1FormModel.setNoArchivo(noArchivo);
    }

    public String getTipoEvaluacion() {
        return step1FormModel.getTipoEvaluacion();
    }

    public void setTipoEvaluacion(String tipoEvaluacion) {
        step1FormModel.setTipoEvaluacion(tipoEvaluacion);
    }

    public String getMotivoObs() {
        return step1FormModel.getMotivoObs();
    }

    public void setMotivoObs(String motivoObs) {
        step1FormModel.setMotivoObs(motivoObs);
    }

    public Date getFum() {
        return ginecoObstetricoFormModel.getFum();
    }

    public void setFum(Date fum) {
        ginecoObstetricoFormModel.setFum(fum);
    }

    public String getPlanificacion() {
        return ginecoObstetricoFormModel.getPlanificacion();
    }

    public void setPlanificacion(String planificacion) {
        ginecoObstetricoFormModel.setPlanificacion(planificacion);
    }

    public String getPlanificacionCual() {
        return ginecoObstetricoFormModel.getPlanificacionCual();
    }

    public void setPlanificacionCual(String planificacionCual) {
        ginecoObstetricoFormModel.setPlanificacionCual(planificacionCual);
    }

    public Integer[] getConsTiempoConsumoMeses() {
        return habitosConsumoFormModel.getConsTiempoConsumoMeses();
    }

    public void setConsTiempoConsumoMeses(Integer[] consTiempoConsumoMeses) {
        habitosConsumoFormModel.setConsTiempoConsumoMeses(consTiempoConsumoMeses);
    }

    public Integer[] getConsTiempoAbstinenciaMeses() {
        return habitosConsumoFormModel.getConsTiempoAbstinenciaMeses();
    }

    public void setConsTiempoAbstinenciaMeses(Integer[] consTiempoAbstinenciaMeses) {
        habitosConsumoFormModel.setConsTiempoAbstinenciaMeses(consTiempoAbstinenciaMeses);
    }

    public String getConsOtrasCual() {
        return habitosConsumoFormModel.getConsOtrasCual();
    }

    public void setConsOtrasCual(String consOtrasCual) {
        habitosConsumoFormModel.setConsOtrasCual(consOtrasCual);
    }

    public String getConsumoVidaCondObs() {
        return habitosConsumoFormModel.getConsumoVidaCondObs();
    }

    public void setConsumoVidaCondObs(String consumoVidaCondObs) {
        habitosConsumoFormModel.setConsumoVidaCondObs(consumoVidaCondObs);
    }

    public Date getFechaEmision() {
        return diagnosticoFormModel.getFechaEmision();
    }

    public void setFechaEmision(Date fechaEmision) {
        diagnosticoFormModel.setFechaEmision(fechaEmision);
    }

    public String getnRealizaEvaluacion() {
        return diagnosticoFormModel.getnRealizaEvaluacion();
    }

    public void setnRealizaEvaluacion(String nRealizaEvaluacion) {
        diagnosticoFormModel.setnRealizaEvaluacion(nRealizaEvaluacion);
    }

    public String getnRelacionTrabajo() {
        return diagnosticoFormModel.getnRelacionTrabajo();
    }

    public void setnRelacionTrabajo(String nRelacionTrabajo) {
        diagnosticoFormModel.setnRelacionTrabajo(nRelacionTrabajo);
    }

    public String getnObsRetiro() {
        return diagnosticoFormModel.getnObsRetiro();
    }

    public void setnObsRetiro(String nObsRetiro) {
        diagnosticoFormModel.setnObsRetiro(nObsRetiro);
    }

    public String getCodCie10Ppal() {
        return step3FormModel.getCodCie10Ppal();
    }

    public void setCodCie10Ppal(String codCie10Ppal) {
        step3FormModel.setCodCie10Ppal(codCie10Ppal);
    }

    public String getDescCie10Ppal() {
        return step3FormModel.getDescCie10Ppal();
    }

    public void setDescCie10Ppal(String descCie10Ppal) {
        step3FormModel.setDescCie10Ppal(descCie10Ppal);
    }

    public String[] gethCentroTrabajo() {
        return historiaLaboralFormModel.gethCentroTrabajo();
    }

    public void sethCentroTrabajo(String[] hCentroTrabajo) {
        historiaLaboralFormModel.sethCentroTrabajo(hCentroTrabajo);
    }

    public String[] gethActividad() {
        return historiaLaboralFormModel.gethActividad();
    }

    public void sethActividad(String[] hActividad) {
        historiaLaboralFormModel.sethActividad(hActividad);
    }

    public Boolean[] gethIncidente() {
        return historiaLaboralFormModel.gethIncidente();
    }

    public void sethIncidente(Boolean[] hIncidente) {
        historiaLaboralFormModel.sethIncidente(hIncidente);
    }

    public Boolean[] gethAccidente() {
        return historiaLaboralFormModel.gethAccidente();
    }

    public void sethAccidente(Boolean[] hAccidente) {
        historiaLaboralFormModel.sethAccidente(hAccidente);
    }

    public Integer[] gethTiempo() {
        return historiaLaboralFormModel.gethTiempo();
    }

    public void sethTiempo(Integer[] hTiempo) {
        historiaLaboralFormModel.sethTiempo(hTiempo);
    }

    public Boolean[] gethEnfOcupacional() {
        return historiaLaboralFormModel.gethEnfOcupacional();
    }

    public void sethEnfOcupacional(Boolean[] hEnfOcupacional) {
        historiaLaboralFormModel.sethEnfOcupacional(hEnfOcupacional);
    }

    public Boolean[] gethEnfComun() {
        return historiaLaboralFormModel.gethEnfComun();
    }

    public void sethEnfComun(Boolean[] hEnfComun) {
        historiaLaboralFormModel.sethEnfComun(hEnfComun);
    }

    public Boolean[] gethEnfProfesional() {
        return historiaLaboralFormModel.gethEnfProfesional();
    }

    public void sethEnfProfesional(Boolean[] hEnfProfesional) {
        historiaLaboralFormModel.sethEnfProfesional(hEnfProfesional);
    }

    public Boolean[] gethOtros() {
        return historiaLaboralFormModel.gethOtros();
    }

    public void sethOtros(Boolean[] hOtros) {
        historiaLaboralFormModel.sethOtros(hOtros);
    }

    public String[] gethOtrosCual() {
        return historiaLaboralFormModel.gethOtrosCual();
    }

    public void sethOtrosCual(String[] hOtrosCual) {
        historiaLaboralFormModel.sethOtrosCual(hOtrosCual);
    }

    public Date[] gethFecha() {
        return historiaLaboralFormModel.gethFecha();
    }

    public void sethFecha(Date[] hFecha) {
        historiaLaboralFormModel.sethFecha(hFecha);
    }

    public String[] gethEspecificacion() {
        return historiaLaboralFormModel.gethEspecificacion();
    }

    public void sethEspecificacion(String[] hEspecificacion) {
        historiaLaboralFormModel.sethEspecificacion(hEspecificacion);
    }

    public String[] gethObservacion() {
        return historiaLaboralFormModel.gethObservacion();
    }

    public void sethObservacion(String[] hObservacion) {
        historiaLaboralFormModel.sethObservacion(hObservacion);
    }

    public SignosVitales getSignos() {
        return fichaContext.getSignos();
    }

    public void setSignos(SignosVitales signos) {
        fichaContext.setSignos(signos);
    }

    public ConsultaMedica getConsulta() {
        return fichaContext.getConsulta();
    }

    public void setConsulta(ConsultaMedica consulta) {
        fichaContext.setConsulta(consulta);
    }

    public String getPdfObjectUrl() {
        return pdfPreviewState.getPdfObjectUrl();
    }

    public void setPdfObjectUrl(String pdfObjectUrl) {
        pdfPreviewState.setPdfObjectUrl(pdfObjectUrl);
    }

    public List<String> getActLabIncidente() {
        return actividadLaboralFormModel.getActLabIncidente();
    }

    public void setActLabIncidente(List<String> actLabIncidente) {
        actividadLaboralFormModel.setActLabIncidente(actLabIncidente);
    }

    public List<Date> getActLabFecha() {
        return actividadLaboralFormModel.getActLabFecha();
    }

    public void setActLabFecha(List<Date> actLabFecha) {
        actividadLaboralFormModel.setActLabFecha(actLabFecha);
    }

    public String getPdfTokenCertificado() {
        return pdfPreviewState.getPdfTokenCertificado();
    }

    public String getAntTerapeutica() {
        return antecedentesFormModel.getAntTerapeutica();
    }

    public void setAntTerapeutica(String antTerapeutica) {
        antecedentesFormModel.setAntTerapeutica(antTerapeutica);
    }

    public String getAntObs() {
        return antecedentesFormModel.getAntObs();
    }

    public void setAntObs(String antObs) {
        antecedentesFormModel.setAntObs(antObs);
    }

    public String getGinecoExamen1() {
        return ginecoObstetricoFormModel.getGinecoExamen1();
    }

    public void setGinecoExamen1(String ginecoExamen1) {
        ginecoObstetricoFormModel.setGinecoExamen1(ginecoExamen1);
    }

    public String getGinecoTiempo1() {
        return ginecoObstetricoFormModel.getGinecoTiempo1();
    }

    public void setGinecoTiempo1(String ginecoTiempo1) {
        ginecoObstetricoFormModel.setGinecoTiempo1(ginecoTiempo1);
    }

    public String getGinecoResultado1() {
        return ginecoObstetricoFormModel.getGinecoResultado1();
    }

    public void setGinecoResultado1(String ginecoResultado1) {
        ginecoObstetricoFormModel.setGinecoResultado1(ginecoResultado1);
    }

    public String getGinecoExamen2() {
        return ginecoObstetricoFormModel.getGinecoExamen2();
    }

    public void setGinecoExamen2(String ginecoExamen2) {
        ginecoObstetricoFormModel.setGinecoExamen2(ginecoExamen2);
    }

    public String getGinecoTiempo2() {
        return ginecoObstetricoFormModel.getGinecoTiempo2();
    }

    public void setGinecoTiempo2(String ginecoTiempo2) {
        ginecoObstetricoFormModel.setGinecoTiempo2(ginecoTiempo2);
    }

    public String getGinecoResultado2() {
        return ginecoObstetricoFormModel.getGinecoResultado2();
    }

    public void setGinecoResultado2(String ginecoResultado2) {
        ginecoObstetricoFormModel.setGinecoResultado2(ginecoResultado2);
    }

    public String getGinecoObservacion() {
        return ginecoObstetricoFormModel.getGinecoObservacion();
    }

    public void setGinecoObservacion(String ginecoObservacion) {
        ginecoObstetricoFormModel.setGinecoObservacion(ginecoObservacion);
    }

    public String getConsumoObservacion() {
        return habitosConsumoFormModel.getConsumoObservacion();
    }

    public void setConsumoObservacion(String consumoObservacion) {
        habitosConsumoFormModel.setConsumoObservacion(consumoObservacion);
    }

    public String[] gethCargo() {
        return historiaLaboralFormModel.gethCargo();
    }

    public void sethCargo(String[] hCargo) {
        historiaLaboralFormModel.sethCargo(hCargo);
    }

    public String[] gethEnfermedad() {
        return historiaLaboralFormModel.gethEnfermedad();
    }

    public void sethEnfermedad(String[] hEnfermedad) {
        historiaLaboralFormModel.sethEnfermedad(hEnfermedad);
    }











    public boolean isCertPdfListo() {
        return pdfPreviewState.isCertificadoListo();
    }

    public String getFichaToken() {
        return pdfPreviewState.getPdfTokenFicha();
    }

    public String getCertToken() {
        return pdfPreviewState.getPdfTokenCertificado();
    }

    public boolean isFichaPdfListo() {
        return pdfPreviewState.isFichaPdfListo();
    }

    public String getPdfTokenFicha() {
        return pdfPreviewState.getPdfTokenFicha();
    }

    // Compatibilidad temporal para assemblers/componentes legacy: accesores aplanados.
    // TODO: retirar progresivamente al migrar consumidores a form models/secciones.
    @Deprecated
    public Date getFechaAtencion() {
        return step1FormModel.getFechaAtencion();
    }

    @Deprecated
    public String getTipoEval() {
        return step1FormModel.getTipoEval();
    }

    @Deprecated
    public String getPaStr() {
        return signosVitalesFormModel.getPaStr();
    }

    public Double getTemp() {
        return signosVitalesFormModel.getTemp();
    }

    public Integer getFc() {
        return signosVitalesFormModel.getFc();
    }

    public Integer getFr() {
        return signosVitalesFormModel.getFr();
    }

    public Integer getSatO2() {
        return signosVitalesFormModel.getSatO2();
    }

    public Double getPeso() {
        return signosVitalesFormModel.getPeso();
    }

    public Double getTallaCm() {
        return signosVitalesFormModel.getTallaCm();
    }

    public Double getPerimetroAbd() {
        return signosVitalesFormModel.getPerimetroAbd();
    }

    public boolean isApEmbarazada() {
        return atencionPrioritariaModel.isEmbarazada();
    }

    public boolean isApDiscapacidad() {
        return atencionPrioritariaModel.isDiscapacidad();
    }

    public boolean isApCatastrofica() {
        return atencionPrioritariaModel.isCatastrofica();
    }

    public boolean isApLactancia() {
        return atencionPrioritariaModel.isLactancia();
    }

    public boolean isApAdultoMayor() {
        return atencionPrioritariaModel.isAdultoMayor();
    }

    public String getAntClinicoQuirurgico() {
        return antecedentesFormModel.getAntClinicoQuirurgico();
    }

    public void setAntClinicoQuirurgico(String antClinicoQuirurgico) {
        antecedentesFormModel.setAntClinicoQuirurgico(antClinicoQuirurgico);
    }

    public String getAntFamiliares() {
        return antecedentesFormModel.getAntFamiliares();
    }

    public void setAntFamiliares(String antFamiliares) {
        antecedentesFormModel.setAntFamiliares(antFamiliares);
    }

    public String getCondicionEspecial() {
        return antecedentesFormModel.getCondicionEspecial();
    }

    public void setCondicionEspecial(String condicionEspecial) {
        antecedentesFormModel.setCondicionEspecial(condicionEspecial);
    }

    public String getAutorizaTransfusion() {
        return antecedentesFormModel.getAutorizaTransfusion();
    }

    public void setAutorizaTransfusion(String autorizaTransfusion) {
        antecedentesFormModel.setAutorizaTransfusion(autorizaTransfusion);
    }

    public String getTratamientoHormonal() {
        return antecedentesFormModel.getTratamientoHormonal();
    }

    public void setTratamientoHormonal(String tratamientoHormonal) {
        antecedentesFormModel.setTratamientoHormonal(tratamientoHormonal);
    }

    public String getTratamientoHormonalCual() {
        return antecedentesFormModel.getTratamientoHormonalCual();
    }

    public void setTratamientoHormonalCual(String tratamientoHormonalCual) {
        antecedentesFormModel.setTratamientoHormonalCual(tratamientoHormonalCual);
    }

    public String getDiscapTipo() {
        return atencionPrioritariaModel.getDiscapTipo();
    }

    public String getDiscapDesc() {
        return atencionPrioritariaModel.getDiscapDesc();
    }

    public Integer getDiscapPorc() {
        return atencionPrioritariaModel.getDiscapPorc();
    }

    public String getCatasDiagnostico() {
        return atencionPrioritariaModel.getCatasDiagnostico();
    }

    public Boolean getCatasCalificada() {
        return atencionPrioritariaModel.getCatasCalificada();
    }

    public String getAptitudSel() {
        return diagnosticoFormModel.getAptitudSel();
    }

    public String getLateralidad() {
        return step1FormModel.getLateralidad();
    }

    public void setLateralidad(String lateralidad) {
        step1FormModel.setLateralidad(lateralidad);
    }

    public Step1FormModel getStep1FormModel() {
        return step1FormModel;
    }

    public Step1FormModel getStep1() {
        return step1FormModel;
    }

    public Step2FormModel getStep2FormModel() {
        return step2FormModel;
    }

    public Step2FormModel getStep2() {
        return step2FormModel;
    }

    public AntecedentesFormModel getAntecedentesFormModel() {
        return antecedentesFormModel;
    }

    public void setAntecedentesFormModel(AntecedentesFormModel antecedentesFormModel) {
        this.antecedentesFormModel = (antecedentesFormModel != null)
                ? antecedentesFormModel
                : new AntecedentesFormModel();
    }

    public Step3FormModel getStep3FormModel() {
        return step3FormModel;
    }

    public Step3FormModel getStep3() {
        return step3FormModel;
    }

    public DiagnosticoFormModel getDiagnosticoFormModel() {
        return diagnosticoFormModel;
    }

    public DiagnosticoFormModel getDiagnosticoModel() {
        return diagnosticoFormModel;
    }

    public ActividadLaboralFormModel getActividadLaboralFormModel() {
        return actividadLaboralFormModel;
    }

    public ExamenesComplementariosFormModel getExamenesComplementariosFormModel() {
        return examenesComplementariosFormModel;
    }

    public HabitosConsumoFormModel getHabitosConsumoFormModel() {
        return habitosConsumoFormModel;
    }

    public SignosVitalesFormModel getSignosModel() {
        return signosVitalesFormModel;
    }

    public AtencionPrioritariaModel getAtencionPrioritariaModel() {
        return atencionPrioritariaModel;
    }

    public PdfPreviewState getPdfPreviewState() {
        return pdfPreviewState;
    }

    public PacienteViewState getPacienteViewState() {
        return pacienteViewState;
    }
    public List<String> getActividadesLab() {
        return step2FormModel.getActividadesLab();
    }

    public void setActividadesLab(List<String> actividadesLab) {
        step2FormModel.setActividadesLab(actividadesLab);
    }

    public Map<String, Boolean> getRiesgos() {
        return step2FormModel.getRiesgos();
    }

    public void setRiesgos(Map<String, Boolean> riesgos) {
        step2FormModel.setRiesgos(riesgos);
    }

    public Map<String, String> getOtrosRiesgos() {
        return step2FormModel.getOtrosRiesgos();
    }

    public void setOtrosRiesgos(Map<String, String> otrosRiesgos) {
        step2FormModel.setOtrosRiesgos(otrosRiesgos);
    }

    public List<String> getMedidasPreventivas() {
        return step2FormModel.getMedidasPreventivas();
    }

    public void setMedidasPreventivas(List<String> medidasPreventivas) {
        step2FormModel.setMedidasPreventivas(medidasPreventivas);
    }

}
