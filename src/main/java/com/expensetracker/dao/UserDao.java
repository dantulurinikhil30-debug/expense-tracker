package com.expensetracker.dao;

import com.expensetracker.model.AccountType;
import com.expensetracker.model.Category;
import com.expensetracker.model.CategoryType;
import com.expensetracker.model.PaymentMode;
import com.expensetracker.model.User;
import com.expensetracker.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Data Access Object for User entity operations, including atomic registration
 * and automated seeding of default Categories and Payment Modes.
 */
public class UserDao {

    /**
     * Finds a User by their primary key.
     */
    public User findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds a User by their email address.
     */
    public User findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE LOWER(email) = LOWER(:email)", User.class);
            query.setParameter("email", email.trim());
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if an email address is already registered.
     */
    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    /**
     * Atomically registers a new user with initial Bank and Cash balances,
     * and seeds default Categories and Payment Modes.
     */
    public User registerUserWithDefaults(User user, BigDecimal initialBankBalance, BigDecimal initialCashBalance) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Set balances
            user.setBankBalance(initialBankBalance != null ? initialBankBalance : BigDecimal.ZERO);
            user.setCashBalance(initialCashBalance != null ? initialCashBalance : BigDecimal.ZERO);

            // Persist User
            session.persist(user);

            // 1. Seed Default Expense Categories
            List<String> defaultExpenseCats = Arrays.asList(
                    "Food & Dining",
                    "Rent & Housing",
                    "Groceries",
                    "Shopping",
                    "Transportation",
                    "Utilities & Bills",
                    "Entertainment",
                    "Healthcare"
            );
            for (String catName : defaultExpenseCats) {
                Category cat = new Category(catName, CategoryType.EXPENSE, user);
                session.persist(cat);
            }

            // 2. Seed Default Income Categories
            List<String> defaultIncomeCats = Arrays.asList(
                    "Salary",
                    "Freelancing",
                    "Investments & Dividends",
                    "Gift / Bonus",
                    "Other Income"
            );
            for (String catName : defaultIncomeCats) {
                Category cat = new Category(catName, CategoryType.INCOME, user);
                session.persist(cat);
            }

            // 3. Seed Default Payment Modes
            session.persist(new PaymentMode("Cash", AccountType.CASH, user));
            session.persist(new PaymentMode("Debit Card", AccountType.BANK, user));
            session.persist(new PaymentMode("UPI / Digital Wallet", AccountType.BANK, user));
            session.persist(new PaymentMode("Net Banking", AccountType.BANK, user));
            session.persist(new PaymentMode("Credit Card", AccountType.BANK, user));

            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to register user and seed defaults: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing User's attributes.
     */
    public void updateUser(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all registered users ordered by ID ascending.
     */
    public List<User> findAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User ORDER BY id ASC", User.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Atomically deletes a user and ALL associated data (transactions, payment modes,
     * categories) in the correct order to avoid foreign key constraint violations.
     * Uses explicit ordered HQL bulk deletes within a single transaction.
     */
    public void deleteUserAndAllData(Long userId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // 1. Delete all Transactions belonging to this user
            session.createMutationQuery("DELETE FROM Transaction t WHERE t.user.id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();

            // 2. Delete all PaymentModes belonging to this user
            session.createMutationQuery("DELETE FROM PaymentMode pm WHERE pm.user.id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();

            // 3. Delete all Categories belonging to this user
            session.createMutationQuery("DELETE FROM Category c WHERE c.user.id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();

            // 4. Delete the User record itself
            User user = session.get(User.class, userId);
            if (user != null) {
                session.remove(user);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete user and all data: " + e.getMessage(), e);
        }
    }
}
