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
        FichaOcupacional ficha = ctx.ficha;
        enrichPielYOjos(ctx, ficha);
        enrichOidoYOrofaringe(ctx, ficha);
        enrichNarizYCuello(ctx, ficha);
        enrichTorsoYColumna(ctx, ficha);
        enrichPelvisExtremidadesYNeuro(ctx, ficha);
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
            Object value;
            if (sourceField != null) {
                value = getFieldValue(sourceField, source);
            } else {
                value = getPropertyValueByGetter(source, targetField.getName());
            }
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

    private Object getPropertyValueByGetter(Object source, String fieldName) {
        String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String[] getterNames = new String[] {"get" + suffix, "is" + suffix};
        for (String getterName : getterNames) {
            try {
                java.lang.reflect.Method method = source.getClass().getMethod(getterName);
                if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0) {
                    continue;
                }
                return method.invoke(source);
            } catch (NoSuchMethodException ex) {
                // Probar siguiente alternativa de getter
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("No se pudo leer propiedad '" + fieldName + "'", ex);
            }
        }
        return null;
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

    private void enrichPielYOjos(FichaPdfViewModelContext ctx, FichaOcupacional ficha) {
        ctx.exfPielCicatrices = preferCurrent(ficha != null ? ficha.getExfPielCicatrices() : null, ctx.exfPielCicatrices);
        ctx.exfOjosParpados = preferCurrent(ficha != null ? ficha.getExfOjosParpados() : null, ctx.exfOjosParpados);
        ctx.exfOjosConjuntivas = preferCurrent(ficha != null ? ficha.getExfOjosConjuntivas() : null, ctx.exfOjosConjuntivas);
        ctx.exfOjosPupilas = preferCurrent(ficha != null ? ficha.getExfOjosPupilas() : null, ctx.exfOjosPupilas);
        ctx.exfOjosCornea = preferCurrent(ficha != null ? ficha.getExfOjosCornea() : null, ctx.exfOjosCornea);
        ctx.exfOjosMotilidad = preferCurrent(ficha != null ? ficha.getExfOjosMotilidad() : null, ctx.exfOjosMotilidad);
    }

    private void enrichOidoYOrofaringe(FichaPdfViewModelContext ctx, FichaOcupacional ficha) {
        ctx.exfOidoConducto = preferCurrent(ficha != null ? ficha.getExfOidoConducto() : null, ctx.exfOidoConducto);
        ctx.exfOidoPabellon = preferCurrent(ficha != null ? ficha.getExfOidoPabellon() : null, ctx.exfOidoPabellon);
        ctx.exfOidoTimpanos = preferCurrent(ficha != null ? ficha.getExfOidoTimpanos() : null, ctx.exfOidoTimpanos);
        ctx.exfOroLabios = preferCurrent(ficha != null ? ficha.getExfOroLabios() : null, ctx.exfOroLabios);
        ctx.exfOroLengua = preferCurrent(ficha != null ? ficha.getExfOroLengua() : null, ctx.exfOroLengua);
        ctx.exfOroFaringe = preferCurrent(ficha != null ? ficha.getExfOroFaringe() : null, ctx.exfOroFaringe);
        ctx.exfOroAmigdalas = preferCurrent(ficha != null ? ficha.getExfOroAmigdalas() : null, ctx.exfOroAmigdalas);
        ctx.exfOroDentadura = preferCurrent(ficha != null ? ficha.getExfOroDentadura() : null, ctx.exfOroDentadura);
    }

    private void enrichNarizYCuello(FichaPdfViewModelContext ctx, FichaOcupacional ficha) {
        ctx.exfNarizTabique = preferCurrent(ficha != null ? ficha.getExfNarizTabique() : null, ctx.exfNarizTabique);
        ctx.exfNarizCornetes = preferCurrent(ficha != null ? ficha.getExfNarizCornetes() : null, ctx.exfNarizCornetes);
        ctx.exfNarizMucosas = preferCurrent(ficha != null ? ficha.getExfNarizMucosas() : null, ctx.exfNarizMucosas);
        ctx.exfNarizSenos = preferCurrent(ficha != null ? ficha.getExfNarizSenosParanasa() : null, ctx.exfNarizSenos);
        ctx.exfCuelloTiroides = preferCurrent(ficha != null ? ficha.getExfCuelloTiroidesMasas() : null, ctx.exfCuelloTiroides);
        ctx.exfCuelloMovilidad = preferCurrent(ficha != null ? ficha.getExfCuelloMovilidad() : null, ctx.exfCuelloMovilidad);
    }

    private void enrichTorsoYColumna(FichaPdfViewModelContext ctx, FichaOcupacional ficha) {
        ctx.exfToraxMamas = preferCurrent(ficha != null ? ficha.getExfToraxMamas() : null, ctx.exfToraxMamas);
        ctx.exfToraxPulmones = preferCurrent(ficha != null ? ficha.getExfToraxPulmones() : null, ctx.exfToraxPulmones);
        ctx.exfToraxCorazon = preferCurrent(ficha != null ? ficha.getExfToraxCorazon() : null, ctx.exfToraxCorazon);
        ctx.exfToraxParrilla = preferCurrent(ficha != null ? ficha.getExfToraxParrillaCostal() : null, ctx.exfToraxParrilla);
        ctx.exfAbdomenVisceras = preferCurrent(ficha != null ? ficha.getExfAbdVisceras() : null, ctx.exfAbdomenVisceras);
        ctx.exfAbdomenPared = preferCurrent(ficha != null ? ficha.getExfAbdParedAbdominal() : null, ctx.exfAbdomenPared);
        ctx.exfColumnaFlexibilidad = preferCurrent(ficha != null ? ficha.getExfColFlexibilidad() : null, ctx.exfColumnaFlexibilidad);
        ctx.exfColumnaDesviacion = preferCurrent(ficha != null ? ficha.getExfColDesviacion() : null, ctx.exfColumnaDesviacion);
        ctx.exfColumnaDolor = preferCurrent(ficha != null ? ficha.getExfColDolor() : null, ctx.exfColumnaDolor);
    }

    private void enrichPelvisExtremidadesYNeuro(FichaPdfViewModelContext ctx, FichaOcupacional ficha) {
        ctx.exfPelvisPelvis = preferCurrent(ficha != null ? ficha.getExfPelvisPelvis() : null, ctx.exfPelvisPelvis);
        ctx.exfPelvisGenitales = preferCurrent(ficha != null ? ficha.getExfPelvisGenitales() : null, ctx.exfPelvisGenitales);
        ctx.exfExtVascular = preferCurrent(ficha != null ? ficha.getExfExtVascular() : null, ctx.exfExtVascular);
        ctx.exfExtSup = preferCurrent(ficha != null ? ficha.getExfExtMiembrosSup() : null, ctx.exfExtSup);
        ctx.exfExtInf = preferCurrent(ficha != null ? ficha.getExfExtMiembrosInf() : null, ctx.exfExtInf);
        ctx.exfNeuroFuerza = preferCurrent(ficha != null ? ficha.getExfNeuroFuerza() : null, ctx.exfNeuroFuerza);
        ctx.exfNeuroSensibilidad = preferCurrent(ficha != null ? ficha.getExfNeuroSensibilidad() : null, ctx.exfNeuroSensibilidad);
        ctx.exfNeuroMarcha = preferCurrent(ficha != null ? ficha.getExfNeuroMarcha() : null, ctx.exfNeuroMarcha);
        ctx.exfNeuroReflejos = preferCurrent(ficha != null ? ficha.getExfNeuroReflejos() : null, ctx.exfNeuroReflejos);
    }

    private String preferCurrent(String current, String fallback) {
        return PdfTextUtil.preferCurrent(current, fallback);
    }
}
