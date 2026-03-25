package ec.gob.igm.rrhh.consultorio.web.servlet;

/**
 *
 * @author GUERRA_KLEBER
 */

import ec.gob.igm.rrhh.consultorio.web.pdf.PdfSessionStore;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/pdf")
/**
 * Class PdfServlet: contiene la lÃ³gica de la aplicaciÃ³n.
 */
public class PdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private transient PdfSessionStore pdfSessionStore;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            pdfSessionStore = CDI.current().select(PdfSessionStore.class).get();
        } catch (Exception ex) {
            throw new ServletException("No se pudo inicializar PdfSessionStore", ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token = requireToken(req, resp);
        if (token == null) {
            return;
        }

        HttpSession session = requireSession(req, resp);
        if (session == null) {
            return;
        }

        byte[] bytes = findPdfBytes(session, token, resp);
        if (bytes == null) {
            return;
        }

        preparePdfResponse(req, resp, token, bytes.length);
        writePdf(resp, bytes);
    }

    private String requireToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = req.getParameter("token");
        if (token != null && !token.trim().isEmpty()) {
            return token;
        }
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Token requerido");
        return null;
    }

    private HttpSession requireSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            return session;
        }
        resp.sendError(HttpServletResponse.SC_GONE, "SesiÃ³n expirada");
        return null;
    }

    private byte[] findPdfBytes(HttpSession session, String token, HttpServletResponse resp) throws IOException {
        byte[] bytes = pdfSessionStore.find(session, token);
        if (bytes != null && bytes.length > 0) {
            return bytes;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF no disponible");
        return null;
    }

    private void preparePdfResponse(HttpServletRequest req, HttpServletResponse resp, String token, int contentLength) {
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setContentType("application/pdf");
        resp.setContentLength(contentLength);
        String disposition = isDownloadRequested(req) ? "attachment" : "inline";
        resp.setHeader("Content-Disposition", disposition + "; filename=\"" + resolveFilename(token) + "\"");
    }

    private boolean isDownloadRequested(HttpServletRequest req) {
        return "1".equals(req.getParameter("download"))
                || "true".equalsIgnoreCase(req.getParameter("download"));
    }

    private String resolveFilename(String token) {
        if (token.startsWith("FICHA_")) {
            return "Ficha.pdf";
        }
        if (token.startsWith("CERT_")) {
            return "Certificado.pdf";
        }
        return "Documento.pdf";
    }

    private void writePdf(HttpServletResponse resp, byte[] bytes) throws IOException {
        try (OutputStream out = resp.getOutputStream()) {
            out.write(bytes);
            out.flush();
        }
    }
}
