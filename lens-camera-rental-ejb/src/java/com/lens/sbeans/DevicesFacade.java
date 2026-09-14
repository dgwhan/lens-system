package com.lens.sbeans;

import com.lens.ebeans.Devices;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author Duong Ngoc Han
 */
@Stateless
public class DevicesFacade extends AbstractFacade<Devices> implements DevicesFacadeLocal {

    @PersistenceContext(unitName = "lens-camera-rental-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DevicesFacade() {
        super(Devices.class);
    }

    // Kiểm tra trùng lặp serial number
    @Override
    public boolean isSerialNumber(String serialNumber, Integer id) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return false;
        }
        
        StringBuilder jpql = new StringBuilder("SELECT COUNT(d) FROM Devices d WHERE LOWER(TRIM(d.serialNumber)) = LOWER(TRIM(:serialNumber))");
        if (id != null) {
            jpql.append(" AND d.id != :id");
        }
        
        var query = em.createQuery(jpql.toString(), Long.class).setParameter("serialNumber", serialNumber.trim());
        
        if (id != null) {
            query.setParameter("id", id);
        }
        
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    // Lấy toàn bộ danh sách thiết bị sắp xếp theo ID giảm dần
    @Override
    public java.util.List<Devices> findAll() {
        return em.createQuery("SELECT d FROM Devices d ORDER BY d.id DESC", Devices.class)
                .getResultList();
    }

    // Tìm kiếm thiết bị theo từ khóa (serial, model, brand) và trạng thái
    @Override
    public java.util.List<Devices> search(String keyword, String status) {
        StringBuilder jpql = new StringBuilder("SELECT d FROM Devices d WHERE 1=1 ");
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();

        if (hasKeyword) {
            jpql.append(" AND (LOWER(d.serialNumber) LIKE :keyword ")
                .append(" OR LOWER(d.deviceModelId.name) LIKE :keyword ")
                .append(" OR LOWER(d.deviceModelId.brand) LIKE :keyword ")
                .append(" OR LOWER(d.deviceModelId.model) LIKE :keyword)");
        }

        if (hasStatus) {
            jpql.append(" AND d.status = :status");
        }

        jpql.append(" ORDER BY d.id DESC");

        var query = em.createQuery(jpql.toString(), Devices.class);

        if (hasKeyword) {
            query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        if (hasStatus) {
            query.setParameter("status", status.trim());
        }

        return query.getResultList();
    }
}
