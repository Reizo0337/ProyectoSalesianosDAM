package com.salesianos.models;

public class Product {
    private long idProducto;
    private String nombre;
    private String descripcion;
    private long idProveedor;
    private double precioMedio;
    private String nombreProveedor;

    public Product() {}

    // Getters y Setters
    public long getIdProducto() { return idProducto; }
    public void setIdProducto(long idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public long getIdProveedor() { return idProveedor; }
    public void setIdProveedor(long idProveedor) { this.idProveedor = idProveedor; }

    public double getPrecioMedio() { return precioMedio; }
    public void setPrecioMedio(double precioMedio) { this.precioMedio = precioMedio; }

    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String nombreProveedor) { this.nombreProveedor = nombreProveedor; }

    public java.util.Map<String, String> toMap() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("idproducto", String.valueOf(idProducto));
        map.put("nombre", nombre == null ? "" : nombre);
        map.put("descripcion", descripcion == null ? "" : descripcion);
        map.put("idproveedor", String.valueOf(idProveedor));
        map.put("proveedor", nombreProveedor == null ? "No asignado" : nombreProveedor);
        map.put("precio_medio", String.valueOf(precioMedio));
        return map;
    }
}
