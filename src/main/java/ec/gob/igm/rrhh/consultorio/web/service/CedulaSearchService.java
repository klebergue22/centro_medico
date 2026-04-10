package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.Date;

import ec.gob.igm.rrhh.consultorio.domain.dto.EmpleadoCargoDTO;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoRhService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;
import ec.gob.igm.rrhh.consultorio.web.util.SnUtils;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
/**
 * Class CedulaSearchService: orquesta la lógica de presentación y flujo web.
 */
public class CedulaSearchService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private EmpleadoService empleadoService;

    @EJB
    private EmpleadoRhService empleadoRhService;

    @EJB
    private PersonaAuxService personaAuxService;

    public CedulaSearchResult search(String cedulaBusqueda, FichaOcupacional ficha, PersonaAux personaAux) {
        final String cedula = normalizeCedulaOrThrow(cedulaBusqueda);

        FichaOcupacional fichaSafe = (ficha != null) ? ficha : new FichaOcupacional();
        PersonaAux personaAuxSafe = (personaAux != null) ? personaAux : new PersonaAux();
        personaAuxSafe.setCedula(cedula);

        DatEmpleado emp = empleadoService.buscarPorCedula(cedula);

        if (emp != null) {
            return buildFoundResult(cedula, emp, fichaSafe, personaAuxSafe);
        }

        PersonaAux personaAuxEncontrada = personaAuxService.findByCedulaConFichaYCertificado(cedula);
        if (personaAuxEncontrada != null) {
            return buildFoundPersonaAuxResult(cedula, fichaSafe, personaAuxEncontrada);
        }

        return buildManualResult(cedula, fichaSafe, personaAuxSafe);
    }

    public CargoLookupResult lookupCargo(String cedulaBusqueda) {
        final String cedula = SnUtils.trimToNull(cedulaBusqueda);
        if (cedula == null) {
            return CargoLookupResult.emptyCedula();
        }

        EmpleadoCargoDTO dto = empleadoRhService.buscarPorCedulaEnVista(cedula);
        String cargo = (dto == null) ? null : SnUtils.trimToNull(dto.getCargoDescrip());
        if (cargo == null) {
            return CargoLookupResult.notFound();
        }
        return CargoLookupResult.found(cargo);
    }

    private CedulaSearchResult buildFoundResult(String cedula, DatEmpleado emp, FichaOcupacional ficha, PersonaAux personaAux) {
        ficha.setNoHistoriaClinica(emp.getNoCedula());
        ficha.setNoArchivo(emp.getNoCedula());
        ficha.setEmpleado(emp);
        ficha.setPersonaAux(null);

        String[] nombresSeparados = separarNombres(emp.getNombres());

        return CedulaSearchResult.found(
                cedula,
                ficha,
                personaAux,
                emp,
                emp.getNoPersona(),
                emp.getPriApellido(),
                emp.getSegApellido(),
                nombresSeparados[0],
                nombresSeparados[1],
                (emp.getSexo() != null) ? emp.getSexo().getCodigo() : null,
                emp.getfNacimiento(),
                calcularEdad(emp.getfNacimiento())
        );
    }

    private String[] separarNombres(String nombresCompletos) {
        String nombresNormalizados = SnUtils.trimToNull(nombresCompletos);
        if (nombresNormalizados == null) {
            return new String[] { null, null };
        }

        String[] partes = nombresNormalizados.split("\\s+", 2);
        String nombre1 = partes.length > 0 ? partes[0] : null;
        String nombre2 = partes.length > 1 ? partes[1] : null;
        return new String[] { nombre1, nombre2 };
    }

    private CedulaSearchResult buildManualResult(String cedula, FichaOcupacional ficha, PersonaAux personaAux) {
        personaAux.setApellido1(null);
        personaAux.setApellido2(null);
        personaAux.setNombre1(null);
        personaAux.setNombre2(null);
        personaAux.setSexo(null);
        personaAux.setFechaNac(null);

        ficha.setNoHistoriaClinica(cedula);
        ficha.setNoArchivo(cedula);

        return CedulaSearchResult.manual(cedula, ficha, personaAux);
    }

    private CedulaSearchResult buildFoundPersonaAuxResult(String cedula, FichaOcupacional ficha, PersonaAux personaAux) {
        ficha.setNoHistoriaClinica(cedula);
        ficha.setNoArchivo(cedula);
        ficha.setEmpleado(null);
        ficha.setPersonaAux(personaAux);

        return CedulaSearchResult.found(
                cedula,
                ficha,
                personaAux,
                null,
                null,
                personaAux.getApellido1(),
                personaAux.getApellido2(),
                personaAux.getNombre1(),
                personaAux.getNombre2(),
                personaAux.getSexo(),
                personaAux.getFechaNac(),
                calcularEdad(personaAux.getFechaNac())
        );
    }

    private int calcularEdad(Date fechaNac) {
        if (fechaNac == null) {
            return 0;
        }

        java.util.Calendar nacimiento = java.util.Calendar.getInstance();
        nacimiento.setTime(fechaNac);

        java.util.Calendar hoy = java.util.Calendar.getInstance();
        int edad = hoy.get(java.util.Calendar.YEAR) - nacimiento.get(java.util.Calendar.YEAR);

        if (hoy.get(java.util.Calendar.DAY_OF_YEAR) < nacimiento.get(java.util.Calendar.DAY_OF_YEAR)) {
            edad--;
        }

        return Math.max(edad, 0);
    }

    private String normalizeCedulaOrThrow(String cedulaBusqueda) {
        String cedula = SnUtils.trimToNull(cedulaBusqueda);
        if (cedula == null) {
            throw new CedulaValidationException("Ingrese una cédula para realizar la búsqueda.");
        }
        return cedula;
    }

    public static class CedulaValidationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public CedulaValidationException(String message) {
            super(message);
        }
    }

    public static class CargoLookupResult {

        private final boolean found;
        private final boolean emptyCedula;
        private final String cargoDescripcion;

        private CargoLookupResult(boolean found, boolean emptyCedula, String cargoDescripcion) {
            this.found = found;
            this.emptyCedula = emptyCedula;
            this.cargoDescripcion = cargoDescripcion;
        }

        public static CargoLookupResult found(String cargoDescripcion) {
            return new CargoLookupResult(true, false, cargoDescripcion);
        }

        public static CargoLookupResult notFound() {
            return new CargoLookupResult(false, false, null);
        }

        public static CargoLookupResult emptyCedula() {
            return new CargoLookupResult(false, true, null);
        }

        public boolean isFound() {
            return found;
        }

        public boolean isEmptyCedula() {
            return emptyCedula;
        }

        public String getCargoDescripcion() {
            return cargoDescripcion;
        }
    }

    public static class CedulaSearchResult {

        private final boolean found;
        private final boolean showManual;
        private final String cedula;
        private final FichaOcupacional ficha;
        private final PersonaAux personaAux;
        private final DatEmpleado empleadoSel;
        private final Integer noPersonaSel;
        private final String apellido1;
        private final String apellido2;
        private final String nombre1;
        private final String nombre2;
        private final String sexo;
        private final Date fechaNacimiento;
        private final Integer edad;

        private CedulaSearchResult(boolean found, boolean showManual, String cedula, FichaOcupacional ficha, PersonaAux personaAux,
                DatEmpleado empleadoSel, Integer noPersonaSel, String apellido1, String apellido2, String nombre1, String nombre2,
                String sexo, Date fechaNacimiento, Integer edad) {
            this.found = found;
            this.showManual = showManual;
            this.cedula = cedula;
            this.ficha = ficha;
            this.personaAux = personaAux;
            this.empleadoSel = empleadoSel;
            this.noPersonaSel = noPersonaSel;
            this.apellido1 = apellido1;
            this.apellido2 = apellido2;
            this.nombre1 = nombre1;
            this.nombre2 = nombre2;
            this.sexo = sexo;
            this.fechaNacimiento = fechaNacimiento;
            this.edad = edad;
        }

        public static CedulaSearchResult found(String cedula, FichaOcupacional ficha, PersonaAux personaAux,
                DatEmpleado empleadoSel, Integer noPersonaSel, String apellido1, String apellido2,
                String nombre1, String nombre2, String sexo, Date fechaNacimiento, Integer edad) {
            return new CedulaSearchResult(true, false, cedula, ficha, personaAux, empleadoSel, noPersonaSel,
                    apellido1, apellido2, nombre1, nombre2, sexo, fechaNacimiento, edad);
        }

        public static CedulaSearchResult manual(String cedula, FichaOcupacional ficha, PersonaAux personaAux) {
            return new CedulaSearchResult(false, true, cedula, ficha, personaAux, null, null,
                    null, null, null, null, null, null, null);
        }

        public static CedulaSearchResult notFoundNoManual() {
            return new CedulaSearchResult(false, false, null, null, null, null, null,
                    null, null, null, null, null, null, null);
        }

        public boolean isFound() {
            return found;
        }

        public boolean isShowManual() {
            return showManual;
        }

        public String getCedula() {
            return cedula;
        }

        public FichaOcupacional getFicha() {
            return ficha;
        }

        public PersonaAux getPersonaAux() {
            return personaAux;
        }

        public DatEmpleado getEmpleadoSel() {
            return empleadoSel;
        }

        public Integer getNoPersonaSel() {
            return noPersonaSel;
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

        public Integer getEdad() {
            return edad;
        }
    }
}
