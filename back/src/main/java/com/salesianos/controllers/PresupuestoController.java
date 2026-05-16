package com.salesianos.controllers;

import com.salesianos.models.Budget;
import com.salesianos.models.Role;
import com.salesianos.services.BudgetService;
import com.salesianos.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PresupuestoController {

    private final BudgetService budgetService = new BudgetService();

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

        List<Budget> budgets = budgetService.getBudgetsByDept(dep, anio);
        List<Map<String, String>> data = budgets.stream().map(Budget::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "presupuestos");
    }

    private String handleAllPresupuestos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String anioStr = JsonUtil.findJsonField(body, "anio");
        int anio = (anioStr != null && !anioStr.isEmpty()) ? Integer.parseInt(anioStr) : java.time.LocalDate.now().getYear();

        List<Budget> budgets = budgetService.getBudgetsByYear(anio);
        List<Map<String, String>> data = budgets.stream().map(Budget::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "presupuestos");
    }

    private String handleGetYears(HttpServletResponse response) {
        List<Integer> years = budgetService.getYears();
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
        
        boolean ok = budgetService.cloneBudgets(fromYear, toYear);
        return ok ? JsonUtil.messageJson("Presupuestos clonados correctamente") : JsonUtil.errorJson("Error al clonar presupuestos");
    }
}
