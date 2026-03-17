package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;

/**
 * Class Step2FormModel: contiene la lógica de la aplicación.
 */
public class Step2FormModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private FichaRiesgo fichaRiesgo;
    private List<String> actividadesLab = new ArrayList<>();
    private List<String> riskCols = new ArrayList<>();
    private Map<String, Boolean> riesgos = new LinkedHashMap<>();
    private Map<String, String> otrosRiesgos = new LinkedHashMap<>();
    private List<String> medidasPreventivas = new ArrayList<>();

    public FichaRiesgo getFichaRiesgo() { return fichaRiesgo; }
    public void setFichaRiesgo(FichaRiesgo fichaRiesgo) { this.fichaRiesgo = fichaRiesgo; }
    public List<String> getActividadesLab() { return actividadesLab; }
    public void setActividadesLab(List<String> actividadesLab) { this.actividadesLab = actividadesLab; }
    public List<String> getRiskCols() { return riskCols; }
    public void setRiskCols(List<String> riskCols) { this.riskCols = riskCols; }
    public Map<String, Boolean> getRiesgos() { return riesgos; }
    public void setRiesgos(Map<String, Boolean> riesgos) { this.riesgos = riesgos; }
    public Map<String, String> getOtrosRiesgos() { return otrosRiesgos; }
    public void setOtrosRiesgos(Map<String, String> otrosRiesgos) { this.otrosRiesgos = otrosRiesgos; }
    public List<String> getMedidasPreventivas() { return medidasPreventivas; }
    public void setMedidasPreventivas(List<String> medidasPreventivas) { this.medidasPreventivas = medidasPreventivas; }
}
