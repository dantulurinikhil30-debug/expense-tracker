package com.expensetracker.servlet;

import com.expensetracker.dao.UserDao;
import com.expensetracker.model.User;
import com.expensetracker.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Servlet handling user registration, input validation, and automatic
 * category and payment mode seeding.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

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
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String bankBalStr = req.getParameter("initialBankBalance");
        String cashBalStr = req.getParameter("initialCashBalance");

        // Preserve input fields for user convenience on error
        req.setAttribute("fullName", fullName);
        req.setAttribute("email", email);
        req.setAttribute("initialBankBalance", bankBalStr);
        req.setAttribute("initialCashBalance", cashBalStr);

        // Validation
        if (fullName == null || fullName.trim().isEmpty()) {
            req.setAttribute("errorMessage", "Full Name is required.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        if (email == null || email.trim().isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            req.setAttribute("errorMessage", "A valid email address is required.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        if (userDao.emailExists(email.trim())) {
            req.setAttribute("errorMessage", "This email address is already registered. Please log in.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        if (password == null || password.length() < 6) {
            req.setAttribute("errorMessage", "Password must be at least 6 characters long.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        if (!password.equals(confirmPassword)) {
            req.setAttribute("errorMessage", "Passwords do not match.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        BigDecimal initialBankBalance = BigDecimal.ZERO;
        BigDecimal initialCashBalance = BigDecimal.ZERO;

        try {
            if (bankBalStr != null && !bankBalStr.trim().isEmpty()) {
                initialBankBalance = new BigDecimal(bankBalStr.trim());
                if (initialBankBalance.compareTo(BigDecimal.ZERO) < 0) {
                    req.setAttribute("errorMessage", "Initial bank balance cannot be negative.");
                    req.getRequestDispatcher("/register.jsp").forward(req, resp);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            req.setAttribute("errorMessage", "Invalid initial bank balance format.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        try {
            if (cashBalStr != null && !cashBalStr.trim().isEmpty()) {
                initialCashBalance = new BigDecimal(cashBalStr.trim());
                if (initialCashBalance.compareTo(BigDecimal.ZERO) < 0) {
                    req.setAttribute("errorMessage", "Initial cash balance cannot be negative.");
                    req.getRequestDispatcher("/register.jsp").forward(req, resp);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            req.setAttribute("errorMessage", "Invalid initial cash balance format.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        try {
            String hashedPassword = PasswordUtil.hashPassword(password);
            User newUser = new User(fullName.trim(), email.trim(), hashedPassword, initialBankBalance, initialCashBalance);
            userDao.registerUserWithDefaults(newUser, initialBankBalance, initialCashBalance);

            resp.sendRedirect(req.getContextPath() + "/login?registered=true");
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Registration failed: " + e.getMessage());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        }
    }
}
