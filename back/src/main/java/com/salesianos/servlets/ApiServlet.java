package com.salesianos.servlets;

import com.salesianos.controllers.AuthController;
import com.salesianos.controllers.OrderController;
import com.salesianos.controllers.PresupuestoController;
import com.salesianos.controllers.ProductController;
import com.salesianos.controllers.SupplierController;
import com.salesianos.utils.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "ApiMain", urlPatterns = {"/api/*"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class ApiServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ApiServlet.class.getName());

    private final AuthController authController = new AuthController();
    private final OrderController orderController = new OrderController();
    private final PresupuestoController presupuestoController = new PresupuestoController();
    private final ProductController productController = new ProductController();
    private final SupplierController supplierController = new SupplierController();

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
        String path = request.getPathInfo();
        if (path == null) path = "/";
        
        LOGGER.log(Level.INFO, "API Request received: {0} {1}", new Object[]{request.getMethod(), path});

        response.setCharacterEncoding("UTF-8");

        String jsonResponse = "";
        HttpSession session = request.getSession(false);
        boolean isAuth = (session != null && session.getAttribute("user") != null);

        // Rutas públicas
        if (path.equals("/login") || path.equals("/register")) {
            jsonResponse = authController.handle(request, response, path, session);
        } else if (!isAuth) {
            // Protección global para rutas privadas
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse = JsonUtil.errorJson("Autenticación requerida");
        } else {
            try {
                // Rutas privadas delegadas a controladores
                if (path.equals("/me") || path.equals("/logout")) {
                    jsonResponse = authController.handle(request, response, path, session);
                } else if (path.startsWith("/presupuestos")) {
                    jsonResponse = presupuestoController.handle(request, response, path, session);
                } else if (path.startsWith("/ordenes") || path.startsWith("/facturas") || path.startsWith("/comentarios") || path.startsWith("/notificaciones")) {
                    jsonResponse = orderController.handle(request, response, path, session);
                } else if (path.startsWith("/proveedores")) {
                    jsonResponse = supplierController.handle(request, response, path, session);
                } else if (path.startsWith("/productos")) {
                    jsonResponse = productController.handle(request, response, path);
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    jsonResponse = JsonUtil.messageJson("API System Online");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Internal Error at " + path, e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse = JsonUtil.errorJson("Pro Error: " + e.getMessage());
            }
        }

        // Si la respuesta es nula, significa que el controlador manejó la respuesta directamente (ej. descargas de PDF)
        if (jsonResponse != null && !jsonResponse.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write(jsonResponse);
        }
    }
}
