package ec.gob.igm.rrhh.consultorio.web.facade;

import com.lowagie.text.DocumentException;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfRenderer;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Named("centroMedicoPdfFacade")
@ViewScoped
public class CentroMedicoPdfFacade implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(CentroMedicoPdfFacade.class);

    private static final long serialVersionUID = 1L;

    private static final String TEMPLATE_PATH = "/resources/pdf/plantilla_ficha.html";

    @Inject
    private PdfTemplateEngine pdfTemplateEngine;

    @Inject
    private PdfRenderer pdfRenderer;

    @Inject
    private PdfSessionStore pdfSessionStore;

        private static final long serialVersionUID = 1L;

        public BusinessValidationException(String message) {
            super(message);
        }
    }

    private void fail(String message) {
        throw new BusinessValidationException(message);
    }

    private void handleUnexpected(String action, Throwable t) {
        log.error("Unexpected error during {}. activeStep={}, noPersonaSel={}, cedulaBusqueda={}",
                action, activeStep, noPersonaSel, cedulaBusqueda, t);
        error("Ocurrió un error inesperado al " + action + ". Revise el log o contacte a soporte.");
    }

    private static final int H_ROWS = 4;
    private static final int CONSUMO_ROWS = 3;
    private static final int DIAG_ROWS = 6;

    private String activeStep = "step1";
    private boolean cedulaDlgAutoOpened = false;

    private boolean mostrarDlgCedula = true;
    private boolean preRenderDone = false;
    private boolean mostrarDialogoAux;
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

        String html = construirHtmlDesdePlantilla(ctx, reemplazos);
        byte[] bytes = pdfRenderer.render(html);

        String token = "FICHA_" + UUID.randomUUID().toString().replace("-", "");
        pdfSessionStore.store(ctx.getExternalContext(), token, bytes);

    private String grupoSanguineo;
    private String lateralidad;
    private String motivoObs;

    private boolean apEmbarazada;
    private boolean apDiscapacidad;
    private boolean apCatastrofica;
    private boolean apLactancia;
    private boolean apAdultoMayor;

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
    private transient Step2RiskOrchestratorService step2RiskOrchestratorService;

    @jakarta.inject.Inject
    private transient PdfTemplateEngine pdfTemplateEngine;

    @jakarta.inject.Inject
    private transient PdfRenderer pdfRenderer;

    @jakarta.inject.Inject
    private transient PdfSessionStore pdfSessionStore;

    @EJB
    private transient FichaExamenCompService fichaExamenCompService;
    @EJB
    private transient ExamenFisicoRegionalService examenFisicoRegionalService;

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

            LOG.debug("GET? {} activeStep={} empleadoSel={} mostrarDlgCedula={}",
                    !FacesContext.getCurrentInstance().isPostback(), activeStep,
                    (empleadoSel == null), mostrarDlgCedula);

        } catch (RuntimeException e) {
            log.error("preRenderInit failed. activeStep={}, noPersonaSel={}, cedulaBusqueda={}",
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
        final String p = (prefix == null || prefix.isBlank()) ? "PDF_" : prefix;
        final String token = p + UUID.randomUUID().toString().replace("-", "");
        pdfSessionStore.store(ctx.getExternalContext(), token, pdfBytes);
        return token;
    }

    private String construirHtmlDesdePlantilla(FacesContext ctx, Map<String, String> rep) throws IOException {
        String plantilla = leerRecursoComoString(ctx.getExternalContext(), TEMPLATE_PATH);
        String html = pdfTemplateEngine.render(plantilla, rep);
        return normalizarXhtml(html);
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

    /**
     * Normaliza XHTML para Flying Saucer:
     * - asegura xmlns
     * - corrige & sueltos
     * - etc.
     *
     * Deja esta función pequeña; si ya tienes una implementación más completa,
     * la migramos aquí también.
     */
    private String normalizarXhtml(String html) {
        if (html == null) return "";

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

}
