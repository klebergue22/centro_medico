package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.service.Step1FichaService;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.mapper.Step1CommandAssembler;
import ec.gob.igm.rrhh.consultorio.web.service.UserContextService;
import ec.gob.igm.rrhh.consultorio.web.mapper.Step1ViewDataAssembler;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
/**
 * Fachada de caso de uso para agrupar dependencias del guardado de Step 1.
 */
public class Step1Facade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private Step1CommandAssembler step1CommandAssembler;
    @Inject
    private Step1ViewDataAssembler step1ViewDataAssembler;
    @Inject
    private PacienteRegistrationFacade pacienteRegistrationFacade;
    @EJB
    private Step1FichaService step1FichaService;
    @EJB
    private UserContextService userContextService;

    public SaveStep1Result guardar(SaveStep1Command cmd) {
        PacienteRegistrationFacade.UiResult preUiResult = pacienteRegistrationFacade.asegurarEmpleadoEnViewScope(
                cmd.permitirIngresoManual,
                cmd.empleadoSel,
                cmd.noPersonaSel,
                cmd.ficha,
                cmd.personaAux);

        final String user = userContextService.resolveCurrentUser();
        Step1FichaService.Step1Command command = step1CommandAssembler.toCommand(
                step1ViewDataAssembler.capture(cmd.source, user));

        try {
            Step1FichaService.Step1Result result = step1FichaService.guardar(command);
            PacienteRegistrationFacade.UiResult postUiResult = pacienteRegistrationFacade.syncPatientStateAfterStep1(
                    cmd.permitirIngresoManual,
                    result.empleadoSel(),
                    cmd.noPersonaSel,
                    result.personaAux(),
                    result.ficha());
            return new SaveStep1Result(
                    result.ficha(),
                    result.empleadoSel(),
                    result.personaAux(),
                    result.signos(),
                    preUiResult,
                    postUiResult);
        } catch (Step1FichaService.Step1ValidationException ex) {
            throw new CentroMedicoCtrl.BusinessValidationException(ex.getMessage());
        }
    }

    public static final class SaveStep1Command {
        public final boolean permitirIngresoManual;
        public final DatEmpleado empleadoSel;
        public final Integer noPersonaSel;
        public final FichaOcupacional ficha;
        public final PersonaAux personaAux;
        public final CentroMedicoCtrl source;

        public SaveStep1Command(boolean permitirIngresoManual,
                                DatEmpleado empleadoSel,
                                Integer noPersonaSel,
                                FichaOcupacional ficha,
                                PersonaAux personaAux,
                                CentroMedicoCtrl source) {
            this.permitirIngresoManual = permitirIngresoManual;
            this.empleadoSel = empleadoSel;
            this.noPersonaSel = noPersonaSel;
            this.ficha = ficha;
            this.personaAux = personaAux;
            this.source = source;
        }
    }

    public static final class SaveStep1Result {
        public final FichaOcupacional ficha;
        public final DatEmpleado empleadoSel;
        public final PersonaAux personaAux;
        public final SignosVitales signos;
        public final PacienteRegistrationFacade.UiResult preUiResult;
        public final PacienteRegistrationFacade.UiResult postUiResult;

        public SaveStep1Result(FichaOcupacional ficha,
                               DatEmpleado empleadoSel,
                               PersonaAux personaAux,
                               SignosVitales signos,
                               PacienteRegistrationFacade.UiResult preUiResult,
                               PacienteRegistrationFacade.UiResult postUiResult) {
            this.ficha = ficha;
            this.empleadoSel = empleadoSel;
            this.personaAux = personaAux;
            this.signos = signos;
            this.preUiResult = preUiResult;
            this.postUiResult = postUiResult;
        }
    }
}

