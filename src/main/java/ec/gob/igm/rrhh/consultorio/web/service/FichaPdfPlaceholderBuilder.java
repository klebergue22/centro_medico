package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Supplier;

import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;

@Stateless
public class FichaPdfPlaceholderBuilder implements Serializable {

    public String construirHtmlFichaDesdePlantilla(
            Supplier<String> templateLoader,
            Runnable syncData,
            Supplier<Map<String, String>> reemplazosSupplier,
            Supplier<String> tipoEvaluacionSupplier,
            CentroMedicoPdfFacade facade) throws IOException {

        String template = templateLoader.get();
        syncData.run();

        Map<String, String> rep = reemplazosSupplier.get();
        String html = facade.aplicarReemplazos(template, rep);
        html = facade.aplicarBloquesSexo(html, rep.get("sexo"));
        html = facade.aplicarBloquesTipoEvaluacion(html, tipoEvaluacionSupplier.get());

        return html;
    }
}
