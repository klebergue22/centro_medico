package ec.gob.igm.rrhh.consultorio.web.ctrl;

import static ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils.esVacio;
import static ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils.getSafe;
import static ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils.isBlank;
import static ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils.isTrue;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.PrimeFaces;
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
import ec.gob.igm.rrhh.consultorio.service.Step1FichaService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.jsf.CentroMedicoMessageService;
import ec.gob.igm.rrhh.consultorio.web.mapper.Step1CommandAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.Step3CommandAssembler;
import ec.gob.igm.rrhh.consultorio.web.audit.CentroMedicoAuditService;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfContextAssembler;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfPlaceholderAssembler;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTextUtil;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.service.CedulaDialogUiCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.CedulaSearchService;
import ec.gob.igm.rrhh.consultorio.web.service.Cie10LookupService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoFormInitializer;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoFormStateService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfWorkflowService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfFacadeService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfUiCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardNavigationCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfDataMapper;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfMappedData;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfPlaceholderBuilder;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfViewModelBuilder;
import ec.gob.igm.rrhh.consultorio.web.service.PacienteUiFlowCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.PersonaAuxDialogUiCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.PersonaAuxFlowService;
import ec.gob.igm.rrhh.consultorio.web.service.Step2OrchestratorService;
import ec.gob.igm.rrhh.consultorio.web.service.Step2OrchestratorService.Step2RiskCommand;
import ec.gob.igm.rrhh.consultorio.web.service.Step3OrchestratorService;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoCalcUtil;
import ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils;
import ec.gob.igm.rrhh.consultorio.web.validation.FichaCompletaValidator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step1Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step2Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step3Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.ValidationResult;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PacienteViewState;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfPreviewState;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step1FormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step2FormModel;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step3FormModel;

/**
 *
 * @author GUERRA_KLEBER
 */
@Named("centroMedicoCtrl")
@ViewScoped

public class CentroMedicoCtrl implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(CentroMedicoCtrl.class);

    private static final long serialVersionUID = 2L;
    private static final List<String> STATIC_RISK_COLS = new ArrayList<>();

    static {
        for (int i = 1; i <= 7; i++) {
            STATIC_RISK_COLS.add(String.valueOf(i));
        }
    }

    public static class BusinessValidationException extends RuntimeException {

        private static final long serialVersionUID = 2L;

        public BusinessValidationException(String message) {
            super(message);
        }
    }

    private void fail(String message) {
        throw new BusinessValidationException(message);
    }

    private void applyPacienteUiFlow(PacienteUiFlowCoordinator.UiFlowResult result) {
        if (result == null) {
            return;
        }
        pacienteViewState.setEmpleadoSel(result.getEmpleadoSel());
        pacienteViewState.setNoPersonaSel(result.getNoPersonaSel());
        pacienteViewState.setPersonaAux(result.getPersonaAux());
        pacienteViewState.setPermitirIngresoManual(result.isPermitirIngresoManual());

        ficha = result.getFicha();
        empleadoSel = result.getEmpleadoSel();
        noPersonaSel = result.getNoPersonaSel();
        personaAux = result.getPersonaAux();
        permitirIngresoManual = result.isPermitirIngresoManual();
    }

    private static final int H_ROWS = 4;
    private static final int CONSUMO_ROWS = 3;
    private static final int DIAG_ROWS = 6;

    private final Step1Validator step1Validator = new Step1Validator();
    private final Step2Validator step2Validator = new Step2Validator();
    private final Step3Validator step3Validator = new Step3Validator();
    private final FichaCompletaValidator fichaCompletaValidator = new FichaCompletaValidator();

    private final Step1FormModel step1FormModel = new Step1FormModel();
    private final Step2FormModel step2FormModel = new Step2FormModel();
    private final Step3FormModel step3FormModel = new Step3FormModel();
    private final PdfPreviewState pdfPreviewState = new PdfPreviewState();
    private final PacienteViewState pacienteViewState = new PacienteViewState();

    private String activeStep = "step1";
    private boolean cedulaDlgAutoOpened = false;

    private boolean mostrarDlgCedula = true;
    private boolean preRenderDone = false;
    private boolean mostrarDiaLOGoAux;
    private boolean permitirIngresoManual;

    private String cedulaBusqueda;
    private Integer noPersonaSel;
    private DatEmpleado empleadoSel;
    private PersonaAux personaAux;

    private String institucion;
    private String ruc;
    private String ciiu;
    private String centroTrabajo;

    private String noHistoria;
    private String noArchivo;

    private String apellido1;
    private String apellido2;
    private String nombre1;
    private String nombre2;

    private String sexo;
    private Date fechaNacimiento;
    private Integer edad;

    private Date fechaAtencion;
    private String tipoEval;
    private String tipoEvaluacion;

    private Date fecIngreso;
    private Date fecReintegro;
    private Date fecRetiro;

    private String grupoSanguineo;
    private String lateralidad;
    private String motivoObs;

    private boolean apEmbarazada;
    private boolean apDiscapacidad;
    private boolean apCatastrofica;
    private boolean apLactancia;
    private boolean apAdultoMayor;
    private String discapTipo;
    private String discapDesc;
    private Integer discapPorc;

    private String catasDiagnostico;
    private Boolean catasCalificada; // en UI como boolean

    private String antClinicoQuirurgico;
    private String antFamiliares;
    private String antTerapeutica;
    private String antObs;
    private String condicionEspecial;

    private String autorizaTransfusion;
    private String tratamientoHormonal;
    private String tratamientoHormonalCual;

    private String examenReproMasculino;
    private Integer tiempoReproMasculino;

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

    private Double peso;
    private Double tallaCm;
    private Double imc;
    private Double temp;
    private String paStr;
    private Integer fc;
    private Integer fr;
    private Integer satO2;
    private Double perimetroAbd;

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

    private FichaRiesgo fichaRiesgo;
    private java.util.List<String> actividadesLab = new ArrayList<>();
    private java.util.Map<String, Boolean> riesgos = new LinkedHashMap<>();
    private java.util.Map<String, String> otrosRiesgos = new LinkedHashMap<>();
    private java.util.List<String> medidasPreventivas = new ArrayList<>();
    private java.util.List<String> riskCols;

    private Date fechaEmision;
    private String aptitudSel;
    private String detalleObservaciones;
    private String recomendaciones;
    private String medicoNombre;
    private String medicoCodigo;

    private String nRealizaEvaluacion;
    private String nRelacionTrabajo;
    private String nObsRetiro;

    private String codCie10Ppal;
    private String descCie10Ppal;
    private String dialogDiagnosticoCodigo;
    private String dialogDiagnosticoDescripcion;
    private Integer dialogDiagnosticoIdx;

    private java.util.List<ConsultaDiagnostico> listaDiag = new ArrayList<>();

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

    private java.util.List<Date> iessFecha;
    private java.util.List<Date> fechaAct;

    private java.util.List<String> tipoAct;

    private java.util.List<String> descAct;

    private FichaOcupacional ficha;
    private SignosVitales signos;
    private ConsultaMedica consulta;

    private boolean certificadoListo;
    private boolean fichaPdfListo;
    private String pdfObjectUrl;
    private String pdfTokenFicha;
    private String pdfTokenCertificado;

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

    private java.util.List<String> examNombre = new ArrayList<>();
    private java.util.List<String> examResultado = new ArrayList<>();
    private java.util.List<Date> examFecha = new ArrayList<>();

// D. ENFERMEDAD O PROBLEMA ACTUAL
    private String enfermedadActual;

