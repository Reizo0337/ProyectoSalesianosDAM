package com.salesianos.servlets;

import com.salesianos.utils.DBConnector;
import com.salesianos.utils.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getPathInfo();
        if (path == null) path = "/";

        String jsonResponse;

        switch (path) {

            case "/db":
                try (Connection conn = DBConnector.getConnection()) {
                    jsonResponse = JsonUtil.messageJson("Conexión a la base de datos exitosa.");
                } catch (SQLException e) {
                    jsonResponse = JsonUtil.errorJson("Error al conectar con la base de datos: " + e.getMessage());
                }
                break;

            default:
                jsonResponse = JsonUtil.messageJson("API funcionando en endpoint: " + path);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(jsonResponse);
    }
}
