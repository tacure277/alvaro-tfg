package com.alvaro.deto_android.service;

import com.alvaro.deto_android.models.Usuario;
import com.alvaro.deto_android.requests.LoginRequest;
import com.alvaro.deto_android.requests.RegistroRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UsuarioService {

    @POST("auth/login/")
    Call<ResponseBody> login(@Body LoginRequest request);

    @POST("auth/registro/")
    Call<ResponseBody> registro(@Body RegistroRequest request);

    @GET("auth/perfil/{id}/")
    Call<Usuario> obtenerPerfilPorId(@Path("id") int usuarioId);

    @Multipart
    @POST("auth/perfil/editar/")
    Call<Usuario> editarPerfil(
            @Part("nombre") RequestBody nombre,
            @Part("descripcion") RequestBody descripcion,
            @Part MultipartBody.Part foto_perfil
    );

    @Multipart
    @POST("auth/perfil/foto/")
    Call<Usuario> actualizarFotoPerfil(@Part MultipartBody.Part foto);

    @GET("auth/buscar/")
    Call<List<Usuario>> buscarUsuarios(@Query("q") String query);

    @POST("seguidores/seguir/{id}/")
    Call<ResponseBody> seguir(@Path("id") int usuarioId, @Body Map<String, Integer> body);

    @HTTP(method = "DELETE", path = "seguidores/dejar-seguir/{id}/", hasBody = true)
    Call<ResponseBody> dejarSeguir(@Path("id") int usuarioId, @Body Map<String, Integer> body);

    @GET("seguidores/{usuario_id}/verifica/{seguidor_id}/")
    Call<ResponseBody> verificarSiSigue(
            @Path("usuario_id") int usuarioId,
            @Path("seguidor_id") int seguidorId
    );
}