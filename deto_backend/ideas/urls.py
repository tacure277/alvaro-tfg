from django.urls import path
from django.views.decorators.csrf import csrf_exempt
from ideas.views import idea_views
from comentarios.views import comentario_views

urlpatterns = [
    path('ideas/', idea_views.lista_ideas, name='lista_ideas'),
    path('ideas/crear/', idea_views.crear_idea, name='crear_idea'),
    path('ideas/mis-ideas/', idea_views.mis_ideas, name='mis_ideas'),
    path('ideas/<int:idea_id>/', idea_views.detalle_idea, name='detalle_idea'),
    path('ideas/<int:idea_id>/editar/', csrf_exempt(idea_views.editar_idea), name='editar_idea'),
    path('ideas/<int:idea_id>/eliminar/', csrf_exempt(idea_views.eliminar_idea), name='eliminar_idea'),
    
    path('ideas/<int:idea_id>/comentarios/', comentario_views.comentarios_idea, name='comentarios_idea'),
]