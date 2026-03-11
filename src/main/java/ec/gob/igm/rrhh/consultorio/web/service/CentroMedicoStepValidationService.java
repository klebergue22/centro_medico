package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.web.validation.Step1Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step2Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step3Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.ValidationResult;

@ApplicationScoped
public class CentroMedicoStepValidationService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Step1Validator step1Validator = new Step1Validator();
    private final Step2Validator step2Validator = new Step2Validator();
    private final Step3Validator step3Validator = new Step3Validator();

    public ValidationResult validarStep1(String apellido1, String apellido2, String nombre1, String nombre2,
                                         String sexo, String tipoEval, String paStr, Integer fc, Double peso, Double tallaCm,
                                         SignosVitales signos, FichaRiesgo fichaRiesgo) {
        return step1Validator.validate(apellido1, apellido2, nombre1, nombre2, sexo, tipoEval,
                paStr, fc, peso, tallaCm, signos, fichaRiesgo);
    }

    public ValidationResult validarStep2(FichaRiesgo fichaRiesgo, List<String> actividadesLab, List<String> medidasPreventivas) {
        return step2Validator.validate(fichaRiesgo, actividadesLab, medidasPreventivas);
    }

    public ValidationResult validarStep3(List<ConsultaDiagnostico> listaDiag, String aptitudSel,
                                         String recomendaciones, String medicoNombre, String medicoCodigo) {
        return step3Validator.validate(listaDiag, aptitudSel, recomendaciones, medicoNombre, medicoCodigo);
    }

    public void addValidationMessages(String step, ValidationResult result) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null || result == null || result.isValid()) {
            return;
        }
        for (String error : result.getErrors()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, step, error));
        }
    }
}
