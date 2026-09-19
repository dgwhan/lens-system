package com.lens.device_model;

import com.lens.common.util.FormatUtil;
import com.lens.common.util.ImageUtil;
import com.lens.device_model.entity.DeviceModels;
import com.lens.device_model.facade.DeviceModelsFacadeLocal;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Duong Ngoc Han
 */
@Named(value = "deviceCatalogController")
@ViewScoped
public class DeviceCatalogController implements Serializable {

    private static final long serialVersionUID = 1L;

    // Globally identifiable Constants for Rental Rate Filter Ranges
    public static final String PRICE_RANGE_UNDER_100 = "under_100";
    public static final String PRICE_RANGE_100_TO_300 = "100_300";
    public static final String PRICE_RANGE_300_TO_500 = "300_500";
    public static final String PRICE_RANGE_OVER_500 = "over_500";

    public static final long THRESHOLD_LOW_RATE = 100_000L;
    public static final long THRESHOLD_MID_RATE = 300_000L;
    public static final long THRESHOLD_HIGH_RATE = 500_000L;

    @EJB
    private DeviceModelsFacadeLocal deviceModelsFacade;

    // Filter Parameters
    private String search = "";
    private String type = "";
    private String brand = "";
    private String rate = "";

    // Pagination Parameters
    private int page = 1;
    private int pageSize = 12; // 12 items (fits clean 4-column card grid and table)

    // Metrics & Data Collections
    private int totalCount = 0;
    private int filteredCount = 0;
    private int totalPages = 1;

    private List<String> types = new ArrayList<>();
    private List<String> brands = new ArrayList<>();
    private List<DeviceModels> allDevices = new ArrayList<>();
    private List<DeviceModels> filteredDevices = new ArrayList<>();
    private List<DeviceModels> pagedDevices = new ArrayList<>();

    public DeviceCatalogController() {
    }

    @PostConstruct
    public void init() {
        loadFilterOptions();
        applyFilter();
    }

    /**
     * Loads distinct categories and brands from database.
     */
    public void loadFilterOptions() {
        try {
            types = deviceModelsFacade.findDistinctTypes();
            brands = deviceModelsFacade.findDistinctBrands();
        } catch (Exception e) {
            types = Collections.emptyList();
            brands = Collections.emptyList();
        }
    }

    /**
     * Applies search, category, brand, and rental rate filters,
     * updates counts, and slices for current page.
     */
    public void applyFilter() {
        try {
            allDevices = deviceModelsFacade.findAll();
            if (allDevices == null) {
                allDevices = Collections.emptyList();
            }
            totalCount = allDevices.size();

            // Apply filter criteria
            filteredDevices = allDevices.stream().filter(deviceModel -> {
                // 1. Search Query
                if (search != null && !search.trim().isEmpty()) {
                    String searchKeyword = search.trim().toLowerCase();
                    boolean isNameMatched = deviceModel.getName() != null && deviceModel.getName().toLowerCase().contains(searchKeyword);
                    boolean isBrandMatched = deviceModel.getBrand() != null && deviceModel.getBrand().toLowerCase().contains(searchKeyword);
                    boolean isModelMatched = deviceModel.getModel() != null && deviceModel.getModel().toLowerCase().contains(searchKeyword);
                    boolean isTypeMatched = deviceModel.getType() != null && deviceModel.getType().toLowerCase().contains(searchKeyword);
                    if (!isNameMatched && !isBrandMatched && !isModelMatched && !isTypeMatched) {
                        return false;
                    }
                }

                // 2. Category Type Filter
                if (type != null && !type.trim().isEmpty()) {
                    if (deviceModel.getType() == null || !deviceModel.getType().equalsIgnoreCase(type.trim())) {
                        return false;
                    }
                }

                // 3. Brand Filter
                if (brand != null && !brand.trim().isEmpty()) {
                    if (deviceModel.getBrand() == null || !deviceModel.getBrand().equalsIgnoreCase(brand.trim())) {
                        return false;
                    }
                }

                // 4. Rental Rate Range Filter (Self-explanatory, clean global identifiers)
                if (rate != null && !rate.trim().isEmpty()) {
                    long rentalPricePerDay = deviceModel.getRentalPrice();
                    String selectedRateRange = rate.trim();
                    switch (selectedRateRange) {
                        case PRICE_RANGE_UNDER_100:
                            if (rentalPricePerDay >= THRESHOLD_LOW_RATE) return false;
                            break;
                        case PRICE_RANGE_100_TO_300:
                            if (rentalPricePerDay < THRESHOLD_LOW_RATE || rentalPricePerDay > THRESHOLD_MID_RATE) return false;
                            break;
                        case PRICE_RANGE_300_TO_500:
                            if (rentalPricePerDay < THRESHOLD_MID_RATE || rentalPricePerDay > THRESHOLD_HIGH_RATE) return false;
                            break;
                        case PRICE_RANGE_OVER_500:
                            if (rentalPricePerDay <= THRESHOLD_HIGH_RATE) return false;
                            break;
                        default:
                            break;
                    }
                }

                return true;
            }).collect(Collectors.toList());

            filteredCount = filteredDevices.size();
            totalPages = Math.max(1, (int) Math.ceil((double) filteredCount / pageSize));

            // Clamp page
            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;

            // Slice for current page
            int fromIndex = (page - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, filteredCount);

            if (fromIndex < filteredCount) {
                pagedDevices = filteredDevices.subList(fromIndex, toIndex);
            } else {
                pagedDevices = Collections.emptyList();
            }

        } catch (Exception e) {
            e.printStackTrace();
            allDevices = Collections.emptyList();
            filteredDevices = Collections.emptyList();
            pagedDevices = Collections.emptyList();
            totalCount = 0;
            filteredCount = 0;
            totalPages = 1;
        }
    }

    /**
     * Formats currency amount using standard FormatUtil.
     */
    public String formatPrice(long price) {
        return FormatUtil.formatPrice(price);
    }

    /**
     * Resolves device model image URL.
     */
    public String getImageUrl(String imageName) {
        return ImageUtil.getDeviceImageUrl(imageName);
    }

    /**
     * Default placeholder device image.
     */
    public String getDefaultImageUrl() {
        return ImageUtil.getDefaultImageUrl();
    }

    // Getters and Setters
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getFilteredCount() {
        return filteredCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getBrands() {
        return brands;
    }

    public List<DeviceModels> getFilteredDevices() {
        return filteredDevices;
    }

    public List<DeviceModels> getPagedDevices() {
        return pagedDevices;
    }
}
