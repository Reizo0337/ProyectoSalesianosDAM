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
                return handleAllPresupuestos(response);
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return JsonUtil.errorJson("Ruta de presupuestos no encontrada");
        }
    }

    private String handlePresupuestos(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String dep = JsonUtil.findJsonField(body, "nombreDepartamento");

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
        List<Map<String, String>> data = util.getPresupuestosByDept(dep);
        return JsonUtil.listToJson(data, "presupuestos");
    }

    private String handleAllPresupuestos(HttpServletResponse response) {
        Presupuestos util = new Presupuestos();
        List<Map<String, String>> data = util.getAllPresupuestos();
        return JsonUtil.listToJson(data, "presupuestos");
    }
}
