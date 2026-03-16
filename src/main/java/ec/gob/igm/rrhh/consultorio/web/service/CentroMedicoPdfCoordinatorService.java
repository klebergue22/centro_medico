package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;

@Stateless
/**
 * Class CentroMedicoPdfCoordinatorService: orquesta la lógica de presentación y flujo web.
 */
public class CentroMedicoPdfCoordinatorService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private FichaPdfPreparationService fichaPdfPreparationService;

    public FichaResult prepararFicha(PrepareFichaCommand cmd) {
        FichaPdfPreparationService.FichaPdfPrepareResult result = fichaPdfPreparationService.preparar(
                cmd.ficha,
                cmd.empleadoSel,
                cmd.personaAux,
                cmd.permitirIngresoManual,
                cmd.asegurarPersonaAuxPersistida,
                cmd.htmlFichaSupplier,
                cmd.pdfFacade);

        if (!result.valid) {
            return FichaResult.invalid(result.errores);
        }

        return FichaResult.ready(result.ficha, result.token, result.listo);
    }

    public CertificadoResult prepararCertificado(PrepareCertificadoCommand cmd) {
        if (cmd.ficha == null) {
            return CertificadoResult.invalid(List.of("No existe ficha para generar certificado."));
        }

        if (cmd.ficha.getFechaEmision() == null) {
            cmd.ficha.setFechaEmision(new Date());
        }
        cmd.fechaEmisionConsumer.accept(cmd.ficha.getFechaEmision());

        List<String> errores = new ArrayList<>();
        if (!cmd.verificarFichaCompleta.get()) {
            errores.add("La ficha no está completa para generar certificado.");
            return CertificadoResult.invalid(errores);
        }

        String token = cmd.pdfFacade.generarDesdeHtml(cmd.htmlCertificadoSupplier.get(), "CERT_");
        return CertificadoResult.ready(token);
    }

    public Step4Result regenerarStep4(RegenerarStep4Command cmd) {
        if (cmd.ficha == null || cmd.ficha.getIdFicha() == null) {
            return Step4Result.skipped();
        }

        FichaResult fichaResult = prepararFicha(cmd.prepareFichaCommand);
        if (!fichaResult.listo) {
            return Step4Result.from(fichaResult, null);
        }

        CertificadoResult certificadoResult = prepararCertificado(cmd.prepareCertificadoCommand.withFicha(fichaResult.ficha));
        return Step4Result.from(fichaResult, certificadoResult);
    }

    public static class PrepareFichaCommand {

        public final FichaOcupacional ficha;
        public final DatEmpleado empleadoSel;
        public final PersonaAux personaAux;
        public final boolean permitirIngresoManual;
        public final Runnable asegurarPersonaAuxPersistida;
        public final Supplier<String> htmlFichaSupplier;
        public final CentroMedicoPdfFacade pdfFacade;

        public PrepareFichaCommand(FichaOcupacional ficha, DatEmpleado empleadoSel, PersonaAux personaAux,
                boolean permitirIngresoManual, Runnable asegurarPersonaAuxPersistida,
                Supplier<String> htmlFichaSupplier, CentroMedicoPdfFacade pdfFacade) {
            this.ficha = ficha;
            this.empleadoSel = empleadoSel;
            this.personaAux = personaAux;
            this.permitirIngresoManual = permitirIngresoManual;
            this.asegurarPersonaAuxPersistida = asegurarPersonaAuxPersistida;
            this.htmlFichaSupplier = htmlFichaSupplier;
            this.pdfFacade = pdfFacade;
        }
    }

    public static class PrepareCertificadoCommand {

        public final FichaOcupacional ficha;
        public final Supplier<Boolean> verificarFichaCompleta;
        public final Supplier<String> htmlCertificadoSupplier;
        public final Consumer<Date> fechaEmisionConsumer;
        public final CentroMedicoPdfFacade pdfFacade;

        public PrepareCertificadoCommand(FichaOcupacional ficha,
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

        public PrepareCertificadoCommand withFicha(FichaOcupacional nuevaFicha) {
            return new PrepareCertificadoCommand(nuevaFicha, verificarFichaCompleta, htmlCertificadoSupplier,
                    fechaEmisionConsumer, pdfFacade);
        }
    }

    public static class RegenerarStep4Command {

        public final FichaOcupacional ficha;
        public final PrepareFichaCommand prepareFichaCommand;
        public final PrepareCertificadoCommand prepareCertificadoCommand;

        public RegenerarStep4Command(FichaOcupacional ficha,
                PrepareFichaCommand prepareFichaCommand,
                PrepareCertificadoCommand prepareCertificadoCommand) {
            this.ficha = ficha;
            this.prepareFichaCommand = prepareFichaCommand;
            this.prepareCertificadoCommand = prepareCertificadoCommand;
        }
    }

    public static class FichaResult {

        public final boolean listo;
        public final List<String> errores;
        public final FichaOcupacional ficha;
        public final String token;

        private FichaResult(boolean listo, List<String> errores, FichaOcupacional ficha, String token) {
            this.listo = listo;
            this.errores = errores;
            this.ficha = ficha;
            this.token = token;
        }

        static FichaResult invalid(List<String> errores) {
            return new FichaResult(false, errores, null, null);
        }

        static FichaResult ready(FichaOcupacional ficha, String token, boolean listo) {
            return new FichaResult(listo, List.of(), ficha, token);
        }
    }

    public static class CertificadoResult {

        public final boolean listo;
        public final String token;
        public final List<String> errores;

        private CertificadoResult(boolean listo, String token, List<String> errores) {
            this.listo = listo;
            this.token = token;
            this.errores = errores;
        }

        static CertificadoResult ready(String token) {
            return new CertificadoResult(token != null, token, List.of());
        }

        static CertificadoResult invalid(List<String> errores) {
            return new CertificadoResult(false, null, errores);
        }
    }

    public static class Step4Result {

        public final boolean skipped;
        public final FichaResult ficha;
        public final CertificadoResult certificado;

        private Step4Result(boolean skipped, FichaResult ficha, CertificadoResult certificado) {
            this.skipped = skipped;
            this.ficha = ficha;
            this.certificado = certificado;
        }

        static Step4Result skipped() {
            return new Step4Result(true, null, null);
        }

        static Step4Result from(FichaResult ficha, CertificadoResult certificado) {
            return new Step4Result(false, ficha, certificado);
        }
    }
}