// F. EXAMEN FÍSICO REGIONAL
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
    private String exfNarizSenos;           // Mapeará a exfNarizSenosParanasa
    private String exfCuelloTiroides;       // Mapeará a exfCuelloTiroidesMasas
    private String exfCuelloMovilidad;
    private String exfToraxMamas;
    private String exfToraxPulmones;
    private String exfToraxCorazon;
    private String exfToraxParrilla;         // Mapeará a exfToraxParrillaCostal
    private String exfAbdomenVisceras;       // Mapeará a exfAbdVisceras
    private String exfAbdomenPared;          // Mapeará a exfAbdParedAbdominal
    private String exfColumnaFlexibilidad;   // Mapeará a exfColFlexibilidad
    private String exfColumnaDesviacion;     // Mapeará a exfColDesviacion
    private String exfColumnaDolor;          // Mapeará a exfColDolor
    private String exfPelvisPelvis;
    private String exfPelvisGenitales;
    private String exfExtVascular;
    private String exfExtSup;                // Mapeará a exfExtMiembrosSup
    private String exfExtInf;                // Mapeará a exfExtMiembrosInf
    private String exfNeuroFuerza;
    private String exfNeuroSensibilidad;
    private String exfNeuroMarcha;
    private String exfNeuroReflejos;
    private String obsExamenFisico;          // Mapeará a obsExamenFisicoRegional o obsExamenFisicoReg

    @EJB
    private EmpleadoRhService empleadoRhService;
    @EJB
    private transient Cie10Service cie10Service;
    @EJB
    private transient FichaOcupacionalService fichaService;
    @EJB
    private transient Step1FichaService step1FichaService;
    @EJB
    private transient FichaDiagnosticoService fichaDiagnosticoService;
    @EJB
    private transient EmpleadoService empleadoService;
    @EJB
    private transient PersonaAuxService personaAuxService;
    @EJB
    private transient FichaActLaboralService fichaActLaboralService;

    @EJB
    private transient Step2OrchestratorService step2OrchestratorService;

    @EJB
    private transient FichaExamenCompService fichaExamenCompService;
    @EJB
    private transient ExamenFisicoRegionalService examenFisicoRegionalService;

    @EJB
    private transient CentroMedicoAuditService centroMedicoAuditService;

    @Inject
    private transient PdfSessionStore pdfSessionStore;

    @Inject
    private transient PdfTemplateEngine pdfTemplateEngine;

    @Inject
    private transient PdfResourceResolver pdfResourceResolver;

    @EJB
    private transient FichaPdfTemplateService fichaPdfTemplateService;

    @Inject
    private transient CertificadoPdfTemplateService certificadoPdfTemplateService;

    @Inject
    private transient CentroMedicoPdfFacade centroMedicoPdfFacade;


    @Inject
    private transient CentroMedicoWizardNavigationCoordinator wizardNavigationCoordinator;

    @Inject
    private transient CentroMedicoMessageService messageService;

    @Inject
    private transient ControllerActionTemplate controllerActionTemplate;

    @EJB
    private transient Cie10LookupService cie10LookupService;

    @Inject
    private transient CentroMedicoCalcUtil calcUtil;

    @Inject
    private transient CedulaSearchService cedulaSearchService;

    @Inject
    private transient CedulaDialogUiCoordinator cedulaDialogUiCoordinator;

    @Inject
    private transient CentroMedicoFormInitializer centroMedicoFormInitializer;

    @Inject
    private transient CentroMedicoFormStateService centroMedicoFormStateService;

    @Inject
    private transient PersonaAuxFlowService personaAuxFlowService;

    @Inject
    private transient PacienteUiFlowCoordinator pacienteUiFlowCoordinator;

    @Inject
    private transient PersonaAuxDialogUiCoordinator personaAuxDialogUiCoordinator;

    @Inject
    private transient Step1CommandAssembler step1CommandAssembler;

    @Inject
    private transient Step3CommandAssembler step3CommandAssembler;

    @EJB
    private transient Step3OrchestratorService step3OrchestratorService;

    @EJB
    private transient FichaPdfDataMapper fichaPdfDataMapper;

    @EJB
    private transient FichaPdfPlaceholderBuilder fichaPdfPlaceholderBuilder;

    @EJB
    private transient FichaPdfViewModelBuilder fichaPdfViewModelBuilder;

    @EJB
    private transient FichaPdfContextAssembler fichaPdfContextAssembler;

    @EJB
    private transient FichaPdfPlaceholderAssembler fichaPdfPlaceholderAssembler;

    @EJB
    private transient CentroMedicoPdfWorkflowService centroMedicoPdfWorkflowService;

    @EJB
    private transient CentroMedicoPdfFacadeService centroMedicoPdfFacadeService;

    @Inject
    private transient CentroMedicoPdfUiCoordinator centroMedicoPdfUiCoordinator;

    // JSF Lifecycle / Inicialización
    public void preRenderInit() {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc == null) {
                return;
            }
            applyPacienteUiFlow(pacienteUiFlowCoordinator.ensureEmpleadoSelEnViewScope(
                    permitirIngresoManual,
                    empleadoSel,
                    noPersonaSel,
                    ficha,
                    personaAux));
            final boolean postback = fc.isPostback();

            if (!"step1".equals(activeStep)) {
                mostrarDlgCedula = false;
            } else {

                mostrarDlgCedula = (empleadoSel == null);
            }

            if (!preRenderDone) {
                centroMedicoFormStateService.prepareStep3Collections(this, H_ROWS, DIAG_ROWS, 5);
                preRenderDone = true;
            } else {
                centroMedicoFormStateService.prepareStep3Collections(this, H_ROWS, DIAG_ROWS, 5);
            }

            LOG.info("GET? {} activeStep={} empleadoSel={} mostrarDlgCedula={}",
                    !FacesContext.getCurrentInstance().isPostback(),
                    activeStep,
                    (empleadoSel == null),
                    mostrarDlgCedula);

        } catch (RuntimeException e) {
            LOG.error("preRenderInit failed. activeStep={}, noPersonaSel={}, cedulaBusqueda={}",
                    activeStep, noPersonaSel, cedulaBusqueda, e);
        }
    }

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

    public void onNoConsumeChange(int idx) {
        if (Boolean.TRUE.equals(consNoConsume[idx])) {
            consExConsumidor[idx] = false;
            consTiempoConsumoMeses[idx] = 0;
            consTiempoAbstinenciaMeses[idx] = 0;
        }
    }

    public void onDlgCedulaShown() {
        mostrarDlgCedula = false;
    }

    public void onCie10BlurCodigo(int index) {
        ConsultaDiagnostico diag = centroMedicoFormStateService.ensureDiag(this, index);
        if (diag == null) {
            return;
        }
        cie10LookupService.completarDiagnosticoPorCodigo(diag);
    }

    public void onCie10FilaSelect(int idx) {
        if (listaDiag == null || idx < 0 || idx >= listaDiag.size()) {
            return;
        }

        cie10LookupService.sincronizarFilaSeleccionada(listaDiag.get(idx));
    }

    public void onFechaNacimientoSelect(SelectEvent e) {
        this.fechaNacimiento = (java.util.Date) e.getObject();
        recalculateEdadAndNotify();
    }

    public void onFechaNacimientoChange() {
        recalculateEdadAndNotify();
    }

    private void recalculateEdadAndNotify() {
        this.edad = calcUtil.calcularEdad(this.fechaNacimiento);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cálculo de edad",
                        "Edad calculada: " + (edad == null ? "(sin fecha)" : edad + " años")));
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date f) {
        this.fechaNacimiento = f;
        this.edad = calcUtil.calcularEdad(f);
    }

    /**
     * cambio de step
     *
     * @param event
     * @return
     */
    public String onFlow(FlowEvent event) {
        // Navegación del wizard (PrimeFaces)
        // NOTA: La persistencia/validación por paso se maneja en los botones (guardarStepActual / retrocederStep)
        // para evitar doble guardado cuando se ejecuta PF('wdzFicha').next()/back().
        final String nextStep = event.getNewStep();
        this.activeStep = nextStep;
        if ("step1".equals(nextStep)) {
            cedulaDlgAutoOpened = false;
        }
        return nextStep;
    }

    /**
     * Metodo que se usa en la vista
     */
    public void calcularEdad() {
        this.edad = calcUtil.calcularEdad(this.fechaNacimiento);
    }

    public Date getFechaMaximaNacimiento() {
        return calcUtil.getFechaMaximaNacimiento();
    }

    /**
     * Se usa en calculo de la edad para ejercer un trabajo
     */
    public void validarEdadMinima() {
        if (!calcUtil.validarEdadMinima(edad)) {
            messageService.error("La edad debe ser ≥ 18 años");
            fechaNacimiento = null;
            edad = null;
        }
    }

    /**
     * calculo del Indice de masa corporal
     */
    public void recalcularIMC() {
        this.imc = calcUtil.recalcularIMC(peso, tallaCm);
    }

    private void registrarAuditoria(String accion, String tabla, String campo, String observaciones) {
        s3("registrarAuditoria() accion=" + accion + " tabla=" + tabla + " campo=" + campo);

        try {
            centroMedicoAuditService.registrar(accion, tabla, campo, observaciones);
            s3("registrarAuditoria() OK");
        } catch (RuntimeException e) {
            s3e("registrarAuditoria() FALLÓ", e);
        }
    }

    /**
     * guarda el step actual
     */
    public void guardarStepActual() {
        LOG.info(">>> ENTRO A guardarStepActual, step={}", activeStep);
        controllerActionTemplate.executeWithResult(
                "guardarStepActual",
                () -> {
                    wizardNavigationCoordinator.guardarStepActual(
                            new CentroMedicoWizardNavigationCoordinator.GuardarStepActualCommand(
                                    activeStep,
                                    this::guardarStep1,
                                    this::guardarStep2,
                                    this::guardarStep3,
                                    next -> this.activeStep = next,
                                    () -> PrimeFaces.current().ajax().update("@([id$=wdzFicha])"),
                                    this::resetStep4PdfState,
                                    this::applyStep4State,
                                    ficha,
                                    buildPrepareFichaCommand(),
                                    buildPrepareCertificadoCommand()));
                    return activeStep;
                },
                ignored -> {
                },
                LOG,
                activeStep,
                noPersonaSel,
                cedulaBusqueda);
    }

    private void resetStep4PdfState() {
        applyPdfUiState(centroMedicoPdfUiCoordinator.resetStep4PdfState());
    }

    private void applyStep4State(CentroMedicoWizardNavigationCoordinator.Step4UiState state) {
        applyPdfUiState(centroMedicoPdfUiCoordinator.applyStep4State(state, null));
    }

    private void asegurarPersonaAuxPersistida() {
        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.asegurarPersonaAuxPersistida(
                permitirIngresoManual,
                ficha,
                personaAux);
        applyPacienteUiFlow(result);
    }

    public void retrocederStep() {
        activeStep = wizardNavigationCoordinator.retrocederStep(activeStep);
    }

    private boolean validarStep1() {
        ValidationResult result = step1Validator.validate(
                apellido1,
                apellido2,
                nombre1,
                nombre2,
                sexo,
                tipoEval,
                paStr,
                fc,
                peso,
                tallaCm,
                signos,
                fichaRiesgo != null ? fichaRiesgo.getPuestoTrabajo() : null,
                fichaRiesgo);
        messageService.addValidationMessages("Step 1", result);
        return result.isValid();
    }

    private String usuarioReal() {
        try {

            return "USR_APP";
        } catch (RuntimeException e) {
            return "USR_APP";
        }
    }

    // Wizard - Guardado Step 1
    public void guardarStep1() {
        controllerActionTemplate.execute(
                "guardarStep1",
                () -> {
                    saveStep1();
                    return true;
                },
                () -> messageService.info("Step 1 guardado correctamente (BORRADOR)."),
                LOG,
                activeStep,
                noPersonaSel,
                cedulaBusqueda);
    }

    private void saveStep1() {
        applyPacienteUiFlow(pacienteUiFlowCoordinator.ensureEmpleadoSelEnViewScope(
                permitirIngresoManual,
                empleadoSel,
                noPersonaSel,
                ficha,
                personaAux));

        Step1FichaService.Step1Command command = step1CommandAssembler.toCommand(
                ficha,
                empleadoSel,
                personaAux,
                signos,
                noPersonaSel,
                fechaAtencion,
                tipoEval,
                paStr,
                temp,
                fc,
                fr,
                satO2,
                peso,
                tallaCm,
                perimetroAbd,
                apEmbarazada,
                apDiscapacidad,
                apCatastrofica,
                apLactancia,
                apAdultoMayor,
                antClinicoQuirurgico,
                antFamiliares,
                condicionEspecial,
                autorizaTransfusion,
                tratamientoHormonal,
                tratamientoHormonalCual,
                examenReproMasculino,
                tiempoReproMasculino,
                ginecoExamen1,
                ginecoTiempo1,
                ginecoResultado1,
                ginecoExamen2,
                ginecoTiempo2,
                ginecoResultado2,
                ginecoObservacion,
                fum,
                gestas,
                partos,
                cesareas,
                abortos,
                planificacion,
                planificacionCual,
                discapTipo,
                discapDesc,
                discapPorc,
                catasDiagnostico,
                catasCalificada,
                nRealizaEvaluacion,
                nRelacionTrabajo,
                nObsRetiro,
                consTiempoConsumoMeses,
                consExConsumidor,
                consTiempoAbstinenciaMeses,
                consNoConsume,
                consOtrasCual,
                afCual,
                afTiempo,
                medCual,
                medCant,
                consumoVidaCondObs,
                usuarioReal());

        try {
            Step1FichaService.Step1Result result = step1FichaService.guardar(command);
            ficha = result.ficha();
            empleadoSel = result.empleadoSel();
            personaAux = result.personaAux();
            signos = result.signos();
            applyPacienteUiFlow(pacienteUiFlowCoordinator.syncPatientStateAfterStep1(
                    permitirIngresoManual,
                    empleadoSel,
                    noPersonaSel,
                    personaAux,
                    ficha));
        } catch (Step1FichaService.Step1ValidationException ex) {
            throw new BusinessValidationException(ex.getMessage());
        }
    }

    public void onBuscarPorCedulaRh() {
        try {
            buscarCedula();
        } catch (RuntimeException ex) {
            messageService.handleUnexpected(LOG, "onBuscarPorCedulaRh", ex, activeStep, noPersonaSel, cedulaBusqueda);
            cedulaDialogUiCoordinator.onRhError();
        }
    }

    private boolean validarStep2() {
        ValidationResult result = step2Validator.validate(fichaRiesgo, actividadesLab, medidasPreventivas);
        messageService.addValidationMessages("Step 2", result);
        return result.isValid();
    }

    // Wizard - Guardado Step 2
    public void guardarStep2() {
        controllerActionTemplate.execute(
                "guardarStep2",
                () -> {
                    FacesContext ctx = FacesContext.getCurrentInstance();
                    if (!validarStep2()) {
                        if (ctx != null) {
                            ctx.validationFailed();
                        }
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
                activeStep,
                noPersonaSel,
                cedulaBusqueda);
    }

    private void saveStep2() {
        final Date now = new Date();
        final String user = usuarioReal();

        try {
            fichaRiesgo = step2OrchestratorService.save(new Step2RiskCommand(
                    ficha,
                    fichaRiesgo,
                    actividadesLab,
                    medidasPreventivas,
                    riesgos,
                    otrosRiesgos,
                    now,
                    user
            ));
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException(ex.getMessage());
        }

        registrarAuditoria("GUARDAR_STEP2", "FICHA_RIESGO / FICHA_RIESGO_DET", "*",
                "Step 2 guardado. ID_FICHA=" + ficha.getIdFicha());
    }

    private void ensureFichaSavedOrThrow() {
        if (ficha == null || ficha.getIdFicha() == null) {
            throw new BusinessValidationException("Primero debe existir y estar guardada la ficha (ID_FICHA).");
        }
    }

    private boolean validarStep3() {
        s3("validarStep3() INICIO");
        ValidationResult result = step3Validator.validate(listaDiag, aptitudSel, recomendaciones, medicoNombre, medicoCodigo);
        if (!result.isValid()) {
            for (String error : result.getErrors()) {
                s3("validarStep3() FAIL: " + error);
            }
        }
        messageService.addValidationMessages("Step 3", result);
        s3("validarStep3() FIN -> " + result.isValid());
        return result.isValid();
    }

    // Wizard - Guardado Step 3
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
                activeStep,
                noPersonaSel,
                cedulaBusqueda);
    }

    private void saveStep3() {
        ensureFichaSavedOrThrow();
        try {
            applyPacienteUiFlow(pacienteUiFlowCoordinator.ensurePatientAssignedForFicha(
                    permitirIngresoManual,
                    empleadoSel,
                    noPersonaSel,
                    personaAux,
                    ficha));
        } catch (IllegalStateException ex) {
            fail(ex.getMessage());
        }

        final Date now = new Date();
        final String user = usuarioReal();

        try {
            ficha = step3OrchestratorService.saveStep3(step3CommandAssembler.toCommand(
                    ficha,
                    codCie10Ppal,
                    obsExamenFisico,
                    aptitudSel,
                    detalleObservaciones,
                    recomendaciones,
                    nObsRetiro,
                    medicoNombre,
                    medicoCodigo,
                    fechaEmision,
                    now,
                    user,
                    this::asegurarPersonaAuxPersistida,
                    () -> centroMedicoFormStateService.ensureActLabSize(this, H_ROWS),
                    actLabCentroTrabajo,
                    actLabActividad,
                    actLabTiempo,
                    actLabTrabajoAnterior,
                    actLabTrabajoActual,
                    actLabIncidenteChk,
                    actLabAccidenteChk,
                    actLabEnfermedadChk,
                    iessFecha,
                    iessEspecificar,
                    actLabObservaciones,
                    tipoAct,
                    fechaAct,
                    descAct,
                    examNombre,
                    examFecha,
                    examResultado,
                    listaDiag));
        } catch (IllegalArgumentException ex) {
            fail(ex.getMessage());
        }

        registrarAuditoria("GUARDAR_STEP3", "FICHA_OCUPACIONAL / H / I / J / K", "*",
                "Step 3 guardado. ID_FICHA=" + ficha.getIdFicha());
    }

    private boolean verificarFichaCompleta() {
        ValidationResult result = fichaCompletaValidator.validate(
                ficha,
                permitirIngresoManual,
                personaAux,
                empleadoSel,
                aptitudSel,
                fechaEmision,
                this::inferCie10PrincipalFromListaK);

        if (!result.isValid()) {
            StringBuilder sb = new StringBuilder();
            for (String error : result.getErrors()) {
                sb.append("- ").append(error).append("\n");
            }
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Validación antes de generar el certificado",
                            sb.toString()));
        }

        return result.isValid();
    }

    // PDF - Ficha Ocupacional
    public void prepararVistaPreviaFicha() {
        onPrepararFichaPdf();
    }

    public void onPrepararFichaPdf() {
        controllerActionTemplate.executeWithResult(
                "onPrepararFichaPdf",
                () -> centroMedicoPdfWorkflowService.onPrepararFichaPdf(
                        new CentroMedicoPdfWorkflowService.PrepareFichaFlowCommand(buildPrepareFichaCommand())),
                this::onPrepararFichaPdfSuccess,
                LOG,
                activeStep,
                noPersonaSel,
                cedulaBusqueda);
    }

    private void onPrepararFichaPdfSuccess(CentroMedicoPdfWorkflowService.FichaFlowResult result) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null || result == null) {
            return;
        }
        if (!result.listo) {
            showValidationMessage(ctx, "Validación antes de generar la ficha", result.errores);
            fichaPdfListo = false;
            pdfTokenFicha = null;
            return;
        }

        ficha = result.ficha;
        pdfTokenFicha = result.token;
        fichaPdfListo = true;
        activeStep = "step4";
        mostrarDlgCedula = false;

        ctx.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "PDF Ficha listo",
                "Se generó la ficha para vista previa y descarga."
        ));

        PrimeFaces.current().ajax().addCallbackParam("fichaListo", fichaPdfListo);
        PrimeFaces.current().ajax().update(":msgs", "@([id$=wdzFicha])");
    }

    private CentroMedicoPdfWorkflowService.PrepareFichaCommandData buildPrepareFichaCommand() {
        CentroMedicoPdfUiCoordinator.BuildPrepareFichaUiCommand cmd = new CentroMedicoPdfUiCoordinator.BuildPrepareFichaUiCommand();
        cmd.ficha = ficha;
        cmd.empleadoSel = empleadoSel;
        cmd.personaAux = personaAux;
        cmd.permitirIngresoManual = permitirIngresoManual;
        cmd.asegurarPersonaAuxPersistida = this::asegurarPersonaAuxPersistida;
        cmd.htmlFichaSupplier = this::construirHtmlFichaDesdePlantilla;
        cmd.centroMedicoPdfFacade = centroMedicoPdfFacade;
        return centroMedicoPdfUiCoordinator.buildPrepareFichaCommand(cmd);
    }

    private CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData buildPrepareCertificadoCommand() {
        CentroMedicoPdfUiCoordinator.BuildPrepareCertificadoUiCommand cmd = new CentroMedicoPdfUiCoordinator.BuildPrepareCertificadoUiCommand();
        cmd.ficha = ficha;
        cmd.verificarFichaCompleta = this::verificarFichaCompleta;
        cmd.htmlCertificadoSupplier = this::construirHtmlDesdePlantilla;
        cmd.fechaEmisionSetter = fecha -> this.fechaEmision = fecha;
        cmd.centroMedicoPdfFacade = centroMedicoPdfFacade;
        return centroMedicoPdfUiCoordinator.buildPrepareCertificadoCommand(cmd);
    }

    private String construirHtmlFichaDesdePlantilla() {
        CentroMedicoPdfFacadeService.FichaTemplateCommand cmd = new CentroMedicoPdfFacadeService.FichaTemplateCommand();
        cmd.source = this;
        cmd.log = LOG;
        cmd.fichaPdfTemplateService = fichaPdfTemplateService;
        cmd.fichaPdfPlaceholderBuilder = fichaPdfPlaceholderBuilder;
        cmd.pdfResourceResolver = pdfResourceResolver;
        cmd.fichaPdfPlaceholderAssembler = fichaPdfPlaceholderAssembler;
        cmd.fichaPdfContextAssembler = fichaPdfContextAssembler;
        cmd.fichaPdfDataMapper = fichaPdfDataMapper;
        cmd.fichaPdfViewModelBuilder = fichaPdfViewModelBuilder;
        cmd.centroMedicoPdfFacade = centroMedicoPdfFacade;
        cmd.syncCamposDesdeObjetos = this::syncCamposDesdeObjetosInternal;
        cmd.obtenerTipoEvaluacionPdf = this::obtenerTipoEvaluacionPdf;
        cmd.recalcularIMC = this::recalcularIMC;
        cmd.cargarAtencionPrioritaria = this::cargarAtencionPrioritaria;
        cmd.cargarActividadLaboralArrays = this::cargarActividadLaboralArrays;
        cmd.fallbackObservacionSupplier = () -> getFichaStringByReflection(ficha,
                "getDetalleObs",
                "getDetalleObservaciones",
                "getObservaciones",
                "getObs",
                "getObservacion");
        cmd.getSafe = CentroMedicoViewUtils::getSafe;
        cmd.toDate = this::toDate;
        return centroMedicoPdfFacadeService.construirHtmlFichaDesdePlantilla(cmd);
    }

    private String obtenerTipoEvaluacionPdf() {
        String tipo = PdfTextUtil.trimToNull(tipoEval);
        if (tipo == null) {
            tipo = PdfTextUtil.trimToNull(tipoEvaluacion);
        }
        return tipo;
    }

    private void syncCamposDesdeObjetosInternal() {
        FichaPdfMappedData data = fichaPdfContextAssembler.syncCamposDesdeObjetos(
                fichaPdfDataMapper,
                ficha,
                empleadoSel,
                fechaNacimiento);
        this.institucion = data.institucion;
        this.ruc = data.ruc;
        this.centroTrabajo = data.centroTrabajo;
        this.ciiu = data.ciiu;
        this.noHistoria = data.noHistoria;
        this.noArchivo = data.noArchivo;
        this.ginecoExamen1 = data.ginecoExamen1;
        this.ginecoTiempo1 = data.ginecoTiempo1;
        this.ginecoResultado1 = data.ginecoResultado1;
        this.ginecoExamen2 = data.ginecoExamen2;
        this.ginecoTiempo2 = data.ginecoTiempo2;
        this.ginecoResultado2 = data.ginecoResultado2;
        this.ginecoObservacion = data.ginecoObservacion;
        this.enfermedadActual = data.enfermedadActual;
        if (data.apellido1 != null) {
            this.apellido1 = data.apellido1;
        }
        if (data.apellido2 != null) {
            this.apellido2 = data.apellido2;
        }
        if (data.nombre1 != null) {
            this.nombre1 = data.nombre1;
        }
        if (data.nombre2 != null) {
            this.nombre2 = data.nombre2;
        }
        if (data.edad != null) {
            this.edad = data.edad;
        }
    }

    public void prepararVistaPrevia() {
        controllerActionTemplate.executeWithResult(
                "prepararVistaPrevia",
                () -> centroMedicoPdfWorkflowService.prepararVistaPrevia(
                        new CentroMedicoPdfWorkflowService.PreparePreviewFlowCommand(
                                fichaPdfListo,
                                pdfTokenFicha,
                                this::verificarFichaCompleta,
                                buildPrepareFichaCommand(),
                                buildPrepareCertificadoCommand())),
                this::onPrepararVistaPreviaSuccess,
                LOG,
                activeStep,
                noPersonaSel,
                cedulaBusqueda);
    }

    private void onPrepararVistaPreviaSuccess(CentroMedicoPdfWorkflowService.PreviewFlowResult result) {
        applyPdfUiState(centroMedicoPdfUiCoordinator.onPrepararVistaPreviaSuccess(
                result,
                FacesContext.getCurrentInstance(),
                pdfSessionStore,
                pdfTokenCertificado));
    }

    private void cleanupPdfPreview(FacesContext ctx) {
        applyPdfUiState(centroMedicoPdfUiCoordinator.cleanupPdfPreview(ctx, pdfSessionStore, pdfTokenCertificado));
    }

    public void limpiarVistaPrevia() {
        centroMedicoPdfFacadeService.cleanupPdfPreview(
                FacesContext.getCurrentInstance(),
                pdfSessionStore,
                pdfTokenCertificado);
        applyCleanupPdfPreviewState();
    }

    private void applyCleanupPdfPreviewState() {
        applyPdfUiState(centroMedicoPdfUiCoordinator.applyCleanupPdfPreviewState(null));
    }

    // PDF - Certificado Médico
    public void prepararVistaPreviaCertificado() {
        onPrepararCertificadoPdf();
    }

    public void onPrepararCertificadoPdf() {
        controllerActionTemplate.executeWithResult(
                "onPrepararCertificadoPdf",
                () -> centroMedicoPdfWorkflowService.onPrepararCertificadoPdf(
                        new CentroMedicoPdfWorkflowService.PrepareCertificadoFlowCommand(
                                fichaPdfListo,
                                pdfTokenFicha,
                                buildPrepareFichaCommand(),
                                buildPrepareCertificadoCommand())),
                this::onPrepararCertificadoPdfSuccess,
                LOG,
                activeStep,
                noPersonaSel,
                cedulaBusqueda);
    }

    private void onPrepararCertificadoPdfSuccess(CentroMedicoPdfWorkflowService.CertificadoFlowResult result) {
        applyPdfUiState(centroMedicoPdfUiCoordinator.onPrepararCertificadoPdfSuccess(
                result,
                FacesContext.getCurrentInstance(),
                pdfSessionStore,
                pdfTokenCertificado));
    }

    private void showValidationMessage(FacesContext ctx, String summary, List<String> errors) {
        centroMedicoPdfFacadeService.showValidationMessage(ctx, summary, errors);
    }

    private String construirHtmlDesdePlantilla() throws java.io.IOException {
        CentroMedicoPdfUiCoordinator.ConstruirCertificadoHtmlCommand cmd = new CentroMedicoPdfUiCoordinator.ConstruirCertificadoHtmlCommand();
        cmd.ficha = ficha;
        cmd.fechaEmision = fechaEmision;
        cmd.aptitudSel = aptitudSel;
        cmd.tipoEval = tipoEval;
        cmd.tipoEvaluacion = tipoEvaluacion;
        cmd.institucion = institucion;
        cmd.ruc = ruc;
        cmd.noHistoria = noHistoria;
        cmd.noArchivo = noArchivo;
        cmd.centroTrabajo = centroTrabajo;
        cmd.ciiu = ciiu;
        cmd.apellido1 = apellido1;
        cmd.apellido2 = apellido2;
        cmd.nombre1 = nombre1;
        cmd.nombre2 = nombre2;
        cmd.sexo = sexo;
        cmd.detalleObservaciones = detalleObservaciones;
        cmd.recomendaciones = recomendaciones;
        cmd.medicoNombre = medicoNombre;
        cmd.medicoCodigo = medicoCodigo;
        cmd.pdfResourceResolver = pdfResourceResolver;
        cmd.pdfTemplateEngine = pdfTemplateEngine;
        cmd.certificadoPdfTemplateService = certificadoPdfTemplateService;
        return centroMedicoPdfUiCoordinator.construirHtmlDesdePlantilla(cmd);
    }

    private void applyPdfUiState(CentroMedicoPdfUiCoordinator.PdfUiState state) {
        centroMedicoPdfUiCoordinator.applyPdfUiState(
                state,
                pdfPreviewState,
                value -> this.ficha = value,
                value -> this.fichaPdfListo = value,
                value -> this.pdfTokenFicha = value,
                value -> this.certificadoListo = value,
                value -> this.pdfTokenCertificado = value,
                value -> this.pdfObjectUrl = value,
                value -> this.activeStep = value,
                value -> this.mostrarDlgCedula = value);
    }

    public void syncTipoEvaluacion() {
        this.tipoEvaluacion = this.tipoEval;
    }

    private void syncCie10PrincipalFromK() {

        if (codCie10Ppal != null && !codCie10Ppal.trim().isEmpty()) {
            return;
        }

        if (listaDiag == null || listaDiag.isEmpty()) {
            return;
        }

        ConsultaDiagnostico best = null;

        for (ConsultaDiagnostico r : listaDiag) {
            if (r == null) {
                continue;
            }
            String cod = r.getCodigo() != null ? r.getCodigo().trim() : "";
            if (cod.isEmpty()) {
                continue;
            }

            if ("D".equals(r.getTipoDiag())) {
                best = r;
                break;
            }
            if (best == null) {
                best = r;
            }
        }

        if (best != null) {
            codCie10Ppal = best.getCodigo();
            descCie10Ppal = best.getDescripcion();
        }
    }

    public List<Cie10> completarCie10(String query) {
        return cie10LookupService.completarPorCodigoODescripcion(query, 20);
    }

    public List<Cie10> completarCie10PorCodigo(String query) {
        return cie10LookupService.completarPorCodigo(query);
    }

    public List<Cie10> completarCie10PorDescripcion(String query) {
        return cie10LookupService.completarPorDescripcion(query, 20);
    }

    public void onCie10CodigoSelect(SelectEvent event) {
        String codigo = (String) event.getObject();
        this.codCie10Ppal = codigo;
        this.descCie10Ppal = cie10LookupService.buscarDescripcionPorCodigo(codigo);
    }

    public void onCie10CodigoBlur() {
        this.descCie10Ppal = cie10LookupService.buscarDescripcionPorCodigo(this.codCie10Ppal);
    }

    public void onCie10DescripcionSelect(SelectEvent event) {
        String descripcion = (String) event.getObject();
        this.descCie10Ppal = descripcion;
        this.codCie10Ppal = cie10LookupService.buscarCodigoPorDescripcion(descripcion);
    }

    public void onCie10DescripcionBlur() {
        this.codCie10Ppal = cie10LookupService.buscarCodigoPorDescripcion(this.descCie10Ppal);
    }

    private Cie10 inferCie10PrincipalFromListaK() {
        return cie10LookupService.inferirPrincipalDesdeLista(listaDiag);
    }

    public List<String> completarCie10FilaPorCodigo(String query) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            String viewId = (fc != null && fc.getViewRoot() != null) ? fc.getViewRoot().getViewId() : "null";
            LOG.info(">>> [AC-K-COD] complete ENTER query=[{}] viewId={}", query, viewId);

            List<String> out = cie10LookupService.completarFilaPorCodigo(query);

            LOG.info("<<< [AC-K-COD] RETURN out.size={}{}",
                    out.size(),
                    out.isEmpty() ? "" : " first=[" + out.get(0) + "]");
            return out;

        } catch (Exception e) {
            LOG.error("!!! [AC-K-COD] ERROR {} : {}", e.getClass().getName(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<String> completarCie10FilaPorDescripcion(String query) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            String viewId = (fc != null && fc.getViewRoot() != null) ? fc.getViewRoot().getViewId() : "null";
            LOG.info(">>> [AC-K-DESC] complete ENTER query=[{}] viewId={}", query, viewId);

            List<String> out = cie10LookupService.completarFilaPorDescripcion(query, 20);

            LOG.info("<<< [AC-K-DESC] RETURN out.size={}{}",
                    out.size(),
                    out.isEmpty() ? "" : " first=[" + out.get(0) + "]");
            return out;

        } catch (Exception e) {
            LOG.error("!!! [AC-K-DESC] ERROR {} : {}", e.getClass().getName(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public void abrirPersonaAuxManual() {
        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.abrirPersonaAuxManual(
                cedulaBusqueda,
                personaAux,
                ficha,
                empleadoSel,
                noPersonaSel,
                permitirIngresoManual,
                mostrarDlgCedula);
        applyPacienteUiFlow(result);
        this.mostrarDiaLOGoAux = result.isMostrarDialogoAux();
        for (String script : result.getScripts()) {
            PrimeFaces.current().executeScript(script);
        }
    }

    // Persona Auxiliar - Registro y Selección
    public void guardarPersonaAuxYUsar() {

        LOG.info(String.valueOf("INGRESA AL METODO DE GUARDAR "));
        LOG.info(String.valueOf("PERSONA AUXILIAR ANTES VALIDAR: " + personaAux));

        try {
            PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.guardarPersonaAuxYUsar(
                    personaAux,
                    ficha,
                    empleadoSel,
                    noPersonaSel);

            applyPacienteUiFlow(result);
            this.cedulaBusqueda = result.getCedulaBusqueda();
            this.apellido1 = result.getApellido1();
            this.apellido2 = result.getApellido2();
            this.nombre1 = result.getNombre1();
            this.nombre2 = result.getNombre2();
            this.sexo = result.getSexo();
            this.fechaNacimiento = result.getFechaNacimiento();
            this.noHistoria = result.getNoHistoria();
            this.mostrarDiaLOGoAux = result.isMostrarDialogoAux();
            this.mostrarDlgCedula = result.isMostrarDlgCedula();

            personaAuxDialogUiCoordinator.onGuardarSuccess(result);

            LOG.info("PersonaAux guardada manualmente: {} {} / {} {} (cedula={})",
                    personaAux.getApellido1(),
                    personaAux.getApellido2(),
                    personaAux.getNombre1(),
                    personaAux.getNombre2(),
                    personaAux.getCedula());

        } catch (PersonaAuxFlowService.PersonaAuxValidationException e) {
            LOG.warn("Validación PersonaAux en flujo manual: {}", e.getMessage());
            personaAuxDialogUiCoordinator.onValidationFailure(e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("Error guardando datos manuales", e);
            personaAuxDialogUiCoordinator.onTechnicalFailure();
        }
    }

    // Búsqueda de Cédula / Diálogo Inicial
    public void buscarCedula() {
        try {
            PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.buscarCedula(
                    cedulaBusqueda,
                    ficha,
                    personaAux,
                    permitirIngresoManual);
            applyCedulaSearchResult(result);

            if (result.isFound()) {
                cedulaDialogUiCoordinator.onFound(CedulaSearchService.CedulaSearchResult.found(
                        result.getCedulaBusqueda(),
                        result.getFicha(),
                        result.getPersonaAux(),
                        result.getEmpleadoSel(),
                        result.getNoPersonaSel(),
                        result.getApellido1(),
                        result.getApellido2(),
                        result.getNombre1(),
                        result.getNombre2(),
                        result.getSexo(),
                        result.getFechaNacimiento(),
                        result.getEdad()));
                if (result.isCargoNoEncontrado()) {
                    cedulaDialogUiCoordinator.showCargoMissing();
                }
            } else if (result.isShowManual()) {
                cedulaDialogUiCoordinator.onManualEnabled(CedulaSearchService.CedulaSearchResult.manual(
                        result.getCedulaBusqueda(),
                        result.getFicha(),
                        result.getPersonaAux()));
            }

            cedulaDialogUiCoordinator.refreshMainViews();
        } catch (CedulaSearchService.CedulaValidationException ex) {
            cedulaDialogUiCoordinator.onValidationWarning(ex.getMessage());
        } catch (RuntimeException ex) {
            messageService.handleUnexpected(LOG, "buscarCedula", ex, activeStep, noPersonaSel, cedulaBusqueda);
            cedulaDialogUiCoordinator.onSearchError();
        }
    }

    private void applyCedulaSearchResult(PacienteUiFlowCoordinator.UiFlowResult result) {
        this.cedulaBusqueda = result.getCedulaBusqueda();
        this.ficha = result.getFicha();
        this.personaAux = result.getPersonaAux();
        this.empleadoSel = result.getEmpleadoSel();
        this.noPersonaSel = result.getNoPersonaSel();

        if (result.isFound()) {
            this.apellido1 = result.getApellido1();
            this.apellido2 = result.getApellido2();
            this.nombre1 = result.getNombre1();
            this.nombre2 = result.getNombre2();
            this.sexo = result.getSexo();
            this.fechaNacimiento = result.getFechaNacimiento();
            this.edad = result.getEdad();
        }
        this.mostrarDlgCedula = result.isMostrarDlgCedula();
        applyPacienteUiFlow(result);
    }

    private void safeUpdate(String clientId) {
        try {

            PrimeFaces.current().ajax().update(clientId);
        } catch (RuntimeException ex) {

        }
    }

    public boolean isMostrarDlgCedula() {
        return mostrarDlgCedula;
    }

    public void setMostrarDlgCedula(boolean mostrarDlgCedula) {
        this.mostrarDlgCedula = mostrarDlgCedula;
    }

    private String esNulo(String s) {
        return s == null ? "" : s;
    }

    private String[] splitEnDos(String valor) {
        String res1 = null;
        String res2 = null;

        if (!isBlank(valor)) {
            String trimmed = valor.trim();
            String[] partes = trimmed.split("\\s+");
            if (partes.length == 1) {
                res1 = partes[0];
            } else if (partes.length > 1) {
                res1 = partes[0];

                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < partes.length; i++) {
                    if (i > 1) {
                        sb.append(' ');
                    }
                    sb.append(partes[i]);
                }
                res2 = sb.toString();
            }
        }

        return new String[]{res1, res2};
    }

    public void prepararIngresoManual() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            PersonaAuxFlowService.ManualPreparationResult result = personaAuxFlowService.prepararIngresoManual(
                    cedulaBusqueda,
                    personaAux);
            this.personaAux = result.getPersonaAux();
            this.permitirIngresoManual = result.isPermitirIngresoManual();
        } catch (PersonaAuxFlowService.PersonaAuxValidationException ex) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Cédula requerida",
                    ex.getMessage()
            ));
        }
    }

    private String primerToken(String texto) {
        if (esVacio(texto)) {
            return null;
        }
        String[] partes = texto.trim().split("\\s+");
        return partes[0];
    }

    private String restoTokens(String texto) {
        if (esVacio(texto)) {
            return null;
        }
        String[] partes = texto.trim().split("\\s+");
        if (partes.length <= 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < partes.length; i++) {
            if (i > 1) {
                sb.append(' ');
            }
            sb.append(partes[i]);
        }
        return sb.toString();
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
        return activeStep;
    }

    public void setStepActual(String stepActual) {
        this.activeStep = stepActual;
    }

    public String getActiveStep() {
        return this.activeStep;
    }

    public void setActiveStep(String activeStep) {
        this.activeStep = activeStep;
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

    private boolean filaActLabTieneAlgo(int i) {

        if (!isBlank(actLabCentroTrabajo.get(i))) {
            return true;
        }
        if (!isBlank(actLabActividad.get(i))) {
            return true;
        }
        if (!isBlank(actLabTiempo.get(i))) {
            return true;
        }
        if (!isBlank(actLabObservaciones.get(i))) {
            return true;
        }
        if (!isBlank(iessEspecificar.get(i))) {
            return true;
        }

        if (Boolean.TRUE.equals(actLabTrabajoAnterior.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(actLabTrabajoActual.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(actLabIncidenteChk.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(actLabAccidenteChk.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(actLabEnfermedadChk.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(iessSi.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(iessNo.get(i))) {
            return true;
        }

        if (iessFecha.get(i) != null) {
            return true;
        }

        return false;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getProcessStepId() {

        return ":wiz:" + activeStep;
    }

    private static final int CONS_ROWS = 3;

    private void initConsumoVidaCond() {

        final int N = CONS_ROWS;

        if (consTiempoConsumoMeses == null) {
            consTiempoConsumoMeses = new Integer[N];
        }
        if (consExConsumidor == null) {
            consExConsumidor = new Boolean[N];
        }
        if (consTiempoAbstinenciaMeses == null) {
            consTiempoAbstinenciaMeses = new Integer[N];
        }
        if (consNoConsume == null) {
            consNoConsume = new Boolean[N];
        }

        if (afCual == null) {
            afCual = new String[N];
        }
        if (afTiempo == null) {
            afTiempo = new String[N];
        }

        if (medCual == null) {
            medCual = new String[N];
        }
        if (medCant == null) {
            medCant = new Integer[N];
        }

        for (int i = 0; i < N; i++) {
            if (consExConsumidor[i] == null) {
                consExConsumidor[i] = Boolean.FALSE;
            }
            if (consNoConsume[i] == null) {
                consNoConsume[i] = Boolean.FALSE;
            }
        }

        if (consumoVidaCondObs == null) {
            consumoVidaCondObs = "";
        }
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
        return nRealizaEvaluacion;
    }

    public void setNRealizaEvaluacion(String nRealizaEvaluacion) {
        this.nRealizaEvaluacion = nRealizaEvaluacion;
    }

    public String getNRelacionTrabajo() {
        return nRelacionTrabajo;
    }

    public void setNRelacionTrabajo(String nRelacionTrabajo) {
        this.nRelacionTrabajo = nRelacionTrabajo;
    }

    public String getNObsRetiro() {
        return nObsRetiro;
    }

    public void setNObsRetiro(String nObsRetiro) {
        this.nObsRetiro = nObsRetiro;
    }

    public Map<String, Boolean> getRiesgos() {
        if (riesgos == null) {
            riesgos = new java.util.LinkedHashMap<>();
        }
        return riesgos;
    }

    public Map<String, String> getOtrosRiesgos() {
        if (otrosRiesgos == null) {
            otrosRiesgos = new java.util.LinkedHashMap<>();
        }
        return otrosRiesgos;
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

    public long getTs() {
        return System.currentTimeMillis();
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

    public void initConsumoVidaCondDefaults() {
        initConsumoVidaCond();
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
        return actividadesLab;
    }

    public void setActividadesLab(List<String> actividadesLab) {
        this.actividadesLab = actividadesLab;
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
        return apAdultoMayor;
    }

    public void setApAdultoMayor(boolean apAdultoMayor) {
        this.apAdultoMayor = apAdultoMayor;
    }

    public boolean isApCatastrofica() {
        return apCatastrofica;
    }

    public void setApCatastrofica(boolean apCatastrofica) {
        this.apCatastrofica = apCatastrofica;
    }

    public boolean isApDiscapacidad() {
        return apDiscapacidad;
    }

    public void setApDiscapacidad(boolean apDiscapacidad) {
        this.apDiscapacidad = apDiscapacidad;
    }

    public boolean isApEmbarazada() {
        return apEmbarazada;
    }

    public void setApEmbarazada(boolean apEmbarazada) {
        this.apEmbarazada = apEmbarazada;
    }

    public boolean isApLactancia() {
        return apLactancia;
    }

    public void setApLactancia(boolean apLactancia) {
        this.apLactancia = apLactancia;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public String getAptitudSel() {
        return aptitudSel;
    }

    public void setAptitudSel(String aptitudSel) {
        this.aptitudSel = aptitudSel;
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
        return certificadoListo;
    }

    public void setCertificadoListo(boolean certificadoListo) {
        this.certificadoListo = certificadoListo;
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
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
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
        return fc;
    }

    public void setFc(Integer fc) {
        this.fc = fc;
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
        return fichaRiesgo;
    }

    public void setFichaRiesgo(FichaRiesgo fichaRiesgo) {
        this.fichaRiesgo = fichaRiesgo;
    }

    public Integer getFr() {
        return fr;
    }

    public void setFr(Integer fr) {
        this.fr = fr;
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
        return imc;
    }

    public void setImc(Double imc) {
        this.imc = imc;
    }

    public String getLateralidad() {
        return lateralidad;
    }

    public void setLateralidad(String lateralidad) {
        this.lateralidad = lateralidad;
    }

    public List<ConsultaDiagnostico> getListaDiag() {
        return listaDiag;
    }

    public void setListaDiag(List<ConsultaDiagnostico> listaDiag) {
        this.listaDiag = listaDiag;
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
        return medicoCodigo;
    }

    public void setMedicoCodigo(String medicoCodigo) {
        this.medicoCodigo = medicoCodigo;
    }

    public String getMedicoNombre() {
        return medicoNombre;
    }

    public void setMedicoNombre(String medicoNombre) {
        this.medicoNombre = medicoNombre;
    }

    public List<String> getMedidasPreventivas() {
        return medidasPreventivas;
    }

    public void setMedidasPreventivas(List<String> medidasPreventivas) {
        this.medidasPreventivas = medidasPreventivas;
    }

    public String getNombre1() {
        return nombre1;
    }

    public void setNombre1(String nombre1) {
        this.nombre1 = nombre1;
    }

    public String getNombre2() {
        return nombre2;
    }

    public void setNombre2(String nombre2) {
        this.nombre2 = nombre2;
    }

    public String getObsJ() {
        return obsJ;
    }

    public void setObsJ(String obsJ) {
        this.obsJ = obsJ;
    }

    public void setOtrosRiesgos(Map<String, String> otrosRiesgos) {
        this.otrosRiesgos = otrosRiesgos;
    }

    public String getPaStr() {
        return paStr;
    }

    public void setPaStr(String paStr) {
        this.paStr = paStr;
    }

    public Integer getPartos() {
        return partos;
    }

    public void setPartos(Integer partos) {
        this.partos = partos;
    }

    public String getPdfToken() {
        return pdfTokenCertificado;
    }

    public void setPdfToken(String pdfTokenCertificado) {
        this.pdfTokenCertificado = pdfTokenCertificado;
    }

    public Double getPerimetroAbd() {
        return perimetroAbd;
    }

    public void setPerimetroAbd(Double perimetroAbd) {
        this.perimetroAbd = perimetroAbd;
    }

    public boolean isPermitirIngresoManual() {
        return permitirIngresoManual;
    }

    public void setPermitirIngresoManual(boolean permitirIngresoManual) {
        this.permitirIngresoManual = permitirIngresoManual;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    public void setRiesgos(Map<String, Boolean> riesgos) {
        this.riesgos = riesgos;
    }

    public Integer getSatO2() {
        return satO2;
    }

    public void setSatO2(Integer satO2) {
        this.satO2 = satO2;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Double getTallaCm() {
        return tallaCm;
    }

    public void setTallaCm(Double tallaCm) {
        this.tallaCm = tallaCm;
    }

    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
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

    private String getCompValueDebug(UIComponent comp) {
        if (comp == null) {
            LOG.info("... [VAL] comp=null");
            return null;
        }

        String clientId;
        try {
            clientId = comp.getClientId(FacesContext.getCurrentInstance());
        } catch (RuntimeException e) {
            clientId = String.valueOf(comp.getId());
        }

        Object idxAttr = comp.getAttributes() != null ? comp.getAttributes().get("idx") : null;

        Object submitted = null;
        Object local = null;
        Object value = null;

        if (comp instanceof UIInput) {
            UIInput in = (UIInput) comp;
            submitted = in.getSubmittedValue();
            local = in.getLocalValue();
            value = in.getValue();
        }

        LOG.debug("... [VAL] compId=" + comp.getId()
                + " clientId=" + clientId
                + " idxAttr=" + idxAttr
                + " submitted=" + submitted
                + " local=" + local
                + " value=" + value);

        if (submitted != null) {
            return submitted.toString();
        }
        if (value != null) {
            return value.toString();
        }
        if (local != null) {
            return local.toString();
        }
        return null;
    }

    private Integer extraerIdx(UIComponent comp) {
        if (comp == null) {
            LOG.info("... [IDX] extraerIdx comp=null");
            return null;
        }
        Object idxObj = comp.getAttributes().get("idx");

        String clientId;
        try {
            clientId = comp.getClientId(FacesContext.getCurrentInstance());
        } catch (RuntimeException e) {
            clientId = String.valueOf(comp.getId());
        }

        LOG.debug("... [IDX] extraerIdx compId=" + comp.getId()
                + " clientId=" + clientId
                + " idxAttr=" + idxObj);

        if (idxObj == null) {
            return null;
        }
        try {
            return Integer.parseInt(idxObj.toString());
        } catch (NumberFormatException e) {
            LOG.info("... [IDX] extraerIdx parse ERROR idxAttr={} ex={}", idxObj, e.toString());
            return null;
        }
    }

    private ConsultaDiagnostico getDiagRow(Integer idx, String contexto) {
        if (idx == null || listaDiag == null || idx < 0 || idx >= listaDiag.size()) {
            LOG.info("<<< [{}] idx INVALID => {}", contexto, idx);
            return null;
        }
        return listaDiag.get(idx);
    }

    public void onKCieCodigoSelect(SelectEvent<String> event) {
        UIComponent comp = (event != null ? event.getComponent() : null);
        Integer idx = extraerIdx(comp);
        String selected = (event != null ? event.getObject() : null);

        LOG.info(">>> [AC-K-COD] itemSelect idx=" + idx
                + " selected=[" + selected + "] clientId=" + (comp != null ? comp.getClientId() : "null"));

        ConsultaDiagnostico row = getDiagRow(idx, "AC-K-COD itemSelect");
        if (row == null) {
            return;
        }

        if (selected == null || selected.trim().isEmpty()) {
            LOG.info("<<< [AC-K-COD] itemSelect empty selection => no-op");
            return;
        }

        String codigo = selected.trim().toUpperCase();
        row.setCodigo(codigo);

        Cie10 cie = cie10Service.buscarPorCodigo(codigo);
        LOG.info("... [AC-K-COD] itemSelect buscarPorCodigo(" + codigo + ") => " + (cie != null ? cie.getCodigo() : "null"));

        if (cie != null) {
            row.setCodigo(cie.getCodigo());
            row.setDescripcion(cie.getDescripcion());
            row.setCie10(cie);
        } else {
            row.setDescripcion(null);
            row.setCie10(null);
        }

        LOG.info("<<< [AC-K-COD] itemSelect AFTER codigo=[" + row.getCodigo() + "] desc=[" + row.getDescripcion() + "]");
    }

    public void abrirDialogoDiagnostico(AjaxBehaviorEvent event) {
        UIComponent comp = (event != null ? event.getComponent() : null);
        Integer idx = extraerIdx(comp);
        ConsultaDiagnostico row = getDiagRow(idx, "K-DLG abrir");
        if (row == null) {
            return;
        }

        dialogDiagnosticoIdx = idx;
        codCie10Ppal = row.getCodigo();
        descCie10Ppal = row.getDescripcion();
        PrimeFaces.current().executeScript("PF('kDiagDialogWv').show();");
    }

    public void aceptarDialogoDiagnostico() {
        ConsultaDiagnostico row = getDiagRow(dialogDiagnosticoIdx, "K-DLG aceptar");
        if (row == null) {
            return;
        }

        String codigo = codCie10Ppal != null ? codCie10Ppal.trim().toUpperCase() : "";
        String descripcion = descCie10Ppal != null ? descCie10Ppal.trim() : "";

        Cie10 cie = null;
        if (!codigo.isEmpty()) {
            cie = cie10Service.buscarPorCodigo(codigo);
        }
        if (cie == null && !descripcion.isEmpty()) {
            cie = cie10Service.buscarPrimeroPorDescripcion(descripcion);
        }

        if (cie != null) {
            row.setCodigo(cie.getCodigo());
            row.setDescripcion(cie.getDescripcion());
            row.setCie10(cie);
        } else {
            row.setCodigo(codigo.isEmpty() ? null : codigo);
            row.setDescripcion(descripcion.isEmpty() ? null : descripcion);
            row.setCie10(null);
        }

        syncCie10PrincipalFromK();
        PrimeFaces.current().executeScript("PF('kDiagDialogWv').hide();");
    }

    public void cerrarDialogoDiagnostico() {
        PrimeFaces.current().executeScript("PF('kDiagDialogWv').hide();");
    }

    public void onKCieCodigoBlur(AjaxBehaviorEvent event) {
        UIComponent comp = event != null ? event.getComponent() : null;
        Integer idx = extraerIdx(comp);

        String clientId = safeClientId(comp);
        String typed = getAutoCompleteTypedRobusto(comp);

        LOG.info(">>> [AC-K-COD] blur idx=" + idx + " clientId=" + clientId + " typed=[" + typed + "]");

        ConsultaDiagnostico row = getDiagRow(idx, "AC-K-COD blur");
        if (row == null) {
            return;
        }

        String codigo = typed != null ? typed.trim() : "";
        if (codigo.isEmpty()) {
            row.setCodigo(null);
            row.setDescripcion(null);
            row.setCie10(null);
            LOG.info("<<< [AC-K-COD] blur empty => cleared row");
            return;
        }

        String codigoUp = codigo.toUpperCase();

        if (codigoUp.length() < 3) {
            row.setCodigo(codigoUp);
            row.setDescripcion(null);
            row.setCie10(null);
            LOG.info("<<< [AC-K-COD] blur partial [" + codigoUp + "] => keep, no exact lookup");
            return;
        }

        Cie10 cie = cie10Service.buscarPorCodigo(codigoUp);

        if (cie == null) {
            List<Cie10> sugerencias = cie10Service.buscarJerarquiaPorTerm(codigoUp);
            if (sugerencias != null) {
                String codigoNorm = codigoUp.replaceAll("[^A-Z0-9]", "");
                for (Cie10 candidato : sugerencias) {
                    if (candidato == null || candidato.getCodigo() == null) {
                        continue;
                    }

                    String candidatoNorm = candidato.getCodigo().toUpperCase().replaceAll("[^A-Z0-9]", "");
                    if (candidatoNorm.equals(codigoNorm)) {
                        cie = candidato;
                        break;
                    }
                }
            }
        }

        LOG.debug("... [AC-K-COD] buscarPorCodigo(" + codigoUp + ") => "
                + (cie != null ? (cie.getCodigo() + " | " + cie.getDescripcion()) : "null"));
        if (cie != null) {
            row.setCodigo(cie.getCodigo());
            row.setDescripcion(cie.getDescripcion());
            row.setCie10(cie);
            LOG.info("<<< [AC-K-COD] blur AFTER MATCH codigo=[" + row.getCodigo() + "] desc=[" + row.getDescripcion() + "]");
            return;
        }

        row.setCodigo(codigoUp);
        row.setDescripcion(null);
        row.setCie10(null);
        LOG.info("<<< [AC-K-COD] blur AFTER NO-MATCH keep codigo=[" + row.getCodigo() + "]");
    }

    public void onKDescSelect(SelectEvent<String> event) {
        String descripcion = event.getObject();
        UIComponent comp = event.getComponent();
        Integer idx = extraerIdx(comp);

        LOG.info(">>> [AC-K-DESC] itemSelect ENTER desc=[" + descripcion + "] idx=" + idx);

        ConsultaDiagnostico row = getDiagRow(idx, "AC-K-DESC itemSelect");
        if (row == null) {
            return;
        }

        row.setDescripcion(descripcion);

        if (descripcion != null && !descripcion.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(descripcion.trim());
            LOG.info("... [AC-K-DESC] buscarPrimeroPorDescripcion => " + (cie != null ? cie.getCodigo() : "null"));

            if (cie != null) {
                row.setCodigo(cie.getCodigo());
                row.setCie10(cie);
            } else {
                row.setCodigo(null);
                row.setCie10(null);
            }
        }

        LOG.info("<<< [AC-K-DESC] itemSelect row AFTER idx=" + idx
                + " codigo=[" + row.getCodigo() + "] desc=[" + row.getDescripcion() + "]");

        syncCie10PrincipalFromK();
    }

    public void onKDescBlur(AjaxBehaviorEvent event) {
        UIComponent comp = (event != null ? event.getComponent() : null);
        Integer idx = extraerIdx(comp);
        String clientId = safeClientId(comp);
        String typed = getAutoCompleteTypedRobusto(comp);

        LOG.info(">>> [AC-K-DESC] blur idx=" + idx + " clientId=" + clientId + " typed=[" + typed + "]");

        ConsultaDiagnostico row = getDiagRow(idx, "AC-K-DESC blur");
        if (row == null) {
            return;
        }

        String desc = (typed != null ? typed.trim() : "");
        if (desc.isEmpty()) {
            row.setDescripcion(null);
            row.setCie10(null);
            LOG.info("<<< [AC-K-DESC] blur empty => cleared descripcion");
            return;
        }

        row.setDescripcion(desc);

        if (desc.length() < 4) {
            row.setCie10(null);
            LOG.info("<<< [AC-K-DESC] blur partial => keep desc only");
            return;
        }

        Cie10 cie = null;
        try {
            List<Cie10> candidatos = cie10Service.buscarPorDescripcionLike(desc, 20);
            LOG.info("... [AC-K-DESC] candidatos.size=" + (candidatos == null ? "null" : candidatos.size()));
            if (candidatos != null && !candidatos.isEmpty()) {
                cie = cie10LookupService.buscarMejorCoincidenciaPorDescripcion(candidatos, desc);
            }
        } catch (RuntimeException e) {
            LOG.error("!!! [AC-K-DESC] error: {}", e.getMessage(), e);
        }

        LOG.debug("... [AC-K-DESC] pickBest => "
                + (cie != null ? (cie.getCodigo() + " | " + cie.getDescripcion()) : "null"));

        if (cie != null) {
            row.setCodigo(cie.getCodigo());
            row.setDescripcion(cie.getDescripcion());
            row.setCie10(cie);
            LOG.info("<<< [AC-K-DESC] blur AFTER MATCH codigo=[" + row.getCodigo() + "] desc=[" + row.getDescripcion() + "]");
            return;
        }

        row.setCie10(null);
        LOG.info("<<< [AC-K-DESC] blur AFTER NO-MATCH keep desc=[" + row.getDescripcion() + "]");
    }

    public void onKTipoChange(AjaxBehaviorEvent event) {
        UIComponent comp = (event != null ? event.getComponent() : null);
        Integer idx = extraerIdx(comp);

        String clientId = safeClientId(comp);

        LOG.info(">>> [K-TIPO] change ENTER idx=" + idx + " clientId=" + clientId);

        ConsultaDiagnostico row = getDiagRow(idx, "K-TIPO change");
        if (row == null) {
            return;
        }

        LOG.info("<<< [K-TIPO] AFTER idx=" + idx
                + " codigo=[" + row.getCodigo() + "]"
                + " desc=[" + row.getDescripcion() + "]"
                + " tipo=[" + row.getTipoDiag() + "]");
    }

    private String getAutoCompleteTypedRobusto(UIComponent comp) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc == null || comp == null) {
                return null;
            }

            String base = comp.getClientId(fc);
            Map<String, String> params = fc.getExternalContext().getRequestParameterMap();

            String[] keys = new String[]{
                base + "_input",
                base,
                base + "_hinput",
                base + "_query"
            };

            for (String k : keys) {
                String v = params.get(k);
                if (v != null) {
                    LOG.info("... [REQ] AC typed key=" + k + " => [" + v + "]");
                    return v;
                }
            }

            LOG.warn("!!! [REQ] AC typed NOT FOUND for base=" + base
                    + " (tried _input, base, _hinput, _query)");
            return null;

        } catch (RuntimeException e) {
            LOG.error("!!! [REQ] getAutoCompleteTypedRobusto ERROR: {}", e.getMessage(), e);
            return null;
        }
    }

    private String safeClientId(UIComponent comp) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            return (fc != null && comp != null) ? comp.getClientId(fc) : "null";
        } catch (RuntimeException e) {
            return "err:" + e.getMessage();
        }
    }

    public String getStepProcessId() {

        return "@([id$=" + activeStep + "])";
    }

    private Date toDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date d) {
            return d;
        }
        if (value instanceof LocalDate ld) {
            return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    public void autoOpenCedulaIfNeeded() {

        boolean open = false;

        if (!cedulaDlgAutoOpened) {

            if (mostrarDlgCedula && "step1".equals(activeStep)) {
                open = true;
                cedulaDlgAutoOpened = true;
            }
        }

        org.primefaces.PrimeFaces.current().ajax().addCallbackParam("openCedulaDlg", open);
    }

    public void consumirAutoOpenCedulaDlg() {
        boolean open = false;

        if ("step1".equals(activeStep)
                && empleadoSel == null
                && !cedulaDlgAutoOpened) {

            open = true;
            cedulaDlgAutoOpened = true;
        }

        PrimeFaces.current().ajax().addCallbackParam("openCedulaDlg", open);
    }

    public boolean isCedulaDlgAutoOpened() {
        return cedulaDlgAutoOpened;
    }

    public String getDialogDiagnosticoCodigo() {
        return dialogDiagnosticoCodigo;
    }

    public void setDialogDiagnosticoCodigo(String dialogDiagnosticoCodigo) {
        this.dialogDiagnosticoCodigo = dialogDiagnosticoCodigo;
    }

    public String getDialogDiagnosticoDescripcion() {
        return dialogDiagnosticoDescripcion;
    }

    public void setDialogDiagnosticoDescripcion(String dialogDiagnosticoDescripcion) {
        this.dialogDiagnosticoDescripcion = dialogDiagnosticoDescripcion;
    }

    public void setCedulaDlgAutoOpened(boolean cedulaDlgAutoOpened) {
        this.cedulaDlgAutoOpened = cedulaDlgAutoOpened;
    }

    public boolean isPreRenderDone() {
        return preRenderDone;
    }

    public void setPreRenderDone(boolean preRenderDone) {
        this.preRenderDone = preRenderDone;
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
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getnRealizaEvaluacion() {
        return nRealizaEvaluacion;
    }

    public void setnRealizaEvaluacion(String nRealizaEvaluacion) {
        this.nRealizaEvaluacion = nRealizaEvaluacion;
    }

    public String getnRelacionTrabajo() {
        return nRelacionTrabajo;
    }

    public void setnRelacionTrabajo(String nRelacionTrabajo) {
        this.nRelacionTrabajo = nRelacionTrabajo;
    }

    public String getnObsRetiro() {
        return nObsRetiro;
    }

    public void setnObsRetiro(String nObsRetiro) {
        this.nObsRetiro = nObsRetiro;
    }

    public String getCodCie10Ppal() {
        return codCie10Ppal;
    }

    public void setCodCie10Ppal(String codCie10Ppal) {
        this.codCie10Ppal = codCie10Ppal;
    }

    public String getDescCie10Ppal() {
        return descCie10Ppal;
    }

    public void setDescCie10Ppal(String descCie10Ppal) {
        this.descCie10Ppal = descCie10Ppal;
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
        return pdfObjectUrl;
    }

    public void setPdfObjectUrl(String pdfObjectUrl) {
        this.pdfObjectUrl = pdfObjectUrl;
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

    public FichaActLaboralService getFichaActLaboralService() {
        return fichaActLaboralService;
    }

    public void setFichaActLaboralService(FichaActLaboralService fichaActLaboralService) {
        this.fichaActLaboralService = fichaActLaboralService;
    }

    public FichaExamenCompService getFichaExamenCompService() {
        return fichaExamenCompService;
    }

    public void setFichaExamenCompService(FichaExamenCompService fichaExamenCompService) {
        this.fichaExamenCompService = fichaExamenCompService;
    }

    public void onDlgCedulaHide() {
        mostrarDlgCedula = false;
    }

    public void onDlgCedulaClose() {
        mostrarDlgCedula = false;
    }

    public String getPdfTokenCertificado() {
        return pdfTokenCertificado;
    }

    private static String safe(String s) {
        if (s == null) {
            return "";
        }
        String out = s;
        out = out.replace("&", "&amp;");
        out = out.replace("<", "&lt;");
        out = out.replace(">", "&gt;");
        out = out.replace("\"", "&quot;");

        out = out.replace("'", "&#39;");
        return out;
    }

    /**
     * Convierte valores tipo SI/NO (S/N, SI/NO, true/false) a marca "X" o
     * vacío. - Si es "S"/"SI"/"TRUE"/"1"/"X" => "X" - Caso contrario (incluye
     * "N"/"NO"/null) => ""
     */
    private static String markX(Object v) {
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            return "";
        }
        s = s.toUpperCase();
        if ("S".equals(s) || "SI".equals(s) || "TRUE".equals(s) || "1".equals(s) || "X".equals(s) || "✔".equals(s)) {
            return "X";
        }
        return "";
    }

    private static boolean isYes(Object v) {
        if (v == null) {
            return false;
        }
        String s = String.valueOf(v).trim().toUpperCase();
        return "S".equals(s) || "SI".equals(s) || "TRUE".equals(s) || "1".equals(s) || "X".equals(s) || "✔".equals(s);
    }

    private static boolean isNo(Object v) {
        if (v == null) {
            return false;
        }
        String s = String.valueOf(v).trim().toUpperCase();
        return "N".equals(s) || "NO".equals(s) || "FALSE".equals(s) || "0".equals(s);
    }

    private static String normalizarXhtml(String html) {
        if (html == null) {
            return "";
        }
        String out = html;

        out = out.replace("&nbsp;", " ");

        out = out.replaceAll("(?i)<br(\s*)>", "<br/>");

        out = out.replaceAll("(?i)<hr(\s*)>", "<hr/>");

        return out;
    }

    private String val(Object root, String path) {
        if (root == null || path == null || path.isBlank()) {
            return "";
        }
        Object cur = root;
        for (String part : path.split("\\.")) {
            if (cur == null) {
                return "";
            }
            cur = readProperty(cur, part);
        }
        return cur == null ? "" : String.valueOf(cur);
    }

    private Object readProperty(Object obj, String prop) {
        try {
            // intenta getProp()
            String m = "get" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
            return obj.getClass().getMethod(m).invoke(obj);
        } catch (Exception ignore) {
            try {
                // intenta isProp()
                String m = "is" + Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
                return obj.getClass().getMethod(m).invoke(obj);
            } catch (Exception ignore2) {
                try {
                    // intenta campo directo
                    java.lang.reflect.Field f = obj.getClass().getDeclaredField(prop);
                    f.setAccessible(true);
                    return f.get(obj);
                } catch (Exception ignore3) {
                    return null;
                }
            }
        }
    }

    private String fmtDate(java.util.Date d) {
        if (d == null) {
            return "";
        }
        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(d);
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

    void cargarAtencionPrioritaria(Map<String, String> rep) {

        rep.put("apCatastrofica_sn", apCatastrofica ? "SI" : "NO");
        rep.put("apDiscapacidad_sn", apDiscapacidad ? "SI" : "NO");
        rep.put("apEmbarazada_sn", apEmbarazada ? "SI" : "NO");
        rep.put("apLactancia_sn", apLactancia ? "SI" : "NO");
        rep.put("apAdultoMayor_sn", apAdultoMayor ? "SI" : "NO");

        boolean aplicaDis = false;
        boolean aplicaCat = false;

        if (ficha != null) {
            aplicaDis = "S".equalsIgnoreCase(safe(ficha.getApDiscapacidad()));
            aplicaCat = "S".equalsIgnoreCase(safe(ficha.getApCatastrofica()));
        } else {
            aplicaDis = apDiscapacidad;
            aplicaCat = apCatastrofica;
        }

        // Engine no soporta if => ocultamos por style
        rep.put("apDiscapacidad_style", aplicaDis ? "" : "display:none;");
        rep.put("apCatastrofica_style", aplicaCat ? "" : "display:none;");

        rep.put("disTipo", safe(ficha != null ? ficha.getDisTipo() : null));
        rep.put("disDescripcion", safe(ficha != null ? ficha.getDisDescripcion() : null));

        Integer porc = (ficha != null ? ficha.getDisPorcentaje() : null);
        rep.put("disPorcentaje", porc == null ? "" : String.valueOf(porc)); // la plantilla le pone “%” si quieres

        rep.put("catDiagnostico", safe(ficha != null ? ficha.getCatDiagnostico() : null));

        String catCal = safe(ficha != null ? ficha.getCatCalificada() : null);
        if ("S".equalsIgnoreCase(catCal) || "TRUE".equalsIgnoreCase(catCal)) {
            rep.put("catCalificada", "SI");
        } else if ("N".equalsIgnoreCase(catCal) || "FALSE".equalsIgnoreCase(catCal)) {
            rep.put("catCalificada", "NO");
        } else {
            rep.put("catCalificada", catCal); // por si viene vacío o ya viene SI/NO
        }
        LOG.info("[PDF] disTipo=" + rep.get("disTipo")
                + " disDescripcion=" + rep.get("disDescripcion")
                + " disPorcentaje=" + rep.get("disPorcentaje")
                + " catDiagnostico=" + rep.get("catDiagnostico")
                + " catCalificada=" + rep.get("catCalificada")
                + " styleDis=" + rep.get("apDiscapacidad_style")
                + " styleCat=" + rep.get("apCatastrofica_style"));

    }

    private void cargarActividadLaboralArrays(Map<String, String> rep) {
        for (int i = 0; i < H_ROWS; i++) {
            int idx = i + 1; // índice 1-based
            rep.put("h_centro_" + idx, safe(getSafe(actLabCentroTrabajo, i)));
            rep.put("h_actividad_" + idx, safe(getSafe(actLabActividad, i)));
            rep.put("h_tiempo_" + idx, safe(getSafe(actLabTiempo, i)));
            rep.put("h_anterior_" + idx, isTrue(getSafe(actLabTrabajoAnterior, i)) ? "X" : "");
            rep.put("h_actual_" + idx, isTrue(getSafe(actLabTrabajoActual, i)) ? "X" : "");
            rep.put("h_incidente_" + idx, isTrue(getSafe(actLabIncidenteChk, i)) ? "X" : "");
            rep.put("h_accidente_" + idx, isTrue(getSafe(actLabAccidenteChk, i)) ? "X" : "");
            rep.put("h_enfermedad_" + idx, isTrue(getSafe(actLabEnfermedadChk, i)) ? "X" : "");
            rep.put("h_iess_si_" + idx, isTrue(getSafe(iessSi, i)) ? "X" : "");
            rep.put("h_iess_no_" + idx, isTrue(getSafe(iessNo, i)) ? "X" : "");
            rep.put("h_iess_fecha_" + idx, fmtDate(toDate(getSafe(iessFecha, i))));
            rep.put("h_iess_especificar_" + idx, safe(getSafe(iessEspecificar, i)));
            rep.put("h_obs_" + idx, safe(getSafe(actLabObservaciones, i)));
        }
    }

    /**
     * Retorna el primer String no nulo y no vacío (trim).
     */
    private static String firstNonEmpty(String... vals) {
        if (vals == null) {
            return null;
        }
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) {
                return v;
            }
        }
        return null;
    }

    /**
     * Intenta obtener un String desde la entidad Ficha mediante reflexión. Útil
     * cuando el bean del controlador no tiene el valor pero la entidad sí.
     */
    private static String getFichaStringByReflection(Object fo, String... getterNames) {
        if (fo == null || getterNames == null) {
            return null;
        }
        for (String g : getterNames) {
            if (g == null || g.trim().isEmpty()) {
                continue;
            }
            try {
                java.lang.reflect.Method m = fo.getClass().getMethod(g);
                Object r = m.invoke(fo);
                if (r != null) {
                    String s = String.valueOf(r);
                    if (!s.trim().isEmpty()) {
                        return s;
                    }
                }
            } catch (Exception ignore) {
                // si no existe el getter, seguimos con el siguiente
            }
        }
        return null;
    }

    public void onToggleDiscapacidad() {
        if (!apDiscapacidad) {
            discapTipo = null;
            discapDesc = null;
            discapPorc = null;
        }
    }

    public void onToggleCatastrofica() {
        if (!apCatastrofica) {
            catasDiagnostico = null;
            catasCalificada = null;
        }
    }

    /**
     * PARA MANEJAR LA GENERACION Y REGENERACION
     *
     * @return
     */
    public void reloadFichaDesdeBd() {
        if (this.ficha == null || this.ficha.getIdFicha() == null) {
            return;
        }
        this.ficha = fichaService.findById(this.ficha.getIdFicha());
    }

    public String getDiscapTipo() {
        return discapTipo;
    }

    public void setDiscapTipo(String discapTipo) {
        this.discapTipo = discapTipo;
    }

    public String getDiscapDesc() {
        return discapDesc;
    }

    public void setDiscapDesc(String discapDesc) {
        this.discapDesc = discapDesc;
    }

    public Integer getDiscapPorc() {
        return discapPorc;
    }

    public void setDiscapPorc(Integer discapPorc) {
        this.discapPorc = discapPorc;
    }

    public String getCatasDiagnostico() {
        return catasDiagnostico;
    }

    public void setCatasDiagnostico(String catasDiagnostico) {
        this.catasDiagnostico = catasDiagnostico;
    }

    public Boolean getCatasCalificada() {
        return catasCalificada;
    }

    public void setCatasCalificada(Boolean catasCalificada) {
        this.catasCalificada = catasCalificada;
    }

    public boolean isCertPdfListo() {
        return certificadoListo;
    }

    public String getFichaToken() {
        return pdfTokenFicha;
    }

    public String getCertToken() {
        return pdfTokenCertificado;
    }

    private int stepIndex = 1;

    public boolean isFichaPdfListo() {
        return fichaPdfListo;
    }

    public String getPdfTokenFicha() {
        return pdfTokenFicha;
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
