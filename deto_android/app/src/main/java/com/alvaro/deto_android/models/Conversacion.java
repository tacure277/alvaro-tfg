package com.alvaro.deto_android.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class Conversacion {
    private Usuario otro_usuario;
    private Mensaje ultimo_mensaje;
    private int mensajes_no_leidos;
}
