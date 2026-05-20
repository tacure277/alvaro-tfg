package com.alvaro.deto_android.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class Mensaje {
    private int mensaje_id;
    private int emisor;
    private int receptor;
    private String texto;
    private String fecha_envio;
    private boolean leido;
    private Usuario emisor_info;
    private Usuario receptor_info;
}
