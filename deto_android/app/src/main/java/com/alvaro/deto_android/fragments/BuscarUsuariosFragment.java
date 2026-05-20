package com.alvaro.deto_android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvaro.deto_android.PerfilUsuarioActivity;
import com.alvaro.deto_android.R;
import com.alvaro.deto_android.RetrofitClient;
import com.alvaro.deto_android.adapters.UsuariosAdapter;
import com.alvaro.deto_android.models.Usuario;
import com.alvaro.deto_android.service.UsuarioService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarUsuariosFragment extends Fragment {

    private EditText etBuscar;
    private RecyclerView recyclerViewUsuarios;
    private TextView tvResultados;
    private LinearLayout layoutSinResultados;

    private UsuariosAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_buscar_usuarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        etBuscar = view.findViewById(R.id.etBuscar);
        recyclerViewUsuarios = view.findViewById(R.id.recyclerViewUsuarios);
        tvResultados = view.findViewById(R.id.tvResultados);
        layoutSinResultados = view.findViewById(R.id.layoutSinResultados);

        recyclerViewUsuarios.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        adapter = new UsuariosAdapter(usuario -> {

            Intent intent = new Intent(getContext(), PerfilUsuarioActivity.class);
            intent.putExtra("usuario_id", usuario.getUsuario_id());
            startActivity(intent);

        });

        recyclerViewUsuarios.setAdapter(adapter);

        etBuscar.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String query = s.toString().trim();

                if (query.length() >= 2) {
                    buscarUsuarios(query);
                } else {
                    adapter.setUsuarios(new ArrayList<>());
                    recyclerViewUsuarios.setVisibility(View.GONE);
                    layoutSinResultados.setVisibility(View.GONE);
                    tvResultados.setText("0 usuarios encontrados");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void buscarUsuarios(String query) {

        UsuarioService api = RetrofitClient.getUsuarioService();

        Call<List<Usuario>> call = api.buscarUsuarios(query);

        call.enqueue(new Callback<List<Usuario>>() {

            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<Usuario> usuarios = response.body();

                    tvResultados.setText(usuarios.size() + " usuarios encontrados");

                    if (usuarios.isEmpty()) {
                        layoutSinResultados.setVisibility(View.VISIBLE);
                        recyclerViewUsuarios.setVisibility(View.GONE);
                    } else {
                        layoutSinResultados.setVisibility(View.GONE);
                        recyclerViewUsuarios.setVisibility(View.VISIBLE);
                        adapter.setUsuarios(usuarios);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {

                Toast.makeText(getContext(),
                        "Error de conexión",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}