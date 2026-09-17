package com.lens.device.facade;

import com.lens.device.entity.Devices;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Local
public interface DevicesFacadeLocal {

    void create(Devices devices);

    void edit(Devices devices);

    void remove(Devices devices);

    Devices find(Object id);

    List<Devices> findAll();

    List<Devices> findRange(int[] range);

    int count();

    boolean isSerialNumber(String serialNumber, Integer id);

    List<Devices> search(String keyword, String status);

    int totalDevices();

    int totalDevicesAvailable();

    int totalDevicesRenting();

    List<Devices> findByDeviceModelId(Integer modelId);
}
