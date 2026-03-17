package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.ejb.Stateless;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfPlaceholderAssembler.FichaState;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfDataMapper;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfMappedData;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfViewModelBuilder;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfViewModelBuilder.FichaPdfViewModelContext;

@Stateless
/**
 * Class FichaPdfContextAssembler: gestiona la construcción y renderización de documentos PDF.
 */
public class FichaPdfContextAssembler implements Serializable {

    public FichaPdfMappedData syncCamposDesdeObjetos(
            FichaPdfDataMapper fichaPdfDataMapper,
            FichaOcupacional ficha,
            DatEmpleado empleadoSel,
            PersonaAux personaAux,
            java.util.Date fechaNacimiento) {
        return fichaPdfDataMapper.map(ficha, empleadoSel, personaAux, fechaNacimiento);
    }

    public FichaState buildFichaState(
            Object source,
            CentroMedicoPdfFacade centroMedicoPdfFacade,
            Logger log,
            FichaPdfViewModelBuilder fichaPdfViewModelBuilder,
            FichaPdfViewModelContext fichaPdfViewModelContext,
            String fallbackObservacion,
            BiFunction<List<?>, Integer, Object> getSafe,
            Function<Object, java.util.Date> toDateParser) {
        FichaState state = new FichaState();
        copyMatchingFields(source, state);
        state.centroMedicoPdfFacade = centroMedicoPdfFacade;
        state.log = log;
        state.fichaPdfViewModelBuilder = fichaPdfViewModelBuilder;
        state.fichaPdfViewModelContext = fichaPdfViewModelContext;
        state.fallbackObservacion = fallbackObservacion;
        state.getSafe = getSafe;
        state.toDateParser = toDateParser;
        return state;
    }

    public FichaPdfViewModelContext buildFichaPdfViewModelContext(Object source) {
        FichaPdfViewModelContext ctx = new FichaPdfViewModelContext();
        copyMatchingFields(source, ctx);
        enrichExamenFisicoRegion(ctx);
        return ctx;
    }

    private void enrichExamenFisicoRegion(FichaPdfViewModelContext ctx) {
        ctx.exfPielCicatrices = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfPielCicatrices() : null, ctx.exfPielCicatrices);
        ctx.exfOjosParpados = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOjosParpados() : null, ctx.exfOjosParpados);
        ctx.exfOjosConjuntivas = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOjosConjuntivas() : null, ctx.exfOjosConjuntivas);
        ctx.exfOjosPupilas = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOjosPupilas() : null, ctx.exfOjosPupilas);
        ctx.exfOjosCornea = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOjosCornea() : null, ctx.exfOjosCornea);
        ctx.exfOjosMotilidad = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOjosMotilidad() : null, ctx.exfOjosMotilidad);
        ctx.exfOidoConducto = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOidoConducto() : null, ctx.exfOidoConducto);
        ctx.exfOidoPabellon = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOidoPabellon() : null, ctx.exfOidoPabellon);
        ctx.exfOidoTimpanos = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOidoTimpanos() : null, ctx.exfOidoTimpanos);
        ctx.exfOroLabios = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOroLabios() : null, ctx.exfOroLabios);
        ctx.exfOroLengua = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOroLengua() : null, ctx.exfOroLengua);
        ctx.exfOroFaringe = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOroFaringe() : null, ctx.exfOroFaringe);
        ctx.exfOroAmigdalas = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOroAmigdalas() : null, ctx.exfOroAmigdalas);
        ctx.exfOroDentadura = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfOroDentadura() : null, ctx.exfOroDentadura);
        ctx.exfNarizTabique = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfNarizTabique() : null, ctx.exfNarizTabique);
        ctx.exfNarizCornetes = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfNarizCornetes() : null, ctx.exfNarizCornetes);
        ctx.exfNarizMucosas = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfNarizMucosas() : null, ctx.exfNarizMucosas);
        ctx.exfNarizSenos = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfNarizSenosParanasa() : null, ctx.exfNarizSenos);
        ctx.exfCuelloTiroides = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfCuelloTiroidesMasas() : null, ctx.exfCuelloTiroides);
        ctx.exfCuelloMovilidad = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfCuelloMovilidad() : null, ctx.exfCuelloMovilidad);
        ctx.exfToraxMamas = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfToraxMamas() : null, ctx.exfToraxMamas);
        ctx.exfToraxPulmones = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfToraxPulmones() : null, ctx.exfToraxPulmones);
        ctx.exfToraxCorazon = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfToraxCorazon() : null, ctx.exfToraxCorazon);
        ctx.exfToraxParrilla = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfToraxParrillaCostal() : null, ctx.exfToraxParrilla);
        ctx.exfAbdomenVisceras = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfAbdVisceras() : null, ctx.exfAbdomenVisceras);
        ctx.exfAbdomenPared = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfAbdParedAbdominal() : null, ctx.exfAbdomenPared);
        ctx.exfColumnaFlexibilidad = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfColFlexibilidad() : null, ctx.exfColumnaFlexibilidad);
        ctx.exfColumnaDesviacion = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfColDesviacion() : null, ctx.exfColumnaDesviacion);
        ctx.exfColumnaDolor = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfColDolor() : null, ctx.exfColumnaDolor);
        ctx.exfPelvisPelvis = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfPelvisPelvis() : null, ctx.exfPelvisPelvis);
        ctx.exfPelvisGenitales = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfPelvisGenitales() : null, ctx.exfPelvisGenitales);
        ctx.exfExtVascular = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfExtVascular() : null, ctx.exfExtVascular);
        ctx.exfExtSup = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfExtMiembrosSup() : null, ctx.exfExtSup);
        ctx.exfExtInf = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfExtMiembrosInf() : null, ctx.exfExtInf);
        ctx.exfNeuroFuerza = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfNeuroFuerza() : null, ctx.exfNeuroFuerza);
        ctx.exfNeuroSensibilidad = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfNeuroSensibilidad() : null, ctx.exfNeuroSensibilidad);
        ctx.exfNeuroMarcha = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfNeuroMarcha() : null, ctx.exfNeuroMarcha);
        ctx.exfNeuroReflejos = PdfTextUtil.preferCurrent(ctx.ficha != null ? ctx.ficha.getExfNeuroReflejos() : null, ctx.exfNeuroReflejos);
    }

    private void copyMatchingFields(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        for (Field targetField : target.getClass().getFields()) {
            if (Modifier.isStatic(targetField.getModifiers())) {
                continue;
            }
            Field sourceField = findField(source.getClass(), targetField.getName());
            if (sourceField == null) {
                continue;
            }
            Object value = getFieldValue(sourceField, source);
            if (value == null || !isAssignable(targetField.getType(), value.getClass())) {
                continue;
            }
            setFieldValue(targetField, target, value);
        }
    }

    private Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ex) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private Object getFieldValue(Field field, Object source) {
        try {
            field.setAccessible(true);
            return field.get(source);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("No se pudo leer campo '" + field.getName() + "'", ex);
        }
    }

    private void setFieldValue(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("No se pudo escribir campo '" + field.getName() + "'", ex);
        }
    }

    private boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }
        if (!targetType.isPrimitive()) {
            return false;
        }
        return (targetType == int.class && sourceType == Integer.class)
                || (targetType == long.class && sourceType == Long.class)
                || (targetType == double.class && sourceType == Double.class)
                || (targetType == float.class && sourceType == Float.class)
                || (targetType == boolean.class && sourceType == Boolean.class)
                || (targetType == byte.class && sourceType == Byte.class)
                || (targetType == short.class && sourceType == Short.class)
                || (targetType == char.class && sourceType == Character.class);
    }
}
