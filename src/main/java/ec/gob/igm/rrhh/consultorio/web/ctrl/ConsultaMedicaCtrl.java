package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;
import ec.gob.igm.rrhh.consultorio.service.ConsultaMedicaService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.SignosVitalesService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.service.Cie10LookupService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.primefaces.event.SelectEvent;

@Named("consultaMedicaCtrl")
@ViewScoped
public class ConsultaMedicaCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient EmpleadoService empleadoService;
    @Inject
    private transient ConsultaMedicaService consultaMedicaService;
    @Inject
    private transient CentroMedicoPdfFacade centroMedicoPdfFacade;
    @Inject
    private transient Cie10LookupService cie10LookupService;
    @Inject
    private transient Cie10Service cie10Service;
    @Inject
    private transient SignosVitalesService signosVitalesService;

    private String cedulaBusqueda;
    private DatEmpleado empleado;
    private ConsultaMedica consulta;
    private List<ConsultaDiagnostico> diagnosticos;
    private List<RecetaItem> recetas;
    private Date vigenciaReceta;
    private String recomendaciones;
    private String signosAlarma;
    private String alergias;
    private String tokenPdf;
    private Date fechaNacimientoPaciente;
    private SignosVitales signosModel;
    private String paStr;

    @PostConstruct
    public void init() {
        consulta = new ConsultaMedica();
        consulta.setFechaConsulta(new Date());
        consulta.setEstado("ACTIVO");
        consulta.setDiagnosticos(new ArrayList<>());
        diagnosticos = new ArrayList<>();
        recetas = new ArrayList<>();
        vigenciaReceta = new Date();
        signosModel = new SignosVitales();
        agregarDiagnostico();
        agregarReceta();
    }

    public void buscarPorCedula() {
        if (cedulaBusqueda == null || cedulaBusqueda.isBlank()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Cédula requerida", "Ingrese la cédula del paciente.");
            return;
        }
        empleado = empleadoService.buscarPorCedula(cedulaBusqueda.trim());
        if (empleado == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Sin resultados", "No existe un empleado con esa cédula.");
            return;
        }
        consulta.setEmpleado(empleado);
        fechaNacimientoPaciente = empleado.getfNacimiento();
        alergias = empleado.getAlergia();
        addMessage(FacesMessage.SEVERITY_INFO, "Paciente cargado", empleado.getNombreC());
    }

    public void agregarDiagnostico() {
        diagnosticos.add(new ConsultaDiagnostico());
    }

    public void eliminarDiagnostico(int index) {
        if (index >= 0 && index < diagnosticos.size()) {
            diagnosticos.remove(index);
        }
        if (diagnosticos.isEmpty()) {
            agregarDiagnostico();
        }
    }

    public void agregarReceta() {
        recetas.add(new RecetaItem());
    }

    public void eliminarReceta(int index) {
        if (index >= 0 && index < recetas.size()) {
            recetas.remove(index);
        }
        if (recetas.isEmpty()) {
            agregarReceta();
        }
    }

    public void guardarConsulta() {
        if (consulta.getEmpleado() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Paciente requerido", "Busque un paciente antes de guardar.");
            return;
        }

        List<ConsultaDiagnostico> limpios = limpiarDiagnosticos();
        if (limpios.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Diagnóstico requerido",
                    "Debe ingresar al menos un diagnóstico válido antes de guardar.");
            return;
        }

        normalizarTipoDiagnostico(limpios);
        consulta.setDiagnosticos(limpios);
        persistirAlergiasEmpleado();
        persistirSignosVitales();
        consultaMedicaService.guardar(consulta, "WEB");
        addMessage(FacesMessage.SEVERITY_INFO, "Consulta guardada", "Se registró la consulta médica.");
    }

    private List<ConsultaDiagnostico> limpiarDiagnosticos() {
        List<ConsultaDiagnostico> limpios = new ArrayList<>();
        Set<String> codigosUsados = new HashSet<>();
        int duplicados = 0;

        for (ConsultaDiagnostico d : diagnosticos) {
            if (d == null) {
                continue;
            }
            String codigo = normalizarCodigo(d.getCodigo());
            if (codigo == null && isBlank(d.getDescripcion())) {
                continue;
            }
            if (codigo != null && !codigosUsados.add(codigo)) {
                duplicados++;
                continue;
            }

            d.setCodigo(codigo);
            d.setConsulta(consulta);
            d.setEstado("ACTIVO");
            limpios.add(d);
        }

        if (duplicados > 0) {
            addMessage(FacesMessage.SEVERITY_WARN, "Diagnósticos duplicados omitidos",
                    "Se omitieron " + duplicados + " diagnóstico(s) repetido(s) por código CIE10.");
        }

        return limpios;
    }

    private void normalizarTipoDiagnostico(List<ConsultaDiagnostico> lista) {
        if (lista == null || lista.isEmpty()) {
            return;
        }
        int idxPrincipal = -1;
        for (int i = 0; i < lista.size(); i++) {
            ConsultaDiagnostico d = lista.get(i);
            if (d == null) {
                continue;
            }
            String tipo = normalizarTipo(d.getTipoDiag());
            d.setTipoDiag(tipo);
            if ("P".equals(tipo) && idxPrincipal < 0) {
                idxPrincipal = i;
            }
        }

        if (idxPrincipal < 0) {
            idxPrincipal = 0;
        }

        for (ConsultaDiagnostico d : lista) {
            if (d == null) {
                continue;
            }
            d.setTipoDiag("S");
        }
        lista.get(idxPrincipal).setTipoDiag("P");
    }

    private String normalizarTipo(String tipo) {
        if (isBlank(tipo)) {
            return "S";
        }
        String value = tipo.trim().toUpperCase();
        return ("P".equals(value) || "D".equals(value)) ? "P" : "S";
    }

    private String normalizarCodigo(String codigo) {
        if (isBlank(codigo)) {
            return null;
        }
        return codigo.trim().toUpperCase();
    }

    private void persistirAlergiasEmpleado() {
        if (empleado == null) {
            return;
        }
        String alergiasNormalizadas = alergias == null ? null : alergias.trim();
        if (alergiasNormalizadas != null && alergiasNormalizadas.isEmpty()) {
            alergiasNormalizadas = null;
        }
        if (java.util.Objects.equals(empleado.getAlergia(), alergiasNormalizadas)) {
            return;
        }
        empleado.setAlergia(alergiasNormalizadas);
        empleadoService.guardar(empleado);
    }

    private void persistirSignosVitales() {
        if (!tieneSignosVitalesIngresados()) {
            consulta.setSignos(null);
            return;
        }
        parsearPresionArterial();
        SignosVitales guardados = signosVitalesService.guardar(signosModel);
        consulta.setSignos(guardados);
    }

    private boolean tieneSignosVitalesIngresados() {
        return signosModel != null
                && (signosModel.getTemperaturaC() != null
                || !isBlank(paStr)
                || signosModel.getFrecuenciaCard() != null
                || signosModel.getFrecuenciaResp() != null
                || signosModel.getSatO2() != null
                || signosModel.getPesoKg() != null
                || signosModel.getTallaM() != null
                || signosModel.getPerimetroAbdCm() != null);
    }

    private void parsearPresionArterial() {
        if (isBlank(paStr)) {
            signosModel.setPaSistolica(null);
            signosModel.setPaDiastolica(null);
            return;
        }
        String[] partes = paStr.trim().split("/");
        if (partes.length != 2) {
            return;
        }
        try {
            signosModel.setPaSistolica(Integer.valueOf(partes[0].trim()));
            signosModel.setPaDiastolica(Integer.valueOf(partes[1].trim()));
        } catch (NumberFormatException ignored) {
            // se conserva el texto ingresado por el usuario en paStr
        }
    }

    public List<String> completarDiagnosticoCodigo(String query) {
        return cie10LookupService.completarFilaPorCodigo(query);
    }

    public List<String> completarDiagnosticoDescripcion(String query) {
        return cie10LookupService.completarFilaPorDescripcion(query, 20);
    }

    public void onDiagnosticoCodigoSelect(SelectEvent<String> event) {
        ConsultaDiagnostico diag = resolveDiagFromEvent(event != null ? event.getComponent() : null);
        if (diag == null || event == null) {
            return;
        }
        String codigo = cie10LookupService.extraerCodigoDeSugerencia(event.getObject());
        diag.setCodigo(codigo);
        var cie = cie10Service.buscarPorCodigo(codigo);
        if (cie != null) {
            diag.setDescripcion(cie.getDescripcion());
            diag.setCie10(cie);
        }
    }

    public void onDiagnosticoDescripcionSelect(SelectEvent<String> event) {
        ConsultaDiagnostico diag = resolveDiagFromEvent(event != null ? event.getComponent() : null);
        if (diag == null || event == null) {
            return;
        }
        String descripcion = event.getObject();
        diag.setDescripcion(descripcion);
        var cie = cie10Service.buscarPrimeroPorDescripcion(descripcion);
        if (cie != null) {
            diag.setCodigo(cie.getCodigo());
            diag.setCie10(cie);
        }
    }

    public void onDiagnosticoCodigoBlur(AjaxBehaviorEvent event) {
        ConsultaDiagnostico diag = resolveDiagFromEvent(event != null ? event.getComponent() : null);
        if (diag == null) {
            return;
        }
        String typed = getTypedAutoCompleteValue(event.getComponent());
        String codigo = cie10LookupService.extraerCodigoDeSugerencia(typed);
        if (isBlank(codigo)) {
            return;
        }
        diag.setCodigo(codigo);
        var cie = cie10Service.buscarPorCodigo(codigo);
        if (cie != null) {
            diag.setDescripcion(cie.getDescripcion());
            diag.setCie10(cie);
        }
    }

    public void onDiagnosticoDescripcionBlur(AjaxBehaviorEvent event) {
        ConsultaDiagnostico diag = resolveDiagFromEvent(event != null ? event.getComponent() : null);
        if (diag == null) {
            return;
        }
        String descripcion = getTypedAutoCompleteValue(event.getComponent());
        if (isBlank(descripcion)) {
            return;
        }
        diag.setDescripcion(descripcion);
        var cie = cie10Service.buscarPrimeroPorDescripcion(descripcion.trim());
        if (cie != null) {
            diag.setCodigo(cie.getCodigo());
            diag.setCie10(cie);
        }
    }

    private ConsultaDiagnostico resolveDiagFromEvent(UIComponent component) {
        if (component == null) {
            return null;
        }
        Object idxObj = component.getAttributes().get("idx");
        if (idxObj == null) {
            return null;
        }
        int idx;
        try {
            idx = Integer.parseInt(String.valueOf(idxObj));
        } catch (NumberFormatException ex) {
            return null;
        }
        if (idx < 0 || idx >= diagnosticos.size()) {
            return null;
        }
        return diagnosticos.get(idx);
    }

    private String getTypedAutoCompleteValue(UIComponent component) {
        if (component == null) {
            return null;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return null;
        }
        String base = component.getClientId(fc);
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        String value = params.get(base + "_input");
        if (value == null) {
            value = params.get(base);
        }
        return value;
    }

    public void generarPdfReceta() {
        if (consulta.getEmpleado() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Paciente requerido", "Busque un paciente para generar el PDF.");
            return;
        }
        String html = construirHtmlReceta();
        tokenPdf = centroMedicoPdfFacade.generarDesdeHtml(html, "CONS_");
        addMessage(FacesMessage.SEVERITY_INFO, "PDF generado", "Ya puede visualizar o descargar la receta.");
    }

    public String getPdfUrl() {
        return tokenPdf == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?token=" + tokenPdf;
    }

    public String getPdfDownloadUrl() {
        return tokenPdf == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?download=1&amp;token=" + tokenPdf;
    }

    private String construirHtmlReceta() {
        String fecha = formatDate(consulta.getFechaConsulta());
        String fechaVigencia = formatDate(vigenciaReceta);
        String nombrePaciente = empleado.getNombreC() == null ? "" : empleado.getNombreC();
        String cedula = empleado.getNoCedula() == null ? "" : empleado.getNoCedula();
        String edad = calcularEdadTexto(fechaNacimientoPaciente);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html xmlns='http://www.w3.org/1999/xhtml'><head><meta charset='UTF-8'/>")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;font-size:12px;margin:30px;color:#000;}")
                .append(".grid{display:grid;grid-template-columns:1fr 1fr;gap:30px;}")
                .append(".panel{min-height:1000px;position:relative;}")
                .append(".titulo{font-weight:bold;margin-top:15px;margin-bottom:6px;}")
                .append(".row{margin-bottom:6px;}")
                .append("table{width:100%;border-collapse:collapse;}th,td{padding:4px 2px;vertical-align:top;}")
                .append(".firmante{position:absolute;left:0;bottom:20px;}")
                .append(".small{font-size:10px;}")
                .append("</style></head><body><div class='grid'>");

        html.append(construirColumnaReceta(fecha, nombrePaciente, cedula, edad, fechaVigencia));
        html.append(construirColumnaReceta(fecha, nombrePaciente, cedula, edad, fechaVigencia));

        html.append("</div></body></html>");
        return html.toString();
    }

    private String construirColumnaReceta(String fecha, String nombrePaciente, String cedula, String edad, String fechaVigencia) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='panel'>")
                .append("<div class='row'>QUITO,").append(escape(fecha)).append("</div>")
                .append("<div class='row'><b>Paciente:</b> ").append(escape(nombrePaciente)).append("</div>")
                .append("<div class='row'><b>Cédula:</b> ").append(escape(cedula)).append("</div>")
                .append("<div class='row'><b>Edad:</b> ").append(escape(edad)).append("</div>")
                .append("<div class='titulo'>DIAGNÓSTICO</div>");

        int count = 1;
        for (ConsultaDiagnostico d : diagnosticos) {
            if (d == null || (isBlank(d.getCodigo()) && isBlank(d.getDescripcion()))) {
                continue;
            }
            sb.append("<div class='row'>").append(count++)
                    .append(". ").append(escape(d.getCodigo()))
                    .append(" ").append(escape(d.getDescripcion()))
                    .append("</div>");
        }

        sb.append("<div class='titulo'>Antecedente de Alergias:</div>")
                .append("<div class='row'>").append(escape(resolveAlergiasTexto())).append("</div>")
                .append("<div class='titulo'>Vigencia del pedido hasta el : ").append(escape(fechaVigencia)).append("</div>")
                .append("<table><thead><tr><th>Código RP.</th><th>Medicamento e indicaciones</th><th>Cantidad</th></tr></thead><tbody>");

        for (RecetaItem item : recetas) {
            if (item == null || isBlank(item.getNombreMedicamento())) {
                continue;
            }
            sb.append("<tr><td>").append(escape(item.getCodigo())).append("</td><td>")
                    .append(escape(item.getNombreMedicamento())).append("<br/><span class='small'>")
                    .append(escape(item.getIndicaciones())).append("</span></td><td>")
                    .append(escape(item.getCantidad())).append("</td></tr>");
        }

        sb.append("</tbody></table>")
                .append("<div class='titulo'>Recomendaciones no farmacológicas:</div>")
                .append("<div class='row'>").append(escape(recomendaciones)).append("</div>")
                .append("<div class='titulo'>Signos de alarma:</div>")
                .append("<div class='row'>").append(escape(signosAlarma)).append("</div>")
                .append("<div class='firmante'>")
                .append("<div>").append(escape(consulta.getMedicoNombre())).append("</div>")
                .append("<div>").append(escape(consulta.getMedicoCodigo())).append("</div>")
                .append("</div></div>");

        return sb.toString();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy/MM/dd").format(date);
    }

    private String calcularEdadTexto(Date nacimiento) {
        if (nacimiento == null) {
            return "";
        }
        LocalDate fn = Instant.ofEpochMilli(nacimiento.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        int years = Period.between(fn, LocalDate.now()).getYears();
        return years + " Años";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    private String resolveAlergiasTexto() {
        if (!isBlank(alergias)) {
            return alergias;
        }
        if (consulta != null && consulta.getEmpleado() != null && !isBlank(consulta.getEmpleado().getAlergia())) {
            return consulta.getEmpleado().getAlergia();
        }
        return "SIN REGISTRO";
    }

    public String getCedulaBusqueda() { return cedulaBusqueda; }
    public void setCedulaBusqueda(String cedulaBusqueda) { this.cedulaBusqueda = cedulaBusqueda; }
    public DatEmpleado getEmpleado() { return empleado; }
    public ConsultaMedica getConsulta() { return consulta; }
    public List<ConsultaDiagnostico> getDiagnosticos() { return diagnosticos; }
    public List<RecetaItem> getRecetas() { return recetas; }
    public Date getVigenciaReceta() { return vigenciaReceta; }
    public void setVigenciaReceta(Date vigenciaReceta) { this.vigenciaReceta = vigenciaReceta; }
    public String getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }
    public String getSignosAlarma() { return signosAlarma; }
    public void setSignosAlarma(String signosAlarma) { this.signosAlarma = signosAlarma; }
    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
    public Date getFechaNacimientoPaciente() { return fechaNacimientoPaciente; }
    public void setFechaNacimientoPaciente(Date fechaNacimientoPaciente) { this.fechaNacimientoPaciente = fechaNacimientoPaciente; }
    public String getEdadPaciente() { return calcularEdadTexto(fechaNacimientoPaciente); }
    public SignosVitales getSignosModel() { return signosModel; }
    public String getPaStr() { return paStr; }
    public void setPaStr(String paStr) { this.paStr = paStr; }

    public BigDecimal getTallaCm() {
        if (signosModel == null || signosModel.getTallaM() == null) {
            return null;
        }
        return signosModel.getTallaM().multiply(BigDecimal.valueOf(100));
    }

    public void setTallaCm(BigDecimal tallaCm) {
        if (signosModel == null) {
            signosModel = new SignosVitales();
        }
        if (tallaCm == null) {
            signosModel.setTallaM(null);
            return;
        }
        signosModel.setTallaM(tallaCm.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
    }

    public static class RecetaItem implements Serializable {
        private String codigo;
        private String nombreMedicamento;
        private String indicaciones;
        private String cantidad;

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public String getNombreMedicamento() { return nombreMedicamento; }
        public void setNombreMedicamento(String nombreMedicamento) { this.nombreMedicamento = nombreMedicamento; }
        public String getIndicaciones() { return indicaciones; }
        public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
        public String getCantidad() { return cantidad; }
        public void setCantidad(String cantidad) { this.cantidad = cantidad; }
    }
}
