package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.persistence.Persistence;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;

@Stateless
/**
 * Class FichaPdfViewModelBuilder: orquesta la lógica de presentación y flujo web.
 */
public class FichaPdfViewModelBuilder implements Serializable {

    @jakarta.inject.Inject
    private EmpleadoService empleadoService;

    public Map<String, String> buildReemplazosFicha(FichaPdfViewModelContext ctx) {
        Map<String, String> rep = new LinkedHashMap<>();
        cargarDatosCabecera(rep, ctx);
        cargarDatosPersonales(rep, ctx);
        cargarConsumo(rep, ctx);
        cargarCamposDirectosFaltantes(rep, ctx);
        cargarExamenFisicoRegional(rep, ctx);
        rep.put("enfermedad_actual", safe(ctx.ficha != null ? ctx.ficha.getEnfermedadProbActual() : ctx.enfermedadActual));
        corregirOtrosRiesgos(rep, ctx.otrosRiesgos);
        return rep;
    }

    private void cargarExamenFisicoRegional(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        cargarExamenFisicoCabezaCuello(rep, ctx);
        cargarExamenFisicoTorso(rep, ctx);
        cargarExamenFisicoExtremidades(rep, ctx);
        rep.put("obs_examen_fisico", safe(resolveObsExamenFisico(ctx)));
    }

    private void cargarExamenFisicoCabezaCuello(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        rep.put("exf_piel_cicatrices", markX(ctx.exfPielCicatrices));
        rep.put("exf_ojos_parpados", markX(ctx.exfOjosParpados));
        rep.put("exf_ojos_conjuntivas", markX(ctx.exfOjosConjuntivas));
        rep.put("exf_ojos_pupilas", markX(ctx.exfOjosPupilas));
        rep.put("exf_ojos_cornea", markX(ctx.exfOjosCornea));
        rep.put("exf_ojos_motilidad", markX(ctx.exfOjosMotilidad));
        rep.put("exf_oido_conducto", markX(ctx.exfOidoConducto));
        rep.put("exf_oido_pabellon", markX(ctx.exfOidoPabellon));
        rep.put("exf_oido_timpanos", markX(ctx.exfOidoTimpanos));
        rep.put("exf_oro_labios", markX(ctx.exfOroLabios));
        rep.put("exf_oro_lengua", markX(ctx.exfOroLengua));
        rep.put("exf_oro_faringe", markX(ctx.exfOroFaringe));
        rep.put("exf_oro_amigdalas", markX(ctx.exfOroAmigdalas));
        rep.put("exf_oro_dentadura", markX(ctx.exfOroDentadura));
        rep.put("exf_nariz_tabique", markX(ctx.exfNarizTabique));
        rep.put("exf_nariz_cornetes", markX(ctx.exfNarizCornetes));
        rep.put("exf_nariz_mucosas", markX(ctx.exfNarizMucosas));
        rep.put("exf_nariz_senos", markX(ctx.exfNarizSenos));
        rep.put("exf_cuello_tiroides", markX(ctx.exfCuelloTiroides));
        rep.put("exf_cuello_movilidad", markX(ctx.exfCuelloMovilidad));
    }

    private void cargarExamenFisicoTorso(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        rep.put("exf_torax_mamas", markX(ctx.exfToraxMamas));
        rep.put("exf_torax_pulmones", markX(ctx.exfToraxPulmones));
        rep.put("exf_torax_corazon", markX(ctx.exfToraxCorazon));
        rep.put("exf_torax_parrilla", markX(ctx.exfToraxParrilla));
        rep.put("exf_abdomen_visceras", markX(ctx.exfAbdomenVisceras));
        rep.put("exf_abdomen_pared", markX(ctx.exfAbdomenPared));
        rep.put("exf_columna_flexibilidad", markX(ctx.exfColumnaFlexibilidad));
        rep.put("exf_columna_desviacion", markX(ctx.exfColumnaDesviacion));
        rep.put("exf_columna_dolor", markX(ctx.exfColumnaDolor));
        rep.put("exf_pelvis_pelvis", markX(ctx.exfPelvisPelvis));
        rep.put("exf_pelvis_genitales", markX(ctx.exfPelvisGenitales));
    }

