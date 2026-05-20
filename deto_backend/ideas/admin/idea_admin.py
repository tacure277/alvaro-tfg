from django.contrib import admin
from ideas.models.idea_model import Idea


@admin.register(Idea)
class IdeaAdmin(admin.ModelAdmin):
    list_display = ['idea_id', 'titulo', 'usuario', 'es_anonima', 'fecha_publicacion', 'descripcion']
    search_fields = ['titulo', 'descripcion']
    list_filter = ['es_anonima', 'fecha_publicacion']
    fields = ['titulo', 'descripcion', 'es_anonima', 'usuario', 'imagen']
