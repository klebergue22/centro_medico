package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.dto.HistorialFichaCertificadoDTO;
import ec.gob.igm.rrhh.consultorio.domain.enums.EstadoCivil;
import ec.gob.igm.rrhh.consultorio.domain.enums.GrupoSangre;
import ec.gob.igm.rrhh.consultorio.domain.enums.Sexo;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.service.AuditoriaConsultorioService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;
import ec.gob.igm.rrhh.consultorio.service.SeguridadAccesoService;
import ec.gob.igm.rrhh.consultorio.web.service.UserContextService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.primefaces.PrimeFaces;

@Named("empleadosController")
@ViewScoped
public class EmpleadosController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient EmpleadoService empleadoService;

    @Inject
    private transient FichaOcupacionalService fichaOcupacionalService;
    @Inject
    private transient UserContextService userContextService;
    @Inject
    private transient SeguridadAccesoService seguridadAccesoService;
    @Inject
    private transient AuditoriaConsultorioService auditoriaConsultorioService;

    private List<DatEmpleado> lista;
    private DatEmpleado seleccionado;

    @PostConstruct
    public void init() {
        preRenderView();
        if (seleccionado == null) {
            seleccionado = new DatEmpleado();
        }
    }

    public void preRenderView() {
        lista = empleadoService.listarTodos();
    }

    public void nuevo() {
        seleccionado = new DatEmpleado();
    }

    public String guardar() {
        if (seleccionado == null) {
            addWarn("No hay un empleado seleccionado para guardar.");
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
            return null;
        }

        if (isBlank(seleccionado.getNoCedula())) {
            addWarn("La cédula es obligatoria.");
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
            return null;
        }

        DatEmpleado existente = empleadoService.buscarPorCedula(seleccionado.getNoCedula());
        if (existsWithDifferentId(existente, seleccionado)) {
            addWarn("Ya existe un empleado con esa cédula.");
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
            return null;
        }

        empleadoService.guardar(seleccionado);
        preRenderView();
        addInfo("Empleado guardado correctamente.");
        return null;
    }

    public void eliminar(Integer noPersona) {
        empleadoService.eliminar(noPersona);
        preRenderView();
        addInfo("Empleado eliminado correctamente.");
    }

    public void debugLog() {
        addInfo("DEBUG: noPersona=" + (seleccionado != null ? seleccionado.getNoPersona() : null));
    }

    public List<HistorialFichaCertificadoDTO> obtenerHistorialPorCedula(String cedula) {
        if (isBlank(cedula)) {
            return Collections.emptyList();
        }
        List<HistorialFichaCertificadoDTO> historial = fichaOcupacionalService.listarHistorialPorCedula(cedula);
        registrarConsultaReporte(cedula, historial.size());
        historial.forEach(item -> {
            item.setUltimaAccion(Objects.requireNonNullElse(item.getEstado(), "N/D"));
            item.setFechaUltimaAccion(firstNonNull(item.getFechaEmision(), item.getFechaEvaluacion()));
        });
        return historial;
    }

    public void visualizarFicha(HistorialFichaCertificadoDTO historial) {
        if (historial == null || historial.getIdFicha() == null) {
            addWarn("No se pudo identificar la ficha seleccionada.");
            return;
        }
        redirectTo("/pages/ficha/fichaPrint.xhtml?idFicha=" + historial.getIdFicha());
    }

    public void visualizarCertificado(HistorialFichaCertificadoDTO historial) {
        if (historial == null || historial.getIdFicha() == null) {
            addWarn("No se pudo identificar el certificado seleccionado.");
            return;
        }
        redirectTo("/pages/step4.xhtml?idFicha=" + historial.getIdFicha());
    }

    public void editarYRegenerar(HistorialFichaCertificadoDTO historial) {
        if (historial == null || historial.getIdFicha() == null) {
            addWarn("No se pudo identificar la ficha seleccionada.");
            return;
        }
        redirectTo("/pages/centroMedico.xhtml?idFicha=" + historial.getIdFicha());
    }

    public void validarMayorDeEdad(FacesContext ctx, UIComponent component, Object value) {
        if (!(value instanceof Date fechaNacimiento)) {
            return;
        }

        Date hoy = new Date();
        long diffMillis = hoy.getTime() - fechaNacimiento.getTime();
        long years = diffMillis / (1000L * 60 * 60 * 24 * 365);

        if (years < 18) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Validación", "El empleado debe ser mayor de edad."));
        }
    }

    public List<GrupoSangre> getGruposSangre() {
        return Arrays.asList(GrupoSangre.values());
    }

    public List<Sexo> getSexos() {
        return Arrays.asList(Sexo.values());
    }

    public List<EstadoCivil> getEstadosCiviles() {
        return Arrays.asList(EstadoCivil.values());
    }

    public Date getFechaMaxNacimiento() {
        return new Date();
    }

    public List<DatEmpleado> getLista() {
        return lista;
    }

    public DatEmpleado getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(DatEmpleado seleccionado) {
        this.seleccionado = seleccionado;
    }

    private boolean existsWithDifferentId(DatEmpleado existente, DatEmpleado actual) {
        return existente != null && (actual.getNoPersona() == null || !actual.getNoPersona().equals(existente.getNoPersona()));
    }

    private Date firstNonNull(Date d1, Date d2) {
        return d1 != null ? d1 : d2;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void addInfo(String summary) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null));
    }

    private void addWarn(String summary) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, summary, null));
    }

    private void redirectTo(String target) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String contextPath = ctx.getExternalContext().getRequestContextPath();
        try {
            ctx.getExternalContext().redirect(contextPath + target);
            ctx.responseComplete();
        } catch (IOException e) {
            addWarn("No fue posible redireccionar a la pantalla solicitada.");
        }
    }

    private void registrarConsultaReporte(String cedula, int totalRegistros) {
        String user = userContextService.resolveCurrentUser();
        String detalle = "Consulta historial por cédula " + cedula + ". Registros=" + totalRegistros;
        try {
            seguridadAccesoService.registrarEvento(
                    null,
                    user,
                    "CONSULTA_REPORTE",
                    true,
                    detalle
            );
        } catch (RuntimeException e) {
            auditoriaConsultorioService.registrar(
                    "EMPLEADOS",
                    user,
                    "CONSULTA_REPORTE",
                    "FICHA_OCUPACIONAL",
                    "NO_CEDULA",
                    detalle
            );
        }
    }

}
