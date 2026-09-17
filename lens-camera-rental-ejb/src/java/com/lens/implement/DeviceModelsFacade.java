package com.lens.implement;

import com.lens.facade.AbstractFacade;
import com.lens.facade.DeviceModelsFacadeLocal;
import com.lens.entity.DeviceModels;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class DeviceModelsFacade extends AbstractFacade<DeviceModels> implements DeviceModelsFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DeviceModelsFacade() {
        super(DeviceModels.class);
    }

    @Override
    public boolean isBrandModelExists(String brand, String model, Integer id) {
        if (brand == null || brand.trim().isEmpty() || model == null || model.trim().isEmpty()) {
            return false;
        }
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(dm) FROM DeviceModels dm WHERE LOWER(TRIM(dm.brand)) = LOWER(TRIM(:brand)) AND LOWER(TRIM(dm.model)) = LOWER(TRIM(:model)) ");
        if (id != null) {
            jpql.append("AND dm.id != :id");
        }
        var query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("brand", brand.trim())
                .setParameter("model", model.trim());
        if (id != null) {
            query.setParameter("id", id);
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean isDeviceModelNameExists(String name, Integer id) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(dm) FROM DeviceModels dm WHERE LOWER(TRIM(dm.name)) = LOWER(TRIM(:name)) ");
        if (id != null) {
            jpql.append("AND dm.id != :id");
        }
        var query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("name", name.trim());
        if (id != null) {
            query.setParameter("id", id);
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public List<DeviceModels> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return em.createQuery("SELECT dm FROM DeviceModels dm ORDER BY dm.id DESC", DeviceModels.class)
                    .getResultList();
        }
        String jpql = "SELECT dm FROM DeviceModels dm WHERE "
                + "LOWER(dm.name) LIKE :keyword "
                + "OR LOWER(dm.brand) LIKE :keyword "
                + "OR LOWER(dm.model) LIKE :keyword "
                + "OR LOWER(dm.type) LIKE :keyword "
                + "ORDER BY dm.id DESC";
        return em.createQuery(jpql, DeviceModels.class)
                .setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public int totalDeviceModels() {
        Long count = em.createQuery("SELECT COUNT(dm) from DeviceModels dm", Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int totalModelBrand() {
        Long count = em.createQuery("SELECT COUNT(DISTINCT dm.brand) FROM DeviceModels dm", Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int totalModelType() {
        Long count = em.createQuery("SELECT COUNT(DISTINCT dm.type) FROM DeviceModels dm", Long.class).getSingleResult();
        return count != null ? count.intValue() : 0;
    }

}
