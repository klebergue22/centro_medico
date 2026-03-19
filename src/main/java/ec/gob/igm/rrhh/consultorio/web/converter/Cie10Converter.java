package ec.gob.igm.rrhh.consultorio.web.converter;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(value = "cie10Converter", managed = true)
public class Cie10Converter implements Converter<Cie10> {

    @Inject
    private Cie10Service cie10Service;

    @Override
    public Cie10 getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String codigo = value;
        int sep = value.indexOf(" - ");
        if (sep >= 0) {
            codigo = value.substring(0, sep);
        }

        codigo = codigo.trim().toUpperCase();
        if (codigo.isEmpty()) {
            return null;
        }

        Cie10Service service = obtenerCie10Service();
        Cie10 cie10 = service.buscarPorCodigo(codigo);
        if (cie10 != null) {
            return cie10;
        }

        Cie10 temporal = new Cie10();
        temporal.setCodigo(codigo);
        return temporal;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Cie10 value) {
        if (value == null || value.getCodigo() == null || value.getCodigo().trim().isEmpty()) {
            return "";
        }

        return value.getCodigo().trim();
    }

    private Cie10Service obtenerCie10Service() {
        if (cie10Service != null) {
            return cie10Service;
        }

        try {
            Instance<Cie10Service> instancia = CDI.current().select(Cie10Service.class);
            if (instancia.isResolvable()) {
                cie10Service = instancia.get();
            }
        } catch (IllegalStateException | jakarta.enterprise.inject.UnsatisfiedResolutionException
                 | jakarta.enterprise.inject.AmbiguousResolutionException ex) {
            throw new IllegalStateException(
                    "No se pudo resolver Cie10Service para el converter cie10Converter.", ex);
        }

        if (cie10Service == null) {
            throw new IllegalStateException(
                    "Cie10Service no fue inyectado ni resuelto por CDI en el converter cie10Converter.");
        }

        return cie10Service;
    }
}
