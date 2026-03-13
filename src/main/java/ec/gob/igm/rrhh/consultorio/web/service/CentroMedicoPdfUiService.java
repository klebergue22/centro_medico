package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.faces.context.FacesContext;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;

@Stateless
public class CentroMedicoPdfUiService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CentroMedicoPdfCoordinatorService centroMedicoPdfCoordinatorService;

    public CentroMedicoPdfCoordinatorService.PrepareFichaCommand buildPrepareFichaCommand(PrepareFichaUiCommand cmd) {
        return new CentroMedicoPdfCoordinatorService.PrepareFichaCommand(
                cmd.ficha,
                cmd.empleadoSel,
                cmd.personaAux,
                cmd.permitirIngresoManual,
                cmd.asegurarPersonaAuxPersistida,
                cmd.htmlFichaSupplier,
                cmd.pdfFacade);
    }

    public CentroMedicoPdfCoordinatorService.PrepareCertificadoCommand buildPrepareCertificadoCommand(PrepareCertificadoUiCommand cmd) {
        return new CentroMedicoPdfCoordinatorService.PrepareCertificadoCommand(
                cmd.ficha,
                cmd.verificarFichaCompleta,
                cmd.htmlCertificadoSupplier,
                cmd.fechaEmisionConsumer,
                cmd.pdfFacade);
    }

    public CentroMedicoPdfCoordinatorService.FichaResult prepararFicha(PrepareFichaUiCommand cmd) {
        return centroMedicoPdfCoordinatorService.prepararFicha(buildPrepareFichaCommand(cmd));
    }

    public CentroMedicoPdfCoordinatorService.CertificadoResult prepararCertificado(PrepareCertificadoUiCommand cmd) {
        return centroMedicoPdfCoordinatorService.prepararCertificado(buildPrepareCertificadoCommand(cmd));
    }

    public CertificadoPreviewResult generarCertificadoPreview(FichaOcupacional ficha,
            Supplier<Boolean> verificarFichaCompleta,
            Supplier<String> htmlCertificadoSupplier,
            CentroMedicoPdfFacade facade,
            Consumer<Date> fechaEmisionConsumer) {

        if (ficha == null) {
            return CertificadoPreviewResult.invalid(List.of("No existe ficha para generar certificado."));
        }

        if (ficha.getFechaEmision() == null) {
            ficha.setFechaEmision(new Date());
        }
        fechaEmisionConsumer.accept(ficha.getFechaEmision());

        List<String> errores = new ArrayList<>();
        if (!Boolean.TRUE.equals(verificarFichaCompleta.get())) {
            errores.add("La ficha no está completa para generar certificado.");
            return CertificadoPreviewResult.invalid(errores);
        }

        String token = facade.generarDesdeHtml(htmlCertificadoSupplier.get(), "CERT_");
        return CertificadoPreviewResult.ready(token);
    }

    public void cleanupPdfPreview(FacesContext ctx, PdfSessionStore pdfSessionStore, String token) {
        if (ctx == null) {
            return;
        }
        if (token != null) {
            pdfSessionStore.remove(ctx, token);
        }
    }

    public static class PrepareFichaUiCommand {

        public final FichaOcupacional ficha;
        public final DatEmpleado empleadoSel;
        public final PersonaAux personaAux;
        public final boolean permitirIngresoManual;
        public final Runnable asegurarPersonaAuxPersistida;
        public final Supplier<String> htmlFichaSupplier;
        public final CentroMedicoPdfFacade pdfFacade;

        public PrepareFichaUiCommand(FichaOcupacional ficha,
                DatEmpleado empleadoSel,
                PersonaAux personaAux,
                boolean permitirIngresoManual,
                Runnable asegurarPersonaAuxPersistida,
                Supplier<String> htmlFichaSupplier,
                CentroMedicoPdfFacade pdfFacade) {
            this.ficha = ficha;
            this.empleadoSel = empleadoSel;
            this.personaAux = personaAux;
            this.permitirIngresoManual = permitirIngresoManual;
            this.asegurarPersonaAuxPersistida = asegurarPersonaAuxPersistida;
            this.htmlFichaSupplier = htmlFichaSupplier;
            this.pdfFacade = pdfFacade;
        }
    }

    public static class PrepareCertificadoUiCommand {

        public final FichaOcupacional ficha;
        public final Supplier<Boolean> verificarFichaCompleta;
        public final Supplier<String> htmlCertificadoSupplier;
        public final Consumer<Date> fechaEmisionConsumer;
        public final CentroMedicoPdfFacade pdfFacade;

        public PrepareCertificadoUiCommand(FichaOcupacional ficha,
                Supplier<Boolean> verificarFichaCompleta,
                Supplier<String> htmlCertificadoSupplier,
                Consumer<Date> fechaEmisionConsumer,
                CentroMedicoPdfFacade pdfFacade) {
            this.ficha = ficha;
            this.verificarFichaCompleta = verificarFichaCompleta;
            this.htmlCertificadoSupplier = htmlCertificadoSupplier;
            this.fechaEmisionConsumer = fechaEmisionConsumer;
            this.pdfFacade = pdfFacade;
        }
    }

    public static class CertificadoPreviewResult {

        public final boolean listo;
        public final String token;
        public final List<String> errores;

        private CertificadoPreviewResult(boolean listo, String token, List<String> errores) {
            this.listo = listo;
            this.token = token;
            this.errores = errores;
        }

        static CertificadoPreviewResult ready(String token) {
            return new CertificadoPreviewResult(token != null, token, List.of());
        }

        static CertificadoPreviewResult invalid(List<String> errores) {
            return new CertificadoPreviewResult(false, null, errores);
        }
    }
}
