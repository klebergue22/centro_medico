package ec.gob.igm.rrhh.consultorio.web.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthFilter implements Filter {

    private static final String KEY_AUTH_USER = "AUTH_USER";
    private static final String KEY_FORCE_CHANGE = "AUTH_PASSWORD_CHANGE_REQUIRED";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();

        if (session == null || session.getAttribute(KEY_AUTH_USER) == null) {
            res.sendRedirect(contextPath + "/login.xhtml");
            return;
        }

        boolean forceChange = Boolean.TRUE.equals(session.getAttribute(KEY_FORCE_CHANGE));
        boolean isChangePage = uri.endsWith("/change-password.xhtml");

        if (forceChange && !isChangePage) {
            res.sendRedirect(contextPath + "/change-password.xhtml");
            return;
        }

        if (!forceChange && isChangePage) {
            res.sendRedirect(contextPath + "/index.xhtml");
            return;
        }

        chain.doFilter(request, response);
    }
}
