package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.Date;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
/**
 * Class PacienteViewBinder: orquesta la lógica de presentación y flujo web.
 */
public class PacienteViewBinder implements Serializable {

    private static final long serialVersionUID = 1L;

    public PacienteUiPatch forCedulaSearch(PacienteUiFlowCoordinator.UiFlowResult result) {
        PacienteUiPatch patch = basePatch(result)
                .cedulaBusqueda(result.getCedulaBusqueda())
                .mostrarDlgCedula(result.isMostrarDlgCedula());

        if (result.isFound()) {
            patch.apellido1(result.getApellido1())
                    .apellido2(result.getApellido2())
                    .nombre1(result.getNombre1())
                    .nombre2(result.getNombre2())
                    .sexo(result.getSexo())
                    .fechaNacimiento(result.getFechaNacimiento())
                    .edad(result.getEdad());
        }

        return patch;
    }

    public PacienteUiPatch forAbrirPersonaAuxManual(PacienteUiFlowCoordinator.UiFlowResult result) {
        return basePatch(result).mostrarDialogoAux(result.isMostrarDialogoAux());
    }

    public PacienteUiPatch forGuardarPersonaAux(PacienteUiFlowCoordinator.UiFlowResult result) {
        return basePatch(result)
                .cedulaBusqueda(result.getCedulaBusqueda())
                .apellido1(result.getApellido1())
                .apellido2(result.getApellido2())
                .nombre1(result.getNombre1())
                .nombre2(result.getNombre2())
                .sexo(result.getSexo())
                .fechaNacimiento(result.getFechaNacimiento())
                .noHistoria(result.getNoHistoria())
                .mostrarDialogoAux(result.isMostrarDialogoAux())
                .mostrarDlgCedula(result.isMostrarDlgCedula());
    }

    public PacienteUiPatch forManualPreparation(PersonaAuxFlowService.ManualPreparationResult result) {
        return PacienteUiPatch.builder()
                .personaAux(result.getPersonaAux())
                .permitirIngresoManual(result.isPermitirIngresoManual());
    }

    public PacienteUiPatch forGeneralFlow(PacienteUiFlowCoordinator.UiFlowResult result) {
        return basePatch(result);
    }

    private PacienteUiPatch basePatch(PacienteUiFlowCoordinator.UiFlowResult result) {
        return PacienteUiPatch.builder()
                .ficha(result.getFicha())
                .empleadoSel(result.getEmpleadoSel())
                .noPersonaSel(result.getNoPersonaSel())
                .personaAux(result.getPersonaAux())
                .permitirIngresoManual(result.isPermitirIngresoManual());
    }

    public static final class PacienteUiPatch {
        private FichaOcupacional ficha;
        private DatEmpleado empleadoSel;
        private Integer noPersonaSel;
        private PersonaAux personaAux;
        private Boolean permitirIngresoManual;
        private String cedulaBusqueda;
        private String apellido1;
        private String apellido2;
        private String nombre1;
        private String nombre2;
        private String sexo;
        private Date fechaNacimiento;
        private Integer edad;
        private String noHistoria;
        private Boolean mostrarDlgCedula;
        private Boolean mostrarDialogoAux;

        private boolean applyFicha;
        private boolean applyEmpleadoSel;
        private boolean applyNoPersonaSel;
        private boolean applyPersonaAux;
        private boolean applyPermitirIngresoManual;
        private boolean applyCedulaBusqueda;
        private boolean applyApellido1;
        private boolean applyApellido2;
        private boolean applyNombre1;
        private boolean applyNombre2;
        private boolean applySexo;
        private boolean applyFechaNacimiento;
        private boolean applyEdad;
        private boolean applyNoHistoria;
        private boolean applyMostrarDlgCedula;
        private boolean applyMostrarDialogoAux;

        public static PacienteUiPatch builder() { return new PacienteUiPatch(); }

