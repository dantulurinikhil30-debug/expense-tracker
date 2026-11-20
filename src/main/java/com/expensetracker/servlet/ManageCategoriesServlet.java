package com.expensetracker.servlet;

import com.expensetracker.dao.CategoryDao;
import com.expensetracker.dao.PaymentModeDao;
import com.expensetracker.model.AccountType;
import com.expensetracker.model.Category;
import com.expensetracker.model.CategoryType;
import com.expensetracker.model.PaymentMode;
import com.expensetracker.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Servlet managing custom Categories and Payment Modes (side-by-side management).
 */
@WebServlet("/manage-categories")
public class ManageCategoriesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private CategoryDao categoryDao;
    private PaymentModeDao paymentModeDao;

    @Override
    public void init() throws ServletException {
        categoryDao = new CategoryDao();
        paymentModeDao = new PaymentModeDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }

        List<Category> categories = categoryDao.findByUserId(sessionUser.getId());
        List<PaymentMode> paymentModes = paymentModeDao.findByUserId(sessionUser.getId());

        String success = req.getParameter("success");
        String error = req.getParameter("error");
        if (success != null && !success.trim().isEmpty()) {
            req.setAttribute("successMessage", success);
        }
        if (error != null && !error.trim().isEmpty()) {
            req.setAttribute("errorMessage", error);
        }

        req.setAttribute("categories", categories);
        req.setAttribute("paymentModes", paymentModes);

        req.getRequestDispatcher("/manage-categories.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            resp.sendRedirect(req.getContextPath() + "/manage-categories");
            return;
        }

        try {
            switch (action) {
                case "addCategory": {
                    String categoryName = req.getParameter("categoryName");
                    String categoryTypeStr = req.getParameter("categoryType");

                    if (categoryName == null || categoryName.trim().isEmpty()) {
                        redirectWithError(req, resp, "Category name cannot be blank.");
                        return;
                    }

                    CategoryType type = CategoryType.valueOf(categoryTypeStr);
                    Category category = new Category(categoryName.trim(), type, sessionUser);
                    categoryDao.save(category);

                    redirectWithSuccess(req, resp, "Category '" + categoryName.trim() + "' added successfully!");
                    break;
                }

                case "addPaymentMode": {
                    String modeName = req.getParameter("modeName");
                    String accountTypeStr = req.getParameter("accountType");

                    if (modeName == null || modeName.trim().isEmpty()) {
                        redirectWithError(req, resp, "Payment mode name cannot be blank.");
                        return;
                    }

                    AccountType accountType = AccountType.valueOf(accountTypeStr);
                    PaymentMode mode = new PaymentMode(modeName.trim(), accountType, sessionUser);
                    paymentModeDao.save(mode);

                    redirectWithSuccess(req, resp, "Payment Mode '" + modeName.trim() + "' added successfully!");
                    break;
                }

                case "deleteCategory": {
                    String categoryIdStr = req.getParameter("categoryId");
                    Long categoryId = Long.parseLong(categoryIdStr);

                    if (categoryDao.isCategoryUsed(categoryId)) {
                        redirectWithError(req, resp, "Cannot delete category: it is currently linked to existing transactions.");
                        return;
                    }

                    boolean deleted = categoryDao.delete(categoryId, sessionUser.getId());
                    if (deleted) {
                        redirectWithSuccess(req, resp, "Category deleted successfully.");
                    } else {
                        redirectWithError(req, resp, "Category not found or could not be deleted.");
                    }
                    break;
                }

                case "deletePaymentMode": {
                    String paymentModeIdStr = req.getParameter("paymentModeId");
                    Long paymentModeId = Long.parseLong(paymentModeIdStr);

                    if (paymentModeDao.isPaymentModeUsed(paymentModeId)) {
                        redirectWithError(req, resp, "Cannot delete payment mode: it is currently linked to existing transactions.");
                        return;
                    }

                    boolean deleted = paymentModeDao.delete(paymentModeId, sessionUser.getId());
                    if (deleted) {
                        redirectWithSuccess(req, resp, "Payment mode deleted successfully.");
                    } else {
                        redirectWithError(req, resp, "Payment mode not found or could not be deleted.");
                    }
                    break;
                }

                default:
                    resp.sendRedirect(req.getContextPath() + "/manage-categories");
            }
        } catch (Exception e) {
            redirectWithError(req, resp, "Operation failed: " + e.getMessage());
        }
    }

    private void redirectWithSuccess(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/manage-categories?success=" + encoded);
    }

    private void redirectWithError(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/manage-categories?error=" + encoded);
    }
}
