package com.expensetracker.util;

import com.expensetracker.dao.UserDao;
import com.expensetracker.model.Role;
import com.expensetracker.model.User;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.math.BigDecimal;

/**
 * Application startup listener that seeds the default admin account
 * if one does not already exist. Runs on application deployment/start.
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@expensetracker.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_NAME = "System Admin";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[ExpenseTracker] Application starting - checking for default admin account...");

        try {
            // Force HibernateUtil static initializer to run (creates tables via hbm2ddl.auto=update)
            HibernateUtil.getSessionFactory();

            UserDao userDao = new UserDao();
            User existingAdmin = userDao.findByEmail(DEFAULT_ADMIN_EMAIL);

            if (existingAdmin == null) {
                User adminUser = new User();
                adminUser.setFullName(DEFAULT_ADMIN_NAME);
                adminUser.setEmail(DEFAULT_ADMIN_EMAIL);
                adminUser.setPassword(PasswordUtil.hashPassword(DEFAULT_ADMIN_PASSWORD));
                adminUser.setRole(Role.ADMIN);
                adminUser.setBankBalance(BigDecimal.ZERO);
                adminUser.setCashBalance(BigDecimal.ZERO);

                // Register with default categories and payment modes (admin may need them too)
                userDao.registerUserWithDefaults(adminUser, BigDecimal.ZERO, BigDecimal.ZERO);

                System.out.println("[ExpenseTracker] Default admin account created: " + DEFAULT_ADMIN_EMAIL);
            } else {
                System.out.println("[ExpenseTracker] Default admin account already exists.");
            }
        } catch (Exception e) {
            System.err.println("[ExpenseTracker] WARNING: Could not seed default admin account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[ExpenseTracker] Application shutting down...");
        HibernateUtil.shutdown();
    }
}
