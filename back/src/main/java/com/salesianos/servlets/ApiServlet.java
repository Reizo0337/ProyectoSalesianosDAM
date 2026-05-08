package com.salesianos.servlets;

import com.salesianos.utils.JsonUtil;
import com.salesianos.utils.Login;
import com.salesianos.utils.Presupuestos;
import com.salesianos.utils.Orders;
import com.salesianos.utils.Suppliers;
import com.salesianos.utils.Users;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

@WebServlet(name = "ApiMain", urlPatterns = {"/api/*"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50   // 50MB
)
public class ApiServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ApiServlet.class.getName());

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

        // Set default encoding, but defer content type
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = "";
        HttpSession session = request.getSession(false);
        boolean isAuth = (session != null && session.getAttribute("user") != null);

        try {
            switch (path) {
                case "/login":
                    jsonResponse = handleLogin(request, response);
                    break;

                case "/me":
                    jsonResponse = handleMe(request, response, session);
                    break;

                case "/presupuestos":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handlePresupuestos(request, response);
                    break;

                case "/presupuestos/all":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleAllPresupuestos(response);
                    break;

                case "/ordenes":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleOrders(request, response, session);
                    break;

                case "/ordenes/all":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleAllOrders(request, response);
                    break;

                case "/ordenes/update":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleOrderUpdate(request, response, session);
                    break;

                case "/ordenes/next-number":
                    if (!isAuth) returnAuthError(response);
                    else {
                        String dept = request.getParameter("dept");
                        String year = request.getParameter("year");
                        Orders oUtil = new Orders();
                        String nextSeq = oUtil.getNextOrderSequence(dept, year);
                        jsonResponse = "{\"status\":\"success\",\"nextSequence\":\"" + nextSeq + "\"}";
                    }
                    break;

                case "/ordenes/detail":
                    if (!isAuth) returnAuthError(response);
                    else {
                        String idParam = request.getParameter("id");
                        if (idParam != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> user = (Map<String, String>) session.getAttribute("user");
                            Orders detailUtil = new Orders();
                            jsonResponse = detailUtil.getOrderDetail(Integer.parseInt(idParam), user.get("idDepartamento"), user.get("rol"));
                        } else {
                            jsonResponse = JsonUtil.errorJson("Falta el parámetro id");
                        }
                    }
                    break;

                case "/ordenes/upload-invoice":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleFileUpload(request);
                    break;

                case "/ordenes/years":
                    if (!isAuth) returnAuthError(response);
                    else {
                        Orders yUtil = new Orders();
                        List<Integer> yearsList = yUtil.getYearsWithOrders();
                        jsonResponse = "{\"status\":\"success\",\"years\":" + yearsList.toString() + "}";
                    }
                    break;
                
                case "/ordenes/update-observations":
                    if (!isAuth) returnAuthError(response);
                    else {
                        @SuppressWarnings("unchecked")
                        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
                        String rol = user.get("rol");
                        if (!"Admin".equals(rol) && !"Administrador".equals(rol) && !"Contable".equals(rol)) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo el administrador o el contable pueden añadir observaciones");
                            return;
                        }
                        String body = JsonUtil.getRequestBody(request);
                        String idStr = JsonUtil.findJsonField(body, "id");
                        String desc = JsonUtil.findJsonField(body, "descripcion");
                        if (desc == null) desc = JsonUtil.findJsonField(body, "observaciones"); // fallback
                        if (idStr != null) {
                            Orders obsUtil = new Orders();
                            jsonResponse = obsUtil.updateOrderDescription(Integer.parseInt(idStr), desc);
                        } else {
                            jsonResponse = JsonUtil.errorJson("Falta el parámetro id");
                        }
                    }
                    break;

                case "/ordenes/update-status":
                    if (!isAuth) returnAuthError(response);
                    else {
                        @SuppressWarnings("unchecked")
                        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
                        String rol = user.get("rol");
                        if (!"Admin".equals(rol) && !"Administrador".equals(rol) && !"Jefe de Equipo".equals(rol)) {
                             response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para cambiar el estado");
                             return;
                        }
                        String body = JsonUtil.getRequestBody(request);
                        String idStr = JsonUtil.findJsonField(body, "id");
                        String status = JsonUtil.findJsonField(body, "estado");
                        if (idStr != null && status != null) {
                            Orders statusUtil = new Orders();
                            jsonResponse = statusUtil.updateOrderStatus(Integer.parseInt(idStr), status);
                        } else {
                            jsonResponse = JsonUtil.errorJson("Faltan parámetros id o estado");
                        }
                    }
                    break;

                case "/facturas/view":
                    if (!isAuth) returnAuthError(response);
                    else {
                        String idFactura = request.getParameter("id");
                        @SuppressWarnings("unchecked")
                        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
                        String rol = user.get("rol");
                        if (!"Admin".equals(rol) && !"Administrador".equals(rol)) {
                            String sqlCheck = "SELECT d.Nombre FROM facturas f " +
                                              "JOIN ordencompra oc ON f.idOrdenCompra = oc.idOrden " +
                                              "JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto " +
                                              "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                                              "WHERE f.idFactura = ?";
                            try (java.sql.Connection conn = com.salesianos.utils.DatabaseManager.getConnection("webapp");
                                 java.sql.PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
                                stmt.setInt(1, Integer.parseInt(idFactura));
                                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                                    if (rs.next() && !user.get("idDepartamento").equals(rs.getString("Nombre"))) {
                                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado");
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error de seguridad");
                                return;
                            }
                        }
                        handleViewFactura(request, response);
                        return;
                    }

                case "/proveedores":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleSuppliers(request, response, session);
                    break;

                case "/comentarios":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleComments(request, response, session);
                    break;

                case "/notificaciones":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleNotifications(request, response, session);
                    break;

                case "/notificaciones/read":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleMarkNotifRead(request, response, session);
                    break;

                case "/register":
                    jsonResponse = handleRegister(request, response);
                    break;

                case "/productos":
                    if (!isAuth) returnAuthError(response);
                    else jsonResponse = handleProducts(request, response);
                    break;

                case "/logout":
                    jsonResponse = handleLogout(request);
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_OK);
                    jsonResponse = JsonUtil.messageJson("API System Online");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Internal Error at " + path, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse = JsonUtil.errorJson("Pro Error: " + e.getMessage());
        }

        if (jsonResponse != null && !jsonResponse.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write(jsonResponse);
        }
    }

    private void handleViewFactura(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        String action = request.getParameter("action");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el ID de la factura");
            return;
        }

        String sql = "SELECT blobFactura FROM facturas WHERE idFactura = ?";
        try (java.sql.Connection conn = com.salesianos.utils.DatabaseManager.getConnection("webapp");
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, Integer.parseInt(idParam));
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] pdfData = rs.getBytes("blobFactura");
                    if (pdfData != null) {
                        response.setContentType("application/pdf");
                        if ("download".equals(action)) {
                            response.setHeader("Content-Disposition", "attachment; filename=\"factura_" + idParam + ".pdf\"");
                        }
                        response.setContentLength(pdfData.length);
                        response.getOutputStream().write(pdfData);
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Blob vacío");
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Factura no encontrada");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error serving PDF", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String handleFileUpload(HttpServletRequest request) {
        try {
            String idParam = request.getParameter("id");
            Part filePart = request.getPart("file");
            if (idParam != null && filePart != null) {
                long orderId = Long.parseLong(idParam);
                byte[] data = readPartBytes(filePart);
                Orders util = new Orders();
                util.addInvoice(orderId, data);
                // asfasf
                return JsonUtil.messageJson("Factura subida correctamente");
            }
            return JsonUtil.errorJson("Faltan parámetros id o archivo");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error uploading file", e);
            return JsonUtil.errorJson("Error al subir archivo: " + e.getMessage());
        }
    }

    private byte[] readPartBytes(Part part) throws IOException {
        try (InputStream is = part.getInputStream();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
            return os.toByteArray();
        }
    }

    private void returnAuthError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(JsonUtil.errorJson("Autenticación Pro requerida"));
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

    private String handlePresupuestos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String dep = JsonUtil.findJsonField(body, "nombreDepartamento");

        HttpSession session = request.getSession(false);
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        String rol = user.get("rol");
        if (!"Admin".equals(rol) && !"Administrador".equals(rol)) {
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

    private String handleOrders(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        Orders util = new Orders();
        String body = JsonUtil.getRequestBody(request);

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (body.contains("idPresupuesto")) {
                Map<String, String> data = new HashMap<>();
                data.put("idPresupuesto", JsonUtil.findJsonField(body, "idPresupuesto"));
                data.put("numero_orden", JsonUtil.findJsonField(body, "numero_orden"));
                data.put("numero_plan", JsonUtil.findJsonField(body, "numero_plan"));
                data.put("Cantidad", JsonUtil.findJsonField(body, "Cantidad"));
                data.put("Inversion", JsonUtil.findJsonField(body, "Inversion"));
                data.put("Tipo", JsonUtil.findJsonField(body, "Tipo"));
                data.put("Observaciones", JsonUtil.findJsonField(body, "Observaciones"));
                
                long orderId = util.createOrderWithId(data);
                if (orderId > 0) {
                    // Associate products if present in the JSON body
                    // Products are sent as products_ids and products_prices (comma separated)
                    String prodIds = JsonUtil.findJsonField(body, "products_ids");
                    String prodPrices = JsonUtil.findJsonField(body, "products_prices");
                    if (prodIds != null && !prodIds.isEmpty()) {
                        String[] ids = prodIds.split(",");
                        String[] prices = (prodPrices != null) ? prodPrices.split(",") : new String[0];
                        for (int i = 0; i < ids.length; i++) {
                            String price = (i < prices.length) ? prices[i] : "0";
                            util.addProductToOrder(orderId, ids[i].trim(), price.trim());
                        }
                    }
                    return "{\"status\":\"success\",\"message\":\"Orden creada\",\"orderId\":\"" + orderId + "\"}";
                } else {
                    return JsonUtil.errorJson("No se pudo crear la orden");
                }
            } else {
                String dep = JsonUtil.findJsonField(body, "nombreDepartamento");
                String yearStr = JsonUtil.findJsonField(body, "year");
                int year = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr) : java.time.LocalDate.now().getYear();
                
                @SuppressWarnings("unchecked")
                Map<String, String> userData = (Map<String, String>) session.getAttribute("user");
                String userRol = userData.get("rol");
                if (!"Admin".equals(userRol) && !"Administrador".equals(userRol) && !"Contable".equals(userRol)) {
                    dep = userData.get("idDepartamento");
                }
                
                if (dep == null) {
                    dep = (userData != null) ? userData.get("idDepartamento") : null;
                }
                List<Map<String, String>> data = (dep != null) ? util.getOrdersByDept(dep, year) : new ArrayList<>();
                return JsonUtil.listToJson(data, "orders");
            }
        }
        return JsonUtil.errorJson("Método no soportado para /ordenes");
    }

    private String handleAllOrders(HttpServletRequest request, HttpServletResponse response) {
        String yearStr = request.getParameter("year");
        int year = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr) : java.time.LocalDate.now().getYear();
        Orders util = new Orders();
        List<Map<String, String>> data = util.getAllOrders(year);
        return JsonUtil.listToJson(data, "orders");
    }

    private String handleSuppliers(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        Suppliers util = new Suppliers();
        String method = request.getMethod();
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");
        
        if ("POST".equalsIgnoreCase(method)) {
            @SuppressWarnings("unchecked")
            Map<String, String> user = (Map<String, String>) session.getAttribute("user");
            String rol = user.get("rol");
            if (!"Admin".equals(rol) && !"Administrador".equals(rol) && !"Jefe de Equipo".equals(rol)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para gestionar proveedores");
                return null;
            }

            if ("delete".equals(action) && idParam != null) {
                return util.deleteSupplier(Integer.parseInt(idParam));
            }
            
            String body = JsonUtil.getRequestBody(request);
            Map<String, String> data = new HashMap<>();
            data.put("Nombre", JsonUtil.findJsonField(body, "Nombre"));
            data.put("Telefono", JsonUtil.findJsonField(body, "Telefono"));
            data.put("Direccion", JsonUtil.findJsonField(body, "Direccion"));
            
            if ("update".equals(action) && idParam != null) {
                return util.updateSupplier(Integer.parseInt(idParam), data);
            } else if ("assignProduct".equals(action) && idParam != null) {
                String prodId = request.getParameter("productId");
                return util.assignProduct(Integer.parseInt(idParam), Integer.parseInt(prodId));
            } else if ("removeProduct".equals(action) && idParam != null) {
                String prodId = request.getParameter("productId");
                return util.removeProduct(Integer.parseInt(idParam), Integer.parseInt(prodId));
            }
            return util.createSupplier(data);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (idParam != null) {
                return util.deleteSupplier(Integer.parseInt(idParam));
            }
        }
        
        if (idParam != null) {
            String detail = request.getParameter("detail");
            if ("products".equals(detail)) {
                List<Map<String, String>> products = util.getSupplierProducts(Integer.parseInt(idParam));
                return JsonUtil.listToJson(products, "productos");
            }
        }

        List<Map<String, String>> data = util.getAllSuppliers();
        return JsonUtil.listToJson(data, "suppliers");
    }

    private String handleProducts(HttpServletRequest request, HttpServletResponse response) throws IOException {
        com.salesianos.utils.Products util = new com.salesianos.utils.Products();
        String method = request.getMethod();
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        if ("POST".equalsIgnoreCase(method)) {
            if ("delete".equals(action) && idParam != null) {
                return util.deleteProduct(Integer.parseInt(idParam));
            }
            
            String body = JsonUtil.getRequestBody(request);
            Map<String, String> data = new HashMap<>();
            data.put("nombre", JsonUtil.findJsonField(body, "nombre"));
            data.put("descripcion", JsonUtil.findJsonField(body, "descripcion"));
            data.put("idProveedor", JsonUtil.findJsonField(body, "idProveedor"));
            
            if ("update".equals(action) && idParam != null) {
                return util.updateProduct(Integer.parseInt(idParam), data);
            }
            return util.createProduct(data);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (idParam != null) {
                return util.deleteProduct(Integer.parseInt(idParam));
            }
        }
        
        List<Map<String, String>> data = util.getAllProducts();
        return JsonUtil.listToJson(data, "productos");
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

    private String handleComments(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String method = request.getMethod();
        Orders util = new Orders();
        if ("POST".equalsIgnoreCase(method)) {
            String body = JsonUtil.getRequestBody(request);
            String idOrdenStr = JsonUtil.findJsonField(body, "idOrden");
            String comentario = JsonUtil.findJsonField(body, "comentario");
            @SuppressWarnings("unchecked")
            Map<String, String> user = (Map<String, String>) session.getAttribute("user");
            int idUsuario = Integer.parseInt(user.get("idUsuario"));

            if (idOrdenStr != null && comentario != null) {
                boolean ok = util.addOrderComment(Integer.parseInt(idOrdenStr), idUsuario, comentario);
                return ok ? JsonUtil.messageJson("Comentario añadido") : JsonUtil.errorJson("Error al añadir comentario");
            }
            return JsonUtil.errorJson("Faltan parámetros");
        } else {
            String idOrdenStr = request.getParameter("idOrden");
            if (idOrdenStr != null) {
                List<Map<String, String>> list = util.getOrderComments(Integer.parseInt(idOrdenStr));
                return JsonUtil.listToJson(list, "comentarios");
            }
            return JsonUtil.errorJson("Falta idOrden");
        }
    }

    private String handleNotifications(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        Orders util = new Orders();
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        int idUsuario = Integer.parseInt(user.get("idUsuario"));
        List<Map<String, String>> list = util.getUserNotifications(idUsuario);
        return JsonUtil.listToJson(list, "notificaciones");
    }

    private String handleMarkNotifRead(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String idNotifStr = JsonUtil.findJsonField(body, "idNotificacion");
        if (idNotifStr != null) {
            Orders util = new Orders();
            boolean ok = util.markNotificationAsRead(Integer.parseInt(idNotifStr));
            return ok ? JsonUtil.messageJson("Notificación marcada como leída") : JsonUtil.errorJson("Error");
        }
        return JsonUtil.errorJson("Falta idNotificacion");
    }

    private String handleOrderUpdate(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String idStr = JsonUtil.findJsonField(body, "id");
        if (idStr == null) return JsonUtil.errorJson("Falta ID de la orden");
        
        Map<String, String> data = new HashMap<>();
        data.put("Cantidad", JsonUtil.findJsonField(body, "cantidad"));
        data.put("numero_plan", JsonUtil.findJsonField(body, "numero_plan"));
        data.put("Tipo", JsonUtil.findJsonField(body, "tipo"));
        data.put("Inversion", JsonUtil.findJsonField(body, "inversion"));
        data.put("descripcion", JsonUtil.findJsonField(body, "descripcion"));
        data.put("idPresupuesto", JsonUtil.findJsonField(body, "idPresupuesto"));

        Orders util = new Orders();
        return util.updateOrder(Integer.parseInt(idStr), data);
    }
}
