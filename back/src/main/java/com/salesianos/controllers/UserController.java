package com.salesianos.controllers;

import com.salesianos.models.User;
import com.salesianos.repositories.UserRepository;
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

public class UserController {
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());
    private final UserRepository userRepository = new UserRepository();

    public String handle(HttpServletRequest request, HttpServletResponse response, String path, HttpSession session) throws IOException {
        try {
            // Solo administradores pueden gestionar usuarios
            @SuppressWarnings("unchecked")
            Map<String, String> userSession = (Map<String, String>) session.getAttribute("user");
            if (userSession == null || !"Administrador".equals(userSession.get("rol"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return JsonUtil.errorJson("No tienes permisos para gestionar usuarios");
            }

            String method = request.getMethod();

            // Normalizar path eliminando barra final si existe
            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }

            if (path.equals("/usuarios") && method.equals("GET")) {
                return handleListUsers();
            } else if (path.equals("/usuarios/roles") && method.equals("GET")) {
                return JsonUtil.objectToJson(userRepository.findAllRoles());
            } else if (path.equals("/usuarios/departamentos") && method.equals("GET")) {
                return JsonUtil.objectToJson(userRepository.findAllDepartments());
            } else if (path.startsWith("/usuarios/verify/") && method.equals("POST")) {
                return handleVerifyUser(request, path);
            } else if (path.startsWith("/usuarios/update/") && method.equals("POST")) {
                return handleUpdateUser(request, path);
            } else if (path.startsWith("/usuarios/password/") && method.equals("POST")) {
                return handleUpdatePassword(request, path);
            } else if (path.startsWith("/usuarios/delete/") && method.equals("DELETE")) {
                return handleDeleteUser(path);
            }

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return JsonUtil.errorJson("Ruta de usuarios no encontrada: " + path);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in UserController at " + path, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return JsonUtil.errorJson("Error interno: " + e.getMessage());
        }
    }

    private String handleListUsers() {
        List<User> users = userRepository.findAllUsers();
        List<Map<String, String>> usersMap = users.stream().map(User::toMap).collect(Collectors.toList());
        return JsonUtil.objectToJson(usersMap);
    }

    private String handleVerifyUser(HttpServletRequest request, String path) throws IOException {
        String[] parts = path.split("/");
        long id = Long.parseLong(parts[parts.length - 1]);
        String body = JsonUtil.getRequestBody(request);
        long idRol = Long.parseLong(JsonUtil.findJsonField(body, "idRol"));
        String deptIdStr = JsonUtil.findJsonField(body, "idDepartamento");
        
        Long idDepartamento = null;
        if (deptIdStr != null && !deptIdStr.isEmpty() && !deptIdStr.equals("null")) {
            idDepartamento = Long.parseLong(deptIdStr);
        }

        if (userRepository.verifyUser(id, idRol, idDepartamento)) {
            return JsonUtil.messageJson("Usuario verificado con éxito");
        }
        return JsonUtil.errorJson("Error al verificar usuario");
    }

    private String handleUpdateUser(HttpServletRequest request, String path) throws IOException {
        String[] parts = path.split("/");
        long id = Long.parseLong(parts[parts.length - 1]);
        String body = JsonUtil.getRequestBody(request);
        String nombre = JsonUtil.findJsonField(body, "nombre");
        String apellidos = JsonUtil.findJsonField(body, "apellidos");
        String correo = JsonUtil.findJsonField(body, "correo");
        String rolIdStr = JsonUtil.findJsonField(body, "idRol");
        String deptIdStr = JsonUtil.findJsonField(body, "idDepartamento");

        Long idRol = (rolIdStr != null && !rolIdStr.isEmpty()) ? Long.parseLong(rolIdStr) : null;
        Long idDepartamento = (deptIdStr != null && !deptIdStr.isEmpty()) ? Long.parseLong(deptIdStr) : null;

        if (userRepository.updateUser(id, nombre, apellidos, correo, idRol, idDepartamento)) {
            return JsonUtil.messageJson("Usuario actualizado con éxito");
        }
        return JsonUtil.errorJson("Error al actualizar usuario");
    }

    private String handleUpdatePassword(HttpServletRequest request, String path) throws IOException {
        String[] parts = path.split("/");
        long id = Long.parseLong(parts[parts.length - 1]);
        String body = JsonUtil.getRequestBody(request);
        String newPassword = JsonUtil.findJsonField(body, "newPassword");

        if (userRepository.updatePassword(id, newPassword)) {
            return JsonUtil.messageJson("Contraseña actualizada con éxito");
        }
        return JsonUtil.errorJson("Error al actualizar contraseña");
    }

    private String handleDeleteUser(String path) {
        String[] parts = path.split("/");
        long id = Long.parseLong(parts[parts.length - 1]);
        if (userRepository.deleteUser(id)) {
            return JsonUtil.messageJson("Usuario eliminado con éxito");
        }
        return JsonUtil.errorJson("Error al eliminar usuario");
    }
}
