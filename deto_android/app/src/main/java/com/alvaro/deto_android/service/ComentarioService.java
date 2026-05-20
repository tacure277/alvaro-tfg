package com.alvaro.deto_android.service;

import com.alvaro.deto_android.models.Comentario;
import com.alvaro.deto_android.requests.ComentarioRequest;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ComentarioService {

    @GET("ideas/{idea_id}/comentarios/")
    Call<List<Comentario>> obtenerComentarios(@Path("idea_id") int ideaId);

    @POST("ideas/{idea_id}/comentarios/")
    Call<Comentario> crearComentario(@Path("idea_id") int ideaId, @Body ComentarioRequest request);

    // ✅ NUEVO: Editar comentario
    @PUT("comentarios/{comentario_id}/editar/")
    Call<Comentario> editarComentario(@Path("comentario_id") int comentarioId, @Body ComentarioRequest request);

    // ✅ NUEVO: Eliminar comentario
    @HTTP(method = "DELETE", path = "comentarios/{comentario_id}/eliminar/")
    Call<ResponseBody> eliminarComentario(@Path("comentario_id") int comentarioId);
}