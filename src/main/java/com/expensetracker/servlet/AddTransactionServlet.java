package com.expensetracker.servlet;

import com.expensetracker.dao.CategoryDao;
import com.expensetracker.dao.PaymentModeDao;
import com.expensetracker.dao.TransactionDao;
import com.expensetracker.dao.UserDao;
import com.expensetracker.model.AccountType;
import com.expensetracker.model.Category;
import com.expensetracker.model.PaymentMode;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * Servlet handling transaction creation with balance adjustments and
 * negative balance prevention.
 */
@WebServlet("/add-transaction")
public class AddTransactionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TransactionDao transactionDao;
    private CategoryDao categoryDao;
    private PaymentModeDao paymentModeDao;
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        transactionDao = new TransactionDao();
        categoryDao = new CategoryDao();
        paymentModeDao = new PaymentModeDao();
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

        // Fetch fresh user for up-to-date balances
        User freshUser = userDao.findById(sessionUser.getId());
        if (freshUser != null) {
            session.setAttribute("currentUser", freshUser);
            req.setAttribute("user", freshUser);
        }

        List<Category> categories = categoryDao.findByUserId(sessionUser.getId());
        List<PaymentMode> paymentModes = paymentModeDao.findByUserId(sessionUser.getId());

        req.setAttribute("categories", categories);
        req.setAttribute("paymentModes", paymentModes);
        req.setAttribute("defaultDate", LocalDate.now().toString());

        req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }

        User freshUser = userDao.findById(sessionUser.getId());
        if (freshUser == null) {
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }
        session.setAttribute("currentUser", freshUser);
        req.setAttribute("user", freshUser);

        String typeStr = req.getParameter("type");
        String amountStr = req.getParameter("amount");
        String dateStr = req.getParameter("transactionDate");
        String categoryIdStr = req.getParameter("categoryId");
        String paymentModeIdStr = req.getParameter("paymentModeId");
        String description = req.getParameter("description");

        // Preserve input values for convenience on error
        req.setAttribute("selectedType", typeStr);
        req.setAttribute("enteredAmount", amountStr);
        req.setAttribute("selectedDate", dateStr);
        req.setAttribute("selectedCategoryId", categoryIdStr);
        req.setAttribute("selectedPaymentModeId", paymentModeIdStr);
        req.setAttribute("enteredDescription", description);

        List<Category> categories = categoryDao.findByUserId(sessionUser.getId());
        List<PaymentMode> paymentModes = paymentModeDao.findByUserId(sessionUser.getId());
        req.setAttribute("categories", categories);
        req.setAttribute("paymentModes", paymentModes);

        // 1. Transaction Type Validation
        TransactionType type;
        try {
            type = TransactionType.valueOf(typeStr);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Invalid transaction type selected.");
            req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
            return;
        }

        // 2. Amount Validation
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                req.setAttribute("errorMessage", "Transaction amount must be greater than zero.");
                req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
                return;
            }
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Please enter a valid monetary amount.");
            req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
            return;
        }

        // 3. Date Validation
        LocalDate transactionDate;
        try {
            transactionDate = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            req.setAttribute("errorMessage", "Please select a valid date in YYYY-MM-DD format.");
            req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
            return;
        }

        // 4. Category & PaymentMode Selection Validation
        Long categoryId;
        Long paymentModeId;
        try {
            categoryId = Long.parseLong(categoryIdStr);
            paymentModeId = Long.parseLong(paymentModeIdStr);
        } catch (NumberFormatException e) {
            req.setAttribute("errorMessage", "Invalid Category or Payment Mode selection.");
            req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
            return;
        }

        Category category = categoryDao.findById(categoryId);
        PaymentMode paymentMode = paymentModeDao.findById(paymentModeId);

        if (category == null || !category.getUser().getId().equals(sessionUser.getId())) {
            req.setAttribute("errorMessage", "Selected Category does not belong to your account.");
            req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
            return;
        }

        if (paymentMode == null || !paymentMode.getUser().getId().equals(sessionUser.getId())) {
            req.setAttribute("errorMessage", "Selected Payment Mode does not belong to your account.");
            req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
            return;
        }

        // 5. Pre-check Available Balance for EXPENSE transactions to prevent negative balances
        if (type == TransactionType.EXPENSE) {
            AccountType accountType = paymentMode.getAccountType();
            BigDecimal available = (accountType == AccountType.BANK) ? freshUser.getBankBalance() : freshUser.getCashBalance();
            String poolName = (accountType == AccountType.BANK) ? "Bank Account" : "Cash";

            if (amount.compareTo(available) > 0) {
                String formattedAvailable = String.format(Locale.forLanguageTag("en-IN"), "%,.2f", available);
                String formattedAttempted = String.format(Locale.forLanguageTag("en-IN"), "%,.2f", amount);
                req.setAttribute("errorMessage", "Insufficient " + poolName + " balance! Available: ₹" +
                        formattedAvailable + ", Attempted Expense: ₹" + formattedAttempted);
                req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
                return;
            }
        }

        // 6. Construct and persist transaction
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTransactionDate(transactionDate);
        transaction.setDescription(description != null ? description.trim() : null);
        transaction.setCategory(category);
        transaction.setPaymentMode(paymentMode);
        transaction.setUser(freshUser);

        try {
            transactionDao.addTransactionAndUpdateBalance(transaction, freshUser.getId());

            // Refresh user in session
            User updatedUser = userDao.findById(freshUser.getId());
            session.setAttribute("currentUser", updatedUser);

            String successMsg = URLEncoder.encode("Transaction recorded successfully!", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/dashboard?success=" + successMsg);
        } catch (IllegalArgumentException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Failed to record transaction: " + e.getMessage());
            req.getRequestDispatcher("/add-transaction.jsp").forward(req, resp);
        }
    }
}
