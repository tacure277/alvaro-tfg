from django.urls import path
from seguidores.views import seguidor_views

urlpatterns = [
    path('<int:usuario_id>/seguir/', seguidor_views.seguir, name='seguir'),
    path('<int:usuario_id>/dejar-seguir/', seguidor_views.dejar_seguir, name='dejar_seguir'),
    path('<int:usuario_id>/seguidores/', seguidor_views.lista_seguidores, name='lista_seguidores'),
    path('<int:usuario_id>/siguiendo/', seguidor_views.lista_siguiendo, name='lista_siguiendo'),
    path('<int:usuario_id>/verifica/<int:seguidor_id>/', seguidor_views.verificar_si_sigue, name='verificar_si_sigue'),
]