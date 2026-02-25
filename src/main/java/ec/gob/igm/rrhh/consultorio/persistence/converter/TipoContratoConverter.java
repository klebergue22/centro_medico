/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.persistence.converter;

/**
 *
 * @author GUERRA_KLEBER
 */
 



import ec.gob.igm.rrhh.consultorio.domain.enums.TipoContrato;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TipoContratoConverter implements AttributeConverter<TipoContrato, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TipoContrato attribute) {
        return attribute != null ? attribute.getCodigo() : null;
    }

    @Override
    public TipoContrato convertToEntityAttribute(Integer dbData) {
        return TipoContrato.fromCodigo(dbData);
    }
}

