package com.salesianos.controllers;

import com.salesianos.utils.JsonUtil;
import com.salesianos.utils.Products;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductController {

    public String handle(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        if (!"/productos".equals(path)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Ruta de productos no encontrada");
        }
        return handleProducts(request, response);
    }

    private String handleProducts(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Products util = new Products();
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
}
