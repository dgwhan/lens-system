package com.lens.mbeans;

import com.lens.ebeans.DeviceModels;
import com.lens.ebeans.Devices;
import com.lens.sbeans.DeviceModelsFacadeLocal;
import com.lens.sbeans.DevicesFacadeLocal;
import com.lens.util.FacesUtil;
import com.lens.util.ImageUtil;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "devicesMB")
@SessionScoped
public class DevicesMB implements Serializable {

    @jakarta.ejb.EJB
    private DevicesFacadeLocal devicesFacade;

    @jakarta.ejb.EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    private Devices devices = new Devices();
    private Integer selectedDeviceModelId;
    private boolean editMode;
    private String keyword = "";
    private String status = "";

    public DevicesMB() {
        devices.setStatus("AVAILABLE");
    }

    //insert 
    public String newDevice() {
        devices = new Devices();
        devices.setStatus("AVAILABLE");
        selectedDeviceModelId = null;
        editMode = false;
        return "form";
    }

    public String insertDevice() {
        boolean hasError = false;
        if (selectedDeviceModelId == null) {
            FacesUtil.addFieldError("deviceForm:deviceModel", "Device model is required.");
            hasError = true;
        }
        if (isDuplicateSerialNumber(null)) {
            hasError = true;
        }
        if (hasError) {
            return null;
        }
        try {
            DeviceModels dm = deviceModelsFacade.find(selectedDeviceModelId);
            devices.setDeviceModelId(dm);
            devices.setStatus("AVAILABLE");

            Date now = new Date();
            devices.setCreatedAt(now);
            devices.setUpdatedAt(now);

            devicesFacade.create(devices);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Device created successfully.");
            return "list?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to create device.");
            return null;
        }
    }
    
    // Mở form chỉnh sửa thiết bị
    public String editDevice(Integer id) {
        devices = devicesFacade.find(id);
        if (devices == null) {
            return "list?faces-redirect=true";
        }
        if (devices.getDeviceModelId() != null) {
            selectedDeviceModelId = devices.getDeviceModelId().getId();
        } else {
            selectedDeviceModelId = null;
        }
        editMode = true;
        return "form";
    }

    // Cập nhật thông tin thiết bị
    public String updateDevice() {
        boolean hasError = false;
        if (selectedDeviceModelId == null && (devices == null || devices.getDeviceModelId() == null)) {
            FacesUtil.addFieldError("deviceForm:deviceModel", "Device model is required.");
            hasError = true;
        }
        if (devices == null) {
            FacesUtil.addErrorMessage("Device not found.");
            return null;
        }
        if (isDuplicateSerialNumber(devices.getId())) {
            hasError = true;
        }

        if (hasError) {
            return null;
        }

        try {
            if (selectedDeviceModelId != null) {
                DeviceModels dm = deviceModelsFacade.find(selectedDeviceModelId);
                devices.setDeviceModelId(dm);
            }
            devices.setUpdatedAt(new Date());
            devicesFacade.edit(devices);

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Device updated successfully.");
            return "list?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to update device.");
            return null;
        }
    }

    // Xem chi tiết thiết bị
    public String detailDevice(Integer id) {
        devices = devicesFacade.find(id);
        if (devices == null) {
            return "list?faces-redirect=true";
        }
        return "detail";
    }

    // Xóa thiết bị
    public void deleteDevice(Integer id) {
        try {
            Devices d = devicesFacade.find(id);
            if (d != null) {
                devicesFacade.remove(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to delete device.");
        }
    }

    // Lấy danh sách thiết bị (hỗ trợ tìm kiếm theo từ khóa và trạng thái)
    public List<Devices> showAllDevices() {
        return devicesFacade.findAll();
    }

    public List<Devices> getDevicesList() {
        return devicesFacade.search(keyword, status);
    }

    // Đặt lại bộ lọc tìm kiếm
    public void resetFilter() {
        this.keyword = "";
        this.status = "";
    }

    public List<DeviceModels> getAllDeviceModels() {
        return deviceModelsFacade.findAll();
    }

    // Hỗ trợ hiển thị ảnh của model thiết bị
    public String getImageUrl(String imageUrl) {
        return ImageUtil.getDeviceImageUrl(imageUrl);
    }

    public String getDefaultImageUrl() {
        return ImageUtil.getDefaultImageUrl();
    }

    public String getModelImageUrl() {
        if (devices != null && devices.getDeviceModelId() != null) {
            return ImageUtil.getDeviceImageUrl(devices.getDeviceModelId().getImageUrl());
        }
        return ImageUtil.getDefaultImageUrl();
    }

    //validate 
    private boolean isDuplicateSerialNumber(Integer excludeId) {
        if (devicesFacade.isSerialNumber(devices.getSerialNumber(), excludeId)) {
            FacesUtil.addFieldError("deviceForm:serialNumber", "SerialNumber already exits.");
            return true;
        }
        return false;
    }

    public Devices getDevices() {
        return devices;
    }

    public void setDevices(Devices devices) {
        this.devices = devices;
    }

    public Integer getSelectedDeviceModelId() {
        if (selectedDeviceModelId != null) {
            return selectedDeviceModelId;
        }
        if (devices != null && devices.getDeviceModelId() != null) {
            return devices.getDeviceModelId().getId();
        }
        return null;
    }

    public void setSelectedDeviceModelId(Integer selectedDeviceModelId) {
        this.selectedDeviceModelId = selectedDeviceModelId;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
