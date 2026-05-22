from django.urls import path
from comentarios.views import comentario_views

urlpatterns = [
    path('comentarios/<int:comentario_id>/editar/', comentario_views.editar_comentario, name='editar_comentario'),
    path('comentarios/<int:comentario_id>/eliminar/', comentario_views.eliminar_comentario, name='eliminar_comentario'),
]