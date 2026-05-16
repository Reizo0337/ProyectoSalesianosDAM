package com.salesianos.models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private long idUsuario;
    private String nombre;
    private String apellidos;
    private String correo;
    private String rol;
    private String nombreDepartamento;
    private String codigoDepartamento;
    private boolean isVerified;
    private long idDepartamento;

    public User() {}

    public long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getNombreDepartamento() { return nombreDepartamento; }
    public void setNombreDepartamento(String nombreDepartamento) { this.nombreDepartamento = nombreDepartamento; }

    public String getCodigoDepartamento() { return codigoDepartamento; }
    public void setCodigoDepartamento(String codigoDepartamento) { this.codigoDepartamento = codigoDepartamento; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public long getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(long idDepartamento) { this.idDepartamento = idDepartamento; }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("IdUsuario", String.valueOf(idUsuario));
        map.put("nombre", nombre);
        map.put("correo", correo);
        map.put("rol", rol);
        map.put("nombreDepartamento", nombreDepartamento);
        map.put("idDepartamento", nombreDepartamento); // Compatibility
        map.put("codigoDepartamento", codigoDepartamento);
        map.put("isVerified", String.valueOf(isVerified));
        map.put("idDepartamento", String.valueOf(idDepartamento));
        return map;
    }
}
