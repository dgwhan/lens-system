package com.lens.common.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 *
 * @author Duong Ngoc Han
 */
public final class FormatUtil {

    private FormatUtil() {
    }


    public static String formatNumber(long number) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(number);
    }


    public static String formatNumber(Long number) {
        if (number == null) {
            return "0";
        }
        return formatNumber(number.longValue());
    }

    public static String formatPrice(long price) {
        return formatNumber(price) + " VND";
    }

    public static String formatPrice(Long price) {
        if (price == null) {
            return "0 VND";
        }
        return formatPrice(price.longValue());
    }
}
