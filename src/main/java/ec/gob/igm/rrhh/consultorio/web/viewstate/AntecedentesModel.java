package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;
import java.util.Date;

public class AntecedentesModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String antClinicoQuirurgico;
    private String antFamiliares;
    private String antTerapeutica;
    private String antObs;
    private String condicionEspecial;
    private String autorizaTransfusion;
    private String tratamientoHormonal;
    private String tratamientoHormonalCual;
    private String examenReproMasculino;
    private Integer tiempoReproMasculino;
    private String ginecoExamen1;
    private String ginecoTiempo1;
    private String ginecoResultado1;
    private String ginecoExamen2;
    private String ginecoTiempo2;
    private String ginecoResultado2;
    private String ginecoObservacion;
    private Date fum;
    private Integer gestas;
    private Integer partos;
    private Integer cesareas;
    private Integer abortos;
    private String planificacion;
    private String planificacionCual;

    public String getAntClinicoQuirurgico() { return antClinicoQuirurgico; }
    public void setAntClinicoQuirurgico(String v) { this.antClinicoQuirurgico = v; }
    public String getAntFamiliares() { return antFamiliares; }
    public void setAntFamiliares(String v) { this.antFamiliares = v; }
    public String getAntTerapeutica() { return antTerapeutica; }
    public void setAntTerapeutica(String v) { this.antTerapeutica = v; }
    public String getAntObs() { return antObs; }
    public void setAntObs(String v) { this.antObs = v; }
    public String getCondicionEspecial() { return condicionEspecial; }
    public void setCondicionEspecial(String v) { this.condicionEspecial = v; }
    public String getAutorizaTransfusion() { return autorizaTransfusion; }
    public void setAutorizaTransfusion(String v) { this.autorizaTransfusion = v; }
    public String getTratamientoHormonal() { return tratamientoHormonal; }
    public void setTratamientoHormonal(String v) { this.tratamientoHormonal = v; }
    public String getTratamientoHormonalCual() { return tratamientoHormonalCual; }
    public void setTratamientoHormonalCual(String v) { this.tratamientoHormonalCual = v; }
    public String getExamenReproMasculino() { return examenReproMasculino; }
    public void setExamenReproMasculino(String v) { this.examenReproMasculino = v; }
    public Integer getTiempoReproMasculino() { return tiempoReproMasculino; }
    public void setTiempoReproMasculino(Integer v) { this.tiempoReproMasculino = v; }
    public String getGinecoExamen1() { return ginecoExamen1; }
    public void setGinecoExamen1(String v) { this.ginecoExamen1 = v; }
    public String getGinecoTiempo1() { return ginecoTiempo1; }
    public void setGinecoTiempo1(String v) { this.ginecoTiempo1 = v; }
    public String getGinecoResultado1() { return ginecoResultado1; }
    public void setGinecoResultado1(String v) { this.ginecoResultado1 = v; }
    public String getGinecoExamen2() { return ginecoExamen2; }
    public void setGinecoExamen2(String v) { this.ginecoExamen2 = v; }
    public String getGinecoTiempo2() { return ginecoTiempo2; }
    public void setGinecoTiempo2(String v) { this.ginecoTiempo2 = v; }
    public String getGinecoResultado2() { return ginecoResultado2; }
    public void setGinecoResultado2(String v) { this.ginecoResultado2 = v; }
    public String getGinecoObservacion() { return ginecoObservacion; }
    public void setGinecoObservacion(String v) { this.ginecoObservacion = v; }
    public Date getFum() { return fum; }
    public void setFum(Date fum) { this.fum = fum; }
    public Integer getGestas() { return gestas; }
    public void setGestas(Integer gestas) { this.gestas = gestas; }
    public Integer getPartos() { return partos; }
    public void setPartos(Integer partos) { this.partos = partos; }
    public Integer getCesareas() { return cesareas; }
    public void setCesareas(Integer cesareas) { this.cesareas = cesareas; }
    public Integer getAbortos() { return abortos; }
    public void setAbortos(Integer abortos) { this.abortos = abortos; }
    public String getPlanificacion() { return planificacion; }
    public void setPlanificacion(String planificacion) { this.planificacion = planificacion; }
    public String getPlanificacionCual() { return planificacionCual; }
    public void setPlanificacionCual(String planificacionCual) { this.planificacionCual = planificacionCual; }
}
