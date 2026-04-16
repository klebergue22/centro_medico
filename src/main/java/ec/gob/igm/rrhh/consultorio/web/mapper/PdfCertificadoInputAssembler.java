package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
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

    @jakarta.inject.Inject
    private EmpleadoService empleadoService;

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
        DatEmpleado empleadoSel = source.getEmpleadoSel() != null
                ? source.getEmpleadoSel()
                : (source.getFicha() != null ? source.getFicha().getEmpleado() : null);
        empleadoSel = resolveManagedEmpleado(empleadoSel);
        PersonaAux personaAux = source.getPersonaAux() != null
                ? source.getPersonaAux()
                : (source.getFicha() != null ? source.getFicha().getPersonaAux() : null);
        String[] nombreCompleto = splitNombreCompleto(empleadoSel != null ? empleadoSel.getNombreC() : null);

        input.apellido1 = firstNotBlank(source.getApellido1(),
                personaAux != null ? personaAux.getApellido1() : null,
                empleadoSel != null ? empleadoSel.getPriApellido() : null,
                nombreCompleto[0]);
        input.apellido2 = firstNotBlank(source.getApellido2(),
                personaAux != null ? personaAux.getApellido2() : null,
                empleadoSel != null ? empleadoSel.getSegApellido() : null,
                nombreCompleto[1]);

        String[] nombresEmpleado = splitNombres(empleadoSel != null ? empleadoSel.getNombres() : null);
        input.nombre1 = firstNotBlank(source.getNombre1(),
                personaAux != null ? personaAux.getNombre1() : null,
                nombresEmpleado[0],
                nombreCompleto[2]);
        input.nombre2 = firstNotBlank(source.getNombre2(),
                personaAux != null ? personaAux.getNombre2() : null,
                nombresEmpleado[1],
                nombreCompleto[3]);

        input.sexo = firstNotBlank(source.getSexo(),
                personaAux != null ? personaAux.getSexo() : null,
                empleadoSel != null && empleadoSel.getSexo() != null ? empleadoSel.getSexo().getDescripcion() : null);
    }

    private DatEmpleado resolveManagedEmpleado(DatEmpleado empleadoSel) {
        if (empleadoSel == null) {
            return null;
        }
        Integer noPersona = empleadoSel.getNoPersona();
        if (noPersona == null || empleadoService == null) {
            return empleadoSel;
        }
        DatEmpleado managed = empleadoService.buscarPorId(noPersona);
        return managed != null ? managed : empleadoSel;
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

    private String[] splitNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return new String[] {"", "", "", ""};
        }
        String[] parts = nombreCompleto.trim().split("\\s+");
        String apellido1 = parts.length > 0 ? parts[0] : "";
        String apellido2 = parts.length > 1 ? parts[1] : "";
        String nombre1 = parts.length > 2 ? parts[2] : "";
        String nombre2 = "";
        if (parts.length > 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < parts.length; i++) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(parts[i]);
            }
            nombre2 = sb.toString();
        }
        return new String[] {apellido1, apellido2, nombre1, nombre2};
    }
}
