package com.expensetracker.dao;

import com.expensetracker.dto.CategorySpendSummary;
import com.expensetracker.model.AccountType;
import com.expensetracker.model.Category;
import com.expensetracker.model.PaymentMode;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;
import com.expensetracker.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Data Access Object for financial Transactions, featuring atomic balance
 * adjustments, negative balance protection, and aggregated spending analytics.
 */
public class TransactionDao {

    public Transaction findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Transaction.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves recent transactions for a user sorted by date descending, then id descending.
     */
    public List<Transaction> findRecentByUserId(Long userId, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Transaction> query = session.createQuery(
                    "FROM Transaction t WHERE t.user.id = :userId ORDER BY t.transactionDate DESC, t.id DESC", Transaction.class);
            query.setParameter("userId", userId);
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Atomically adds a new transaction and adjusts the user's Bank or Cash balance.
     * Prevents negative balances on EXPENSE transactions.
     */
    public boolean addTransactionAndUpdateBalance(Transaction transaction, Long userId) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Load managed user entity
            User user = session.get(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + userId);
            }

            // Ensure category and paymentMode are managed
            Category category = session.get(Category.class, transaction.getCategory().getId());
            PaymentMode paymentMode = session.get(PaymentMode.class, transaction.getPaymentMode().getId());

            if (category == null || !category.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Invalid category selected.");
            }
            if (paymentMode == null || !paymentMode.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Invalid payment mode selected.");
            }

            transaction.setUser(user);
            transaction.setCategory(category);
            transaction.setPaymentMode(paymentMode);

            BigDecimal amount = transaction.getAmount();
            TransactionType type = transaction.getType();
            AccountType accountType = paymentMode.getAccountType();

            // Balance Calculation & Negative Balance Prevention Logic
            if (type == TransactionType.INCOME) {
                if (accountType == AccountType.BANK) {
                    user.setBankBalance(user.getBankBalance().add(amount));
                } else if (accountType == AccountType.CASH) {
                    user.setCashBalance(user.getCashBalance().add(amount));
                }
            } else if (type == TransactionType.EXPENSE) {
                BigDecimal availableBalance = (accountType == AccountType.BANK) ? user.getBankBalance() : user.getCashBalance();
                String poolName = (accountType == AccountType.BANK) ? "Bank Account" : "Cash";

                // Guard against negative balance
                if (amount.compareTo(availableBalance) > 0) {
                    String formattedAvailable = String.format(Locale.forLanguageTag("en-IN"), "%,.2f", availableBalance);
                    String formattedAttempted = String.format(Locale.forLanguageTag("en-IN"), "%,.2f", amount);
                    throw new IllegalArgumentException("Insufficient " + poolName + " balance! Available: ₹" +
                            formattedAvailable + ", Attempted Expense: ₹" + formattedAttempted);
                }

                if (accountType == AccountType.BANK) {
                    user.setBankBalance(user.getBankBalance().subtract(amount));
                } else if (accountType == AccountType.CASH) {
                    user.setCashBalance(user.getCashBalance().subtract(amount));
                }
            }

            session.persist(transaction);
            session.merge(user);

