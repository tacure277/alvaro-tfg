from django.contrib import admin
from comentarios.models.comentario_model import Comentario


@admin.register(Comentario)
class ComentarioAdmin(admin.ModelAdmin):
    list_display = ['comentario_id', 'usuario', 'idea', 'fecha_comentario','contenido']
    search_fields = ['contenido']
    list_filter = ['fecha_comentario']