package com.alvaro.deto_android.response;

import com.alvaro.deto_android.models.Usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
        private String access;
        private String refresh;
        private Usuario usuario;

    }

