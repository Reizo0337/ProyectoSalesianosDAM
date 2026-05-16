package com.salesianos.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;

// Utilidad básica para JSON.
public class JsonUtil {

    // Crea un JSON de error.
    public static String errorJson(String message) {
        return String.format("{\"status\":\"error\",\"message\":\"%s\"}", escape(message));
    }

    // Crea un JSON de mensaje.
    public static String messageJson(String message) {
        return String.format("{\"status\":\"success\",\"message\":\"%s\"}", escape(message));
    }

    // Lee el cuerpo de la petición y lo devuelve como String.
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

    // Busca un campo en un JSON.
    public static String findJsonField(String json, String fieldName) {
        if (json == null || json.isEmpty()) return null;
        Pattern pattern = Pattern.compile("\"" + fieldName + "\":\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Convierte un mapa a JSON.
    public static String mapToJson(java.util.Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        if (map != null) {
            for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(entry.getKey())).append("\":\"")
                  .append(escape(entry.getValue())).append("\"");
                first = false;
            }
        }
        if (!first) {
            sb.append(",");
            sb.append("\"status\":\"success\"}");
        } else {
            sb.append("}");
        }
        return sb.toString();
    }

    // Convierte una lista de mapas a JSON.
    public static String listToJson(java.util.List<java.util.Map<String, String>> list) {
        return listToJson(list, "data");
    }

    // Convierte una lista de mapas a JSON con una clave específica.
    public static String listToJson(java.util.List<java.util.Map<String, String>> list, String key) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"status\":\"success\",");
        sb.append("\"").append(key).append("\":[");
        boolean firstMap = true;
        if (list != null) {
            for (java.util.Map<String, String> map : list) {
                if (!firstMap) sb.append(",");
                sb.append("{");
                boolean firstEntry = true;
                for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
                    if (!firstEntry) sb.append(",");
                    sb.append("\"").append(escape(entry.getKey())).append("\":\"")
                      .append(escape(entry.getValue())).append("\"");
                    firstEntry = false;
                }
                sb.append("}");
                firstMap = false;
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    // Escapa caracteres especiales en un String para que sea seguro incluirlo en un JSON.
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
