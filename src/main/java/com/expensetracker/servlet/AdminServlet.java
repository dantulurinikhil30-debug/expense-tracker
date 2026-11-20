package com.expensetracker.servlet;

import com.expensetracker.dao.TransactionDao;
import com.expensetracker.dao.UserDao;
import com.expensetracker.model.Role;
import com.expensetracker.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Panel servlet providing system-wide user management and overview statistics.
 * Only accessible to users with Role.ADMIN.
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

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
        // Role guard (double-check in addition to AuthFilter)
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=Unauthorized+access");
            return;
        }

        // Fetch all registered users
        List<User> allUsers = userDao.findAllUsers();

        // Compute per-user transaction counts
        Map<Long, Long> userTransactionCounts = new HashMap<>();
        for (User u : allUsers) {
            long count = transactionDao.getTransactionCountForUser(u.getId());
            userTransactionCounts.put(u.getId(), count);
        }

        // Compute system-wide totals
        int totalUsers = allUsers.size();
        long totalSystemTransactions = transactionDao.getTotalTransactionCount();

        BigDecimal totalSystemBankBalance = BigDecimal.ZERO;
        BigDecimal totalSystemCashBalance = BigDecimal.ZERO;
        for (User u : allUsers) {
            totalSystemBankBalance = totalSystemBankBalance.add(
                    u.getBankBalance() != null ? u.getBankBalance() : BigDecimal.ZERO);
            totalSystemCashBalance = totalSystemCashBalance.add(
                    u.getCashBalance() != null ? u.getCashBalance() : BigDecimal.ZERO);
        }
        BigDecimal totalSystemBalance = totalSystemBankBalance.add(totalSystemCashBalance);

        // Set request attributes
        req.setAttribute("allUsers", allUsers);
        req.setAttribute("userTransactionCounts", userTransactionCounts);
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("totalSystemTransactions", totalSystemTransactions);
        req.setAttribute("totalSystemBankBalance", totalSystemBankBalance);
        req.setAttribute("totalSystemCashBalance", totalSystemCashBalance);
        req.setAttribute("totalSystemBalance", totalSystemBalance);

        req.getRequestDispatcher("/admin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Role guard
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/dashboard?error=Unauthorized+access");
            return;
        }

        String action = req.getParameter("action");

        if ("deleteUser".equals(action)) {
            String userIdParam = req.getParameter("userId");

            if (userIdParam == null || userIdParam.trim().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin?error=Invalid+user+ID");
                return;
            }

            try {
                Long targetUserId = Long.parseLong(userIdParam.trim());

                // Prevent admin from deleting their own account
                if (targetUserId.equals(currentUser.getId())) {
                    resp.sendRedirect(req.getContextPath() + "/admin?error=Cannot+delete+your+own+admin+account");
                    return;
                }

                User targetUser = userDao.findById(targetUserId);
                if (targetUser == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin?error=User+not+found");
                    return;
                }

                String deletedName = targetUser.getFullName();
                userDao.deleteUserAndAllData(targetUserId);

                resp.sendRedirect(req.getContextPath() + "/admin?success=User+'" + 
                        java.net.URLEncoder.encode(deletedName, "UTF-8") + "'+deleted+successfully");

            } catch (NumberFormatException e) {
                resp.sendRedirect(req.getContextPath() + "/admin?error=Invalid+user+ID+format");
            } catch (Exception e) {
                resp.sendRedirect(req.getContextPath() + "/admin?error=Failed+to+delete+user:+" +
                        java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin?error=Unknown+action");
        }
    }
}
