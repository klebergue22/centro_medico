package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;

/**
 * Class Step3FormModel: contiene la lógica de la aplicación.
 */
public class Step3FormModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codCie10Ppal;
    private String descCie10Ppal;
    private String dialogDiagnosticoCodigo;
    private String dialogDiagnosticoDescripcion;
    private Integer dialogDiagnosticoIdx;
    private List<ConsultaDiagnostico> listaDiag = new ArrayList<>();

    public String getCodCie10Ppal() { return codCie10Ppal; }
    public void setCodCie10Ppal(String codCie10Ppal) { this.codCie10Ppal = codCie10Ppal; }
    public String getDescCie10Ppal() { return descCie10Ppal; }
    public void setDescCie10Ppal(String descCie10Ppal) { this.descCie10Ppal = descCie10Ppal; }
    public String getDialogDiagnosticoCodigo() { return dialogDiagnosticoCodigo; }
    public void setDialogDiagnosticoCodigo(String dialogDiagnosticoCodigo) { this.dialogDiagnosticoCodigo = dialogDiagnosticoCodigo; }
    public String getDialogDiagnosticoDescripcion() { return dialogDiagnosticoDescripcion; }
    public void setDialogDiagnosticoDescripcion(String dialogDiagnosticoDescripcion) { this.dialogDiagnosticoDescripcion = dialogDiagnosticoDescripcion; }
    public Integer getDialogDiagnosticoIdx() { return dialogDiagnosticoIdx; }
    public void setDialogDiagnosticoIdx(Integer dialogDiagnosticoIdx) { this.dialogDiagnosticoIdx = dialogDiagnosticoIdx; }
    public List<ConsultaDiagnostico> getListaDiag() { return listaDiag; }
    public void setListaDiag(List<ConsultaDiagnostico> listaDiag) { this.listaDiag = listaDiag; }
}
