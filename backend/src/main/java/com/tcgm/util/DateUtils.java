package com.tcgm.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {

    // Formateurs de date
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN_FR = "dd/MM/yyyy";
    public static final String DATE_TIME_PATTERN_FR = "dd/MM/yyyy HH:mm:ss";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER_FR = DateTimeFormatter.ofPattern(DATE_PATTERN_FR);
    private static final DateTimeFormatter DATE_TIME_FORMATTER_FR = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN_FR);

    /**
     * Formate une LocalDate en chaîne (yyyy-MM-dd)
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formate une LocalDate en chaîne avec le format FR (dd/MM/yyyy)
     */
    public static String formatDateFr(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER_FR);
    }

    /**
     * Formate une LocalDateTime en chaîne (yyyy-MM-dd HH:mm:ss)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Formate une LocalDateTime en chaîne avec le format FR (dd/MM/yyyy HH:mm:ss)
     */
    public static String formatDateTimeFr(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATE_TIME_FORMATTER_FR);
    }

    /**
     * Parse une chaîne en LocalDate (yyyy-MM-dd)
     */
    public static LocalDate parseDate(String date) {
        if (date == null || date.isEmpty()) return null;
        try {
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(date, DATE_FORMATTER_FR);
            } catch (DateTimeParseException ex) {
                return LocalDate.parse(date);
            }
        }
    }

    /**
     * Parse une chaîne en LocalDateTime (yyyy-MM-dd HH:mm:ss)
     */
    public static LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER_FR);
            } catch (DateTimeParseException ex) {
                return LocalDateTime.parse(dateTime);
            }
        }
    }

    /**
     * Calcule le nombre de jours entre deux dates
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calcule le nombre de jours entre deux dates (valeur absolue)
     */
    public static long daysBetweenAbs(LocalDate start, LocalDate end) {
        return Math.abs(daysBetween(start, end));
    }

    /**
     * Vérifie si une date est aujourd'hui
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * Vérifie si une date est dans le passé
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Vérifie si une date est dans le futur
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Vérifie si une date est entre deux dates
     */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) return false;
        return (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end));
    }

    /**
     * Ajoute des jours à une date
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) return null;
        return date.plusDays(days);
    }

    /**
     * Ajoute des mois à une date
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        if (date == null) return null;
        return date.plusMonths(months);
    }

    /**
     * Ajoute des années à une date
     */
    public static LocalDate addYears(LocalDate date, long years) {
        if (date == null) return null;
        return date.plusYears(years);
    }

    /**
     * Obtient le début de la journée (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay();
    }

    /**
     * Obtient la fin de la journée (23:59:59.999)
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atTime(23, 59, 59);
    }

    /**
     * Convertit java.util.Date en LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return new java.sql.Date(date.getTime()).toLocalDate();
    }

    /**
     * Convertit LocalDate en java.util.Date
     */
    public static Date toDate(LocalDate date) {
        if (date == null) return null;
        return java.sql.Date.valueOf(date);
    }

    /**
     * Convertit LocalDateTime en java.util.Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return java.sql.Timestamp.valueOf(dateTime);
    }

    /**
     * Obtient l'heure actuelle formatée
     */
    public static String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * Obtient la date actuelle formatée
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * Obtient la date et l'heure actuelles formatées
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * Vérifie si une chaîne est une date valide
     */
    public static boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) return false;
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}