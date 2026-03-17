package com.example.tailorapp.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("dateFormatter")
public class DateFormatter {

    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DD_MM_YYYY_HH_MM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Format LocalDate to DD/MM/YYYY format
     * @param date the date to format
     * @return formatted date string or empty string if null
     */
    public String format(LocalDate date) {
        return date != null ? date.format(DD_MM_YYYY) : "";
    }

    /**
     * Format LocalDateTime to DD/MM/YYYY HH:mm format
     * @param dateTime the datetime to format
     * @return formatted datetime string or empty string if null
     */
    public String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DD_MM_YYYY_HH_MM) : "";
    }

    /**
     * Format LocalDate to DD/MM/YYYY format (static method)
     * @param date the date to format
     * @return formatted date string or empty string if null
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DD_MM_YYYY) : "";
    }

    /**
     * Format LocalDateTime to DD/MM/YYYY HH:mm format (static method)
     * @param dateTime the datetime to format
     * @return formatted datetime string or empty string if null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DD_MM_YYYY_HH_MM) : "";
    }
}