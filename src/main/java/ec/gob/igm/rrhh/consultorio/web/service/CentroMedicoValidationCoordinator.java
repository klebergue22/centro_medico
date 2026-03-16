package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.web.validation.FichaCompletaValidator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step1Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step2Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step3Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.ValidationResult;

@ApplicationScoped
/**
 * Class CentroMedicoValidationCoordinator: orquesta la lógica de presentación y flujo web.
 */
public class CentroMedicoValidationCoordinator implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Step1Validator step1Validator = new Step1Validator();
    private final Step2Validator step2Validator = new Step2Validator();
    private final Step3Validator step3Validator = new Step3Validator();
    private final FichaCompletaValidator fichaCompletaValidator = new FichaCompletaValidator();

    public ValidationUiResult validarStep1(Step1ValidationInput input) {
        ValidationResult result = step1Validator.validate(
                input.apellido1,
                input.apellido2,
                input.nombre1,
                input.nombre2,
                input.sexo,
                input.tipoEval,
                input.paStr,
                input.fc,
                input.peso,
                input.tallaCm,
                input.signos,
                input.puestoTrabajoCiuo,
                input.fichaRiesgo);
        return new ValidationUiResult(result, "Step 1", false, null, false);
    }

    public ValidationUiResult validarStep2(FichaRiesgo fichaRiesgo, List<String> actividadesLab,
                                           List<String> medidasPreventivas, boolean markValidationFailedOnError) {
        ValidationResult result = step2Validator.validate(fichaRiesgo, actividadesLab, medidasPreventivas);
        return new ValidationUiResult(result, "Step 2", false, null, markValidationFailedOnError);
    }

    public ValidationUiResult validarStep3(List<ConsultaDiagnostico> listaDiag, String aptitudSel,
                                           String recomendaciones, String medicoNombre, String medicoCodigo) {
        ValidationResult result = step3Validator.validate(listaDiag, aptitudSel, recomendaciones, medicoNombre, medicoCodigo);
        return new ValidationUiResult(result, "Step 3", false, null, false);
    }

    public ValidationUiResult verificarFichaCompleta(FichaCompletaValidationInput input) {
        ValidationResult result = fichaCompletaValidator.validate(
                input.ficha,
                input.permitirIngresoManual,
                input.personaAux,
                input.empleadoSel,
                input.aptitudSel,
                input.fechaEmision,
                input.cie10PrincipalSupplier);
        return new ValidationUiResult(
                result,
                "Validación antes de generar el certificado",
                true,
                "Validación antes de generar el certificado",
                false);
    }

    public static class Step1ValidationInput implements Serializable {
        private static final long serialVersionUID = 1L;

        public String apellido1;
        public String apellido2;
        public String nombre1;
        public String nombre2;
        public String sexo;
        public String tipoEval;
        public String paStr;
        public Integer fc;
        public Double peso;
        public Double tallaCm;
        public SignosVitales signos;
        public String puestoTrabajoCiuo;
        public FichaRiesgo fichaRiesgo;
    }

    public static class FichaCompletaValidationInput implements Serializable {
        private static final long serialVersionUID = 1L;

        public FichaOcupacional ficha;
        public boolean permitirIngresoManual;
        public PersonaAux personaAux;
        public DatEmpleado empleadoSel;
        public String aptitudSel;
        public java.util.Date fechaEmision;
        public Supplier<Cie10> cie10PrincipalSupplier;
    }
}
