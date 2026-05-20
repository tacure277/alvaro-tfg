package com.alvaro.deto_android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Idea  implements Serializable { //  necesita pasarlos a bytes
    private int idea_id;
    private String titulo;
    private String descripcion;
    private boolean es_anonima;
    private String fecha_publicacion;
    private int usuario_id;
    private String autor;
    private int  num_comentarios;
    @SerializedName("imagen_url")
    private String imagen_url;
    @SerializedName("foto_autor_url")
    private String foto_autor_url;
    private int num_likes;
    private boolean usuario_dio_like;



}