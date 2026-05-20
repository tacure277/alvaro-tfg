package com.alvaro.deto_android.service;

import com.alvaro.deto_android.models.Idea;

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
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CrearIdeaService {

    @GET("ideas/")
    Call<List<Idea>> getIdeas(@Query("usuario_id") int usuarioId);

    @Multipart
    @POST("ideas/crear/")
    Call<Idea> crearIdea(
            @Part("titulo") RequestBody titulo,
            @Part("descripcion") RequestBody descripcion,
            @Part("es_anonima") RequestBody esAnonima,
            @Part MultipartBody.Part imagen
    );

    @POST("likes/{idea_id}/")
    Call<ResponseBody> darLike(@Path("idea_id") int ideaId, @Body Map<String, Integer> body);

    @HTTP(method = "DELETE", path = "likes/{idea_id}/", hasBody = true)
    Call<ResponseBody> quitarLike(@Path("idea_id") int ideaId, @Body Map<String, Integer> body);

    // ✅ NUEVO: Editar idea
    @Multipart
    @PUT("ideas/{idea_id}/editar/")
    Call<Idea> editarIdea(
            @Path("idea_id") int ideaId,
            @Part("titulo") RequestBody titulo,
            @Part("descripcion") RequestBody descripcion,
            @Part MultipartBody.Part imagen
    );

    // ✅ NUEVO: Eliminar idea
    @HTTP(method = "DELETE", path = "ideas/{idea_id}/eliminar/")
    Call<ResponseBody> eliminarIdea(@Path("idea_id") int ideaId);
}