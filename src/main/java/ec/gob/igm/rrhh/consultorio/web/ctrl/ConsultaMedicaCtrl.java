package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.RecetaItem;
import ec.gob.igm.rrhh.consultorio.domain.model.RecetaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;
import ec.gob.igm.rrhh.consultorio.service.ConsultaMedicaService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoRhService;
import ec.gob.igm.rrhh.consultorio.service.SignosVitalesService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.service.Cie10LookupService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.format.TextStyle;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.primefaces.event.SelectEvent;

@Named("consultaMedicaCtrl")
@SessionScoped
public class ConsultaMedicaCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient EmpleadoService empleadoService;
    @Inject
    private transient EmpleadoRhService empleadoRhService;
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
    private List<RecetaItemForm> recetas;
    private Date vigenciaReceta;
    private String recomendaciones;
    private String signosAlarma;
    private String alergias;
    private String tokenPdf;
    private String tokenPdfCertificado;
    private Date fechaNacimientoPaciente;
    private SignosVitales signosModel;
    private String paStr;
    private boolean generarCertificado;
    private Date certFechaInicio;
    private Date certFechaFin;
    private String certFechaInicioLetras;
    private String certFechaFinLetras;
    private String certDomicilio;
    private String certTelefono;
    private String certTipoContingencia;
    private String certMedicoCargo;
    private String certMedicoTelefono;
    private String certMedicoCorreo;

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
        certTipoContingencia = "MEDICO";
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
        recetas.add(new RecetaItemForm());
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
        consulta.setRecetas(construirRecetasParaPersistencia());
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

    private List<RecetaMedica> construirRecetasParaPersistencia() {
        List<RecetaItem> items = new ArrayList<>();
        for (RecetaItemForm fila : recetas) {
            if (fila == null || isBlank(fila.getMedicamento())) {
                continue;
            }
            RecetaItem item = new RecetaItem();
            item.setMedicamento(fila.getMedicamento().trim());
            item.setDosis(normalizarTexto(fila.getDosis()));
            item.setFrecuencia(normalizarTexto(fila.getFrecuencia()));
            item.setVia(normalizarTexto(fila.getVia()));
            item.setDuracionDias(fila.getDuracionDias());
            item.setIndicaciones(normalizarTexto(fila.getIndicaciones()));
            item.setEstado("ACTIVO");
            items.add(item);
        }
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        RecetaMedica recetaMedica = new RecetaMedica();
        recetaMedica.setConsulta(consulta);
        recetaMedica.setFechaEmision(vigenciaReceta != null ? vigenciaReceta : consulta.getFechaConsulta());
        recetaMedica.setIndicaciones(construirIndicacionesRecetaCabecera());
        recetaMedica.setEstado("ACTIVA");

        for (RecetaItem item : items) {
            item.setReceta(recetaMedica);
        }
        recetaMedica.setItems(items);

        List<RecetaMedica> resultado = new ArrayList<>();
        resultado.add(recetaMedica);
        return resultado;
    }

    private String construirIndicacionesRecetaCabecera() {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(recomendaciones)) {
            sb.append("Recomendaciones no farmacológicas: ").append(recomendaciones.trim());
        }
        if (!isBlank(signosAlarma)) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append("Signos de alarma: ").append(signosAlarma.trim());
        }
        return sb.toString();
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

    public void prepararDialogoCertificado() {
        if (consulta.getEmpleado() == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Paciente requerido", "Busque un paciente antes de generar certificado.");
            return;
        }
        if (certFechaInicio == null) {
            certFechaInicio = consulta.getFechaConsulta() != null ? consulta.getFechaConsulta() : new Date();
        }
        if (certFechaFin == null) {
            certFechaFin = certFechaInicio;
        }
        if (isBlank(certFechaInicioLetras) && certFechaInicio != null) {
            certFechaInicioLetras = fechaEnLetrasCompleta(certFechaInicio);
        }
        if (isBlank(certFechaFinLetras) && certFechaFin != null) {
            certFechaFinLetras = fechaEnLetrasCompleta(certFechaFin);
        }
        if (isBlank(certTipoContingencia)) {
            certTipoContingencia = "MEDICO";
        }
    }

    public void generarPdfCertificado() {
        if (consulta.getEmpleado() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Paciente requerido", "Busque un paciente para generar el certificado.");
            return;
        }
        if (certFechaInicio == null || certFechaFin == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Fechas requeridas", "Ingrese fecha inicio y fin para el certificado.");
            return;
        }
        if (certFechaFin.before(certFechaInicio)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Rango inválido", "La fecha fin no puede ser menor a la fecha inicio.");
            return;
        }
        if (!validarCamposObligatoriosCertificado()) {
            return;
        }
        String html = construirHtmlCertificado();
        tokenPdfCertificado = centroMedicoPdfFacade.generarDesdeHtml(html, "CERT_MED_");
        addMessage(FacesMessage.SEVERITY_INFO, "Certificado generado", "Ya puede visualizar o descargar el certificado.");
    }

    public String getPdfUrl() {
        String token = tokenPdfCertificado != null ? tokenPdfCertificado : tokenPdf;
        return token == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?token=" + token;
    }

    public String getPdfDownloadUrl() {
        String token = tokenPdfCertificado != null ? tokenPdfCertificado : tokenPdf;
        return token == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?download=1&amp;token=" + token;
    }

    public String getRecetaPdfUrl() {
        return tokenPdf == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?token=" + tokenPdf;
    }

    public String getRecetaPdfDownloadUrl() {
        return tokenPdf == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?download=1&amp;token=" + tokenPdf;
    }

    public String getCertificadoPdfUrl() {
        return tokenPdfCertificado == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?token=" + tokenPdfCertificado;
    }

    public String getCertificadoPdfDownloadUrl() {
        return tokenPdfCertificado == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?download=1&amp;token=" + tokenPdfCertificado;
    }

    private String construirHtmlReceta() {
        String fecha = fechaEnLetrasCompleta(consulta.getFechaConsulta());
        String fechaVigencia = formatDate(vigenciaReceta);
        String nombrePaciente = empleado.getNombreC() == null ? "" : empleado.getNombreC();
        String cedula = empleado.getNoCedula() == null ? "" : empleado.getNoCedula();
        String edad = calcularEdadTexto(fechaNacimientoPaciente);
        String medicoNombre = consulta.getMedicoNombre() == null ? "" : consulta.getMedicoNombre();
        String medicoMsp = consulta.getMedicoCodigo() == null ? "" : consulta.getMedicoCodigo();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html xmlns='http://www.w3.org/1999/xhtml'><head><meta charset='UTF-8'/>")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;font-size:12px;margin:22px;color:#000;}")
                .append(".grid{display:grid;grid-template-columns:1fr 1fr;gap:30px;}")
                .append(".panel{min-height:1000px;position:relative;border:1px solid #9ca3af;padding:14px 16px 150px 16px;}")
                .append(".encabezado{border-bottom:2px solid #1f2937;margin-bottom:10px;padding-bottom:7px;}")
                .append(".encabezado-titulo{text-align:center;font-size:22px;font-weight:700;letter-spacing:1px;margin:2px 0 0 0;}")
                .append(".encabezado-sub{font-size:11px;color:#374151;text-align:center;margin:3px 0 0 0;}")
                .append(".titulo{font-weight:bold;margin-top:15px;margin-bottom:6px;}")
                .append(".row{margin-bottom:6px;}")
                .append("table{width:100%;border-collapse:collapse;}th,td{padding:4px 2px;vertical-align:top;}")
                .append(".firmante{position:absolute;left:16px;right:16px;bottom:24px;text-align:center;}")
                .append(".firma-linea{border-top:1px solid #000;width:260px;margin:0 auto 8px auto;}")
                .append(".small{font-size:10px;}")
                .append("</style></head><body><div class='grid'>");

        html.append(construirColumnaReceta(fecha, nombrePaciente, cedula, edad, fechaVigencia, medicoNombre, medicoMsp));
        html.append(construirColumnaReceta(fecha, nombrePaciente, cedula, edad, fechaVigencia, medicoNombre, medicoMsp));

        html.append("</div></body></html>");
        return html.toString();
    }

    private String construirColumnaReceta(String fecha, String nombrePaciente, String cedula, String edad,
            String fechaVigencia, String medicoNombre, String medicoMsp) {
        String sexo = empleado != null && empleado.getSexo() != null ? empleado.getSexo().getDescripcion() : "";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='panel'>")
                .append("<div class='encabezado'>")
                .append("<div class='encabezado-titulo'>RECETA</div>")
                .append("<div class='encabezado-sub'>CONSULTORIO MÉDICO IGM</div>")
                .append("</div>")
                .append("<div class='row'>QUITO,").append(escape(fecha).toUpperCase()).append("</div>")
                .append("<div class='row'><b>Paciente:</b> ").append(escape(nombrePaciente)).append("</div>")
                .append("<div class='row'><b>Cédula:</b> ").append(escape(cedula)).append("</div>")
                .append("<div class='row'><b>Sexo:</b> ").append(escape(sexo)).append("</div>")
                .append("<div class='row'><b>Edad:</b> ").append(escape(edad)).append("</div>");

        sb.append("<div class='titulo'>Antecedente de Alergias:</div>")
                .append("<div class='row'>").append(escape(resolveAlergiasTexto())).append("</div>")
                .append("<div class='titulo'>Vigencia del pedido hasta el : ").append(escape(fechaVigencia)).append("</div>")
                .append("<table><thead><tr><th>Medicamento</th><th>Dosis</th><th>Frecuencia</th><th>Vía</th><th>Días</th><th>Indicaciones</th></tr></thead><tbody>");

        for (RecetaItemForm item : recetas) {
            if (item == null || isBlank(item.getMedicamento())) {
                continue;
            }
            sb.append("<tr><td>").append(escape(item.getMedicamento())).append("</td><td>")
                    .append(escape(item.getDosis())).append("</td><td>")
                    .append(escape(item.getFrecuencia())).append("</td><td>")
                    .append(escape(item.getVia())).append("</td><td>")
                    .append(item.getDuracionDias() == null ? "" : item.getDuracionDias()).append("</td><td>")
                    .append(escape(item.getIndicaciones())).append("</td></tr>");
        }

        sb.append("</tbody></table>")
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

        sb.append("<div class='titulo'>Recomendaciones no farmacológicas:</div>")
                .append("<div class='row'>").append(escape(recomendaciones)).append("</div>")
                .append("<div class='titulo'>Signos de alarma:</div>")
                .append("<div class='row'>").append(escape(signosAlarma)).append("</div>")
                .append("<div class='firmante'>")
                .append("<div class='firma-linea'></div>")
                .append("<div><b>Firma:</b> ____________________________</div>")
                .append("<div><b>Nombre:</b> ").append(escape(medicoNombre)).append("</div>")
                .append("<div><b>MSP:</b> ").append(escape(medicoMsp)).append("</div>")
                .append("</div></div>");

        return sb.toString();
    }

    private String construirHtmlCertificado() {
        String nombrePaciente = empleado.getNombreC() == null ? "" : empleado.getNombreC();
        String cedula = empleado.getNoCedula() == null ? "" : empleado.getNoCedula();
        String cargoPaciente = resolveCargoPaciente();
        String fechaInicioNum = formatDate(certFechaInicio).replace("/", "-");
        String fechaFinNum = formatDate(certFechaFin).replace("/", "-");
        String fechaInicioTxt = fechaEnLetrasCompleta(certFechaInicio);
        String fechaFinTxt = fechaEnLetrasCompleta(certFechaFin);
        long diasReposo = getCertDiasReposo();
        String diasReposoLetras = numeroEnLetras((int) diasReposo);
        String medicoMsp = consulta.getMedicoCodigo() == null ? "" : consulta.getMedicoCodigo();

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/>")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;font-size:15px;line-height:1.35;margin:48px;color:#1f2937;}")
                .append(".titulo{text-align:center;font-size:42px;font-weight:700;margin:20px 0 30px 0;}")
                .append(".hl{background:#fff176;padding:0 3px;}")
                .append(".lbl{font-weight:700;}")
                .append("</style></head><body>")
                .append("<div class='titulo'>CERTIFICADO MEDICO</div>")
                .append("<p>EL MEDICO CERTIFICA:</p>")
                .append("<p>El señor(a) <span class='hl'>").append(escape(nombrePaciente)).append("</span> con Cédula de Ciudadanía ")
                .append("<span class='hl'>").append(escape(cedula)).append("</span>")
                .append(", cargo <span class='hl'>").append(escape(cargoPaciente)).append("</span>, presenta diagnóstico: ")
                .append("<span class='hl'>").append(escape(getDiagnosticosTexto())).append("</span>; por lo que requiere reposo médico absoluto.</p>")
                .append("<p><span class='lbl'>Número de historia clínica:</span> ").append(escape(cedula)).append("</p>")
                .append("<p>Desde ").append(escape(fechaInicioNum)).append(" (").append(escape(fechaInicioTxt)).append(").<br/>")
                .append("Hasta ").append(escape(fechaFinNum)).append(" (").append(escape(fechaFinTxt)).append(").</p>")
                .append("<p><span class='lbl'>Número de días de reposo:</span> ")
                .append(diasReposo)
                .append(" (").append(escape(diasReposoLetras)).append(").</p>")
                .append("<p><span class='lbl'>Domicilio del paciente:</span> ").append(escape(certDomicilio)).append("<br/>")
                .append("<span class='lbl'>Teléfono de contacto:</span> ").append(escape(certTelefono)).append("<br/>")
                .append("<span class='lbl'>Tipo de contingencia:</span> ").append(escape(certTipoContingencia)).append("</p>")
                .append("<p style='margin-top:40px;'>")
                .append("Quito DM, ").append(escape(fechaEnLetrasCompleta(consulta.getFechaConsulta() != null ? consulta.getFechaConsulta() : new Date()))).append(".</p>")
                .append("<p style='margin-top:80px;'>").append(escape(consulta.getMedicoNombre())).append("<br/>")
                .append(escape(certMedicoCargo)).append("<br/>")
                .append("MSP: ").append(escape(medicoMsp)).append("<br/>")
                .append("Teléfono: ").append(escape(certMedicoTelefono)).append("<br/>")
                .append("Correo: ").append(escape(certMedicoCorreo)).append("</p>")
                .append("</body></html>");
        return html.toString();
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

    private String fechaEnLetras(Date date) {
        if (date == null) {
            return "";
        }
        LocalDate local = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        String mes = local.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("es", "EC"));
        return local.getDayOfMonth() + " de " + mes + " de " + local.getYear();
    }

    private String fechaEnLetrasCompleta(Date date) {
        if (date == null) {
            return "";
        }
        LocalDate local = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        String mes = local.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("es", "EC"));
        return numeroEnLetras(local.getDayOfMonth()) + " de " + mes + " de " + anioEnLetras(local.getYear());
    }

    public void onFechaInicioSelect() {
        certFechaInicioLetras = fechaEnLetrasCompleta(certFechaInicio);
    }

    public void onFechaFinSelect() {
        certFechaFinLetras = fechaEnLetrasCompleta(certFechaFin);
    }

    private boolean validarCamposObligatoriosCertificado() {
        List<String> faltantes = new ArrayList<>();
        if (getCertDiasReposo() <= 0) {
            faltantes.add("días de reposo");
        }
        if (isBlank(certDomicilio)) {
            faltantes.add("domicilio");
        }
        if (isBlank(certTelefono)) {
            faltantes.add("teléfono de contacto");
        }
        if (isBlank(certTipoContingencia)) {
            faltantes.add("tipo de contingencia");
        }
        if (isBlank(certMedicoCargo)) {
            faltantes.add("cargo del médico");
        }
        if (isBlank(certMedicoTelefono)) {
            faltantes.add("teléfono del médico");
        }
        if (isBlank(certMedicoCorreo)) {
            faltantes.add("correo del médico");
        }
        if (!faltantes.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Campos requeridos",
                    "Complete: " + String.join(", ", faltantes) + ".");
            return false;
        }
        return true;
    }

    private String resolveCargoPaciente() {
        if (empleado == null) {
            return "";
        }
        if (!isBlank(empleado.getCargoLossca())) {
            return empleado.getCargoLossca();
        }
        if (empleadoRhService == null || isBlank(empleado.getNoCedula())) {
            return "";
        }
        var cargoRh = empleadoRhService.buscarPorCedulaEnVista(empleado.getNoCedula());
        return cargoRh != null && !isBlank(cargoRh.getCargoDescrip()) ? cargoRh.getCargoDescrip() : "";
    }

    public long getCertDiasReposo() {
        if (certFechaInicio == null || certFechaFin == null) {
            return 0L;
        }
        LocalDate inicio = Instant.ofEpochMilli(certFechaInicio.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate fin = Instant.ofEpochMilli(certFechaFin.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        long dias = ChronoUnit.DAYS.between(inicio, fin);
        return Math.max(0L, dias);
    }

    public String getCertDiasReposoLetras() {
        return numeroEnLetras((int) getCertDiasReposo());
    }

    public String getCertCargoPaciente() {
        return resolveCargoPaciente();
    }

    public String getDiagnosticoPrincipal() {
        if (diagnosticos == null) {
            return "";
        }
        for (ConsultaDiagnostico d : diagnosticos) {
            if (d != null && (!isBlank(d.getCodigo()) || !isBlank(d.getDescripcion()))) {
                return (d.getCodigo() == null ? "" : d.getCodigo() + " ") + (d.getDescripcion() == null ? "" : d.getDescripcion());
            }
        }
        return "";
    }

    public String getDiagnosticosTexto() {
        if (diagnosticos == null) {
            return "";
        }
        List<String> valores = new ArrayList<>();
        for (ConsultaDiagnostico d : diagnosticos) {
            if (d == null || (isBlank(d.getCodigo()) && isBlank(d.getDescripcion()))) {
                continue;
            }
            String texto = ((d.getCodigo() == null ? "" : d.getCodigo().trim()) + " "
                    + (d.getDescripcion() == null ? "" : d.getDescripcion().trim())).trim();
            if (!texto.isEmpty()) {
                valores.add(texto);
            }
        }
        return String.join("; ", valores);
    }

    private String anioEnLetras(int anio) {
        int miles = anio / 1000;
        int resto = anio % 1000;
        String milesTxt = (miles == 2) ? "dos mil" : (numeroEnLetras(miles) + " mil");
        if (resto == 0) {
            return milesTxt;
        }
        return milesTxt + " " + numeroEnLetras(resto);
    }

    private String numeroEnLetras(int numero) {
        if (numero <= 0) {
            return "cero";
        }
        if (numero < 30) {
            String[] base = {"", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve",
                    "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho",
                    "diecinueve", "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco",
                    "veintiséis", "veintisiete", "veintiocho", "veintinueve"};
            return base[numero];
        }
        if (numero < 100) {
            String[] decenas = {"", "", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
            int d = numero / 10;
            int u = numero % 10;
            if (u == 0) {
                return decenas[d];
            }
            return decenas[d] + " y " + numeroEnLetras(u);
        }
        if (numero == 100) {
            return "cien";
        }
        if (numero < 1000) {
            String[] centenas = {"", "ciento", "doscientos", "trescientos", "cuatrocientos",
                    "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"};
            int c = numero / 100;
            int resto = numero % 100;
            if (resto == 0) {
                return centenas[c];
            }
            return centenas[c] + " " + numeroEnLetras(resto);
        }
        return Integer.toString(numero);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizarTexto(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
    public List<RecetaItemForm> getRecetas() { return recetas; }
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

    public static class RecetaItemForm implements Serializable {
        private String medicamento;
        private String dosis;
        private String frecuencia;
        private String via;
        private Integer duracionDias;
        private String indicaciones;

        public String getMedicamento() { return medicamento; }
        public void setMedicamento(String medicamento) { this.medicamento = medicamento; }
        public String getDosis() { return dosis; }
        public void setDosis(String dosis) { this.dosis = dosis; }
        public String getFrecuencia() { return frecuencia; }
        public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }
        public String getVia() { return via; }
        public void setVia(String via) { this.via = via; }
        public Integer getDuracionDias() { return duracionDias; }
        public void setDuracionDias(Integer duracionDias) { this.duracionDias = duracionDias; }
        public String getIndicaciones() { return indicaciones; }
        public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    }

    public boolean isGenerarCertificado() { return generarCertificado; }
    public void setGenerarCertificado(boolean generarCertificado) { this.generarCertificado = generarCertificado; }
    public Date getCertFechaInicio() { return certFechaInicio; }
    public void setCertFechaInicio(Date certFechaInicio) { this.certFechaInicio = certFechaInicio; }
    public Date getCertFechaFin() { return certFechaFin; }
    public void setCertFechaFin(Date certFechaFin) { this.certFechaFin = certFechaFin; }
    public String getCertFechaInicioLetras() { return certFechaInicioLetras; }
    public void setCertFechaInicioLetras(String certFechaInicioLetras) { this.certFechaInicioLetras = certFechaInicioLetras; }
    public String getCertFechaFinLetras() { return certFechaFinLetras; }
    public void setCertFechaFinLetras(String certFechaFinLetras) { this.certFechaFinLetras = certFechaFinLetras; }
    public String getCertDomicilio() { return certDomicilio; }
    public void setCertDomicilio(String certDomicilio) { this.certDomicilio = certDomicilio; }
    public String getCertTelefono() { return certTelefono; }
    public void setCertTelefono(String certTelefono) { this.certTelefono = certTelefono; }
    public String getCertTipoContingencia() { return certTipoContingencia; }
    public void setCertTipoContingencia(String certTipoContingencia) { this.certTipoContingencia = certTipoContingencia; }
    public String getCertMedicoCargo() { return certMedicoCargo; }
    public void setCertMedicoCargo(String certMedicoCargo) { this.certMedicoCargo = certMedicoCargo; }
    public String getCertMedicoTelefono() { return certMedicoTelefono; }
    public void setCertMedicoTelefono(String certMedicoTelefono) { this.certMedicoTelefono = certMedicoTelefono; }
    public String getCertMedicoCorreo() { return certMedicoCorreo; }
    public void setCertMedicoCorreo(String certMedicoCorreo) { this.certMedicoCorreo = certMedicoCorreo; }
}
