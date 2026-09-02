package com.lens.sbeans;

import com.lens.ebeans.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class UsersFacade extends AbstractFacade<Users> implements UsersFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsersFacade() {
        super(Users.class);
    }

    @Override
    public void remove(Users entity) {
        if (entity != null) {
            entity.setStatus("INACTIVE");
            entity.setUpdatedAt(new Date());
            getEntityManager().merge(entity);
        }
    }

    @Override
    public List<Users> findAll() {
        return em.createQuery("SELECT u FROM Users u WHERE u.status = 'ACTIVE' ORDER BY u.id DESC", Users.class)
                .getResultList();
    }

    @Override
    public List<Users> search(String keyword, String role) {
        return search(keyword, role, "DESC");
    }

    @Override
    public List<Users> search(String keyword, String role, String sortOrder) {

        String jpql = "SELECT u FROM Users u "
                + "WHERE u.status = 'ACTIVE' "
                + "AND (u.username LIKE :keyword "
                + "OR u.fullName LIKE :keyword "
                + "OR u.email LIKE :keyword "
                + "OR u.phone LIKE :keyword)";

        if (role != null && !role.trim().isEmpty()) {
            jpql += " AND u.role = :role";
        }

        String order = "ASC".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
        jpql += " ORDER BY u.createdAt " + order + ", u.id " + order;

        var query = em.createQuery(jpql, Users.class)
                .setParameter("keyword", "%" + (keyword == null ? "" : keyword.trim()) + "%");

        if (role != null && !role.trim().isEmpty()) {
            query.setParameter("role", role);
        }

        return query.getResultList();

    }

    @Override
    public boolean isUsernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String jpql = "SELECT COUNT(u) FROM Users u WHERE u.username = :username";
        Long count = em.createQuery(jpql, Long.class)
                .setParameter("username", username.trim())
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean isEmailExists(String email, Integer id) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        StringBuilder jpql = new StringBuilder("SELECT COUNT(u) FROM Users u WHERE u.email = :email ");
        if (id != null) {
            jpql.append("AND u.id != :id");
        }
        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("email", email.trim());
        if (id != null) {
            query.setParameter("id", id);
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean isPhoneExists(String phone, Integer id) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        StringBuilder jpql = new StringBuilder("SELECT COUNT(u) FROM Users u WHERE u.phone = :phone ");
        if (id != null) {
            jpql.append("AND u.id != :id");
        }
        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("phone", phone.trim());
        if (id != null) {
            query.setParameter("id", id);
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

}
