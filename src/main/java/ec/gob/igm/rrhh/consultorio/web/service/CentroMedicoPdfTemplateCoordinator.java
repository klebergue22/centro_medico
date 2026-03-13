package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfContextAssembler;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfPlaceholderAssembler;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils;

@Stateless
public class CentroMedicoPdfTemplateCoordinator implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CentroMedicoPdfUiCoordinator centroMedicoPdfUiCoordinator;
    @EJB
    private CentroMedicoPdfFacadeService centroMedicoPdfFacadeService;
    @EJB
    private FichaPdfTemplateService fichaPdfTemplateService;
    @EJB
    private FichaPdfContextAssembler fichaPdfContextAssembler;
    @EJB
    private FichaPdfPlaceholderAssembler fichaPdfPlaceholderAssembler;
    @EJB
    private FichaPdfDataMapper fichaPdfDataMapper;
    @EJB
    private FichaPdfPlaceholderBuilder fichaPdfPlaceholderBuilder;
    @EJB
    private FichaPdfViewModelBuilder fichaPdfViewModelBuilder;

    public CentroMedicoPdfWorkflowService.PrepareFichaCommandData buildPrepareFichaCommand(PrepareFichaRequest req) {
        CentroMedicoPdfUiCoordinator.BuildPrepareFichaUiCommand cmd = new CentroMedicoPdfUiCoordinator.BuildPrepareFichaUiCommand();
        cmd.ficha = req.ficha;
        cmd.empleadoSel = req.empleadoSel;
        cmd.personaAux = req.personaAux;
        cmd.permitirIngresoManual = req.permitirIngresoManual;
        cmd.asegurarPersonaAuxPersistida = req.asegurarPersonaAuxPersistida;
        cmd.htmlFichaSupplier = () -> construirHtmlFichaDesdePlantilla(req);
        cmd.centroMedicoPdfFacade = req.centroMedicoPdfFacade;
        return centroMedicoPdfUiCoordinator.buildPrepareFichaCommand(cmd);
    }

    public CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData buildPrepareCertificadoCommand(PrepareCertificadoRequest req) {
        CentroMedicoPdfUiCoordinator.BuildPrepareCertificadoUiCommand cmd = new CentroMedicoPdfUiCoordinator.BuildPrepareCertificadoUiCommand();
        cmd.ficha = req.ficha;
        cmd.verificarFichaCompleta = req.verificarFichaCompleta;
        cmd.htmlCertificadoSupplier = () -> construirHtmlDesdePlantilla(req);
        cmd.fechaEmisionSetter = req.fechaEmisionSetter;
        cmd.centroMedicoPdfFacade = req.centroMedicoPdfFacade;
        return centroMedicoPdfUiCoordinator.buildPrepareCertificadoCommand(cmd);
    }

    private String construirHtmlFichaDesdePlantilla(PrepareFichaRequest req) {
        CentroMedicoPdfFacadeService.FichaTemplateCommand cmd = new CentroMedicoPdfFacadeService.FichaTemplateCommand();
        cmd.source = req.source;
        cmd.log = req.log;
        cmd.fichaPdfTemplateService = fichaPdfTemplateService;
        cmd.fichaPdfPlaceholderBuilder = fichaPdfPlaceholderBuilder;
        cmd.pdfResourceResolver = req.pdfResourceResolver;
        cmd.fichaPdfPlaceholderAssembler = fichaPdfPlaceholderAssembler;
        cmd.fichaPdfContextAssembler = fichaPdfContextAssembler;
        cmd.fichaPdfDataMapper = fichaPdfDataMapper;
        cmd.fichaPdfViewModelBuilder = fichaPdfViewModelBuilder;
        cmd.centroMedicoPdfFacade = req.centroMedicoPdfFacade;
        cmd.syncCamposDesdeObjetos = req.syncCamposDesdeObjetos;
        cmd.obtenerTipoEvaluacionPdf = req.obtenerTipoEvaluacionPdf;
        cmd.recalcularIMC = req.recalcularIMC;
        cmd.cargarAtencionPrioritaria = req.cargarAtencionPrioritaria;
        cmd.cargarActividadLaboralArrays = req.cargarActividadLaboralArrays;
        cmd.fallbackObservacionSupplier = req.fallbackObservacionSupplier;
        cmd.getSafe = req.getSafe != null ? req.getSafe : CentroMedicoViewUtils::getSafe;
        cmd.toDate = req.toDate;
        return centroMedicoPdfFacadeService.construirHtmlFichaDesdePlantilla(cmd);
    }

    private String construirHtmlDesdePlantilla(PrepareCertificadoRequest req) throws IOException {
        CentroMedicoPdfUiCoordinator.ConstruirCertificadoHtmlCommand cmd = new CentroMedicoPdfUiCoordinator.ConstruirCertificadoHtmlCommand();
        cmd.ficha = req.ficha;
        cmd.fechaEmision = req.fechaEmision;
        cmd.aptitudSel = req.aptitudSel;
        cmd.tipoEval = req.tipoEval;
        cmd.tipoEvaluacion = req.tipoEvaluacion;
        cmd.institucion = req.institucion;
        cmd.ruc = req.ruc;
        cmd.noHistoria = req.noHistoria;
        cmd.noArchivo = req.noArchivo;
        cmd.centroTrabajo = req.centroTrabajo;
        cmd.ciiu = req.ciiu;
        cmd.apellido1 = req.apellido1;
        cmd.apellido2 = req.apellido2;
        cmd.nombre1 = req.nombre1;
        cmd.nombre2 = req.nombre2;
        cmd.sexo = req.sexo;
        cmd.detalleObservaciones = req.detalleObservaciones;
        cmd.recomendaciones = req.recomendaciones;
        cmd.medicoNombre = req.medicoNombre;
        cmd.medicoCodigo = req.medicoCodigo;
        cmd.pdfResourceResolver = req.pdfResourceResolver;
        cmd.pdfTemplateEngine = req.pdfTemplateEngine;
        cmd.certificadoPdfTemplateService = req.certificadoPdfTemplateService;
        return centroMedicoPdfUiCoordinator.construirHtmlDesdePlantilla(cmd);
    }

    public static class PrepareFichaRequest {
        public Object source;
        public Logger log;
        public FichaOcupacional ficha;
        public DatEmpleado empleadoSel;
        public PersonaAux personaAux;
        public boolean permitirIngresoManual;
        public Runnable asegurarPersonaAuxPersistida;
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
        public PdfResourceResolver pdfResourceResolver;
        public Runnable syncCamposDesdeObjetos;
        public Supplier<String> obtenerTipoEvaluacionPdf;
        public Runnable recalcularIMC;
        public Consumer<Map<String, String>> cargarAtencionPrioritaria = m -> {};
        public Consumer<Map<String, String>> cargarActividadLaboralArrays = m -> {};
        public Supplier<String> fallbackObservacionSupplier = () -> "";
        public BiFunction<java.util.List<?>, Integer, Object> getSafe;
        public Function<Object, Date> toDate;
    }

    public static class PrepareCertificadoRequest {
        public FichaOcupacional ficha;
        public Supplier<Boolean> verificarFichaCompleta;
        public Consumer<Date> fechaEmisionSetter;
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
        public Date fechaEmision;
        public String aptitudSel;
        public String tipoEval;
        public String tipoEvaluacion;
        public String institucion;
        public String ruc;
        public String noHistoria;
        public String noArchivo;
        public String centroTrabajo;
        public String ciiu;
        public String apellido1;
        public String apellido2;
        public String nombre1;
        public String nombre2;
        public String sexo;
        public String detalleObservaciones;
        public String recomendaciones;
        public String medicoNombre;
        public String medicoCodigo;
        public PdfResourceResolver pdfResourceResolver;
        public PdfTemplateEngine pdfTemplateEngine;
        public CertificadoPdfTemplateService certificadoPdfTemplateService;
    }
}
