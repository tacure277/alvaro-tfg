package com.alvaro.deto_android.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alvaro.deto_android.R;
import com.alvaro.deto_android.RetrofitClient;
import com.alvaro.deto_android.models.Idea;
import com.alvaro.deto_android.service.CrearIdeaService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateFragment extends Fragment {

    private TextInputEditText etTitulo;
    private TextInputEditText etDescripcion;
    private MaterialSwitch switchAnonimo;
    private MaterialButton btnPublicar;
    private MaterialButton btnCancelar;

    private MaterialCardView cardSeleccionarImagen;
    private MaterialCardView cardPreview;
    private ImageView imgPreviewGrande;
    private MaterialButton btnQuitarImagen;

    private Uri imagenUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imagenUri = result.getData().getData();
                    cardPreview.setVisibility(View.VISIBLE);
                    cardSeleccionarImagen.setVisibility(View.GONE);
                    imgPreviewGrande.setImageURI(imagenUri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        etTitulo = view.findViewById(R.id.etTitulo);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        switchAnonimo = view.findViewById(R.id.switchAnonimo);
        btnPublicar = view.findViewById(R.id.btnPublicar);
        btnCancelar = view.findViewById(R.id.btnCancelar);

        cardSeleccionarImagen = view.findViewById(R.id.cardSeleccionarImagen);
        cardPreview = view.findViewById(R.id.cardPreview);
        imgPreviewGrande = view.findViewById(R.id.imgPreviewGrande);
        btnQuitarImagen = view.findViewById(R.id.btnQuitarImagen);

        btnCancelar.setOnClickListener(v -> getActivity().onBackPressed());
        cardSeleccionarImagen.setOnClickListener(v -> seleccionarImagen());
        btnQuitarImagen.setOnClickListener(v -> {
            imagenUri = null;
            cardPreview.setVisibility(View.GONE);
            cardSeleccionarImagen.setVisibility(View.VISIBLE);
        });
        btnPublicar.setOnClickListener(v -> crearIdea());

        return view;
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void crearIdea() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        boolean anonimo = switchAnonimo.isChecked();

        if (titulo.isEmpty()) {
            Toast.makeText(getContext(), "Escribe un título", Toast.LENGTH_SHORT).show();
            return;
        }
        if (descripcion.isEmpty()) {
            Toast.makeText(getContext(), "Escribe una descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPublicar.setEnabled(false);
        btnPublicar.setText("Publicando...");

        try {
            RequestBody tituloBody = RequestBody.create(MediaType.parse("text/plain"), titulo);
            RequestBody descripcionBody = RequestBody.create(MediaType.parse("text/plain"), descripcion);
            RequestBody anonimoBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(anonimo));

            MultipartBody.Part imagenPart = null;
            if (imagenUri != null) {
                InputStream inputStream = getContext().getContentResolver().openInputStream(imagenUri);
                File file = new File(getContext().getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                RequestBody imagenBody = RequestBody.create(MediaType.parse("image/*"), file);
                imagenPart = MultipartBody.Part.createFormData("imagen", file.getName(), imagenBody);
            }

            CrearIdeaService api = RetrofitClient.getCrearIdeaService();
            Call<Idea> call = api.crearIdea(tituloBody, descripcionBody, anonimoBody, imagenPart);

            call.enqueue(new Callback<Idea>() {
                @Override
                public void onResponse(Call<Idea> call, Response<Idea> response) {
                    btnPublicar.setEnabled(true);
                    btnPublicar.setText("Publicar");

                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "¡Idea creada!", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                    } else {
                        String errorMsg = "Error " + response.code();
                        if (response.code() == 401) errorMsg = "Sesión expirada";
                        else if (response.code() == 400) errorMsg = "Datos inválidos";
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Idea> call, Throwable t) {
                    btnPublicar.setEnabled(true);
                    btnPublicar.setText("Publicar");
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al procesar imagen", Toast.LENGTH_SHORT).show();
            btnPublicar.setEnabled(true);
            btnPublicar.setText("Publicar");
        }
    }
}