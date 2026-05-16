package com.salesianos.models;

import java.util.HashMap;
import java.util.Map;

public class Notification {
    private long idNotificacion;
    private long idUsuarioDestino;
    private String mensaje;
    private boolean leida;
    private String fecha;
    private Long idOrden;

    public Notification() {}

    public long getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(long idNotificacion) { this.idNotificacion = idNotificacion; }

    public long getIdUsuarioDestino() { return idUsuarioDestino; }
    public void setIdUsuarioDestino(long idUsuarioDestino) { this.idUsuarioDestino = idUsuarioDestino; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Long getIdOrden() { return idOrden; }
    public void setIdOrden(Long idOrden) { this.idOrden = idOrden; }

    public Map<String, String> toMap() {
        Map<String, String> m = new HashMap<>();
        m.put("idNotificacion", String.valueOf(idNotificacion));
        m.put("mensaje", mensaje == null ? "" : mensaje);
        m.put("leida", String.valueOf(leida));
        m.put("fecha", fecha == null ? "" : fecha);
        m.put("idOrden", idOrden == null ? "" : String.valueOf(idOrden));
        return m;
    }
}
