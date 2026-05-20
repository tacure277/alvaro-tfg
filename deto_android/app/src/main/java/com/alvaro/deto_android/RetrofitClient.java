package com.alvaro.deto_android;


import android.content.Context;

import com.alvaro.deto_android.Auth.AuthInterceptor;
import com.alvaro.deto_android.service.ComentarioService;
import com.alvaro.deto_android.service.CrearIdeaService;
import com.alvaro.deto_android.service.UsuarioService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    //private static final String BASE_URL = "http://10.0.2.2:8000/api/ideas";
//aqui se configura  conexionses htttp con el servidor de dj jango
//y tiene que estar el hots del servidor de djgano

    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private static Retrofit retrofit = null;
    private static Context context;

    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;

}
    public static CrearIdeaService getCrearIdeaService() {
        return getClient().create(CrearIdeaService.class);
    }
    public static ComentarioService getComentarioService() {
    return getClient().create(ComentarioService.class);
}
    public static UsuarioService getUsuarioService() {
        return getClient().create(UsuarioService.class);
    }

}