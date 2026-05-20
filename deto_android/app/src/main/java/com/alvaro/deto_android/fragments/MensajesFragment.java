package com.alvaro.deto_android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvaro.deto_android.ChatActivity;
import com.alvaro.deto_android.R;
import com.alvaro.deto_android.RetrofitClient;
import com.alvaro.deto_android.adapters.ConversacionesAdapter;
import com.alvaro.deto_android.models.Conversacion;
import com.alvaro.deto_android.service.MensajeService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MensajesFragment extends Fragment {

    private RecyclerView recyclerViewConversaciones;
    private TextView tvEmpty;
    private ConversacionesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mensajes, container, false);

        recyclerViewConversaciones = view.findViewById(R.id.recyclerViewConversaciones);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerViewConversaciones.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ConversacionesAdapter(conversacion -> {
            // Abrir chat con este usuario
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("usuario_id", conversacion.getOtro_usuario().getUsuario_id());
            intent.putExtra("usuario_nombre", conversacion.getOtro_usuario().getNombre());
            intent.putExtra("usuario_foto", conversacion.getOtro_usuario().getFoto_perfil_url());
            startActivity(intent);
        });

        recyclerViewConversaciones.setAdapter(adapter);

        cargarConversaciones();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarConversaciones();
    }

    private void cargarConversaciones() {
        MensajeService api = RetrofitClient.getMensajeService();
        Call<List<Conversacion>> call = api.getConversaciones();

        call.enqueue(new Callback<List<Conversacion>>() {
            @Override
            public void onResponse(Call<List<Conversacion>> call, Response<List<Conversacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Conversacion> conversaciones = response.body();

                    if (conversaciones.isEmpty()) {
                        recyclerViewConversaciones.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerViewConversaciones.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                        adapter.setConversaciones(conversaciones);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar conversaciones", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Conversacion>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
