package com.salesianos.models;

import java.util.Date;

public class Order {
    private long idOrden;
    private String numeroOrden;
    private String numeroPlan;
    private double cantidad;
    private String tipo; // Ordinario, Inversion
    private String estado; // Pendiente, Aprobado, Rechazado, Cerrada
    private Date fechaCreacion;
    private String descripcion;
    private boolean inversion;
    private long idPresupuesto;
    private String nombreDepartamento; // Campo auxiliar para la vista

    public Order() {}

    // Getters y Setters
    public long getIdOrden() { return idOrden; }
    public void setIdOrden(long idOrden) { this.idOrden = idOrden; }

    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }

    public String getNumeroPlan() { return numeroPlan; }
    public void setNumeroPlan(String numeroPlan) { this.numeroPlan = numeroPlan; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isInversion() { return inversion; }
    public void setInversion(boolean inversion) { this.inversion = inversion; }

    public long getIdPresupuesto() { return idPresupuesto; }
    public void setIdPresupuesto(long idPresupuesto) { this.idPresupuesto = idPresupuesto; }

    public String getNombreDepartamento() { return nombreDepartamento; }
    public void setNombreDepartamento(String nombreDepartamento) { this.nombreDepartamento = nombreDepartamento; }

    public java.util.Map<String, String> toMap() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("idorden", String.valueOf(idOrden));
        map.put("numero_orden", numeroOrden == null ? "" : numeroOrden);
        map.put("numero_plan", numeroPlan == null ? "" : numeroPlan);
        map.put("cantidad", String.valueOf(cantidad));
        map.put("tipo", tipo == null ? "" : tipo);
        map.put("estado", estado == null ? "" : estado);
        map.put("fechacreacion", fechaCreacion == null ? "" : fechaCreacion.toString());
        map.put("descripcion", descripcion == null ? "" : descripcion);
        map.put("inversion", String.valueOf(inversion));
        map.put("nombredepartamento", nombreDepartamento == null ? "" : nombreDepartamento);
        return map;
    }
}
