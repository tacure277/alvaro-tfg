package com.alvaro.deto_android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvaro.deto_android.adapters.MensajesAdapter;
import com.alvaro.deto_android.models.Mensaje;
import com.alvaro.deto_android.service.MensajeService;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMensajes;
    private EditText etMensaje;
    private FloatingActionButton fabEnviar;
    private MensajesAdapter adapter;
    private int otroUsuarioId;
    private int miUsuarioId;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Obtener datos del intent
        otroUsuarioId = getIntent().getIntExtra("usuario_id", 0);
        String otroUsuarioNombre = getIntent().getStringExtra("usuario_nombre");
        String otroUsuarioFoto = getIntent().getStringExtra("usuario_foto");

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        miUsuarioId = prefs.getInt("usuario_id", 0);

        // Referencias
        recyclerViewMensajes = findViewById(R.id.recyclerViewMensajes);
        etMensaje = findViewById(R.id.etMensaje);
        fabEnviar = findViewById(R.id.fabEnviar);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        ShapeableImageView imgAvatarChat = findViewById(R.id.imgAvatarChat);

        // Configurar header
        tvNombreUsuario.setText(otroUsuarioNombre);
        if (otroUsuarioFoto != null && !otroUsuarioFoto.isEmpty()) {
            String url = otroUsuarioFoto;
            if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", "10.0.2.2");
            }
            Glide.with(this).load(url).circleCrop().into(imgAvatarChat);
        }

        btnBack.setOnClickListener(v -> finish());

        // Configurar RecyclerView
        recyclerViewMensajes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MensajesAdapter(miUsuarioId);
        recyclerViewMensajes.setAdapter(adapter);

        // Cargar mensajes
        cargarMensajes();

        // Enviar mensaje
        fabEnviar.setOnClickListener(v -> enviarMensaje());

        // Polling cada 3 segundos para nuevos mensajes
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                cargarMensajes();
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void cargarMensajes() {
        MensajeService api = RetrofitClient.getMensajeService();
        Call<List<Mensaje>> call = api.getMensajesConUsuario(otroUsuarioId);

        call.enqueue(new Callback<List<Mensaje>>() {
            @Override
            public void onResponse(Call<List<Mensaje>> call, Response<List<Mensaje>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setMensajes(response.body());
                    if (adapter.getItemCount() > 0) {
                        recyclerViewMensajes.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Mensaje>> call, Throwable t) {
                // Error silencioso en polling
            }
        });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();

        if (texto.isEmpty()) {
            Toast.makeText(this, "Escribe un mensaje", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("receptor_id", otroUsuarioId);
        body.put("texto", texto);

        MensajeService api = RetrofitClient.getMensajeService();
        Call<Mensaje> call = api.enviarMensaje(body);

        call.enqueue(new Callback<Mensaje>() {
            @Override
            public void onResponse(Call<Mensaje> call, Response<Mensaje> response) {
                if (response.isSuccessful() && response.body() != null) {
                    etMensaje.setText("");
                    adapter.addMensaje(response.body());
                    recyclerViewMensajes.scrollToPosition(adapter.getItemCount() - 1);
                } else {
                    Toast.makeText(ChatActivity.this, "Error al enviar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Mensaje> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
