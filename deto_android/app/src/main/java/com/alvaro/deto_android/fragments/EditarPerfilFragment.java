package com.alvaro.deto_android.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alvaro.deto_android.R;
import com.alvaro.deto_android.RetrofitClient;
import com.alvaro.deto_android.models.Usuario;
import com.alvaro.deto_android.service.UsuarioService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class EditarPerfilFragment extends Fragment {

    private TextInputEditText etNombre, etDescripcion;
    private MaterialButton btnGuardar, btnCancelar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar_perfil, container, false);

        etNombre = view.findViewById(R.id.etNombre);
        etDescripcion = view.findViewById(R.id.etDescripcion);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnCancelar = view.findViewById(R.id.btnCancelar);

        cargarDatosActuales();

        btnGuardar.setOnClickListener(v -> guardarCambios());

        btnCancelar.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void cargarDatosActuales() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String nombre = prefs.getString("nombre", "");
        String descripcion = prefs.getString("descripcion", "");

        etNombre.setText(nombre);
        etDescripcion.setText(descripcion);
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody nombreBody = RequestBody.create(MediaType.parse("text/plain"), nombre);
        RequestBody descripcionBody = RequestBody.create(MediaType.parse("text/plain"), descripcion);

        UsuarioService api = RetrofitClient.getUsuarioService();
        Call<Usuario> call = api.editarPerfil(nombreBody, descripcionBody, null);

        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuario = response.body();

                    SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("nombre", usuario.getNombre())
                            .putString("descripcion", usuario.getDescripcion())
                            .apply();

                    Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
