package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.util.SnUtils;
import jakarta.ejb.Stateless;

@Stateless
public class ExamenFisicoStep1Mapper {

    public void applyUltimosDeTopicos(FichaOcupacional ficha) {
        if (ficha == null) return;

        applyNarizCuelloTorax(ficha);
        applyAbdomenYColumna(ficha);
        applyExtremidades(ficha);
    }

    private void applyNarizCuelloTorax(FichaOcupacional ficha) {
        ficha.setExfNarizSenosParanasa(SnUtils.fromBoolean(ficha.getExfNarizSenosBool()));
        ficha.setExfCuelloTiroidesMasas(SnUtils.fromBoolean(ficha.getExfCuelloTiroidesBool()));
        ficha.setExfToraxParrillaCostal(SnUtils.fromBoolean(ficha.getExfToraxParrillaBool()));
    }

    private void applyAbdomenYColumna(FichaOcupacional ficha) {
        ficha.setExfAbdVisceras(SnUtils.fromBoolean(ficha.getExfAbdomenViscerasBool()));
        ficha.setExfAbdParedAbdominal(SnUtils.fromBoolean(ficha.getExfAbdomenParedBool()));
        ficha.setExfColFlexibilidad(SnUtils.fromBoolean(ficha.getExfColumnaFlexibilidadBool()));
        ficha.setExfColDesviacion(SnUtils.fromBoolean(ficha.getExfColumnaDesviacionBool()));
        ficha.setExfColDolor(SnUtils.fromBoolean(ficha.getExfColumnaDolorBool()));
    }

    private void applyExtremidades(FichaOcupacional ficha) {
        ficha.setExfExtMiembrosSup(SnUtils.fromBoolean(ficha.getExfExtSupBool()));
        ficha.setExfExtMiembrosInf(SnUtils.fromBoolean(ficha.getExfExtInfBool()));
    }
}
