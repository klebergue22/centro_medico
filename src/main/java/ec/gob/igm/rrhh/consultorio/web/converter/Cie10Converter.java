package ec.gob.igm.rrhh.consultorio.web.converter;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(value = "cie10Converter", managed = true)
public class Cie10Converter implements Converter<Cie10> {

    @EJB
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

        Cie10 cie10 = cie10Service.buscarPorCodigo(codigo);
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
}
