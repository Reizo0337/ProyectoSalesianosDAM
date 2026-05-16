package com.salesianos.controllers;

import com.salesianos.models.Product;
import com.salesianos.services.ProductService;
import com.salesianos.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductController {

    private final ProductService productService = new ProductService();

    public String handle(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        if (!"/productos".equals(path)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Ruta de productos no encontrada");
        }
        return handleProducts(request, response);
    }

    private String handleProducts(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String method = request.getMethod();
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        if ("POST".equalsIgnoreCase(method)) {
            if ("delete".equals(action) && idParam != null) {
                boolean ok = productService.deleteProduct(Long.parseLong(idParam));
                return ok ? JsonUtil.messageJson("Producto eliminado") : JsonUtil.errorJson("Error al eliminar producto");
            }
            
            String body = JsonUtil.getRequestBody(request);
            Product p = new Product();
            p.setNombre(JsonUtil.findJsonField(body, "nombre"));
            p.setDescripcion(JsonUtil.findJsonField(body, "descripcion"));
            String idProv = JsonUtil.findJsonField(body, "idProveedor");
            if (idProv != null && !idProv.isEmpty()) p.setIdProveedor(Long.parseLong(idProv));
            
            if ("update".equals(action) && idParam != null) {
                p.setIdProducto(Long.parseLong(idParam));
                boolean ok = productService.updateProduct(p);
                return ok ? JsonUtil.messageJson("Producto actualizado") : JsonUtil.errorJson("Error al actualizar producto");
            }
            
            long newId = productService.createProduct(p);
            return newId > 0 ? JsonUtil.messageJson("Producto creado") : JsonUtil.errorJson("Error al crear producto");
        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (idParam != null) {
                boolean ok = productService.deleteProduct(Long.parseLong(idParam));
                return ok ? JsonUtil.messageJson("Producto eliminado") : JsonUtil.errorJson("Error al eliminar producto");
            }
        }
        
        List<Product> products = productService.getAllProducts();
        List<Map<String, String>> data = products.stream().map(Product::toMap).collect(Collectors.toList());
        return JsonUtil.listToJson(data, "productos");
    }
}
