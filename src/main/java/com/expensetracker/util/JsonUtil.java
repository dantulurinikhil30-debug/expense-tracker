package com.expensetracker.util;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lightweight JSON utility for safely converting collections to JSON arrays
 * for frontend Chart.js consumption without external library friction.
 */
public class JsonUtil {

    /**
     * Converts a List of Strings into a JSON array string: ["Item 1", "Item 2"]
     */
    public static String toJsonStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return "[" + list.stream()
                .map(s -> "\"" + escapeJson(s) + "\"")
                .collect(Collectors.joining(",")) + "]";
    }

    /**
     * Converts a List of Numbers into a JSON array string: [100.00, 250.50]
     */
    public static String toJsonNumberList(List<? extends Number> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return "[" + list.stream()
                .map(Number::toString)
                .collect(Collectors.joining(",")) + "]";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
