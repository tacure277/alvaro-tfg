from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from likes.models.like_model import Like
from usuarios.models.usuario_model import Usuario
from ideas.models.idea_model import Idea


@api_view(['POST', 'DELETE'])
def toggle_like(request, idea_id):
    usuario_id = request.data.get('usuario_id')
    
    if not usuario_id:
        return Response({'error': 'usuario_id requerido'}, status=400)
    
    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
        idea = Idea.objects.get(idea_id=idea_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)
    except Idea.DoesNotExist:
        return Response({'error': 'Idea no encontrada'}, status=404)
    
    if request.method == 'POST':
        if Like.objects.filter(usuario=usuario, idea=idea).exists():
            return Response({'error': 'Ya diste like a esta idea'}, status=400)
        
        Like.objects.create(usuario=usuario, idea=idea)
        return Response({
            'mensaje': 'Like añadido', 
            'num_likes': idea.likes.count()
        }, status=201)
    
    elif request.method == 'DELETE':
        try:
            like = Like.objects.get(usuario=usuario, idea=idea)
            like.delete()
            return Response({
                'mensaje': 'Like eliminado', 
                'num_likes': idea.likes.count()
            }, status=200)
        except Like.DoesNotExist:
            return Response({'error': 'No habías dado like'}, status=404)