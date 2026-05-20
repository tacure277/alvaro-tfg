package com.alvaro.deto_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alvaro.deto_android.R;
import com.alvaro.deto_android.models.Conversacion;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ConversacionesAdapter extends RecyclerView.Adapter<ConversacionesAdapter.ConversacionViewHolder> {

    private List<Conversacion> conversaciones = new ArrayList<>();
    private OnConversacionClickListener listener;

    public interface OnConversacionClickListener {
        void onConversacionClick(Conversacion conversacion);
    }

    public ConversacionesAdapter(OnConversacionClickListener listener) {
        this.listener = listener;
    }

    public void setConversaciones(List<Conversacion> conversaciones) {
        this.conversaciones = conversaciones;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversacion, parent, false);
        return new ConversacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversacionViewHolder holder, int position) {
        Conversacion conversacion = conversaciones.get(position);

        holder.tvNombre.setText(conversacion.getOtro_usuario().getNombre());
        holder.tvUltimoMensaje.setText(conversacion.getUltimo_mensaje().getTexto());

        // Mostrar badge de no leídos
        if (conversacion.getMensajes_no_leidos() > 0) {
            holder.tvNoLeidos.setVisibility(View.VISIBLE);
            holder.tvNoLeidos.setText(String.valueOf(conversacion.getMensajes_no_leidos()));
        } else {
            holder.tvNoLeidos.setVisibility(View.GONE);
        }

        // Cargar foto de perfil
        if (conversacion.getOtro_usuario().getFoto_perfil_url() != null) {
            String url = conversacion.getOtro_usuario().getFoto_perfil_url();
            if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", "10.0.2.2");
            }
            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .circleCrop()
                    .into(holder.imgAvatar);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversacionClick(conversacion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversaciones.size();
    }

    static class ConversacionViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgAvatar;
        TextView tvNombre;
        TextView tvUltimoMensaje;
        TextView tvNoLeidos;

        public ConversacionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvUltimoMensaje = itemView.findViewById(R.id.tvUltimoMensaje);
            tvNoLeidos = itemView.findViewById(R.id.tvNoLeidos);
        }
    }
}
