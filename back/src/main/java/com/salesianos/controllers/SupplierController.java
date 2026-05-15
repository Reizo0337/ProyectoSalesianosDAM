package com.salesianos.controllers;

import com.salesianos.models.Role;
import com.salesianos.utils.JsonUtil;
import com.salesianos.utils.Suppliers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierController {

    public String handle(HttpServletRequest request, HttpServletResponse response, String path, HttpSession session) throws IOException {
        if (!"/proveedores".equals(path)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Ruta de proveedores no encontrada");
        }
        return handleSuppliers(request, response, session);
    }

    private String handleSuppliers(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        Suppliers util = new Suppliers();
        String method = request.getMethod();
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");
        
        if ("POST".equalsIgnoreCase(method)) {
            @SuppressWarnings("unchecked")
            Map<String, String> user = (Map<String, String>) session.getAttribute("user");
            Role role = Role.fromString(user.get("rol"));
            if (!role.canManageSuppliers()) {
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
}
