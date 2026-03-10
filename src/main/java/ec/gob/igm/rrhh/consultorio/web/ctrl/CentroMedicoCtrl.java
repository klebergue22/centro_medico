package ec.gob.igm.rrhh.consultorio.web.ctrl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import ec.gob.igm.rrhh.consultorio.domain.dto.EmpleadoCargoDTO;

import ec.gob.igm.rrhh.consultorio.domain.model.AuditoriaConsultorio;
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
import ec.gob.igm.rrhh.consultorio.service.AuditoriaConsultorioService;
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
import ec.gob.igm.rrhh.consultorio.service.SignosVitalesService;
import ec.gob.igm.rrhh.consultorio.service.Step1VitalSignsManager;
import ec.gob.igm.rrhh.consultorio.web.util.SnUtils;

/**
 *
 * @author GUERRA_KLEBER
 */
@Named("centroMedicoCtrl")
@ViewScoped

public class CentroMedicoCtrl implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(CentroMedicoCtrl.class);

    private static final long serialVersionUID = 1L;
    private static final List<String> STATIC_RISK_COLS = new ArrayList<>();

    static {
        for (int i = 1; i <= 7; i++) {
            STATIC_RISK_COLS.add(String.valueOf(i));
        }
    }

    public static class BusinessValidationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

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

    // ============================================================
// D. ENFERMEDAD O PROBLEMA ACTUAL
// ============================================================
    private String enfermedadActual;

