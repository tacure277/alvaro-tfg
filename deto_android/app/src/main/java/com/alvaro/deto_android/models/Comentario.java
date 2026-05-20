package com.alvaro.deto_android.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Comentario implements Serializable {

    private int comentario_id;

    private String contenido;

    private String fecha_comentario;

    private int usuario_id;

    private int idea_id;

    private String autor;

    private String foto_autor_url;

    // Getters
    public int getComentario_id() { return comentario_id; }
    public String getContenido() { return contenido; }
    public String getFecha_comentario() { return fecha_comentario; }
    public int getUsuario_id() { return usuario_id; }
    public int getIdea_id() { return idea_id; }
    public String getAutor() { return autor; }
    public String getFoto_autor_url() { return foto_autor_url; }
}