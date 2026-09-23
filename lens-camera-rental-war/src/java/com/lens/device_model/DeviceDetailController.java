package com.lens.device_model;

import com.lens.availability.dto.AvailabilityResult;
import com.lens.availability.service.AvailabilityServiceLocal;
import com.lens.common.util.DateUtil;
import com.lens.common.util.FormatUtil;
import com.lens.device.facade.DevicesFacadeLocal;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "deviceDetailController")
@ViewScoped
public class DeviceDetailController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    @EJB
    private AvailabilityServiceLocal availabilityService;

    private DeviceModels deviceModel;
    private Integer id;
    private String startDate;
    private String endDate;
    private AvailabilityResult availabilityResult;

    public DeviceDetailController() {
    }

    public void loadDeviceDetail() {
        if (id == null || id <= 0) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                String idParam = context.getExternalContext().getRequestParameterMap().get("id");
                if (idParam == null || idParam.isEmpty()) {
                    idParam = context.getExternalContext().getRequestParameterMap().get("deviceModelIdHidden");
                }
                if (idParam != null && !idParam.isEmpty()) {
                    try {
                        id = Integer.parseInt(idParam);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        if (id == null || id <= 0) {
            redirectTo404();
            return;
        }

        deviceModel = deviceModelsFacade.find(id);

        if (deviceModel == null) {
            redirectTo404();
        }
    }

    //kiểm tra ngày thuê đang chọn có khả dụng không
    public void checkAvailability() {
        if (id == null || id <= 0) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                String idParam = context.getExternalContext().getRequestParameterMap().get("id");
                if (idParam == null || idParam.isEmpty()) {
                    idParam = context.getExternalContext().getRequestParameterMap().get("deviceModelIdHidden");
                }
                if (idParam != null && !idParam.isEmpty()) {
                    try {
                        id = Integer.parseInt(idParam);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        if (deviceModel == null && id != null && id > 0) {
            deviceModel = deviceModelsFacade.find(id);
        }

        //kiểm tra ngày trước khi parse
        if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                if (startDate == null || startDate.trim().isEmpty()) {
                    startDate = context.getExternalContext().getRequestParameterMap().get("startDate");
                    if (startDate == null || startDate.trim().isEmpty()) {
                        startDate = context.getExternalContext().getRequestParameterMap().get("rentalStartDate");
                    }
                }
                if (endDate == null || endDate.trim().isEmpty()) {
                    endDate = context.getExternalContext().getRequestParameterMap().get("endDate");
                    if (endDate == null || endDate.trim().isEmpty()) {
                        endDate = context.getExternalContext().getRequestParameterMap().get("rentalEndDate");
                    }
                }
            }
        }

        if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            availabilityResult = new AvailabilityResult(false, 0, 0, 0, "Please select both start date and end date.");
            return;
        }

        try {
            //convert String từ form thành Date thông qua DateUtil
            Date requestedStartDate = DateUtil.parseDate(startDate);
            Date requestedEndDate = DateUtil.parseDate(endDate);
            //gọi business service
            availabilityResult = availabilityService.checkAvailability(id, requestedStartDate, requestedEndDate);
        } catch (ParseException ex) {
            //ngày không đúng format
            availabilityResult = new AvailabilityResult(false, 0, 0, 0, "Please select a valid rental period.");
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

    //thiết bị còn hàng
    public boolean isProductAvailable() {
        if (id == null || availabilityService == null) {
            return false;
        }
        return availabilityService.isProductAvailable(id);
    }

    //thiết bị hết hàng
    public String getProductAvailabilityStatus() {
        if (id == null || availabilityService == null) {
            return "OUT OF STOCK";
        }
        return availabilityService.getProductAvailabilityStatus(id);
    }

    public boolean isAvailable() {
        return isProductAvailable();
    }

    public String getAvailabilityStatus() {
        return getProductAvailabilityStatus();
    }

    public String getStorePhone() {
        return "+84 xxx xxx xxx";
    }


    public String formatPrice(long price) {
        return FormatUtil.formatPrice(price);
    }

    public String formatNumber(long number) {
        return FormatUtil.formatNumber(number);
    }

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

    public AvailabilityResult getAvailabilityResult() {
        return availabilityResult;
    }

    public long getRentalDays() {
        return DateUtil.calculateDaysBetween(startDate, endDate);
    }

    public long getCalculatedRentalFee() {
        if (deviceModel == null) {
            return 0;
        }
        return getRentalDays() * deviceModel.getRentalPrice();
    }

    public String getFormattedRentalFee() {
        return formatPrice(getCalculatedRentalFee());
    }

    public long getEstimatedTotalAmount() {
        if (deviceModel == null) {
            return 0;
        }
        return getCalculatedRentalFee() + deviceModel.getDepositAmount();
    }

    public String getFormattedEstimatedTotal() {
        return formatPrice(getEstimatedTotalAmount());
    }

    public String getDisplayStartDate() {
        return DateUtil.formatDisplayDate(startDate);
    }

    public String getDisplayEndDate() {
        return DateUtil.formatDisplayDate(endDate);
    }

    public String formatDisplayDate(String dateStr) {
        return DateUtil.formatDisplayDate(dateStr);
    }
}