// ============================================================
// F. EXAMEN FÍSICO REGIONAL
// ============================================================
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
    private transient SignosVitalesService signosService;
    @EJB
    private transient Step1VitalSignsManager step1VitalSignsManager;
    @EJB
    private transient FichaRiesgoService fichaRiesgoService;
    @EJB
    private transient FichaDiagnosticoService fichaDiagnosticoService;
    @EJB
    private transient EmpleadoService empleadoService;
    @EJB
    private transient PersonaAuxService personaAuxService;
    @EJB
    private transient AuditoriaConsultorioService auditoriaService;
    @EJB
    private transient FichaActLaboralService fichaActLaboralService;
    @EJB
    private transient FichaRiesgoDetService fichaRiesgoDetService;

    @EJB
    private transient FichaExamenCompService fichaExamenCompService;
    @EJB
    private transient ExamenFisicoRegionalService examenFisicoRegionalService;

    // =====================================================
    // JSF Lifecycle / Inicialización
    // =====================================================
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
                initExamenes(5);
                ensureActLabSize();
                ensureDiagSize(6);
                preRenderDone = true;
            } else {

                ensureActLabSize();
                ensureDiagSize(6);
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

    private void initExamenes(int n) {

        examNombre = new ArrayList<>(java.util.Collections.nCopies(n, ""));
        examFecha = new ArrayList<>(java.util.Collections.nCopies(n, null));
        examResultado = new ArrayList<>(java.util.Collections.nCopies(n, ""));
    }

    private void initActLab(int n) {
        actLabRows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            actLabRows.add(String.valueOf(i + 1));
        }

        actLabCentroTrabajo = new ArrayList<>(java.util.Collections.nCopies(n, ""));
        actLabActividad = new ArrayList<>(java.util.Collections.nCopies(n, ""));
        actLabIncidente = new ArrayList<>(java.util.Collections.nCopies(n, ""));
        actLabFecha = new ArrayList<>(java.util.Collections.nCopies(n, null));
        actLabTiempo = new ArrayList<>(java.util.Collections.nCopies(n, ""));

        actLabTrabajoAnterior = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        actLabTrabajoActual = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        actLabIncidenteChk = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        actLabAccidenteChk = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        actLabEnfermedadChk = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));

        actLabObservaciones = new ArrayList<>(java.util.Collections.nCopies(n, ""));

        iessSi = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        iessNo = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        iessFecha = new ArrayList<>(java.util.Collections.nCopies(n, null));
        iessEspecificar = new ArrayList<>(java.util.Collections.nCopies(n, ""));
    }

    private void initActividadesExtra(int n) {
        fechaAct = new ArrayList<>(java.util.Collections.nCopies(n, null));
        tipoAct = new ArrayList<>(java.util.Collections.nCopies(n, ""));
        descAct = new ArrayList<>(java.util.Collections.nCopies(n, ""));
    }

    @PostConstruct
    public void init() {
        initUiDefaults();
        initDomainDefaults();
        initStep2Defaults();
        initStep3Defaults();
    }

    private void initUiDefaults() {
        mostrarDlgCedula = true;
        fechaAtencion = new Date();

        initActLab(3);
        initActividadesExtra(3);

        tipoEval = "INGRESO";
        sexo = "M";
        grupoSanguineo = "";
        lateralidad = "";
        examenReproMasculino = "";

        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getViewRoot() != null) {
            fc.getViewRoot().setLocale(new Locale("es", "EC"));
        }

        institucion = "Instituto Geográfico Militar";
        institucion = institucion.toUpperCase();
        ruc = "1768007200001";
    }

    private void initDomainDefaults() {
        ficha = new FichaOcupacional();

        ficha.setNoHistoriaClinica(null);
        ficha.setInstSistema(institucion);

        signos = new SignosVitales();

        consulta = new ConsultaMedica();
        listaDiag = new ArrayList<ConsultaDiagnostico>();

        fichaRiesgo = new FichaRiesgo();
        fichaRiesgo.setFicha(ficha);
        fichaRiesgo.setEstado("BORRADOR");

        personaAux = new PersonaAux();

        if (medidasPreventivas == null) {
            medidasPreventivas = new ArrayList<String>();
        }

        if (empleadoSel != null) {
            ficha.setEmpleado(empleadoSel);
            consulta.setEmpleado(empleadoSel);
        }

        if (ficha.getRucEstablecimiento() == null || ficha.getRucEstablecimiento().isBlank()) {
            ficha.setRucEstablecimiento(ruc);

        }

        ficha.setFechaEvaluacion(fechaAtencion);
        ficha.setTipoEvaluacion(tipoEval);

        initDefaultDiagnosisRows();
    }

    private void initDefaultDiagnosisRows() {
        listaDiag = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ConsultaDiagnostico cd = new ConsultaDiagnostico();
            cd.setTipoDiag(null);
            cd.setCie10(null);
            cd.setCodigo(null);
            cd.setDescripcion(null);
            listaDiag.add(cd);
        }
    }

    private void initStep2Defaults() {

        if (ficha == null) {
            ficha = new FichaOcupacional();
        }

        if (fichaRiesgo == null) {
            fichaRiesgo = new FichaRiesgo();
            fichaRiesgo.setFicha(ficha);
            fichaRiesgo.setEstado("BORRADOR");
        }

        if (actividadesLab == null) {
            actividadesLab = new ArrayList<String>();
        }
        while (actividadesLab.size() < 7) {
            actividadesLab.add(null);
        }

        if (medidasPreventivas == null) {
            medidasPreventivas = new ArrayList<String>();
        }
        while (medidasPreventivas.size() < 7) {
            medidasPreventivas.add(null);
        }

        if (riesgos == null) {
            riesgos = new LinkedHashMap<String, Boolean>();
        }
        if (otrosRiesgos == null) {
            otrosRiesgos = new LinkedHashMap<String, String>();
        }
        if (fichaRiesgo == null) {
            fichaRiesgo = new FichaRiesgo();
        }

        if (STATIC_RISK_COLS == null || STATIC_RISK_COLS.isEmpty()) {
            LOG.warn("CRÍTICO: STATIC_RISK_COLS está vacío. Reinicializando...");
            STATIC_RISK_COLS.clear();
            for (int i = 1; i <= 7; i++) {
                STATIC_RISK_COLS.add(String.valueOf(i));
            }
        }

        this.riskCols = STATIC_RISK_COLS;
    }

    private void initStep3Defaults() {

        ginecoExamen1 = "";
        ginecoTiempo1 = "";
        ginecoResultado1 = "";
        ginecoExamen2 = "";
        ginecoTiempo2 = "";
        ginecoResultado2 = "";
        ginecoObservacion = "";
        // Inicializar enfermedad actual
        enfermedadActual = "";

// Inicializar examen físico
        exfPielCicatrices = "";
        exfOjosParpados = "";
        exfOjosConjuntivas = "";
        exfOjosPupilas = "";
        exfOjosCornea = "";
        exfOjosMotilidad = "";
        exfOidoConducto = "";
        exfOidoPabellon = "";
        exfOidoTimpanos = "";
        exfOroLabios = "";
        exfOroLengua = "";
        exfOroFaringe = "";
        exfOroAmigdalas = "";
        exfOroDentadura = "";
        exfNarizTabique = "";
        exfNarizCornetes = "";
        exfNarizMucosas = "";
        exfNarizSenos = "";
        exfCuelloTiroides = "";
        exfCuelloMovilidad = "";
        exfToraxMamas = "";
        exfToraxPulmones = "";
        exfToraxCorazon = "";
        exfToraxParrilla = "";
        exfAbdomenVisceras = "";
        exfAbdomenPared = "";
        exfColumnaFlexibilidad = "";
        exfColumnaDesviacion = "";
        exfColumnaDolor = "";
        exfPelvisPelvis = "";
        exfPelvisGenitales = "";
        exfExtVascular = "";
        exfExtSup = "";
        exfExtInf = "";
        exfNeuroFuerza = "";
        exfNeuroSensibilidad = "";
        exfNeuroMarcha = "";
        exfNeuroReflejos = "";
        obsExamenFisico = "";

        if (examFecha == null) {
            examFecha = new ArrayList<Date>();
        } else {
            examFecha.clear();
        }
        for (int i = 0; i < 5; i++) {
            examFecha.add(null);
        }

        if (examNombre == null) {
            examNombre = new ArrayList<String>();
        } else {
            examNombre.clear();
        }
        for (int i = 0; i < 5; i++) {
            examNombre.add("");
        }

        if (examResultado == null) {
            examResultado = new ArrayList<String>();
        } else {
            examResultado.clear();
        }
        for (int i = 0; i < 5; i++) {
            examResultado.add("");
        }

        if (obsJ == null) {
            obsJ = "";
        }

        hCentroTrabajo = new String[H_ROWS];
        hActividad = new String[H_ROWS];
        hIncidente = new Boolean[H_ROWS];
        hAccidente = new Boolean[H_ROWS];
        hTiempo = new Integer[H_ROWS];
        hEnfOcupacional = new Boolean[H_ROWS];
        hEnfComun = new Boolean[H_ROWS];
        hEnfProfesional = new Boolean[H_ROWS];
        hOtros = new Boolean[H_ROWS];
        hOtrosCual = new String[H_ROWS];
        hFecha = new Date[H_ROWS];
        hEspecificacion = new String[H_ROWS];
        hObservacion = new String[H_ROWS];

        consTiempoConsumoMeses = new Integer[]{0, 0, 0};
        consTiempoAbstinenciaMeses = new Integer[]{0, 0, 0};

        consExConsumidor = new Boolean[]{false, false, false};
        consNoConsume = new Boolean[]{false, false, false};

        afCual = new String[3];
        afTiempo = new String[3];

        medCual = new String[3];
        medCant = new Integer[3];

        consOtrasCual = null;
        consumoVidaCondObs = null;

        if (tipoAct == null) {
            tipoAct = new ArrayList<String>();
        } else {
            tipoAct.clear();
        }
        if (fechaAct == null) {
            fechaAct = new ArrayList<Date>();
        } else {
            fechaAct.clear();
        }
        if (descAct == null) {
            descAct = new ArrayList<String>();
        } else {
            descAct.clear();
        }
        for (int i = 0; i < 3; i++) {
            tipoAct.add("");
            fechaAct.add(null);
            descAct.add("");
        }

        actLabRows = Arrays.asList("1", "2", "3", "4");
        initActLab(H_ROWS);
        ensureActLabSize();

        ensureDiagSize(DIAG_ROWS);

        initConsumoVidaCond();

        if (personaAux == null) {
            personaAux = new PersonaAux();
        }
        permitirIngresoManual = false;
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

    private void ensureDiagSize(int size) {
        if (size <= 0) {
            return;
        }
        if (listaDiag == null) {
            listaDiag = new ArrayList<ConsultaDiagnostico>();
        }
        while (listaDiag.size() < size) {
            ConsultaDiagnostico d = new ConsultaDiagnostico();

            d.setTipoDiag("P");
            listaDiag.add(d);
        }
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
            AuditoriaConsultorio aud = new AuditoriaConsultorio();
            aud.setModulo("CENTRO_MEDICO");
            aud.setUsuario("USR_APP");
            aud.setFecha(new Date());
            aud.setAccion(accion);
            aud.setTablaAfecta(tabla);
            aud.setCampoAfecta(campo);
            aud.setObservaciones(observaciones);

            auditoriaService.guardar(aud);

            s3("registrarAuditoria() OK");
        } catch (RuntimeException e) {

            s3e("registrarAuditoria() FALLÓ", e);
        }
    }

    /**
     * guarda el step actual
     */
    public void guardarStepActual() {
    LOG.info(">>> ENTRO A guardarStepActual, step=" + activeStep);
    FacesContext ctx = FacesContext.getCurrentInstance();

    try {
        final String next = saveCurrentStepAndGetNext();

        if (ctx != null && !ctx.isValidationFailed() && next != null) {
            activeStep = next;

            // ✅ Aquí sí o sí: cada vez que llegas a Step4 regenera ambos PDFs
            if ("step4".equals(next)) {
                onEnterStep4AutoRegenerar();

                // ✅ refresca el wizard para que el <object> tome el nuevo token
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

        // ✅ fuerza render y evita “quedarse pegado”
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

    // 1) Solo aplica para ingreso manual
    if (!permitirIngresoManual) {
        return;
    }

    // 2) Debe existir personaAux
    if (personaAux == null) {
        return;
    }

    // 3) Si ya está persistida, solo asegurar relación
    if (personaAux.getIdPersonaAux() != null) {
        if (ficha != null) {
            ficha.setPersonaAux(personaAux);
        }
        return;
    }

    // 4) Persistir PersonaAux primero
    PersonaAux saved = personaAuxService.guardar(personaAux);  // <-- tu service real

    this.personaAux = saved;

    if (ficha != null) {
        ficha.setPersonaAux(saved);
    }
}
    private String saveCurrentStepAndGetNext() {
        if ("step1".equals(activeStep)) {
            guardarStep1();
            return "step2";
        }
        if ("step2".equals(activeStep)) {
            guardarStep2();
            return "step3";
        }
        if ("step3".equals(activeStep)) {
            guardarStep3();
            return "step4";
        }
        return null;
    }

    public void retrocederStep() {
        if ("step2".equals(activeStep)) {
            activeStep = "step1";
        } else if ("step3".equals(activeStep)) {
            activeStep = "step2";
        } else if ("step4".equals(activeStep)) {
            activeStep = "step3";
        }
    }

    private boolean validarStep1() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        boolean valido = true;

        if (isBlank(apellido1) && isBlank(apellido2)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar al menos un apellido."));
            valido = false;
        }

        if (isBlank(nombre1) && isBlank(nombre2)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar al menos un nombre."));
            valido = false;
        }

        if (isBlank(sexo)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe seleccionar el sexo del paciente."));
            valido = false;
        }

        if (isBlank(tipoEval)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe seleccionar el tipo de evaluación (Ingreso, Periódica, etc.)."));
            valido = false;
        }

        if (signos == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe registrar los signos vitales."));
            return false;
        }

        if (signos.getPaSistolica() == null || signos.getPaDiastolica() == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar la presión arterial completa (PA sistólica y diastólica)."));
            valido = false;
        }

        if (signos.getFrecuenciaCard() == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar la frecuencia cardíaca (FC)."));
            valido = false;
        }

        if (signos.getPesoKg() == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar el peso (kg)."));
            valido = false;
        }

        if (signos.getTallaM() == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar la talla (en metros o convertir desde cm)."));
            valido = false;
        }

        if (fichaRiesgo == null || isBlank(fichaRiesgo.getPuestoTrabajo())) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar el puesto de trabajo."));
            valido = false;
        }

        return valido;
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

    private void mapConsumoVidaCondToFicha(FichaOcupacional ficha) {
        if (ficha == null) {
            return;
        }

        if (consTiempoConsumoMeses == null || consTiempoConsumoMeses.length < 3) {
            consTiempoConsumoMeses = new Integer[3];
        }

        if (consExConsumidor == null || consExConsumidor.length < 3) {
            consExConsumidor = new Boolean[3];
        }

        if (consTiempoAbstinenciaMeses == null || consTiempoAbstinenciaMeses.length < 3) {
            consTiempoAbstinenciaMeses = new Integer[3];
        }

        if (consNoConsume == null || consNoConsume.length < 3) {
            consNoConsume = new Boolean[3];
        }

        if (afCual == null || afCual.length < 3) {
            afCual = new String[3];
        }
        if (afTiempo == null || afTiempo.length < 3) {
            afTiempo = new String[3];
        }

        if (medCual == null || medCual.length < 3) {
            medCual = new String[3];
        }
        if (medCant == null || medCant.length < 3) {
            medCant = new Integer[3];
        }

        ficha.setTabConsMeses(consTiempoConsumoMeses[0]);
        ficha.setTabExCons(sn(consExConsumidor[0]));
        ficha.setTabAbsMeses(consTiempoAbstinenciaMeses[0]);
        ficha.setTabNoCons(sn(consNoConsume[0]));

        ficha.setAlcConsMeses(consTiempoConsumoMeses[1]);
        ficha.setAlcExCons(sn(consExConsumidor[1]));
        ficha.setAlcAbsMeses(consTiempoAbstinenciaMeses[1]);
        ficha.setAlcNoCons(sn(consNoConsume[1]));

        ficha.setOtrCual(consOtrasCual);
        ficha.setOtrConsMeses(consTiempoConsumoMeses[2]);
        ficha.setOtrExCons(sn(consExConsumidor[2]));
        ficha.setOtrAbsMeses(consTiempoAbstinenciaMeses[2]);
        ficha.setOtrNoCons(sn(consNoConsume[2]));

        ficha.setAfCual1(afCual[0]);
        ficha.setAfTiempo1(afTiempo[0]);
        ficha.setAfCual2(afCual[1]);
        ficha.setAfTiempo2(afTiempo[1]);
        ficha.setAfCual3(afCual[2]);
        ficha.setAfTiempo3(afTiempo[2]);

        ficha.setMedCual1(medCual[0]);
        ficha.setMedCant1(medCant[0]);
        ficha.setMedCual2(medCual[1]);
        ficha.setMedCant2(medCant[1]);
        ficha.setMedCual3(medCual[2]);
        ficha.setMedCant3(medCant[2]);

        ficha.setObsConsumoVidaCond(consumoVidaCondObs);
    }

    private String usuarioReal() {
        try {

            return "USR_APP";
        } catch (RuntimeException e) {
            return "USR_APP";
        }
    }

    // =====================================================
    // Wizard - Guardado Step 1
    // =====================================================
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
        final Date now = new Date();
        final String user = usuarioReal();

        ensureFichaInitialized();
        resolveSelectedEmployeeIfNeeded();

        validateStep1InputsOrThrow();

        final String patientId = assignPatientAndHistory(now, user);

        mapStep1ToOccupationalRecord(now, user, patientId);

        final SignosVitales savedSv = upsertVitalSigns(now, user);

        saveDraftOccupationalRecord(now, user);
        
        

        auditStep1(savedSv);
    }

    private void ensureFichaInitialized() {
        if (ficha == null) {
            ficha = new FichaOcupacional();
        }
    }

    private void resolveSelectedEmployeeIfNeeded() {
        if (empleadoSel == null && noPersonaSel != null) {
            empleadoSel = empleadoService.buscarPorId(noPersonaSel);
        }
    }

    public void onBuscarPorCedulaRh() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        PrimeFaces pf = PrimeFaces.current();

        try {
            // 1) Búsqueda normal (ya te llena empleadoSel/personaAux y cierra diálogo si aplica)
            buscarCedula();

            // 2) Cargar cargo vigente desde la vista RRHH y setearlo en ficha.ciiu
            tryLoadCargoFromVista(ctx);

            // 3) Refrescar UI (panel principal + mensajes)
            pf.ajax().update(":msgs", "@([id$=wdzFicha])");

        } catch (RuntimeException ex) {
            handleUnexpected("onBuscarPorCedulaRh", ex);
            addCedulaDiaLOGMessage(ctx, FacesMessage.SEVERITY_ERROR, "Error",
                    "Ocurrió un error al consultar datos de RRHH.");
            pf.ajax().update(":msgs");
        }
    }

    private void validateStep1InputsOrThrow() {
        if (fechaAtencion == null) {
            fail("Debe ingresar la fecha de atención.");
        }
        if (esVacio(tipoEval)) {
            fail("Debe seleccionar el tipo de evaluación.");
        }
        validatePatientOrThrow();
        validateVitalSignsInputsOrThrow();
    }

    private void validatePatientOrThrow() {
        if (empleadoSel != null) {
            return;
        }
        if (personaAux == null || esVacio(personaAux.getCedula())) {
            fail("Debe seleccionar un empleado de RRHH o registrar una persona auxiliar (cédula obligatoria).");
        }
        if (esVacio(personaAux.getApellido1()) || esVacio(personaAux.getNombre1()) || esVacio(personaAux.getSexo())) {
            fail("En Persona Auxiliar: primer apellido, primer nombre y sexo son obligatorios.");
        }
    }

    private void validateVitalSignsInputsOrThrow() {
        if (esVacio(paStr)) {
            fail("Debe ingresar la presión arterial (PA) en formato 120/80.");
        }
        if (fc == null) {
            fail("Debe ingresar la frecuencia cardíaca (FC).");
        }
        if (peso == null || peso <= 0) {
            fail("Debe ingresar el peso (kg).");
        }
        if (tallaCm == null) {
            fail("Debe ingresar la talla (cm).");
        }
    }

    private String assignPatientAndHistory(Date now, String user) {
        String cedulaPaciente;

        if (empleadoSel != null) {
            ficha.setEmpleado(empleadoSel);
            ficha.setPersonaAux(null);
            cedulaPaciente = empleadoSel.getNoCedula();
        } else {
            if (personaAux.getIdPersonaAux() == null) {
                personaAux.setFechaCreacion(now);
                personaAux.setUsrCreacion(user);
                personaAux = personaAuxService.guardar(personaAux);
            }
            ficha.setPersonaAux(personaAux);
            ficha.setEmpleado(null);
            cedulaPaciente = personaAux.getCedula();
        }

        ficha.setNoHistoriaClinica(cedulaPaciente);
        ficha.setNoArchivo(cedulaPaciente);
        return cedulaPaciente;
    }

    private void mapStep1ToOccupationalRecord(Date now, String user, String patientId) {

    ficha.setFechaEvaluacion(fechaAtencion);
    ficha.setTipoEvaluacion(tipoEval);

    ficha.setGinecoExamen1(ginecoExamen1);
    ficha.setGinecoTiempo1(ginecoTiempo1);
    ficha.setGinecoResultado1(ginecoResultado1);
    ficha.setGinecoExamen2(ginecoExamen2);
    ficha.setGinecoTiempo2(ginecoTiempo2);
    ficha.setGinecoResultado2(ginecoResultado2);
    ficha.setGinecoObservacion(ginecoObservacion);

    ficha.setApEmbarazada(sn(apEmbarazada));
    ficha.setApDiscapacidad(sn(apDiscapacidad));
    ficha.setApCatastrofica(sn(apCatastrofica));
    ficha.setApLactancia(sn(apLactancia));
    ficha.setApAdultoMayor(sn(apAdultoMayor));

    ficha.setAntClinicoQuir(antClinicoQuirurgico);
    ficha.setAntFamiliares(antFamiliares);
    ficha.setCondicionEspecial(condicionEspecial);

    ficha.setAutorizaTransfusion(autorizaTransfusion);
    ficha.setTratHormonal(tratamientoHormonal);
    ficha.setTratHormonalCual(tratamientoHormonalCual);

    ficha.setExamReproMasc(examenReproMasculino);
    ficha.setTiempoReproMasc(tiempoReproMasculino);

    ficha.setFum(fum);
    ficha.setGestas(gestas);
    ficha.setPartos(partos);
    ficha.setCesareas(cesareas);
    ficha.setAbortos(abortos);
    ficha.setPlanificacion(planificacion);
    ficha.setPlanificacionCual(planificacionCual);

    // D. ENFERMEDAD O PROBLEMA ACTUAL (Step 1)
    //ficha.setEnfermedadProbActual(trimToNull(enfermedadActual));
    if (ficha.getEnfermedadProbActual() != null) {
        ficha.setEnfermedadProbActual(ficha.getEnfermedadProbActual().trim());
    }
    // =====================================================
    // Discapacidad (solo si AP_DISCAPACIDAD = S)
    // =====================================================
    if (apDiscapacidad) {
        ficha.setDisTipo(trimToNull(discapTipo));
        ficha.setDisDescripcion(trimToNull(discapDesc));
        ficha.setDisPorcentaje(discapPorc);
    } else {
        ficha.setDisTipo(null);
        ficha.setDisDescripcion(null);
        ficha.setDisPorcentaje(null);
    }

    // =====================================================
    // Catastrófica / Huérfana / Rara (solo si AP_CATASTROFICA = S)
    // =====================================================
    if (apCatastrofica) {
        ficha.setCatDiagnostico(trimToNull(catasDiagnostico));
        ficha.setCatCalificada(Boolean.TRUE.equals(catasCalificada) ? "S" : "N");
    } else {
        ficha.setCatDiagnostico(null);
        // Si no aplica, evita NULL si tu columna es NOT NULL; si permite null, puedes dejar null
        ficha.setCatCalificada(null);
    }

    // =====================================================
    // Panel N. Retiro (solo si tipo evaluación = RETIRO)
    // ✅ FIX ORA-01407: N_RET_EVAL NO puede ser NULL -> usar "N" por defecto
    // =====================================================
    String tipo = trimToNull(tipoEval);

    if ("RETIRO".equalsIgnoreCase(tipo)) {

        String realiza = trimToNull(nRealizaEvaluacion);
        String relTrab = trimToNull(nRelacionTrabajo);

        ficha.setnRetEval(realiza != null ? realiza : "N");
        ficha.setnRetRelTrab(relTrab != null ? relTrab : "N");
        ficha.setnRetObs(trimToNull(nObsRetiro));

    } else {
        // ⚠️ NO NULL porque la columna N_RET_EVAL es NOT NULL
        ficha.setnRetEval("N");
        ficha.setnRetRelTrab("N");  // recomendado para consistencia
        ficha.setnRetObs(null);
    }

    mapConsumoVidaCondToFicha(ficha);
}

    /**
     * Normaliza entradas tipo "SI/NO", "S/N", "true/false" a "S" o "N".
     */
    private String normalizeSn(String v) {
        String x = trimToNull(v);
        if (x == null) {
            return "N";
        }
        x = x.trim().toUpperCase();
        if ("S".equals(x) || "SI".equals(x) || "1".equals(x) || "TRUE".equals(x)) {
            return "S";
        }
        return "N";
    }

    private SignosVitales upsertVitalSigns(Date now, String user) {
        SignosVitales current = (ficha.getSignos() != null) ? ficha.getSignos() : this.signos;

        try {
            SignosVitales sv = step1VitalSignsManager.upsertVitalSigns(
                    current,
                    paStr,
                    temp,
                    fc,
                    fr,
                    satO2,
                    peso,
                    tallaCm,
                    perimetroAbd,
                    now,
                    user);

            this.signos = sv;
            ficha.setSignos(sv);
            return sv;
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException(ex.getMessage());
        }
    }

    private void saveDraftOccupationalRecord(Date now, String user) {
        ficha.setEstado("BORRADOR");

        if (ficha.getFechaEmision() == null) {
            ficha.setFechaEmision(now);
        }

        stampAuditFieldsForFicha(ficha, now, user);
        asegurarPersonaAuxPersistida();
        ficha = fichaService.guardar(ficha);
    }

    private void stampAuditFieldsForFicha(FichaOcupacional f, Date now, String user) {
        if (f.getIdFicha() == null) {
            f.setFechaCreacion(now);
            f.setUsrCreacion(user);
        } else {
            f.setFechaActualizacion(now);
            f.setUsrActualizacion(user);
        }
    }

    private void auditStep1(SignosVitales sv) {
        registrarAuditoria("GUARDAR_STEP1", "FICHA_OCUPACIONAL", "*",
                "Step 1 guardado. ID_FICHA=" + ficha.getIdFicha());
        registrarAuditoria("GUARDAR_STEP1", "SIGNOS_VITALES", "*",
                "Signos guardados. ID_SIGNOS=" + sv.getIdSignos());
    }

    private boolean validarStep2() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        boolean valido = true;

        if (this == null) {

            throw new NullPointerException("El controlador 'centroMedicoCtrl' es nulo. Recargue la página.");
        }

        if (fichaRiesgo == null || isBlank(fichaRiesgo.getPuestoTrabajo())) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Step 2",
                    "Debe ingresar el puesto de trabajo."));
            valido = false;
        }

        boolean hayActividad = false;
        if (actividadesLab != null) {
            for (String a : actividadesLab) {
                if (!isBlank(a)) {
                    hayActividad = true;
                    break;
                }
            }
        }
        if (!hayActividad) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Step 2",
                    "Debe registrar al menos una actividad laboral."));
            valido = false;
        }

        boolean hayMedida = false;
        if (medidasPreventivas != null) {
            for (String m : medidasPreventivas) {
                if (!isBlank(m)) {
                    hayMedida = true;
                    break;
                }
            }
        }
        if (!hayMedida) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Step 2",
                    "Debe registrar al menos una medida preventiva."));
            valido = false;
        }

        return valido;
    }

    // =====================================================
    // Wizard - Guardado Step 2
    // =====================================================
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
        FacesContext ctx = FacesContext.getCurrentInstance();
        s3("validarStep3() INICIO");

        boolean valido = true;

        boolean hayDiagnostico = false;
        if (listaDiag != null) {
            for (int i = 0; i < listaDiag.size(); i++) {
                ConsultaDiagnostico d = listaDiag.get(i);
                if (d == null) {
                    continue;
                }

                boolean tiene = !isBlank(d.getCodigo()) || !isBlank(d.getDescripcion()) || d.getCie10() != null;
                if (tiene) {
                    hayDiagnostico = true;
                    s3("validarStep3(): diagnóstico encontrado en fila " + (i + 1)
                            + " codigo=" + d.getCodigo() + " cie=" + (d.getCie10() != null));
                    break;
                }
            }
        }
        if (!hayDiagnostico) {
            s3("validarStep3() FAIL: no hay diagnósticos");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe registrar al menos un diagnóstico (CIE10)."));
            valido = false;
        }

        if (isBlank(aptitudSel)) {
            s3("validarStep3() FAIL: aptitudSel vacío");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe seleccionar la aptitud médica."));
            valido = false;
        }

        if (isBlank(recomendaciones)) {
            s3("validarStep3() FAIL: recomendaciones vacío");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe ingresar al menos una recomendación."));
            valido = false;
        }

        if (isBlank(medicoNombre)) {
            s3("validarStep3() FAIL: medicoNombre vacío");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe ingresar el nombre del profesional."));
            valido = false;
        }

        if (isBlank(medicoCodigo)) {
            s3("validarStep3() FAIL: medicoCodigo vacío");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe ingresar el código del médico."));
            valido = false;
        }

        s3("validarStep3() FIN -> " + valido);
        return valido;
    }

    // =====================================================
    // Wizard - Guardado Step 3
    // =====================================================
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

        persistStep3Blocks(now, user);

        registrarAuditoria("GUARDAR_STEP3", "FICHA_OCUPACIONAL / H / I / J / K", "*",
                "Step 3 guardado. ID_FICHA=" + ficha.getIdFicha());
    }

    private void persistStep3Blocks(Date now, String user) {
        FacesContext ctx = FacesContext.getCurrentInstance();

        guardarStep3_FichaGeneral(ctx, now, user);
        guardarStep3_H_ActividadLaboral(now, user);
        guardarStep3_I_Extralaborales(now, user);
        guardarStep3_J_Examenes(now, user);
        guardarStep3_K_Diagnosticos(now, user);
    }

    private void guardarStep3_FichaGeneral(FacesContext ctx, Date ahora, String usuario) {

        LOG.info(String.valueOf("STEP3-A: Guardando datos generales en FICHA_OCUPACIONAL"));

        if (!isBlank(codCie10Ppal)) {
            Cie10 cie = cie10Service.buscarPorCodigo(codCie10Ppal.trim());
            if (cie == null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Validación",
                        "El código CIE10 principal no existe: " + codCie10Ppal
                ));
                ctx.validationFailed();
                throw new IllegalStateException("CIE10 principal no existe: " + codCie10Ppal);
            }
            ficha.setCie10Principal(cie);
        } else {
            ficha.setCie10Principal(null);
        }

        // Enfermedad actual (viene directamente del formulario en ficha.enfermedadProbActual)
        ficha.setEnfermedadProbActual(trimToNull(ficha.getEnfermedadProbActual()));