    private void cargarExamenFisicoExtremidades(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        rep.put("exf_ext_vascular", markX(ctx.exfExtVascular));
        rep.put("exf_ext_sup", markX(ctx.exfExtSup));
        rep.put("exf_ext_inf", markX(ctx.exfExtInf));
        rep.put("exf_neuro_fuerza", markX(ctx.exfNeuroFuerza));
        rep.put("exf_neuro_sensibilidad", markX(ctx.exfNeuroSensibilidad));
        rep.put("exf_neuro_marcha", markX(ctx.exfNeuroMarcha));
        rep.put("exf_neuro_reflejos", markX(ctx.exfNeuroReflejos));
    }

    private String resolveObsExamenFisico(FichaPdfViewModelContext ctx) {
        String observacion = trimToNull(ctx.obsExamenFisico);
        if (observacion != null) {
            return observacion;
        }
        return ctx.ficha != null ? trimToNull(ctx.ficha.getObsExamenFisicoReg()) : null;
    }

    private void cargarDatosCabecera(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        rep.put("institucion", safe(ctx.institucion));
        rep.put("ruc", safe(ctx.ruc));
        rep.put("centroTrabajo", safe(ctx.centroTrabajo));
        rep.put("ciiu", safe(ctx.ciiu));
        rep.put("noHistoria", safe(ctx.noHistoria));
        rep.put("noArchivo", safe(ctx.noArchivo));
        rep.put("num_formulario", safe(ctx.noHistoria));
        rep.put("no_historia_clinica", safe(ctx.noHistoria));
        rep.put("no_archivo", safe(ctx.noArchivo));
    }

    private void cargarDatosPersonales(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = resolveEmpleado(ctx);
        PersonaAux personaAux = resolvePersonaAux(ctx);
        rep.put("apellido1", safe(resolveApellido1(ctx)));
        rep.put("apellido2", safe(resolveApellido2(ctx)));
        rep.put("nombre1", safe(resolveNombre1(ctx)));
        rep.put("nombre2", safe(resolveNombre2(ctx)));
        rep.put("sexo", safe(resolveSexo(ctx)));
        java.util.Date fechaNacimiento = resolveFechaNacimiento(ctx);
        rep.put("fechaNacimiento", fmtDate(fechaNacimiento));
        Integer edad = resolveEdad(ctx, fechaNacimiento);
        rep.put("edad", edad == null ? "" : String.valueOf(edad));
        rep.put("grupoSanguineo", safe(ctx.grupoSanguineo));
        rep.put("lateralidad", safe(ctx.lateralidad));
        rep.put("cedula", safe(resolveCedulaForPdf(empleado, personaAux, ctx.cedulaBusqueda)));
        rep.put("email", "");
    }

    private void cargarConsumo(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        putBoolArray0Based(rep, "cons_ex_consumidor_", ctx.consExConsumidor);
        putBoolArray0Based(rep, "cons_no_consume_", ctx.consNoConsume);
        putIntArray0Based(rep, "cons_tiempo_consumo_", ctx.consTiempoConsumoMeses);
        putIntArray0Based(rep, "cons_tiempo_abstinencia_", ctx.consTiempoAbstinenciaMeses);
        putStringArray0Based(rep, "af_cual_", ctx.afCual);
        putStringArray0Based(rep, "af_tiempo_", ctx.afTiempo);
        putStringArray0Based(rep, "med_cual_", ctx.medCual);
        putIntArray0Based(rep, "med_cant_", ctx.medCant);
        rep.put("consumo_observacion", safe(trimToNull(ctx.consumoVidaCondObs) != null
                ? trimToNull(ctx.consumoVidaCondObs)
                : (ctx.ficha != null ? trimToNull(ctx.ficha.getObsConsumoVidaCond()) : null)));
        rep.put("consumo_vida_cond_obs", safe(ctx.consumoVidaCondObs));
        rep.put("cons_otras_cual", safe(ctx.consOtrasCual));
    }

