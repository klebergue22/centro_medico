package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;

public class SignosVitalesFormModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private Double peso;
    private Double tallaCm;
    private Double imc;
    private Double temp;
    private String paStr;
    private Integer fc;
    private Integer fr;
    private Integer satO2;
    private Double perimetroAbd;

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }
    public Double getTallaCm() { return tallaCm; }
    public void setTallaCm(Double tallaCm) { this.tallaCm = tallaCm; }
    public Double getImc() { return imc; }
    public void setImc(Double imc) { this.imc = imc; }
    public Double getTemp() { return temp; }
    public void setTemp(Double temp) { this.temp = temp; }
    public String getPaStr() { return paStr; }
    public void setPaStr(String paStr) { this.paStr = paStr; }
    public Integer getFc() { return fc; }
    public void setFc(Integer fc) { this.fc = fc; }
    public Integer getFr() { return fr; }
    public void setFr(Integer fr) { this.fr = fr; }
    public Integer getSatO2() { return satO2; }
    public void setSatO2(Integer satO2) { this.satO2 = satO2; }
    public Double getPerimetroAbd() { return perimetroAbd; }
    public void setPerimetroAbd(Double perimetroAbd) { this.perimetroAbd = perimetroAbd; }
}
