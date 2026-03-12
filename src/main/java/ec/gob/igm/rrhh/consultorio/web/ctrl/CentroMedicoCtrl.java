package ec.gob.igm.rrhh.consultorio.web.ctrl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

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
import jakarta.servlet.http.HttpSession;
import com.lowagie.text.DocumentException;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaActLaboral;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaExamenComp;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgoDet;
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
import ec.gob.igm.rrhh.consultorio.service.FichaRiesgoDetService;
import ec.gob.igm.rrhh.consultorio.service.FichaRiesgoService;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;
import ec.gob.igm.rrhh.consultorio.service.Step1FichaService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.audit.CentroMedicoAuditService;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfRenderer;
import ec.gob.igm.rrhh.consultorio.web.service.CedulaDialogUiCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.CedulaSearchService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoFormInitializer;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardService;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfDataMapper;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfMappedData;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfPlaceholderBuilder;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfPreparationService;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfViewModelBuilder;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfViewModelBuilder.FichaPdfViewModelContext;
import ec.gob.igm.rrhh.consultorio.web.service.Step3OrchestratorService;
import ec.gob.igm.rrhh.consultorio.web.service.Step3OrchestratorService.Step3SaveCommand;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.util.SnUtils;
import ec.gob.igm.rrhh.consultorio.web.validation.FichaCompletaValidator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step1Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step2Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step3Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.ValidationResult;

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

    private void handleUnexpected(String action, Throwable t) {
        LOG.error("Unexpected error during {}. activeStep={}, noPersonaSel={}, cedulaBusqueda={}",
                action, activeStep, noPersonaSel, cedulaBusqueda, t);
        error("Ocurrió un error inesperado al " + action + ". Revise el LOG o contacte a soporte.");
    }

    private static final int H_ROWS = 4;
    private static final int CONSUMO_ROWS = 3;
    private static final int DIAG_ROWS = 6;

    private final Step1Validator step1Validator = new Step1Validator();
    private final Step2Validator step2Validator = new Step2Validator();
    private final Step3Validator step3Validator = new Step3Validator();
    private final FichaCompletaValidator fichaCompletaValidator = new FichaCompletaValidator();

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
    private transient FichaRiesgoService fichaRiesgoService;
    @EJB
    private transient FichaDiagnosticoService fichaDiagnosticoService;
    @EJB
    private transient EmpleadoService empleadoService;
    @EJB
    private transient PersonaAuxService personaAuxService;
    @EJB
    private transient FichaActLaboralService fichaActLaboralService;
    @EJB
    private transient FichaRiesgoDetService fichaRiesgoDetService;

    @EJB
    private transient FichaExamenCompService fichaExamenCompService;
    @EJB
    private transient ExamenFisicoRegionalService examenFisicoRegionalService;

    @EJB
    private transient CentroMedicoAuditService centroMedicoAuditService;

    @Inject
    private transient PdfSessionStore pdfSessionStore;

    @Inject
    private transient PdfRenderer pdfRenderer;

    @Inject
    private transient PdfTemplateEngine pdfTemplateEngine;

    @Inject
    private transient CentroMedicoPdfFacade centroMedicoPdfFacade;

    @Inject
    private transient CentroMedicoWizardService centroMedicoWizardService;

    @Inject
    private transient CedulaSearchService cedulaSearchService;

    @Inject
    private transient CedulaDialogUiCoordinator cedulaDialogUiCoordinator;

    @Inject
    private transient CentroMedicoFormInitializer centroMedicoFormInitializer;

    @EJB
    private transient Step3OrchestratorService step3OrchestratorService;

    @EJB
    private transient FichaPdfPreparationService fichaPdfPreparationService;

    @EJB
    private transient FichaPdfDataMapper fichaPdfDataMapper;

    @EJB
    private transient FichaPdfPlaceholderBuilder fichaPdfPlaceholderBuilder;

    @EJB
    private transient FichaPdfViewModelBuilder fichaPdfViewModelBuilder;

    // JSF Lifecycle / Inicialización
    public void preRenderInit() {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc == null) {
                return;
            }
            final boolean postback = fc.isPostback();

            if (!"step1".equals(activeStep)) {
                mostrarDlgCedula = false;
            } else {

                mostrarDlgCedula = (empleadoSel == null);
            }

            if (!preRenderDone) {
                centroMedicoFormInitializer.initExamenes(this, 5);
                ensureActLabSize();
                centroMedicoFormInitializer.ensureDiagSize(this, 6);
                preRenderDone = true;
            } else {

                ensureActLabSize();
                centroMedicoFormInitializer.ensureDiagSize(this, 6);
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
        ensureActLabSize();
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

    private ConsultaDiagnostico ensureDiag(int index) {

        while (listaDiag.size() <= index) {
            listaDiag.add(new ConsultaDiagnostico());
        }

        ConsultaDiagnostico d = listaDiag.get(index);
        if (d == null) {
            d = new ConsultaDiagnostico();
            listaDiag.set(index, d);
        }
        return d;
    }

    public void onCie10BlurCodigo(int index) {
        ConsultaDiagnostico diag = ensureDiag(index);

        String codigo = diag.getCodigo();
        if (codigo == null || codigo.trim().isEmpty()) {
            diag.setDescripcion(null);
            diag.setCie10(null);
            return;
        }

        Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
        if (cie != null) {
            diag.setDescripcion(cie.getDescripcion());
            diag.setCie10(cie);
        } else {
            diag.setDescripcion(null);
            diag.setCie10(null);
        }
    }

    public void onCie10FilaSelect(int idx) {
        if (listaDiag == null) {
            return;
        }
        if (idx < 0 || idx >= listaDiag.size()) {
            return;
        }

        ConsultaDiagnostico d = listaDiag.get(idx);
        if (d == null || d.getCie10() == null) {
            return;
        }

        d.setCodigo(d.getCie10().getCodigo());
        d.setDescripcion(d.getCie10().getDescripcion());
    }

    public void onFechaNacimientoSelect(SelectEvent e) {
        this.fechaNacimiento = (java.util.Date) e.getObject();
        this.edad = calcularEdad(this.fechaNacimiento);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cálculo de edad",
                        "Edad calculada: " + (edad == null ? "(sin fecha)" : edad + " años")));
    }

    public void onFechaNacimientoChange() {
        this.edad = calcularEdad(this.fechaNacimiento);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cálculo de edad",
                        "Edad calculada: " + (edad == null ? "(sin fecha)" : edad + " años")));
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date f) {
        this.fechaNacimiento = f;
        this.edad = calcularEdad(f);
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
        this.edad = calcularEdad(this.fechaNacimiento);
    }

    /**
     * Recibe una fecha y calcula la edad actual
     *
     * @param fechaNacimiento
     * @return
     */
    private Integer calcularEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return null;
        }

        Calendar hoy = Calendar.getInstance();
        Calendar nac = Calendar.getInstance();
        nac.setTime(fechaNacimiento);

        limpiarHora(hoy);
        limpiarHora(nac);

        if (nac.after(hoy)) {
            return null;
        }

        int years = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);

        int mesHoy = hoy.get(Calendar.MONTH);
        int mesNac = nac.get(Calendar.MONTH);

        if (mesHoy < mesNac || (mesHoy == mesNac && hoy.get(Calendar.DAY_OF_MONTH) < nac.get(Calendar.DAY_OF_MONTH))) {
            years--;
        }

        return years;
    }

    /**
     * limpia la hora
     *
     * @param cal
     */
    private void limpiarHora(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Valida que sea una edad valida mayor a 18 años
     *
     * @return
     */
    public Date getFechaMaximaNacimiento() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Se usa en calculo de la edad para ejercer un trabajo
     */
    public void validarEdadMinima() {
        if (edad != null && edad < 18) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La edad debe ser ≥ 18 años"));
            fechaNacimiento = null;
            edad = null;
        }
    }

    /**
     * calculo del Indice de masa corporal
     */
    public void recalcularIMC() {
        if (peso != null && tallaCm != null && tallaCm > 0) {
            double m = tallaCm / 100.0;
            this.imc = Math.round((peso / (m * m)) * 100.0) / 100.0;
        } else {
            this.imc = null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void addMsg(FacesMessage.Severity sev, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(sev, summary, detail));
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
        FacesContext ctx = FacesContext.getCurrentInstance();

        try {
            final String next = centroMedicoWizardService.guardarStepActual(
                    activeStep,
                    this::guardarStep1,
                    this::guardarStep2,
                    this::guardarStep3,
                    this::onEnterStep4AutoRegenerar);

            if (ctx != null && !ctx.isValidationFailed() && next != null) {
                activeStep = next;

                if ("step4".equals(next)) {
                    PrimeFaces.current().ajax().update("@([id$=wdzFicha])");
                }
            }

        } catch (RuntimeException ex) {
            handleUnexpected("guardarStepActual", ex);
            if (ctx != null) {
                ctx.validationFailed();
            }
        }
    }

    private void onEnterStep4AutoRegenerar() {
        try {
            if (this.ficha == null || this.ficha.getIdFicha() == null) {
                return;
            }

            this.fichaPdfListo = false;
            this.certificadoListo = false;
            this.pdfTokenFicha = null;
            this.pdfTokenCertificado = null;

            prepararVistaPreviaFicha();
            prepararVistaPreviaCertificado();

        } catch (Exception ex) {
            LOG.warn("Auto-regeneración Step4 falló", ex);
        }
    }
    private void asegurarPersonaAuxPersistida() {

        // Si NO es ingreso manual, la ficha no debe conservar referencia a PersonaAux.
        if (!permitirIngresoManual) {
            if (ficha != null) {
                ficha.setPersonaAux(null);
            }
            personaAux = null;
            return;
        }

        // Debe existir personaAux en flujo manual.
        if (personaAux == null) {
            return;
        }

        // Si ya está persistida, solo asegurar relación.
        if (personaAux.getIdPersonaAux() != null) {
            if (ficha != null) {
                ficha.setPersonaAux(personaAux);
            }
            return;
        }

        // Persistir PersonaAux primero.
        PersonaAux saved = personaAuxService.guardar(personaAux);

        this.personaAux = saved;

        if (ficha != null) {
            ficha.setPersonaAux(saved);
        }
    }
    public void retrocederStep() {
        activeStep = centroMedicoWizardService.retrocederStep(activeStep);
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
        addValidationMessages("Step 1", result);
        return result.isValid();
    }

    private void addValidationMessages(String step, ValidationResult result) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null || result == null || result.isValid()) {
            return;
        }
        for (String error : result.getErrors()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, step, error));
        }
    }

    private void warn(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Step 1", msg));
    }

    private void info(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Step 1", msg));
    }

    private void error(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
    }

    private String construirMedidas(List<String> medidas) {
        if (medidas == null || medidas.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < medidas.size(); i++) {
            String m = medidas.get(i);
            if (!isBlank(m)) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append("M").append(i + 1).append(": ").append(m.trim());
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private RiskKey parseRiskKey(String key) {
        if (isBlank(key)) {
            return null;
        }
        String k = key.trim();
        int last = k.lastIndexOf('_');
        if (last < 0) {
            return null;
        }
        String actStr = k.substring(last + 1);
        Integer act;
        try {
            act = Integer.valueOf(actStr);
        } catch (NumberFormatException ex) {
            return null;
        }
        // prefijo de 3 letras (FIS/SEG/QUI/BIO/ERG/PSI)
        if (k.length() < 3) {
            return null;
        }
        String prefRaw = k.substring(0, 3);
        String grupo = grupoFromPrefix(prefRaw);
        String item = k.substring(0, last).replace('_', ' ');
        String prefWithSpace = prefRaw + " ";
        if (item.startsWith(prefWithSpace)) {
            item = item.substring(prefWithSpace.length());
        }
        return new RiskKey(grupo, item, act);
    }

    private RiskKey parseRiskKeyOtros(String key) {
        if (isBlank(key)) {
            return null;
        }
        String k = key.trim();
        int last = k.lastIndexOf('_');
        if (last < 0) {
            return null;
        }
        String actStr = k.substring(last + 1);
        Integer act;
        try {
            act = Integer.valueOf(actStr);
        } catch (NumberFormatException ex) {
            return null;
        }
        if (k.length() < 3) {
            return null;
        }
        String prefRaw = k.substring(0, 3);
        String grupo = grupoFromPrefix(prefRaw);
        return new RiskKey(grupo, "OTROS", act);
    }

    private String grupoFromPrefix(String pref) {
        switch (pref) {
            case "FIS":
                return "FISICO";
            case "SEG":
                return "SEGURIDAD";
            case "QUI":
                return "QUIMICO";
            case "BIO":
                return "BIOLOGICO";
            case "ERG":
                return "ERGONOMICO";
            case "PSI":
                return "PSICOSOCIAL";
            default:
                return "OTROS";
        }
    }

    /**
     * Prefijo (tal como se usa en la plantilla) a partir del grupo guardado en
     * BD. Nota: la plantilla usa minúsculas (fis/seg/qui/bio/erg/psi).
     */
    private String prefixFromGrupoLower(String grupo) {
        if (grupo == null) {
            return "otr";
        }
        switch (grupo.toUpperCase()) {
            case "FISICO":
                return "fis";
            case "SEGURIDAD":
                return "seg";
            case "QUIMICO":
                return "qui";
            case "BIOLOGICO":
                return "bio";
            case "ERGONOMICO":
                return "erg";
            case "PSICOSOCIAL":
                return "psi";
            default:
                return "otr";
        }
    }

    private static class RiskKey {

        final String grupo;
        final String item;
        final Integer actividad;

        RiskKey(String grupo, String item, Integer actividad) {
            this.grupo = grupo;
            this.item = item;
            this.actividad = actividad;
        }
    }

    private boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private java.util.List<Date> ensureSize(List<Date> list, int size) {
        if (list == null) {
            list = new ArrayList<>();
        }
        while (list.size() < size) {
            list.add(null);
        }
        return list;
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
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            saveStep1();
            info("Step 1 guardado correctamente (BORRADOR).");
        } catch (BusinessValidationException ex) {
            warn(ex.getMessage());
            if (ctx != null) {
                ctx.validationFailed();
            }
        } catch (RuntimeException ex) {
            handleUnexpected("guardarStep1", ex);
            if (ctx != null) {
                ctx.validationFailed();
            }
        }
    }

    private void saveStep1() {
        Step1FichaService.Step1Command command = new Step1FichaService.Step1Command(
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
        } catch (Step1FichaService.Step1ValidationException ex) {
            throw new BusinessValidationException(ex.getMessage());
        }
    }

    public void onBuscarPorCedulaRh() {
        try {
            buscarCedula();
        } catch (RuntimeException ex) {
            handleUnexpected("onBuscarPorCedulaRh", ex);
            cedulaDialogUiCoordinator.onRhError();
        }
    }


    private boolean validarStep2() {
        ValidationResult result = step2Validator.validate(fichaRiesgo, actividadesLab, medidasPreventivas);
        addValidationMessages("Step 2", result);
        return result.isValid();
    }

    // Wizard - Guardado Step 2
    public void guardarStep2() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            if (!validarStep2()) {
                if (ctx != null) {
                    ctx.validationFailed();
                }
                return;
            }

            saveStep2();

            if (ctx != null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO, "Step 2",
                        "Riesgos laborales guardados correctamente (encabezado + detalle)."));
            }
        } catch (BusinessValidationException ex) {
            warn(ex.getMessage());
            if (ctx != null) {
                ctx.validationFailed();
            }
        } catch (RuntimeException ex) {
            handleUnexpected("guardarStep2", ex);
            if (ctx != null) {
                ctx.validationFailed();
            }
        }
    }

    private void saveStep2() {
        final Date now = new Date();
        final String user = usuarioReal();

        ensureFichaSavedOrThrow();
        upsertRiskHeader(now, user);
        replaceRiskDetails(user);

        registrarAuditoria("GUARDAR_STEP2", "FICHA_RIESGO / FICHA_RIESGO_DET", "*",
                "Step 2 guardado. ID_FICHA=" + ficha.getIdFicha());
    }

    private void ensureFichaSavedOrThrow() {
        if (ficha == null || ficha.getIdFicha() == null) {
            throw new BusinessValidationException("Primero debe existir y estar guardada la ficha (ID_FICHA).");
        }
    }

    private void upsertRiskHeader(Date now, String user) {
        if (fichaRiesgo == null) {
            fichaRiesgo = new FichaRiesgo();
        }
        fichaRiesgo.setFicha(ficha);

        mapRiskActivitiesToHeader();
        fichaRiesgo.setMedidasPreventivas(construirMedidas(medidasPreventivas));

        stampAuditFieldsForRiskHeader(fichaRiesgo, now, user);

        fichaRiesgo = fichaRiesgoService.guardar(fichaRiesgo);
    }

    private void mapRiskActivitiesToHeader() {
        fichaRiesgo.setActividad1(getSafe(actividadesLab, 0));
        fichaRiesgo.setActividad2(getSafe(actividadesLab, 1));
        fichaRiesgo.setActividad3(getSafe(actividadesLab, 2));
        fichaRiesgo.setActividad4(getSafe(actividadesLab, 3));
        fichaRiesgo.setActividad5(getSafe(actividadesLab, 4));
        fichaRiesgo.setActividad6(getSafe(actividadesLab, 5));
        fichaRiesgo.setActividad7(getSafe(actividadesLab, 6));
    }

    private void stampAuditFieldsForRiskHeader(FichaRiesgo fr, Date now, String user) {
        if (fr.getIdFichaRiesgo() == null) {
            fr.setEstado("BORRADOR");
            fr.setFCreacion(now);
            fr.setUsrCreacion(user);
        } else {
            fr.setFActualizacion(now);
            fr.setUsrActualizacion(user);
        }
    }

    private void replaceRiskDetails(String user) {

        fichaRiesgoDetService.eliminarPorFicha(ficha.getIdFicha());

        persistCheckedRiskItems(user);
        persistOtherRiskItems(user);
    }

    private void persistCheckedRiskItems(String user) {
        if (riesgos == null || riesgos.isEmpty()) {
            return;
        }
        int orden = 1;

        for (Map.Entry<String, Boolean> e : riesgos.entrySet()) {
            if (!Boolean.TRUE.equals(e.getValue())) {
                continue;
            }

            RiskKey rk = parseRiskKey(e.getKey());
            if (rk == null) {
                continue;
            }

            FichaRiesgoDet det = new FichaRiesgoDet();
            det.setFicha(ficha);
            det.setGrupo(rk.grupo);
            det.setItem(rk.item);
            det.setActividadNro(rk.actividad);
            det.setMarcado("S");
            det.setOrden(orden++);

            fichaRiesgoDetService.guardar(det, user);
        }
    }

    private void persistOtherRiskItems(String user) {
        if (otrosRiesgos == null || otrosRiesgos.isEmpty()) {
            return;
        }
        int ordenOtros = 10000;

        for (Map.Entry<String, String> e : otrosRiesgos.entrySet()) {
            String val = e.getValue();
            if (isBlank(val)) {
                continue;
            }

            RiskKey rk = parseRiskKeyOtros(e.getKey());
            if (rk == null) {
                continue;
            }

            FichaRiesgoDet det = new FichaRiesgoDet();
            det.setFicha(ficha);
            det.setGrupo(rk.grupo);
            det.setItem("OTROS: " + val.trim());
            det.setActividadNro(rk.actividad);
            det.setMarcado("S");
            det.setOrden(ordenOtros++);

            fichaRiesgoDetService.guardar(det, user);
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
        addValidationMessages("Step 3", result);
        s3("validarStep3() FIN -> " + result.isValid());
        return result.isValid();
    }

    // Wizard - Guardado Step 3
    public void guardarStep3() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            saveStep3();

            if (ctx != null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO, "OK", "Step 3 guardado correctamente."));
            }
        } catch (BusinessValidationException ex) {
            warn(ex.getMessage());
            if (ctx != null) {
                ctx.validationFailed();
            }
        } catch (RuntimeException ex) {
            handleUnexpected("guardarStep3", ex);
            if (ctx != null) {
                ctx.validationFailed();
            }
        }
    }

    private void saveStep3() {
        ensureFichaSavedOrThrow();

        final Date now = new Date();
        final String user = usuarioReal();

        try {
            ficha = step3OrchestratorService.saveStep3(new Step3SaveCommand(
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
                    this::ensureActLabSize,
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

    private <T> T getSafe(List<T> list, int idx) {
        if (list == null) {
            s3("getSafe() list=null idx=" + idx);
            return null;
        }
        if (idx < 0 || idx >= list.size()) {
            s3("getSafe() idx fuera de rango idx=" + idx + " size=" + list.size());
            return null;
        }
        return list.get(idx);
    }

    private boolean isTrue(Boolean b) {
        return b != null && b;
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
        FacesContext ctx = FacesContext.getCurrentInstance();

        try {
            FichaPdfPreparationService.FichaPdfPrepareResult result = fichaPdfPreparationService.preparar(
                    ficha,
                    empleadoSel,
                    personaAux,
                    permitirIngresoManual,
                    this::asegurarPersonaAuxPersistida,
                    this::construirHtmlFichaDesdePlantilla,
                    centroMedicoPdfFacade);

            if (!result.valid) {
                StringBuilder sb = new StringBuilder();
                for (String error : result.errores) {
                    sb.append("- ").append(error).append("\n");
                }
                ctx.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Validación antes de generar la ficha",
                                sb.toString()));
                fichaPdfListo = false;
                return;
            }

            ficha = result.ficha;
            this.pdfTokenFicha = result.token;
            this.fichaPdfListo = result.listo;

            if (!fichaPdfListo) {
                return;
            }

            this.activeStep = "step4";
            this.mostrarDlgCedula = false;

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "PDF Ficha listo",
                    "Se generó la ficha para vista previa y descarga."
            ));

            PrimeFaces.current().ajax().addCallbackParam("fichaListo", fichaPdfListo);
            PrimeFaces.current().ajax().update(":msgs", "@([id$=wdzFicha])");

        } catch (Exception ex) {
            this.fichaPdfListo = false;
            this.pdfTokenFicha = null;

            LOG.error("Error generando PDF FICHA", ex);

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo generar el PDF de la ficha"
            ));
        }
    }

    private void debugObjetosAntesDeImprimir() {
        LOG.info("[PRINT] ficha=" + (ficha != null));
        if (ficha != null) {
            LOG.info("[PRINT] ficha.instSistema=" + ficha.getInstSistema());
            LOG.info("[PRINT] ficha.ruc=" + ficha.getRucEstablecimiento());
            LOG.info("[PRINT] ficha.centroTrabajo=" + ficha.getEstablecimientoCt());
            LOG.info("[PRINT] ficha.ciiu=" + ficha.getCiiu());
            LOG.info("[PRINT] ficha.noHistoria=" + ficha.getNoHistoriaClinica());
            LOG.info("[PRINT] ficha.noArchivo=" + ficha.getNoArchivo());
        }

        LOG.info("[PRINT] empleadoSel=" + (empleadoSel != null));
        if (empleadoSel != null) {
            LOG.info("[PRINT] empleadoSel.priApellido=" + empleadoSel.getPriApellido());
            LOG.info("[PRINT] empleadoSel.segApellido=" + empleadoSel.getSegApellido());
            LOG.info("[PRINT] empleadoSel.nombres=" + empleadoSel.getNombres());
            // si tienes:
            // LOG.info("[PRINT] empleadoSel.sexo=" + empleadoSel.getSexo());
            // LOG.info("[PRINT] empleadoSel.fechaNac=" + empleadoSel.getFechaNacimiento());
        }
    }

    private String construirHtmlFichaDesdeFacelet() {
        String html = centroMedicoPdfFacade.renderFaceletToHtml("/pages/ficha/fichaPrint.xhtml");
        return normalizarXhtmlPdf(html);
    }

    private String leerRecursoComoString(String classpathLocation) {

        InputStream is = null;

        try {

            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    classpathLocation.startsWith("/") ? classpathLocation.substring(1) : classpathLocation
            );

            if (is == null) {
                FacesContext fc = FacesContext.getCurrentInstance();
                if (fc != null) {
                    ExternalContext ec = fc.getExternalContext();
                    is = ec.getResourceAsStream(classpathLocation.startsWith("/") ? classpathLocation : ("/" + classpathLocation));
                }
            }

            if (is == null) {
                throw new IllegalStateException("No se encontró el template en classpath: " + classpathLocation
                        + " (Revisa src/main/resources y la ruta).");
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo recurso: " + classpathLocation, e);
        }
    }

    private String safePdf(String s) {
        return s == null ? "" : escHtml(s.trim());
    }

    private String safeDate(Date d) {
        if (d == null) {
            return "";
        }

        return new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "EC")).format(d);
    }

    private String safeNum(Object n) {
        return n == null ? "" : String.valueOf(n);
    }

    private String escHtml(String s) {

        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String normalizarXhtmlPdf(String html) {
        if (html == null) {
            return "";
        }

        return html.replace("\u00A0", " ");
    }

    private String renderFaceletToHtml(String viewId) {
        LOG.info("FichaPrint: renderFaceletToHtml delegado a facade. viewId={}", viewId);
        return centroMedicoPdfFacade.renderFaceletToHtml(viewId);
    }

    private String construirHtmlFichaDesdePlantilla() {
        try {
            return fichaPdfPlaceholderBuilder.construirHtmlFichaDesdePlantilla(
                    () -> {
                        try {
                            return cargarRecursoComoString("plantilla_ficha.html");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    this::syncCamposDesdeObjetos,
                    this::buildReemplazosFicha,
                    this::obtenerTipoEvaluacionPdf,
                    centroMedicoPdfFacade);
        } catch (RuntimeException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            LOG.error("[FICHA] Error cargando plantilla_ficha.html", cause);
            return "<html><body><h3>Error cargando plantilla_ficha.html</h3><pre>"
                    + safe(cause.getMessage()) + "</pre></body></html>";
        } catch (Exception e) {
            LOG.error("[FICHA] Error cargando plantilla_ficha.html", e);
            return "<html><body><h3>Error cargando plantilla_ficha.html</h3><pre>"
                    + safe(e.getMessage()) + "</pre></body></html>";
        }
    }

    private String obtenerTipoEvaluacionPdf() {
        String tipo = trimToNull(tipoEval);
        if (tipo == null) {
            tipo = trimToNull(tipoEvaluacion);
        }
        return tipo;
    }

    private String s(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof java.util.Date) {
            return new java.text.SimpleDateFormat("dd/MM/yyyy").format((java.util.Date) v);
        }
        return String.valueOf(v);
    }

    private void syncCamposDesdeObjetos() {
        FichaPdfMappedData data = fichaPdfDataMapper.map(ficha, empleadoSel, fechaNacimiento);
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

    private void LOGDebugStep1() {
        LOG.info("[STEP1] institucion=" + institucion);
        LOG.info("[STEP1] ruc=" + ruc);
        LOG.info("[STEP1] centroTrabajo=" + centroTrabajo);
        LOG.info("[STEP1] ciiu=" + ciiu);
        LOG.info("[STEP1] noHistoria=" + noHistoria);
        LOG.info("[STEP1] noArchivo=" + noArchivo);

        LOG.info("[STEP1] apellido1=" + apellido1);
        LOG.info("[STEP1] apellido2=" + apellido2);
        LOG.info("[STEP1] nombre1=" + nombre1);
        LOG.info("[STEP1] nombre2=" + nombre2);

        LOG.info("[STEP1] sexo=" + sexo);
        LOG.info("[STEP1] fechaNacimiento=" + safeDate(fechaNacimiento));
        LOG.info("[STEP1] edad=" + safeNum(edad));
        LOG.info("[STEP1] grupoSanguineo=" + grupoSanguineo);
        LOG.info("[STEP1] lateralidad=" + lateralidad);
    }

    private Map<String, String> buildReemplazosFicha() {
        Map<String, String> rep = new LinkedHashMap<>();

        cargarLogos(rep);
        cargarAtencionPrioritaria(rep);
        cargarFechaActual(rep);
        cargarActividadLaboralArrays(rep);
        cargarAntecedentes(rep);
        cargarRiesgos(rep);
        recalcularIMC();

        rep.putAll(fichaPdfViewModelBuilder.buildReemplazosFicha(buildFichaPdfViewModelContext()));

        LOG.info("=== buildReemplazosFicha - valores en el mapa ===");
        LOG.info("gineco_examen1 = " + rep.get("gineco_examen1"));
        LOG.info("gineco_tiempo1 = " + rep.get("gineco_tiempo1"));
        LOG.info("gineco_resultado1 = " + rep.get("gineco_resultado1"));
        LOG.info("gineco_examen2 = " + rep.get("gineco_examen2"));
        LOG.info("gineco_tiempo2 = " + rep.get("gineco_tiempo2"));
        LOG.info("gineco_resultado2 = " + rep.get("gineco_resultado2"));
        LOG.info("gineco_observacion = " + rep.get("gineco_observacion"));
        LOG.info("=================================================");

        cargarActividadesLaboralesList(rep);
        cargarMedidasPreventivasList(rep);
        cargarHActividadLaboral(rep);
        cargarIActividadesExtra(rep);
        cargarJExamenes(rep);
        cargarKDiagnosticos(rep);
        cargarLAptitud(rep);
        cargarNRetiro(rep);
        cargarOProfesional(rep);
        cargarAntecedentesCamelCase(rep);
        corregirOtrosRiesgos(rep);

        return rep;
    }

    private FichaPdfViewModelContext buildFichaPdfViewModelContext() {
        FichaPdfViewModelContext ctx = new FichaPdfViewModelContext();
        ctx.ficha = ficha;
        ctx.fichaRiesgo = fichaRiesgo;
        ctx.empleadoSel = empleadoSel;
        ctx.personaAux = personaAux;
        ctx.cedulaBusqueda = cedulaBusqueda;
        ctx.institucion = institucion;
        ctx.ruc = ruc;
        ctx.centroTrabajo = centroTrabajo;
        ctx.ciiu = ciiu;
        ctx.noHistoria = noHistoria;
        ctx.noArchivo = noArchivo;
        ctx.apellido1 = apellido1;
        ctx.apellido2 = apellido2;
        ctx.nombre1 = nombre1;
        ctx.nombre2 = nombre2;
        ctx.sexo = sexo;
        ctx.fechaNacimiento = fechaNacimiento;
        ctx.edad = edad;
        ctx.grupoSanguineo = grupoSanguineo;
        ctx.lateralidad = lateralidad;
        ctx.consTiempoConsumoMeses = consTiempoConsumoMeses;
        ctx.consExConsumidor = consExConsumidor;
        ctx.consTiempoAbstinenciaMeses = consTiempoAbstinenciaMeses;
        ctx.consNoConsume = consNoConsume;
        ctx.consOtrasCual = consOtrasCual;
        ctx.afCual = afCual;
        ctx.afTiempo = afTiempo;
        ctx.medCual = medCual;
        ctx.medCant = medCant;
        ctx.consumoVidaCondObs = consumoVidaCondObs;
        ctx.recomendaciones = recomendaciones;
        ctx.fechaAtencion = fechaAtencion;
        ctx.fecIngreso = fecIngreso;
        ctx.fecReintegro = fecReintegro;
        ctx.fecRetiro = fecRetiro;
        ctx.motivoObs = motivoObs;
        ctx.condicionEspecial = condicionEspecial;
        ctx.autorizaTransfusion = autorizaTransfusion;
        ctx.tratamientoHormonal = tratamientoHormonal;
        ctx.tratamientoHormonalCual = tratamientoHormonalCual;
        ctx.examenReproMasculino = examenReproMasculino;
        ctx.tiempoReproMasculino = tiempoReproMasculino;
        ctx.fum = fum;
        ctx.gestas = gestas;
        ctx.cesareas = cesareas;
        ctx.partos = partos;
        ctx.abortos = abortos;
        ctx.planificacion = planificacion;
        ctx.planificacionCual = planificacionCual;
        ctx.temp = temp;
        ctx.paStr = paStr;
        ctx.fc = fc;
        ctx.fr = fr;
        ctx.satO2 = satO2;
        ctx.peso = peso;
        ctx.tallaCm = tallaCm;
        ctx.imc = imc;
        ctx.perimetroAbd = perimetroAbd;
        ctx.ginecoExamen1 = ginecoExamen1;
        ctx.ginecoTiempo1 = ginecoTiempo1;
        ctx.ginecoResultado1 = ginecoResultado1;
        ctx.ginecoExamen2 = ginecoExamen2;
        ctx.ginecoTiempo2 = ginecoTiempo2;
        ctx.ginecoResultado2 = ginecoResultado2;
        ctx.ginecoObservacion = ginecoObservacion;
        ctx.tipoEval = tipoEval;
        ctx.enfermedadActual = enfermedadActual;
        ctx.otrosRiesgos = otrosRiesgos;
        ctx.exfPielCicatrices = exfPielCicatrices;
        ctx.exfOjosParpados = exfOjosParpados;
        ctx.exfOjosConjuntivas = exfOjosConjuntivas;
        ctx.exfOjosPupilas = exfOjosPupilas;
        ctx.exfOjosCornea = exfOjosCornea;
        ctx.exfOjosMotilidad = exfOjosMotilidad;
        ctx.exfOidoConducto = exfOidoConducto;
        ctx.exfOidoPabellon = exfOidoPabellon;
        ctx.exfOidoTimpanos = exfOidoTimpanos;
        ctx.exfOroLabios = exfOroLabios;
        ctx.exfOroLengua = exfOroLengua;
        ctx.exfOroFaringe = exfOroFaringe;
        ctx.exfOroAmigdalas = exfOroAmigdalas;
        ctx.exfOroDentadura = exfOroDentadura;
        ctx.exfNarizTabique = exfNarizTabique;
        ctx.exfNarizCornetes = exfNarizCornetes;
        ctx.exfNarizMucosas = exfNarizMucosas;
        ctx.exfNarizSenos = exfNarizSenos;
        ctx.exfCuelloTiroides = exfCuelloTiroides;
        ctx.exfCuelloMovilidad = exfCuelloMovilidad;
        ctx.exfToraxMamas = exfToraxMamas;
        ctx.exfToraxPulmones = exfToraxPulmones;
        ctx.exfToraxCorazon = exfToraxCorazon;
        ctx.exfToraxParrilla = exfToraxParrilla;
        ctx.exfAbdomenVisceras = exfAbdomenVisceras;
        ctx.exfAbdomenPared = exfAbdomenPared;
        ctx.exfColumnaFlexibilidad = exfColumnaFlexibilidad;
        ctx.exfColumnaDesviacion = exfColumnaDesviacion;
        ctx.exfColumnaDolor = exfColumnaDolor;
        ctx.exfPelvisPelvis = exfPelvisPelvis;
        ctx.exfPelvisGenitales = exfPelvisGenitales;
        ctx.exfExtVascular = exfExtVascular;
        ctx.exfExtSup = exfExtSup;
        ctx.exfExtInf = exfExtInf;
        ctx.exfNeuroFuerza = exfNeuroFuerza;
        ctx.exfNeuroSensibilidad = exfNeuroSensibilidad;
        ctx.exfNeuroMarcha = exfNeuroMarcha;
        ctx.exfNeuroReflejos = exfNeuroReflejos;
        ctx.obsExamenFisico = obsExamenFisico;
        return ctx;
    }

    private void cargarLogos(Map<String, String> rep) {
        try {
            rep.put("LOGO_IGM_DATAURI", dataUriFromResource("images/LOGO_IGM_FULL_COLOR.png"));
        } catch (IOException ex) {
            LOG.warn("[FICHA] No se pudo cargar LOGo IGM", ex);
            rep.put("LOGO_IGM_DATAURI", "");
        }
        try {
            rep.put("LOGO_MIDENA_DATAURI", dataUriFromResource("images/LOGO_MIDENA.png"));
        } catch (IOException ex) {
            LOG.warn("[FICHA] No se pudo cargar LOGo MIDENA", ex);
            rep.put("LOGO_MIDENA_DATAURI", "");
        }
    }

    private void cargarFechaActual(Map<String, String> rep) {
        LocalDate hoy = LocalDate.now();
        rep.put("fecha_yyyy", String.valueOf(hoy.getYear()));
        rep.put("fecha_mm", String.format("%02d", hoy.getMonthValue()));
        rep.put("fecha_dd", String.format("%02d", hoy.getDayOfMonth()));
    }

    private void cargarAntecedentes(Map<String, String> rep) {
        rep.put("ant_clinico_quirurgico", safe(antClinicoQuirurgico));
        rep.put("ant_familiares", safe(antFamiliares));
        rep.put("ant_terapeutica", safe(antTerapeutica));
        rep.put("ant_obs", safe(antObs));
    }

    private void cargarRiesgos(Map<String, String> rep) {
        if (riesgos != null) {
            for (Map.Entry<String, Boolean> e : riesgos.entrySet()) {
                String k = e.getKey();
                if (k == null) {
                    continue;
                }
                String ph = k.toLowerCase();
                rep.put(ph, Boolean.TRUE.equals(e.getValue()) ? "X" : "");
            }
        }
        // Nota: otrosRiesgos se corrige más adelante en corregirOtrosRiesgos()
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void cargarActividadesLaboralesList(Map<String, String> rep) {
        if (actividadesLab != null) {
            for (int i = 0; i < actividadesLab.size() && i < 7; i++) {
                rep.put("actividad_" + (i + 1), safe(actividadesLab.get(i)));
            }
        }
    }

    private void cargarMedidasPreventivasList(Map<String, String> rep) {
        if (medidasPreventivas != null) {
            for (int i = 0; i < medidasPreventivas.size() && i < 7; i++) {
                rep.put("medida_" + (i + 1), safe(medidasPreventivas.get(i)));
            }
        }
    }

    private void cargarHActividadLaboral(Map<String, String> rep) {
        int hRows = H_ROWS;
        for (int i = 0; i < hRows; i++) {
            rep.put("act_lab_centro_" + i, safe(getSafe(actLabCentroTrabajo, i)));
            rep.put("act_lab_actividad_" + i, safe(getSafe(actLabActividad, i)));
            rep.put("act_lab_tiempo_" + i, safe(getSafe(actLabTiempo, i)));
            rep.put("act_lab_anterior_" + i, isTrue(getSafe(actLabTrabajoAnterior, i)) ? "X" : "");
            rep.put("act_lab_actual_" + i, isTrue(getSafe(actLabTrabajoActual, i)) ? "X" : "");
            rep.put("act_lab_incidente_" + i, isTrue(getSafe(actLabIncidenteChk, i)) ? "X" : "");
            rep.put("act_lab_accidente_" + i, isTrue(getSafe(actLabAccidenteChk, i)) ? "X" : "");
            rep.put("act_lab_enfermedad_" + i, isTrue(getSafe(actLabEnfermedadChk, i)) ? "X" : "");
            rep.put("iess_si_" + i, isTrue(getSafe(iessSi, i)) ? "X" : "");
            rep.put("iess_no_" + i, isTrue(getSafe(iessNo, i)) ? "X" : "");
            rep.put("iess_fecha_" + i, fmtDate(toDate(getSafe(iessFecha, i))));
            rep.put("iess_especificar_" + i, safe(getSafe(iessEspecificar, i)));
            rep.put("act_lab_obs_" + i, safe(getSafe(actLabObservaciones, i)));
        }
    }

    private void cargarIActividadesExtra(Map<String, String> rep) {
        for (int i = 0; i < 3; i++) {
            rep.put("tipo_act_" + i, safe(getSafe(tipoAct, i)));
            rep.put("fecha_act_" + i, fmtDate(toDate(getSafe(fechaAct, i))));
            rep.put("desc_act_" + i, safe(getSafe(descAct, i)));
        }
    }

    private void cargarJExamenes(Map<String, String> rep) {
        for (int i = 0; i < 5; i++) {
            rep.put("exam_nombre_" + i, safe(getSafe(examNombre, i)));
            rep.put("exam_fecha_" + i, fmtDate(toDate(getSafe(examFecha, i))));
            rep.put("exam_resultado_" + i, safe(getSafe(examResultado, i)));
        }
        rep.put("obs_j", safe(obsJ));
    }

    private void cargarKDiagnosticos(Map<String, String> rep) {
        if (listaDiag != null) {
            for (int i = 0; i < listaDiag.size() && i < 6; i++) {
                ConsultaDiagnostico d = listaDiag.get(i);
                if (d != null) {
                    rep.put("k_codigo_" + i, safe(d.getCodigo()));
                    rep.put("k_desc_" + i, safe(d.getDescripcion()));
                    rep.put("k_pre_" + i, "P".equals(d.getTipoDiag()) ? "X" : "");
                    rep.put("k_def_" + i, "D".equals(d.getTipoDiag()) ? "X" : "");
                } else {
                    rep.put("k_codigo_" + i, "");
                    rep.put("k_desc_" + i, "");
                    rep.put("k_pre_" + i, "");
                    rep.put("k_def_" + i, "");
                }
            }
        }
    }

    private void cargarLAptitud(Map<String, String> rep) {
        String aptoX = "", aptoObsX = "", aptoLimitX = "", noAptoX = "";
        if (aptitudSel != null) {
            switch (aptitudSel) {
                case "APTO":
                    aptoX = "X";
                    break;
                case "APTO_EN_OBS":
                    aptoObsX = "X";
                    break;
                case "APTO_LIMIT":
                    aptoLimitX = "X";
                    break;
                case "NO_APTO":
                    noAptoX = "X";
                    break;
            }
        }
        rep.put("l_apto", aptoX);
        rep.put("l_apto_obs", aptoObsX);
        rep.put("l_apto_limit", aptoLimitX);
        rep.put("l_no_apto", noAptoX);
        String obs = detalleObservaciones;
        if (obs == null || obs.trim().isEmpty()) {
            // fallback: si observaciones ya están en la entidad Ficha, las tomamos de ahí
            obs = getFichaStringByReflection(ficha,
                    "getDetalleObs",
                    "getDetalleObservaciones",
                    "getObservaciones",
                    "getObs",
                    "getObservacion"
            );
        }
        rep.put("l_observaciones", safe(obs));
    }

    private void cargarNRetiro(Map<String, String> rep) {
        // Acepta valores S/N, SI/NO, true/false, 1/0
        rep.put("n_realiza_eval_si", isYes(nRealizaEvaluacion) ? "X" : "");
        rep.put("n_realiza_eval_no", isNo(nRealizaEvaluacion) ? "X" : "");

        rep.put("n_relacion_trabajo_si", isYes(nRelacionTrabajo) ? "X" : "");
        rep.put("n_relacion_trabajo_no", isNo(nRelacionTrabajo) ? "X" : "");

        rep.put("n_obs_retiro", safe(nObsRetiro));
    }

    private void cargarOProfesional(Map<String, String> rep) {
        rep.put("medico_nombre", safe(medicoNombre));
        rep.put("medico_codigo", safe(medicoCodigo));
    }

    private void cargarAntecedentesCamelCase(Map<String, String> rep) {
        rep.put("antClinicoQuirurgico", safe(antClinicoQuirurgico));
        rep.put("antFamiliares", safe(antFamiliares));
        rep.put("antTerapeutica", safe(antTerapeutica));
        rep.put("antObs", safe(antObs));
    }

    private void corregirOtrosRiesgos(Map<String, String> rep) {
        if (otrosRiesgos != null) {
            for (Map.Entry<String, String> e : otrosRiesgos.entrySet()) {
                String k = e.getKey();
                if (k == null) {
                    continue;
                }
                String ph = k.toLowerCase();
                rep.put(ph, safe(e.getValue()));
            }
        }
    }

    /**
     * actividad_1..N desde String[]
     */
    private void putArray1Based(Map<String, String> rep, String prefix, String[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + (i + 1), safe(arr[i]));
        }
    }

    /**
     * ta_1..N desde Integer[]
     */
    private void putIntArray1Based(Map<String, String> rep, String prefix, Integer[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + (i + 1), (arr[i] == null) ? "" : String.valueOf(arr[i]));
        }
    }

    /**
     * ii_1..N desde Boolean[] -> "X"
     */
    private void putBoolArray1Based(Map<String, String> rep, String prefix, Boolean[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + (i + 1), Boolean.TRUE.equals(arr[i]) ? "X" : "");
        }
    }

    /**
     * Interpreta marcas tipo "true", "1", "X", "SI"
     */
    private boolean isTruthyMark(String v) {
        if (v == null) {
            return false;
        }
        String s = v.trim();
        if (s.isEmpty()) {
            return false;
        }
        s = s.toUpperCase();
        return "TRUE".equals(s) || "1".equals(s) || "X".equals(s) || "SI".equals(s) || "S".equals(s);
    }

    private Map<String, String> buildVarsFicha() {
        Map<String, String> m = new HashMap<>();

        m.put("institucion", safe(institucion));
        m.put("ruc", safe(ruc));
        m.put("centroTrabajo", safe(centroTrabajo));
        // m.put("num_formulario", safe(numFormulario));

        m.put("apellido1", safe(apellido1));
        m.put("apellido2", safe(apellido2));
        m.put("nombre1", safe(nombre1));
        m.put("nombre2", safe(nombre2));
        m.put("sexo", safe(sexo));
        m.put("ciiu", safe(ciiu));

        m.put("no_historia", safe(noHistoria));
        //m.put("puesto_trabajo", safe(puestoTrabajo));

        /*m.put("fecha_yyyy", safe(fecha_yyyy));
    m.put("fecha_MM", safe(fecha_MM));
    m.put("fecha_dd", safe(fecha_dd));*/
        m.put("LOGO_IGM_DATAURI", buildLogoDataUri("LOGO_IGM_FULL_COLOR.png"));
        m.put("LOGO_MIDENA_DATAURI", buildLogoDataUri("LOGomidena.png"));

        return m;
    }

    private String aplicarPlaceholders(String html, Map<String, String> vars) {
        String out = html;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            out = out.replace("{{" + e.getKey() + "}}", safe(e.getValue()));
        }
        return out;
    }

    private String buildLogoDataUri(String fileName) {
        try (InputStream in = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResourceAsStream("/resources/images/" + fileName)) {

            if (in == null) {
                return "";
            }

            byte[] bytes = in.readAllBytes();
            String b64 = java.util.Base64.getEncoder().encodeToString(bytes);

            String lower = fileName.toLowerCase();
            String mime = lower.endsWith(".png") ? "image/png"
                    : (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) ? "image/jpeg"
                    : "application/octet-stream";

            return "data:" + mime + ";base64," + b64;

        } catch (Exception e) {
            return "";
        }
    }

    private String renderTemplate(String template, Map<String, String> rep) {
        return pdfTemplateEngine.render(template, rep);
    }

    private String construirHtmlFichaDesdePrintFacelets() {

        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.getSessionMap().put("centroMedicoPrint", this);
        } catch (Exception e) {
            LOG.warn("No se pudo colocar centroMedicoPrint en sesión.", e);
        }

        String html = centroMedicoPdfFacade.renderFaceletToHtml("/pages/ficha/fichaPrint.xhtml");
        if (html != null) {
            html = html.replace("\u00A0", " ");
        }
        return html;
    }

    public void prepararVistaPrevia() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (!fichaPdfListo) {
            prepararVistaPreviaFicha();
            if (!fichaPdfListo) {
                certificadoListo = false;
                return;
            }
        }
        try {
            if (!verificarFichaCompleta()) {
                certificadoListo = false;
                return;
            }
            generatePdfPreview();
            certificadoListo = true;
            if (ctx != null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO, "PDF listo",
                        "Se generó el certificado para vista previa y descarga."));
            }
        } catch (RuntimeException ex) {
            certificadoListo = false;
            cleanupPdfPreview(ctx);
            if (ctx != null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Error", "No se pudo generar el PDF"));
            }
            LOG.error("Unexpected error while preparing PDF preview.", ex);
        }
    }

    private void generatePdfPreview() {
        try {
            String token = centroMedicoPdfFacade.generarDesdeHtml(construirHtmlDesdePlantilla(), "CERT_");

            this.pdfTokenCertificado = token;
            this.pdfObjectUrl = null;
        } catch (Exception e) {
            handleUnexpected("generatePdfPreview", e);
        }
    }

    private void cleanupPdfPreview(FacesContext ctx) {
        if (ctx == null) {
            pdfTokenCertificado = null;
            pdfObjectUrl = null;
            return;
        }
        pdfSessionStore.remove(ctx, pdfTokenCertificado);
        pdfTokenCertificado = null;
        pdfObjectUrl = null;
    }

    public void limpiarVistaPrevia() {
        if (pdfTokenCertificado != null) {
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap()
                    .remove(pdfTokenCertificado);
        }
        certificadoListo = false;
        pdfTokenCertificado = null;
        pdfObjectUrl = null;
    }

    // PDF - Certificado Médico
    public void prepararVistaPreviaCertificado() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        try {

            // 1) Asegurar que la FICHA esté lista (porque el certificado depende de la ficha)
            if (!fichaPdfListo || pdfTokenFicha == null) {
                prepararVistaPreviaFicha();
                if (!fichaPdfListo || pdfTokenFicha == null) {
                    certificadoListo = false;
                    return;
                }
            }

            // 2) ✅ Setear FECHA DE EMISIÓN del certificado (solo si está null)
            //    Esta fecha pertenece al certificado, por eso se setea aquí,
            //    cuando el usuario pide "vista previa / descarga del certificado".
            if (ficha != null && ficha.getFechaEmision() == null) {
                ficha.setFechaEmision(new java.util.Date());
            }
            if (ficha != null) {
                this.fechaEmision = ficha.getFechaEmision();
            }

            // 3) Validar que todo esté completo (incluye fecha de emisión)
            if (!verificarFichaCompleta()) {
                certificadoListo = false;
                return;
            }

            // 4) Generar el PDF del certificado
            generatePdfPreview();

            certificadoListo = (pdfTokenCertificado != null);

            this.activeStep = "step4";
            this.mostrarDlgCedula = false;

            if (certificadoListo && ctx != null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "PDF Certificado listo",
                        "Se generó el certificado para vista previa y descarga."
                ));
            }

        } catch (RuntimeException ex) {
            certificadoListo = false;
            cleanupPdfPreview(ctx);
            if (ctx != null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Error",
                        "No se pudo generar el PDF del certificado"
                ));
            }
            LOG.error("Error generando PDF CERTIFICADO", ex);
        }
    }

    private static final Pattern AMP_BAD = Pattern.compile(
            "&(?!amp;|lt;|gt;|quot;|apos;|#\\d+;|#x[0-9A-Fa-f]+;)"
    );

    private String sanitizeXhtmlForPdf(String html) {
        if (html == null) {
            return null;
        }

        html = html.replace("&nbsp;", "&#160;");

        html = AMP_BAD.matcher(html).replaceAll("&amp;");
        return html;
    }

    private void dumpXhtmlDebug(String fileName, String xhtml, Exception ex) {
        try {
            Path p = Path.of(System.getProperty("java.io.tmpdir"), fileName);
            Files.writeString(p, xhtml, StandardCharsets.UTF_8);
            LOG.error("PDF DEBUG: Se guardó XHTML en {}", p.toAbsolutePath(), ex);
        } catch (Exception ignore) {
            LOG.error("PDF DEBUG: No se pudo guardar XHTML de diagnóstico.", ignore);
        }
    }

    private byte[] renderizarPdf(String xhtml) throws IOException, DocumentException {

        if (xhtml == null || xhtml.trim().isEmpty()) {
            LOG.error("renderizarPdf: El string HTML recibido es NULO o VACÍO.");
            throw new IllegalArgumentException(
                    "El contenido HTML para generar el PDF está vacío. "
                    + "Esto generalmente significa que el método renderFaceletToHtml falló. "
                    + "Revise el LOG del servidor buscando 'FichaPrint: renderFaceletToHtml ERROR' para ver el error real."
            );
        }

        final String xhtmlOk = sanitizeXhtmlForPdf(xhtml);

        try {
            return pdfRenderer.render(xhtmlOk);
        } catch (RuntimeException ex) {
            dumpXhtmlDebug("fichaPrint_debug.xhtml", xhtmlOk, ex);
            throw ex;
        }
    }

    private String construirHtmlDesdePlantilla() throws IOException {

        String template = cargarRecursoComoString("plantilla_certificado.html");
        template = normalizarXhtml(template);

        Date f = (ficha != null && ficha.getFechaEmision() != null)
                ? ficha.getFechaEmision()
                : ((fechaEmision != null) ? fechaEmision : new Date());
        SimpleDateFormat yy = new SimpleDateFormat("yyyy");
        SimpleDateFormat MM = new SimpleDateFormat("MM");
        SimpleDateFormat dd = new SimpleDateFormat("dd");

        String aApto = "&nbsp;", aObs = "&nbsp;", aLim = "&nbsp;", aNo = "&nbsp;";
        if (aptitudSel != null) {
            switch (aptitudSel) {
                case "APTO":
                    aApto = "X";
                    break;
                case "APTO_EN_OBS":
                    aObs = "X";
                    break;
                case "APTO_LIMIT":
                    aLim = "X";
                    break;
                case "NO_APTO":
                    aNo = "X";
                    break;
            }
        }

        if (tipoEval != null && (tipoEvaluacion == null || tipoEvaluacion.isEmpty())) {
            tipoEvaluacion = tipoEval;
        }

        String chkIngreso = "&nbsp;", chkPeriodico = "&nbsp;", chkReintegro = "&nbsp;", chkRetiro = "&nbsp;";
        if (tipoEvaluacion != null) {
            switch (tipoEvaluacion.toUpperCase()) {
                case "INGRESO":
                    chkIngreso = "X";
                    break;
                case "PERIODICO":
                case "PERIÓDICO":
                    chkPeriodico = "X";
                    break;
                case "REINTEGRO":
                    chkReintegro = "X";
                    break;
                case "RETIRO":
                    chkRetiro = "X";
                    break;
            }
        }

        String LOGoIgmUrl = "";
        String LOGoMidenaUrl = "";
        try {
            LOGoIgmUrl = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getResource("/resources/images/LOGO_IGM_FULL_COLOR.png")
                    .toExternalForm();
        } catch (RuntimeException ex) {
            LOG.error("[PDF] No se pudo resolver LOGO_IGM_FULL_COLOR.png: " + ex.getMessage());
        }
        try {
            LOGoMidenaUrl = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getResource("/resources/images/LOGO_MIDENA.png")
                    .toExternalForm();
        } catch (RuntimeException ex) {
            LOG.error("[PDF] No se pudo resolver LOGO_MIDENA.png: " + ex.getMessage());
        }

        Map<String, String> rep = new LinkedHashMap<>();

        rep.put("LOGO_IGM_DATAURI", LOGoIgmUrl);
        rep.put("LOGO_MIDENA_DATAURI", LOGoMidenaUrl);

        rep.put("institucion", safe(institucion));
        rep.put("ruc", safe(ruc));
        rep.put("num_formulario", safe(noHistoria));
        rep.put("num_archivo", safe(noArchivo));
        rep.put("centroTrabajo", safe(centroTrabajo));
        rep.put("ciiu", safe(ciiu));

        rep.put("apellido1", safe(apellido1));
        rep.put("apellido2", safe(apellido2));
        rep.put("nombre1", safe(nombre1));
        rep.put("nombre2", safe(nombre2));
        rep.put("sexo", safe(sexo));

        rep.put("fecha_yyyy", yy.format(f));
        rep.put("fecha_MM", MM.format(f));
        rep.put("fecha_dd", dd.format(f));
        rep.put("chk_ingreso", chkIngreso);
        rep.put("chk_periodico", chkPeriodico);
        rep.put("chk_reintegro", chkReintegro);
        rep.put("chk_retiro", chkRetiro);

        rep.put("chk_apto", aApto);
        rep.put("chk_obs", aObs);
        rep.put("chk_lim", aLim);
        rep.put("chk_noapto", aNo);

        rep.put("detalleObservaciones", safe(detalleObservaciones));
        rep.put("recomendaciones", safe(recomendaciones));
        rep.put("medicoNombre", safe(medicoNombre));
        rep.put("medicoCodigo", safe(medicoCodigo));

        for (Map.Entry<String, String> e : rep.entrySet()) {
            String key = e.getKey();
            String val = (e.getValue() == null) ? "" : e.getValue();
            template = template.replace("{{" + key + "}}", val);
        }

        return template;
    }

    private String cargarRecursoComoString(String pathRelativo) throws IOException {
        InputStream in = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResourceAsStream("/resources/pdf/" + pathRelativo);
        if (in == null) {
            throw new IllegalArgumentException("No se encontró la plantilla: /resources/pdf/" + pathRelativo);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    public void syncTipoEvaluacion() {
        this.tipoEvaluacion = this.tipoEval;
    }

    private String dataUriFromResource(String pathFromResources) throws IOException {
        InputStream in = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResourceAsStream("/resources/" + pathFromResources);
        if (in == null) {
            LOG.error("[PDF] No se encontró recurso: /resources/" + pathFromResources);
            return "";
        }
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }
            bytes = bos.toByteArray();
        }
        String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
        String mime = "image/png";
        String lower = pathFromResources.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            mime = "image/jpeg";
        } else if (lower.endsWith(".gif")) {
            mime = "image/gif";
        }
        return "data:" + mime + ";base64," + base64;
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
        return cie10Service.buscarPorCodigoODescripcion(query, 20);
    }

    public List<Cie10> completarCie10PorCodigo(String query) {
        if (query == null) {
            return new ArrayList<Cie10>();
        }
        String q = query.trim().toUpperCase();
        if (q.isEmpty()) {
            return new ArrayList<Cie10>();
        }

        List<Cie10> lista = cie10Service.buscarJerarquiaPorTerm(q);

        List<Cie10> out = new ArrayList<Cie10>();
        for (Cie10 c : lista) {
            if (c != null && c.getCodigo() != null
                    && c.getCodigo().toUpperCase().startsWith(q)) {
                out.add(c);
            }
        }
        return out;
    }

    public List<Cie10> completarCie10PorDescripcion(String query) {
        if (query == null) {
            return new ArrayList<Cie10>();
        }
        String q = query.trim();
        if (q.isEmpty()) {
            return new ArrayList<Cie10>();
        }

        return cie10Service.buscarPorDescripcionLike(q, 20);
    }

    public void onCie10CodigoSelect(SelectEvent event) {
        String codigo = (String) event.getObject();
        this.codCie10Ppal = codigo;

        if (codigo != null && !codigo.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
            if (cie != null) {
                this.descCie10Ppal = cie.getDescripcion();
            } else {
                this.descCie10Ppal = null;
            }
        } else {
            this.descCie10Ppal = null;
        }
    }

    public void onCie10CodigoBlur() {
        if (this.codCie10Ppal != null && !this.codCie10Ppal.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPorCodigo(this.codCie10Ppal.trim());
            if (cie != null) {
                this.descCie10Ppal = cie.getDescripcion();
            } else {
                this.descCie10Ppal = null;
            }
        } else {
            this.descCie10Ppal = null;
        }
    }

    public void onCie10DescripcionSelect(SelectEvent event) {
        String descripcion = (String) event.getObject();
        this.descCie10Ppal = descripcion;

        if (descripcion != null && !descripcion.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(descripcion);
            if (cie != null) {
                this.codCie10Ppal = cie.getCodigo();
            } else {
                this.codCie10Ppal = null;
            }
        } else {
            this.codCie10Ppal = null;
        }
    }

    public void onCie10DescripcionBlur() {
        if (this.descCie10Ppal != null && !this.descCie10Ppal.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(this.descCie10Ppal.trim());
            if (cie != null) {
                this.codCie10Ppal = cie.getCodigo();
            } else {
                this.codCie10Ppal = null;
            }
        } else {
            this.codCie10Ppal = null;
        }
    }

    private Cie10 inferCie10PrincipalFromListaK() {
        if (listaDiag == null || listaDiag.isEmpty()) {
            return null;
        }

        ConsultaDiagnostico best = null;

        for (ConsultaDiagnostico r : listaDiag) {
            if (r == null) {
                continue;
            }
            String cod = (r.getCodigo() != null) ? r.getCodigo().trim() : "";
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

        if (best == null || best.getCodigo() == null || best.getCodigo().trim().isEmpty()) {
            return null;
        }

        return cie10Service.buscarPorCodigo(best.getCodigo().trim());
    }

    private Cie10 pickBestByDescripcion(List<Cie10> list, String input) {
        if (list == null || list.isEmpty() || input == null) {
            return null;
        }

        final String needle = norm(input);

        Cie10 best = null;
        int bestScore = Integer.MAX_VALUE;
        int bestLen = Integer.MAX_VALUE;

        for (Cie10 c : list) {
            if (c == null || c.getDescripcion() == null) {
                continue;
            }

            String cand = norm(c.getDescripcion());
            int score;

            if (cand.equals(needle)) {
                score = 0;
            } else if (cand.startsWith(needle)) {
                score = 1;
            } else if (cand.contains(needle)) {
                score = 2;
            } else {
                score = 9;
            }
            int len = cand.length();

            if (best == null || score < bestScore || (score == bestScore && len < bestLen)) {
                best = c;
                bestScore = score;
                bestLen = len;
            }
        }

        if (bestScore >= 9) {
            return null;
        }

        return best;
    }

    private String norm(String s) {

        return s.trim().toLowerCase()
                .replaceAll("\\s+", " ");
    }

    public List<String> completarCie10FilaPorCodigo(String query) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            String viewId = (fc != null && fc.getViewRoot() != null) ? fc.getViewRoot().getViewId() : "null";
            LOG.info(">>> [AC-K-COD] complete ENTER query=[" + query + "] viewId=" + viewId);

            if (query == null) {
                LOG.info("<<< [AC-K-COD] query=null => return empty");
                return new ArrayList<>();
            }

            String qRaw = query.trim().toUpperCase();
            String q = qRaw.replaceAll("[^A-Z0-9]", "");
            if (q.isEmpty() && qRaw.isEmpty()) {
                LOG.info("<<< [AC-K-COD] q empty after normalize => return empty");
                return new ArrayList<>();
            }

            List<String> out = new ArrayList<>();

            List<Cie10> lista = cie10Service.buscarJerarquiaPorTerm(q);
            LOG.debug("... [AC-K-COD] service.buscarJerarquiaPorTerm(q={}) size={}", q,
                    (lista == null ? "null" : lista.size()));

            agregarCoincidenciasCodigo(out, lista, q, qRaw);

            // Fallback: hay catálogos con códigos no normalizados (espacios/puntos).
            // Si no hubo resultados por prefijo, hacemos búsqueda tolerante por código.
            if (out.isEmpty()) {
                List<Cie10> alterna = cie10Service.buscarPorCodigoAproximado(q, 20);
                LOG.debug("... [AC-K-COD] fallback buscarPorCodigoAproximado(q={}) size={}", q,
                        (alterna == null ? "null" : alterna.size()));
                agregarCoincidenciasCodigo(out, alterna, q, qRaw);
            }

            // Último fallback: búsqueda general por término (código/descr.).
            if (out.isEmpty()) {
                List<Cie10> general = cie10Service.buscarPorTermino(q, 20);
                LOG.debug("... [AC-K-COD] fallback buscarPorTermino(q={}) size={}", q,
                        (general == null ? "null" : general.size()));
                agregarCoincidenciasCodigo(out, general, q, qRaw);
            }

            LOG.info("<<< [AC-K-COD] RETURN out.size=" + out.size()
                    + (out.isEmpty() ? "" : (" first=[" + out.get(0) + "]")));
            return out;

        } catch (Exception e) {
            LOG.error("!!! [AC-K-COD] ERROR {} : {}", e.getClass().getName(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private void agregarCoincidenciasCodigo(List<String> out, List<Cie10> lista, String qCodigo, String qTexto) {
        if (lista == null || out == null) {
            return;
        }

        String qCodigoNorm = (qCodigo == null) ? "" : qCodigo.trim();
        String qTextoNorm = (qTexto == null) ? "" : qTexto.trim().toUpperCase();

        for (Cie10 c : lista) {
            if (c == null || c.getCodigo() == null) {
                continue;
            }

            String codigo = c.getCodigo().trim();
            if (codigo.isEmpty()) {
                continue;
            }

            String codNorm = codigo.toUpperCase().replaceAll("[^A-Z0-9]", "");
            boolean coincideCodigo = !qCodigoNorm.isEmpty() && codNorm.contains(qCodigoNorm);
            String desc = c.getDescripcion();
            boolean coincideDescripcion = !qTextoNorm.isEmpty()
                    && desc != null
                    && desc.toUpperCase().contains(qTextoNorm);

            if (!coincideCodigo && !coincideDescripcion) {
                continue;
            }

            if (!out.contains(codigo)) {
                out.add(codigo);
            }

            if (out.size() >= 20) {
                return;
            }
        }
    }

    public List<String> completarCie10FilaPorDescripcion(String query) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            String viewId = (fc != null && fc.getViewRoot() != null) ? fc.getViewRoot().getViewId() : "null";
            LOG.info(">>> [AC-K-DESC] complete ENTER query=[" + query + "] viewId=" + viewId);

            List<String> out = new ArrayList<String>();
            if (query == null || query.trim().isEmpty()) {
                LOG.info("<<< [AC-K-DESC] query empty => return empty");
                return out;
            }

            List<Cie10> lista = cie10Service.buscarPorDescripcionLike(query, 20);
            LOG.debug("... [AC-K-DESC] service.buscarPorDescripcionLike size={}",
                    (lista == null ? "null" : lista.size()));

            if (lista != null) {
                for (Cie10 c : lista) {
                    if (c != null && c.getDescripcion() != null) {
                        out.add(c.getDescripcion());
                    }
                }
            }

            LOG.info("<<< [AC-K-DESC] RETURN out.size=" + out.size()
                    + (out.isEmpty() ? "" : (" first=[" + out.get(0) + "]")));
            return out;

        } catch (Exception e) {
            LOG.error("!!! [AC-K-DESC] ERROR {} : {}", e.getClass().getName(), e.getMessage(), e);
            return new ArrayList<String>();
        }
    }

    private String sn(Boolean b) {
        return Boolean.TRUE.equals(b) ? "S" : "N";
    }

    private String sn(boolean b) {
        return b ? "S" : "N";
    }

    public void abrirPersonaAuxManual() {

        if (personaAux == null) {
            personaAux = new PersonaAux();
        }

        if (!esVacio(cedulaBusqueda)
                && (personaAux.getCedula() == null || personaAux.getCedula().isEmpty())) {
            personaAux.setCedula(cedulaBusqueda.trim());
        }

        mostrarDiaLOGoAux = true;
        PrimeFaces.current().executeScript("PF('dlgPersonaAux').show();");
    }

    // Persona Auxiliar - Registro y Selección
    public void guardarPersonaAuxYUsar() {

        LOG.info(String.valueOf("INGRESA AL METODO DE GUARDAR "));
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (personaAux == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No existe información de la persona para guardar."
            ));
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
            return;
        }

        LOG.info(String.valueOf("PERSONA AUXILIAR ANTES VALIDAR: " + personaAux));

        if (esVacio(personaAux.getCedula())
                || esVacio(personaAux.getApellido1())
                || esVacio(personaAux.getNombre1())
                || esVacio(personaAux.getSexo())
                || personaAux.getFechaNac() == null) {

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Datos incompletos",
                    "Cédula, primer apellido, primer nombre, sexo y fecha de nacimiento son obligatorios."
            ));
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
            return;
        }

        try {

            personaAux.setCedula(personaAux.getCedula().trim());

            if (!esVacio(personaAux.getApellido1())) {
                personaAux.setApellido1(personaAux.getApellido1().trim().toUpperCase());
            }
            if (!esVacio(personaAux.getApellido2())) {
                personaAux.setApellido2(personaAux.getApellido2().trim().toUpperCase());
            }
            if (!esVacio(personaAux.getNombre1())) {
                personaAux.setNombre1(personaAux.getNombre1().trim().toUpperCase());
            }
            if (!esVacio(personaAux.getNombre2())) {
                personaAux.setNombre2(personaAux.getNombre2().trim().toUpperCase());
            }
            if (!esVacio(personaAux.getSexo())) {
                personaAux.setSexo(personaAux.getSexo().trim().toUpperCase());
            }

            Date ahora = new Date();
            if (personaAux.getIdPersonaAux() == null) {
                personaAux.setEstado("A");
                personaAux.setFechaCreacion(ahora);
                personaAux.setUsrCreacion("SISTEMA");
            } else {
                personaAux.setFechaActualizacion(ahora);
                personaAux.setUsrActualizacion("SISTEMA");
            }

            personaAux = personaAuxService.guardar(personaAux);

            this.cedulaBusqueda = personaAux.getCedula();
            this.apellido1 = personaAux.getApellido1();
            this.apellido2 = personaAux.getApellido2();
            this.nombre1 = personaAux.getNombre1();
            this.nombre2 = personaAux.getNombre2();
            this.sexo = personaAux.getSexo();
            this.fechaNacimiento = personaAux.getFechaNac();
            this.noHistoria = personaAux.getCedula();

            mostrarDiaLOGoAux = false;
            mostrarDlgCedula = false;
            permitirIngresoManual = false;
            PrimeFaces.current().ajax().update(":noHistoriaClinica");
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", false);
            PrimeFaces.current().executeScript(
                    "PF('dlgPersonaAux').hide(); PF('dlgCedula').hide();"
            );

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Datos guardados",
                    "Se guardó la persona auxiliar y se cargaron los datos en la ficha."
            ));

            LOG.info("PersonaAux guardada manualmente: {} {} / {} {} (cedula={})",
                    personaAux.getApellido1(),
                    personaAux.getApellido2(),
                    personaAux.getNombre1(),
                    personaAux.getNombre2(),
                    personaAux.getCedula());

        } catch (RuntimeException e) {
            LOG.error("Error guardando datos manuales", e);
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Ocurrió un error al procesar y guardar los datos."
            ));
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
        }
    }

    // Búsqueda de Cédula / Diálogo Inicial
    public void buscarCedula() {
        try {
            CedulaSearchService.CedulaSearchResult result = cedulaSearchService.search(cedulaBusqueda, ficha, personaAux);
            applyCedulaSearchResult(result);

            if (result.isFound()) {
                tryLoadCargoFromVista();
                cedulaDialogUiCoordinator.onFound(result);
            } else if (result.isShowManual()) {
                cedulaDialogUiCoordinator.onManualEnabled(result);
            }

            if (result.isFound() || result.isShowManual()) {
                mostrarDlgCedula = false;
            }

            cedulaDialogUiCoordinator.refreshMainViews();
        } catch (CedulaSearchService.CedulaValidationException ex) {
            cedulaDialogUiCoordinator.onValidationWarning(ex.getMessage());
        } catch (RuntimeException ex) {
            handleUnexpected("buscarCedula", ex);
            cedulaDialogUiCoordinator.onSearchError();
        }
    }

    private void tryLoadCargoFromVista() {
        CedulaSearchService.CargoLookupResult cargo = cedulaSearchService.lookupCargo(cedulaBusqueda);

        if (ficha == null) {
            return;
        }

        if (cargo.isEmptyCedula()) {
            ficha.setCiiu(null);
            return;
        }

        if (!cargo.isFound()) {
            ficha.setCiiu(null);
            cedulaDialogUiCoordinator.showCargoMissing();
            return;
        }

        ficha.setCiiu(cargo.getCargoDescripcion());
    }

    private void applyCedulaSearchResult(CedulaSearchService.CedulaSearchResult result) {
        this.cedulaBusqueda = result.getCedula();
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
            this.permitirIngresoManual = false;
            this.personaAux = null;
            if (this.ficha != null) {
                this.ficha.setPersonaAux(null);
            }
        } else {
            this.permitirIngresoManual = result.isShowManual();
        }
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

        if (esVacio(cedulaBusqueda)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Cédula requerida",
                    "Ingrese la cédula antes de continuar."
            ));
            return;
        }

        if (personaAux == null) {
            personaAux = new PersonaAux();
        }

        personaAux.setCedula(cedulaBusqueda.trim());

        personaAux.setApellido1(null);
        personaAux.setApellido2(null);
        personaAux.setNombre1(null);
        personaAux.setNombre2(null);
        personaAux.setSexo(null);
        personaAux.setFechaNac(null);

        permitirIngresoManual = true;
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

    private void ensureActLabSize() {
        final int n = H_ROWS;

        initActivityLabListsIfNull();

        ensureListSize(actLabCentroTrabajo, n, null);
        ensureListSize(actLabActividad, n, null);
        ensureListSize(actLabTiempo, n, null);
        ensureListSize(actLabObservaciones, n, null);
        ensureListSize(iessEspecificar, n, null);

        ensureListSize(actLabTrabajoAnterior, n, null);
        ensureListSize(actLabTrabajoActual, n, null);
        ensureListSize(actLabIncidenteChk, n, null);
        ensureListSize(actLabAccidenteChk, n, null);
        ensureListSize(actLabEnfermedadChk, n, null);
        ensureListSize(iessSi, n, null);
        ensureListSize(iessNo, n, null);

        ensureListSize(iessFecha, n, null);

        rebuildActivityLabRowNumbers(n);
    }

    private void initActivityLabListsIfNull() {
        if (actLabRows == null) {
            actLabRows = new ArrayList<String>();
        }
        if (actLabCentroTrabajo == null) {
            actLabCentroTrabajo = new ArrayList<String>();
        }
        if (actLabActividad == null) {
            actLabActividad = new ArrayList<String>();
        }
        if (actLabTiempo == null) {
            actLabTiempo = new ArrayList<String>();
        }
        if (actLabObservaciones == null) {
            actLabObservaciones = new ArrayList<String>();
        }

        if (actLabTrabajoAnterior == null) {
            actLabTrabajoAnterior = new ArrayList<Boolean>();
        }
        if (actLabTrabajoActual == null) {
            actLabTrabajoActual = new ArrayList<Boolean>();
        }
        if (actLabIncidenteChk == null) {
            actLabIncidenteChk = new ArrayList<Boolean>();
        }
        if (actLabAccidenteChk == null) {
            actLabAccidenteChk = new ArrayList<Boolean>();
        }
        if (actLabEnfermedadChk == null) {
            actLabEnfermedadChk = new ArrayList<Boolean>();
        }

        if (iessSi == null) {
            iessSi = new ArrayList<Boolean>();
        }
        if (iessNo == null) {
            iessNo = new ArrayList<Boolean>();
        }
        if (iessFecha == null) {
            iessFecha = new ArrayList<Date>();
        }
        if (iessEspecificar == null) {
            iessEspecificar = new ArrayList<String>();
        }
    }

    private <T> void ensureListSize(List<T> list, int size, T defaultValue) {
        while (list.size() < size) {
            list.add(defaultValue);
        }
        if (list.size() > size) {
            list.subList(size, list.size()).clear();
        }
    }

    private void rebuildActivityLabRowNumbers(int n) {
        actLabRows.clear();
        for (int i = 1; i <= n; i++) {
            actLabRows.add(String.valueOf(i));
        }
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

    public void setExfPielCicatrices(String exfPielCicatrices) { this.exfPielCicatrices = exfPielCicatrices; }
    public void setExfOjosParpados(String exfOjosParpados) { this.exfOjosParpados = exfOjosParpados; }
    public void setExfOjosConjuntivas(String exfOjosConjuntivas) { this.exfOjosConjuntivas = exfOjosConjuntivas; }
    public void setExfOjosPupilas(String exfOjosPupilas) { this.exfOjosPupilas = exfOjosPupilas; }
    public void setExfOjosCornea(String exfOjosCornea) { this.exfOjosCornea = exfOjosCornea; }
    public void setExfOjosMotilidad(String exfOjosMotilidad) { this.exfOjosMotilidad = exfOjosMotilidad; }
    public void setExfOidoConducto(String exfOidoConducto) { this.exfOidoConducto = exfOidoConducto; }
    public void setExfOidoPabellon(String exfOidoPabellon) { this.exfOidoPabellon = exfOidoPabellon; }
    public void setExfOidoTimpanos(String exfOidoTimpanos) { this.exfOidoTimpanos = exfOidoTimpanos; }
    public void setExfOroLabios(String exfOroLabios) { this.exfOroLabios = exfOroLabios; }
    public void setExfOroLengua(String exfOroLengua) { this.exfOroLengua = exfOroLengua; }
    public void setExfOroFaringe(String exfOroFaringe) { this.exfOroFaringe = exfOroFaringe; }
    public void setExfOroAmigdalas(String exfOroAmigdalas) { this.exfOroAmigdalas = exfOroAmigdalas; }
    public void setExfOroDentadura(String exfOroDentadura) { this.exfOroDentadura = exfOroDentadura; }
    public void setExfNarizTabique(String exfNarizTabique) { this.exfNarizTabique = exfNarizTabique; }
    public void setExfNarizCornetes(String exfNarizCornetes) { this.exfNarizCornetes = exfNarizCornetes; }
    public void setExfNarizMucosas(String exfNarizMucosas) { this.exfNarizMucosas = exfNarizMucosas; }
    public void setExfNarizSenos(String exfNarizSenos) { this.exfNarizSenos = exfNarizSenos; }
    public void setExfCuelloTiroides(String exfCuelloTiroides) { this.exfCuelloTiroides = exfCuelloTiroides; }
    public void setExfCuelloMovilidad(String exfCuelloMovilidad) { this.exfCuelloMovilidad = exfCuelloMovilidad; }
    public void setExfToraxMamas(String exfToraxMamas) { this.exfToraxMamas = exfToraxMamas; }
    public void setExfToraxPulmones(String exfToraxPulmones) { this.exfToraxPulmones = exfToraxPulmones; }
    public void setExfToraxCorazon(String exfToraxCorazon) { this.exfToraxCorazon = exfToraxCorazon; }
    public void setExfToraxParrilla(String exfToraxParrilla) { this.exfToraxParrilla = exfToraxParrilla; }
    public void setExfAbdomenVisceras(String exfAbdomenVisceras) { this.exfAbdomenVisceras = exfAbdomenVisceras; }
    public void setExfAbdomenPared(String exfAbdomenPared) { this.exfAbdomenPared = exfAbdomenPared; }
    public void setExfColumnaFlexibilidad(String exfColumnaFlexibilidad) { this.exfColumnaFlexibilidad = exfColumnaFlexibilidad; }
    public void setExfColumnaDesviacion(String exfColumnaDesviacion) { this.exfColumnaDesviacion = exfColumnaDesviacion; }
    public void setExfColumnaDolor(String exfColumnaDolor) { this.exfColumnaDolor = exfColumnaDolor; }
    public void setExfPelvisPelvis(String exfPelvisPelvis) { this.exfPelvisPelvis = exfPelvisPelvis; }
    public void setExfPelvisGenitales(String exfPelvisGenitales) { this.exfPelvisGenitales = exfPelvisGenitales; }
    public void setExfExtVascular(String exfExtVascular) { this.exfExtVascular = exfExtVascular; }
    public void setExfExtSup(String exfExtSup) { this.exfExtSup = exfExtSup; }
    public void setExfExtInf(String exfExtInf) { this.exfExtInf = exfExtInf; }
    public void setExfNeuroFuerza(String exfNeuroFuerza) { this.exfNeuroFuerza = exfNeuroFuerza; }
    public void setExfNeuroSensibilidad(String exfNeuroSensibilidad) { this.exfNeuroSensibilidad = exfNeuroSensibilidad; }
    public void setExfNeuroMarcha(String exfNeuroMarcha) { this.exfNeuroMarcha = exfNeuroMarcha; }
    public void setExfNeuroReflejos(String exfNeuroReflejos) { this.exfNeuroReflejos = exfNeuroReflejos; }

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
                cie = pickBestByDescripcion(candidatos, desc);
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

    public FichaRiesgoService getFichaRiesgoService() {
        return fichaRiesgoService;
    }

    public void setFichaRiesgoService(FichaRiesgoService fichaRiesgoService) {
        this.fichaRiesgoService = fichaRiesgoService;
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

    public FichaRiesgoDetService getFichaRiesgoDetService() {
        return fichaRiesgoDetService;
    }

    public void setFichaRiesgoDetService(FichaRiesgoDetService fichaRiesgoDetService) {
        this.fichaRiesgoDetService = fichaRiesgoDetService;
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

    private String sn(Object boolOrString) {
        if (boolOrString == null) {
            return "NO";
        }
        String s = String.valueOf(boolOrString).trim();
        if ("true".equalsIgnoreCase(s) || "1".equals(s) || "X".equalsIgnoreCase(s) || "SI".equalsIgnoreCase(s)) {
            return "SI";
        }
        return "NO";
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

}
