package com.expensetracker.servlet;

import com.expensetracker.dao.TransactionDao;
import com.expensetracker.dao.UserDao;
import com.expensetracker.dto.CategorySpendSummary;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.User;
import com.expensetracker.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet powering the main Dashboard: summary cards, Chart.js spending analytics,
 * highest/lowest spend highlights, and recent transaction history.
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private UserDao userDao;
    private TransactionDao transactionDao;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
        transactionDao = new TransactionDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }

        // Always re-fetch fresh user entity to reflect latest database state
        User user = userDao.findById(sessionUser.getId());
        if (user == null) {
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }
        // Update session
        session.setAttribute("currentUser", user);

        // Fetch recent transactions
        List<Transaction> recentTransactions = transactionDao.findRecentByUserId(user.getId(), 25);

        // Fetch category expense breakdown
        List<CategorySpendSummary> categorySummaries = transactionDao.getExpenseBreakdownByCategory(user.getId());

        // Find highest and lowest spend categories
        CategorySpendSummary highestSpend = null;
        CategorySpendSummary lowestSpend = null;

        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartData = new ArrayList<>();

        if (!categorySummaries.isEmpty()) {
            highestSpend = categorySummaries.get(0); // already sorted DESC
            lowestSpend = categorySummaries.get(categorySummaries.size() - 1);

            for (CategorySpendSummary summary : categorySummaries) {
                chartLabels.add(summary.getCategoryName());
                chartData.add(summary.getTotalAmount());
            }
        }

        BigDecimal totalExpenses = transactionDao.getTotalExpenses(user.getId());
        BigDecimal totalIncomes = transactionDao.getTotalIncomes(user.getId());

        // Check for flash messages
        String success = req.getParameter("success");
        String error = req.getParameter("error");
        if (success != null && !success.trim().isEmpty()) {
            req.setAttribute("successMessage", success);
        }
        if (error != null && !error.trim().isEmpty()) {
            req.setAttribute("errorMessage", error);
        }

        req.setAttribute("user", user);
        req.setAttribute("recentTransactions", recentTransactions);
        req.setAttribute("categorySummaries", categorySummaries);
        req.setAttribute("highestSpend", highestSpend);
        req.setAttribute("lowestSpend", lowestSpend);
        req.setAttribute("totalExpenses", totalExpenses);
        req.setAttribute("totalIncomes", totalIncomes);
        req.setAttribute("chartLabelsJson", JsonUtil.toJsonStringList(chartLabels));
        req.setAttribute("chartDataJson", JsonUtil.toJsonNumberList(chartData));

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
