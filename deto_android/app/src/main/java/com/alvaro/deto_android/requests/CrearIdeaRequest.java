package com.alvaro.deto_android.requests;


import org.jspecify.annotations.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor

@AllArgsConstructor
@Getter
// se envia datoss  aquioi
public class CrearIdeaRequest {
    private String titulo;
    private String descripcion;
    private boolean es_anonima;



}