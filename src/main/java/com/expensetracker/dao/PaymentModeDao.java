package com.expensetracker.dao;

import com.expensetracker.model.PaymentMode;
import com.expensetracker.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for PaymentMode entity operations.
 */
public class PaymentModeDao {

    public PaymentMode findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(PaymentMode.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<PaymentMode> findByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<PaymentMode> query = session.createQuery(
                    "FROM PaymentMode pm WHERE pm.user.id = :userId ORDER BY pm.accountType, pm.modeName ASC", PaymentMode.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void save(PaymentMode paymentMode) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(paymentMode);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to save payment mode: " + e.getMessage(), e);
        }
    }

    public boolean isPaymentModeUsed(Long paymentModeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(t) FROM Transaction t WHERE t.paymentMode.id = :paymentModeId", Long.class);
            query.setParameter("paymentModeId", paymentModeId);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // fail safe
        }
    }

    public boolean delete(Long paymentModeId, Long userId) {
        if (isPaymentModeUsed(paymentModeId)) {
            return false;
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            PaymentMode pm = session.get(PaymentMode.class, paymentModeId);
            if (pm != null && pm.getUser().getId().equals(userId)) {
                session.remove(pm);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete payment mode: " + e.getMessage(), e);
        }
    }
}
