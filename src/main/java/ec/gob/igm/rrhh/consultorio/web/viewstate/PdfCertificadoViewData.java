package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;

/**
 * Class PdfCertificadoViewData: contiene la lógica de la aplicación.
 */
public class PdfCertificadoViewData {

    public final FichaOcupacional ficha;
    public final Supplier<Boolean> verificarFichaCompleta;
    public final Consumer<Date> fechaEmisionSetter;
    public final CentroMedicoPdfFacade centroMedicoPdfFacade;
    public final Date fechaEmision;
    public final String aptitudSel;
    public final String tipoEval;
    public final String tipoEvaluacion;
    public final String institucion;
    public final String ruc;
    public final String noHistoria;
    public final String noArchivo;
    public final String centroTrabajo;
    public final String ciiu;
    public final String apellido1;
    public final String apellido2;
    public final String nombre1;
    public final String nombre2;
    public final String sexo;
    public final String detalleObservaciones;
    public final String recomendaciones;
    public final String medicoNombre;
    public final String medicoCodigo;
    public final PdfResourceResolver pdfResourceResolver;
    public final PdfTemplateEngine pdfTemplateEngine;
    public final CertificadoPdfTemplateService certificadoPdfTemplateService;

    public PdfCertificadoViewData(FichaOcupacional ficha, Supplier<Boolean> verificarFichaCompleta,
                                  Consumer<Date> fechaEmisionSetter, CentroMedicoPdfFacade centroMedicoPdfFacade,
                                  Date fechaEmision, String aptitudSel, String tipoEval, String tipoEvaluacion,
                                  String institucion, String ruc, String noHistoria, String noArchivo,
                                  String centroTrabajo, String ciiu, String apellido1, String apellido2,
                                  String nombre1, String nombre2, String sexo, String detalleObservaciones,
                                  String recomendaciones, String medicoNombre, String medicoCodigo,
                                  PdfResourceResolver pdfResourceResolver, PdfTemplateEngine pdfTemplateEngine,
                                  CertificadoPdfTemplateService certificadoPdfTemplateService) {
        this.ficha = ficha;
        this.verificarFichaCompleta = verificarFichaCompleta;
        this.fechaEmisionSetter = fechaEmisionSetter;
        this.centroMedicoPdfFacade = centroMedicoPdfFacade;
        this.fechaEmision = fechaEmision;
        this.aptitudSel = aptitudSel;
        this.tipoEval = tipoEval;
        this.tipoEvaluacion = tipoEvaluacion;
        this.institucion = institucion;
        this.ruc = ruc;
        this.noHistoria = noHistoria;
        this.noArchivo = noArchivo;
        this.centroTrabajo = centroTrabajo;
        this.ciiu = ciiu;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.nombre1 = nombre1;
        this.nombre2 = nombre2;
        this.sexo = sexo;
        this.detalleObservaciones = detalleObservaciones;
        this.recomendaciones = recomendaciones;
        this.medicoNombre = medicoNombre;
        this.medicoCodigo = medicoCodigo;
        this.pdfResourceResolver = pdfResourceResolver;
        this.pdfTemplateEngine = pdfTemplateEngine;
        this.certificadoPdfTemplateService = certificadoPdfTemplateService;
    }
}
