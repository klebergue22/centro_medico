package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.Date;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;
import ec.gob.igm.rrhh.consultorio.web.util.SnUtils;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PersonaAuxFlowService implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String USUARIO_SISTEMA = "SISTEMA";

    @EJB
    private PersonaAuxService personaAuxService;

    public OpenManualResult abrirPersonaAuxManual(String cedulaBusqueda, PersonaAux personaAuxActual) {
        PersonaAux personaAux = (personaAuxActual != null) ? personaAuxActual : new PersonaAux();

        String cedula = SnUtils.trimToNull(cedulaBusqueda);
        if (cedula != null && SnUtils.trimToNull(personaAux.getCedula()) == null) {
            personaAux.setCedula(cedula);
        }

        return new OpenManualResult(personaAux, true);
    }

    public SavePersonaAuxResult guardarPersonaAuxYUsar(PersonaAux personaAuxActual) {
        if (personaAuxActual == null) {
            throw new PersonaAuxValidationException("No existe información de la persona para guardar.");
        }

        validarCamposObligatorios(personaAuxActual);
        normalizarDatos(personaAuxActual);
        PersonaAux personaGuardada = persistirConAuditoria(personaAuxActual);

        return SavePersonaAuxResult.of(personaGuardada);
    }

    public ManualPreparationResult prepararIngresoManual(String cedulaBusqueda, PersonaAux personaAuxActual) {
        String cedula = SnUtils.trimToNull(cedulaBusqueda);
        if (cedula == null) {
            throw new PersonaAuxValidationException("Ingrese la cédula antes de continuar.");
        }

        PersonaAux personaAux = (personaAuxActual != null) ? personaAuxActual : new PersonaAux();
        personaAux.setCedula(cedula);
        personaAux.setApellido1(null);
        personaAux.setApellido2(null);
        personaAux.setNombre1(null);
        personaAux.setNombre2(null);
        personaAux.setSexo(null);
        personaAux.setFechaNac(null);

        return new ManualPreparationResult(personaAux, true);
    }

    public EnsurePersonaAuxResult asegurarPersonaAuxPersistida(boolean permitirIngresoManual, FichaOcupacional ficha,
            PersonaAux personaAuxActual) {
        if (!permitirIngresoManual) {
            if (ficha != null) {
                ficha.setPersonaAux(null);
            }
            return new EnsurePersonaAuxResult(ficha, null);
        }

        if (personaAuxActual == null) {
            return new EnsurePersonaAuxResult(ficha, null);
        }

        PersonaAux personaAux = personaAuxActual;
        if (personaAux.getIdPersonaAux() == null) {
            personaAux = personaAuxService.guardar(personaAux);
        }

        if (ficha != null) {
            ficha.setPersonaAux(personaAux);
        }

        return new EnsurePersonaAuxResult(ficha, personaAux);
    }

    private void validarCamposObligatorios(PersonaAux personaAux) {
        if (SnUtils.trimToNull(personaAux.getCedula()) == null
                || SnUtils.trimToNull(personaAux.getApellido1()) == null
                || SnUtils.trimToNull(personaAux.getNombre1()) == null
                || SnUtils.trimToNull(personaAux.getSexo()) == null
                || personaAux.getFechaNac() == null) {
            throw new PersonaAuxValidationException(
                    "Cédula, primer apellido, primer nombre, sexo y fecha de nacimiento son obligatorios.");
        }
    }

    private void normalizarDatos(PersonaAux personaAux) {
        personaAux.setCedula(SnUtils.trimToNull(personaAux.getCedula()));
        personaAux.setApellido1(normalizarMayusculas(personaAux.getApellido1()));
        personaAux.setApellido2(normalizarMayusculas(personaAux.getApellido2()));
        personaAux.setNombre1(normalizarMayusculas(personaAux.getNombre1()));
        personaAux.setNombre2(normalizarMayusculas(personaAux.getNombre2()));
        personaAux.setSexo(normalizarMayusculas(personaAux.getSexo()));
    }

    private String normalizarMayusculas(String texto) {
        String valor = SnUtils.trimToNull(texto);
        return (valor == null) ? null : valor.toUpperCase();
    }

    private PersonaAux persistirConAuditoria(PersonaAux personaAux) {
        Date ahora = new Date();
        if (personaAux.getIdPersonaAux() == null) {
            personaAux.setEstado("A");
            personaAux.setFechaCreacion(ahora);
            personaAux.setUsrCreacion(USUARIO_SISTEMA);
        } else {
            personaAux.setFechaActualizacion(ahora);
            personaAux.setUsrActualizacion(USUARIO_SISTEMA);
        }
        return personaAuxService.guardar(personaAux);
    }

    public static class PersonaAuxValidationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public PersonaAuxValidationException(String message) {
            super(message);
        }
    }

    public static class OpenManualResult {

        private final PersonaAux personaAux;
        private final boolean mostrarDialogoAux;

        public OpenManualResult(PersonaAux personaAux, boolean mostrarDialogoAux) {
            this.personaAux = personaAux;
            this.mostrarDialogoAux = mostrarDialogoAux;
        }

        public PersonaAux getPersonaAux() {
            return personaAux;
        }

        public boolean isMostrarDialogoAux() {
            return mostrarDialogoAux;
        }
    }

    public static class ManualPreparationResult {

        private final PersonaAux personaAux;
        private final boolean permitirIngresoManual;

        public ManualPreparationResult(PersonaAux personaAux, boolean permitirIngresoManual) {
            this.personaAux = personaAux;
            this.permitirIngresoManual = permitirIngresoManual;
        }

        public PersonaAux getPersonaAux() {
            return personaAux;
        }

        public boolean isPermitirIngresoManual() {
            return permitirIngresoManual;
        }
    }

    public static class EnsurePersonaAuxResult {

        private final FichaOcupacional ficha;
        private final PersonaAux personaAux;

        public EnsurePersonaAuxResult(FichaOcupacional ficha, PersonaAux personaAux) {
            this.ficha = ficha;
            this.personaAux = personaAux;
        }

        public FichaOcupacional getFicha() {
            return ficha;
        }

        public PersonaAux getPersonaAux() {
            return personaAux;
        }
    }

    public static class SavePersonaAuxResult {

        private final PersonaAux personaAux;
        private final String cedulaBusqueda;
        private final String apellido1;
        private final String apellido2;
        private final String nombre1;
        private final String nombre2;
        private final String sexo;
        private final Date fechaNacimiento;
        private final String noHistoria;
        private final boolean mostrarDialogoAux;
        private final boolean mostrarDialogoCedula;
        private final boolean permitirIngresoManual;

        private SavePersonaAuxResult(PersonaAux personaAux, String cedulaBusqueda, String apellido1, String apellido2,
                String nombre1, String nombre2, String sexo, Date fechaNacimiento, String noHistoria,
                boolean mostrarDialogoAux, boolean mostrarDialogoCedula, boolean permitirIngresoManual) {
            this.personaAux = personaAux;
            this.cedulaBusqueda = cedulaBusqueda;
            this.apellido1 = apellido1;
            this.apellido2 = apellido2;
            this.nombre1 = nombre1;
            this.nombre2 = nombre2;
            this.sexo = sexo;
            this.fechaNacimiento = fechaNacimiento;
            this.noHistoria = noHistoria;
            this.mostrarDialogoAux = mostrarDialogoAux;
            this.mostrarDialogoCedula = mostrarDialogoCedula;
            this.permitirIngresoManual = permitirIngresoManual;
        }

        public static SavePersonaAuxResult of(PersonaAux personaAux) {
            return new SavePersonaAuxResult(
                    personaAux,
                    personaAux.getCedula(),
                    personaAux.getApellido1(),
                    personaAux.getApellido2(),
                    personaAux.getNombre1(),
                    personaAux.getNombre2(),
                    personaAux.getSexo(),
                    personaAux.getFechaNac(),
                    personaAux.getCedula(),
                    false,
                    false,
                    false);
        }

        public PersonaAux getPersonaAux() {
            return personaAux;
        }

        public String getCedulaBusqueda() {
            return cedulaBusqueda;
        }

        public String getApellido1() {
            return apellido1;
        }

        public String getApellido2() {
            return apellido2;
        }

        public String getNombre1() {
            return nombre1;
        }

        public String getNombre2() {
            return nombre2;
        }

        public String getSexo() {
            return sexo;
        }

        public Date getFechaNacimiento() {
            return fechaNacimiento;
        }

        public String getNoHistoria() {
            return noHistoria;
        }

        public boolean isMostrarDialogoAux() {
            return mostrarDialogoAux;
        }

        public boolean isMostrarDialogoCedula() {
            return mostrarDialogoCedula;
        }

        public boolean isPermitirIngresoManual() {
            return permitirIngresoManual;
        }
    }
}
