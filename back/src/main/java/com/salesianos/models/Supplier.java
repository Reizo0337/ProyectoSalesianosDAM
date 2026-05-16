package com.salesianos.models;

public class Supplier {
    private long idProveedor;
    private String nombre;
    private String telefono;
    private String direccion;

    public Supplier() {}

    // Getters y Setters
    public long getIdProveedor() { return idProveedor; }
    public void setIdProveedor(long idProveedor) { this.idProveedor = idProveedor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public java.util.Map<String, String> toMap() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("idproveedor", String.valueOf(idProveedor));
        map.put("nombre", nombre == null ? "" : nombre);
        map.put("telefono", telefono == null ? "" : telefono);
        map.put("direccion", direccion == null ? "" : direccion);
        return map;
    }
}
