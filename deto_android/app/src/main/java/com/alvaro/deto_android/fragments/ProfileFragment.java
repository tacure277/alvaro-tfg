package com.alvaro.deto_android.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvaro.deto_android.DetalleIdeaActivity;
import com.alvaro.deto_android.LoginActivity;
import com.alvaro.deto_android.R;
import com.alvaro.deto_android.RetrofitClient;
import com.alvaro.deto_android.adapters.IdeasAdapter;
import com.alvaro.deto_android.models.Idea;
import com.alvaro.deto_android.models.Usuario;
import com.alvaro.deto_android.service.CrearIdeaService;
import com.alvaro.deto_android.service.UsuarioService;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private ShapeableImageView imgAvatar;
    private TextView tvNombre, tvEmail, tvDescripcion;
    private TextView tvNumIdeas, tvNumSeguidores, tvNumSiguiendo;
    private MaterialButton btnEditarPerfil, btnCerrarSesion;
    private RecyclerView recyclerViewMisIdeas;
    private LinearLayout layoutEmpty;
    private IdeasAdapter adapter;
    private List<Idea> misIdeas = new ArrayList<>();

    private Uri fotoUri;
    private File fotoArchivo;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK) {
                            if (fotoUri != null) {
                                imgAvatar.setImageURI(fotoUri);
                                subirFotoPerfil(fotoUri);
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            fotoUri = result.getData().getData();
                            imgAvatar.setImageURI(fotoUri);
                            subirFotoPerfil(fotoUri);
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvNombre = view.findViewById(R.id.tvNombre);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvDescripcion = view.findViewById(R.id.tvDescripcion);
        tvNumIdeas = view.findViewById(R.id.tvNumIdeas);
        tvNumSeguidores = view.findViewById(R.id.tvNumSeguidores);
        tvNumSiguiendo = view.findViewById(R.id.tvNumSiguiendo);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        recyclerViewMisIdeas = view.findViewById(R.id.recyclerViewMisIdeas);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        recyclerViewMisIdeas.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new IdeasAdapter(new IdeasAdapter.OnIdeaClickListener() {
            @Override
            public void onIdeaClick(Idea idea) {
                Intent intent = new Intent(getActivity(), DetalleIdeaActivity.class);
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
                mostrarDialogEditar(idea, position);
            }

            @Override
            public void onEliminarClick(Idea idea, int position) {
                mostrarDialogEliminar(idea, position);
            }
        });

        recyclerViewMisIdeas.setAdapter(adapter);

        cargarPerfil();
        cargarMisIdeas();

        imgAvatar.setOnClickListener(v -> mostrarDialogoSeleccionImagen());

        btnEditarPerfil.setOnClickListener(v -> {
            EditarPerfilFragment editarFragment = new EditarPerfilFragment();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, editarFragment)
                    .addToBackStack("editar_perfil")
                    .commit();
        });

        btnCerrarSesion.setOnClickListener(v -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
            new Thread(() -> Glide.get(requireContext()).clearDiskCache()).start();
            Glide.get(requireContext()).clearMemory();
            prefs.edit().clear().apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void cargarPerfil() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int usuarioId = prefs.getInt("usuario_id", 0);

        if (usuarioId == 0) {
            cargarDesdePrefs();
            return;
        }

        UsuarioService api = RetrofitClient.getUsuarioService();
        Call<Usuario> call = api.obtenerPerfilPorId(usuarioId);

        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuario = response.body();

                    tvNombre.setText(usuario.getNombre());
                    tvEmail.setText(usuario.getCorreo());
                    tvDescripcion.setText(usuario.getDescripcion() != null ? usuario.getDescripcion() : "Sin descripción");
                    tvNumIdeas.setText(String.valueOf(usuario.getNum_ideas()));
                    tvNumSeguidores.setText(String.valueOf(usuario.getNum_seguidores()));
                    tvNumSiguiendo.setText(String.valueOf(usuario.getNum_siguiendo()));

                    String fotoUrl = usuario.getFoto_perfil_url();
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        if (fotoUrl.contains("127.0.0.1")) {
                            fotoUrl = fotoUrl.replace("127.0.0.1", "10.0.2.2");
                        }
                        Glide.with(ProfileFragment.this)
                                .load(fotoUrl)
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher)
                                .circleCrop()
                                .into(imgAvatar);
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarMisIdeas() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int usuarioId = prefs.getInt("usuario_id", 0);

        if (usuarioId == 0) return;

        CrearIdeaService api = RetrofitClient.getCrearIdeaService();
        Call<List<Idea>> call = api.getIdeas(usuarioId);

        call.enqueue(new Callback<List<Idea>>() {
            @Override
            public void onResponse(Call<List<Idea>> call, Response<List<Idea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Idea> todas = response.body();
                    misIdeas.clear();

                    for (Idea idea : todas) {
                        if (idea.getUsuario_id() == usuarioId) {
                            misIdeas.add(idea);
                        }
                    }

                    if (misIdeas.isEmpty()) {
                        recyclerViewMisIdeas.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerViewMisIdeas.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                        adapter.setIdeas(misIdeas);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Idea>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al cargar ideas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDesdePrefs() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String nombre = prefs.getString("nombre", "Usuario");
        String email = prefs.getString("correo", "email@example.com");
        String descripcion = prefs.getString("descripcion", "Sin descripción");
        String fotoUrl = prefs.getString("foto_perfil_url", null);

        tvNombre.setText(nombre);
        tvEmail.setText(email);
        tvDescripcion.setText(descripcion);

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            if (fotoUrl.contains("127.0.0.1")) {
                fotoUrl = fotoUrl.replace("127.0.0.1", "10.0.2.2");
            }
            Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(imgAvatar);
        }
    }

    private void mostrarDialogoSeleccionImagen() {
        String[] opciones = {"Tomar foto", "Elegir de galería", "Cancelar"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Actualizar foto de perfil")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirCamara();
                    else if (which == 1) abrirGaleria();
                    else dialog.dismiss();
                })
                .show();
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            try {
                fotoArchivo = crearArchivoImagen();
                fotoUri = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider", fotoArchivo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                cameraLauncher.launch(intent);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Error al crear archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File crearArchivoImagen() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void subirFotoPerfil(Uri imagenUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imagenUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), bytes);
            String fileName = "foto_" + System.currentTimeMillis() + ".jpg";
            MultipartBody.Part fotoPart = MultipartBody.Part.createFormData("foto", fileName, requestFile);

            UsuarioService api = RetrofitClient.getUsuarioService();
            Call<Usuario> call = api.actualizarFotoPerfil(fotoPart);

            call.enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        prefs.edit().putString("foto_perfil_url", response.body().getFoto_perfil_url()).apply();
                        cargarPerfil();
                    }
                }

                @Override
                public void onFailure(Call<Usuario> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al procesar imagen", Toast.LENGTH_SHORT).show();
        }
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
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
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
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                }
            });
        }
    }

    private void mostrarDialogEditar(Idea idea, int position) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_idea, null);

        TextInputEditText etTitulo = dialogView.findViewById(R.id.etTitulo);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);

        etTitulo.setText(idea.getTitulo());
        etDescripcion.setText(idea.getDescripcion());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Editar idea")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoTitulo = etTitulo.getText().toString().trim();
                    String nuevaDesc = etDescripcion.getText().toString().trim();

                    if (nuevoTitulo.isEmpty()) {
                        Toast.makeText(getContext(), "El título no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    editarIdea(idea, nuevoTitulo, nuevaDesc, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
                    misIdeas.remove(position);

                    // Verificar si quedó vacío
                    if (misIdeas.isEmpty()) {
                        recyclerViewMisIdeas.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarPerfil();
        cargarMisIdeas();
    }
}