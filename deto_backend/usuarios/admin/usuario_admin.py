from django.contrib import admin
from usuarios.models.usuario_model import Usuario


@admin.register(Usuario)
class UsuarioAdmin(admin.ModelAdmin):
    list_display = ['usuario_id', 'nombre', 'correo', 'fecha_creacion']
    search_fields = ['nombre', 'correo','contenido']
    list_filter = ['fecha_creacion']
    fields = ['nombre', 'correo', 'contraseña', 'descripcion', 'foto_perfil']
