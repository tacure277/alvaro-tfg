from django.urls import path
from mensajes.views import mensaje_views

urlpatterns = [
    # Lista de conversaciones del usuario
    path('conversaciones/', mensaje_views.lista_conversaciones, name='lista_conversaciones'),
    
    # Mensajes con un usuario específico
    path('chat/<int:otro_usuario_id>/', mensaje_views.mensajes_con_usuario, name='mensajes_con_usuario'),
    
    # Enviar mensaje
    path('enviar/', mensaje_views.enviar_mensaje, name='enviar_mensaje'),
    
    # Marcar mensaje como leído
    path('<int:mensaje_id>/marcar-leido/', mensaje_views.marcar_leido, name='marcar_leido'),
    
    # Total de mensajes no leídos
    path('no-leidos/', mensaje_views.mensajes_no_leidos_total, name='mensajes_no_leidos_total'),
]
