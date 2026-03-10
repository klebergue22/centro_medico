package ec.gob.igm.rrhh.consultorio.web.servlet;




import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@WebServlet("/pdf")
public class PdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        final String token = req.getParameter("token"); // FICHA_xxx o CERT_xxx
        if (token == null || token.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Token requerido");
            return;
        }

        final HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_GONE, "Sesión expirada");
            return;
        }

        byte[] bytes = getBytesFromSession(session, token);

        if (bytes == null || bytes.length == 0) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF no disponible");
            return;
        }

        // cache-control (evita cosas raras con preview)
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        resp.setContentType("application/pdf");
        resp.setContentLength(bytes.length);

        final boolean download = "1".equals(req.getParameter("download"))
                              || "true".equalsIgnoreCase(req.getParameter("download"));

        final String filename = token.startsWith("FICHA_") ? "Ficha.pdf"
                             : token.startsWith("CERT_")  ? "Certificado.pdf"
                             : "Documento.pdf";

        final String disposition = download ? "attachment" : "inline";
        resp.setHeader("Content-Disposition", disposition + "; filename=\"" + filename + "\"");

        try (OutputStream out = resp.getOutputStream()) {
            out.write(bytes);
            out.flush();
        }

        // Si quieres token “de un solo uso”, descomenta:
        // session.removeAttribute(token);
    }

    private byte[] getBytesFromSession(HttpSession session, String token) {
        // 1) Forma directa: session.setAttribute(token, bytes)
        Object obj = session.getAttribute(token);
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }

        // 2) Forma Map: session.setAttribute("PDF_STORE", map)
        Object storeObj = session.getAttribute("PDF_STORE");
        if (storeObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, byte[]> store = (Map<String, byte[]>) storeObj;
            return store.get(token);
        }

        return null;
    }
}
