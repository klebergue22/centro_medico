package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfControllerSupport;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PacienteFormData;
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
        PacienteFormData paciente = source.getStep1FormModel().getPaciente();
        input.apellido1 = paciente.getApellido1();
        input.apellido2 = paciente.getApellido2();
        input.nombre1 = paciente.getNombre1();
        input.nombre2 = paciente.getNombre2();
        input.sexo = paciente.getSexo();
        input.detalleObservaciones = source.getDetalleObservaciones();
        input.recomendaciones = source.getRecomendaciones();
        input.medicoNombre = source.getMedicoNombre();
        input.medicoCodigo = source.getMedicoCodigo();
        input.pdfResourceResolver = pdfResourceResolver;
        input.pdfTemplateEngine = pdfTemplateEngine;
        input.certificadoPdfTemplateService = certificadoPdfTemplateService;
        return input;
    }
}
