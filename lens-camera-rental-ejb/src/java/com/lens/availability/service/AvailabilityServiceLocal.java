package com.lens.availability.service;

import com.lens.availability.dto.AvailabilityResult;
import java.util.Date;

/**
 *
 * @author Duong Ngoc Han
 */
public interface AvailabilityServiceLocal {
    AvailabilityResult checkAvailability(Integer deviceModelId, Date requestedStartDate, Date requestedEndDate);

    // customer availability
    boolean isProductAvailable(Integer deviceModelId);

    // customer availability
    String getProductAvailabilityStatus(Integer deviceModelId);
}

