package com.alvaro.deto_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alvaro.deto_android.adapters.IdeasAdapter;
import com.alvaro.deto_android.models.Idea;
import com.alvaro.deto_android.models.Usuario;
import com.alvaro.deto_android.service.CrearIdeaService;
import com.alvaro.deto_android.service.UsuarioService;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static android.content.Context.MODE_PRIVATE;

public class PerfilUsuarioActivity extends AppCompatActivity {

    private ShapeableImageView imgAvatar;
    private TextView tvNombre, tvCorreo, tvDescripcion, tvNumSeguidores, tvNumSiguiendo, tvNumIdeas;
    private Button btnSeguir;
    private RecyclerView recyclerViewIdeas;
    private IdeasAdapter adapter;
    private int usuarioId;
    private int miUsuarioId;
    private boolean siguiendo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        usuarioId = getIntent().getIntExtra("usuario_id", 0);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        miUsuarioId = prefs.getInt("usuario_id", 0);

        imgAvatar = findViewById(R.id.imgAvatar);
        tvNombre = findViewById(R.id.tvNombre);
        tvCorreo = findViewById(R.id.tvCorreo);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvNumSeguidores = findViewById(R.id.tvNumSeguidores);
        tvNumSiguiendo = findViewById(R.id.tvNumSiguiendo);
        tvNumIdeas = findViewById(R.id.tvNumIdeas);
        btnSeguir = findViewById(R.id.btnSeguir);
        recyclerViewIdeas = findViewById(R.id.recyclerViewIdeas);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerViewIdeas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IdeasAdapter(new IdeasAdapter.OnIdeaClickListener() {
            @Override
            public void onIdeaClick(Idea idea) {
                Intent intent = new Intent(PerfilUsuarioActivity.this, DetalleIdeaActivity.class);
                intent.putExtra("idea_id", idea.getIdea_id());
                startActivity(intent);
            }

            @Override
            public void onAutorClick(int usuarioId, String nombre) {
            }

            @Override
            public void onLikeClick(Idea idea, int position) {
                darOQuitarLike(idea, position);
            }

            @Override
            public void onEditarClick(Idea idea, int position) {
            }

            @Override
            public void onEliminarClick(Idea idea, int position) {
            }
        });
        recyclerViewIdeas.setAdapter(adapter);

        if (usuarioId == miUsuarioId) {
            btnSeguir.setVisibility(Button.GONE);
        }

        cargarPerfil();
        cargarIdeas();
        verificarSiSigue();

