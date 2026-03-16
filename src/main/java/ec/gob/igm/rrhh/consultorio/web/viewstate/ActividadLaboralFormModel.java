package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ActividadLaboralFormModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Date> iessFecha;
    private List<Date> fechaAct;
    private List<String> tipoAct;
    private List<String> descAct;
    private List<String> actLabRows;
    private List<String> actLabCentroTrabajo;
    private List<String> actLabActividad;
    private List<String> actLabIncidente;
    private List<Date> actLabFecha;
    private List<String> actLabTiempo;
    private List<Boolean> actLabTrabajoAnterior;
    private List<Boolean> actLabTrabajoActual;
    private List<Boolean> actLabIncidenteChk;
    private List<Boolean> actLabAccidenteChk;
    private List<Boolean> actLabEnfermedadChk;
    private List<Boolean> iessSi;
    private List<Boolean> iessNo;
    private List<String> iessEspecificar;
    private List<String> actLabObservaciones;

    public List<Date> getIessFecha() { return iessFecha; }
    public void setIessFecha(List<Date> iessFecha) { this.iessFecha = iessFecha; }
    public List<Date> getFechaAct() { return fechaAct; }
    public void setFechaAct(List<Date> fechaAct) { this.fechaAct = fechaAct; }
    public List<String> getTipoAct() { return tipoAct; }
    public void setTipoAct(List<String> tipoAct) { this.tipoAct = tipoAct; }
    public List<String> getDescAct() { return descAct; }
    public void setDescAct(List<String> descAct) { this.descAct = descAct; }
    public List<String> getActLabRows() { return actLabRows; }
    public void setActLabRows(List<String> actLabRows) { this.actLabRows = actLabRows; }
    public List<String> getActLabCentroTrabajo() { return actLabCentroTrabajo; }
    public void setActLabCentroTrabajo(List<String> actLabCentroTrabajo) { this.actLabCentroTrabajo = actLabCentroTrabajo; }
    public List<String> getActLabActividad() { return actLabActividad; }
    public void setActLabActividad(List<String> actLabActividad) { this.actLabActividad = actLabActividad; }
    public List<String> getActLabIncidente() { return actLabIncidente; }
    public void setActLabIncidente(List<String> actLabIncidente) { this.actLabIncidente = actLabIncidente; }
    public List<Date> getActLabFecha() { return actLabFecha; }
    public void setActLabFecha(List<Date> actLabFecha) { this.actLabFecha = actLabFecha; }
    public List<String> getActLabTiempo() { return actLabTiempo; }
    public void setActLabTiempo(List<String> actLabTiempo) { this.actLabTiempo = actLabTiempo; }
    public List<Boolean> getActLabTrabajoAnterior() { return actLabTrabajoAnterior; }
    public void setActLabTrabajoAnterior(List<Boolean> actLabTrabajoAnterior) { this.actLabTrabajoAnterior = actLabTrabajoAnterior; }
    public List<Boolean> getActLabTrabajoActual() { return actLabTrabajoActual; }
    public void setActLabTrabajoActual(List<Boolean> actLabTrabajoActual) { this.actLabTrabajoActual = actLabTrabajoActual; }
    public List<Boolean> getActLabIncidenteChk() { return actLabIncidenteChk; }
    public void setActLabIncidenteChk(List<Boolean> actLabIncidenteChk) { this.actLabIncidenteChk = actLabIncidenteChk; }
    public List<Boolean> getActLabAccidenteChk() { return actLabAccidenteChk; }
    public void setActLabAccidenteChk(List<Boolean> actLabAccidenteChk) { this.actLabAccidenteChk = actLabAccidenteChk; }
    public List<Boolean> getActLabEnfermedadChk() { return actLabEnfermedadChk; }
    public void setActLabEnfermedadChk(List<Boolean> actLabEnfermedadChk) { this.actLabEnfermedadChk = actLabEnfermedadChk; }
    public List<Boolean> getIessSi() { return iessSi; }
    public void setIessSi(List<Boolean> iessSi) { this.iessSi = iessSi; }
    public List<Boolean> getIessNo() { return iessNo; }
    public void setIessNo(List<Boolean> iessNo) { this.iessNo = iessNo; }
    public List<String> getIessEspecificar() { return iessEspecificar; }
    public void setIessEspecificar(List<String> iessEspecificar) { this.iessEspecificar = iessEspecificar; }
    public List<String> getActLabObservaciones() { return actLabObservaciones; }
    public void setActLabObservaciones(List<String> actLabObservaciones) { this.actLabObservaciones = actLabObservaciones; }
}
