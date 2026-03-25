package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
/**
 * Class CertificadoPdfTemplateService: gestiona la construccion y renderizacion de documentos PDF.
 */
public class CertificadoPdfTemplateService {

    public String construirHtmlDesdePlantilla(CertificadoTemplateData data) {
        String template = PdfTextUtil.normalizarXhtmlPdf(data.template);
        Map<String, String> rep = new LinkedHashMap<>();
        Date fecha = data.fechaEmision != null ? data.fechaEmision : new Date();

        putFecha(rep, data, fecha);
        putAptitud(rep, data);
        putTipoEvaluacion(rep, data);
        putDatosGenerales(rep, data);

        return data.templateEngine.render(template, rep);
    }

    public static class CertificadoTemplateData {
        public String template;
        public Date fechaEmision;
        public String apto = "";
        public String obs = "";
        public String lim = "";
        public String noApto = "";
        public String chkIngreso = "";
        public String chkPeriodico = "";
        public String chkReintegro = "";
        public String chkRetiro = "";
        public String logoIgm = "";
        public String logoMidena = "";
        public String institucion = "";
        public String ruc = "";
        public String noHistoria = "";
        public String noArchivo = "";
        public String centroTrabajo = "";
        public String ciiu = "";
        public String apellido1 = "";
        public String apellido2 = "";
        public String nombre1 = "";
        public String nombre2 = "";
        public String sexo = "";
        public String detalleObservaciones = "";
        public String recomendaciones = "";
        public String medicoNombre = "";
        public String medicoCodigo = "";
        public PdfTemplateEngine templateEngine;

        private String yyyy(Date f) { return new java.text.SimpleDateFormat("yyyy").format(f); }
        private String MM(Date f) { return new java.text.SimpleDateFormat("MM").format(f); }
        private String dd(Date f) { return new java.text.SimpleDateFormat("dd").format(f); }
    }

    private void putFecha(Map<String, String> rep, CertificadoTemplateData data, Date fecha) {
        rep.put("fecha_yyyy", data.yyyy(fecha));
        rep.put("fecha_MM", data.MM(fecha));
        rep.put("fecha_dd", data.dd(fecha));
    }

    private void putAptitud(Map<String, String> rep, CertificadoTemplateData data) {
        rep.put("chk_apto", data.apto);
        rep.put("chk_obs", data.obs);
        rep.put("chk_lim", data.lim);
        rep.put("chk_noapto", data.noApto);
    }

    private void putTipoEvaluacion(Map<String, String> rep, CertificadoTemplateData data) {
        rep.put("chk_ingreso", data.chkIngreso);
        rep.put("chk_periodico", data.chkPeriodico);
        rep.put("chk_reintegro", data.chkReintegro);
        rep.put("chk_retiro", data.chkRetiro);
    }

    private void putDatosGenerales(Map<String, String> rep, CertificadoTemplateData data) {
        rep.put("LOGO_IGM_DATAURI", data.logoIgm);
        rep.put("LOGO_MIDENA_DATAURI", data.logoMidena);
        rep.put("institucion", data.institucion);
        rep.put("ruc", data.ruc);
        rep.put("num_formulario", data.noHistoria);
        rep.put("num_archivo", data.noArchivo);
        rep.put("centroTrabajo", data.centroTrabajo);
        rep.put("ciiu", data.ciiu);
        rep.put("apellido1", data.apellido1);
        rep.put("apellido2", data.apellido2);
        rep.put("nombre1", data.nombre1);
        rep.put("nombre2", data.nombre2);
        rep.put("sexo", data.sexo);
        rep.put("detalleObservaciones", data.detalleObservaciones);
        rep.put("recomendaciones", data.recomendaciones);
        rep.put("medicoNombre", data.medicoNombre);
        rep.put("medicoCodigo", data.medicoCodigo);
    }
}