        btnSeguir.setOnClickListener(v -> {
            if (siguiendo) {
                dejarSeguir();
            } else {
                seguir();
            }
        });
    }

    private void cargarPerfil() {
        UsuarioService api = RetrofitClient.getUsuarioService();
        Call<Usuario> call = api.obtenerPerfilPorId(usuarioId);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario u = response.body();
                    tvNombre.setText(u.getNombre());
                    tvCorreo.setText(u.getCorreo());
                    tvDescripcion.setText(u.getDescripcion() != null ? u.getDescripcion() : "Sin descripción");
                    tvNumSeguidores.setText(String.valueOf(u.getNum_seguidores()));
                    tvNumSiguiendo.setText(String.valueOf(u.getNum_siguiendo()));
                    tvNumIdeas.setText(String.valueOf(u.getNum_ideas()));

                    if (u.getFoto_perfil_url() != null && !u.getFoto_perfil_url().isEmpty()) {
                        String url = u.getFoto_perfil_url();
                        if (url.contains("127.0.0.1")) url = url.replace("127.0.0.1", "10.0.2.2");
                        Glide.with(PerfilUsuarioActivity.this).load(url).circleCrop().into(imgAvatar);
                    }
                }
            }
            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(PerfilUsuarioActivity.this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarIdeas() {
        CrearIdeaService api = RetrofitClient.getCrearIdeaService();
        Call<List<Idea>> call = api.getIdeas(miUsuarioId);
        call.enqueue(new Callback<List<Idea>>() {
            @Override
            public void onResponse(Call<List<Idea>> call, Response<List<Idea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Idea> todas = response.body();
                    List<Idea> misIdeas = new java.util.ArrayList<>();
                    for (Idea i : todas) {
                        if (i.getUsuario_id() == usuarioId) {
                            misIdeas.add(i);
                        }
                    }
                    adapter.setIdeas(misIdeas);
                }
            }
            @Override
            public void onFailure(Call<List<Idea>> call, Throwable t) {
                Toast.makeText(PerfilUsuarioActivity.this, "Error al cargar ideas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarSiSigue() {
        UsuarioService api = RetrofitClient.getUsuarioService();
        Call<ResponseBody> call = api.verificarSiSigue(usuarioId, miUsuarioId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        siguiendo = responseBody.contains("true") || responseBody.contains("sigue");
                        Log.d("VERIFICAR_SEGUIR", "Response: " + responseBody + " | Siguiendo: " + siguiendo);
                    } catch (Exception e) {
                        Log.e("VERIFICAR_SEGUIR", "Error al leer respuesta", e);
                    }
                } else {
                    siguiendo = false;
                }
                actualizarBotonSeguir();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("VERIFICAR_SEGUIR", "Error de conexión", t);
                siguiendo = false;
                actualizarBotonSeguir();
            }
        });
    }

    private void actualizarBotonSeguir() {
        if (siguiendo) {
            btnSeguir.setText("Siguiendo");
            btnSeguir.setBackgroundColor(getResources().getColor(R.color.gris_oscuro, null));
        } else {
            btnSeguir.setText("Seguir");
            btnSeguir.setBackgroundColor(getResources().getColor(R.color.verde_claro, null));
        }
    }

    private void seguir() {
        UsuarioService api = RetrofitClient.getUsuarioService();
        Map<String, Integer> body = new HashMap<>();
        body.put("seguidor_id", miUsuarioId);
        Call<ResponseBody> call = api.seguir(usuarioId, body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    siguiendo = true;
                    actualizarBotonSeguir();
                    Toast.makeText(PerfilUsuarioActivity.this, "Ahora sigues a este usuario", Toast.LENGTH_SHORT).show();
                    cargarPerfil();
                } else {
                    try {
                        String error = response.errorBody().string();
                        if (error.contains("Ya sigues")) {
                            siguiendo = true;
                            actualizarBotonSeguir();
                        }
                        Toast.makeText(PerfilUsuarioActivity.this, error, Toast.LENGTH_SHORT).show();
                        Log.e("SEGUIR_ERROR", error);
                    } catch (Exception e) {
                        Toast.makeText(PerfilUsuarioActivity.this, "Error al seguir", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PerfilUsuarioActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("SEGUIR_ERROR", "Failure", t);
            }
        });
    }

    private void dejarSeguir() {
        UsuarioService api = RetrofitClient.getUsuarioService();
        Map<String, Integer> body = new HashMap<>();
        body.put("seguidor_id", miUsuarioId);
        Call<ResponseBody> call = api.dejarSeguir(usuarioId, body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    siguiendo = false;
                    actualizarBotonSeguir();
                    Toast.makeText(PerfilUsuarioActivity.this, "Dejaste de seguir", Toast.LENGTH_SHORT).show();
                    cargarPerfil();
                } else {
                    Toast.makeText(PerfilUsuarioActivity.this, "Error al dejar de seguir", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PerfilUsuarioActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void darOQuitarLike(Idea idea, int position) {
        CrearIdeaService api = RetrofitClient.getCrearIdeaService();

        Map<String, Integer> body = new HashMap<>();
        body.put("usuario_id", miUsuarioId);

        if (idea.isUsuario_dio_like()) {
            Call<ResponseBody> call = api.quitarLike(idea.getIdea_id(), body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        idea.setUsuario_dio_like(false);
                        idea.setNum_likes(idea.getNum_likes() - 1);
                        adapter.notifyItemChanged(position);
                    } else {
                        Toast.makeText(PerfilUsuarioActivity.this, "Error al quitar like", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(PerfilUsuarioActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Call<ResponseBody> call = api.darLike(idea.getIdea_id(), body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        idea.setUsuario_dio_like(true);
                        idea.setNum_likes(idea.getNum_likes() + 1);
                        adapter.notifyItemChanged(position);
                    } else {
                        Toast.makeText(PerfilUsuarioActivity.this, "Error al dar like", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(PerfilUsuarioActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}