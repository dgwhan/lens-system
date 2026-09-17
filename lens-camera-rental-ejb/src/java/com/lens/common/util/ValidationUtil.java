package com.lens.common.util;

import java.util.regex.Pattern;

/**
 *
 * @author Duong Ngoc Han
 */
public final class ValidationUtil {

    public static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9]).{8,255}$");
    public static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9}$");
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private ValidationUtil() {
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Optional email
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
