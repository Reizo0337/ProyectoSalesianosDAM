package com.salesianos.controllers;

import com.salesianos.utils.JsonUtil;
import com.salesianos.utils.Login;
import com.salesianos.utils.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class AuthController {

    public String handle(HttpServletRequest request, HttpServletResponse response, String path, HttpSession session) throws IOException {
        switch (path) {
            case "/login":
                return handleLogin(request, response);
            case "/me":
                return handleMe(request, response, session);
            case "/register":
                return handleRegister(request, response);
            case "/logout":
                return handleLogout(request);
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return JsonUtil.errorJson("Ruta de autenticación no encontrada");
        }
    }

    private String handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String usuario = JsonUtil.findJsonField(body, "usuario");
        String password = JsonUtil.findJsonField(body, "password");

        if (usuario == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Faltan credenciales");
        }

        Login loginUtil = new Login();
        Map<String, String> userData = loginUtil.authenticate(usuario, password);
        
        if (userData != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", userData);
            response.setStatus(HttpServletResponse.SC_OK);
            return JsonUtil.mapToJson(userData);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return JsonUtil.errorJson("Credenciales Pro incorrectas");
        }
    }

    private String handleMe(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        if (session != null && session.getAttribute("user") != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            @SuppressWarnings("unchecked")
            Map<String, String> userData = (Map<String, String>) session.getAttribute("user");
            return JsonUtil.mapToJson(userData);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return JsonUtil.errorJson("No autenticado");
        }
    }

    private String handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String nombre = JsonUtil.findJsonField(body, "nombre");
        String email = JsonUtil.findJsonField(body, "email");
        String password = JsonUtil.findJsonField(body, "password");

        if (nombre == null || email == null || password == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Faltan campos Pro obligatorios");
        }

        Users util = new Users();
        String result = util.register(nombre, JsonUtil.findJsonField(body,"apellidos"), email, password, JsonUtil.findJsonField(body,"telefono"));
        response.setStatus(result.contains("success") ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        return result;
    }

    private String handleLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return JsonUtil.messageJson("Sesión cerrada");
    }
}
