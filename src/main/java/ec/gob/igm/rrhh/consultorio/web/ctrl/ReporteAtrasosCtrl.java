package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.dto.ReporteAtrasoCitaDTO;
import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;
import ec.gob.igm.rrhh.consultorio.service.citas.CitaCatalogoService;
import ec.gob.igm.rrhh.consultorio.service.citas.CitaReporteService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Named("reporteAtrasosCtrl")
@ViewScoped
public class ReporteAtrasosCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CitaReporteService citaReporteService;

    @Inject
    private CitaCatalogoService citaCatalogoService;

    private Date fechaDesde;
    private Date fechaHasta;
    private Long idProfesional;
    private List<CitProfesional> profesionales;
    private List<ReporteAtrasoCitaDTO> atrasos;

    @PostConstruct
    public void init() {
        LocalDate hoy = LocalDate.now();
        fechaDesde = Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant());
        fechaHasta = new Date();
        profesionales = citaCatalogoService.listarProfesionalesActivos(null);
        buscar();
    }

    public void buscar() {
        atrasos = citaReporteService.listarAtrasos(fechaDesde, fechaHasta, idProfesional);
    }

    public void limpiar() {
        idProfesional = null;
        fechaDesde = null;
        fechaHasta = new Date();
        buscar();
    }

    public Date getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(Date fechaDesde) { this.fechaDesde = fechaDesde; }
    public Date getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(Date fechaHasta) { this.fechaHasta = fechaHasta; }
    public Long getIdProfesional() { return idProfesional; }
    public void setIdProfesional(Long idProfesional) { this.idProfesional = idProfesional; }
    public List<CitProfesional> getProfesionales() { return profesionales; }
    public List<ReporteAtrasoCitaDTO> getAtrasos() { return atrasos; }
    public int getTotalAtrasos() { return atrasos == null ? 0 : atrasos.size(); }
}
