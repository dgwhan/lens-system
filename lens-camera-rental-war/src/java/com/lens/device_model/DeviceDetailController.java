package com.lens.device_model;

import com.lens.common.util.FormatUtil;
import com.lens.device.entity.Devices;
import com.lens.device.facade.DevicesFacadeLocal;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "deviceDetailController")
@RequestScoped
public class DeviceDetailController {

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @EJB
    private DevicesFacadeLocal devicesFacade;

    private DeviceModels deviceModel;
    private Integer id;
    private String startDate;
    private String endDate;

    public DeviceDetailController() {
    }

    public void loadDeviceDetail() {
        if (id == null || id <= 0) {
            redirectTo404();
            return;
        }
        deviceModel = deviceModelsFacade.find(id);

        if (deviceModel == null) {
            redirectTo404();
        }
    }

    private void redirectTo404() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            ExternalContext ec = facesContext.getExternalContext();
            try {
                ec.responseSendError(404, "Device not found");
                facesContext.responseComplete();
            } catch (IOException ex) {
                // Ignore fallback
            }
        }
    }

    /**
     * Check if at least one physical device of this model has status AVAILABLE.
     *
     * @return true if available
     */
    public boolean isAvailable() {
        if (id == null) {
            return false;
        }
        List<Devices> devices = devicesFacade.findByDeviceModelId(id);
        if (devices == null || devices.isEmpty()) {
            return false;
        }
        return devices.stream().anyMatch(d -> "AVAILABLE".equalsIgnoreCase(d.getStatus()));
    }

    /**
     * Get human-readable availability status string ("AVAILABLE", "RENTING", or "UNAVAILABLE").
     *
     * @return status name
     */
    public String getAvailabilityStatus() {
        if (id == null) {
            return "UNAVAILABLE";
        }
        List<Devices> devices = devicesFacade.findByDeviceModelId(id);
        if (devices == null || devices.isEmpty()) {
            return "UNAVAILABLE";
        }
        boolean hasAvailable = devices.stream().anyMatch(d -> "AVAILABLE".equalsIgnoreCase(d.getStatus()));
        return hasAvailable ? "AVAILABLE" : "RENTING";
    }

    /**
     * Format currency amount for display (e.g. 350000 -> "350,000 VND").
     *
     * @param price the price to format
     * @return formatted currency
     */
    public String formatPrice(long price) {
        return FormatUtil.formatPrice(price);
    }

    /**
     * Format number with thousand separators (e.g. 350000 -> "350,000").
     *
     * @param number the number to format
     * @return formatted string
     */
    public String formatNumber(long number) {
        return FormatUtil.formatNumber(number);
    }

    /**
     * Get browser request URL for device image.
     *
     * @param imageName the image filename or path
     * @return full resource request URL
     */
    public String getImageUrl(String imageName) {
        return com.lens.common.util.ImageUtil.getDeviceImageUrl(imageName);
    }

    public DeviceModels getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(DeviceModels deviceModel) {
        this.deviceModel = deviceModel;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
