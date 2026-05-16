package com.salesianos.models;

public class Budget {
    private long idPresupuesto;
    private String codigo;
    private String nombrePresupuesto;
    private double cantidad;
    private double gasto;
    private String type; // Presupuesto, PlanInversion
    private long idDepartamento;
    private String nombreDepartamento; // Auxiliar

    public Budget() {}

    // Getters y Setters
    public long getIdPresupuesto() { return idPresupuesto; }
    public void setIdPresupuesto(long idPresupuesto) { this.idPresupuesto = idPresupuesto; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombrePresupuesto() { return nombrePresupuesto; }
    public void setNombrePresupuesto(String nombrePresupuesto) { this.nombrePresupuesto = nombrePresupuesto; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public double getGasto() { return gasto; }
    public void setGasto(double gasto) { this.gasto = gasto; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(long idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getNombreDepartamento() { return nombreDepartamento; }
    public void setNombreDepartamento(String nombreDepartamento) { this.nombreDepartamento = nombreDepartamento; }
    
    public double getDisponible() {
        return cantidad - gasto;
    }

    public java.util.Map<String, String> toMap() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("idpresupuesto", String.valueOf(idPresupuesto));
        map.put("codigo", codigo == null ? "" : codigo);
        map.put("nombrepresupuesto", nombrePresupuesto == null ? "" : nombrePresupuesto);
        map.put("cantidad", String.valueOf(cantidad));
        map.put("gasto", String.valueOf(gasto));
        map.put("disponible", String.valueOf(getDisponible()));
        map.put("type", type == null ? "" : type);
        map.put("iddepartamento", String.valueOf(idDepartamento));
        map.put("nombredepartamento", nombreDepartamento == null ? "" : nombreDepartamento);
        return map;
    }
}
