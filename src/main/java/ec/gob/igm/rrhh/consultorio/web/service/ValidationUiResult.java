package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import ec.gob.igm.rrhh.consultorio.web.jsf.CentroMedicoMessageService;
import ec.gob.igm.rrhh.consultorio.web.validation.ValidationResult;

public class ValidationUiResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ValidationResult validationResult;
    private final String summary;
    private final boolean combineErrors;
    private final String combinedDetailTitle;
    private final boolean markValidationFailedOnError;

    public ValidationUiResult(ValidationResult validationResult, String summary,
                              boolean combineErrors, String combinedDetailTitle,
                              boolean markValidationFailedOnError) {
        this.validationResult = validationResult;
        this.summary = summary;
        this.combineErrors = combineErrors;
        this.combinedDetailTitle = combinedDetailTitle;
        this.markValidationFailedOnError = markValidationFailedOnError;
    }

    public boolean isValid() {
        return validationResult != null && validationResult.isValid();
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void applyUi(CentroMedicoMessageService messageService) {
        if (validationResult == null || validationResult.isValid()) {
            return;
        }

        if (combineErrors) {
            StringBuilder sb = new StringBuilder();
            for (String error : validationResult.getErrors()) {
                sb.append("- ").append(error).append("\n");
            }
            messageService.addMsg(FacesMessage.SEVERITY_ERROR, combinedDetailTitle, sb.toString());
        } else {
            messageService.addValidationMessages(summary, validationResult);
        }

        if (markValidationFailedOnError) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx != null) {
                ctx.validationFailed();
            }
        }
    }
}

