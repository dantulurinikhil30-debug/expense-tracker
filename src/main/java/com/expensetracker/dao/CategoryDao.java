package com.expensetracker.dao;

import com.expensetracker.model.Category;
import com.expensetracker.model.CategoryType;
import com.expensetracker.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for Category entity operations.
 */
public class CategoryDao {

    public Category findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Category.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Category> findByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery(
                    "FROM Category c WHERE c.user.id = :userId ORDER BY c.type, c.name ASC", Category.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Category> findByUserIdAndType(Long userId, CategoryType type) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery(
                    "FROM Category c WHERE c.user.id = :userId AND c.type = :type ORDER BY c.name ASC", Category.class);
            query.setParameter("userId", userId);
            query.setParameter("type", type);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void save(Category category) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(category);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to save category: " + e.getMessage(), e);
        }
    }

    public boolean isCategoryUsed(Long categoryId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(t) FROM Transaction t WHERE t.category.id = :categoryId", Long.class);
            query.setParameter("categoryId", categoryId);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // fail safe: assume used to prevent accidental foreign key error
        }
    }

    public boolean delete(Long categoryId, Long userId) {
        if (isCategoryUsed(categoryId)) {
            return false;
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Category cat = session.get(Category.class, categoryId);
            if (cat != null && cat.getUser().getId().equals(userId)) {
                session.remove(cat);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to delete category: " + e.getMessage(), e);
        }
    }
}
