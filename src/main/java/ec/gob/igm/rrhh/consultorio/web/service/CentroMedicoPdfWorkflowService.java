package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.Collections;
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
import ec.gob.igm.rrhh.consultorio.web.pdf.CentroMedicoPdfCommandFactory;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;

@Stateless
/**
 * Class CentroMedicoPdfWorkflowService: orquesta la lógica de presentación y flujo web.
 */
public class CentroMedicoPdfWorkflowService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CentroMedicoPdfCoordinatorService centroMedicoPdfCoordinatorService;

    @EJB
    private CentroMedicoPdfUiService centroMedicoPdfUiService;

    @EJB
    private CentroMedicoPdfCommandFactory centroMedicoPdfCommandFactory;

    public Step4FlowResult onEnterStep4AutoRegenerar(Step4FlowCommand cmd) {
        CentroMedicoPdfCoordinatorService.Step4Result result = centroMedicoPdfCoordinatorService.regenerarStep4(
                new CentroMedicoPdfCoordinatorService.RegenerarStep4Command(
                        cmd.ficha,
                        centroMedicoPdfUiService.buildPrepareFichaCommand(buildPrepareFichaCommand(cmd.prepareFichaCommand)),
                        centroMedicoPdfUiService.buildPrepareCertificadoCommand(buildPrepareCertificadoCommand(cmd.prepareCertificadoCommand))));

        if (result == null || result.skipped || result.ficha == null) {
            return Step4FlowResult.skipped();
        }

        String fichaToken = null;
        String certificadoToken = null;
        FichaOcupacional fichaResult = cmd.ficha;
        boolean fichaLista = false;
        boolean certificadoListo = false;

        if (result.ficha.listo) {
            fichaResult = result.ficha.ficha;
            fichaToken = result.ficha.token;
            fichaLista = true;
        }

        if (result.certificado != null && result.certificado.listo) {
            certificadoToken = result.certificado.token;
            certificadoListo = true;
        }

        return Step4FlowResult.ready(fichaResult, fichaLista, fichaToken, certificadoListo, certificadoToken);
    }

    public FichaFlowResult onPrepararFichaPdf(PrepareFichaFlowCommand cmd) {
        CentroMedicoPdfCoordinatorService.FichaResult result = centroMedicoPdfUiService.prepararFicha(
                buildPrepareFichaCommand(cmd.prepareFichaCommand));

        if (!result.listo) {
            return FichaFlowResult.invalid(result.errores);
        }

        return FichaFlowResult.ready(result.ficha, result.token);
    }

    public CertificadoFlowResult onPrepararCertificadoPdf(PrepareCertificadoFlowCommand cmd) {
        if (!cmd.fichaPdfListo || cmd.pdfTokenFicha == null) {
            FichaFlowResult fichaResult = onPrepararFichaPdf(new PrepareFichaFlowCommand(cmd.prepareFichaCommand));
            if (!fichaResult.listo || fichaResult.token == null) {
                return CertificadoFlowResult.needsFicha(fichaResult);
            }
        }

        CentroMedicoPdfCoordinatorService.CertificadoResult result = centroMedicoPdfUiService.prepararCertificado(
                buildPrepareCertificadoCommand(cmd.prepareCertificadoCommand));

        if (!result.listo) {
            return CertificadoFlowResult.invalid(result.errores);
        }

        return CertificadoFlowResult.ready(result.token);
    }

    public PreviewFlowResult prepararVistaPrevia(PreparePreviewFlowCommand cmd) {
        FichaFlowResult fichaResult = null;
        if (!cmd.fichaPdfListo) {
            fichaResult = onPrepararFichaPdf(new PrepareFichaFlowCommand(cmd.prepareFichaCommand));
            if (!fichaResult.listo) {
                return PreviewFlowResult.invalidFicha(fichaResult);
            }
        }

        if (!Boolean.TRUE.equals(cmd.verificarFichaCompleta.get())) {
            return PreviewFlowResult.invalidCertificado(Collections.emptyList(), fichaResult);
        }

        CertificadoFlowResult certificadoResult = onPrepararCertificadoPdf(
                new PrepareCertificadoFlowCommand(
                        true,
                        cmd.fichaPdfListo ? cmd.pdfTokenFicha : (fichaResult != null ? fichaResult.token : null),
                        cmd.prepareFichaCommand,
                        cmd.prepareCertificadoCommand));

        if (!certificadoResult.listo) {
            return PreviewFlowResult.invalidCertificado(certificadoResult.errores, fichaResult);
        }

        return PreviewFlowResult.ready(
                fichaResult,
                certificadoResult.token);
    }

    public void limpiarVistaPrevia(CleanupPdfPreviewCommand cmd) {
        centroMedicoPdfUiService.cleanupPdfPreview(cmd.ctx, cmd.pdfSessionStore, cmd.pdfTokenCertificado);
    }

    public CentroMedicoPdfUiService.PrepareFichaUiCommand buildPrepareFichaCommand(PrepareFichaCommandData cmd) {
        return centroMedicoPdfCommandFactory.buildPrepareFichaUiCommand(
                cmd.ficha,
                cmd.empleadoSel,
                cmd.personaAux,
                cmd.permitirIngresoManual,
                cmd.asegurarPersonaAuxPersistida,
                cmd.htmlFichaSupplier,
                cmd.centroMedicoPdfFacade);
    }

    public CentroMedicoPdfUiService.PrepareCertificadoUiCommand buildPrepareCertificadoCommand(PrepareCertificadoCommandData cmd) {
        return centroMedicoPdfCommandFactory.buildPrepareCertificadoUiCommand(
                cmd.ficha,
                cmd.verificarFichaCompleta,
                cmd.htmlCertificadoSupplier,
                cmd.fechaEmisionSetter,
                cmd.centroMedicoPdfFacade);
    }

    public String construirHtmlDesdePlantillaUnchecked(CentroMedicoPdfCommandFactory.ThrowingSupplier<String> supplier) {
        return centroMedicoPdfCommandFactory.construirHtmlDesdePlantillaUnchecked(supplier);
    }

    public static class Step4FlowCommand {
        public final FichaOcupacional ficha;
        public final PrepareFichaCommandData prepareFichaCommand;
        public final PrepareCertificadoCommandData prepareCertificadoCommand;

        public Step4FlowCommand(FichaOcupacional ficha,
                PrepareFichaCommandData prepareFichaCommand,
                PrepareCertificadoCommandData prepareCertificadoCommand) {
            this.ficha = ficha;
            this.prepareFichaCommand = prepareFichaCommand;
            this.prepareCertificadoCommand = prepareCertificadoCommand;
        }
    }

    public static class PrepareFichaFlowCommand {
        public final PrepareFichaCommandData prepareFichaCommand;

        public PrepareFichaFlowCommand(PrepareFichaCommandData prepareFichaCommand) {
            this.prepareFichaCommand = prepareFichaCommand;
        }
    }

    public static class PrepareCertificadoFlowCommand {
        public final boolean fichaPdfListo;
        public final String pdfTokenFicha;
        public final PrepareFichaCommandData prepareFichaCommand;
        public final PrepareCertificadoCommandData prepareCertificadoCommand;

        public PrepareCertificadoFlowCommand(boolean fichaPdfListo,
                String pdfTokenFicha,
                PrepareFichaCommandData prepareFichaCommand,
                PrepareCertificadoCommandData prepareCertificadoCommand) {
            this.fichaPdfListo = fichaPdfListo;
            this.pdfTokenFicha = pdfTokenFicha;
            this.prepareFichaCommand = prepareFichaCommand;
            this.prepareCertificadoCommand = prepareCertificadoCommand;
        }
    }

    public static class PreparePreviewFlowCommand {
        public final boolean fichaPdfListo;
        public final String pdfTokenFicha;
        public final Supplier<Boolean> verificarFichaCompleta;
        public final PrepareFichaCommandData prepareFichaCommand;
        public final PrepareCertificadoCommandData prepareCertificadoCommand;

        public PreparePreviewFlowCommand(boolean fichaPdfListo,
                String pdfTokenFicha,
                Supplier<Boolean> verificarFichaCompleta,
                PrepareFichaCommandData prepareFichaCommand,
                PrepareCertificadoCommandData prepareCertificadoCommand) {
            this.fichaPdfListo = fichaPdfListo;
            this.pdfTokenFicha = pdfTokenFicha;
            this.verificarFichaCompleta = verificarFichaCompleta;
            this.prepareFichaCommand = prepareFichaCommand;
            this.prepareCertificadoCommand = prepareCertificadoCommand;
        }
    }

    public static class CleanupPdfPreviewCommand {
        public final FacesContext ctx;
        public final PdfSessionStore pdfSessionStore;
        public final String pdfTokenCertificado;

        public CleanupPdfPreviewCommand(FacesContext ctx, PdfSessionStore pdfSessionStore, String pdfTokenCertificado) {
            this.ctx = ctx;
            this.pdfSessionStore = pdfSessionStore;
            this.pdfTokenCertificado = pdfTokenCertificado;
        }
    }

    public static class PrepareFichaCommandData {
        public final FichaOcupacional ficha;
        public final DatEmpleado empleadoSel;
        public final PersonaAux personaAux;
        public final boolean permitirIngresoManual;
        public final Runnable asegurarPersonaAuxPersistida;
        public final Supplier<String> htmlFichaSupplier;
        public final CentroMedicoPdfFacade centroMedicoPdfFacade;

        public PrepareFichaCommandData(FichaOcupacional ficha,
                DatEmpleado empleadoSel,
                PersonaAux personaAux,
                boolean permitirIngresoManual,
                Runnable asegurarPersonaAuxPersistida,
                Supplier<String> htmlFichaSupplier,
                CentroMedicoPdfFacade centroMedicoPdfFacade) {
            this.ficha = ficha;
            this.empleadoSel = empleadoSel;
            this.personaAux = personaAux;
            this.permitirIngresoManual = permitirIngresoManual;
            this.asegurarPersonaAuxPersistida = asegurarPersonaAuxPersistida;
            this.htmlFichaSupplier = htmlFichaSupplier;
            this.centroMedicoPdfFacade = centroMedicoPdfFacade;
        }
    }

    public static class PrepareCertificadoCommandData {
        public final FichaOcupacional ficha;
        public final Supplier<Boolean> verificarFichaCompleta;
        public final Supplier<String> htmlCertificadoSupplier;
        public final Consumer<java.util.Date> fechaEmisionSetter;
        public final CentroMedicoPdfFacade centroMedicoPdfFacade;

        public PrepareCertificadoCommandData(FichaOcupacional ficha,
                Supplier<Boolean> verificarFichaCompleta,
                Supplier<String> htmlCertificadoSupplier,
                Consumer<java.util.Date> fechaEmisionSetter,
                CentroMedicoPdfFacade centroMedicoPdfFacade) {
            this.ficha = ficha;
            this.verificarFichaCompleta = verificarFichaCompleta;
            this.htmlCertificadoSupplier = htmlCertificadoSupplier;
            this.fechaEmisionSetter = fechaEmisionSetter;
            this.centroMedicoPdfFacade = centroMedicoPdfFacade;
        }
    }

    public static class Step4FlowResult {
        public final boolean skipped;
        public final FichaOcupacional ficha;
        public final boolean fichaLista;
        public final String fichaToken;
        public final boolean certificadoListo;
        public final String certificadoToken;

        private Step4FlowResult(boolean skipped,
                FichaOcupacional ficha,
                boolean fichaLista,
                String fichaToken,
                boolean certificadoListo,
                String certificadoToken) {
            this.skipped = skipped;
            this.ficha = ficha;
            this.fichaLista = fichaLista;
            this.fichaToken = fichaToken;
            this.certificadoListo = certificadoListo;
            this.certificadoToken = certificadoToken;
        }

        static Step4FlowResult skipped() {
            return new Step4FlowResult(true, null, false, null, false, null);
        }

        static Step4FlowResult ready(FichaOcupacional ficha,
                boolean fichaLista,
                String fichaToken,
                boolean certificadoListo,
                String certificadoToken) {
            return new Step4FlowResult(false, ficha, fichaLista, fichaToken, certificadoListo, certificadoToken);
        }
    }

    public static class FichaFlowResult {
        public final boolean listo;
        public final FichaOcupacional ficha;
        public final String token;
        public final List<String> errores;

        private FichaFlowResult(boolean listo, FichaOcupacional ficha, String token, List<String> errores) {
            this.listo = listo;
            this.ficha = ficha;
            this.token = token;
            this.errores = errores;
        }

        static FichaFlowResult ready(FichaOcupacional ficha, String token) {
            return new FichaFlowResult(true, ficha, token, List.of());
        }

        static FichaFlowResult invalid(List<String> errores) {
            return new FichaFlowResult(false, null, null, errores);
        }
    }

    public static class CertificadoFlowResult {
        public final boolean listo;
        public final String token;
        public final List<String> errores;
        public final FichaFlowResult fichaResult;

        private CertificadoFlowResult(boolean listo, String token, List<String> errores, FichaFlowResult fichaResult) {
            this.listo = listo;
            this.token = token;
            this.errores = errores;
            this.fichaResult = fichaResult;
        }

        static CertificadoFlowResult ready(String token) {
            return new CertificadoFlowResult(true, token, List.of(), null);
        }

        static CertificadoFlowResult invalid(List<String> errores) {
            return new CertificadoFlowResult(false, null, errores, null);
        }

        static CertificadoFlowResult needsFicha(FichaFlowResult fichaResult) {
            return new CertificadoFlowResult(false, null, fichaResult.errores, fichaResult);
        }
    }

    public static class PreviewFlowResult {
        public final boolean listo;
        public final FichaFlowResult fichaResult;
        public final String certificadoToken;
        public final List<String> certificadoErrores;

        private PreviewFlowResult(boolean listo,
                FichaFlowResult fichaResult,
                String certificadoToken,
                List<String> certificadoErrores) {
            this.listo = listo;
            this.fichaResult = fichaResult;
            this.certificadoToken = certificadoToken;
            this.certificadoErrores = certificadoErrores;
        }

        static PreviewFlowResult ready(FichaFlowResult fichaResult, String certificadoToken) {
            return new PreviewFlowResult(true, fichaResult, certificadoToken, List.of());
        }

        static PreviewFlowResult invalidFicha(FichaFlowResult fichaResult) {
            return new PreviewFlowResult(false, fichaResult, null, List.of());
        }

        static PreviewFlowResult invalidCertificado(List<String> certificadoErrores, FichaFlowResult fichaResult) {
            return new PreviewFlowResult(false, fichaResult, null, certificadoErrores);
        }
    }
}
