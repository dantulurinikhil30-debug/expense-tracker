package com.expensetracker.servlet;

import com.expensetracker.dao.CategoryDao;
import com.expensetracker.dao.TransactionDao;
import com.expensetracker.dao.UserDao;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Servlet powering the dedicated Transaction History & Filter page (/transactions).
 * Supports filtering by start date, end date, category, and transaction type.
 */
@WebServlet("/transactions")
public class TransactionsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TransactionDao transactionDao;
    private CategoryDao categoryDao;
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        transactionDao = new TransactionDao();
        categoryDao = new CategoryDao();
        userDao = new UserDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }

        // Re-fetch fresh user entity
        User user = userDao.findById(sessionUser.getId());
        if (user == null) {
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }
        session.setAttribute("currentUser", user);

        // Read query parameters
        String startDateStr = req.getParameter("startDate");
        String endDateStr = req.getParameter("endDate");
        String categoryIdStr = req.getParameter("categoryId");
        String typeStr = req.getParameter("type");

        // Parse startDate
        LocalDate startDate = null;
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                startDate = LocalDate.parse(startDateStr.trim());
            } catch (DateTimeParseException ignored) {
                startDateStr = "";
            }
        }

        // Parse endDate
        LocalDate endDate = null;
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                endDate = LocalDate.parse(endDateStr.trim());
            } catch (DateTimeParseException ignored) {
                endDateStr = "";
            }
        }

        // Parse categoryId
        Long categoryId = null;
        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty() && !"all".equalsIgnoreCase(categoryIdStr.trim())) {
            try {
                categoryId = Long.parseLong(categoryIdStr.trim());
            } catch (NumberFormatException ignored) {
                categoryIdStr = "all";
            }
        } else {
            categoryIdStr = "all";
        }

        // Parse type
        TransactionType transactionType = null;
        if (typeStr != null && !typeStr.trim().isEmpty() && !"all".equalsIgnoreCase(typeStr.trim())) {
            try {
                transactionType = TransactionType.valueOf(typeStr.trim().toUpperCase());
                typeStr = transactionType.name();
            } catch (IllegalArgumentException ignored) {
                typeStr = "ALL";
            }
        } else {
            typeStr = "ALL";
        }

        // Fetch user categories for dropdown
        List<Category> categories = categoryDao.findByUserId(user.getId());

        // Query matching transactions
        List<Transaction> transactions = transactionDao.findFilteredTransactions(
                user.getId(), startDate, endDate, categoryId, transactionType);

        // Compute summary metrics for filtered results
        BigDecimal filteredTotalIncome = BigDecimal.ZERO;
        BigDecimal filteredTotalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                filteredTotalIncome = filteredTotalIncome.add(t.getAmount());
            } else if (t.getType() == TransactionType.EXPENSE) {
                filteredTotalExpense = filteredTotalExpense.add(t.getAmount());
            }
        }
        BigDecimal filteredNetAmount = filteredTotalIncome.subtract(filteredTotalExpense);

        // Flash messages
        String success = req.getParameter("success");
        String error = req.getParameter("error");
        if (success != null && !success.trim().isEmpty()) {
            req.setAttribute("successMessage", success);
        }
        if (error != null && !error.trim().isEmpty()) {
            req.setAttribute("errorMessage", error);
        }

        // Set request attributes
        req.setAttribute("user", user);
        req.setAttribute("categories", categories);
        req.setAttribute("transactions", transactions);
        req.setAttribute("matchCount", transactions.size());
        req.setAttribute("filteredTotalIncome", filteredTotalIncome);
        req.setAttribute("filteredTotalExpense", filteredTotalExpense);
        req.setAttribute("filteredNetAmount", filteredNetAmount);

        // Preserve filter parameters for UI state
        req.setAttribute("selectedStartDate", startDateStr != null ? startDateStr : "");
        req.setAttribute("selectedEndDate", endDateStr != null ? endDateStr : "");
        req.setAttribute("selectedCategoryId", categoryIdStr);
        req.setAttribute("selectedType", typeStr);

        req.getRequestDispatcher("/transactions.jsp").forward(req, resp);
    }
}
