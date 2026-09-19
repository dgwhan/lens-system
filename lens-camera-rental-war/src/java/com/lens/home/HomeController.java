package com.lens.home;

import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import com.lens.common.util.ImageUtil;
import com.lens.common.util.FormatUtil;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "homeController")
@RequestScoped
public class HomeController implements Serializable {

    private static final long serialVersionUID = 1L;

    @jakarta.ejb.EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    private List<DeviceModels> featuredDevices;

    public HomeController() {
    }

    @PostConstruct
    public void init() {
        loadFeaturedDevices();
    }

    public void loadFeaturedDevices() {
        try {
            List<DeviceModels> all = deviceModelsFacade.search("");
            if (all != null && !all.isEmpty()) {
                int limit = Math.min(4, all.size());
                featuredDevices = all.subList(0, limit);
            } else {
                featuredDevices = Collections.emptyList();
            }
        } catch (Exception e) {
            featuredDevices = Collections.emptyList();
        }
    }

    public List<DeviceModels> getFeaturedDevices() {
        if (featuredDevices == null) {
            loadFeaturedDevices();
        }
        return featuredDevices;
    }

    public void setFeaturedDevices(List<DeviceModels> featuredDevices) {
        this.featuredDevices = featuredDevices;
    }

    public String formatPrice(long price) {
        return FormatUtil.formatPrice(price);
    }

    public String getImageUrl(String imageName) {
        return ImageUtil.getDeviceImageUrl(imageName);
    }

    public String getDefaultImageUrl() {
        return ImageUtil.getDefaultImageUrl();
    }
}
