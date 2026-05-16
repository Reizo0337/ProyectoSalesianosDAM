package com.salesianos.models;

import java.util.HashMap;
import java.util.Map;

public class Comment {
    private long idComentario;
    private long idOrden;
    private long idUsuario;
    private String usuarioNombre;
    private String comentario;
    private String fecha;

    public Comment() {}

    public long getIdComentario() { return idComentario; }
    public void setIdComentario(long idComentario) { this.idComentario = idComentario; }

    public long getIdOrden() { return idOrden; }
    public void setIdOrden(long idOrden) { this.idOrden = idOrden; }

    public long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(long idUsuario) { this.idUsuario = idUsuario; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("idComentario", String.valueOf(idComentario));
        map.put("usuario", usuarioNombre == null ? "" : usuarioNombre);
        map.put("comentario", comentario == null ? "" : comentario);
        map.put("fecha", fecha == null ? "" : fecha);
        return map;
    }
}
