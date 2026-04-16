package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfControllerSupport;
import jakarta.ejb.Stateless;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Stateless
public class PdfCertificadoInputAssembler {

    public CentroMedicoPdfControllerSupport.CapturePdfCertificadoInput capture(CentroMedicoCtrl source,
            Supplier<Boolean> verificarFichaCompleta,
            Consumer<Date> fechaEmisionSetter,
            CentroMedicoPdfFacade centroMedicoPdfFacade,
            PdfResourceResolver pdfResourceResolver,
            PdfTemplateEngine pdfTemplateEngine,
            CertificadoPdfTemplateService certificadoPdfTemplateService) {
        CentroMedicoPdfControllerSupport.CapturePdfCertificadoInput input = new CentroMedicoPdfControllerSupport.CapturePdfCertificadoInput();
        populateCertificadoCore(input, source, verificarFichaCompleta, fechaEmisionSetter, centroMedicoPdfFacade);
        populatePacienteData(input, source);
        populateCertificadoTemplateData(input, source, pdfResourceResolver, pdfTemplateEngine, certificadoPdfTemplateService);
        return input;
    }

    private void populateCertificadoCore(CentroMedicoPdfControllerSupport.CapturePdfCertificadoInput input,
            CentroMedicoCtrl source, Supplier<Boolean> verificarFichaCompleta, Consumer<Date> fechaEmisionSetter,
            CentroMedicoPdfFacade centroMedicoPdfFacade) {
        input.ficha = source.getFicha();
        input.verificarFichaCompleta = verificarFichaCompleta;
        input.fechaEmisionSetter = fechaEmisionSetter;
        input.centroMedicoPdfFacade = centroMedicoPdfFacade;
        input.fechaEmision = source.getFechaEmision();
        input.aptitudSel = source.getAptitudSel();
        input.tipoEval = source.getTipoEval();
        input.tipoEvaluacion = source.getTipoEvaluacion();
        input.institucion = source.getInstitucion();
        input.ruc = source.getRuc();
        input.noHistoria = source.getNoHistoria();
        input.noArchivo = source.getNoArchivo();
        input.centroTrabajo = source.getCentroTrabajo();
        input.ciiu = source.getCiiu();
    }

    private void populatePacienteData(CentroMedicoPdfControllerSupport.CapturePdfCertificadoInput input,
            CentroMedicoCtrl source) {
        input.apellido1 = source.getApellido1();
        input.apellido2 = source.getApellido2();
        input.nombre1 = source.getNombre1();
        input.nombre2 = source.getNombre2();
        input.sexo = source.getSexo();
    }

    private void populateCertificadoTemplateData(CentroMedicoPdfControllerSupport.CapturePdfCertificadoInput input,
            CentroMedicoCtrl source, PdfResourceResolver pdfResourceResolver, PdfTemplateEngine pdfTemplateEngine,
            CertificadoPdfTemplateService certificadoPdfTemplateService) {
        input.detalleObservaciones = source.getDetalleObservaciones();
        input.recomendaciones = source.getRecomendaciones();
        input.medicoNombre = source.getMedicoNombre();
        input.medicoCodigo = source.getMedicoCodigo();
        input.pdfResourceResolver = pdfResourceResolver;
        input.pdfTemplateEngine = pdfTemplateEngine;
        input.certificadoPdfTemplateService = certificadoPdfTemplateService;
    }
}
