package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;
import java.util.Date;

public class HistoriaLaboralFormModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String[] hCentroTrabajo;
    private String[] hActividad;
    private String[] hCargo;
    private String[] hEnfermedad;
    private Boolean[] hIncidente;
    private Boolean[] hAccidente;
    private Integer[] hTiempo;
    private Boolean[] hEnfOcupacional;
    private Boolean[] hEnfComun;
    private Boolean[] hEnfProfesional;
    private Boolean[] hOtros;
    private String[] hOtrosCual;
    private Date[] hFecha;
    private String[] hEspecificacion;
    private String[] hObservacion;

    public String[] gethCentroTrabajo() { return hCentroTrabajo; }
    public void sethCentroTrabajo(String[] hCentroTrabajo) { this.hCentroTrabajo = hCentroTrabajo; }
    public String[] gethActividad() { return hActividad; }
    public void sethActividad(String[] hActividad) { this.hActividad = hActividad; }
    public String[] gethCargo() { return hCargo; }
    public void sethCargo(String[] hCargo) { this.hCargo = hCargo; }
    public String[] gethEnfermedad() { return hEnfermedad; }
    public void sethEnfermedad(String[] hEnfermedad) { this.hEnfermedad = hEnfermedad; }
    public Boolean[] gethIncidente() { return hIncidente; }
    public void sethIncidente(Boolean[] hIncidente) { this.hIncidente = hIncidente; }
    public Boolean[] gethAccidente() { return hAccidente; }
    public void sethAccidente(Boolean[] hAccidente) { this.hAccidente = hAccidente; }
    public Integer[] gethTiempo() { return hTiempo; }
    public void sethTiempo(Integer[] hTiempo) { this.hTiempo = hTiempo; }
    public Boolean[] gethEnfOcupacional() { return hEnfOcupacional; }
    public void sethEnfOcupacional(Boolean[] hEnfOcupacional) { this.hEnfOcupacional = hEnfOcupacional; }
    public Boolean[] gethEnfComun() { return hEnfComun; }
    public void sethEnfComun(Boolean[] hEnfComun) { this.hEnfComun = hEnfComun; }
    public Boolean[] gethEnfProfesional() { return hEnfProfesional; }
    public void sethEnfProfesional(Boolean[] hEnfProfesional) { this.hEnfProfesional = hEnfProfesional; }
    public Boolean[] gethOtros() { return hOtros; }
    public void sethOtros(Boolean[] hOtros) { this.hOtros = hOtros; }
    public String[] gethOtrosCual() { return hOtrosCual; }
    public void sethOtrosCual(String[] hOtrosCual) { this.hOtrosCual = hOtrosCual; }
    public Date[] gethFecha() { return hFecha; }
    public void sethFecha(Date[] hFecha) { this.hFecha = hFecha; }
    public String[] gethEspecificacion() { return hEspecificacion; }
    public void sethEspecificacion(String[] hEspecificacion) { this.hEspecificacion = hEspecificacion; }
    public String[] gethObservacion() { return hObservacion; }
    public void sethObservacion(String[] hObservacion) { this.hObservacion = hObservacion; }
}
