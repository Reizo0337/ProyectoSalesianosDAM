package com.salesianos.controllers;

import com.salesianos.models.Role;
import com.salesianos.utils.JsonUtil;
import com.salesianos.utils.Orders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderController {

    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());

    public String handle(HttpServletRequest request, HttpServletResponse response, String path, HttpSession session) throws IOException {
        switch (path) {
            case "/ordenes":
                return handleOrders(request, response, session);
            case "/ordenes/all":
                return handleAllOrders(request, response);
            case "/ordenes/update":
                return handleOrderUpdate(request, response, session);
            case "/ordenes/next-number":
                return handleNextNumber(request, response);
            case "/ordenes/detail":
                return handleOrderDetail(request, response, session);
            case "/ordenes/upload-invoice":
                return handleFileUpload(request);
            case "/ordenes/years":
                return handleYears(request, response);
            case "/ordenes/update-observations":
                return handleUpdateObservations(request, response, session);
            case "/ordenes/update-status":
                return handleUpdateStatus(request, response, session);
            case "/facturas/view":
                handleFacturaView(request, response, session);
                return null;
            case "/comentarios":
                return handleComentarios(request, response, session);
            case "/notificaciones":
                return handleNotificaciones(request, response, session);
            case "/notificaciones/read":
                return handleNotificacionesRead(request, response, session);
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return JsonUtil.errorJson("Ruta de órdenes no encontrada");
        }
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
                Role userRole = Role.fromString(userData.get("rol"));
                if (!userRole.hasGlobalAccess()) {
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

    private String handleNextNumber(HttpServletRequest request, HttpServletResponse response) {
        String dept = request.getParameter("dept");
        String year = request.getParameter("year");
        Orders oUtil = new Orders();
        String nextSeq = oUtil.getNextOrderSequence(dept, year);
        return "{\"status\":\"success\",\"nextSequence\":\"" + nextSeq + "\"}";
    }

    private String handleOrderDetail(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String idParam = request.getParameter("id");
        if (idParam != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> user = (Map<String, String>) session.getAttribute("user");
            Orders detailUtil = new Orders();
            return detailUtil.getOrderDetail(Integer.parseInt(idParam), user.get("idDepartamento"), user.get("rol"));
        } else {
            return JsonUtil.errorJson("Falta el parámetro id");
        }
    }

    private String handleYears(HttpServletRequest request, HttpServletResponse response) {
        Orders yUtil = new Orders();
        List<Integer> yearsList = yUtil.getYearsWithOrders();
        return "{\"status\":\"success\",\"years\":" + yearsList.toString() + "}";
    }

    private String handleUpdateObservations(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        Role role = Role.fromString(user.get("rol"));
        if (!role.canComment()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para añadir observaciones");
            return null;
        }
        String body = JsonUtil.getRequestBody(request);
        String idStr = JsonUtil.findJsonField(body, "id");
        String desc = JsonUtil.findJsonField(body, "descripcion");
        if (desc == null) desc = JsonUtil.findJsonField(body, "observaciones"); // fallback
        if (idStr != null) {
            Orders obsUtil = new Orders();
            return obsUtil.updateOrderDescription(Integer.parseInt(idStr), desc);
        } else {
            return JsonUtil.errorJson("Falta el parámetro id");
        }
    }

    private String handleUpdateStatus(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        Role role = Role.fromString(user.get("rol"));
        if (!role.canChangeStatus()) {
             response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para cambiar el estado");
             return null;
        }
        String body = JsonUtil.getRequestBody(request);
        String idStr = JsonUtil.findJsonField(body, "id");
        String status = JsonUtil.findJsonField(body, "estado");
        if (idStr != null && status != null) {
            Orders statusUtil = new Orders();
            return statusUtil.updateOrderStatus(Integer.parseInt(idStr), status);
        } else {
            return JsonUtil.errorJson("Faltan parámetros id o estado");
        }
    }

    private void handleFacturaView(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String idFactura = request.getParameter("id");
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        Role role = Role.fromString(user.get("rol"));
        if (!role.canManageInvoices()) {
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
                boolean success = util.addInvoice(orderId, data);
                if (success) {
                    return JsonUtil.messageJson("Factura subida correctamente");
                } else {
                    return JsonUtil.errorJson("No se puede subir factura: la orden ya está cerrada.");
                }
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

    private String handleComentarios(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
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

    private String handleNotificaciones(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        Orders util = new Orders();
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        int idUsuario = Integer.parseInt(user.get("idUsuario"));
        List<Map<String, String>> list = util.getUserNotifications(idUsuario);
        return JsonUtil.listToJson(list, "notificaciones");
    }

    private String handleNotificacionesRead(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String idNotifStr = JsonUtil.findJsonField(body, "idNotificacion");
        if (idNotifStr != null) {
            Orders util = new Orders();
            boolean ok = util.markNotificationAsRead(Integer.parseInt(idNotifStr));
            return ok ? JsonUtil.messageJson("Notificación marcada como leída") : JsonUtil.errorJson("Error");
        }
        return JsonUtil.errorJson("Falta idNotificacion");
    }
}
