package com.expensetracker.filter;

import com.expensetracker.model.Role;
import com.expensetracker.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication Filter enforcing session authentication on private endpoints
 * and preventing unauthorized access or browser cache leaks.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No special initialization required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());

        // Redirect root "/" to appropriate destination
        if (path.equals("/") || path.isEmpty()) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                res.sendRedirect(contextPath + "/dashboard");
            } else {
                res.sendRedirect(contextPath + "/login");
            }
            return;
        }

        // Check if path is a public resource
        boolean isPublicResource = path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.equals("/favicon.ico") ||
                path.equals("/login") ||
                path.equals("/login.jsp") ||
                path.equals("/register") ||
                path.equals("/register.jsp") ||
                path.equals("/logout");

        if (isPublicResource) {
            chain.doFilter(request, response);
            return;
        }

        // Check active user session
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            // Not authenticated: redirect to login
            res.sendRedirect(contextPath + "/login?authRequired=true");
            return;
        }

        // Cache control headers for authenticated pages to prevent back-button data viewing after logout
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        // Role-based access control: /admin routes require ADMIN role
        if (path.startsWith("/admin")) {
            if (currentUser.getRole() != Role.ADMIN) {
                res.sendRedirect(contextPath + "/dashboard?error=Unauthorized+access");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if necessary
    }
}
