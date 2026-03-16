package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;

public class AntecedentesFormModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String antClinicoQuirurgico;
    private String antFamiliares;
    private String antTerapeutica;
    private String antObs;
    private String condicionEspecial;
    private String autorizaTransfusion;
    private String tratamientoHormonal;
    private String tratamientoHormonalCual;

    public String getAntClinicoQuirurgico() { return antClinicoQuirurgico; }
    public void setAntClinicoQuirurgico(String antClinicoQuirurgico) { this.antClinicoQuirurgico = antClinicoQuirurgico; }
    public String getAntFamiliares() { return antFamiliares; }
    public void setAntFamiliares(String antFamiliares) { this.antFamiliares = antFamiliares; }
    public String getAntTerapeutica() { return antTerapeutica; }
    public void setAntTerapeutica(String antTerapeutica) { this.antTerapeutica = antTerapeutica; }
    public String getAntObs() { return antObs; }
    public void setAntObs(String antObs) { this.antObs = antObs; }
    public String getCondicionEspecial() { return condicionEspecial; }
    public void setCondicionEspecial(String condicionEspecial) { this.condicionEspecial = condicionEspecial; }
    public String getAutorizaTransfusion() { return autorizaTransfusion; }
    public void setAutorizaTransfusion(String autorizaTransfusion) { this.autorizaTransfusion = autorizaTransfusion; }
    public String getTratamientoHormonal() { return tratamientoHormonal; }
    public void setTratamientoHormonal(String tratamientoHormonal) { this.tratamientoHormonal = tratamientoHormonal; }
    public String getTratamientoHormonalCual() { return tratamientoHormonalCual; }
    public void setTratamientoHormonalCual(String tratamientoHormonalCual) { this.tratamientoHormonalCual = tratamientoHormonalCual; }
}
