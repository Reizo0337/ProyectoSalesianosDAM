package com.salesianos.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;

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

    public static String findJsonField(String json, String fieldName) {
        if (json == null || json.isEmpty()) return null;
        // Case-insensitive search for the field name
        Pattern pattern = Pattern.compile("\"" + fieldName + "\":\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) return matcher.group(1);
        
        pattern = Pattern.compile("\"" + fieldName + "\":\\s*([^,}]+)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(json);
        if (matcher.find()) return matcher.group(1).trim().replace("\"", "");
        
        return null;
    }

    /**
     * Convierte un mapa a JSON e incluye status: success para compatibilidad con el frontend.
     */
    public static String mapToJson(Map<String, String> map) {
        if (map == null) return "{}";
        StringBuilder sb = new StringBuilder("{\"status\":\"success\"");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(",\"").append(escape(entry.getKey())).append("\":")
              .append(objectToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    public static String objectToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) obj;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(entry.getKey().toString())).append("\":")
                  .append(objectToJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            List<?> list = (List<?>) obj;
            for (Object item : list) {
                if (!first) sb.append(",");
                sb.append(objectToJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        
        return "\"" + escape(obj.toString()) + "\"";
    }

    public static String successJson(Object obj) {
        if (obj instanceof Map) {
            String json = objectToJson(obj);
            return "{\"status\":\"success\"," + json.substring(1);
        }
        return "{\"status\":\"success\",\"data\":" + objectToJson(obj) + "}";
    }

    public static String listToJson(List<Map<String, String>> list, String key) {
        return "{\"status\":\"success\",\"" + key + "\":" + objectToJson(list) + "}";
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
