from django.urls import path
from usuarios.views import usuario_views

urlpatterns = [
    path('registro/', usuario_views.registro, name='registro'),
    path('login/', usuario_views.login, name='login'),

    path('perfil/', usuario_views.perfil, name='perfil'),
    path('perfil/<int:usuario_id>/', usuario_views.perfil_por_id, name='perfil_por_id'),
    path('perfil/editar/', usuario_views.editar_perfil, name='editar_perfil'),  # ✅ AÑADIR
    path('perfil/foto/', usuario_views.actualizar_foto_perfil, name='actualizar_foto'),

    path('cambiar-password/', usuario_views.cambiar_password, name='cambiar_password'),
    
    path('buscar/', usuario_views.buscar_usuarios, name='buscar_usuarios'),
]