package com.salesianos.utils;

import java.io.BufferedReader;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilidad básica para JSON.
 */
public class JsonUtil {

    public static String errorJson(String message) {
        return String.format("{\"status\":\"error\",\"message\":\"%s\"}", escape(message));
    }

    public static String messageJson(String message) {
        return String.format("{\"status\":\"success\",\"message\":\"%s\"}", escape(message));
    }

    public static String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
