package com.schoolmanagementsystem.util;

import java.util.regex.Pattern;

/**
 * Utility class for data validation
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[\\d\\s\\-\\(\\)\\+]{10,20}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isPositiveInteger(String value) {
        try {
            int num = Integer.parseInt(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidGrade(String grade) {
        if (grade == null || grade.trim().isEmpty()) {
            return true; // Null grades are allowed (not yet assigned)
        }
        return grade.matches("^[A-F][+-]?$");
    }
}
