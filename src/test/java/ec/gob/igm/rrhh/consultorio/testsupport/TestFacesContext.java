package ec.gob.igm.rrhh.consultorio.testsupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;

/**
 * Stub pequeño de {@link FacesContext} para pruebas unitarias/funcionales
 * sin necesidad de arrancar JSF.
 */
public class TestFacesContext extends FacesContext {

    private final List<FacesMessage> messages = new ArrayList<>();
    private UIViewRoot viewRoot;

    public TestFacesContext() {
        setCurrentInstance(this);
    }

    public List<FacesMessage> getCapturedMessages() {
        return List.copyOf(messages);
    }

    public void clearMessages() {
        messages.clear();
    }

    @Override
    public Application getApplication() {
        return null;
    }

    @Override
    public Iterator<String> getClientIdsWithMessages() {
        return Collections.emptyIterator();
    }

    @Override
    public Lifecycle getLifecycle() {
        return null;
    }

    @Override
    public ExternalContext getExternalContext() {
        return null;
    }

    @Override
    public FacesMessage.Severity getMaximumSeverity() {
        if (messages.isEmpty()) {
            return null;
        }
        FacesMessage.Severity max = messages.get(0).getSeverity();
        for (FacesMessage message : messages) {
            if (message != null && message.getSeverity() != null
                    && message.getSeverity().getOrdinal() > max.getOrdinal()) {
                max = message.getSeverity();
            }
        }
        return max;
    }

    @Override
    public Iterator<FacesMessage> getMessages() {
        return messages.iterator();
    }

    @Override
    public Iterator<FacesMessage> getMessages(String clientId) {
        return messages.iterator();
    }

    @Override
    public RenderKit getRenderKit() {
        return null;
    }

    @Override
    public boolean getRenderResponse() {
        return false;
    }

    @Override
    public boolean getResponseComplete() {
        return false;
    }

    @Override
    public ResponseStream getResponseStream() {
        return null;
    }

    @Override
    public void setResponseStream(ResponseStream responseStream) {
        // No-op para pruebas.
    }

    @Override
    public ResponseWriter getResponseWriter() {
        return null;
    }

    @Override
    public void setResponseWriter(ResponseWriter responseWriter) {
        // No-op para pruebas.
    }

    @Override
    public UIViewRoot getViewRoot() {
        return viewRoot;
    }

    @Override
    public void setViewRoot(UIViewRoot root) {
        this.viewRoot = root;
    }

    @Override
    public void addMessage(String clientId, FacesMessage message) {
        messages.add(message);
    }

    @Override
    public void release() {
        setCurrentInstance(null);
        messages.clear();
    }

    @Override
    public void renderResponse() {
        // No-op para pruebas.
    }

    @Override
    public void responseComplete() {
        // No-op para pruebas.
    }
}
