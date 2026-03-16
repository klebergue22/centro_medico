package ec.gob.igm.rrhh.consultorio.web.ctrl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
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
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoRhService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.ExamenFisicoRegionalService;
import ec.gob.igm.rrhh.consultorio.service.FichaActLaboralService;
import ec.gob.igm.rrhh.consultorio.service.FichaDiagnosticoService;
import ec.gob.igm.rrhh.consultorio.service.FichaExamenCompService;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoWizardFacade;
import ec.gob.igm.rrhh.consultorio.web.jsf.CentroMedicoMessageService;
import ec.gob.igm.rrhh.consultorio.web.mapper.Step3CommandAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.StepValidationInputAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.Step3ViewDataAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.PdfCertificadoInputAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.PdfFichaInputAssembler;
import ec.gob.igm.rrhh.consultorio.web.facade.DiagnosticoSectionFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.PacienteSectionFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.PdfPreviewFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.PdfSectionFacade;
import ec.gob.igm.rrhh.consultorio.web.facade.Step1Facade;
import ec.gob.igm.rrhh.consultorio.web.facade.WizardSectionFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfContextAssembler;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.service.CedulaDialogControllerSupport;
import ec.gob.igm.rrhh.consultorio.web.service.CedulaDialogStateService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoFormInitializer;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoFormStateService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfTemplateCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoReactiveUiService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardNavigationCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoValidationCoordinator.FichaCompletaValidationInput;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoValidationCoordinator.Step1ValidationInput;
import ec.gob.igm.rrhh.consultorio.web.service.ValidationUiResult;
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
import ec.gob.igm.rrhh.consultorio.web.viewstate.DiagnosticoFormModel;
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
    private static final int CONSUMO_ROWS = 3;
    private static final int DIAG_ROWS = 6;

    static {
        for (int i = 1; i <= 7; i++) {
            STATIC_RISK_COLS.add(String.valueOf(i));
        }
    }

    // =========================
    // CLASE INTERNA - EXCEPCIÓN
    // =========================
    public static class BusinessValidationException extends RuntimeException {
        private static final long serialVersionUID = 2L;

        public BusinessValidationException(String message) {
            super(message);
        }
    }

    // =========================
    // INYECCIONES DE DEPENDENCIAS - EJB
    // =========================
    @EJB
    private EmpleadoRhService empleadoRhService;
    @EJB
    private transient Cie10Service cie10Service;
    @EJB
    private transient FichaOcupacionalService fichaService;
    @EJB
    private transient FichaDiagnosticoService fichaDiagnosticoService;
    @EJB
    private transient EmpleadoService empleadoService;
    @EJB
    private transient PersonaAuxService personaAuxService;
    @EJB
    private transient FichaActLaboralService fichaActLaboralService;
    @EJB
    private transient FichaExamenCompService fichaExamenCompService;
    @EJB
    private transient ExamenFisicoRegionalService examenFisicoRegionalService;

    // =========================
    // INYECCIONES DE DEPENDENCIAS - INJECT
    // =========================
    @Inject
    private transient PdfSessionStore pdfSessionStore;
    @Inject
    private transient PdfTemplateEngine pdfTemplateEngine;
    @Inject
    private transient PdfResourceResolver pdfResourceResolver;
    @Inject
    private transient CertificadoPdfTemplateService certificadoPdfTemplateService;
    @Inject
    private transient CentroMedicoPdfFacade centroMedicoPdfFacade;
    @Inject
    private transient WizardSectionFacade wizardSectionFacade;
    @Inject
    private transient CentroMedicoMessageService messageService;
    @Inject
    private transient ControllerActionTemplate controllerActionTemplate;
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
    private transient PdfCertificadoInputAssembler pdfCertificadoInputAssembler;
    @Inject
    private transient CentroMedicoPdfTemplateCoordinator centroMedicoPdfTemplateCoordinator;
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

    // =========================
    // MODELOS DE FORMULARIO
    // =========================
    private final Step1FormModel step1FormModel = new Step1FormModel();
    private final Step2FormModel step2FormModel = new Step2FormModel();
    private final Step3FormModel step3FormModel = new Step3FormModel();
    private final AtencionPrioritariaModel atencionPrioritariaModel = new AtencionPrioritariaModel();
    private final SignosVitalesFormModel signosVitalesFormModel = new SignosVitalesFormModel();
    private final DiagnosticoFormModel diagnosticoFormModel = new DiagnosticoFormModel();
    private final PdfPreviewState pdfPreviewState = new PdfPreviewState();
    private final PacienteViewState pacienteViewState = new PacienteViewState();
    private final WizardViewState wizardViewState = new WizardViewState();

    // =========================
    // VARIABLES DE ESTADO DEL WIZARD
    // =========================
    // VARIABLES DE DIÁLOGO
    // =========================
    private boolean mostrarDlgCedula = true;
    private boolean mostrarDiaLOGoAux;
    private boolean permitirIngresoManual;

    // =========================
    // VARIABLES DE PACIENTE
    // =========================
    private String cedulaBusqueda;
    private Integer noPersonaSel;
    private DatEmpleado empleadoSel;
    private PersonaAux personaAux;

    // =========================
    // VARIABLES DE INSTITUCIÓN
    // =========================
    private String institucion;
    private String ruc;
    private String ciiu;
    private String centroTrabajo;

    // =========================
    // VARIABLES DE HISTORIA CLÍNICA
    // =========================
    private String noHistoria;
    private String noArchivo;

    // =========================
    // VARIABLES DE DATOS PERSONALES
    // =========================
    private final PacienteFormData pacienteFormData = step1FormModel.getPaciente();

    // =========================
    // VARIABLES DE EVALUACIÓN
    // =========================
    private Date fechaAtencion;
    private String tipoEval;
    private String tipoEvaluacion;
    private Date fecIngreso;
    private Date fecReintegro;
    private Date fecRetiro;
    private String grupoSanguineo;
    private String lateralidad;
    private String motivoObs;

    // =========================
    // VARIABLES DE ATENCIÓN PRIORITARIA
    // =========================

    // =========================
    // VARIABLES DE ANTECEDENTES
    // =========================
    private String antClinicoQuirurgico;
    private String antFamiliares;
    private String antTerapeutica;
    private String antObs;
    private String condicionEspecial;
    private String autorizaTransfusion;
    private String tratamientoHormonal;
    private String tratamientoHormonalCual;

    // =========================
    // VARIABLES DE EXAMEN REPRODUCTIVO MASCULINO
    // =========================
    private String examenReproMasculino;
    private Integer tiempoReproMasculino;

    // =========================
    // VARIABLES GINECO-OBSTÉTRICAS
    // =========================
    private String ginecoExamen1;
    private String ginecoTiempo1;
    private String ginecoResultado1;
    private String ginecoExamen2;
    private String ginecoTiempo2;
    private String ginecoResultado2;
    private String ginecoObservacion;
    private Date fum;
    private Integer gestas;
    private Integer partos;
    private Integer cesareas;
    private Integer abortos;
    private String planificacion;
    private String planificacionCual;

    // =========================
    // VARIABLES DE SIGNOS VITALES
    // =========================

    // =========================
    // VARIABLES DE CONSUMO/HÁBITOS
    // =========================
    private Integer[] consTiempoConsumoMeses;
    private Boolean[] consExConsumidor;
    private Integer[] consTiempoAbstinenciaMeses;
    private Boolean[] consNoConsume;
    private String consOtrasCual;
    private String[] afCual;
    private String[] afTiempo;
    private String[] medCual;
    private Integer[] medCant;
    private String consumoObservacion;
    private String consumoVidaCondObs;
    private String obsJ;

    // =========================
    // VARIABLES DE RIESGOS LABORALES
    // =========================

    private java.util.List<String> riskCols;

    // =========================
    // VARIABLES DE DIAGNÓSTICO Y OBSERVACIONES
    // =========================
    private String detalleObservaciones;


    // =========================
    // VARIABLES DE HISTORIA LABORAL (H)
    // =========================
    private String[] hCentroTrabajo;
    private String[] hActividad;
    private String[] hCargo;
    private String[] hEnfermedad;
    private Boolean[] hIncidente;
    private Boolean[] hAccidente;
    private Integer[] hTiempo;
    private Boolean[] hEnfOcupacional;
    private Boolean[] hEnfComun;
    private Boolean[] hEnfProfesional;
    private Boolean[] hOtros;
    private String[] hOtrosCual;
    private Date[] hFecha;
    private String[] hEspecificacion;
    private String[] hObservacion;

    // =========================
    // VARIABLES DE ACTIVIDADES LABORALES (LISTAS)
    // =========================
    private java.util.List<Date> iessFecha;
    private java.util.List<Date> fechaAct;
    private java.util.List<String> tipoAct;
    private java.util.List<String> descAct;
    private java.util.List<String> actLabRows;
    private java.util.List<String> actLabCentroTrabajo;
    private java.util.List<String> actLabActividad;
    private java.util.List<String> actLabIncidente;
    private java.util.List<Date> actLabFecha;
    private java.util.List<String> actLabTiempo;
    private java.util.List<Boolean> actLabTrabajoAnterior;
    private java.util.List<Boolean> actLabTrabajoActual;
    private java.util.List<Boolean> actLabIncidenteChk;
    private java.util.List<Boolean> actLabAccidenteChk;
    private java.util.List<Boolean> actLabEnfermedadChk;
    private java.util.List<Boolean> iessSi;
    private java.util.List<Boolean> iessNo;
    private java.util.List<String> iessEspecificar;
    private java.util.List<String> actLabObservaciones;

    // =========================
    // VARIABLES DE EXÁMENES COMPLEMENTARIOS
    // =========================
    private java.util.List<String> examNombre = new ArrayList<>();
    private java.util.List<String> examResultado = new ArrayList<>();
    private java.util.List<Date> examFecha = new ArrayList<>();

    // =========================
    // VARIABLES DE ENFERMEDAD ACTUAL
    // =========================
    private String enfermedadActual;

    // =========================
    // VARIABLES DE EXAMEN FÍSICO REGIONAL
    // =========================
    private String exfPielCicatrices;
    private String exfOjosParpados;
    private String exfOjosConjuntivas;
    private String exfOjosPupilas;
    private String exfOjosCornea;
    private String exfOjosMotilidad;
    private String exfOidoConducto;
    private String exfOidoPabellon;
    private String exfOidoTimpanos;
    private String exfOroLabios;
    private String exfOroLengua;
    private String exfOroFaringe;
    private String exfOroAmigdalas;
    private String exfOroDentadura;
    private String exfNarizTabique;
    private String exfNarizCornetes;
    private String exfNarizMucosas;
    private String exfNarizSenos;
    private String exfCuelloTiroides;
    private String exfCuelloMovilidad;
    private String exfToraxMamas;
    private String exfToraxPulmones;
    private String exfToraxCorazon;
    private String exfToraxParrilla;
    private String exfAbdomenVisceras;
    private String exfAbdomenPared;
    private String exfColumnaFlexibilidad;
    private String exfColumnaDesviacion;
    private String exfColumnaDolor;
    private String exfPelvisPelvis;
    private String exfPelvisGenitales;
    private String exfExtVascular;
    private String exfExtSup;
    private String exfExtInf;
    private String exfNeuroFuerza;
    private String exfNeuroSensibilidad;
    private String exfNeuroMarcha;
    private String exfNeuroReflejos;
    private String obsExamenFisico;

    // =========================
    // VARIABLES DE ENTIDADES DE DOMINIO
    // =========================
    private FichaOcupacional ficha;
    private SignosVitales signos;
    private ConsultaMedica consulta;

    // =========================
    // VARIABLES DE PDF
    // =========================


    // =========================
    // MÉTODOS DE EXCEPCIÓN Y VALIDACIÓN PRIVADOS
    // =========================
    private void fail(String message) {
        throw new BusinessValidationException(message);
    }

    private void s3(String msg) {
        LOG.info("[STEP3] {}", msg);
        LOG.info(String.valueOf("[STEP3] " + msg));
    }

    private void s3e(String msg, Throwable t) {
        LOG.error("[STEP3] " + msg, t);
        LOG.info(String.valueOf("[STEP3-ERROR] " + msg));
        if (t != null) {
            LOG.error("Unexpected error.", t);
        }
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
                    wizardSectionFacade.guardarStepActual(
                            new CentroMedicoWizardFacade.GuardarStepActualCommand(
                                    wizardViewState.getActiveStep(),
                                    this::guardarStep1,
                                    this::guardarStep2,
                                    this::guardarStep3,
                                    wizardViewState::setActiveStep,
                                    "@([id$=wdzFicha])",
                                    () -> pdfPreviewFacade.resetStep4PdfState(
                                            pdfPreviewState,
                                            value -> this.ficha = value,
                                            wizardViewState::setActiveStep,
                                            value -> this.mostrarDlgCedula = value),
                                    this::applyStep4State,
                                    ficha,
                                    () -> pdfPreviewFacade.buildPrepareFichaCommandData(buildBasePrepareCommand()),
                                    () -> pdfPreviewFacade.buildPrepareCertificadoCommandData(buildBasePrepareCommand())));
                    return wizardViewState.getActiveStep();
                },
                ignored -> {
                },
                LOG,
                wizardViewState.getActiveStep(),
                noPersonaSel,
                cedulaBusqueda);
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
                pacienteFormData.getSexo(),
                tipoEval,
                signosVitalesFormModel.getPaStr(),
                signosVitalesFormModel.getFc(),
                signosVitalesFormModel.getPeso(),
                signosVitalesFormModel.getTallaCm(),
                signos,
                fichaRiesgo);

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
        s3("validarStep3() INICIO");
        ValidationUiResult uiResult = wizardSectionFacade.validarStep3(
                step3FormModel.getListaDiag(),
                diagnosticoFormModel.getAptitudSel(),
                diagnosticoFormModel.getRecomendaciones(),
                diagnosticoFormModel.getMedicoNombre(),
                diagnosticoFormModel.getMedicoCodigo());
        if (!uiResult.isValid()) {
            for (String error : uiResult.getValidationResult().getErrors()) {
                s3("validarStep3() FAIL: " + error);
            }
        }
        uiResult.applyUi(messageService);
        s3("validarStep3() FIN -> " + uiResult.isValid());
        return uiResult.isValid();
    }

    private boolean verificarFichaCompleta() {
        FichaCompletaValidationInput input = stepValidationInputAssembler.buildFichaCompletaInput(
                ficha,
                permitirIngresoManual,
                personaAux,
                empleadoSel,
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
        controllerActionTemplate.execute(
                "guardarStep1",
                () -> {
                    saveStep1();
                    return true;
                },
                () -> messageService.info("Step 1 guardado correctamente (BORRADOR)."),
                LOG,
                wizardViewState.getActiveStep(),
                noPersonaSel,
                cedulaBusqueda);
    }

    private void saveStep1() {
        Step1Facade.SaveStep1Result result = wizardSectionFacade.guardarStep1(new Step1Facade.SaveStep1Command(
                permitirIngresoManual,
                empleadoSel,
                noPersonaSel,
                ficha,
                personaAux,
                this));

        pacienteSectionFacade.applyPacienteUiResult(this, result.preUiResult);
        ficha = result.ficha;
        empleadoSel = result.empleadoSel;
        personaAux = result.personaAux;
        signos = result.signos;
        pacienteSectionFacade.applyPacienteUiResult(this, result.postUiResult);
    }

    public void guardarStep2() {
        controllerActionTemplate.execute(
                "guardarStep2",
                () -> {
                    if (!validarStep2()) {
                        return false;
                    }
                    saveStep2();
                    return true;
                },
                () -> {
                    FacesContext ctx = FacesContext.getCurrentInstance();
                    if (ctx != null) {
                        ctx.addMessage(null, new FacesMessage(
                                FacesMessage.SEVERITY_INFO, "Step 2",
                                "Riesgos laborales guardados correctamente (encabezado + detalle)."));
                    }
                },
                LOG,
                wizardViewState.getActiveStep(),
                noPersonaSel,
                cedulaBusqueda);
    }

    private void saveStep2() {
        final Date now = new Date();

        try {
            step2FormModel.setFichaRiesgo(wizardSectionFacade.guardarStep2(new Step2RiskCommand(
                    ficha,
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
                "Step 2 guardado. ID_FICHA=" + ficha.getIdFicha());
    }

    public void guardarStep3() {
        controllerActionTemplate.execute(
                "guardarStep3",
                () -> {
                    saveStep3();
                    return true;
                },
                () -> {
                    FacesContext ctx = FacesContext.getCurrentInstance();
                    if (ctx != null) {
                        ctx.addMessage(null, new FacesMessage(
                                FacesMessage.SEVERITY_INFO, "OK", "Step 3 guardado correctamente."));
                    }
                },
                LOG,
                wizardViewState.getActiveStep(),
                noPersonaSel,
                cedulaBusqueda);
    }

    private void saveStep3() {
        ensureFichaSavedOrThrow();
        try {
            pacienteSectionFacade.asegurarPacienteAsignado(this,
                    permitirIngresoManual,
                    empleadoSel,
                    noPersonaSel,
                    personaAux,
                    ficha);
        } catch (IllegalStateException ex) {
            fail(ex.getMessage());
        }

        final Date now = new Date();

        try {
            ficha = wizardSectionFacade.guardarStep3(step3CommandAssembler.toCommand(
                    step3ViewDataAssembler.capture(this, now, () -> pacienteSectionFacade.asegurarPersonaAuxPersistida(this),
                            () -> centroMedicoFormStateService.ensureActLabSize(this, H_ROWS))));
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        wizardSectionFacade.registrarAuditoria("GUARDAR_STEP3", "FICHA_OCUPACIONAL / H / I / J / K", "*",
                "Step 3 guardado. ID_FICHA=" + ficha.getIdFicha());
    }

    private void ensureFichaSavedOrThrow() {
        if (ficha == null || ficha.getIdFicha() == null) {
            throw new BusinessValidationException("Primero debe existir y estar guardada la ficha (ID_FICHA).");
        }
    }

    // =========================
    // PDF - FICHA OCUPACIONAL
    // =========================
    public void prepararVistaPreviaFicha() {
        pdfPreviewFacade.prepararVistaPreviaFicha(
                new PdfPreviewFacade.PrepareVistaPreviaFichaCommand(
                        controllerActionTemplate,
                        pdfPreviewState,
                        buildBasePrepareCommand()));
    }

    // =========================
    // PDF - CERTIFICADO MÉDICO
    // =========================
    public void prepararVistaPreviaCertificado() {
        pdfPreviewFacade.prepararVistaPreviaCertificado(
                new PdfPreviewFacade.PrepareVistaPreviaCertificadoCommand(
                        controllerActionTemplate,
                        pdfPreviewState,
                        pdfSessionStore,
                        buildBasePrepareCommand()));
    }

    // =========================
    // PDF - VISTA PREVIA GENERAL
    // =========================
    public void prepararVistaPrevia() {
        pdfPreviewFacade.prepararVistaPrevia(
                new PdfPreviewFacade.PrepareVistaPreviaCommand(
                        controllerActionTemplate,
                        pdfPreviewState,
                        pdfSessionStore,
                        this::verificarFichaCompleta,
                        buildBasePrepareCommand()));
    }

    public void limpiarVistaPrevia() {
        pdfPreviewFacade.limpiarVistaPrevia(new PdfPreviewFacade.LimpiarVistaPreviaCommand(
                pdfSessionStore,
                pdfPreviewState,
                value -> this.ficha = value,
                wizardViewState::setActiveStep,
                value -> this.mostrarDlgCedula = value));
    }

    private PdfPreviewFacade.BasePrepareCommand buildBasePrepareCommand() {
        return new PdfPreviewFacade.BasePrepareCommand(
                this,
                LOG,
                wizardViewState.getActiveStep(),
                noPersonaSel,
                cedulaBusqueda,
                () -> pacienteSectionFacade.asegurarPersonaAuxPersistida(this),
                this::syncCamposDesdeObjetosInternal,
                this::recalcularIMC,
                this::verificarFichaCompleta,
                fecha -> diagnosticoFormModel.setFechaEmision(fecha),
                H_ROWS,
                pdfFichaInputAssembler,
                pdfCertificadoInputAssembler,
                centroMedicoPdfTemplateCoordinator,
                centroMedicoPdfFacade,
                pdfResourceResolver,
                pdfTemplateEngine,
                certificadoPdfTemplateService,
                value -> this.ficha = value,
                wizardViewState::setActiveStep,
                value -> this.mostrarDlgCedula = value);
    }

    private void applyStep4State(CentroMedicoWizardNavigationCoordinator.Step4UiState state) {
        pdfSectionFacade.applyStep4State(
                state,
                pdfPreviewState,
                value -> this.ficha = value,
                wizardViewState::setActiveStep,
                value -> this.mostrarDlgCedula = value);
    }

    private void showValidationMessage(FacesContext ctx, String summary, List<String> errors) {
        pdfSectionFacade.showValidationMessage(ctx, summary, errors);
    }

    // =========================
    // CIE10 Y DIAGNÓSTICO
    // =========================
    public void syncTipoEvaluacion() {
        this.tipoEvaluacion = this.tipoEval;
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
        mostrarDlgCedula = cedulaDialogControllerSupport.resetDialogVisibility();
    }

    public void onDlgCedulaHide() {
        mostrarDlgCedula = cedulaDialogControllerSupport.resetDialogVisibility();
    }

    public void onDlgCedulaClose() {
        mostrarDlgCedula = cedulaDialogControllerSupport.resetDialogVisibility();
    }

    public void autoOpenCedulaIfNeeded() {
        CedulaDialogStateService.AutoOpenState state = cedulaDialogControllerSupport.autoOpenIfNeeded(
                cedulaDialogStateService,
                wizardViewState.getActiveStep(),
                mostrarDlgCedula,
                wizardViewState.isCedulaDlgAutoOpened());
        wizardViewState.setCedulaDlgAutoOpened(state.isAutoOpened());
    }

    public void consumirAutoOpenCedulaDlg() {
        CedulaDialogStateService.AutoOpenState state = cedulaDialogControllerSupport.consumeAutoOpen(
                cedulaDialogStateService,
                wizardViewState.getActiveStep(),
                empleadoSel == null,
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
                consNoConsume,
                consExConsumidor,
                consTiempoConsumoMeses,
                consTiempoAbstinenciaMeses,
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
        if (this.ficha == null || this.ficha.getIdFicha() == null) {
            return;
        }
        this.ficha = fichaService.findById(this.ficha.getIdFicha());
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
        return pdfSectionFacade.obtenerTipoEvaluacionPdf(tipoEval, tipoEvaluacion);
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
                ficha,
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
                actLabCentroTrabajo,
                actLabActividad,
                actLabTiempo,
                actLabTrabajoAnterior,
                actLabTrabajoActual,
                actLabIncidenteChk,
                actLabAccidenteChk,
                actLabEnfermedadChk,
                iessSi,
                iessNo,
                iessFecha,
                iessEspecificar,
                actLabObservaciones,
                rep);
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
        return mostrarDlgCedula;
    }

    public void setMostrarDlgCedula(boolean mostrarDlgCedula) {
        this.mostrarDlgCedula = mostrarDlgCedula;
    }

    public PersonaAux getPersonaAux() {
        if (personaAux == null) {
            personaAux = new PersonaAux();
        }
        return personaAux;
    }

    public void setPersonaAux(PersonaAux personaAux) {
        this.personaAux = personaAux;
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

    public Date getFechaAtencion() {
        if (fechaAtencion == null) {
            fechaAtencion = new Date();
        }
        return fechaAtencion;
    }

    public void setFechaAtencion(Date fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }

    public List<String> getTipoAct() {
        if (tipoAct == null) {
            tipoAct = new ArrayList<>();
        }
        return tipoAct;
    }

    public void setTipoAct(List<String> tipoAct) {
        this.tipoAct = tipoAct;
    }

    public List<Date> getFechaAct() {
        if (fechaAct == null) {
            fechaAct = new ArrayList<>();
        }
        return fechaAct;
    }

    public void setFechaAct(List<Date> fechaAct) {
        this.fechaAct = fechaAct;
    }

    public List<String> getDescAct() {
        if (descAct == null) {
            descAct = new ArrayList<>();
        }
        return descAct;
    }

    public void setDescAct(List<String> descAct) {
        this.descAct = descAct;
    }

    public int getStepIndex() {
        return wizardViewState.getStepIndex();
    }

    public void setStepIndex(int stepIndex) {
        wizardViewState.setStepIndex(stepIndex);
    }

    public Integer[] getConsTiempoConsumo() {
        return consTiempoConsumoMeses;
    }

    public void setConsTiempoConsumo(Integer[] v) {
        this.consTiempoConsumoMeses = v;
    }

    public Integer[] getConsTiempoAbstinencia() {
        return consTiempoAbstinenciaMeses;
    }

    public void setConsTiempoAbstinencia(Integer[] v) {
        this.consTiempoAbstinenciaMeses = v;
    }

    public String getObsExamenFisico() {
        return obsExamenFisico;
    }

    public void setObsExamenFisico(String obsExamenFisico) {
        this.obsExamenFisico = obsExamenFisico;
    }

    public String getConsObservacion() {
        return consumoVidaCondObs;
    }

    public void setConsObservacion(String v) {
        this.consumoVidaCondObs = v;
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

    public Map<String, Boolean> getRiesgos() {
        if (step2FormModel.getRiesgos() == null) {
            step2FormModel.setRiesgos(new java.util.LinkedHashMap<>());
        }
        return step2FormModel.getRiesgos();
    }

    public Map<String, String> getOtrosRiesgos() {
        if (step2FormModel.getOtrosRiesgos() == null) {
            step2FormModel.setOtrosRiesgos(new java.util.LinkedHashMap<>());
        }
        return step2FormModel.getOtrosRiesgos();
    }

    public List<String> getRiskCols() {
        return riskCols != null ? riskCols : STATIC_RISK_COLS;
    }

    public void setRiskCols(List<String> riskCols) {
        this.riskCols = riskCols;
    }

    public void setEnfermedadActual(String enfermedadActual) {
        this.enfermedadActual = enfermedadActual;
    }

    public void setExfPielCicatrices(String exfPielCicatrices) {
        this.exfPielCicatrices = exfPielCicatrices;
    }

    public void setExfOjosParpados(String exfOjosParpados) {
        this.exfOjosParpados = exfOjosParpados;
    }

    public void setExfOjosConjuntivas(String exfOjosConjuntivas) {
        this.exfOjosConjuntivas = exfOjosConjuntivas;
    }

    public void setExfOjosPupilas(String exfOjosPupilas) {
        this.exfOjosPupilas = exfOjosPupilas;
    }

    public void setExfOjosCornea(String exfOjosCornea) {
        this.exfOjosCornea = exfOjosCornea;
    }

    public void setExfOjosMotilidad(String exfOjosMotilidad) {
        this.exfOjosMotilidad = exfOjosMotilidad;
    }

    public void setExfOidoConducto(String exfOidoConducto) {
        this.exfOidoConducto = exfOidoConducto;
    }

    public void setExfOidoPabellon(String exfOidoPabellon) {
        this.exfOidoPabellon = exfOidoPabellon;
    }

    public void setExfOidoTimpanos(String exfOidoTimpanos) {
        this.exfOidoTimpanos = exfOidoTimpanos;
    }

    public void setExfOroLabios(String exfOroLabios) {
        this.exfOroLabios = exfOroLabios;
    }

    public void setExfOroLengua(String exfOroLengua) {
        this.exfOroLengua = exfOroLengua;
    }

    public void setExfOroFaringe(String exfOroFaringe) {
        this.exfOroFaringe = exfOroFaringe;
    }

    public void setExfOroAmigdalas(String exfOroAmigdalas) {
        this.exfOroAmigdalas = exfOroAmigdalas;
    }

    public void setExfOroDentadura(String exfOroDentadura) {
        this.exfOroDentadura = exfOroDentadura;
    }

    public void setExfNarizTabique(String exfNarizTabique) {
        this.exfNarizTabique = exfNarizTabique;
    }

    public void setExfNarizCornetes(String exfNarizCornetes) {
        this.exfNarizCornetes = exfNarizCornetes;
    }

    public void setExfNarizMucosas(String exfNarizMucosas) {
        this.exfNarizMucosas = exfNarizMucosas;
    }

    public void setExfNarizSenos(String exfNarizSenos) {
        this.exfNarizSenos = exfNarizSenos;
    }

    public void setExfCuelloTiroides(String exfCuelloTiroides) {
        this.exfCuelloTiroides = exfCuelloTiroides;
    }

    public void setExfCuelloMovilidad(String exfCuelloMovilidad) {
        this.exfCuelloMovilidad = exfCuelloMovilidad;
    }

    public void setExfToraxMamas(String exfToraxMamas) {
        this.exfToraxMamas = exfToraxMamas;
    }

    public void setExfToraxPulmones(String exfToraxPulmones) {
        this.exfToraxPulmones = exfToraxPulmones;
    }

    public void setExfToraxCorazon(String exfToraxCorazon) {
        this.exfToraxCorazon = exfToraxCorazon;
    }

    public void setExfToraxParrilla(String exfToraxParrilla) {
        this.exfToraxParrilla = exfToraxParrilla;
    }

    public void setExfAbdomenVisceras(String exfAbdomenVisceras) {
        this.exfAbdomenVisceras = exfAbdomenVisceras;
    }

    public void setExfAbdomenPared(String exfAbdomenPared) {
        this.exfAbdomenPared = exfAbdomenPared;
    }

    public void setExfColumnaFlexibilidad(String exfColumnaFlexibilidad) {
        this.exfColumnaFlexibilidad = exfColumnaFlexibilidad;
    }

    public void setExfColumnaDesviacion(String exfColumnaDesviacion) {
        this.exfColumnaDesviacion = exfColumnaDesviacion;
    }

    public void setExfColumnaDolor(String exfColumnaDolor) {
        this.exfColumnaDolor = exfColumnaDolor;
    }

    public void setExfPelvisPelvis(String exfPelvisPelvis) {
        this.exfPelvisPelvis = exfPelvisPelvis;
    }

    public void setExfPelvisGenitales(String exfPelvisGenitales) {
        this.exfPelvisGenitales = exfPelvisGenitales;
    }

    public void setExfExtVascular(String exfExtVascular) {
        this.exfExtVascular = exfExtVascular;
    }

    public void setExfExtSup(String exfExtSup) {
        this.exfExtSup = exfExtSup;
    }

    public void setExfExtInf(String exfExtInf) {
        this.exfExtInf = exfExtInf;
    }

    public void setExfNeuroFuerza(String exfNeuroFuerza) {
        this.exfNeuroFuerza = exfNeuroFuerza;
    }

    public void setExfNeuroSensibilidad(String exfNeuroSensibilidad) {
        this.exfNeuroSensibilidad = exfNeuroSensibilidad;
    }

    public void setExfNeuroMarcha(String exfNeuroMarcha) {
        this.exfNeuroMarcha = exfNeuroMarcha;
    }

    public void setExfNeuroReflejos(String exfNeuroReflejos) {
        this.exfNeuroReflejos = exfNeuroReflejos;
    }

    public Integer getAbortos() {
        return abortos;
    }

    public void setAbortos(Integer abortos) {
        this.abortos = abortos;
    }

    public List<Boolean> getActLabAccidenteChk() {
        return actLabAccidenteChk;
    }

    public void setActLabAccidenteChk(List<Boolean> actLabAccidenteChk) {
        this.actLabAccidenteChk = actLabAccidenteChk;
    }

    public List<String> getActLabActividad() {
        return actLabActividad;
    }

    public void setActLabActividad(List<String> actLabActividad) {
        this.actLabActividad = actLabActividad;
    }

    public List<String> getActLabCentroTrabajo() {
        return actLabCentroTrabajo;
    }

    public void setActLabCentroTrabajo(List<String> actLabCentroTrabajo) {
        this.actLabCentroTrabajo = actLabCentroTrabajo;
    }

    public List<Boolean> getActLabEnfermedadChk() {
        return actLabEnfermedadChk;
    }

    public void setActLabEnfermedadChk(List<Boolean> actLabEnfermedadChk) {
        this.actLabEnfermedadChk = actLabEnfermedadChk;
    }

    public List<Boolean> getActLabIncidenteChk() {
        return actLabIncidenteChk;
    }

    public void setActLabIncidenteChk(List<Boolean> actLabIncidenteChk) {
        this.actLabIncidenteChk = actLabIncidenteChk;
    }

    public List<String> getActLabObservaciones() {
        return actLabObservaciones;
    }

    public void setActLabObservaciones(List<String> actLabObservaciones) {
        this.actLabObservaciones = actLabObservaciones;
    }

    public List<String> getActLabRows() {
        return actLabRows;
    }

    public void setActLabRows(List<String> actLabRows) {
        this.actLabRows = actLabRows;
    }

    public List<String> getActLabTiempo() {
        return actLabTiempo;
    }

    public void setActLabTiempo(List<String> actLabTiempo) {
        this.actLabTiempo = actLabTiempo;
    }

    public List<Boolean> getActLabTrabajoActual() {
        return actLabTrabajoActual;
    }

    public void setActLabTrabajoActual(List<Boolean> actLabTrabajoActual) {
        this.actLabTrabajoActual = actLabTrabajoActual;
    }

    public List<Boolean> getActLabTrabajoAnterior() {
        return actLabTrabajoAnterior;
    }

    public void setActLabTrabajoAnterior(List<Boolean> actLabTrabajoAnterior) {
        this.actLabTrabajoAnterior = actLabTrabajoAnterior;
    }

    public List<String> getActividadesLab() {
        return step2FormModel.getActividadesLab();
    }

    public void setActividadesLab(List<String> actividadesLab) {
        step2FormModel.setActividadesLab(actividadesLab);
    }

    public String[] getAfCual() {
        return afCual;
    }

    public void setAfCual(String[] afCual) {
        this.afCual = afCual;
    }

    public String[] getAfTiempo() {
        return afTiempo;
    }

    public void setAfTiempo(String[] afTiempo) {
        this.afTiempo = afTiempo;
    }

    public String getAntClinicoQuirurgico() {
        return antClinicoQuirurgico;
    }

    public void setAntClinicoQuirurgico(String antClinicoQuirurgico) {
        this.antClinicoQuirurgico = antClinicoQuirurgico;
    }

    public String getAntFamiliares() {
        return antFamiliares;
    }

    public void setAntFamiliares(String antFamiliares) {
        this.antFamiliares = antFamiliares;
    }

    public boolean isApAdultoMayor() {
        return atencionPrioritariaModel.isAdultoMayor();
    }

    public void setApAdultoMayor(boolean apAdultoMayor) {
        atencionPrioritariaModel.setAdultoMayor(apAdultoMayor);
    }

    public boolean isApCatastrofica() {
        return atencionPrioritariaModel.isCatastrofica();
    }

    public void setApCatastrofica(boolean apCatastrofica) {
        atencionPrioritariaModel.setCatastrofica(apCatastrofica);
    }

    public boolean isApDiscapacidad() {
        return atencionPrioritariaModel.isDiscapacidad();
    }

    public void setApDiscapacidad(boolean apDiscapacidad) {
        atencionPrioritariaModel.setDiscapacidad(apDiscapacidad);
    }

    public boolean isApEmbarazada() {
        return atencionPrioritariaModel.isEmbarazada();
    }

    public void setApEmbarazada(boolean apEmbarazada) {
        atencionPrioritariaModel.setEmbarazada(apEmbarazada);
    }

    public boolean isApLactancia() {
        return atencionPrioritariaModel.isLactancia();
    }

    public void setApLactancia(boolean apLactancia) {
        atencionPrioritariaModel.setLactancia(apLactancia);
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

    public String getAptitudSel() {
        return diagnosticoFormModel.getAptitudSel();
    }

    public void setAptitudSel(String aptitudSel) {
        diagnosticoFormModel.setAptitudSel(aptitudSel);
    }

    public String getAutorizaTransfusion() {
        return autorizaTransfusion;
    }

    public void setAutorizaTransfusion(String autorizaTransfusion) {
        this.autorizaTransfusion = autorizaTransfusion;
    }

    public String getCedulaBusqueda() {
        return cedulaBusqueda;
    }

    public void setCedulaBusqueda(String cedulaBusqueda) {
        this.cedulaBusqueda = cedulaBusqueda;
    }

    public boolean isCertificadoListo() {
        return pdfPreviewState.isCertificadoListo();
    }

    public void setCertificadoListo(boolean certificadoListo) {
        pdfPreviewState.setCertificadoListo(certificadoListo);
    }

    public Integer getCesareas() {
        return cesareas;
    }

    public void setCesareas(Integer cesareas) {
        this.cesareas = cesareas;
    }

    public String getCondicionEspecial() {
        return condicionEspecial;
    }

    public void setCondicionEspecial(String condicionEspecial) {
        this.condicionEspecial = condicionEspecial;
    }

    public Boolean[] getConsExConsumidor() {
        return consExConsumidor;
    }

    public void setConsExConsumidor(Boolean[] consExConsumidor) {
        this.consExConsumidor = consExConsumidor;
    }

    public Boolean[] getConsNoConsume() {
        return consNoConsume;
    }

    public void setConsNoConsume(Boolean[] consNoConsume) {
        this.consNoConsume = consNoConsume;
    }

    public String getDetalleObservaciones() {
        return detalleObservaciones;
    }

    public void setDetalleObservaciones(String detalleObservaciones) {
        this.detalleObservaciones = detalleObservaciones;
    }

    public Integer getEdad() {
        return pacienteFormData.getEdad();
    }

    public void setEdad(Integer edad) {
        pacienteFormData.setEdad(edad);
    }

    public List<Date> getExamFecha() {
        return examFecha;
    }

    public void setExamFecha(List<Date> examFecha) {
        this.examFecha = examFecha;
    }

    public List<String> getExamNombre() {
        return examNombre;
    }

    public void setExamNombre(List<String> examNombre) {
        this.examNombre = examNombre;
    }

    public List<String> getExamResultado() {
        return examResultado;
    }

    public void setExamResultado(List<String> examResultado) {
        this.examResultado = examResultado;
    }

    public String getExamenReproMasculino() {
        return examenReproMasculino;
    }

    public void setExamenReproMasculino(String examenReproMasculino) {
        this.examenReproMasculino = examenReproMasculino;
    }

    public Integer getFc() {
        return signosVitalesFormModel.getFc();
    }

    public void setFc(Integer fc) {
        signosVitalesFormModel.setFc(fc);
    }

    public Date getFecIngreso() {
        return fecIngreso;
    }

    public void setFecIngreso(Date fecIngreso) {
        this.fecIngreso = fecIngreso;
    }

    public Date getFecReintegro() {
        return fecReintegro;
    }

    public void setFecReintegro(Date fecReintegro) {
        this.fecReintegro = fecReintegro;
    }

    public Date getFecRetiro() {
        return fecRetiro;
    }

    public void setFecRetiro(Date fecRetiro) {
        this.fecRetiro = fecRetiro;
    }

    public FichaOcupacional getFicha() {
        return ficha;
    }

    public void setFicha(FichaOcupacional ficha) {
        this.ficha = ficha;
    }

    public FichaRiesgo getFichaRiesgo() {
        return step2FormModel.getFichaRiesgo();
    }

    public void setFichaRiesgo(FichaRiesgo fichaRiesgo) {
        step2FormModel.setFichaRiesgo(fichaRiesgo);
    }

    public Integer getFr() {
        return signosVitalesFormModel.getFr();
    }

    public void setFr(Integer fr) {
        signosVitalesFormModel.setFr(fr);
    }

    public Integer getGestas() {
        return gestas;
    }

    public void setGestas(Integer gestas) {
        this.gestas = gestas;
    }

    public String getGrupoSanguineo() {
        return grupoSanguineo;
    }

    public void setGrupoSanguineo(String grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    public List<String> getIessEspecificar() {
        return iessEspecificar;
    }

    public void setIessEspecificar(List<String> iessEspecificar) {
        this.iessEspecificar = iessEspecificar;
    }

    public List<Date> getIessFecha() {
        return iessFecha;
    }

    public void setIessFecha(List<Date> iessFecha) {
        this.iessFecha = iessFecha;
    }

    public List<Boolean> getIessNo() {
        return iessNo;
    }

    public void setIessNo(List<Boolean> iessNo) {
        this.iessNo = iessNo;
    }

    public List<Boolean> getIessSi() {
        return iessSi;
    }

    public void setIessSi(List<Boolean> iessSi) {
        this.iessSi = iessSi;
    }

    public Double getImc() {
        return signosVitalesFormModel.getImc();
    }

    public void setImc(Double imc) {
        signosVitalesFormModel.setImc(imc);
    }

    public String getLateralidad() {
        return lateralidad;
    }

    public void setLateralidad(String lateralidad) {
        this.lateralidad = lateralidad;
    }

    public List<ConsultaDiagnostico> getListaDiag() {
        return step3FormModel.getListaDiag();
    }

    public void setListaDiag(List<ConsultaDiagnostico> listaDiag) {
        step3FormModel.setListaDiag(listaDiag);
    }

    public Integer[] getMedCant() {
        return medCant;
    }

    public void setMedCant(Integer[] medCant) {
        this.medCant = medCant;
    }

    public String[] getMedCual() {
        return medCual;
    }

    public void setMedCual(String[] medCual) {
        this.medCual = medCual;
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

    public List<String> getMedidasPreventivas() {
        return step2FormModel.getMedidasPreventivas();
    }

    public void setMedidasPreventivas(List<String> medidasPreventivas) {
        step2FormModel.setMedidasPreventivas(medidasPreventivas);
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
        return obsJ;
    }

    public void setObsJ(String obsJ) {
        this.obsJ = obsJ;
    }

    public void setOtrosRiesgos(Map<String, String> otrosRiesgos) {
        step2FormModel.setOtrosRiesgos(otrosRiesgos);
    }

    public String getPaStr() {
        return signosVitalesFormModel.getPaStr();
    }

    public void setPaStr(String paStr) {
        signosVitalesFormModel.setPaStr(paStr);
    }

    public Integer getPartos() {
        return partos;
    }

    public void setPartos(Integer partos) {
        this.partos = partos;
    }

    public String getPdfToken() {
        return pdfPreviewState.getPdfTokenCertificado();
    }

    public void setPdfToken(String pdfTokenCertificado) {
        pdfPreviewState.setPdfTokenCertificado(pdfTokenCertificado);
    }

    public Double getPerimetroAbd() {
        return signosVitalesFormModel.getPerimetroAbd();
    }

    public void setPerimetroAbd(Double perimetroAbd) {
        signosVitalesFormModel.setPerimetroAbd(perimetroAbd);
    }

    public boolean isPermitirIngresoManual() {
        return permitirIngresoManual;
    }

    public void setPermitirIngresoManual(boolean permitirIngresoManual) {
        this.permitirIngresoManual = permitirIngresoManual;
    }

    public Double getPeso() {
        return signosVitalesFormModel.getPeso();
    }

    public void setPeso(Double peso) {
        signosVitalesFormModel.setPeso(peso);
    }

    public String getRecomendaciones() {
        return diagnosticoFormModel.getRecomendaciones();
    }

    public void setRecomendaciones(String recomendaciones) {
        diagnosticoFormModel.setRecomendaciones(recomendaciones);
    }

    public void setRiesgos(Map<String, Boolean> riesgos) {
        step2FormModel.setRiesgos(riesgos);
    }

    public Integer getSatO2() {
        return signosVitalesFormModel.getSatO2();
    }

    public void setSatO2(Integer satO2) {
        signosVitalesFormModel.setSatO2(satO2);
    }

    public String getSexo() {
        return pacienteFormData.getSexo();
    }

    public void setSexo(String sexo) {
        pacienteFormData.setSexo(sexo);
    }

    public Double getTallaCm() {
        return signosVitalesFormModel.getTallaCm();
    }

    public void setTallaCm(Double tallaCm) {
        signosVitalesFormModel.setTallaCm(tallaCm);
    }

    public Double getTemp() {
        return signosVitalesFormModel.getTemp();
    }

    public void setTemp(Double temp) {
        signosVitalesFormModel.setTemp(temp);
    }

    public Integer getTiempoReproMasculino() {
        return tiempoReproMasculino;
    }

    public void setTiempoReproMasculino(Integer tiempoReproMasculino) {
        this.tiempoReproMasculino = tiempoReproMasculino;
    }

    public String getTipoEval() {
        return tipoEval;
    }

    public void setTipoEval(String tipoEval) {
        this.tipoEval = tipoEval;
    }

    public String getTratamientoHormonal() {
        return tratamientoHormonal;
    }

    public void setTratamientoHormonal(String tratamientoHormonal) {
        this.tratamientoHormonal = tratamientoHormonal;
    }

    public String getTratamientoHormonalCual() {
        return tratamientoHormonalCual;
    }

    public void setTratamientoHormonalCual(String tratamientoHormonalCual) {
        this.tratamientoHormonalCual = tratamientoHormonalCual;
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

    public boolean isMostrarDiaLOGoAux() {
        return mostrarDiaLOGoAux;
    }

    public void setMostrarDiaLOGoAux(boolean mostrarDiaLOGoAux) {
        this.mostrarDiaLOGoAux = mostrarDiaLOGoAux;
    }

    public Integer getNoPersonaSel() {
        return noPersonaSel;
    }

    public void setNoPersonaSel(Integer noPersonaSel) {
        this.noPersonaSel = noPersonaSel;
    }

    public DatEmpleado getEmpleadoSel() {
        return empleadoSel;
    }

    public void setEmpleadoSel(DatEmpleado empleadoSel) {
        this.empleadoSel = empleadoSel;
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getCiiu() {
        return ciiu;
    }

    public void setCiiu(String ciiu) {
        this.ciiu = ciiu;
    }

    public String getCentroTrabajo() {
        return centroTrabajo;
    }

    public void setCentroTrabajo(String centroTrabajo) {
        this.centroTrabajo = centroTrabajo;
    }

    public String getNoHistoria() {
        return noHistoria;
    }

    public void setNoHistoria(String noHistoria) {
        this.noHistoria = noHistoria;
    }

    public String getNoArchivo() {
        return noArchivo;
    }

    public void setNoArchivo(String noArchivo) {
        this.noArchivo = noArchivo;
    }

    public String getTipoEvaluacion() {
        return tipoEvaluacion;
    }

    public void setTipoEvaluacion(String tipoEvaluacion) {
        this.tipoEvaluacion = tipoEvaluacion;
    }

    public String getMotivoObs() {
        return motivoObs;
    }

    public void setMotivoObs(String motivoObs) {
        this.motivoObs = motivoObs;
    }

    public Date getFum() {
        return fum;
    }

    public void setFum(Date fum) {
        this.fum = fum;
    }

    public String getPlanificacion() {
        return planificacion;
    }

    public void setPlanificacion(String planificacion) {
        this.planificacion = planificacion;
    }

    public String getPlanificacionCual() {
        return planificacionCual;
    }

    public void setPlanificacionCual(String planificacionCual) {
        this.planificacionCual = planificacionCual;
    }

    public Integer[] getConsTiempoConsumoMeses() {
        return consTiempoConsumoMeses;
    }

    public void setConsTiempoConsumoMeses(Integer[] consTiempoConsumoMeses) {
        this.consTiempoConsumoMeses = consTiempoConsumoMeses;
    }

    public Integer[] getConsTiempoAbstinenciaMeses() {
        return consTiempoAbstinenciaMeses;
    }

    public void setConsTiempoAbstinenciaMeses(Integer[] consTiempoAbstinenciaMeses) {
        this.consTiempoAbstinenciaMeses = consTiempoAbstinenciaMeses;
    }

    public String getConsOtrasCual() {
        return consOtrasCual;
    }

    public void setConsOtrasCual(String consOtrasCual) {
        this.consOtrasCual = consOtrasCual;
    }

    public String getConsumoVidaCondObs() {
        return consumoVidaCondObs;
    }

    public void setConsumoVidaCondObs(String consumoVidaCondObs) {
        this.consumoVidaCondObs = consumoVidaCondObs;
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
        return hCentroTrabajo;
    }

    public void sethCentroTrabajo(String[] hCentroTrabajo) {
        this.hCentroTrabajo = hCentroTrabajo;
    }

    public String[] gethActividad() {
        return hActividad;
    }

    public void sethActividad(String[] hActividad) {
        this.hActividad = hActividad;
    }

    public Boolean[] gethIncidente() {
        return hIncidente;
    }

    public void sethIncidente(Boolean[] hIncidente) {
        this.hIncidente = hIncidente;
    }

    public Boolean[] gethAccidente() {
        return hAccidente;
    }

    public void sethAccidente(Boolean[] hAccidente) {
        this.hAccidente = hAccidente;
    }

    public Integer[] gethTiempo() {
        return hTiempo;
    }

    public void sethTiempo(Integer[] hTiempo) {
        this.hTiempo = hTiempo;
    }

    public Boolean[] gethEnfOcupacional() {
        return hEnfOcupacional;
    }

    public void sethEnfOcupacional(Boolean[] hEnfOcupacional) {
        this.hEnfOcupacional = hEnfOcupacional;
    }

    public Boolean[] gethEnfComun() {
        return hEnfComun;
    }

    public void sethEnfComun(Boolean[] hEnfComun) {
        this.hEnfComun = hEnfComun;
    }

    public Boolean[] gethEnfProfesional() {
        return hEnfProfesional;
    }

    public void sethEnfProfesional(Boolean[] hEnfProfesional) {
        this.hEnfProfesional = hEnfProfesional;
    }

    public Boolean[] gethOtros() {
        return hOtros;
    }

    public void sethOtros(Boolean[] hOtros) {
        this.hOtros = hOtros;
    }

    public String[] gethOtrosCual() {
        return hOtrosCual;
    }

    public void sethOtrosCual(String[] hOtrosCual) {
        this.hOtrosCual = hOtrosCual;
    }

    public Date[] gethFecha() {
        return hFecha;
    }

    public void sethFecha(Date[] hFecha) {
        this.hFecha = hFecha;
    }

    public String[] gethEspecificacion() {
        return hEspecificacion;
    }

    public void sethEspecificacion(String[] hEspecificacion) {
        this.hEspecificacion = hEspecificacion;
    }

    public String[] gethObservacion() {
        return hObservacion;
    }

    public void sethObservacion(String[] hObservacion) {
        this.hObservacion = hObservacion;
    }

    public SignosVitales getSignos() {
        return signos;
    }

    public void setSignos(SignosVitales signos) {
        this.signos = signos;
    }

    public ConsultaMedica getConsulta() {
        return consulta;
    }

    public void setConsulta(ConsultaMedica consulta) {
        this.consulta = consulta;
    }

    public String getPdfObjectUrl() {
        return pdfPreviewState.getPdfObjectUrl();
    }

    public void setPdfObjectUrl(String pdfObjectUrl) {
        pdfPreviewState.setPdfObjectUrl(pdfObjectUrl);
    }

    public List<String> getActLabIncidente() {
        return actLabIncidente;
    }

    public void setActLabIncidente(List<String> actLabIncidente) {
        this.actLabIncidente = actLabIncidente;
    }

    public List<Date> getActLabFecha() {
        return actLabFecha;
    }

    public void setActLabFecha(List<Date> actLabFecha) {
        this.actLabFecha = actLabFecha;
    }

    public Cie10Service getCie10Service() {
        return cie10Service;
    }

    public void setCie10Service(Cie10Service cie10Service) {
        this.cie10Service = cie10Service;
    }

    public FichaOcupacionalService getFichaService() {
        return fichaService;
    }

    public void setFichaService(FichaOcupacionalService fichaService) {
        this.fichaService = fichaService;
    }

    public FichaDiagnosticoService getFichaDiagnosticoService() {
        return fichaDiagnosticoService;
    }

    public void setFichaDiagnosticoService(FichaDiagnosticoService fichaDiagnosticoService) {
        this.fichaDiagnosticoService = fichaDiagnosticoService;
    }

    public EmpleadoService getEmpleadoService() {
        return empleadoService;
    }

    public void setEmpleadoService(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    public PersonaAuxService getPersonaAuxService() {
        return personaAuxService;
    }

    public void setPersonaAuxService(PersonaAuxService personaAuxService) {
        this.personaAuxService = personaAuxService;
    }


    public FichaExamenCompService getFichaExamenCompService() {
        return fichaExamenCompService;
    }

    public void setFichaExamenCompService(FichaExamenCompService fichaExamenCompService) {
        this.fichaExamenCompService = fichaExamenCompService;
    }

    public String getPdfTokenCertificado() {
        return pdfPreviewState.getPdfTokenCertificado();
    }

    public String getAntTerapeutica() {
        return antTerapeutica;
    }

    public void setAntTerapeutica(String antTerapeutica) {
        this.antTerapeutica = antTerapeutica;
    }

    public String getAntObs() {
        return antObs;
    }

    public void setAntObs(String antObs) {
        this.antObs = antObs;
    }

    public String getGinecoExamen1() {
        return ginecoExamen1;
    }

    public void setGinecoExamen1(String ginecoExamen1) {
        this.ginecoExamen1 = ginecoExamen1;
    }

    public String getGinecoTiempo1() {
        return ginecoTiempo1;
    }

    public void setGinecoTiempo1(String ginecoTiempo1) {
        this.ginecoTiempo1 = ginecoTiempo1;
    }

    public String getGinecoResultado1() {
        return ginecoResultado1;
    }

    public void setGinecoResultado1(String ginecoResultado1) {
        this.ginecoResultado1 = ginecoResultado1;
    }

    public String getGinecoExamen2() {
        return ginecoExamen2;
    }

    public void setGinecoExamen2(String ginecoExamen2) {
        this.ginecoExamen2 = ginecoExamen2;
    }

    public String getGinecoTiempo2() {
        return ginecoTiempo2;
    }

    public void setGinecoTiempo2(String ginecoTiempo2) {
        this.ginecoTiempo2 = ginecoTiempo2;
    }

    public String getGinecoResultado2() {
        return ginecoResultado2;
    }

    public void setGinecoResultado2(String ginecoResultado2) {
        this.ginecoResultado2 = ginecoResultado2;
    }

    public String getGinecoObservacion() {
        return ginecoObservacion;
    }

    public void setGinecoObservacion(String ginecoObservacion) {
        this.ginecoObservacion = ginecoObservacion;
    }

    public String getConsumoObservacion() {
        return consumoObservacion;
    }

    public void setConsumoObservacion(String consumoObservacion) {
        this.consumoObservacion = consumoObservacion;
    }

    public String[] gethCargo() {
        return hCargo;
    }

    public void sethCargo(String[] hCargo) {
        this.hCargo = hCargo;
    }

    public String[] gethEnfermedad() {
        return hEnfermedad;
    }

    public void sethEnfermedad(String[] hEnfermedad) {
        this.hEnfermedad = hEnfermedad;
    }

    public String getDiscapTipo() {
        return atencionPrioritariaModel.getDiscapTipo();
    }

    public void setDiscapTipo(String discapTipo) {
        atencionPrioritariaModel.setDiscapTipo(discapTipo);
    }

    public String getDiscapDesc() {
        return atencionPrioritariaModel.getDiscapDesc();
    }

    public void setDiscapDesc(String discapDesc) {
        atencionPrioritariaModel.setDiscapDesc(discapDesc);
    }

    public Integer getDiscapPorc() {
        return atencionPrioritariaModel.getDiscapPorc();
    }

    public void setDiscapPorc(Integer discapPorc) {
        atencionPrioritariaModel.setDiscapPorc(discapPorc);
    }

    public String getCatasDiagnostico() {
        return atencionPrioritariaModel.getCatasDiagnostico();
    }

    public void setCatasDiagnostico(String catasDiagnostico) {
        atencionPrioritariaModel.setCatasDiagnostico(catasDiagnostico);
    }

    public Boolean getCatasCalificada() {
        return atencionPrioritariaModel.getCatasCalificada();
    }

    public void setCatasCalificada(Boolean catasCalificada) {
        atencionPrioritariaModel.setCatasCalificada(catasCalificada);
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

    public Step1FormModel getStep1FormModel() {
        return step1FormModel;
    }

    public Step2FormModel getStep2FormModel() {
        return step2FormModel;
    }

    public Step3FormModel getStep3FormModel() {
        return step3FormModel;
    }

    public PdfPreviewState getPdfPreviewState() {
        return pdfPreviewState;
    }

    public PacienteViewState getPacienteViewState() {
        return pacienteViewState;
    }
}
