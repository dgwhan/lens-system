package com.lens.sbeans;

import com.lens.ebeans.DeviceModels;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface DeviceModelsFacadeLocal {

    void create(DeviceModels deviceModels);

    void edit(DeviceModels deviceModels);

    void remove(DeviceModels deviceModels);

    DeviceModels find(Object id);

    List<DeviceModels> findAll();

    List<DeviceModels> findRange(int[] range);

    int count();
    
    boolean isBrandModelExists(String brand, String model, Integer id);

    boolean isDeviceModelNameExists(String name, Integer id);

    List<DeviceModels> search(String keyword);

}
