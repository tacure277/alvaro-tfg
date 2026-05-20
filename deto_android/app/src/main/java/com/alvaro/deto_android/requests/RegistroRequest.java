package com.alvaro.deto_android.requests;


import com.google.gson.annotations.SerializedName;

import lombok.*;



@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegistroRequest {
    private String nombre;
    private String correo;

    @SerializedName("contraseña")
    private String contrasena;


}