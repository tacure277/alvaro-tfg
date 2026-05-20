package com.alvaro.deto_android.fragments;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvaro.deto_android.R;
import com.alvaro.deto_android.RetrofitClient;
import com.alvaro.deto_android.adapters.ComentariosAdapter;
import com.alvaro.deto_android.models.Comentario;
import com.alvaro.deto_android.models.Idea;
import com.alvaro.deto_android.requests.ComentarioRequest;
import com.alvaro.deto_android.service.ComentarioService;
import com.alvaro.deto_android.service.CrearIdeaService;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class DetalleIdea extends Fragment {

    private static final String ARG_IDEA_ID = "idea_id";

    private int ideaId;
    private Idea idea;
    private ComentariosAdapter adapter;

    private TextInputEditText etComentario;
    private TextView tvNumComentariosDetail;
    private ImageView imgIdeaDetail;
    private ShapeableImageView imgAvatarDetail;
    private TextView tvTituloDetail;
    private TextView tvDescripcionDetail;
    private TextView tvAutorDetail;
    private TextView tvFechaDetail;
    private Chip chipAnonimoDetail;
    private RecyclerView recyclerViewComentarios;
    private MaterialButton btnEnviarComentario;

    public static DetalleIdea newInstance(int ideaId) {
        DetalleIdea fragment = new DetalleIdea();
        Bundle args = new Bundle();
        args.putInt(ARG_IDEA_ID, ideaId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ideaId = getArguments().getInt(ARG_IDEA_ID, 0);
            Log.d("DETALLE_DEBUG", "idea_id recibido: " + ideaId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        imgAvatarDetail = view.findViewById(R.id.imgAvatarDetail);
        imgIdeaDetail = view.findViewById(R.id.imgIdeaDetail);
        tvAutorDetail = view.findViewById(R.id.tvAutorDetail);
        tvTituloDetail = view.findViewById(R.id.tvTituloDetail);
        tvDescripcionDetail = view.findViewById(R.id.tvDescripcionDetail);
        tvFechaDetail = view.findViewById(R.id.tvFechaDetail);
        chipAnonimoDetail = view.findViewById(R.id.chipAnonimoDetail);
        recyclerViewComentarios = view.findViewById(R.id.recyclerViewComentarios);
        etComentario = view.findViewById(R.id.etComentario);
        btnEnviarComentario = view.findViewById(R.id.btnEnviarComentario);
        tvNumComentariosDetail = view.findViewById(R.id.tvNumComentariosDetail);

        if (tvTituloDetail == null) Log.e("LAYOUT_DEBUG", "tvTituloDetail es null");
        if (tvDescripcionDetail == null) Log.e("LAYOUT_DEBUG", "tvDescripcionDetail es null");

        recyclerViewComentarios.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ComentariosAdapter();
        recyclerViewComentarios.setAdapter(adapter);

        cargarIdeaCompleta();

        btnEnviarComentario.setOnClickListener(v -> enviarComentario());

        return view;
    }

    private void cargarIdeaCompleta() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int miUsuarioId = prefs.getInt("usuario_id", 0);

        CrearIdeaService api = RetrofitClient.getCrearIdeaService();
        Call<List<Idea>> call = api.getIdeas(miUsuarioId);

        call.enqueue(new Callback<List<Idea>>() {
            @Override
            public void onResponse(Call<List<Idea>> call, Response<List<Idea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Idea> ideas = response.body();

                    for (Idea i : ideas) {
                        if (i.getIdea_id() == ideaId) {
                            idea = i;
                            break;
                        }
                    }

                    if (idea != null) {
                        mostrarDatos();
                        cargarComentarios();
                    } else {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Idea no encontrada", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Error al cargar idea", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Idea>> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void mostrarDatos() {
        if (idea == null) return;

        tvTituloDetail.setText(idea.getTitulo());
        tvDescripcionDetail.setText(idea.getDescripcion());
        tvFechaDetail.setText(idea.getFecha_publicacion());

        String fotoAutor = idea.getFoto_autor_url();
        if (fotoAutor != null && !fotoAutor.isEmpty()) {
            if (fotoAutor.contains("127.0.0.1")) {
                fotoAutor = fotoAutor.replace("127.0.0.1", "10.0.2.2");
            }
            Glide.with(this)
                    .load(fotoAutor)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(imgAvatarDetail);
        } else {
            imgAvatarDetail.setImageResource(R.mipmap.ic_launcher);
        }

        if (idea.isEs_anonima()) {
            tvAutorDetail.setText("Anónimo");
            chipAnonimoDetail.setVisibility(View.VISIBLE);
            imgAvatarDetail.setImageResource(R.mipmap.ic_launcher);
        } else {
            tvAutorDetail.setText(idea.getAutor() != null ? idea.getAutor() : "Usuario");
            chipAnonimoDetail.setVisibility(View.GONE);
        }

        if (idea.getImagen_url() != null && !idea.getImagen_url().isEmpty()) {
            String urlImagen = idea.getImagen_url();
            if (urlImagen.contains("127.0.0.1")) {
                urlImagen = urlImagen.replace("127.0.0.1", "10.0.2.2");
            }
            imgIdeaDetail.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(urlImagen)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(imgIdeaDetail);
        } else {
            imgIdeaDetail.setVisibility(View.GONE);
        }
    }

    private void cargarComentarios() {
        if (idea == null) return;

        ComentarioService api = RetrofitClient.getComentarioService();
        Call<List<Comentario>> call = api.obtenerComentarios(idea.getIdea_id());

        call.enqueue(new Callback<List<Comentario>>() {
            @Override
            public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comentario> comentarios = response.body();
                    adapter.setComentarios(comentarios);
                    tvNumComentariosDetail.setText("💬 " + comentarios.size() + " Comentarios");
                }
            }

            @Override
            public void onFailure(Call<List<Comentario>> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar comentarios", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void enviarComentario() {
        if (idea == null) return;

        String contenido = etComentario.getText().toString().trim();

        if (contenido.isEmpty()) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Escribe un comentario", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ComentarioRequest request = new ComentarioRequest(contenido);
        ComentarioService api = RetrofitClient.getComentarioService();
        Call<Comentario> call = api.crearComentario(idea.getIdea_id(), request);

        call.enqueue(new Callback<Comentario>() {
            @Override
            public void onResponse(Call<Comentario> call, Response<Comentario> response) {
                if (response.isSuccessful()) {
                    etComentario.setText("");
                    cargarComentarios();
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Comentario publicado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (isAdded() && getContext() != null) {
                        String errorMsg = "Error " + response.code();
                        if (response.code() == 401) {
                            errorMsg = "Sesión expirada";
                        }
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Comentario> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}