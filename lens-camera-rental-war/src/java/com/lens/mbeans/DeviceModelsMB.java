package com.lens.mbeans;

import com.lens.ebeans.DeviceModels;
import com.lens.sbeans.DeviceModelsFacadeLocal;
import com.lens.util.FacesUtil;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "deviceModelsMB")
@SessionScoped
public class DeviceModelsMB implements Serializable {

    private static final String DEFAULT_IMAGE_NAME = "default-img.png";
    private static final String DEFAULT_IMAGE_LIBRARY = "images";
    private static final String IMAGE_PATH_PREFIX = "device/";

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    private DeviceModels deviceModels = new DeviceModels();
    private boolean editMode;
    private String keyword = "";

    public DeviceModelsMB() {
    }

    //insert
    public String newDeviceModel() {
        deviceModels = new DeviceModels();
        deviceModels.setImageUrl(DEFAULT_IMAGE_NAME);
        editMode = false;
        return "form";
    }

    public String insertDeviceModel() {
        boolean hasError = false;
        if (isDuplicateName(null)) {
            hasError = true;
        }
        if (isDuplicateBrandModel(null)) {
            hasError = true;
        }
        if (hasError) {
            return null;
        }

        try {
            if (deviceModels.getImageUrl() == null || deviceModels.getImageUrl().isBlank()) {
                deviceModels.setImageUrl(DEFAULT_IMAGE_NAME);
            }

            Date now = new Date();
            deviceModels.setCreatedAt(now);
            deviceModels.setUpdatedAt(now);

            deviceModelsFacade.create(deviceModels);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Device model created successfully.");
            return "list?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to create device model.");
            return null;
        }
    }

    
    //open edit form
    public String editDeviceModel(Integer id) {
        deviceModels = deviceModelsFacade.find(id);
        if (deviceModels == null) {
            return "list?faces-redirect=true";
        }
        editMode = true;
        return "form";
    }

    public String updateDeviceModel() {
        boolean hasError = false;
        if (isDuplicateName(deviceModels.getId())) {
            hasError = true;
        }
        if (isDuplicateBrandModel(deviceModels.getId())) {
            hasError = true;
        }
        if (hasError) {
            return null;
        }

        try {
            if (deviceModels.getImageUrl() == null || deviceModels.getImageUrl().isBlank()) {
                deviceModels.setImageUrl(DEFAULT_IMAGE_NAME);
            }

            deviceModels.setUpdatedAt(new Date());
            deviceModelsFacade.edit(deviceModels);

            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("actionAlert", "Device model updated successfully.");
            return "list?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to update device model.");
            return null;
        }
    }

    //detail
    public String detailDeviceModel(Integer id) {
        deviceModels = deviceModelsFacade.find(id);
        return deviceModels != null ? "detail" : "list?faces-redirect=true";
    }

    
    //delete
    public void deleteDeviceModel(Integer id) {
        try {
            DeviceModels target = deviceModelsFacade.find(id);
            if (target != null) {
                deviceModelsFacade.remove(target);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesUtil.addErrorMessage("Failed to delete device model.");
        }
    }

    //list device model
    public List<DeviceModels> showAllDeviceModel() {
        return deviceModelsFacade.search(keyword);
    }

    public List<DeviceModels> getDeviceModelsList() {
        return deviceModelsFacade.search(keyword);
    }

    
    //reset search
    public void resetFilter() {
        this.keyword = "";
    }

    //validate
    private boolean isDuplicateBrandModel(Integer excludeId) {
        if (deviceModelsFacade.isBrandModelExists(deviceModels.getBrand(), deviceModels.getModel(), excludeId)) {
            FacesUtil.addFieldError("deviceModelForm:brand", "");
            FacesUtil.addFieldError("deviceModelForm:model", "Brand and Model combination already exists.");
            return true;
        }
        return false;
    }

    private boolean isDuplicateName(Integer excludeId) {
        if (deviceModelsFacade.isDeviceModelNameExists(deviceModels.getName(), excludeId)) {
            FacesUtil.addFieldError("deviceModelForm:name", "Device model name already exists.");
            return true;
        }
        return false;
    }

    public boolean isDeviceModelNameExists(String name, Integer id) {
        return deviceModelsFacade.isDeviceModelNameExists(name, id);
    }

    
    //set default img device
    public String getDefaultImageUrl() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                return context.getApplication()
                        .getResourceHandler()
                        .createResource(DEFAULT_IMAGE_NAME, DEFAULT_IMAGE_LIBRARY)
                        .getRequestPath();
            }
        } catch (Exception ignored) {
        }
        return "/resources/images/" + DEFAULT_IMAGE_NAME;
    }

    public String getImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return getDefaultImageUrl();
        }

        String path = imageUrl.trim();
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("/")) {
            return path;
        }

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                String resourceName = (path.equals(DEFAULT_IMAGE_NAME) || path.startsWith(IMAGE_PATH_PREFIX))
                        ? path
                        : IMAGE_PATH_PREFIX + path;

                return context.getApplication()
                        .getResourceHandler()
                        .createResource(resourceName, DEFAULT_IMAGE_LIBRARY)
                        .getRequestPath();
            }
        } catch (Exception ignored) {
        }

        return path;
    }

    public String getModelImageUrl() {
        return getImageUrl(deviceModels != null ? deviceModels.getImageUrl() : null);
    }

    public DeviceModels getDeviceModels() {
        return deviceModels;
    }

    public void setDeviceModels(DeviceModels deviceModels) {
        this.deviceModels = deviceModels;
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
}
