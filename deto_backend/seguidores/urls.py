from django.urls import path
from seguidores.views import seguidor_views

urlpatterns = [
    path('seguir/<int:usuario_id>/', seguidor_views.seguir, name='seguir'),
    path('dejar-seguir/<int:usuario_id>/', seguidor_views.dejar_seguir, name='dejar_seguir'),
    path('seguidores/<int:usuario_id>/', seguidor_views.lista_seguidores, name='lista_seguidores'),
    path('siguiendo/<int:usuario_id>/', seguidor_views.lista_siguiendo, name='lista_siguiendo'),
    
    path('<int:usuario_id>/verifica/<int:seguidor_id>/', seguidor_views.verificar_si_sigue, name='verificar_si_sigue'),
]