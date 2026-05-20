package com.alvaro.deto_android.service;

import com.alvaro.deto_android.models.Conversacion;
import com.alvaro.deto_android.models.Mensaje;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MensajeService {

    // Lista de conversaciones
    @GET("mensajes/conversaciones/")
    Call<List<Conversacion>> getConversaciones();

    // Mensajes con un usuario específico
    @GET("mensajes/chat/{usuario_id}/")
    Call<List<Mensaje>> getMensajesConUsuario(@Path("usuario_id") int usuarioId);

    // Enviar mensaje
    @POST("mensajes/enviar/")
    Call<Mensaje> enviarMensaje(@Body Map<String, Object> body);

    // Marcar como leído
    @PUT("mensajes/{mensaje_id}/marcar-leido/")
    Call<ResponseBody> marcarLeido(@Path("mensaje_id") int mensajeId);

    // Total de no leídos
    @GET("mensajes/no-leidos/")
    Call<Map<String, Integer>> getTotalNoLeidos();
}
