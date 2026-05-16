package com.salesianos.controllers;

import com.salesianos.models.Product;
import com.salesianos.models.Role;
import com.salesianos.models.Supplier;
import com.salesianos.services.SupplierService;
import com.salesianos.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SupplierController {

    private final SupplierService supplierService = new SupplierService();

    public String handle(HttpServletRequest request, HttpServletResponse response, String path, HttpSession session) throws IOException {
        if (!"/proveedores".equals(path)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Ruta de proveedores no encontrada");
        }
        return handleSuppliers(request, response, session);
    }

    private String handleSuppliers(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
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
                boolean ok = supplierService.deleteSupplier(Long.parseLong(idParam));
                return ok ? JsonUtil.messageJson("Proveedor eliminado") : JsonUtil.errorJson("Error al eliminar proveedor");
            }
            
            String body = JsonUtil.getRequestBody(request);
            Supplier s = new Supplier();
            s.setNombre(JsonUtil.findJsonField(body, "Nombre"));
            s.setTelefono(JsonUtil.findJsonField(body, "Telefono"));
            s.setDireccion(JsonUtil.findJsonField(body, "Direccion"));
            
            if ("update".equals(action) && idParam != null) {
                s.setIdProveedor(Long.parseLong(idParam));
                boolean ok = supplierService.updateSupplier(s);
                return ok ? JsonUtil.messageJson("Proveedor actualizado") : JsonUtil.errorJson("Error al actualizar proveedor");
            } else if ("assignProduct".equals(action) && idParam != null) {
                String prodId = request.getParameter("productId");
                boolean ok = supplierService.assignProduct(Long.parseLong(idParam), Long.parseLong(prodId));
                return ok ? JsonUtil.messageJson("Producto asignado") : JsonUtil.errorJson("Error al asignar producto");
            } else if ("removeProduct".equals(action) && idParam != null) {
                String prodId = request.getParameter("productId");
                boolean ok = supplierService.removeProduct(Long.parseLong(idParam), Long.parseLong(prodId));
                return ok ? JsonUtil.messageJson("Producto desvinculado") : JsonUtil.errorJson("Error al desvincular producto");
            }
            
            long newId = supplierService.createSupplier(s);
            return newId > 0 ? JsonUtil.messageJson("Proveedor creado") : JsonUtil.errorJson("Error al crear proveedor");
            
        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (idParam != null) {
                boolean ok = supplierService.deleteSupplier(Long.parseLong(idParam));
                return ok ? JsonUtil.messageJson("Proveedor eliminado") : JsonUtil.errorJson("Error al eliminar proveedor");
            }
        }
        
        if (idParam != null) {
            String detail = request.getParameter("detail");
            if ("products".equals(detail)) {
                List<Product> products = supplierService.getProducts(Long.parseLong(idParam));
                List<Map<String, String>> data = products.stream().map(Product::toMap).collect(Collectors.toList());
                return JsonUtil.listToJson(data, "productos");
            }
        }

        List<Supplier> suppliers = supplierService.getAllSuppliers();
        List<Map<String, String>> data = suppliers.stream().map(Supplier::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "suppliers");
    }
}
