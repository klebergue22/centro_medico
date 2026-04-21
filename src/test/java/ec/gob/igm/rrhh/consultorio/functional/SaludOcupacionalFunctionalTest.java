package ec.gob.igm.rrhh.consultorio.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.web.validation.Step2Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.Step3Validator;
import ec.gob.igm.rrhh.consultorio.web.validation.ValidationResult;

class SaludOcupacionalFunctionalTest {

    private final Step2Validator step2Validator = new Step2Validator();
    private final Step3Validator step3Validator = new Step3Validator();

    @Test
    void step2DebeRechazarCuandoFaltanDatosObligatorios() {
        ValidationResult result = step2Validator.validate(new FichaRiesgo(), "   ", List.of(" "), List.of(""));

        assertFalse(result.isValid());
        assertEquals(List.of(
                "Debe ingresar el puesto de trabajo.",
                "Debe registrar al menos una actividad laboral.",
                "Debe registrar al menos una medida preventiva."
        ), result.getErrors());
    }

    @Test
    void step2DebeAceptarCuandoElPuestoVieneDeLaFichaYHayDatosMinimos() {
        ValidationResult result = step2Validator.validate(
                new FichaRiesgo(),
                "ANALISTA CARTOGRAFICO",
                List.of("Levantamiento de informacion en campo"),
                List.of("Uso permanente de EPP"));

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void step3DebeExigirDiagnosticoAptitudRecomendacionYDatosDelProfesional() {
        ValidationResult result = step3Validator.validate(List.of(new ConsultaDiagnostico()), " ", "", null, null);

        assertFalse(result.isValid());
        assertEquals(List.of(
                "Debe registrar al menos un diagn\u00f3stico (CIE10).",
                "Debe seleccionar la aptitud m\u00e9dica.",
                "Debe ingresar al menos una recomendaci\u00f3n.",
                "Debe ingresar el nombre del profesional.",
                "Debe ingresar el c\u00f3digo del m\u00e9dico."
        ), result.getErrors());
    }

    @Test
    void step3DebeAceptarCierreCompletoDeLaFichaOcupacional() {
        ConsultaDiagnostico diagnostico = new ConsultaDiagnostico();
        diagnostico.setCodigo("M545");
        diagnostico.setDescripcion("Lumbalgia");

        ValidationResult result = step3Validator.validate(
                List.of(diagnostico),
                "APTO",
                "Realizar pausas activas durante la jornada.",
                "Dra. Maria Perez",
                "MSP-44521");

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }
}
