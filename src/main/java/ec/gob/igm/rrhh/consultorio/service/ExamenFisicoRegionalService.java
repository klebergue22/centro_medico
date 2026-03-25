package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import jakarta.ejb.Stateless;

/**
 * Encapsula la logica de normalizacion S/N del examen fisico regional.
 *
 * SRP: esta clase concentra unicamente la traduccion de valores checkbox (*Bool)
 * al formato persistido ("S"/"N") dentro de {@link FichaOcupacional}.
 */
@Stateless
public class ExamenFisicoRegionalService {

    public void aplicarExamenFisicoRegional(FichaOcupacional ficha) {
        if (ficha == null) {
            return;
        }

        aplicarPielYOjos(ficha);
        aplicarOidoYOrofaringe(ficha);
        aplicarNarizYCuello(ficha);
        aplicarTorsoYColumna(ficha);
        aplicarPelvisExtremidadesYNeuro(ficha);
    }

    public void aplicarRetiro(FichaOcupacional ficha) {
        if (ficha == null) {
            return;
        }
        ficha.setnRetEval(preferSn(ficha.getnRetEval(), ficha.getnRetEvalBool()));
        ficha.setnRetRelTrab(preferSn(ficha.getnRetRelTrab(), ficha.getnRetRelTrabBool()));
    }

    private String preferSn(String currentSn, Boolean boolValue) {
        if ("S".equalsIgnoreCase(currentSn) || "N".equalsIgnoreCase(currentSn)) {
            return currentSn.toUpperCase();
        }
        return Boolean.TRUE.equals(boolValue) ? "S" : "N";
    }

    private void aplicarPielYOjos(FichaOcupacional ficha) {
        ficha.setExfPielCicatrices(preferSn(ficha.getExfPielCicatrices(), ficha.getExfPielCicatricesBool()));
        ficha.setExfOjosParpados(preferSn(ficha.getExfOjosParpados(), ficha.getExfOjosParpadosBool()));
        ficha.setExfOjosConjuntivas(preferSn(ficha.getExfOjosConjuntivas(), ficha.getExfOjosConjuntivasBool()));
        ficha.setExfOjosPupilas(preferSn(ficha.getExfOjosPupilas(), ficha.getExfOjosPupilasBool()));
        ficha.setExfOjosCornea(preferSn(ficha.getExfOjosCornea(), ficha.getExfOjosCorneaBool()));
        ficha.setExfOjosMotilidad(preferSn(ficha.getExfOjosMotilidad(), ficha.getExfOjosMotilidadBool()));
    }

    private void aplicarOidoYOrofaringe(FichaOcupacional ficha) {
        ficha.setExfOidoConducto(preferSn(ficha.getExfOidoConducto(), ficha.getExfOidoConductoBool()));
        ficha.setExfOidoPabellon(preferSn(ficha.getExfOidoPabellon(), ficha.getExfOidoPabellonBool()));
        ficha.setExfOidoTimpanos(preferSn(ficha.getExfOidoTimpanos(), ficha.getExfOidoTimpanosBool()));
        ficha.setExfOroLabios(preferSn(ficha.getExfOroLabios(), ficha.getExfOroLabiosBool()));
        ficha.setExfOroLengua(preferSn(ficha.getExfOroLengua(), ficha.getExfOroLenguaBool()));
        ficha.setExfOroFaringe(preferSn(ficha.getExfOroFaringe(), ficha.getExfOroFaringeBool()));
        ficha.setExfOroAmigdalas(preferSn(ficha.getExfOroAmigdalas(), ficha.getExfOroAmigdalasBool()));
        ficha.setExfOroDentadura(preferSn(ficha.getExfOroDentadura(), ficha.getExfOroDentaduraBool()));
    }

    private void aplicarNarizYCuello(FichaOcupacional ficha) {
        ficha.setExfNarizTabique(preferSn(ficha.getExfNarizTabique(), ficha.getExfNarizTabiqueBool()));
        ficha.setExfNarizCornetes(preferSn(ficha.getExfNarizCornetes(), ficha.getExfNarizCornetesBool()));
        ficha.setExfNarizMucosas(preferSn(ficha.getExfNarizMucosas(), ficha.getExfNarizMucosasBool()));
        ficha.setExfNarizSenosParanasa(preferSn(ficha.getExfNarizSenosParanasa(), ficha.getExfNarizSenosBool()));
        ficha.setExfCuelloTiroidesMasas(preferSn(ficha.getExfCuelloTiroidesMasas(), ficha.getExfCuelloTiroidesBool()));
        ficha.setExfCuelloMovilidad(preferSn(ficha.getExfCuelloMovilidad(), ficha.getExfCuelloMovilidadBool()));
    }

    private void aplicarTorsoYColumna(FichaOcupacional ficha) {
        ficha.setExfToraxMamas(preferSn(ficha.getExfToraxMamas(), ficha.getExfToraxMamasBool()));
        ficha.setExfToraxPulmones(preferSn(ficha.getExfToraxPulmones(), ficha.getExfToraxPulmonesBool()));
        ficha.setExfToraxCorazon(preferSn(ficha.getExfToraxCorazon(), ficha.getExfToraxCorazonBool()));
        ficha.setExfToraxParrillaCostal(preferSn(ficha.getExfToraxParrillaCostal(), ficha.getExfToraxParrillaBool()));
        ficha.setExfAbdVisceras(preferSn(ficha.getExfAbdVisceras(), ficha.getExfAbdomenViscerasBool()));
        ficha.setExfAbdParedAbdominal(preferSn(ficha.getExfAbdParedAbdominal(), ficha.getExfAbdomenParedBool()));
        ficha.setExfColFlexibilidad(preferSn(ficha.getExfColFlexibilidad(), ficha.getExfColumnaFlexibilidadBool()));
        ficha.setExfColDesviacion(preferSn(ficha.getExfColDesviacion(), ficha.getExfColumnaDesviacionBool()));
        ficha.setExfColDolor(preferSn(ficha.getExfColDolor(), ficha.getExfColumnaDolorBool()));
    }

    private void aplicarPelvisExtremidadesYNeuro(FichaOcupacional ficha) {
        ficha.setExfPelvisPelvis(preferSn(ficha.getExfPelvisPelvis(), ficha.getExfPelvisPelvisBool()));
        ficha.setExfPelvisGenitales(preferSn(ficha.getExfPelvisGenitales(), ficha.getExfPelvisGenitalesBool()));
        ficha.setExfExtVascular(preferSn(ficha.getExfExtVascular(), ficha.getExfExtVascularBool()));
        ficha.setExfExtMiembrosSup(preferSn(ficha.getExfExtMiembrosSup(), ficha.getExfExtSupBool()));
        ficha.setExfExtMiembrosInf(preferSn(ficha.getExfExtMiembrosInf(), ficha.getExfExtInfBool()));
        ficha.setExfNeuroFuerza(preferSn(ficha.getExfNeuroFuerza(), ficha.getExfNeuroFuerzaBool()));
        ficha.setExfNeuroSensibilidad(preferSn(ficha.getExfNeuroSensibilidad(), ficha.getExfNeuroSensibilidadBool()));
        ficha.setExfNeuroMarcha(preferSn(ficha.getExfNeuroMarcha(), ficha.getExfNeuroMarchaBool()));
        ficha.setExfNeuroReflejos(preferSn(ficha.getExfNeuroReflejos(), ficha.getExfNeuroReflejosBool()));
    }
}
