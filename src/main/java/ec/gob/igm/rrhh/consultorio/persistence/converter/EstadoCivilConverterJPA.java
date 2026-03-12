
package ec.gob.igm.rrhh.consultorio.persistence.converter;

import ec.gob.igm.rrhh.consultorio.domain.enums.EstadoCivil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EstadoCivilConverterJPA implements AttributeConverter<EstadoCivil, String> {

    @Override
    public String convertToDatabaseColumn(EstadoCivil attribute) {
        return attribute != null ? attribute.getCodigo() : null;
    }

    @Override
    public EstadoCivil convertToEntityAttribute(String dbData) {
        return dbData != null ? EstadoCivil.fromCodigo(dbData) : null;
    }
}