            tx.commit();
            return true;
        } catch (IllegalArgumentException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error adding transaction: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Atomically deletes a transaction and reverses the corresponding Bank or Cash balance.
     * Prevents negative balances when deleting an INCOME transaction.
     */
    public boolean deleteTransactionAndRevertBalance(Long transactionId, Long userId) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Transaction transaction = session.get(Transaction.class, transactionId);
            if (transaction == null || !transaction.getUser().getId().equals(userId)) {
                return false;
            }

            User user = session.get(User.class, userId);
            if (user == null) {
                return false;
            }

            BigDecimal amount = transaction.getAmount();
            TransactionType type = transaction.getType();
            AccountType accountType = transaction.getPaymentMode().getAccountType();

            // Reversal Logic:
            // - If INCOME was added to Bank/Cash, deleting it subtracts it from that pool.
            // - We must ensure subtracting doesn't push the balance below 0.
            // - If EXPENSE was deducted from Bank/Cash, deleting it refunds it (always safe).
            if (type == TransactionType.INCOME) {
                BigDecimal availableBalance = (accountType == AccountType.BANK) ? user.getBankBalance() : user.getCashBalance();
                String poolName = (accountType == AccountType.BANK) ? "Bank Account" : "Cash";

                if (amount.compareTo(availableBalance) > 0) {
                    String formattedAmount = String.format(Locale.forLanguageTag("en-IN"), "%,.2f", amount);
                    String formattedAvailable = String.format(Locale.forLanguageTag("en-IN"), "%,.2f", availableBalance);
                    throw new IllegalArgumentException("Cannot delete income transaction: reversing this income of ₹" +
                            formattedAmount + " would result in a negative " + poolName + " balance (Current available: ₹" +
                            formattedAvailable + ").");
                }

                if (accountType == AccountType.BANK) {
                    user.setBankBalance(user.getBankBalance().subtract(amount));
                } else if (accountType == AccountType.CASH) {
                    user.setCashBalance(user.getCashBalance().subtract(amount));
                }
            } else if (type == TransactionType.EXPENSE) {
                if (accountType == AccountType.BANK) {
                    user.setBankBalance(user.getBankBalance().add(amount));
                } else if (accountType == AccountType.CASH) {
                    user.setCashBalance(user.getCashBalance().add(amount));
                }
            }

            session.remove(transaction);
            session.merge(user);

            tx.commit();
            return true;
        } catch (IllegalArgumentException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Computes the aggregated expense breakdown by Category for spending analytics.
     * Returns a list of CategorySpendSummary ordered descending by total amount.
     */
    public List<CategorySpendSummary> getExpenseBreakdownByCategory(Long userId) {
        List<CategorySpendSummary> summaries = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(
                    "SELECT t.category.name, SUM(t.amount) " +
                    "FROM Transaction t " +
                    "WHERE t.user.id = :userId AND t.type = :expType " +
                    "GROUP BY t.category.name " +
                    "ORDER BY SUM(t.amount) DESC", Object[].class);
            query.setParameter("userId", userId);
            query.setParameter("expType", TransactionType.EXPENSE);

            List<Object[]> results = query.list();

            // Calculate total expenses for percentage calculation
            BigDecimal totalExpense = BigDecimal.ZERO;
            for (Object[] row : results) {
                BigDecimal sum = (BigDecimal) row[1];
                if (sum != null) {
                    totalExpense = totalExpense.add(sum);
                }
            }

            for (Object[] row : results) {
                String catName = (String) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                double percentage = 0.0;
                if (totalExpense.compareTo(BigDecimal.ZERO) > 0 && amount != null) {
                    percentage = amount.divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                }
                summaries.add(new CategorySpendSummary(catName, amount, percentage));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summaries;
    }

    /**
     * Calculates the sum of all expenses for the user.
     */
    public BigDecimal getTotalExpenses(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                    "WHERE t.user.id = :userId AND t.type = :expType", BigDecimal.class);
            query.setParameter("userId", userId);
            query.setParameter("expType", TransactionType.EXPENSE);
            BigDecimal total = query.uniqueResult();
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calculates the sum of all incomes for the user.
     */
    public BigDecimal getTotalIncomes(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
                    "WHERE t.user.id = :userId AND t.type = :incType", BigDecimal.class);
            query.setParameter("userId", userId);
            query.setParameter("incType", TransactionType.INCOME);
            BigDecimal total = query.uniqueResult();
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Dynamically filters transactions by date range, category, and transaction type.
     * Results are ordered by transactionDate DESC, id DESC.
     */
    public List<Transaction> findFilteredTransactions(Long userId, java.time.LocalDate startDate,
                                                     java.time.LocalDate endDate, Long categoryId,
                                                     TransactionType type) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Transaction t WHERE t.user.id = :userId");

            if (startDate != null) {
                hql.append(" AND t.transactionDate >= :startDate");
            }
            if (endDate != null) {
                hql.append(" AND t.transactionDate <= :endDate");
            }
            if (categoryId != null && categoryId > 0) {
                hql.append(" AND t.category.id = :categoryId");
            }
            if (type != null) {
                hql.append(" AND t.type = :type");
            }

            hql.append(" ORDER BY t.transactionDate DESC, t.id DESC");

            Query<Transaction> query = session.createQuery(hql.toString(), Transaction.class);
            query.setParameter("userId", userId);

            if (startDate != null) {
                query.setParameter("startDate", startDate);
            }
            if (endDate != null) {
                query.setParameter("endDate", endDate);
            }
            if (categoryId != null && categoryId > 0) {
                query.setParameter("categoryId", categoryId);
            }
            if (type != null) {
                query.setParameter("type", type);
            }

            return query.list();
        } catch (Exception e) {
            System.err.println("Error filtering transactions: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Returns the total count of transactions across all users in the system.
     */
    public long getTotalTransactionCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(t) FROM Transaction t", Long.class);
            Long count = query.uniqueResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * Returns the count of transactions for a specific user.
     */
    public long getTransactionCountForUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId", Long.class);
            query.setParameter("userId", userId);
            Long count = query.uniqueResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
