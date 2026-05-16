package com.salesianos.controllers;

import com.salesianos.models.Role;
import com.salesianos.utils.JsonUtil;
import com.salesianos.utils.Presupuestos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PresupuestoController {

    public String handle(HttpServletRequest request, HttpServletResponse response, String path, HttpSession session) throws IOException {
        switch (path) {
            case "/presupuestos":
                return handlePresupuestos(request, response, session);
            case "/presupuestos/all":
                return handleAllPresupuestos(request, response);
            case "/presupuestos/years":
                return handleGetYears(response);
            case "/presupuestos/clone":
                return handleClone(request, response);
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return JsonUtil.errorJson("Ruta de presupuestos no encontrada");
        }
    }

    private String handlePresupuestos(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String dep = JsonUtil.findJsonField(body, "nombreDepartamento");
        String anioStr = JsonUtil.findJsonField(body, "anio");
        int anio = (anioStr != null && !anioStr.isEmpty()) ? Integer.parseInt(anioStr) : java.time.LocalDate.now().getYear();

        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        Role role = Role.fromString(user.get("rol"));
        if (!role.hasGlobalAccess()) {
            dep = user.get("idDepartamento");
        }

        if (dep == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Falta el parámetro de departamento");
        }

        Presupuestos util = new Presupuestos();
        List<Map<String, String>> data = util.getPresupuestosByDept(dep, anio);
        return JsonUtil.listToJson(data, "presupuestos");
    }

    private String handleAllPresupuestos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String anioStr = JsonUtil.findJsonField(body, "anio");
        int anio = (anioStr != null && !anioStr.isEmpty()) ? Integer.parseInt(anioStr) : java.time.LocalDate.now().getYear();

        Presupuestos util = new Presupuestos();
        List<Map<String, String>> data = util.getAllPresupuestos(anio);
        return JsonUtil.listToJson(data, "presupuestos");
    }

    private String handleGetYears(HttpServletResponse response) {
        Presupuestos util = new Presupuestos();
        List<Integer> years = util.getYearsWithPresupuestos();
        StringBuilder json = new StringBuilder("{\"status\":\"success\",\"years\":[");
        for (int i = 0; i < years.size(); i++) {
            json.append(years.get(i));
            if (i < years.size() - 1) json.append(",");
        }
        json.append("]}");
        return json.toString();
    }
    private String handleClone(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String fromYearStr = JsonUtil.findJsonField(body, "fromYear");
        String toYearStr = JsonUtil.findJsonField(body, "toYear");
        
        if (fromYearStr == null || toYearStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Faltan parámetros fromYear o toYear");
        }
        
        int fromYear = Integer.parseInt(fromYearStr);
        int toYear = Integer.parseInt(toYearStr);
        
        Presupuestos util = new Presupuestos();
        boolean ok = util.cloneBudgets(fromYear, toYear);
        return ok ? JsonUtil.messageJson("Presupuestos clonados correctamente") : JsonUtil.errorJson("Error al clonar presupuestos");
    }
}