// Examen físico regional (SOLID: lógica centralizada en servicio)
        examenFisicoRegionalService.aplicarExamenFisicoRegional(ficha);
        // OBS_EXAMEN_FISICO_REG: no sobreescribir con null si la UI está enlazada directo a ficha.obsExamenFisicoReg
        String _obsExf = trimToNull(obsExamenFisico);
        if (_obsExf == null) {
            _obsExf = trimToNull(ficha.getObsExamenFisicoReg());
        }
        ficha.setObsExamenFisicoReg(_obsExf);

        ficha.setAptitudSel(aptitudSel);
        ficha.setDetalleObs(detalleObservaciones);
        ficha.setRecomendaciones(recomendaciones);

        examenFisicoRegionalService.aplicarRetiro(ficha);
        ficha.setnRetObs(nObsRetiro);

        ficha.setMedicoNombre(medicoNombre);
        ficha.setMedicoCodigo(medicoCodigo);

        ficha.setFechaEmision(fechaEmision != null ? fechaEmision : ahora);

        ficha.setFechaActualizacion(ahora);
        ficha.setUsrActualizacion(usuario);
        asegurarPersonaAuxPersistida();
        ficha = fichaService.guardar(ficha);

        LOG.info(String.valueOf("STEP3-A-OK: FICHA_OCUPACIONAL actualizada. ID_FICHA=" + ficha.getIdFicha()));
    }

    private void guardarStep3_H_ActividadLaboral(Date ahora, String usuario) {

        LOG.info(String.valueOf("STEP3-H: Procesando Actividad Laboral (FICHA_ACT_LABORAL)"));

        ensureActLabSize();

        for (int i = 0; i < H_ROWS; i++) {

            int nroFila = i + 1;

            boolean filaTieneDatos
                    = !isBlank(getSafe(actLabCentroTrabajo, i))
                    || !isBlank(getSafe(actLabActividad, i))
                    || !isBlank(getSafe(actLabTiempo, i))
                    || isTrue(getSafe(actLabTrabajoAnterior, i))
                    || isTrue(getSafe(actLabTrabajoActual, i))
                    || isTrue(getSafe(actLabIncidenteChk, i))
                    || isTrue(getSafe(actLabAccidenteChk, i))
                    || isTrue(getSafe(actLabEnfermedadChk, i))
                    || getSafe(iessFecha, i) != null
                    || !isBlank(getSafe(iessEspecificar, i))
                    || !isBlank(getSafe(actLabObservaciones, i));

            if (!filaTieneDatos) {

                fichaActLaboralService.eliminarPorFichaYFila(ficha.getIdFicha(), nroFila);
                continue;
            }

            FichaActLaboral fal = fichaActLaboralService.buscarPorFichaYFila(ficha.getIdFicha(), nroFila);

            if (fal == null) {
                fal = new FichaActLaboral();
                fal.setFicha(ficha);
                fal.setNroFila(nroFila);
                fal.setFCreacion(ahora);
                fal.setUsrCreacion(usuario);
            } else {
                fal.setFActualizacion(ahora);
                fal.setUsrActualizacion(usuario);
            }

            fal.setCentroTrabajo(getSafe(actLabCentroTrabajo, i));
            fal.setActividad(getSafe(actLabActividad, i));
            fal.setTiempo(getSafe(actLabTiempo, i));

            fal.setEsAnterior(sn(getSafe(actLabTrabajoAnterior, i)));
            fal.setEsActual(sn(getSafe(actLabTrabajoActual, i)));
            fal.setIncidente(sn(getSafe(actLabIncidenteChk, i)));
            fal.setAccidente(sn(getSafe(actLabAccidenteChk, i)));
            fal.setEnfOcupacional(sn(getSafe(actLabEnfermedadChk, i)));

            fal.setFechaEvento(toDate(getSafe(iessFecha, i)));

            fal.setEspecificar(getSafe(iessEspecificar, i));
            fal.setObservaciones(getSafe(actLabObservaciones, i));

            fichaActLaboralService.guardar(fal);
        }

        LOG.info(String.valueOf("STEP3-H-OK"));
    }

    private void guardarStep3_I_Extralaborales(Date ahora, String usuario) {

        LOG.info(String.valueOf("STEP3-I: Procesando Actividades Extralaborales (SERIALIZADO EN FICHA)"));

        if (tipoAct == null || fechaAct == null || descAct == null) {
            LOG.info(String.valueOf("STEP3-I: Listas I null -> no se guarda (no rompe)"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        Date ultimaFecha = null;

        for (int i = 0; i < tipoAct.size(); i++) {

            String t = getSafe(tipoAct, i);
            Object rawF = ((java.util.List) fechaAct).get(i);
            Date f = toDate(rawF);
            String d = getSafe(descAct, i);

            boolean filaTieneDatos = !isBlank(t) || f != null || !isBlank(d);
            if (!filaTieneDatos) {
                continue;
            }

            sb.append(i + 1).append(") ")
                    .append(nullToDash(t)).append(" | ")
                    .append(f != null ? new java.text.SimpleDateFormat("yyyy/MM/dd").format(f) : "----/--/--")
                    .append(" | ")
                    .append(nullToDash(d))
                    .append("\n");

            if (f != null) {
                ultimaFecha = f;
            }
        }

        ficha.setExtraLabDesc(sb.length() == 0 ? null : sb.toString().trim());
        ficha.setExtraLabFecha(ultimaFecha);

        ficha.setFechaActualizacion(ahora);
        ficha.setUsrActualizacion(usuario);

        ficha = fichaService.guardar(ficha);

        LOG.info(String.valueOf("STEP3-I-OK"));
    }

    private void guardarStep3_J_Examenes(Date ahora, String usuario) {

        LOG.info(String.valueOf("STEP3-J: Procesando Exámenes (FICHA_EXAMEN_COMP)"));

        if (examNombre == null || examFecha == null || examResultado == null) {
            LOG.info(String.valueOf("STEP3-J: Listas J null -> no se guarda J"));
            return;
        }

        int filas = Math.min(examNombre.size(), Math.min(examFecha.size(), examResultado.size()));

        for (int i = 0; i < filas; i++) {

            int nroFila = i + 1;

            String nombre = getSafe(examNombre, i);
            Object rawFecha = ((java.util.List) examFecha).get(i);
            Date fecha = toDate(rawFecha);
            String resultado = getSafe(examResultado, i);

            boolean filaTieneDatos
                    = !isBlank(nombre)
                    || fecha != null
                    || !isBlank(resultado);

            if (!filaTieneDatos) {

                int del = fichaExamenCompService.eliminarPorFichaYFila(ficha.getIdFicha(), nroFila);
                LOG.info(String.valueOf("STEP3-J-FILA " + nroFila + ": vacía -> delete=" + del));
                continue;
            }

            FichaExamenComp ex = fichaExamenCompService.buscarPorFichaYFila(ficha.getIdFicha(), nroFila);

            if (ex == null) {
                ex = new FichaExamenComp();
                ex.setFicha(ficha);
                ex.setNroFila(nroFila);

                LOG.info(String.valueOf("STEP3-J-FILA " + nroFila + ": INSERT"));
            } else {
                LOG.info(String.valueOf("STEP3-J-FILA " + nroFila + ": UPDATE id=" + ex.getIdFichaExamen()));
            }

            ex.setNombreExamen(nombre);
            ex.setFechaExamen(fecha);
            ex.setResultado(resultado);

            fichaExamenCompService.guardar(ex, usuario);
        }

        LOG.info(String.valueOf("STEP3-J-OK"));
    }

    private void guardarStep3_K_Diagnosticos(Date ahora, String usuario) {

        LOG.info(String.valueOf("STEP3-K: Procesando Diagnósticos"));

        if (listaDiag == null || listaDiag.isEmpty()) {
            LOG.info(String.valueOf("STEP3-K: listaDiag vacía -> OK"));
            return;
        }

        if (fichaDiagnosticoService == null) {
            LOG.info(String.valueOf("STEP3-K: fichaDiagnosticoService null -> no se guarda K"));
            return;
        }

        try {
            fichaDiagnosticoService.guardarDiagnosticosDeFicha(ficha.getIdFicha(), listaDiag, ahora, usuario);
            LOG.info(String.valueOf("STEP3-K-OK (service)"));
        } catch (NoSuchMethodError | RuntimeException ex) {
            LOG.info(String.valueOf("STEP3-K: Tu service no tiene guardarDiagnosticosDeFicha(...) -> no se guarda K"));

        }
    }

    private String nullToDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
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
        StringBuilder sb = new StringBuilder();

        if (ficha == null || ficha.getIdFicha() == null) {
            sb.append("- La ficha ocupacional aún no se ha guardado (Steps 1 y 3).\n");
        }

        if (ficha != null) {

            // 0) Detectar modo auxiliar SIN depender solo de permitirIngresoManual
            //    (si hay personaAux en controller o en ficha, entonces es modo auxiliar)
            boolean modoAux = permitirIngresoManual
                    || (this.personaAux != null)
                    || (ficha.getPersonaAux() != null);

            // 1) Sincronizar paciente
            if (modoAux) {
                if (this.personaAux != null
                        && this.personaAux.getCedula() != null
                        && !this.personaAux.getCedula().trim().isEmpty()) {
                    ficha.setPersonaAux(this.personaAux);
                }
                ficha.setEmpleado(null);
            } else {
                if (this.empleadoSel != null) {
                    ficha.setEmpleado(this.empleadoSel);
                }
                // si estás 100% seguro que no aplica personaAux cuando hay empleado, puedes limpiar:
                // ficha.setPersonaAux(null);
            }

            // 2) Completar aptitud desde variable del controlador si en ficha viene vacío
            if (ficha.getAptitudSel() == null || ficha.getAptitudSel().trim().isEmpty()) {
                if (this.aptitudSel != null && !this.aptitudSel.trim().isEmpty()) {
                    ficha.setAptitudSel(this.aptitudSel);
                }
            }

            // 3) Completar fecha de emisión del certificado
            if (ficha.getFechaEmision() == null) {
                ficha.setFechaEmision(this.fechaEmision != null ? this.fechaEmision : new java.util.Date());
            }

            // 4) Validación empleado/persona auxiliar (sin LAZY)
            boolean tieneEmpleado = (this.empleadoSel != null) || (ficha.getEmpleado() != null);

            boolean tienePersonaAux = false;

            // 4.1) Primero por controller (no proxy)
            if (this.personaAux != null
                    && this.personaAux.getCedula() != null
                    && !this.personaAux.getCedula().trim().isEmpty()) {
                tienePersonaAux = true;
            }

            // 4.2) Fallback por ficha sin disparar proxy
            if (!tienePersonaAux && ficha.getPersonaAux() != null) {
                try {
                    boolean loaded = jakarta.persistence.Persistence.getPersistenceUtil().isLoaded(ficha.getPersonaAux());
                    if (loaded) {
                        String ced = ficha.getPersonaAux().getCedula();
                        tienePersonaAux = (ced != null && !ced.trim().isEmpty());
                    } else {
                        // existe relación pero es proxy lazy: no leer campos
                        tienePersonaAux = true;
                    }
                } catch (RuntimeException ex) {
                    LOG.warn("No se pudo validar PersonaAux.cedula por LAZY/proxy. Se omite lectura.", ex);
                    tienePersonaAux = true;
                }
            }

            // 4.3) Aplicar regla por modo
            if (!tieneEmpleado && !tienePersonaAux) {
                sb.append("- Debe seleccionar un empleado o registrar una persona auxiliar.\n");
            } else if (modoAux) {
                if (!tienePersonaAux) {
                    sb.append("- En modo ingreso manual: falta registrar la persona auxiliar.\n");
                }
            } else {
                if (!tieneEmpleado) {
                    sb.append("- Falta seleccionar el empleado.\n");
                }
            }

            // 5) Validaciones generales
            if (ficha.getFechaEvaluacion() == null) {
                sb.append("- Falta la fecha de evaluación.\n");
            }
            if (ficha.getTipoEvaluacion() == null || ficha.getTipoEvaluacion().trim().isEmpty()) {
                sb.append("- Falta el tipo de evaluación (INGRESO/PERÍODICA/etc.).\n");
            }
            if (ficha.getAptitudSel() == null || ficha.getAptitudSel().trim().isEmpty()) {
                sb.append("- Debe seleccionar la aptitud médica.\n");
            }

            // 6) CIE10 principal
            if (ficha.getCie10Principal() == null
                    || ficha.getCie10Principal().getCodigo() == null
                    || ficha.getCie10Principal().getCodigo().trim().isEmpty()) {

                Cie10 inferido = inferCie10PrincipalFromListaK();
                if (inferido != null && inferido.getCodigo() != null && !inferido.getCodigo().trim().isEmpty()) {
                    ficha.setCie10Principal(inferido);
                }
            }

            if (ficha.getCie10Principal() == null
                    || ficha.getCie10Principal().getCodigo() == null
                    || ficha.getCie10Principal().getCodigo().trim().isEmpty()) {
                sb.append("- Debe registrar un diagnóstico CIE10 principal.\n");
            }

            // 7) Signos vitales
            if (ficha.getSignos() == null) {
                sb.append("- Debe registrar signos vitales (peso/talla) en Step 3.\n");
            }

            // 8) Fecha emisión
            if (ficha.getFechaEmision() == null) {
                sb.append("- Falta la fecha de emisión del certificado.\n");
            }
        }

        if (sb.length() > 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Validación antes de generar el certificado",
                            sb.toString()));
            return false;
        }

        return true;
    }

    // =====================================================
    // PDF - Ficha Ocupacional
    // =====================================================
    private void recargarFichaDesdeBdParaImpresion() {
    if (this.ficha == null || this.ficha.getIdFicha() == null) {
        return;
    }

    FichaOcupacional fresh = fichaService.reloadById(this.ficha.getIdFicha());
    if (fresh != null) {
        this.ficha = fresh;
    }
}
 public void prepararVistaPreviaFicha() {
    FacesContext ctx = FacesContext.getCurrentInstance();

    try {
        if (!verificarFichaParaPdfFicha()) {
            fichaPdfListo = false;
            return;
        }

        // ✅ 0) MUY IMPORTANTE: si estás en ingreso manual, PersonaAux debe estar persistida
        //    (esto evita: TransientObjectException: references an unsaved transient instance of PersonaAux)
        asegurarPersonaAuxPersistida();

        // ✅ 1) Persistir lo último en BD (merge/update)
        ficha = fichaService.actualizar(ficha);

        // ✅ 2) Recargar fresh desde BD (evita estado viejo / lazy)
        recargarFichaDesdeBdParaImpresion();

        // ✅ 3) Construir y renderizar
        String html = construirHtmlFichaDesdePlantilla();
        byte[] bytes = renderizarPdf(html);

        // ✅ 4) Guardar token en sesión (tu lógica actual)
        String token = "FICHA_" + UUID.randomUUID().toString().replace("-", "");
        ExternalContext ec = ctx.getExternalContext();
        HttpSession session = (HttpSession) ec.getSession(true);

        @SuppressWarnings("unchecked")
        Map<String, byte[]> pdfStore = (Map<String, byte[]>) session.getAttribute("PDF_STORE");
        if (pdfStore == null) {
            pdfStore = new HashMap<>();
            session.setAttribute("PDF_STORE", pdfStore);
        }
        pdfStore.put(token, bytes);

        this.pdfTokenFicha = token;
        this.fichaPdfListo = true;

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

    private boolean verificarFichaParaPdfFicha() {
        StringBuilder sb = new StringBuilder();
        debugObjetosAntesDeImprimir();

        if (ficha == null) {
            sb.append("- No existe ficha en memoria.\n");
        } else {

            // 1) Determinar "modo auxiliar" sin depender solo de permitirIngresoManual
            //    Si hay personaAux en controller o en ficha, asumimos ingreso manual.
            boolean modoAux = permitirIngresoManual
                    || (this.personaAux != null)
                    || (ficha.getPersonaAux() != null);

            // 2) Validación de empleado / persona auxiliar
            boolean tieneEmpleado = (empleadoSel != null) || (ficha.getEmpleado() != null);

            boolean tienePersonaAux = false;

            // 2.1) Primero con el objeto del controller (no proxy)
            if (this.personaAux != null
                    && this.personaAux.getCedula() != null
                    && !this.personaAux.getCedula().trim().isEmpty()) {
                tienePersonaAux = true;
            }

            // 2.2) Fallback: si existe relación en ficha, NO tocar getters lazy si no está loaded
            if (!tienePersonaAux && ficha.getPersonaAux() != null) {
                try {
                    boolean loaded = jakarta.persistence.Persistence.getPersistenceUtil().isLoaded(ficha.getPersonaAux());
                    if (loaded) {
                        String ced = ficha.getPersonaAux().getCedula();
                        tienePersonaAux = (ced != null && !ced.trim().isEmpty());
                    } else {
                        // existe relación, pero es proxy lazy -> no leer campos
                        tienePersonaAux = true;
                    }
                } catch (RuntimeException ex) {
                    LOG.warn("No se pudo validar PersonaAux por LAZY/proxy. Se omite lectura para evitar LazyInitializationException.", ex);
                    tienePersonaAux = true;
                }
            }

            // 3) Reglas según modo
            if (!tieneEmpleado && !tienePersonaAux) {
                sb.append("- Debe seleccionar un empleado o registrar una persona auxiliar.\n");
            } else if (modoAux) {
                // En modo auxiliar, NO exijas empleado
                if (!tienePersonaAux) {
                    sb.append("- En modo ingreso manual: falta registrar la persona auxiliar.\n");
                }
            } else {
                // En modo empleado, exiges empleado
                if (!tieneEmpleado) {
                    sb.append("- Falta seleccionar el empleado.\n");
                }
            }

            // 4) Validaciones generales
            if (ficha.getFechaEvaluacion() == null) {
                sb.append("- Falta la fecha de evaluación.\n");
            }
            if (ficha.getTipoEvaluacion() == null || ficha.getTipoEvaluacion().trim().isEmpty()) {
                sb.append("- Falta el tipo de evaluación.\n");
            }
            if (ficha.getSignos() == null) {
                sb.append("- Falta registrar signos vitales (Step 3).\n");
            }
            if (ficha.getIdFicha() == null) {
                sb.append("- La ficha aún no se ha guardado.\n");
            }
        }

        if (sb.length() > 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Validación antes de generar la ficha",
                            sb.toString()));
            return false;
        }
        return true;
    }

    private String construirHtmlFichaDesdeFacelet() {

        String html = renderFaceletToHtml("/pages/ficha/fichaPrint.xhtml");

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
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            throw new IllegalStateException("FacesContext es null. Solo dentro de request JSF.");
        }

        LOG.info("FichaPrint: renderFaceletToHtml START viewId={}", viewId);

        UIViewRoot originalViewRoot = fc.getViewRoot();
        ResponseWriter originalWriter = fc.getResponseWriter();

        PartialViewContext pvc = fc.getPartialViewContext();
        boolean hadPvc = (pvc != null);
        boolean oldRenderAll = false;
        if (hadPvc) {
            try {
                oldRenderAll = pvc.isRenderAll();
                pvc.setRenderAll(true);
            } catch (Exception ignore) {
            }
        }

        try {
            ViewDeclarationLanguage vdl = fc.getApplication()
                    .getViewHandler()
                    .getViewDeclarationLanguage(fc, viewId);

            UIViewRoot tempViewRoot = vdl.createView(fc, viewId);
            tempViewRoot.setLocale(fc.getViewRoot() != null ? fc.getViewRoot().getLocale() : fc.getApplication().getDefaultLocale());
            tempViewRoot.setRenderKitId(fc.getApplication().getViewHandler().calculateRenderKitId(fc));

            vdl.buildView(fc, tempViewRoot);

            StringWriter sw = new StringWriter(128 * 1024);
            ResponseWriter rw = fc.getRenderKit()
                    .createResponseWriter(new PrintWriter(sw), "text/html", "UTF-8");

            fc.setViewRoot(tempViewRoot);
            fc.setResponseWriter(rw);

            fc.getApplication().getViewHandler().renderView(fc, tempViewRoot);
            rw.flush();

            String html = sw.toString();
            LOG.info("FichaPrint: renderFaceletToHtml END viewId={} htmlLen={}", viewId, html != null ? html.length() : -1);
            return html;

        } catch (Exception e) {
            LOG.error("FichaPrint: renderFaceletToHtml ERROR viewId={}", viewId, e);
            throw new RuntimeException("Error renderizando facelet " + viewId, e);

        } finally {
            try {
                fc.setResponseWriter(originalWriter);
            } catch (Exception ignore) {
            }
            try {
                fc.setViewRoot(originalViewRoot);
            } catch (Exception ignore) {
            }
            if (hadPvc) {
                try {
                    pvc.setRenderAll(oldRenderAll);
                } catch (Exception ignore) {
                }
            }
        }
    }

    private String construirHtmlFichaDesdePlantilla() {
        try {
            final String template = cargarRecursoComoString("plantilla_ficha.html");

            // IMPORTANTÍSIMO: antes de construir reemplazos, sincroniza desde objetos
            syncCamposDesdeObjetos();

            Map<String, String> rep = buildReemplazosFicha();
            String html = aplicarReemplazos(template, rep);

            // Si tienes bloques {{#if sexoM}} / {{#if sexoF}} en la plantilla:
            html = aplicarBloquesSexo(html, rep.get("sexo"));

            return html;

        } catch (IOException e) {
            LOG.error("[FICHA] Error cargando plantilla_ficha.html", e);
            // Devuelve HTML mínimo para que veas el error en PDF/preview
            return "<html><body><h3>Error cargando plantilla_ficha.html</h3><pre>"
                    + safe(e.getMessage()) + "</pre></body></html>";
        }
    }

    private String aplicarBloquesSexo(String html, String sexo) {
        String sx = (sexo == null) ? "" : sexo.trim().toUpperCase();

        boolean esM = "M".equals(sx) || "MASCULINO".equals(sx) || "H".equals(sx) || "HOMBRE".equals(sx);
        boolean esF = "F".equals(sx) || "FEMENINO".equals(sx) || "MUJER".equals(sx);

        html = aplicarBloqueCondicional(html, "sexoM", esM);
        html = aplicarBloqueCondicional(html, "sexoF", esF);

        return html;
    }

    private String aplicarBloqueCondicional(String html, String nombre, boolean incluir) {
        String open = "{{#if " + nombre + "}}";
        String close = "{{/if}}";

        int from = 0;
        while (true) {
            int i = html.indexOf(open, from);
            if (i < 0) {
                break;
            }

            int j = html.indexOf(close, i + open.length());
            if (j < 0) {

                html = html.replace(open, "");
                break;
            }

            int end = j + close.length();
            String contenido = html.substring(i + open.length(), j);
            String reemplazo = incluir ? contenido : "";

            html = html.substring(0, i) + reemplazo + html.substring(end);
            from = i + reemplazo.length();
        }
        return html;
    }

    private String aplicarReemplazos(String template, Map<String, String> rep) {
        String html = template;
        for (Map.Entry<String, String> e : rep.entrySet()) {
            html = html.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
        }
        return html;
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

        // 1) Desde FICHA (objeto persistido)
        if (ficha != null) {
            institucion = ficha.getInstSistema();
            ruc = ficha.getRucEstablecimiento();
            centroTrabajo = ficha.getEstablecimientoCt();
            ciiu = ficha.getCiiu();
            noHistoria = ficha.getNoHistoriaClinica();
            noArchivo = ficha.getNoArchivo();
            this.ginecoExamen1 = ficha.getGinecoExamen1();
            this.ginecoTiempo1 = ficha.getGinecoTiempo1();
            this.ginecoResultado1 = ficha.getGinecoResultado1();
            this.ginecoExamen2 = ficha.getGinecoExamen2();
            this.ginecoTiempo2 = ficha.getGinecoTiempo2();
            this.ginecoResultado2 = ficha.getGinecoResultado2();
            this.ginecoObservacion = ficha.getGinecoObservacion();

            // Enfermedad actual
            this.enfermedadActual = ficha.getEnfermedadProbActual();

// Examen físico
            this.exfPielCicatrices = ficha.getExfPielCicatrices();
            this.exfOjosParpados = ficha.getExfOjosParpados();
            this.exfOjosConjuntivas = ficha.getExfOjosConjuntivas();
            this.exfOjosPupilas = ficha.getExfOjosPupilas();
            this.exfOjosCornea = ficha.getExfOjosCornea();
            this.exfOjosMotilidad = ficha.getExfOjosMotilidad();
            this.exfOidoConducto = ficha.getExfOidoConducto();
            this.exfOidoPabellon = ficha.getExfOidoPabellon();
            this.exfOidoTimpanos = ficha.getExfOidoTimpanos();
            this.exfOroLabios = ficha.getExfOroLabios();
            this.exfOroLengua = ficha.getExfOroLengua();
            this.exfOroFaringe = ficha.getExfOroFaringe();
            this.exfOroAmigdalas = ficha.getExfOroAmigdalas();
            this.exfOroDentadura = ficha.getExfOroDentadura();
            this.exfNarizTabique = ficha.getExfNarizTabique();
            this.exfNarizCornetes = ficha.getExfNarizCornetes();
            this.exfNarizMucosas = ficha.getExfNarizMucosas();
            this.exfNarizSenos = ficha.getExfNarizSenosParanasa(); // Ajuste
            this.exfCuelloTiroides = ficha.getExfCuelloTiroidesMasas(); // Ajuste
            this.exfCuelloMovilidad = ficha.getExfCuelloMovilidad();
            this.exfToraxMamas = ficha.getExfToraxMamas();
            this.exfToraxPulmones = ficha.getExfToraxPulmones();
            this.exfToraxCorazon = ficha.getExfToraxCorazon();
            this.exfToraxParrilla = ficha.getExfToraxParrillaCostal(); // Ajuste
            this.exfAbdomenVisceras = ficha.getExfAbdVisceras(); // Ajuste
            this.exfAbdomenPared = ficha.getExfAbdParedAbdominal(); // Ajuste
            this.exfColumnaFlexibilidad = ficha.getExfColFlexibilidad(); // Ajuste
            this.exfColumnaDesviacion = ficha.getExfColDesviacion(); // Ajuste
            this.exfColumnaDolor = ficha.getExfColDolor(); // Ajuste
            this.exfPelvisPelvis = ficha.getExfPelvisPelvis();
            this.exfPelvisGenitales = ficha.getExfPelvisGenitales();
            this.exfExtVascular = ficha.getExfExtVascular();
            this.exfExtSup = ficha.getExfExtMiembrosSup(); // Ajuste
            this.exfExtInf = ficha.getExfExtMiembrosInf(); // Ajuste
            this.exfNeuroFuerza = ficha.getExfNeuroFuerza();
            this.exfNeuroSensibilidad = ficha.getExfNeuroSensibilidad();
            this.exfNeuroMarcha = ficha.getExfNeuroMarcha();
            this.exfNeuroReflejos = ficha.getExfNeuroReflejos();
            this.obsExamenFisico = ficha.getObsExamenFisicoRegional(); // o getObsExamenFisicoReg() según tu preferencia

            // Si estos valores están en ficha, úsalo; si no, se llenarán desde empleadoSel
            // grupoSanguineo = ficha.getGrupoSanguineo();   (si existe)
            // lateralidad    = ficha.getLateralidad();      (si existe)
        }

        // 2) Desde EMPLEADO (objeto seleccionado)
        if (empleadoSel != null) {
            apellido1 = empleadoSel.getPriApellido();
            apellido2 = empleadoSel.getSegApellido();

            // Si "getNombres()" trae "KLEBER DAVID", separa:
            String nombres = empleadoSel.getNombres();
            if (nombres != null) {
                String[] parts = nombres.trim().split("\\s+", 2);
                nombre1 = parts.length > 0 ? parts[0] : "";
                nombre2 = parts.length > 1 ? parts[1] : "";
            }

            // Ajusta estos getters a los REALES de tu entidad empleadoSel:
            // sexo = empleadoSel.getSexo();                 // "M" o "F"
            // fechaNacimiento = empleadoSel.getFechaNacimiento(); // Date
            // grupoSanguineo = empleadoSel.getGrupoSanguineo();
            // lateralidad = empleadoSel.getLateralidad();
            // Si no sabes el nombre exacto, imprime en LOG:
            // LOG.info("empleadoSel class = {}", empleadoSel.getClass());
        }

        // 3) Edad si tienes fechaNacimiento
        if (fechaNacimiento != null) {
            java.time.LocalDate fn = fechaNacimiento.toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            edad = java.time.Period.between(fn, java.time.LocalDate.now()).getYears();
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
        cargarDatosCabecera(rep);
        cargarDatosPersonales(rep);
        cargarAtencionPrioritaria(rep);
        cargarFechaActual(rep);
        cargarActividadLaboralArrays(rep);
        cargarAntecedentes(rep);
        cargarRiesgos(rep);
        cargarConsumo(rep);
        recalcularIMC();
        cargarCamposDirectosFaltantes(rep);
        // ✅ FALTABA: D. Enfermedad actual
        rep.put("enfermedad_actual", safe(ficha != null ? ficha.getEnfermedadProbActual() : enfermedadActual));

        // ✅ FALTABA: F. Examen físico regional (todos los exf_*)
        cargarExamenFisicoRegional(rep);  // (creas este método abajo)

        // ✅ FALTABA: “Otros (Físico/Seguridad/...)”
        corregirOtrosRiesgos(rep);        // si ya existe en tu ctrl, LLÁMALO AQUÍ

        // ✅ (si tu plantilla lo usa en ficha)
        // === LOG DE VALORES EN EL MAPA (solo para depuración) ===
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

// ========================= Métodos auxiliares =========================
    private void cargarExamenFisicoRegional(Map<String, String> rep) {
        rep.put("exf_piel_cicatrices", markX(exfPielCicatrices));
        rep.put("exf_ojos_parpados", markX(exfOjosParpados));
        rep.put("exf_ojos_conjuntivas", markX(exfOjosConjuntivas));
        rep.put("exf_ojos_pupilas", markX(exfOjosPupilas));
        rep.put("exf_ojos_cornea", markX(exfOjosCornea));
        rep.put("exf_ojos_motilidad", markX(exfOjosMotilidad));
        rep.put("exf_oido_conducto", markX(exfOidoConducto));
        rep.put("exf_oido_pabellon", markX(exfOidoPabellon));
        rep.put("exf_oido_timpanos", markX(exfOidoTimpanos));
        rep.put("exf_oro_labios", markX(exfOroLabios));
        rep.put("exf_oro_lengua", markX(exfOroLengua));
        rep.put("exf_oro_faringe", markX(exfOroFaringe));
        rep.put("exf_oro_amigdalas", markX(exfOroAmigdalas));
        rep.put("exf_oro_dentadura", markX(exfOroDentadura));
        rep.put("exf_nariz_tabique", markX(exfNarizTabique));
        rep.put("exf_nariz_cornetes", markX(exfNarizCornetes));
        rep.put("exf_nariz_mucosas", markX(exfNarizMucosas));
        rep.put("exf_nariz_senos", markX(exfNarizSenos));
        rep.put("exf_cuello_tiroides", markX(exfCuelloTiroides));
        rep.put("exf_cuello_movilidad", markX(exfCuelloMovilidad));
        rep.put("exf_torax_mamas", markX(exfToraxMamas));
        rep.put("exf_torax_pulmones", markX(exfToraxPulmones));
        rep.put("exf_torax_corazon", markX(exfToraxCorazon));
        rep.put("exf_torax_parrilla", markX(exfToraxParrilla));
        rep.put("exf_abdomen_visceras", markX(exfAbdomenVisceras));
        rep.put("exf_abdomen_pared", markX(exfAbdomenPared));
        rep.put("exf_columna_flexibilidad", markX(exfColumnaFlexibilidad));
        rep.put("exf_columna_desviacion", markX(exfColumnaDesviacion));
        rep.put("exf_columna_dolor", markX(exfColumnaDolor));
        rep.put("exf_pelvis_pelvis", markX(exfPelvisPelvis));
        rep.put("exf_pelvis_genitales", markX(exfPelvisGenitales));
        rep.put("exf_ext_vascular", markX(exfExtVascular));
        rep.put("exf_ext_sup", markX(exfExtSup));
        rep.put("exf_ext_inf", markX(exfExtInf));
        rep.put("exf_neuro_fuerza", markX(exfNeuroFuerza));
        rep.put("exf_neuro_sensibilidad", markX(exfNeuroSensibilidad));
        rep.put("exf_neuro_marcha", markX(exfNeuroMarcha));
        rep.put("exf_neuro_reflejos", markX(exfNeuroReflejos));
        rep.put("obs_examen_fisico", safe(trimToNull(obsExamenFisico) != null ? trimToNull(obsExamenFisico) : (ficha != null ? trimToNull(ficha.getObsExamenFisicoReg()) : null)));
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

    private void cargarDatosCabecera(Map<String, String> rep) {
        rep.put("institucion", safe(institucion));
        rep.put("ruc", safe(ruc));
        rep.put("centroTrabajo", safe(centroTrabajo));
        rep.put("ciiu", safe(ciiu));
        rep.put("noHistoria", safe(noHistoria));
        rep.put("noArchivo", safe(noArchivo));
        rep.put("num_formulario", safe(noHistoria));
        rep.put("no_historia_clinica", safe(noHistoria));
        rep.put("no_archivo", safe(noArchivo));
    }

    private void cargarDatosPersonales(Map<String, String> rep) {
        rep.put("apellido1", safe(apellido1));
        rep.put("apellido2", safe(apellido2));
        rep.put("nombre1", safe(nombre1));
        rep.put("nombre2", safe(nombre2));
        rep.put("sexo", safe(sexo));
        rep.put("fechaNacimiento", fmtDate(fechaNacimiento));
        rep.put("edad", (edad == null) ? "" : String.valueOf(edad));
        rep.put("grupoSanguineo", safe(grupoSanguineo));
        rep.put("lateralidad", safe(lateralidad));
        rep.put("cedula", safe(resolveCedulaForPdf()));
        rep.put("email", "");
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

    private void cargarConsumo(Map<String, String> rep) {
        putBoolArray0Based(rep, "cons_ex_consumidor_", consExConsumidor);
        putBoolArray0Based(rep, "cons_no_consume_", consNoConsume);
        putIntArray0Based(rep, "cons_tiempo_consumo_", consTiempoConsumoMeses);
        putIntArray0Based(rep, "cons_tiempo_abstinencia_", consTiempoAbstinenciaMeses);
        putStringArray0Based(rep, "af_cual_", afCual);
        putStringArray0Based(rep, "af_tiempo_", afTiempo);
        putStringArray0Based(rep, "med_cual_", medCual);
        putIntArray0Based(rep, "med_cant_", medCant);
        rep.put("consumo_observacion", safe(trimToNull(consumoVidaCondObs) != null ? trimToNull(consumoVidaCondObs) : (ficha != null ? trimToNull(ficha.getObsConsumoVidaCond()) : null)));
        rep.put("consumo_vida_cond_obs", safe(consumoVidaCondObs));
        rep.put("cons_otras_cual", safe(consOtrasCual));
    }

    private void cargarCamposDirectosFaltantes(Map<String, String> rep) {

        rep.put("recomendaciones", safe(recomendaciones));
        rep.put("fechaAtencion", fmtDate(fechaAtencion));
        rep.put("fecIngreso", fmtDate(fecIngreso));
        rep.put("fecReintegro", fmtDate(fecReintegro));
        rep.put("fecRetiro", fmtDate(fecRetiro));
        rep.put("motivoObs", safe(motivoObs));
        rep.put("condicionEspecial", safe(condicionEspecial));
        rep.put("autorizaTransfusion", safe(autorizaTransfusion));
        rep.put("tratamientoHormonal", safe(tratamientoHormonal));
        rep.put("tratamientoHormonalCual", safe(tratamientoHormonalCual));
        rep.put("examenReproMasculino", safe(examenReproMasculino));
        rep.put("tiempoReproMasculino", (tiempoReproMasculino == null) ? "" : String.valueOf(tiempoReproMasculino));
        rep.put("fum", fmtDate(fum));
        rep.put("gestas", (gestas == null) ? "" : String.valueOf(gestas));
        rep.put("cesareas", (cesareas == null) ? "" : String.valueOf(cesareas));
        rep.put("partos", (partos == null) ? "" : String.valueOf(partos));
        rep.put("abortos", (abortos == null) ? "" : String.valueOf(abortos));
        rep.put("planificacion", safe(planificacion));
        rep.put("planificacionCual", safe(planificacionCual));
        rep.put("temp", (temp == null) ? "" : String.valueOf(temp));
        rep.put("paStr", safe(paStr));
        rep.put("fc", (fc == null) ? "" : String.valueOf(fc));
        rep.put("fr", (fr == null) ? "" : String.valueOf(fr));
        rep.put("satO2", (satO2 == null) ? "" : String.valueOf(satO2));
        rep.put("peso", (peso == null) ? "" : String.valueOf(peso));
        rep.put("tallaCm", (tallaCm == null) ? "" : String.valueOf(tallaCm));
        rep.put("imc", (imc == null) ? "" : String.valueOf(imc));
        rep.put("perimetroAbd", (perimetroAbd == null) ? "" : String.valueOf(perimetroAbd));
        rep.put("gineco_examen1", safe(ginecoExamen1));
        rep.put("gineco_tiempo1", safe(ginecoTiempo1));
        rep.put("gineco_resultado1", safe(ginecoResultado1));
        rep.put("gineco_examen2", safe(ginecoExamen2));
        rep.put("gineco_tiempo2", safe(ginecoTiempo2));
        rep.put("gineco_resultado2", safe(ginecoResultado2));
        rep.put("gineco_observacion", safe(ginecoObservacion));
        if (fichaRiesgo != null) {
            rep.put("puestoTrabajo", safe(fichaRiesgo.getPuestoTrabajo()));
            // Garantiza que nunca queden {{otros_*}} en el HTML aunque no hayan datos
            for (int i = 1; i <= 7; i++) {
                // En UI a veces las llaves vienen con convenciones distintas; cubrimos variantes
                rep.putIfAbsent("otros_fis_" + i, safe(firstNonEmpty(
                        otrosRiesgos.get("otros_fis_" + i),
                        otrosRiesgos.get("FIS_OTROS_" + i),
                        otrosRiesgos.get("fis_otros_" + i),
                        otrosRiesgos.get("otrosFis" + i),
                        otrosRiesgos.get("otrosFis_" + i)
                )));
                rep.putIfAbsent("otros_seg_" + i, safe(firstNonEmpty(
                        otrosRiesgos.get("otros_seg_" + i),
                        otrosRiesgos.get("SEG_OTROS_" + i),
                        otrosRiesgos.get("seg_otros_" + i),
                        otrosRiesgos.get("otrosSeg" + i),
                        otrosRiesgos.get("otrosSeg_" + i)
                )));
                rep.putIfAbsent("otros_qui_" + i, safe(firstNonEmpty(
                        otrosRiesgos.get("otros_qui_" + i),
                        otrosRiesgos.get("QUI_OTROS_" + i),
                        otrosRiesgos.get("qui_otros_" + i),
                        otrosRiesgos.get("otrosQui" + i),
                        otrosRiesgos.get("otrosQui_" + i)
                )));
                rep.putIfAbsent("otros_bio_" + i, safe(firstNonEmpty(
                        otrosRiesgos.get("otros_bio_" + i),
                        otrosRiesgos.get("BIO_OTROS_" + i),
                        otrosRiesgos.get("bio_otros_" + i),
                        otrosRiesgos.get("otrosBio" + i),
                        otrosRiesgos.get("otrosBio_" + i)
                )));
                rep.putIfAbsent("otros_erg_" + i, safe(firstNonEmpty(
                        otrosRiesgos.get("otros_erg_" + i),
                        otrosRiesgos.get("ERG_OTROS_" + i),
                        otrosRiesgos.get("erg_otros_" + i),
                        otrosRiesgos.get("otrosErg" + i),
                        otrosRiesgos.get("otrosErg_" + i)
                )));
                rep.putIfAbsent("otros_psi_" + i, safe(firstNonEmpty(
                        otrosRiesgos.get("otros_psi_" + i),
                        otrosRiesgos.get("PSI_OTROS_" + i),
                        otrosRiesgos.get("psi_otros_" + i),
                        otrosRiesgos.get("otrosPsi" + i),
                        otrosRiesgos.get("otrosPsi_" + i)
                )));
            }

        } else {
            rep.put("puestoTrabajo", "");
        }
        rep.put("tipoEval", safe(tipoEval));

        // D. ENFERMEDAD O PROBLEMA ACTUAL (texto libre)
        rep.put("enfermedad_actual", safe((ficha != null) ? ficha.getEnfermedadProbActual() : null));
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
     * cons_tiempo_*_0..N desde Integer[]
     */
    private void putIntArray0Based(Map<String, String> rep, String prefix, Integer[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + i, (arr[i] == null) ? "" : String.valueOf(arr[i]));
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

    /**
     * Cédula para PDF: empleadoSel -> personaAux -> cedulaBusqueda
     */
    private String resolveCedulaForPdf() {
        try {
            if (empleadoSel != null && empleadoSel.getNoCedula() != null && !empleadoSel.getNoCedula().isBlank()) {
                return empleadoSel.getNoCedula();
            }
        } catch (RuntimeException ignore) {
            // no-op
        }
        try {
            if (personaAux != null && personaAux.getCedula() != null && !personaAux.getCedula().isBlank()) {
                return personaAux.getCedula();
            }
        } catch (RuntimeException ignore) {
            // no-op
        }
        return (cedulaBusqueda == null) ? "" : cedulaBusqueda;
    }

    /**
     * cons_ex_consumidor_0..N desde Boolean[] -> "X"
     */
    private void putBoolArray0Based(Map<String, String> rep, String prefix, Boolean[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + i, Boolean.TRUE.equals(arr[i]) ? "X" : "");
        }
    }

    /**
     * cons_tiempo_consumo_0..N desde String[]
     */
    private void putStringArray0Based(Map<String, String> rep, String prefix, String[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + i, safe(arr[i]));
        }
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
        // 1) condicionales tipo {{#if key}} ... {{/if}}
        template = applyIfBlocks(template, rep);

        // 2) reemplazo simple {{token}}
        for (Map.Entry<String, String> e : rep.entrySet()) {
            String k = e.getKey();
            String v = e.getValue() == null ? "" : e.getValue();
            template = template.replace("{{" + k + "}}", v);
        }
        // 3) limpia tokens no resueltos
        template = template.replaceAll("\\{\\{[^}]+\\}\\}", "");
        return template;
    }

    private String applyIfBlocks(String template, Map<String, String> rep) {
        // Soporta: {{#if sexoM}} ... {{/if}}
        // Si rep.get("sexoM") es "true"/"1"/"X" => deja el bloque, caso contrario lo elimina
        final java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\\{\\{#if\\s+([a-zA-Z0-9_]+)\\}\\}([\\s\\S]*?)\\{\\{\\/if\\}\\}",
                java.util.regex.Pattern.MULTILINE
        );

        java.util.regex.Matcher m = p.matcher(template);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String body = m.group(2);

            String val = rep.getOrDefault(key, "");
            boolean on = "true".equalsIgnoreCase(val) || "1".equals(val) || "X".equalsIgnoreCase(val) || "SI".equalsIgnoreCase(val);

            m.appendReplacement(out, java.util.regex.Matcher.quoteReplacement(on ? body : ""));
        }
        m.appendTail(out);
        return out.toString();
    }

    private String construirHtmlFichaDesdePrintFacelets() {

        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.getSessionMap().put("centroMedicoPrint", this);
        } catch (Exception e) {
            LOG.warn("No se pudo colocar centroMedicoPrint en sesión.", e);
        }

        String html = renderFaceletToHtml("/pages/ficha/fichaPrint.xhtml");
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
            generatePdfPreview(ctx);
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

    private void generatePdfPreview(FacesContext ctx) {
        try {
            String html = construirHtmlDesdePlantilla();
            byte[] bytes = renderizarPdf(html);

            String token = "CERT_" + UUID.randomUUID().toString().replace("-", "");
            storePdfBytesInPdfStore(ctx, token, bytes);

            this.pdfTokenCertificado = token;
            this.pdfObjectUrl = null;
        } catch (Exception e) {
            handleUnexpected("generatePdfPreview", e);
        }
    }

    private void storePdfBytesInPdfStore(FacesContext ctx, String token, byte[] bytes) {
        if (ctx == null || token == null || bytes == null) {
            return;
        }
        ExternalContext ec = ctx.getExternalContext();
        HttpSession session = (HttpSession) ec.getSession(true);

        @SuppressWarnings("unchecked")
        Map<String, byte[]> pdfStore = (Map<String, byte[]>) session.getAttribute("PDF_STORE");
        if (pdfStore == null) {
            pdfStore = new HashMap<>();
            session.setAttribute("PDF_STORE", pdfStore);
        }
        pdfStore.put(token, bytes);
    }

    private void cleanupPdfPreview(FacesContext ctx) {
        if (ctx == null) {
            pdfTokenCertificado = null;
            pdfObjectUrl = null;
            return;
        }
        ExternalContext ec = ctx.getExternalContext();
        HttpSession session = (HttpSession) ec.getSession(false);
        if (session != null && pdfTokenCertificado != null) {
            @SuppressWarnings("unchecked")
            Map<String, byte[]> pdfStore = (Map<String, byte[]>) session.getAttribute("PDF_STORE");
            if (pdfStore != null) {
                pdfStore.remove(pdfTokenCertificado);
            }
        }
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

    // =====================================================
    // PDF - Certificado Médico
    // =====================================================
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
            generatePdfPreview(ctx);

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

    private byte[] renderizarPdf(String xhtml) throws DocumentException, IOException {

        if (xhtml == null || xhtml.trim().isEmpty()) {
            LOG.error("renderizarPdf: El string HTML recibido es NULO o VACÍO.");
            throw new IllegalArgumentException(
                    "El contenido HTML para generar el PDF está vacío. "
                    + "Esto generalmente significa que el método renderFaceletToHtml falló. "
                    + "Revise el LOG del servidor buscando 'FichaPrint: renderFaceletToHtml ERROR' para ver el error real."
            );
        }

        final String xhtmlOk = sanitizeXhtmlForPdf(xhtml);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        String baseURL = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResource("/")
                .toExternalForm();

        try {
            String fontsBase = FacesContext.getCurrentInstance()
                    .getExternalContext().getRealPath("/resources/fonts/");
            if (fontsBase != null) {
                renderer.getFontResolver().addFont(
                        fontsBase + File.separator + "DejaVuSans.ttf",
                        BaseFont.IDENTITY_H, true
                );
            }
        } catch (DocumentException | IOException e) {
            LOG.debug("Skipping optional font registration for PDF rendering.", e);
        }

        try {
            renderer.setDocumentFromString(xhtmlOk, baseURL);
        } catch (RuntimeException ex) {
            dumpXhtmlDebug("fichaPrint_debug.xhtml", xhtmlOk, ex);
            throw ex;
        }

        renderer.layout();
        renderer.createPDF(baos);
        renderer.finishPDF();

        return baos.toByteArray();
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

            String q = query.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
            if (q.isEmpty()) {
                LOG.info("<<< [AC-K-COD] q empty after normalize => return empty");
                return new ArrayList<>();
            }

            List<String> out = new ArrayList<>(); 

            List<Cie10> lista = cie10Service.buscarJerarquiaPorTerm(q);
            LOG.debug("... [AC-K-COD] service.buscarJerarquiaPorTerm(q={}) size={}", q,
                    (lista == null ? "null" : lista.size()));

            agregarCoincidenciasCodigo(out, lista, q);

            // Fallback: hay catálogos con códigos no normalizados (espacios/puntos).
            // Si no hubo resultados por prefijo, hacemos búsqueda tolerante por código.
            if (out.isEmpty()) {
                List<Cie10> alterna = cie10Service.buscarPorCodigoAproximado(q, 20);
                LOG.debug("... [AC-K-COD] fallback buscarPorCodigoAproximado(q={}) size={}", q,
                        (alterna == null ? "null" : alterna.size()));
                agregarCoincidenciasCodigo(out, alterna, q);
            }

            // Último fallback: búsqueda general por término (código/descr.).
            if (out.isEmpty()) {
                List<Cie10> general = cie10Service.buscarPorTermino(q, 20);
                LOG.debug("... [AC-K-COD] fallback buscarPorTermino(q={}) size={}", q,
                        (general == null ? "null" : general.size()));
                agregarCoincidenciasCodigo(out, general, q);
            }

            LOG.info("<<< [AC-K-COD] RETURN out.size=" + out.size()
                    + (out.isEmpty() ? "" : (" first=[" + out.get(0) + "]")));
            return out;

        } catch (Exception e) {
            LOG.error("!!! [AC-K-COD] ERROR {} : {}", e.getClass().getName(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private void agregarCoincidenciasCodigo(List<String> out, List<Cie10> lista, String q) {
        if (lista == null || out == null || q == null) {
            return;
        }

        for (Cie10 c : lista) {
            if (c == null || c.getCodigo() == null) {
                continue;
            }

            String codigo = c.getCodigo().trim();
            if (codigo.isEmpty()) {
                continue;
            }

            String codNorm = codigo.toUpperCase().replaceAll("[^A-Z0-9]", "");
            if (!codNorm.contains(q)) {
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

    // =====================================================
    // Persona Auxiliar - Registro y Selección
    // =====================================================
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

    // =====================================================
    // Búsqueda de Cédula / DiáLOGo Inicial
    // =====================================================
    // =====================================================
    // Búsqueda de Cédula / Diálogo Inicial
    // =====================================================
    public void buscarCedula() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        PrimeFaces pf = PrimeFaces.current();

        try {
            final CedulaSearchOutcome outcome = searchCedulaAndPrepareUi();

            if (outcome.isEncontrado()) {
                tryLoadCargoFromVista(ctx);
            } else {
                // si no se encontró, limpia el campo en la ficha (para que no se quede “pegado”)
                if (this.ficha != null) {
                    this.ficha.setCiiu(null); // o "" si prefieres vacío
                }
            }

            if (outcome.isEncontrado() || outcome.isMostrarManual()) {
                mostrarDlgCedula = false;
            }

            pushCedulaDiaLOGCallbackParams(pf, outcome);

            updateCedulaDiaLOG(pf);

            // ✅ refresca mensajes + wizard (y el campo cargo si lo tienes en step1)
            pf.ajax().update(":msgs", "@([id$=wdzFicha])");

        } catch (BusinessValidationException ex) {
            addCedulaDiaLOGMessage(ctx, FacesMessage.SEVERITY_WARN, "Búsqueda", ex.getMessage());

            pushCedulaDiaLOGCallbackParams(pf, CedulaSearchOutcome.notFoundNoManual());

            updateCedulaDiaLOG(pf);
            pf.ajax().update(":msgs");

        } catch (RuntimeException ex) {
            handleUnexpected("buscarCedula", ex);
            addCedulaDiaLOGMessage(ctx, FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al buscar la cédula.");

            pushCedulaDiaLOGCallbackParams(pf, CedulaSearchOutcome.notFoundNoManual());

            updateCedulaDiaLOG(pf);
            pf.ajax().update(":msgs");
        }
    }

private void tryLoadCargoFromVista(FacesContext ctx) {

    // Usa la misma cédula que ya tienes en tu flujo
    String ced = SnUtils.trimToNull(this.cedulaBusqueda); // ajusta si tu variable se llama distinto
    if (ced == null) {
        if (this.ficha != null) {
            this.ficha.setCiiu(null);
        }
        return;
    }

    EmpleadoCargoDTO dto = empleadoRhService.buscarPorCedulaEnVista(ced);

    // Si no hay cargo vigente -> deja en blanco y muestra mensaje
    if (dto == null || SnUtils.trimToNull(dto.getCargoDescrip()) == null) {
        if (this.ficha != null) {
            this.ficha.setCiiu(null); // o "" si prefieres
        }

        addCedulaDiaLOGMessage(ctx,
            FacesMessage.SEVERITY_WARN,
            "Cargo",
            "El empleado no registra cargo vigente en RRHH.");
        return;
    }

    // ✅ AQUÍ está el punto: se setea el campo que está en la pantalla
    if (this.ficha != null) {
        this.ficha.setCiiu(dto.getCargoDescrip()); // <-- se refleja en Puesto de Trabajo CIUO
    }
}
    private static final String CEDULA_MSG_CLIENT_ID = "dlgCedulaForm:cedulaBusqueda";

    private static class CedulaSearchOutcome {

        final boolean found;
        final boolean showManual;

        CedulaSearchOutcome(boolean found, boolean showManual) {
            this.found = found;
            this.showManual = showManual;
        }

        boolean isEncontrado() {
            return found;
        }

        boolean isMostrarManual() {
            return showManual;
        }

        static CedulaSearchOutcome found() {
            return new CedulaSearchOutcome(true, false);
        }

        static CedulaSearchOutcome notFoundManual() {
            return new CedulaSearchOutcome(false, true);
        }

        static CedulaSearchOutcome notFoundNoManual() {
            return new CedulaSearchOutcome(false, false);
        }
    }

    private CedulaSearchOutcome searchCedulaAndPrepareUi() {
        PrimeFaces pf = PrimeFaces.current();

        permitirIngresoManual = false;

        final String cedula = normalizeCedulaOrThrow();
        ensureWizardStateForSearch(cedula);

        DatEmpleado emp = empleadoService.buscarPorCedula(cedula);

        if (emp != null) {
            loadEmployeeFromRrhh(emp);
            showPersonaAuxDiaLOG(false);
            addCedulaDiaLOGInfoMessage();
            pf.ajax().update(":wdzFicha", ":msgs");
            updateCedulaDiaLOG(pf);
            return CedulaSearchOutcome.found();
        }

        prepareManualEntry(cedula);
        showPersonaAuxDiaLOG(true);
        addCedulaDiaLOGWarnMessage();
        pf.ajax().update(":wdzFicha", ":msgs",
                ":dlgPersonaAuxForm:cedManual", ":dlgPersonaAuxForm:gridManual", ":dlgPersonaAuxForm:msgPersonaAux");
        updateCedulaDiaLOG(pf);
        return CedulaSearchOutcome.notFoundManual();
    }

    private String normalizeCedulaOrThrow() {
        if (cedulaBusqueda == null || cedulaBusqueda.trim().isEmpty()) {
            throw new BusinessValidationException("Ingrese una cédula para realizar la búsqueda.");
        }
        return cedulaBusqueda.trim();
    }

    private void ensureWizardStateForSearch(String cedula) {
        if (ficha == null) {
            ficha = new FichaOcupacional();
        }
        if (personaAux == null) {
            personaAux = new PersonaAux();
        }

        personaAux.setCedula(cedula);
    }

    private void loadEmployeeFromRrhh(DatEmpleado emp) {
        empleadoSel = emp;
        noPersonaSel = emp.getNoPersona();

        apellido1 = emp.getPriApellido();
        apellido2 = emp.getSegApellido();
        nombre1 = emp.getNombres();
        nombre2 = null;

        sexo = (emp.getSexo() != null) ? emp.getSexo().getCodigo() : null;
        fechaNacimiento = emp.getfNacimiento();
        edad = calcularEdad(fechaNacimiento);

        ficha.setNoHistoriaClinica(emp.getNoCedula());
        ficha.setNoArchivo(emp.getNoCedula());
        ficha.setEmpleado(emp);
        ficha.setPersonaAux(null);

        permitirIngresoManual = false;
        mostrarDlgCedula = false;
    }

    private void prepareManualEntry(String cedula) {
        empleadoSel = null;
        noPersonaSel = null;

        personaAux.setApellido1(null);
        personaAux.setApellido2(null);
        personaAux.setNombre1(null);
        personaAux.setNombre2(null);
        personaAux.setSexo(null);
        personaAux.setFechaNac(null);

        ficha.setNoHistoriaClinica(cedula);
        ficha.setNoArchivo(cedula);

        permitirIngresoManual = true;
        mostrarDlgCedula = true;
    }

    private void showPersonaAuxDiaLOG(boolean show) {

    }

    private void addCedulaDiaLOGInfoMessage() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        addCedulaDiaLOGMessage(ctx, FacesMessage.SEVERITY_INFO, "Búsqueda", "Información cargada desde RRHH.");
    }

    private void addCedulaDiaLOGWarnMessage() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        addCedulaDiaLOGMessage(ctx, FacesMessage.SEVERITY_WARN, "Búsqueda",
                "No se encontró la cédula. Puede ingresar los datos manualmente.");
    }

    private void addCedulaDiaLOGMessage(FacesContext ctx, FacesMessage.Severity sev, String summary, String detail) {
        if (ctx != null) {
            ctx.addMessage(CEDULA_MSG_CLIENT_ID, new FacesMessage(sev, summary, detail));
        }
    }

    private void updateCedulaDiaLOG(PrimeFaces pf) {
        pf.ajax().update(":dlgCedulaForm:msgCedula", ":dlgCedulaForm:panelBtnManualWrap");
    }

    private void pushCedulaDiaLOGCallbackParams(PrimeFaces pf, CedulaSearchOutcome outcome) {
        pf.ajax().addCallbackParam("encontrado", outcome.found);
        pf.ajax().addCallbackParam("mostrarManual", outcome.showManual);
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

        return STATIC_RISK_COLS;
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

    public SignosVitalesService getSignosService() {
        return signosService;
    }

    public void setSignosService(SignosVitalesService signosService) {
        this.signosService = signosService;
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

    public AuditoriaConsultorioService getAuditoriaService() {
        return auditoriaService;
    }

    public void setAuditoriaService(AuditoriaConsultorioService auditoriaService) {
        this.auditoriaService = auditoriaService;
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

        // --- Marca SI/NO (lo que ya tenías) ---
        rep.put("apCatastrofica_sn", apCatastrofica ? "SI" : "NO");
        rep.put("apDiscapacidad_sn", apDiscapacidad ? "SI" : "NO");
        rep.put("apEmbarazada_sn", apEmbarazada ? "SI" : "NO");
        rep.put("apLactancia_sn", apLactancia ? "SI" : "NO");
        rep.put("apAdultoMayor_sn", apAdultoMayor ? "SI" : "NO");

        // --- Para PDF: decide “aplica” usando la ficha (si existe), si no usa el checkbox ---
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

        // --- Discapacidad (DIS_*) ---
        rep.put("disTipo", safe(ficha != null ? ficha.getDisTipo() : null));
        rep.put("disDescripcion", safe(ficha != null ? ficha.getDisDescripcion() : null));

        Integer porc = (ficha != null ? ficha.getDisPorcentaje() : null);
        rep.put("disPorcentaje", porc == null ? "" : String.valueOf(porc)); // la plantilla le pone “%” si quieres

        // --- Catastrófica (CAT_*) ---
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

    // =====================================================
    // Helpers para PDF (Ficha): observaciones y "Otros" riesgos
    // =====================================================
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
