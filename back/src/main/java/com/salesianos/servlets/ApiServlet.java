package com.salesianos.servlets;

import com.salesianos.utils.JsonUtil;
import com.salesianos.utils.Login;
import com.salesianos.utils.Presupuestos;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "ApiMain", urlPatterns = {"/api/*"})
public class ApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRequest(request, response);
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Simple CORS (for development)
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        if (path == null) path = "/";

        String jsonResponse = "";

        switch (path) {
            case "/login":
                try {
                    String body = JsonUtil.getRequestBody(request);
                    String usuario = JsonUtil.findJsonField(body, "usuario");
                    String password = JsonUtil.findJsonField(body, "password");

                    if (usuario == null || password == null) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        jsonResponse = JsonUtil.errorJson("Faltan credenciales");
                    } else {
                        Login loginUtility = new Login();
                        Map<String, String> userData = loginUtility.authenticate(usuario, password);
                        
                        if (userData != null) {
                            System.out.println("User authenticated: " + userData);
                            HttpSession session = request.getSession(true);
                            // Store user data in session
                            session.setAttribute("user", userData);
                            
                            response.setStatus(HttpServletResponse.SC_OK);
                            jsonResponse = JsonUtil.mapToJson(userData);
                        } else {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            jsonResponse = JsonUtil.errorJson("Credenciales incorrectas");
                        }
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    jsonResponse = JsonUtil.errorJson("Error interno: " + e.getMessage());
                }
                break;

            case "/presupuestos":
                try {
                    HttpSession session = request.getSession(false);
                    if (session == null || session.getAttribute("user") == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        jsonResponse = JsonUtil.errorJson("No autenticado. Por favor inicia sesión.");
                    } else {
                        String body = JsonUtil.getRequestBody(request);
                        String dep = JsonUtil.findJsonField(body, "nombreDepartamento");

                        Presupuestos presupuestosUtility = new Presupuestos();
                        if (dep == null) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            jsonResponse = JsonUtil.errorJson("Falta el departamento");
                        } else {
                            java.util.List<java.util.Map<String, String>> presupuestosData = presupuestosUtility.getPresupuestosByDept(dep);
                            jsonResponse = JsonUtil.listToJson(presupuestosData);
                            response.setStatus(HttpServletResponse.SC_OK);
                        }
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    jsonResponse = JsonUtil.errorJson("Error interno: " + e.getMessage());
                }
                break;

            case "/presupuestos/all":
                try {
                    HttpSession session = request.getSession(false);
                    if (session == null || session.getAttribute("user") == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        jsonResponse = JsonUtil.errorJson("No autenticado. Por favor inicia sesión.");
                    } else {
                        Presupuestos utility = new Presupuestos();
                        java.util.List<java.util.Map<String, String>> data = utility.getAllPresupuestos();
                        jsonResponse = JsonUtil.listToJson(data);
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    jsonResponse = JsonUtil.errorJson("Error interno: " + e.getMessage());
                }
                break;

            case "/me":
                HttpSession currentSession = request.getSession(false);
                if (currentSession != null && currentSession.getAttribute("user") != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    @SuppressWarnings("unchecked")
                    Map<String, String> userData = (Map<String, String>) currentSession.getAttribute("user");
                    jsonResponse = JsonUtil.mapToJson(userData);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    jsonResponse = JsonUtil.errorJson("No autenticado");
                }
                break;

            case "/logout":
                HttpSession sessionToInvalidate = request.getSession(false);
                if (sessionToInvalidate != null) {
                    sessionToInvalidate.invalidate();
                }
                response.setStatus(HttpServletResponse.SC_OK);
                jsonResponse = JsonUtil.messageJson("Sesión cerrada");
                break;

            default:
                jsonResponse = JsonUtil.messageJson("API funcionando en endpoint: " + path);
                response.setStatus(HttpServletResponse.SC_OK);
        }

        response.getWriter().write(jsonResponse);
    }
}
