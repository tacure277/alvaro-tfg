package com.alvaro.deto_android.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alvaro.deto_android.R;
import com.alvaro.deto_android.models.Idea;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class IdeasAdapter extends RecyclerView.Adapter<IdeasAdapter.IdeaViewHolder> {

    private List<Idea> ideas = new ArrayList<>();
    private OnIdeaClickListener listener;

    public interface OnIdeaClickListener {
        void onIdeaClick(Idea idea);
        void onAutorClick(int usuarioId, String nombre);
        void onLikeClick(Idea idea, int position);
        void onEditarClick(Idea idea, int position);     // ✅ NUEVO
        void onEliminarClick(Idea idea, int position);   // ✅ NUEVO
    }

    public IdeasAdapter(OnIdeaClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public IdeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_idea, parent, false);
        return new IdeaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IdeaViewHolder holder, int position) {
        Idea idea = ideas.get(position);

        // Foto del autor
        String fotoAutor = idea.getFoto_autor_url();
        if (fotoAutor != null && !fotoAutor.isEmpty()) {
            if (fotoAutor.contains("127.0.0.1")) {
                fotoAutor = fotoAutor.replace("127.0.0.1", "10.0.2.2");
            }
            Glide.with(holder.itemView.getContext())
                    .load(fotoAutor)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.mipmap.ic_launcher);
        }

        String imagenIdea = idea.getImagen_url();
        if (imagenIdea != null && !imagenIdea.isEmpty()) {
            if (imagenIdea.contains("127.0.0.1")) {
                imagenIdea = imagenIdea.replace("127.0.0.1", "10.0.2.2");
            }
            holder.imgIdea.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(imagenIdea)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(holder.imgIdea);
        } else {
            holder.imgIdea.setVisibility(View.GONE);
        }

        if (idea.isEs_anonima()) {
            holder.tvAutor.setText("Anónimo");
            holder.chipAnonimo.setVisibility(View.VISIBLE);
            holder.tvAutor.setClickable(false);
        } else {
            holder.tvAutor.setText(idea.getAutor() != null ? idea.getAutor() : "Usuario");
            holder.chipAnonimo.setVisibility(View.GONE);
            holder.tvAutor.setClickable(true);
            holder.tvAutor.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAutorClick(idea.getUsuario_id(), idea.getAutor());
                }
            });
        }

        holder.tvTitulo.setText(idea.getTitulo() != null ? idea.getTitulo() : "");
        holder.tvDescripcion.setText(idea.getDescripcion() != null ? idea.getDescripcion() : "");
        holder.tvNumComentarios.setText(idea.getNum_comentarios() + " comentarios");
        holder.tvNumLikes.setText(String.valueOf(idea.getNum_likes()));

        if (idea.isUsuario_dio_like()) {
            holder.btnLike.setImageResource(android.R.drawable.star_big_on);
            holder.btnLike.setColorFilter(Color.parseColor("#FFD700")); // Dorado
        } else {
            holder.btnLike.setImageResource(android.R.drawable.star_big_off);
            holder.btnLike.setColorFilter(Color.parseColor("#9E9E9E")); // Gris
        }

        holder.btnLike.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(idea, position);
            }
        });

        // ✅ NUEVO: Mostrar botones solo si es mi idea
        SharedPreferences prefs = holder.itemView.getContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int miUsuarioId = prefs.getInt("usuario_id", 0);

        if (idea.getUsuario_id() == miUsuarioId && !idea.isEs_anonima()) {
            holder.layoutAcciones.setVisibility(View.VISIBLE);

            holder.btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditarClick(idea, position);
                }
            });

            holder.btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarClick(idea, position);
                }
            });
        } else {
            holder.layoutAcciones.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIdeaClick(idea);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ideas.size();
    }

    public void setIdeas(List<Idea> ideas) {
        this.ideas = ideas;
        notifyDataSetChanged();
    }

    // ✅ NUEVO: Métodos para modificar lista
    public void removeIdea(int position) {
        ideas.remove(position);
        notifyItemRemoved(position);
    }

    public void updateIdea(int position, Idea nuevaIdea) {
        ideas.set(position, nuevaIdea);
        notifyItemChanged(position);
    }

    static class IdeaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvAutor;
        TextView tvTitulo;
        TextView tvDescripcion;
        TextView tvNumComentarios;
        TextView tvNumLikes;
        ImageView btnLike;
        Chip chipAnonimo;
        ImageView imgIdea;
        LinearLayout layoutAcciones;
        ImageButton btnEditar;
        ImageButton btnEliminar;

        public IdeaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvNumComentarios = itemView.findViewById(R.id.tvNumComentarios);
            tvNumLikes = itemView.findViewById(R.id.tvNumLikes);
            btnLike = itemView.findViewById(R.id.btnLike);
            chipAnonimo = itemView.findViewById(R.id.chipAnonimo);
            imgIdea = itemView.findViewById(R.id.imgIdea);
            layoutAcciones = itemView.findViewById(R.id.layoutAcciones);     // ✅ NUEVO
            btnEditar = itemView.findViewById(R.id.btnEditar);               // ✅ NUEVO
            btnEliminar = itemView.findViewById(R.id.btnEliminar);           // ✅ NUEVO
        }
    }
}