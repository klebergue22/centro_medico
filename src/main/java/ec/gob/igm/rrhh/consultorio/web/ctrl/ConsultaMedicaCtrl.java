package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.service.ConsultaMedicaService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private String cedulaBusqueda;
    private DatEmpleado empleado;
    private ConsultaMedica consulta;
    private List<ConsultaDiagnostico> diagnosticos;
    private List<RecetaItem> recetas;
    private Date vigenciaReceta;
    private String recomendaciones;
    private String signosAlarma;
    private String tokenPdf;

    @PostConstruct
    public void init() {
        consulta = new ConsultaMedica();
        consulta.setFechaConsulta(new Date());
        consulta.setEstado("ACTIVO");
        consulta.setDiagnosticos(new ArrayList<>());
        diagnosticos = new ArrayList<>();
        recetas = new ArrayList<>();
        vigenciaReceta = new Date();
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

        List<ConsultaDiagnostico> limpios = new ArrayList<>();
        for (ConsultaDiagnostico d : diagnosticos) {
            if (d == null) {
                continue;
            }
            if (isBlank(d.getCodigo()) && isBlank(d.getDescripcion())) {
                continue;
            }
            d.setConsulta(consulta);
            d.setEstado("ACTIVO");
            limpios.add(d);
        }

        consulta.setDiagnosticos(limpios);
        consultaMedicaService.guardar(consulta, "WEB");
        addMessage(FacesMessage.SEVERITY_INFO, "Consulta guardada", "Se registró la consulta médica.");
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
        String edad = calcularEdadTexto(empleado.getfNacimiento());

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
                .append("<div class='row'>").append(escape(consulta.getEmpleado().getAlergia())).append("</div>")
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
        long diff = new Date().getTime() - nacimiento.getTime();
        long years = diff / (1000L * 60 * 60 * 24 * 365);
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
