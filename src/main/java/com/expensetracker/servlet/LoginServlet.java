package com.expensetracker.servlet;

import com.expensetracker.dao.UserDao;
import com.expensetracker.model.Role;
import com.expensetracker.model.User;
import com.expensetracker.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet handling user authentication and login session lifecycle.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        if ("true".equals(req.getParameter("registered"))) {
            req.setAttribute("successMessage", "Account registered successfully! You can now log in.");
        } else if ("true".equals(req.getParameter("loggedOut"))) {
            req.setAttribute("infoMessage", "You have been logged out securely.");
        } else if ("true".equals(req.getParameter("authRequired"))) {
            req.setAttribute("errorMessage", "Please log in to access this page.");
        }

        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("errorMessage", "Email and Password are required.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        User user = userDao.findByEmail(email.trim());
        if (user != null && PasswordUtil.checkPassword(password, user.getPassword())) {
            // Invalidate existing session to mitigate session fixation attacks
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = req.getSession(true);
            newSession.setAttribute("currentUser", user);

            // Redirect admin users to Admin Panel, regular users to Dashboard
            if (user.getRole() == Role.ADMIN) {
                resp.sendRedirect(req.getContextPath() + "/admin");
            } else {
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            }
        } else {
            req.setAttribute("errorMessage", "Invalid email or password.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
