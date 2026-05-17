package com.salesianos.controllers;

import com.salesianos.models.Comment;
import com.salesianos.models.Notification;
import com.salesianos.models.Order;
import com.salesianos.models.Role;
import com.salesianos.services.CommunicationService;
import com.salesianos.services.OrderService;
import com.salesianos.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderController {
    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());

    private final OrderService orderService = new OrderService();
    private final CommunicationService commService = new CommunicationService();

    public String handle(HttpServletRequest request, HttpServletResponse response, String path, HttpSession session) throws IOException {
        if (path.startsWith("/facturas/view")) return handleViewInvoice(request, response);
        
        // IMPORTANTE: Las rutas más específicas van ANTES que las genéricas
        // "/ordenes/update-status" DEBE ir antes de "/ordenes/update"
        if (path.startsWith("/ordenes/all")) return handleAllOrders(request, response);
        if (path.startsWith("/ordenes/detail")) return handleOrderDetail(request, response, session);
        if (path.startsWith("/ordenes/create")) {
            String body = JsonUtil.getRequestBody(request);
            return handleCreateOrderFromBody(request, response, body);
        }
        if (path.startsWith("/ordenes/update-status")) return handleUpdateStatus(request, response);
        if (path.startsWith("/ordenes/upload-invoice")) return handleAddInvoice(request, response);
        if (path.startsWith("/ordenes/update")) return handleUpdateOrder(request, response);
        if (path.startsWith("/ordenes/delete")) return handleDeleteOrder(request, response, session);
        if (path.startsWith("/ordenes/next-number")) return handleGetSequence(request, response);
        if (path.startsWith("/ordenes/years")) return handleGetYears(response);
        if (path.startsWith("/comentarios")) return handleComments(request, response, session);
        if (path.startsWith("/ordenes/usuarios")) return handleUsers(request, response);
        if (path.startsWith("/notificaciones")) return handleNotifications(request, response, session);
        
        // Ruta base /ordenes: POST con idPresupuesto = crear, otro POST = listar filtrado, GET = listar
        if (path.equals("/ordenes")) {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                String body = JsonUtil.getRequestBody(request);
                if (body != null && body.contains("idPresupuesto")) {
                    return handleCreateOrderFromBody(request, response, body);
                }
                return handleAllOrdersFromBody(request, response, body);
            }
            return handleAllOrders(request, response);
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return JsonUtil.errorJson("Ruta de órdenes no encontrada: " + path);
    }

    // GET /ordenes/all o GET /ordenes
    private String handleAllOrders(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String yearStr = request.getParameter("year");
        int year = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr) : java.time.LocalDate.now().getYear();
        
        List<Order> orders = orderService.getOrdersByYear(year);
        List<Map<String, String>> data = orders.stream().map(Order::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "orders");
    }

    // POST /ordenes con body de filtrado (nombreDepartamento, year)
    private String handleAllOrdersFromBody(HttpServletRequest request, HttpServletResponse response, String body) throws IOException {
        String yearStr = JsonUtil.findJsonField(body, "year");
        String dept = JsonUtil.findJsonField(body, "nombreDepartamento");
        
        if (yearStr == null) yearStr = request.getParameter("year");
        int year = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr) : java.time.LocalDate.now().getYear();
        
        LOGGER.log(Level.INFO, "Fetching orders - Year: {0}, Dept: {1}", new Object[]{year, dept});

        boolean isGlobal = dept == null || dept.isEmpty() || 
                           "Resumen Global".equalsIgnoreCase(dept) || 
                           "Admin".equalsIgnoreCase(dept);
        
        List<Order> orders = !isGlobal
            ? orderService.getOrdersByYear(year).stream()
                .filter(o -> dept.equalsIgnoreCase(o.getNombreDepartamento()))
                .collect(Collectors.toList())
            : orderService.getOrdersByYear(year);

        LOGGER.log(Level.INFO, "Found {0} orders", orders.size());

        List<Map<String, String>> data = orders.stream().map(Order::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "orders");
    }

    // POST /ordenes con body de creación (tiene idPresupuesto)
    private String handleCreateOrderFromBody(HttpServletRequest request, HttpServletResponse response, String body) throws IOException {
        Order o = new Order();
        
        String idPre = JsonUtil.findJsonField(body, "idPresupuesto");
        if (idPre == null || idPre.isEmpty()) return JsonUtil.errorJson("Falta ID de presupuesto");
        
        o.setIdPresupuesto(Long.parseLong(idPre));
        o.setNumeroOrden(JsonUtil.findJsonField(body, "numero_orden"));
        o.setNumeroPlan(JsonUtil.findJsonField(body, "numero_plan"));
        
        String cant = JsonUtil.findJsonField(body, "Cantidad");
        o.setCantidad(cant != null && !cant.isEmpty() ? Double.parseDouble(cant) : 0.0);
        
        o.setInversion(Boolean.parseBoolean(JsonUtil.findJsonField(body, "Inversion")));
        o.setTipo(JsonUtil.findJsonField(body, "Tipo"));
        o.setDescripcion(JsonUtil.findJsonField(body, "descripcion"));

        // Parseo de productos
        java.util.List<java.util.Map<String, String>> products = new java.util.ArrayList<>();
        String ids = JsonUtil.findJsonField(body, "products_ids");
        String prices = JsonUtil.findJsonField(body, "products_prices");
        
        if (ids != null && prices != null && !ids.isEmpty()) {
            String[] idArr = ids.split(",");
            String[] priceArr = prices.split(",");
            for (int i = 0; i < idArr.length; i++) {
                if (idArr[i].trim().isEmpty()) continue;
                java.util.Map<String, String> p = new java.util.HashMap<>();
                p.put("id", idArr[i].trim());
                p.put("precio", i < priceArr.length ? priceArr[i].trim() : "0");
                products.add(p);
            }
        }

        LOGGER.log(Level.INFO, "Creating order - Budget: {0}, Amount: {1}, Products: {2}", new Object[]{idPre, cant, products.size()});

        long id = orderService.createOrder(o, products.isEmpty() ? null : products); 
        if (id == -2) return JsonUtil.errorJson("Presupuesto insuficiente");
        if (id > 0) {
            java.util.Map<String, String> res = new java.util.HashMap<>();
            res.put("status", "success");
            res.put("message", "Orden creada");
            res.put("orderId", String.valueOf(id));
            return JsonUtil.mapToJson(res);
        }
        return JsonUtil.errorJson("Error al crear la orden en base de datos");
    }

    private String handleOrderDetail(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) return JsonUtil.errorJson("Falta ID de orden");

        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        Map<String, Object> detail = orderService.getOrderDetail(Long.parseLong(idParam), user.get("nombreDepartamento"), user.get("rol"));
        
        if (detail == null) return JsonUtil.errorJson("Orden no encontrada o acceso denegado");
        return JsonUtil.successJson(detail);
    }

    // BUG FIX: El frontend envía el id en el body JSON, no como query param
    private String handleUpdateOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String idParam = JsonUtil.findJsonField(body, "id");
        if (idParam == null) idParam = request.getParameter("id");
        if (idParam == null) return JsonUtil.errorJson("Falta ID de orden para actualizar");

        Order o = new Order();
        o.setIdOrden(Long.parseLong(idParam));
        
        String cant = JsonUtil.findJsonField(body, "cantidad");
        if (cant == null) cant = JsonUtil.findJsonField(body, "Cantidad");
        o.setCantidad(cant != null ? Double.parseDouble(cant) : 0.0);
        
        o.setNumeroPlan(JsonUtil.findJsonField(body, "numero_plan"));
        
        String tipo = JsonUtil.findJsonField(body, "tipo");
        if (tipo == null) tipo = JsonUtil.findJsonField(body, "Tipo");
        o.setTipo(tipo);
        
        String inv = JsonUtil.findJsonField(body, "inversion");
        if (inv == null) inv = JsonUtil.findJsonField(body, "Inversion");
        o.setInversion(Boolean.parseBoolean(inv));
        
        o.setDescripcion(JsonUtil.findJsonField(body, "descripcion"));
        
        String idPre = JsonUtil.findJsonField(body, "idPresupuesto");
        if (idPre != null && !idPre.isEmpty()) {
            o.setIdPresupuesto(Long.parseLong(idPre));
        }

        boolean ok = orderService.updateOrder(o);
        return ok ? JsonUtil.messageJson("Orden actualizada") : JsonUtil.errorJson("No se pudo actualizar (orden cerrada o error)");
    }

    // BUG FIX: El frontend envía el id en el body JSON, no como query param
    private String handleDeleteOrder(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String idParam = JsonUtil.findJsonField(body, "id");
        if (idParam == null) idParam = request.getParameter("id");
        if (idParam == null) return JsonUtil.errorJson("Falta ID de orden para eliminar");

        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        
        Role role = Role.fromString(user.get("rol"));
        if (!role.canChangeStatus() && !role.hasGlobalAccess()) {
             return JsonUtil.errorJson("No tienes permiso para borrar órdenes");
        }

        boolean ok = orderService.deleteOrder(Long.parseLong(idParam), user.get("nombreDepartamento"), user.get("rol"));
        return ok ? JsonUtil.messageJson("Orden eliminada") : JsonUtil.errorJson("No se pudo eliminar (no autoriz./cerrada)");
    }

    private String handleUpdateStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String idParam = JsonUtil.findJsonField(body, "id");
        if (idParam == null) idParam = request.getParameter("id");
        String status = JsonUtil.findJsonField(body, "estado");
        if (status == null) status = request.getParameter("status");

        if (idParam == null) return JsonUtil.errorJson("Falta ID de orden");
        if (status == null) return JsonUtil.errorJson("Falta el nuevo estado");

        boolean ok = orderService.updateStatus(Long.parseLong(idParam), status);
        return ok ? JsonUtil.messageJson("Estado actualizado") : JsonUtil.errorJson("No se pudo cambiar el estado");
    }

    private String handleGetSequence(HttpServletRequest request, HttpServletResponse response) {
        String dept = request.getParameter("dept");
        String year = request.getParameter("year");
        String seq = orderService.getNextSequence(dept, year);
        return "{\"status\":\"success\",\"nextSequence\":\"" + seq + "\"}";
    }

    private String handleGetYears(HttpServletResponse response) {
        List<Integer> years = orderService.getYears();
        return "{\"status\":\"success\",\"years\":" + years.toString() + "}";
    }

    private String handleAddInvoice(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) return JsonUtil.errorJson("Falta ID de orden para la factura");
        
        try {
            jakarta.servlet.http.Part filePart = request.getPart("file");
            if (filePart == null) return JsonUtil.errorJson("No se recibió el archivo");
            byte[] data = filePart.getInputStream().readAllBytes();
            boolean ok = orderService.addInvoice(Long.parseLong(idParam), data);
            return ok ? JsonUtil.messageJson("Factura añadida") : JsonUtil.errorJson("Error al añadir factura");
        } catch (jakarta.servlet.ServletException e) {
            // Fallback si no es multipart
            byte[] data = request.getInputStream().readAllBytes();
            boolean ok = orderService.addInvoice(Long.parseLong(idParam), data);
            return ok ? JsonUtil.messageJson("Factura añadida") : JsonUtil.errorJson("Error al añadir factura");
        }
    }

    private String handleComments(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String idParam = request.getParameter("idOrden");
        if (idParam == null) idParam = request.getParameter("id");

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String body = JsonUtil.getRequestBody(request);
            if (idParam == null) idParam = JsonUtil.findJsonField(body, "idOrden");
            @SuppressWarnings("unchecked")
            Map<String, String> user = (Map<String, String>) session.getAttribute("user");
            boolean ok = commService.addComment(Long.parseLong(idParam), Long.parseLong(user.get("IdUsuario")), JsonUtil.findJsonField(body, "comentario"));
            return ok ? JsonUtil.messageJson("Comentario añadido") : JsonUtil.errorJson("Error");
        }
        
        if (idParam == null) return JsonUtil.errorJson("Falta idOrden");
        List<Comment> comments = commService.getCommentsByOrder(Long.parseLong(idParam));
        List<Map<String, String>> data = comments.stream().map(Comment::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "comentarios");
    }

    private String handleNotifications(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        long userId = Long.parseLong(user.get("IdUsuario"));
        
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String body = JsonUtil.getRequestBody(request);
            String idParam = JsonUtil.findJsonField(body, "idNotificacion");
            if (idParam == null) idParam = request.getParameter("id");
            boolean ok = commService.markNotificationRead(Long.parseLong(idParam));
            return ok ? JsonUtil.messageJson("Leída") : JsonUtil.errorJson("Error");
        }
        
        List<Notification> notifs = commService.getNotificationsByUser(userId);
        List<Map<String, String>> data = notifs.stream().map(Notification::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "notificaciones");
    }

    private String handleUsers(HttpServletRequest request, HttpServletResponse response) {
        String orderIdParam = request.getParameter("idOrden");
        List<Map<String, String>> users;
        if (orderIdParam != null && !orderIdParam.isEmpty()) {
            users = commService.getRelevantUserNames(Long.parseLong(orderIdParam));
        } else {
            users = commService.getAllUserNames();
        }
        return JsonUtil.listToJson(users, "usuarios");
    }

    private String handleViewInvoice(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Falta ID de factura");
        }

        try {
            long idFactura = Long.parseLong(idParam);
            byte[] pdfBytes = orderService.getInvoiceBlob(idFactura);

            if (pdfBytes == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return JsonUtil.errorJson("Factura no encontrada");
            }

            response.setContentType("application/pdf");
            response.setContentLength(pdfBytes.length);

            String action = request.getParameter("action");
            if ("download".equalsIgnoreCase(action)) {
                response.setHeader("Content-Disposition", "attachment; filename=\"factura_" + idFactura + ".pdf\"");
            } else {
                response.setHeader("Content-Disposition", "inline; filename=\"factura_" + idFactura + ".pdf\"");
            }

            // Escribir los bytes directamente en el output stream de la respuesta
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

            // Retornamos null para que ApiServlet sepa que ya hemos escrito en el output stream y no intente devolver un JSON
            return null;
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("ID de factura inválido");
        }
    }
}
