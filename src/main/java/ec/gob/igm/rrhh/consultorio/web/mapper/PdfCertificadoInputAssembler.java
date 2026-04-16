package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
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
        DatEmpleado empleadoSel = source.getEmpleadoSel();
        PersonaAux personaAux = source.getPersonaAux();

        input.apellido1 = firstNotBlank(source.getApellido1(),
                personaAux != null ? personaAux.getApellido1() : null,
                empleadoSel != null ? empleadoSel.getPriApellido() : null);
        input.apellido2 = firstNotBlank(source.getApellido2(),
                personaAux != null ? personaAux.getApellido2() : null,
                empleadoSel != null ? empleadoSel.getSegApellido() : null);

        String[] nombresEmpleado = splitNombres(empleadoSel != null ? empleadoSel.getNombres() : null);
        input.nombre1 = firstNotBlank(source.getNombre1(),
                personaAux != null ? personaAux.getNombre1() : null,
                nombresEmpleado[0]);
        input.nombre2 = firstNotBlank(source.getNombre2(),
                personaAux != null ? personaAux.getNombre2() : null,
                nombresEmpleado[1]);

        input.sexo = firstNotBlank(source.getSexo(),
                personaAux != null ? personaAux.getSexo() : null,
                empleadoSel != null && empleadoSel.getSexo() != null ? empleadoSel.getSexo().getDescripcion() : null);
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

    private String firstNotBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String[] splitNombres(String nombres) {
        if (nombres == null || nombres.trim().isEmpty()) {
            return new String[] {"", ""};
        }
        String[] parts = nombres.trim().split("\\s+", 2);
        String nombre1 = parts.length > 0 ? parts[0] : "";
        String nombre2 = parts.length > 1 ? parts[1] : "";
        return new String[] {nombre1, nombre2};
    }
}