        public PacienteUiPatch ficha(FichaOcupacional ficha) { this.ficha = ficha; this.applyFicha = true; return this; }
        public PacienteUiPatch empleadoSel(DatEmpleado empleadoSel) { this.empleadoSel = empleadoSel; this.applyEmpleadoSel = true; return this; }
        public PacienteUiPatch noPersonaSel(Integer noPersonaSel) { this.noPersonaSel = noPersonaSel; this.applyNoPersonaSel = true; return this; }
        public PacienteUiPatch personaAux(PersonaAux personaAux) { this.personaAux = personaAux; this.applyPersonaAux = true; return this; }
        public PacienteUiPatch permitirIngresoManual(Boolean permitirIngresoManual) { this.permitirIngresoManual = permitirIngresoManual; this.applyPermitirIngresoManual = true; return this; }
        public PacienteUiPatch cedulaBusqueda(String cedulaBusqueda) { this.cedulaBusqueda = cedulaBusqueda; this.applyCedulaBusqueda = true; return this; }
        public PacienteUiPatch apellido1(String apellido1) { this.apellido1 = apellido1; this.applyApellido1 = true; return this; }
        public PacienteUiPatch apellido2(String apellido2) { this.apellido2 = apellido2; this.applyApellido2 = true; return this; }
        public PacienteUiPatch nombre1(String nombre1) { this.nombre1 = nombre1; this.applyNombre1 = true; return this; }
        public PacienteUiPatch nombre2(String nombre2) { this.nombre2 = nombre2; this.applyNombre2 = true; return this; }
        public PacienteUiPatch sexo(String sexo) { this.sexo = sexo; this.applySexo = true; return this; }
        public PacienteUiPatch fechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; this.applyFechaNacimiento = true; return this; }
        public PacienteUiPatch edad(Integer edad) { this.edad = edad; this.applyEdad = true; return this; }
        public PacienteUiPatch noHistoria(String noHistoria) { this.noHistoria = noHistoria; this.applyNoHistoria = true; return this; }
        public PacienteUiPatch mostrarDlgCedula(Boolean mostrarDlgCedula) { this.mostrarDlgCedula = mostrarDlgCedula; this.applyMostrarDlgCedula = true; return this; }
        public PacienteUiPatch mostrarDialogoAux(Boolean mostrarDialogoAux) { this.mostrarDialogoAux = mostrarDialogoAux; this.applyMostrarDialogoAux = true; return this; }

        public FichaOcupacional getFicha() { return ficha; }
        public DatEmpleado getEmpleadoSel() { return empleadoSel; }
        public Integer getNoPersonaSel() { return noPersonaSel; }
        public PersonaAux getPersonaAux() { return personaAux; }
        public Boolean getPermitirIngresoManual() { return permitirIngresoManual; }
        public String getCedulaBusqueda() { return cedulaBusqueda; }
        public String getApellido1() { return apellido1; }
        public String getApellido2() { return apellido2; }
        public String getNombre1() { return nombre1; }
        public String getNombre2() { return nombre2; }
        public String getSexo() { return sexo; }
        public Date getFechaNacimiento() { return fechaNacimiento; }
        public Integer getEdad() { return edad; }
        public String getNoHistoria() { return noHistoria; }
        public Boolean getMostrarDlgCedula() { return mostrarDlgCedula; }
        public Boolean getMostrarDialogoAux() { return mostrarDialogoAux; }

        public boolean appliesFicha() { return applyFicha; }
        public boolean appliesEmpleadoSel() { return applyEmpleadoSel; }
        public boolean appliesNoPersonaSel() { return applyNoPersonaSel; }
        public boolean appliesPersonaAux() { return applyPersonaAux; }
        public boolean appliesPermitirIngresoManual() { return applyPermitirIngresoManual; }
        public boolean appliesCedulaBusqueda() { return applyCedulaBusqueda; }
        public boolean appliesApellido1() { return applyApellido1; }
        public boolean appliesApellido2() { return applyApellido2; }
        public boolean appliesNombre1() { return applyNombre1; }
        public boolean appliesNombre2() { return applyNombre2; }
        public boolean appliesSexo() { return applySexo; }
        public boolean appliesFechaNacimiento() { return applyFechaNacimiento; }
        public boolean appliesEdad() { return applyEdad; }
        public boolean appliesNoHistoria() { return applyNoHistoria; }
        public boolean appliesMostrarDlgCedula() { return applyMostrarDlgCedula; }
        public boolean appliesMostrarDialogoAux() { return applyMostrarDialogoAux; }
    }
}
