package com.alvaro.deto_android.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alvaro.deto_android.R;
import com.alvaro.deto_android.models.Comentario;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.ViewHolder> {

    private List<Comentario> comentarios = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comentario comentario = comentarios.get(position);


        String fotoAutor = comentario.getFoto_autor_url();
        if (fotoAutor != null && !fotoAutor.isEmpty()) {
            if (fotoAutor.contains("127.0.0.1")) {
                fotoAutor = fotoAutor.replace("127.0.0.1", "10.0.2.2");
            }

            Glide.with(holder.itemView.getContext())
                    .load(fotoAutor)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(holder.imgAvatarComentario);
        } else {
            holder.imgAvatarComentario.setImageResource(R.mipmap.ic_launcher);
        }

        holder.tvAutor.setText(comentario.getAutor());
        holder.tvContenido.setText(comentario.getContenido());
        holder.tvFecha.setText(comentario.getFecha_comentario());
    }
    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgAvatarComentario;
        TextView tvAutor;
        TextView tvContenido;
        TextView tvFecha;

        ViewHolder(View view) {
            super(view);
            imgAvatarComentario = view.findViewById(R.id.imgAvatarComentario);
            tvAutor = view.findViewById(R.id.tvAutorComentario);
            tvContenido = view.findViewById(R.id.tvContenidoComentario);
            tvFecha = view.findViewById(R.id.tvFechaComentario);
        }
    }
}