    private void cargarCamposDirectosFaltantes(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        cargarFechasYContexto(rep, ctx);
        cargarSaludReproductiva(rep, ctx);
        cargarSignosVitales(rep, ctx);
        cargarDatosPuestoYTipoEvaluacion(rep, ctx);
    }

    private void cargarFechasYContexto(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        rep.put("recomendaciones", safe(ctx.recomendaciones));
        rep.put("fechaAtencion", fmtDate(ctx.fechaAtencion));
        rep.put("fecIngreso", fmtDate(resolveFecIngreso(ctx)));
        rep.put("fecReintegro", fmtDate(resolveFecReintegro(ctx)));
        rep.put("fecRetiro", fmtDate(ctx.fecRetiro));
        rep.put("motivoObs", safe(ctx.motivoObs));
        rep.put("condicionEspecial", safe(ctx.condicionEspecial));
        rep.put("autorizaTransfusion", safe(ctx.autorizaTransfusion));
        rep.put("tratamientoHormonal", safe(ctx.tratamientoHormonal));
        rep.put("tratamientoHormonalCual", safe(ctx.tratamientoHormonalCual));
    }

    private void cargarSaludReproductiva(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        rep.put("examenReproMasculino", safe(ctx.examenReproMasculino));
        rep.put("tiempoReproMasculino", ctx.tiempoReproMasculino == null ? "" : String.valueOf(ctx.tiempoReproMasculino));
        rep.put("fum", fmtDate(ctx.fum));
        rep.put("gestas", ctx.gestas == null ? "" : String.valueOf(ctx.gestas));
        rep.put("cesareas", ctx.cesareas == null ? "" : String.valueOf(ctx.cesareas));
        rep.put("partos", ctx.partos == null ? "" : String.valueOf(ctx.partos));
        rep.put("abortos", ctx.abortos == null ? "" : String.valueOf(ctx.abortos));
        rep.put("planificacion", safe(ctx.planificacion));
        rep.put("planificacionCual", safe(ctx.planificacionCual));
        rep.put("gineco_examen1", safe(ctx.ginecoExamen1));
        rep.put("gineco_tiempo1", safe(ctx.ginecoTiempo1));
        rep.put("gineco_resultado1", safe(ctx.ginecoResultado1));
        rep.put("gineco_examen2", safe(ctx.ginecoExamen2));
        rep.put("gineco_tiempo2", safe(ctx.ginecoTiempo2));
        rep.put("gineco_resultado2", safe(ctx.ginecoResultado2));
        rep.put("gineco_observacion", safe(ctx.ginecoObservacion));
    }

    private void cargarSignosVitales(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        rep.put("temp", ctx.temp == null ? "" : String.valueOf(ctx.temp));
        rep.put("paStr", safe(ctx.paStr));
        rep.put("fc", ctx.fc == null ? "" : String.valueOf(ctx.fc));
        rep.put("fr", ctx.fr == null ? "" : String.valueOf(ctx.fr));
        rep.put("satO2", ctx.satO2 == null ? "" : String.valueOf(ctx.satO2));
        rep.put("peso", ctx.peso == null ? "" : String.valueOf(ctx.peso));
        rep.put("tallaCm", ctx.tallaCm == null ? "" : String.valueOf(ctx.tallaCm));
        Double imc = resolveImc(ctx);
        rep.put("imc", imc == null ? "" : String.valueOf(imc));
        rep.put("perimetroAbd", ctx.perimetroAbd == null ? "" : String.valueOf(ctx.perimetroAbd));
    }

    private void cargarDatosPuestoYTipoEvaluacion(Map<String, String> rep, FichaPdfViewModelContext ctx) {
        if (ctx.fichaRiesgo != null) {
            rep.put("puestoTrabajo", safe(ctx.fichaRiesgo.getPuestoTrabajo()));
        } else {
            rep.put("puestoTrabajo", "");
        }
        rep.put("tipoEval", safe(ctx.tipoEval));
    }

