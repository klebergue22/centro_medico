package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.io.Serializable;

import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfDataMapper;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfMappedData;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfViewModelBuilder.FichaPdfViewModelContext;

@Stateless
public class FichaPdfContextAssembler implements Serializable {

    public FichaPdfMappedData syncCamposDesdeObjetos(
            FichaPdfDataMapper fichaPdfDataMapper,
            FichaOcupacional ficha,
            DatEmpleado empleadoSel,
            java.util.Date fechaNacimiento) {
        return fichaPdfDataMapper.map(ficha, empleadoSel, fechaNacimiento);
    }

    public FichaPdfViewModelContext buildFichaPdfViewModelContext(FichaPdfViewModelContext ctx) {
        return ctx;
    }
}
