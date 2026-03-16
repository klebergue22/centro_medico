package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
/**
 * Class PacienteUiFlowCoordinator: orquesta la lógica de presentación y flujo web.
 */
public class PacienteUiFlowCoordinator implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient PacienteFichaStateService pacienteFichaStateService;

    @Inject
    private transient PersonaAuxFlowService personaAuxFlowService;

    @Inject
    private transient CedulaSearchService cedulaSearchService;

    public UiFlowResult abrirPersonaAuxManual(String cedulaBusqueda, PersonaAux personaAuxActual,
            FichaOcupacional fichaActual, DatEmpleado empleadoSel, Integer noPersonaSel, boolean permitirIngresoManual,
            boolean mostrarDlgCedula) {
        PersonaAuxFlowService.OpenManualResult result = personaAuxFlowService.abrirPersonaAuxManual(cedulaBusqueda, personaAuxActual);

        return UiFlowResult.builder()
                .ficha(fichaActual)
                .empleadoSel(empleadoSel)
                .noPersonaSel(noPersonaSel)
                .personaAux(result.getPersonaAux())
                .permitirIngresoManual(permitirIngresoManual)
                .mostrarDlgCedula(mostrarDlgCedula)
                .mostrarDialogoAux(result.isMostrarDialogoAux())
                .addScript("PF('dlgPersonaAux').show();")
                .build();
    }

    public UiFlowResult guardarPersonaAuxYUsar(PersonaAux personaAuxActual, FichaOcupacional fichaActual,
            DatEmpleado empleadoSelActual, Integer noPersonaSelActual) {
        PersonaAuxFlowService.SavePersonaAuxResult result = personaAuxFlowService.guardarPersonaAuxYUsar(personaAuxActual);

        return UiFlowResult.builder()
                .ficha(fichaActual)
                .empleadoSel(empleadoSelActual)
                .noPersonaSel(noPersonaSelActual)
                .personaAux(result.getPersonaAux())
                .cedulaBusqueda(result.getCedulaBusqueda())
                .apellido1(result.getApellido1())
                .apellido2(result.getApellido2())
                .nombre1(result.getNombre1())
                .nombre2(result.getNombre2())
                .sexo(result.getSexo())
                .fechaNacimiento(result.getFechaNacimiento())
                .noHistoria(result.getNoHistoria())
                .mostrarDialogoAux(result.isMostrarDialogoAux())
                .mostrarDlgCedula(result.isMostrarDialogoCedula())
                .permitirIngresoManual(result.isPermitirIngresoManual())
                .addUpdate(":noHistoriaClinica")
                .addScript("PF('dlgPersonaAux').hide(); PF('dlgCedula').hide();")
                .build();
    }

    public UiFlowResult buscarCedula(String cedulaBusqueda, FichaOcupacional fichaActual, PersonaAux personaAuxActual,
            boolean permitirIngresoManualActual) {
        CedulaSearchService.CedulaSearchResult searchResult = cedulaSearchService.search(cedulaBusqueda, fichaActual, personaAuxActual);

        UiFlowResult.Builder builder = UiFlowResult.builder()
                .cedulaBusqueda(searchResult.getCedula())
                .ficha(searchResult.getFicha())
                .personaAux(searchResult.getPersonaAux())
                .empleadoSel(searchResult.getEmpleadoSel())
                .noPersonaSel(searchResult.getNoPersonaSel())
                .found(searchResult.isFound())
                .showManual(searchResult.isShowManual())
                .apellido1(searchResult.getApellido1())
                .apellido2(searchResult.getApellido2())
                .nombre1(searchResult.getNombre1())
                .nombre2(searchResult.getNombre2())
                .sexo(searchResult.getSexo())
                .fechaNacimiento(searchResult.getFechaNacimiento())
                .edad(searchResult.getEdad())
                .mostrarDlgCedula(!(searchResult.isFound() || searchResult.isShowManual()));

        if (searchResult.isFound()) {
            builder.permitirIngresoManual(false)
                    .personaAux(null)
                    .ficha(clearPersonaAux(searchResult.getFicha()));

            PacienteFichaStateService.PatientState state = pacienteFichaStateService.ensureEmpleadoSelEnViewScope(
                    false,
                    searchResult.getEmpleadoSel(),
                    searchResult.getNoPersonaSel(),
                    searchResult.getFicha(),
                    null);
            builder.fromPatientState(state);

            CedulaSearchService.CargoLookupResult cargo = cedulaSearchService.lookupCargo(searchResult.getCedula());
            String cargoDescripcion = resolveCargoDescripcion(cargo, state.getFicha());
            builder.cargoDescripcion(cargoDescripcion)
                    .cargoNoEncontrado(!cargo.isEmptyCedula() && !cargo.isFound());
        } else {
            builder.permitirIngresoManual(searchResult.isShowManual());
        }

        return builder.build();
    }

    public UiFlowResult syncPatientStateAfterStep1(boolean permitirIngresoManual, DatEmpleado empleadoSel,
            Integer noPersonaSel, PersonaAux personaAux, FichaOcupacional ficha) {
        PacienteFichaStateService.PatientState state = pacienteFichaStateService.syncPatientStateAfterStep1(
                permitirIngresoManual,
                empleadoSel,
                noPersonaSel,
                personaAux,
                ficha);
        return UiFlowResult.builder().fromPatientState(state).build();
    }

    public UiFlowResult ensureEmpleadoSelEnViewScope(boolean permitirIngresoManual, DatEmpleado empleadoSel,
            Integer noPersonaSel, FichaOcupacional ficha, PersonaAux personaAux) {
        PacienteFichaStateService.PatientState state = pacienteFichaStateService.ensureEmpleadoSelEnViewScope(
                permitirIngresoManual,
                empleadoSel,
                noPersonaSel,
                ficha,
                personaAux);
        return UiFlowResult.builder().fromPatientState(state).build();
    }

    public UiFlowResult ensurePatientAssignedForFicha(boolean permitirIngresoManual, DatEmpleado empleadoSel,
            Integer noPersonaSel, PersonaAux personaAux, FichaOcupacional ficha) {
        PacienteFichaStateService.PatientState state = pacienteFichaStateService.ensurePatientAssignedForFicha(
                permitirIngresoManual,
                empleadoSel,
                noPersonaSel,
                personaAux,
                ficha);
        return UiFlowResult.builder().fromPatientState(state).build();
    }

    public UiFlowResult asegurarPersonaAuxPersistida(boolean permitirIngresoManual, FichaOcupacional ficha,
            PersonaAux personaAux) {
        PersonaAuxFlowService.EnsurePersonaAuxResult result = personaAuxFlowService.asegurarPersonaAuxPersistida(
                permitirIngresoManual,
                ficha,
                personaAux);

        return UiFlowResult.builder()
                .ficha(result.getFicha())
                .personaAux(result.getPersonaAux())
                .permitirIngresoManual(permitirIngresoManual)
                .build();
    }

    private FichaOcupacional clearPersonaAux(FichaOcupacional ficha) {
        if (ficha != null) {
            ficha.setPersonaAux(null);
        }
        return ficha;
    }

    private String resolveCargoDescripcion(CedulaSearchService.CargoLookupResult cargo, FichaOcupacional ficha) {
        if (ficha == null) {
            return null;
        }

        if (cargo.isEmptyCedula() || !cargo.isFound()) {
            ficha.setCiiu(null);
            return null;
        }

        ficha.setCiiu(cargo.getCargoDescripcion());
        return cargo.getCargoDescripcion();
    }

    public static class UiFlowResult {

        private final FichaOcupacional ficha;
        private final DatEmpleado empleadoSel;
        private final Integer noPersonaSel;
        private final PersonaAux personaAux;
        private final boolean permitirIngresoManual;
        private final boolean mostrarDlgCedula;
        private final boolean mostrarDialogoAux;

        private final boolean found;
        private final boolean showManual;
        private final boolean cargoNoEncontrado;

        private final String cedulaBusqueda;
        private final String apellido1;
        private final String apellido2;
        private final String nombre1;
        private final String nombre2;
        private final String sexo;
        private final java.util.Date fechaNacimiento;
        private final Integer edad;
        private final String noHistoria;
        private final String cargoDescripcion;

        private final List<String> updates;
        private final List<String> scripts;

        private UiFlowResult(Builder builder) {
            this.ficha = builder.ficha;
            this.empleadoSel = builder.empleadoSel;
            this.noPersonaSel = builder.noPersonaSel;
            this.personaAux = builder.personaAux;
            this.permitirIngresoManual = builder.permitirIngresoManual;
            this.mostrarDlgCedula = builder.mostrarDlgCedula;
            this.mostrarDialogoAux = builder.mostrarDialogoAux;
            this.found = builder.found;
            this.showManual = builder.showManual;
            this.cargoNoEncontrado = builder.cargoNoEncontrado;
            this.cedulaBusqueda = builder.cedulaBusqueda;
            this.apellido1 = builder.apellido1;
            this.apellido2 = builder.apellido2;
            this.nombre1 = builder.nombre1;
            this.nombre2 = builder.nombre2;
            this.sexo = builder.sexo;
            this.fechaNacimiento = builder.fechaNacimiento;
            this.edad = builder.edad;
            this.noHistoria = builder.noHistoria;
            this.cargoDescripcion = builder.cargoDescripcion;
            this.updates = Collections.unmodifiableList(new ArrayList<>(builder.updates));
            this.scripts = Collections.unmodifiableList(new ArrayList<>(builder.scripts));
        }

        public static Builder builder() {
            return new Builder();
        }

        public FichaOcupacional getFicha() { return ficha; }
        public DatEmpleado getEmpleadoSel() { return empleadoSel; }
        public Integer getNoPersonaSel() { return noPersonaSel; }
        public PersonaAux getPersonaAux() { return personaAux; }
        public boolean isPermitirIngresoManual() { return permitirIngresoManual; }
        public boolean isMostrarDlgCedula() { return mostrarDlgCedula; }
        public boolean isMostrarDialogoAux() { return mostrarDialogoAux; }
        public boolean isFound() { return found; }
        public boolean isShowManual() { return showManual; }
        public boolean isCargoNoEncontrado() { return cargoNoEncontrado; }
        public String getCedulaBusqueda() { return cedulaBusqueda; }
        public String getApellido1() { return apellido1; }
        public String getApellido2() { return apellido2; }
        public String getNombre1() { return nombre1; }
        public String getNombre2() { return nombre2; }
        public String getSexo() { return sexo; }
        public java.util.Date getFechaNacimiento() { return fechaNacimiento; }
        public Integer getEdad() { return edad; }
        public String getNoHistoria() { return noHistoria; }
        public String getCargoDescripcion() { return cargoDescripcion; }
        public List<String> getUpdates() { return updates; }
        public List<String> getScripts() { return scripts; }

        public static class Builder {
            private FichaOcupacional ficha;
            private DatEmpleado empleadoSel;
            private Integer noPersonaSel;
            private PersonaAux personaAux;
            private boolean permitirIngresoManual;
            private boolean mostrarDlgCedula;
            private boolean mostrarDialogoAux;
            private boolean found;
            private boolean showManual;
            private boolean cargoNoEncontrado;
            private String cedulaBusqueda;
            private String apellido1;
            private String apellido2;
            private String nombre1;
            private String nombre2;
            private String sexo;
            private java.util.Date fechaNacimiento;
            private Integer edad;
            private String noHistoria;
            private String cargoDescripcion;
            private final List<String> updates = new ArrayList<>();
            private final List<String> scripts = new ArrayList<>();

            public Builder fromPatientState(PacienteFichaStateService.PatientState state) {
                if (state == null) {
                    return this;
                }
                this.ficha = state.getFicha();
                this.empleadoSel = state.getEmpleadoSel();
                this.noPersonaSel = state.getNoPersonaSel();
                this.personaAux = state.getPersonaAux();
                this.permitirIngresoManual = state.isPermitirIngresoManual();
                return this;
            }

            public Builder ficha(FichaOcupacional ficha) { this.ficha = ficha; return this; }
            public Builder empleadoSel(DatEmpleado empleadoSel) { this.empleadoSel = empleadoSel; return this; }
            public Builder noPersonaSel(Integer noPersonaSel) { this.noPersonaSel = noPersonaSel; return this; }
            public Builder personaAux(PersonaAux personaAux) { this.personaAux = personaAux; return this; }
            public Builder permitirIngresoManual(boolean permitirIngresoManual) { this.permitirIngresoManual = permitirIngresoManual; return this; }
            public Builder mostrarDlgCedula(boolean mostrarDlgCedula) { this.mostrarDlgCedula = mostrarDlgCedula; return this; }
            public Builder mostrarDialogoAux(boolean mostrarDialogoAux) { this.mostrarDialogoAux = mostrarDialogoAux; return this; }
            public Builder found(boolean found) { this.found = found; return this; }
            public Builder showManual(boolean showManual) { this.showManual = showManual; return this; }
            public Builder cargoNoEncontrado(boolean cargoNoEncontrado) { this.cargoNoEncontrado = cargoNoEncontrado; return this; }
            public Builder cedulaBusqueda(String cedulaBusqueda) { this.cedulaBusqueda = cedulaBusqueda; return this; }
            public Builder apellido1(String apellido1) { this.apellido1 = apellido1; return this; }
            public Builder apellido2(String apellido2) { this.apellido2 = apellido2; return this; }
            public Builder nombre1(String nombre1) { this.nombre1 = nombre1; return this; }
            public Builder nombre2(String nombre2) { this.nombre2 = nombre2; return this; }
            public Builder sexo(String sexo) { this.sexo = sexo; return this; }
            public Builder fechaNacimiento(java.util.Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; return this; }
            public Builder edad(Integer edad) { this.edad = edad; return this; }
            public Builder noHistoria(String noHistoria) { this.noHistoria = noHistoria; return this; }
            public Builder cargoDescripcion(String cargoDescripcion) { this.cargoDescripcion = cargoDescripcion; return this; }
            public Builder addUpdate(String update) { if (update != null) { this.updates.add(update); } return this; }
            public Builder addScript(String script) { if (script != null) { this.scripts.add(script); } return this; }

            public UiFlowResult build() { return new UiFlowResult(this); }
        }
    }
}
