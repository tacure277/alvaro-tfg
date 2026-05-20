package com.alvaro.deto_android.requests;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    private String correo;
    @SerializedName("contraseña")

    private String contrasena;


}