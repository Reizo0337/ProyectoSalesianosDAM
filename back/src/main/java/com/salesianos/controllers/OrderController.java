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

public class OrderController {

    private final OrderService orderService = new OrderService();
    private final CommunicationService commService = new CommunicationService();

    public String handle(HttpServletRequest request, HttpServletResponse response, String path, HttpSession session) throws IOException {
        switch (path) {
            case "/orders": return handleAllOrders(request, response);
            case "/orders/detail": return handleOrderDetail(request, response, session);
            case "/orders/create": return handleCreateOrder(request, response);
            case "/orders/update": return handleUpdateOrder(request, response);
            case "/orders/delete": return handleDeleteOrder(request, response, session);
            case "/orders/status": return handleUpdateStatus(request, response);
            case "/orders/sequence": return handleGetSequence(request, response);
            case "/orders/invoice": return handleAddInvoice(request, response);
            case "/orders/comments": return handleComments(request, response, session);
            case "/notifications": return handleNotifications(request, response, session);
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return JsonUtil.errorJson("Ruta de órdenes no encontrada");
        }
    }

    private String handleAllOrders(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String yearStr = JsonUtil.findJsonField(body, "year");
        int year = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr) : java.time.LocalDate.now().getYear();
        
        List<Order> orders = orderService.getOrdersByYear(year);
        List<Map<String, String>> data = orders.stream().map(Order::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "orders");
    }

    private String handleOrderDetail(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) return JsonUtil.errorJson("Falta ID de orden");

        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        Map<String, Object> detail = orderService.getOrderDetail(Long.parseLong(idParam), user.get("nombreDepartamento"), user.get("rol"));
        
        if (detail == null) return JsonUtil.errorJson("Orden no encontrada o acceso denegado");
        return JsonUtil.objectToJson(detail);
    }

    private String handleCreateOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        Order o = new Order();
        o.setIdPresupuesto(Long.parseLong(JsonUtil.findJsonField(body, "idPresupuesto")));
        o.setNumeroOrden(JsonUtil.findJsonField(body, "numero_orden"));
        o.setNumeroPlan(JsonUtil.findJsonField(body, "numero_plan"));
        o.setCantidad(Double.parseDouble(JsonUtil.findJsonField(body, "Cantidad")));
        o.setInversion(Boolean.parseBoolean(JsonUtil.findJsonField(body, "Inversion")));
        o.setTipo(JsonUtil.findJsonField(body, "Tipo"));
        o.setDescripcion(JsonUtil.findJsonField(body, "descripcion"));

        long id = orderService.createOrder(o, null); 
        if (id == -2) return JsonUtil.errorJson("Presupuesto insuficiente");
        return id > 0 ? JsonUtil.messageJson("Orden creada") : JsonUtil.errorJson("Error al crear");
    }

    private String handleUpdateOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = JsonUtil.getRequestBody(request);
        String idParam = request.getParameter("id");
        Order o = new Order();
        o.setIdOrden(Long.parseLong(idParam));
        o.setCantidad(Double.parseDouble(JsonUtil.findJsonField(body, "Cantidad")));
        o.setNumeroPlan(JsonUtil.findJsonField(body, "numero_plan"));
        o.setTipo(JsonUtil.findJsonField(body, "Tipo"));
        o.setInversion(Boolean.parseBoolean(JsonUtil.findJsonField(body, "Inversion")));
        o.setDescripcion(JsonUtil.findJsonField(body, "descripcion"));
        o.setIdPresupuesto(Long.parseLong(JsonUtil.findJsonField(body, "idPresupuesto")));

        boolean ok = orderService.updateOrder(o);
        return ok ? JsonUtil.messageJson("Orden actualizada") : JsonUtil.errorJson("No se pudo actualizar (orden cerrada o error)");
    }

    private String handleDeleteOrder(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String idParam = request.getParameter("id");
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        
        // Permission check
        Role role = Role.fromString(user.get("rol"));
        if (!role.canChangeStatus() && !role.hasGlobalAccess()) {
             return JsonUtil.errorJson("No tienes permiso para borrar órdenes");
        }

        boolean ok = orderService.deleteOrder(Long.parseLong(idParam), user.get("nombreDepartamento"), user.get("rol"));
        return ok ? JsonUtil.messageJson("Orden eliminada") : JsonUtil.errorJson("No se pudo eliminar (no autoriz./cerrada)");
    }

    private String handleUpdateStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        String status = request.getParameter("status");
        boolean ok = orderService.updateStatus(Long.parseLong(idParam), status);
        return ok ? JsonUtil.messageJson("Estado actualizado") : JsonUtil.errorJson("No se pudo cambiar el estado");
    }

    private String handleGetSequence(HttpServletRequest request, HttpServletResponse response) {
        String dept = request.getParameter("dept");
        String year = request.getParameter("year");
        String seq = orderService.getNextSequence(dept, year);
        return "{\"status\":\"success\",\"sequence\":\"" + seq + "\"}";
    }

    private String handleAddInvoice(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        byte[] data = request.getInputStream().readAllBytes();
        boolean ok = orderService.addInvoice(Long.parseLong(idParam), data);
        return ok ? JsonUtil.messageJson("Factura añadida") : JsonUtil.errorJson("Error al añadir factura");
    }

    private String handleComments(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        String idParam = request.getParameter("id");
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String body = JsonUtil.getRequestBody(request);
            @SuppressWarnings("unchecked")
            Map<String, String> user = (Map<String, String>) session.getAttribute("user");
            boolean ok = commService.addComment(Long.parseLong(idParam), Long.parseLong(user.get("IdUsuario")), JsonUtil.findJsonField(body, "comentario"));
            return ok ? JsonUtil.messageJson("Comentario añadido") : JsonUtil.errorJson("Error");
        }
        List<Comment> comments = commService.getCommentsByOrder(Long.parseLong(idParam));
        List<Map<String, String>> data = comments.stream().map(Comment::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "comments");
    }

    private String handleNotifications(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, String> user = (Map<String, String>) session.getAttribute("user");
        long userId = Long.parseLong(user.get("IdUsuario"));
        
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String idParam = request.getParameter("id");
            boolean ok = commService.markNotificationRead(Long.parseLong(idParam));
            return ok ? JsonUtil.messageJson("Leída") : JsonUtil.errorJson("Error");
        }
        
        List<Notification> notifs = commService.getNotificationsByUser(userId);
        List<Map<String, String>> data = notifs.stream().map(Notification::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "notifications");
    }
}
