package com.alvaro.deto_android.adapters;

import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alvaro.deto_android.R;
import com.alvaro.deto_android.models.Mensaje;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class MensajesAdapter extends RecyclerView.Adapter<MensajesAdapter.MensajeViewHolder> {

    private List<Mensaje> mensajes = new ArrayList<>();
    private int miUsuarioId;

    public MensajesAdapter(int miUsuarioId) {
        this.miUsuarioId = miUsuarioId;
    }

    public void setMensajes(List<Mensaje> mensajes) {
        this.mensajes = mensajes;
        notifyDataSetChanged();
    }

    public void addMensaje(Mensaje mensaje) {
        mensajes.add(mensaje);
        notifyItemInserted(mensajes.size() - 1);
    }

    @NonNull
    @Override
    public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mensaje, parent, false);
        return new MensajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);

        holder.tvTexto.setText(mensaje.getTexto());

        // Alinear según quien envió el mensaje
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.containerMensaje.getLayoutParams();

        if (mensaje.getEmisor() == miUsuarioId) {
            // Mensaje enviado (derecha, verde)
            params.gravity = Gravity.END;
            holder.containerMensaje.setBackgroundResource(R.drawable.bg_mensaje_enviado);
        } else {
            // Mensaje recibido (izquierda, gris)
            params.gravity = Gravity.START;
            holder.containerMensaje.setBackgroundResource(R.drawable.bg_mensaje_recibido);
        }

        holder.containerMensaje.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class MensajeViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerMensaje;
        TextView tvTexto;

        public MensajeViewHolder(@NonNull View itemView) {
            super(itemView);
            containerMensaje = itemView.findViewById(R.id.containerMensaje);
            tvTexto = itemView.findViewById(R.id.tvTexto);
        }
    }
}
