package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.view.ViewDeclarationLanguage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
/**
 * Class JsfFaceletRenderService: orquesta la logica de presentacion y flujo web.
 */
public class JsfFaceletRenderService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(JsfFaceletRenderService.class);

    public String renderFaceletToHtml(String viewId) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            throw new IllegalStateException("FacesContext es null. Solo dentro de request JSF.");
        }

        LOG.info("FichaPrint: renderFaceletToHtml START viewId={}", viewId);
        UIViewRoot originalViewRoot = fc.getViewRoot();
        ResponseWriter originalWriter = fc.getResponseWriter();
        PartialViewContext pvc = fc.getPartialViewContext();
        boolean hadPvc = pvc != null;
        boolean oldRenderAll = enableFullRender(pvc, hadPvc);

        try {
            String html = renderTemporaryView(fc, viewId);
            LOG.info("FichaPrint: renderFaceletToHtml END viewId={} htmlLen={}", viewId, html != null ? html.length() : -1);
            return html;
        } catch (Exception e) {
            LOG.error("FichaPrint: renderFaceletToHtml ERROR viewId={}", viewId, e);
            throw new RuntimeException("Error renderizando facelet " + viewId, e);
        } finally {
            restoreOriginalContext(fc, originalWriter, originalViewRoot, pvc, hadPvc, oldRenderAll);
        }
    }

    private boolean enableFullRender(PartialViewContext pvc, boolean hadPvc) {
        boolean oldRenderAll = false;
        if (hadPvc) {
            try {
                oldRenderAll = pvc.isRenderAll();
                pvc.setRenderAll(true);
            } catch (Exception ignore) {
            }
        }
        return oldRenderAll;
    }

    private String renderTemporaryView(FacesContext fc, String viewId) throws java.io.IOException {
        ViewDeclarationLanguage vdl = fc.getApplication().getViewHandler().getViewDeclarationLanguage(fc, viewId);
        UIViewRoot tempViewRoot = createTemporaryView(fc, viewId, vdl);
        StringWriter sw = new StringWriter(128 * 1024);
        ResponseWriter rw = fc.getRenderKit().createResponseWriter(new PrintWriter(sw), "text/html", "UTF-8");
        fc.setViewRoot(tempViewRoot);
        fc.setResponseWriter(rw);
        fc.getApplication().getViewHandler().renderView(fc, tempViewRoot);
        rw.flush();
        return sw.toString();
    }

    private UIViewRoot createTemporaryView(FacesContext fc, String viewId, ViewDeclarationLanguage vdl) throws java.io.IOException {
        UIViewRoot tempViewRoot = vdl.createView(fc, viewId);
        tempViewRoot.setLocale(fc.getViewRoot() != null ? fc.getViewRoot().getLocale() : fc.getApplication().getDefaultLocale());
        tempViewRoot.setRenderKitId(fc.getApplication().getViewHandler().calculateRenderKitId(fc));
        vdl.buildView(fc, tempViewRoot);
        return tempViewRoot;
    }

    private void restoreOriginalContext(FacesContext fc, ResponseWriter originalWriter, UIViewRoot originalViewRoot,
            PartialViewContext pvc, boolean hadPvc, boolean oldRenderAll) {
        try {
            fc.setResponseWriter(originalWriter);
        } catch (Exception ignore) {
        }
        try {
            fc.setViewRoot(originalViewRoot);
        } catch (Exception ignore) {
        }
        if (hadPvc) {
            try {
                pvc.setRenderAll(oldRenderAll);
            } catch (Exception ignore) {
            }
        }
    }
}
