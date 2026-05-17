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
            case "/presupuestos/create":
                return handleCreate(request, response, session);
            case "/presupuestos/update":
                return handleUpdate(request, response, session);
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
            dep = user.get("nombreDepartamento");
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

    private String handleCreate(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        if (user == null || !("Administrador".equals(user.get("rol")) || "Admin".equals(user.get("rol")))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return JsonUtil.errorJson("No tienes permisos para crear presupuestos");
        }

        String body = JsonUtil.getRequestBody(request);
        String cantidadStr = JsonUtil.findJsonField(body, "cantidad");
        String gastoStr = JsonUtil.findJsonField(body, "gasto");
        String type = JsonUtil.findJsonField(body, "type");
        String idDeptStr = JsonUtil.findJsonField(body, "iddepartamento");
        String anioStr = JsonUtil.findJsonField(body, "anio");

        if (cantidadStr == null || idDeptStr == null || type == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Faltan parámetros requeridos");
        }

        try {
            double cantidad = Double.parseDouble(cantidadStr);
            double gasto = (gastoStr != null && !gastoStr.isEmpty()) ? Double.parseDouble(gastoStr) : 0.0;
            long idDepartamento = Long.parseLong(idDeptStr);
            int anio = (anioStr != null && !anioStr.isEmpty()) ? Integer.parseInt(anioStr) : java.time.LocalDate.now().getYear();

            Budget budget = new Budget();
            budget.setCantidad(cantidad);
            budget.setGasto(gasto);
            budget.setIdDepartamento(idDepartamento);
            budget.setType(type);

            String res = budgetService.createBudget(budget, anio);
            if ("success".equals(res)) {
                return JsonUtil.messageJson("Presupuesto creado correctamente");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return JsonUtil.errorJson(res);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Formato de número inválido");
        }
    }

    private String handleUpdate(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        if (user == null || !("Administrador".equals(user.get("rol")) || "Admin".equals(user.get("rol")))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return JsonUtil.errorJson("No tienes permisos para editar presupuestos");
        }

        String body = JsonUtil.getRequestBody(request);
        String idStr = JsonUtil.findJsonField(body, "idpresupuesto");
        if (idStr == null) {
            idStr = JsonUtil.findJsonField(body, "idPresupuesto");
        }
        String cantidadStr = JsonUtil.findJsonField(body, "cantidad");
        String gastoStr = JsonUtil.findJsonField(body, "gasto");
        String type = JsonUtil.findJsonField(body, "type");
        String idDeptStr = JsonUtil.findJsonField(body, "iddepartamento");
        String anioStr = JsonUtil.findJsonField(body, "anio");

        if (idStr == null || cantidadStr == null || idDeptStr == null || type == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Faltan parámetros requeridos");
        }

        try {
            long id = Long.parseLong(idStr);
            double cantidad = Double.parseDouble(cantidadStr);
            double gasto = (gastoStr != null && !gastoStr.isEmpty()) ? Double.parseDouble(gastoStr) : 0.0;
            long idDepartamento = Long.parseLong(idDeptStr);
            int anio = (anioStr != null && !anioStr.isEmpty()) ? Integer.parseInt(anioStr) : java.time.LocalDate.now().getYear();

            Budget budget = new Budget();
            budget.setIdPresupuesto(id);
            budget.setCantidad(cantidad);
            budget.setGasto(gasto);
            budget.setIdDepartamento(idDepartamento);
            budget.setType(type);

            String res = budgetService.updateBudget(budget, anio);
            if ("success".equals(res)) {
                return JsonUtil.messageJson("Presupuesto actualizado correctamente");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return JsonUtil.errorJson(res);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Formato de número inválido");
        }
    }
}