    private void corregirOtrosRiesgos(Map<String, String> rep, Map<String, String> otrosRiesgos) {
        if (otrosRiesgos == null) {
            return;
        }
        for (Map.Entry<String, String> e : otrosRiesgos.entrySet()) {
            String k = e.getKey();
            if (k == null) {
                continue;
            }
            rep.put(k.toLowerCase(), safe(e.getValue()));
        }
    }

    private void putIntArray0Based(Map<String, String> rep, String prefix, Integer[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + i, arr[i] == null ? "" : String.valueOf(arr[i]));
        }
    }

    private void putBoolArray0Based(Map<String, String> rep, String prefix, Boolean[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + i, Boolean.TRUE.equals(arr[i]) ? "X" : "");
        }
    }

    private void putStringArray0Based(Map<String, String> rep, String prefix, String[] arr) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            rep.put(prefix + i, safe(arr[i]));
        }
    }

    private static String resolveCedulaForPdf(DatEmpleado empleadoSel, PersonaAux personaAux, String cedulaBusqueda) {
        if (empleadoSel != null && empleadoSel.getNoCedula() != null && !empleadoSel.getNoCedula().isBlank()) {
            return empleadoSel.getNoCedula();
        }
        String cedulaPersonaAux = getCedulaIfLoaded(personaAux);
        if (cedulaPersonaAux != null && !cedulaPersonaAux.isBlank()) {
            return cedulaPersonaAux;
        }
        return cedulaBusqueda == null ? "" : cedulaBusqueda;
    }

    private String resolveApellido1(FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = resolveEmpleado(ctx);
        PersonaAux personaAux = resolvePersonaAux(ctx);
        String[] nombreCompleto = splitNombreCompleto(empleado != null ? empleado.getNombreC() : null);
        return firstNotBlank(
                ctx.apellido1,
                personaAux != null ? personaAux.getApellido1() : null,
                empleado != null ? empleado.getPriApellido() : null,
                nombreCompleto[0]);
    }

    private String resolveApellido2(FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = resolveEmpleado(ctx);
        PersonaAux personaAux = resolvePersonaAux(ctx);
        String[] nombreCompleto = splitNombreCompleto(empleado != null ? empleado.getNombreC() : null);
        return firstNotBlank(
                ctx.apellido2,
                personaAux != null ? personaAux.getApellido2() : null,
                empleado != null ? empleado.getSegApellido() : null,
                nombreCompleto[1]);
    }

    private String resolveNombre1(FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = resolveEmpleado(ctx);
        PersonaAux personaAux = resolvePersonaAux(ctx);
        String[] nombresEmpleado = splitNombres(empleado != null ? empleado.getNombres() : null);
        String[] nombreCompleto = splitNombreCompleto(empleado != null ? empleado.getNombreC() : null);
        return firstNotBlank(
                ctx.nombre1,
                personaAux != null ? personaAux.getNombre1() : null,
                nombresEmpleado[0],
                nombreCompleto[2]);
    }

    private String resolveNombre2(FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = resolveEmpleado(ctx);
        PersonaAux personaAux = resolvePersonaAux(ctx);
        String[] nombresEmpleado = splitNombres(empleado != null ? empleado.getNombres() : null);
        String[] nombreCompleto = splitNombreCompleto(empleado != null ? empleado.getNombreC() : null);
        return firstNotBlank(
                ctx.nombre2,
                personaAux != null ? personaAux.getNombre2() : null,
                nombresEmpleado[1],
                nombreCompleto[3]);
    }

    private String resolveSexo(FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = resolveEmpleado(ctx);
        PersonaAux personaAux = resolvePersonaAux(ctx);
        return firstNotBlank(
                ctx.sexo,
                personaAux != null ? personaAux.getSexo() : null,
                empleado != null && empleado.getSexo() != null ? empleado.getSexo().getDescripcion() : null);
    }

    private java.util.Date resolveFechaNacimiento(FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = resolveEmpleado(ctx);
        PersonaAux personaAux = resolvePersonaAux(ctx);
        if (ctx.fechaNacimiento != null) {
            return ctx.fechaNacimiento;
        }
        if (personaAux != null && personaAux.getFechaNac() != null) {
            return personaAux.getFechaNac();
        }
        return empleado != null ? empleado.getfNacimiento() : null;
    }

    private Integer resolveEdad(FichaPdfViewModelContext ctx, java.util.Date fechaNacimiento) {
        if (ctx.edad != null) {
            return ctx.edad;
        }
        if (fechaNacimiento == null) {
            return null;
        }
        LocalDate fn = fechaNacimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(fn, LocalDate.now()).getYears();
    }

    private java.util.Date resolveFecIngreso(FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = resolveEmpleado(ctx);
        if (ctx.fecIngreso != null) {
            return ctx.fecIngreso;
        }
        return empleado != null ? empleado.getfIngreso() : null;
    }

    private java.util.Date resolveFecReintegro(FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = resolveEmpleado(ctx);
        if (ctx.fecReintegro != null) {
            return ctx.fecReintegro;
        }
        return empleado != null ? empleado.getfReingreso() : null;
    }

    private Double resolveImc(FichaPdfViewModelContext ctx) {
        if (ctx.imc != null) {
            return ctx.imc;
        }
        if (ctx.peso == null || ctx.tallaCm == null || ctx.tallaCm <= 0) {
            return null;
        }
        double tallaM = ctx.tallaCm / 100.0d;
        if (tallaM <= 0) {
            return null;
        }
        return ctx.peso / (tallaM * tallaM);
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String[] splitNombres(String nombres) {
        if (nombres == null || nombres.trim().isEmpty()) {
            return new String[] {"", ""};
        }
        String[] parts = nombres.trim().split("\\s+", 2);
        String nombre1 = parts.length > 0 ? parts[0] : "";
        String nombre2 = parts.length > 1 ? parts[1] : "";
        return new String[] {nombre1, nombre2};
    }

    private String[] splitNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return new String[] {"", "", "", ""};
        }
        String[] parts = nombreCompleto.trim().split("\\s+");
        String apellido1 = parts.length > 0 ? parts[0] : "";
        String apellido2 = parts.length > 1 ? parts[1] : "";
        String nombre1 = parts.length > 2 ? parts[2] : "";
        String nombre2 = "";
        if (parts.length > 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < parts.length; i++) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(parts[i]);
            }
            nombre2 = sb.toString();
        }
        return new String[] {apellido1, apellido2, nombre1, nombre2};
    }

    private DatEmpleado resolveEmpleado(FichaPdfViewModelContext ctx) {
        DatEmpleado empleado = ctx.empleadoSel != null
                ? ctx.empleadoSel
                : (ctx.ficha != null ? ctx.ficha.getEmpleado() : null);
        return resolveManagedEmpleado(empleado);
    }

    private DatEmpleado resolveManagedEmpleado(DatEmpleado empleadoSel) {
        if (empleadoSel == null) {
            return null;
        }
        Integer noPersona = resolveNoPersona(empleadoSel);
        if (noPersona == null || empleadoService == null) {
            return empleadoSel;
        }
        try {
            DatEmpleado managed = empleadoService.buscarPorId(noPersona);
            return managed != null ? managed : empleadoSel;
        } catch (RuntimeException ex) {
            return empleadoSel;
        }
    }

    private Integer resolveNoPersona(DatEmpleado empleadoSel) {
        try {
            return empleadoSel.getNoPersona();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private PersonaAux resolvePersonaAux(FichaPdfViewModelContext ctx) {
        if (ctx.personaAux != null) {
            return ctx.personaAux;
        }
        return ctx.ficha != null ? ctx.ficha.getPersonaAux() : null;
    }

    private static String getCedulaIfLoaded(PersonaAux personaAux) {
        if (personaAux == null) {
            return null;
        }
        try {
            boolean loaded = Persistence.getPersistenceUtil().isLoaded(personaAux, "cedula");
            if (!loaded) {
                return null;
            }
            return personaAux.getCedula();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String markX(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? "X" : "";
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            return "";
        }
        String u = s.toUpperCase(Locale.ROOT);
        return ("X".equals(u) || "SI".equals(u) || "S".equals(u) || "TRUE".equals(u) || "1".equals(u)) ? "X" : "";
    }

    private static String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String fmtDate(java.util.Date d) {
        if (d == null) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "EC")).format(d);
    }

    public static class FichaPdfViewModelContext implements Serializable {
        public FichaOcupacional ficha;
        public FichaRiesgo fichaRiesgo;
        public DatEmpleado empleadoSel;
        public PersonaAux personaAux;
        public String cedulaBusqueda;
        public String institucion;
        public String ruc;
        public String centroTrabajo;
        public String ciiu;
        public String noHistoria;
        public String noArchivo;
        public String apellido1;
        public String apellido2;
        public String nombre1;
        public String nombre2;
        public String sexo;
        public java.util.Date fechaNacimiento;
        public Integer edad;
        public String grupoSanguineo;
        public String lateralidad;
        public Integer[] consTiempoConsumoMeses;
        public Boolean[] consExConsumidor;
        public Integer[] consTiempoAbstinenciaMeses;
        public Boolean[] consNoConsume;
        public String consOtrasCual;
        public String[] afCual;
        public String[] afTiempo;
        public String[] medCual;
        public Integer[] medCant;
        public String consumoVidaCondObs;
        public String recomendaciones;
        public java.util.Date fechaAtencion;
        public java.util.Date fecIngreso;
        public java.util.Date fecReintegro;
        public java.util.Date fecRetiro;
        public String motivoObs;
        public String condicionEspecial;
        public String autorizaTransfusion;
        public String tratamientoHormonal;
        public String tratamientoHormonalCual;
        public String examenReproMasculino;
        public Integer tiempoReproMasculino;
        public java.util.Date fum;
        public Integer gestas;
        public Integer cesareas;
        public Integer partos;
        public Integer abortos;
        public String planificacion;
        public String planificacionCual;
        public Double temp;
        public String paStr;
        public Integer fc;
        public Integer fr;
        public Integer satO2;
        public Double peso;
        public Double tallaCm;
        public Double imc;
        public Double perimetroAbd;
        public String ginecoExamen1;
        public String ginecoTiempo1;
        public String ginecoResultado1;
        public String ginecoExamen2;
        public String ginecoTiempo2;
        public String ginecoResultado2;
        public String ginecoObservacion;
        public String tipoEval;
        public String enfermedadActual;
        public Map<String, String> otrosRiesgos;
        public String exfPielCicatrices;
        public String exfOjosParpados;
        public String exfOjosConjuntivas;
        public String exfOjosPupilas;
        public String exfOjosCornea;
        public String exfOjosMotilidad;
        public String exfOidoConducto;
        public String exfOidoPabellon;
        public String exfOidoTimpanos;
        public String exfOroLabios;
        public String exfOroLengua;
        public String exfOroFaringe;
        public String exfOroAmigdalas;
        public String exfOroDentadura;
        public String exfNarizTabique;
        public String exfNarizCornetes;
        public String exfNarizMucosas;
        public String exfNarizSenos;
        public String exfCuelloTiroides;
        public String exfCuelloMovilidad;
        public String exfToraxMamas;
        public String exfToraxPulmones;
        public String exfToraxCorazon;
        public String exfToraxParrilla;
        public String exfAbdomenVisceras;
        public String exfAbdomenPared;
        public String exfColumnaFlexibilidad;
        public String exfColumnaDesviacion;
        public String exfColumnaDolor;
        public String exfPelvisPelvis;
        public String exfPelvisGenitales;
        public String exfExtVascular;
        public String exfExtSup;
        public String exfExtInf;
        public String exfNeuroFuerza;
        public String exfNeuroSensibilidad;
        public String exfNeuroMarcha;
        public String exfNeuroReflejos;
        public String obsExamenFisico;
    }
}
