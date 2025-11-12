package com.example.tailorapp.config;

/**
 * Application-wide constants to avoid magic strings and numbers throughout the codebase.
 */
public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // Payment Status Constants
    public static final String PAYMENT_STATUS_PAID = "Paid";
    public static final String PAYMENT_STATUS_PARTIAL = "Partial";
    public static final String PAYMENT_STATUS_UNPAID = "Unpaid";

    // Company Information
    public static final String COMPANY_NAME = "STITCH & STYLE";

    // File Upload
    public static final long MAX_UPLOAD_SIZE_MB = 10;
    public static final String MAX_UPLOAD_SIZE_ERROR_MESSAGE =
        "File too large! Please upload an image under 10 MB.";

    // PDF Generation Constants
    public static final float PDF_SLIP_WIDTH_DIVISOR = 2f;
    public static final int PDF_TITLE_FONT_SIZE = 10;
    public static final int PDF_HEADER_FONT_SIZE = 8;
    public static final int PDF_LABEL_FONT_SIZE = 7;
    public static final int PDF_VALUE_FONT_SIZE = 6;
    public static final float PDF_LABEL_CELL_PADDING = 3f;
    public static final float PDF_VALUE_CELL_PADDING = 5f;

    // Date/Time Format
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String DATE_FORMAT = "dd-MMM-yyyy";
}