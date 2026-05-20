package com.alvaro.deto_android.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class Usuario {
    private int usuario_id;
    private String nombre;
    private String correo;
    private String descripcion;
    private String fecha_creacion;
    private String foto_perfil_url;
    private int num_ideas;
    private int num_seguidores;
    private int num_siguiendo;
}