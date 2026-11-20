package com.expensetracker.servlet;

import com.expensetracker.dao.TransactionDao;
import com.expensetracker.dao.UserDao;
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

/**
 * Servlet handling transaction deletion and balance reversals with negative balance checks.
 */
@WebServlet("/delete-transaction")
public class DeleteTransactionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TransactionDao transactionDao;
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        transactionDao = new TransactionDao();
        userDao = new UserDao();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }

        String transactionIdStr = req.getParameter("transactionId");
        String redirectTarget = req.getParameter("redirectUrl");
        String destination = (redirectTarget != null && redirectTarget.contains("transactions")) ? "/transactions" : "/dashboard";

        if (transactionIdStr == null || transactionIdStr.trim().isEmpty()) {
            String errorMsg = URLEncoder.encode("Missing transaction identifier.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + destination + "?error=" + errorMsg);
            return;
        }

        try {
            Long transactionId = Long.parseLong(transactionIdStr.trim());
            boolean deleted = transactionDao.deleteTransactionAndRevertBalance(transactionId, sessionUser.getId());

            if (deleted) {
                // Refresh user in session with updated balances
                User freshUser = userDao.findById(sessionUser.getId());
                session.setAttribute("currentUser", freshUser);
                String successMsg = URLEncoder.encode("Transaction deleted and balance restored!", StandardCharsets.UTF_8);
                resp.sendRedirect(req.getContextPath() + destination + "?success=" + successMsg);
            } else {
                String errorMsg = URLEncoder.encode("Transaction not found or permission denied.", StandardCharsets.UTF_8);
                resp.sendRedirect(req.getContextPath() + destination + "?error=" + errorMsg);
            }
        } catch (IllegalArgumentException e) {
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + destination + "?error=" + errorMsg);
        } catch (Exception e) {
            String errorMsg = URLEncoder.encode("Failed to delete transaction: " + e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + destination + "?error=" + errorMsg);
        }
    }
}
