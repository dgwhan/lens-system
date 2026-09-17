package com.lens.controller;

import com.lens.entity.DeviceModels;
import com.lens.entity.Devices;
import com.lens.facade.DeviceModelsFacadeLocal;
import com.lens.facade.DevicesFacadeLocal;
import com.lens.util.ImageUtil;
import com.lens.util.FacesUtil;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "deviceModelsMB")
@SessionScoped
public class DeviceModelController implements Serializable {

    @jakarta.ejb.EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @jakarta.ejb.EJB
    private DevicesFacadeLocal devicesFacade;

    private DeviceModels deviceModels = new DeviceModels();
    private boolean editMode;
    private String keyword = "";
    private Part imagePart;
    private boolean removeCurrentImage;

    public DeviceModelController() {
    }

    //insert
    public String newDeviceModel() {
        deviceModels = new DeviceModels();
        deviceModels.setImageUrl(ImageUtil.DEFAULT_IMAGE_NAME);
        imagePart = null;
        removeCurrentImage = false;
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

        if (imagePart != null && imagePart.getSize() > 0) {
            String uploadedImage = ImageUtil.processUpload(imagePart, "device", "deviceModelForm:imageFile", "model_");
            if (uploadedImage == null || FacesContext.getCurrentInstance().isValidationFailed()) {
                return null;
            }
            deviceModels.setImageUrl(uploadedImage);
        } else {
            deviceModels.setImageUrl(ImageUtil.DEFAULT_IMAGE_NAME);
        }

        try {
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
            return "/404?faces-redirect=true";
        }

        System.out.println("edit img: " + deviceModels.getImageUrl());

        imagePart = null;
        removeCurrentImage = false;
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

        //l╞░u t├¬n ß║únh c┼⌐
        String oldImage = deviceModels.getImageUrl();

        try {
            //upload ß║únh mß╗¢i nß║┐u ng╞░ß╗¥i d├╣ng chß╗ìn
            String uploadedImage = null;
            if (imagePart != null && imagePart.getSize() > 0) {
                uploadedImage = ImageUtil.processUpload(imagePart, "device", "deviceModelForm:imageFile", "model_");
                if (uploadedImage == null || FacesContext.getCurrentInstance().isValidationFailed()) {
                    return null;
                }
                deviceModels.setImageUrl(uploadedImage);
            } else if (removeCurrentImage) {
                deviceModels.setImageUrl(ImageUtil.DEFAULT_IMAGE_NAME);
            }

            //giß╗» ß║únh c┼⌐ nß║┐u kh├┤ng chß╗ìn ß║únh mß╗¢i v├á kh├┤ng x├│a
            if (deviceModels.getImageUrl() == null || deviceModels.getImageUrl().isBlank()) {
                deviceModels.setImageUrl(ImageUtil.DEFAULT_IMAGE_NAME);
            }

            deviceModels.setUpdatedAt(new Date());

            //cß║¡p nhß║¡t database
            deviceModelsFacade.edit(deviceModels);

            //x├│a ß║únh c┼⌐ sau khi cß║¡p nhß║¡t database th├ánh c├┤ng
            if ((uploadedImage != null || removeCurrentImage) && oldImage != null && !oldImage.equals(ImageUtil.DEFAULT_IMAGE_NAME)) {
                ImageUtil.deleteImage(oldImage, "device");
            }

            //reset file upload
            imagePart = null;
            removeCurrentImage = false;

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
        return deviceModels != null ? "detail" : "/404?faces-redirect=true";
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
        return ImageUtil.getDefaultImageUrl();
    }

    public String getImageUrl(String imageUrl) {
        return ImageUtil.getDeviceImageUrl(imageUrl);
    }

    public String getModelImageUrl() {
        return ImageUtil.getDeviceImageUrl(deviceModels != null ? deviceModels.getImageUrl() : null);
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

    public Part getImagePart() {
        return imagePart;
    }

    public void setImagePart(Part imagePart) {
        this.imagePart = imagePart;
    }

    public boolean isRemoveCurrentImage() {
        return removeCurrentImage;
    }

    public void setRemoveCurrentImage(boolean removeCurrentImage) {
        this.removeCurrentImage = removeCurrentImage;
    }

    public boolean isHasCustomImage() {
        if (deviceModels == null) {
            return false;
        }
        String img = deviceModels.getImageUrl();
        return img != null && !img.trim().isEmpty() && !img.equalsIgnoreCase(ImageUtil.DEFAULT_IMAGE_NAME);
    }

    public boolean getHasCustomImage() {
        return isHasCustomImage();
    }

    public int getTotalDeviceModels() {
        return deviceModelsFacade.totalDeviceModels();
    }

    public int getTotalModelBrand() {
        return deviceModelsFacade.totalModelBrand();
    }

    public int getTotalModelType() {
        return deviceModelsFacade.totalModelType();
    }

    public List<Devices> getModelDevices() {
        if (deviceModels == null || deviceModels.getId() == null) {
            return java.util.Collections.emptyList();
        }
        return devicesFacade.findByDeviceModelId(deviceModels.getId());
    }

}
