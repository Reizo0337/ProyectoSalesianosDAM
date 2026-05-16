package com.salesianos.controllers;

import com.salesianos.models.User;
import com.salesianos.services.AuthService;
import com.salesianos.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class AuthController {
    private final AuthService authService = new AuthService();

    public String handle(HttpServletRequest request, HttpServletResponse response, String path, HttpSession session) throws IOException {
        switch (path) {
            case "/login": return handleLogin(request, response);
            case "/me": return handleMe(request, response, session);
            case "/register": return handleRegister(request, response);
            case "/logout": return handleLogout(request);
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return JsonUtil.errorJson("Ruta de autenticación no encontrada");
        }
    }

    private String handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String usuario = JsonUtil.findJsonField(body, "usuario");
        String password = JsonUtil.findJsonField(body, "password");

        User u = authService.login(usuario, password);
        if (u != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", u.toMap());
            return JsonUtil.mapToJson(u.toMap());
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return JsonUtil.errorJson("Credenciales incorrectas");
    }

    private String handleMe(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        if (session != null && session.getAttribute("user") != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> userData = (Map<String, String>) session.getAttribute("user");
            return JsonUtil.mapToJson(userData);
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return JsonUtil.errorJson("No autenticado");
    }

    private String handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String msg = authService.register(
            JsonUtil.findJsonField(body, "nombre"),
            JsonUtil.findJsonField(body, "apellidos"),
            JsonUtil.findJsonField(body, "email"),
            JsonUtil.findJsonField(body, "password"),
            JsonUtil.findJsonField(body, "telefono")
        );
        return msg.contains("éxito") || msg.contains("success") ? JsonUtil.messageJson(msg) : JsonUtil.errorJson(msg);
    }

    private String handleLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return JsonUtil.messageJson("Sesión cerrada");
    }
}
