package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.enums.EstadoContrato;
import ec.gob.igm.rrhh.consultorio.domain.enums.TipoContrato;
import ec.gob.igm.rrhh.consultorio.domain.model.Contratacion;
import ec.gob.igm.rrhh.consultorio.domain.model.ContratacionId;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.service.ContratacionService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.primefaces.PrimeFaces;

@Named("contratacionController")
@ViewScoped
public class ContratacionController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient ContratacionService contratacionService;

    private Long filtroNoPersona;
    private List<Contratacion> lista;
    private Contratacion seleccionado;
    private String nombreEmpleado;

    @PostConstruct
    public void init() {
        lista = Collections.emptyList();
    }

    public void buscar() {
        if (filtroNoPersona == null) {
            lista = Collections.emptyList();
            addWarn("Ingrese el número de persona para buscar.");
            return;
        }

        lista = contratacionService.listarPorPersona(filtroNoPersona);
        DatEmpleado empleado = contratacionService.obtenerEmpleado(filtroNoPersona);
        nombreEmpleado = empleado != null ? empleado.getNombreC() : String.valueOf(filtroNoPersona);

        if (lista == null || lista.isEmpty()) {
            PrimeFaces.current().executeScript("PF('dlgCrear').show();");
            addWarn("No se encontraron contratos para el número de persona ingresado.");
        }
    }

    public void nuevoDesdeBoton() {
        if (filtroNoPersona == null) {
            addWarn("Ingrese el número de persona para crear un contrato.");
            markValidationFailed();
            return;
        }

        DatEmpleado empleado = contratacionService.obtenerEmpleado(filtroNoPersona);
        if (empleado == null) {
            addWarn("No existe empleado para el número de persona ingresado.");
            markValidationFailed();
            return;
        }

        seleccionado = nuevoContratoBase(empleado, filtroNoPersona);
    }

    public void crearNuevoDesdeDialogo() {
        nuevoDesdeBoton();
    }

    public void prepararEdicion(Contratacion c) {
        if (c == null) {
            addWarn("No existe contrato para editar.");
            markValidationFailed();
            return;
        }
        seleccionado = c;
    }

    public void guardar() {
        if (seleccionado == null) {
            addWarn("No hay contrato seleccionado para guardar.");
            markValidationFailed();
            return;
        }

        contratacionService.guardar(seleccionado);

        Long noPersona = seleccionado.getId() != null ? seleccionado.getId().getNoPersona() : filtroNoPersona;
        if (noPersona != null) {
            filtroNoPersona = noPersona;
            lista = contratacionService.listarPorPersona(noPersona);
        }

        addInfo("Contratación guardada correctamente.");
    }

    private Contratacion nuevoContratoBase(DatEmpleado empleado, Long noPersona) {
        Contratacion c = new Contratacion();
        ContratacionId id = new ContratacionId();
        id.setNoPersona(noPersona);
        c.setId(id);
        c.setEmpleado(empleado);
        c.setEstado(EstadoContrato.PENDIENTE);
        return c;
    }

    private void markValidationFailed() {
        PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
    }

    private void addInfo(String summary) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null));
    }

    private void addWarn(String summary) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_WARN, summary, null));
    }

    public Long getFiltroNoPersona() {
        return filtroNoPersona;
    }

    public void setFiltroNoPersona(Long filtroNoPersona) {
        this.filtroNoPersona = filtroNoPersona;
    }

    public List<Contratacion> getLista() {
        return lista;
    }

    public Contratacion getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Contratacion seleccionado) {
        this.seleccionado = seleccionado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public List<EstadoContrato> getEstadosEnum() {
        return Arrays.asList(EstadoContrato.values());
    }

    public List<TipoContrato> getTiposContratoEnum() {
        return Arrays.asList(TipoContrato.values());
    }
}
