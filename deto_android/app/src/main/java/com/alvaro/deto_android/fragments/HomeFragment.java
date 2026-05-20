package com.alvaro.deto_android.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvaro.deto_android.DetalleIdeaActivity;
import com.alvaro.deto_android.PerfilUsuarioActivity;
import com.alvaro.deto_android.R;
import com.alvaro.deto_android.RetrofitClient;
import com.alvaro.deto_android.adapters.IdeasAdapter;
import com.alvaro.deto_android.models.Idea;
import com.alvaro.deto_android.service.CrearIdeaService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewIdeas;
    private LinearLayout layoutEmpty;
    private IdeasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewIdeas = view.findViewById(R.id.recyclerViewIdeas);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        View fabCrearIdea = view.findViewById(R.id.fabCrearIdea);
        fabCrearIdea.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new CreateFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        recyclerViewIdeas.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new IdeasAdapter(new IdeasAdapter.OnIdeaClickListener() {
            @Override
            public void onIdeaClick(Idea idea) {
                Intent intent = new Intent(getActivity(), DetalleIdeaActivity.class);
                intent.putExtra("idea_id", idea.getIdea_id());
                startActivity(intent);
            }

            @Override
            public void onAutorClick(int usuarioId, String nombre) {
                Intent intent = new Intent(getActivity(), PerfilUsuarioActivity.class);
                intent.putExtra("usuario_id", usuarioId);
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Idea idea, int position) {
                darOQuitarLike(idea, position);
            }

            @Override
            public void onEditarClick(Idea idea, int position) {
                mostrarDialogEditar(idea, position);
            }

            @Override
            public void onEliminarClick(Idea idea, int position) {
                mostrarDialogEliminar(idea, position);
            }
        });

        recyclerViewIdeas.setAdapter(adapter);
        cargarIdeas();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarIdeas();
    }

    private void cargarIdeas() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int miUsuarioId = prefs.getInt("usuario_id", 0);

        CrearIdeaService api = RetrofitClient.getCrearIdeaService();
        Call<List<Idea>> call = api.getIdeas(miUsuarioId);

        call.enqueue(new Callback<List<Idea>>() {
            @Override
            public void onResponse(Call<List<Idea>> call, Response<List<Idea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Idea> ideas = response.body();

                    if (ideas.isEmpty()) {
                        recyclerViewIdeas.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerViewIdeas.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                        adapter.setIdeas(ideas);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Idea>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void darOQuitarLike(Idea idea, int position) {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int miUsuarioId = prefs.getInt("usuario_id", 0);

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
                        Toast.makeText(getContext(), "Error al quitar like", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), "Error al dar like", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void mostrarDialogEditar(Idea idea, int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_idea, null);

        TextInputEditText etTitulo = dialogView.findViewById(R.id.etTitulo);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        MaterialButton btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        etTitulo.setText(idea.getTitulo());
        etDescripcion.setText(idea.getDescripcion());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoTitulo = etTitulo.getText().toString().trim();
            String nuevaDesc = etDescripcion.getText().toString().trim();

            if (nuevoTitulo.isEmpty()) {
                Toast.makeText(getContext(), "El título no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            editarIdea(idea, nuevoTitulo, nuevaDesc, position);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void editarIdea(Idea idea, String titulo, String descripcion, int position) {
        RequestBody tituloBody = RequestBody.create(MediaType.parse("text/plain"), titulo);
        RequestBody descripcionBody = RequestBody.create(MediaType.parse("text/plain"), descripcion);

        CrearIdeaService api = RetrofitClient.getCrearIdeaService();
        Call<Idea> call = api.editarIdea(idea.getIdea_id(), tituloBody, descripcionBody, null);

        call.enqueue(new Callback<Idea>() {
            @Override
            public void onResponse(Call<Idea> call, Response<Idea> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateIdea(position, response.body());
                    Toast.makeText(getContext(), "Idea actualizada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Idea> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogEliminar(Idea idea, int position) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar idea")
                .setMessage("¿Estás seguro de que quieres eliminar esta idea? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarIdea(idea, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarIdea(Idea idea, int position) {
        CrearIdeaService api = RetrofitClient.getCrearIdeaService();
        Call<ResponseBody> call = api.eliminarIdea(idea.getIdea_id());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    adapter.removeIdea(position);
                    Toast.makeText(getContext(), "Idea eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}