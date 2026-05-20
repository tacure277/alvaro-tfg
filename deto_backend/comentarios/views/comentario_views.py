from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from comentarios.models.comentario_model import Comentario
from comentarios.serializers.comentario_serializers import ComentarioSerializer
from ideas.models.idea_model import Idea
from usuarios.models.usuario_model import Usuario


@api_view(['GET', 'POST'])
@permission_classes([AllowAny])
def comentarios_idea(request, idea_id):
    
    try:
        idea = Idea.objects.get(idea_id=idea_id)
    except Idea.DoesNotExist:
        return Response({'error': 'Idea no encontrada'}, status=404)

    if request.method == 'GET':
        try:
            comentarios = Comentario.objects.filter(idea=idea).order_by('-fecha_comentario')
            serializer = ComentarioSerializer(comentarios, many=True, context={'request': request})
            return Response(serializer.data, status=200)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    elif request.method == 'POST':
        
        usuario_id = None
        if request.auth and hasattr(request.auth, 'payload'):
            usuario_id = request.auth.payload.get('user_id')
            usuario_id = int(usuario_id) if usuario_id else None
        
        if not usuario_id:
            usuario_id = request.data.get('usuario_id')
            usuario_id = int(usuario_id) if usuario_id else None
        
        if not usuario_id:
            return Response({'error': 'Debes iniciar sesión'}, status=401)
        
        try:
            usuario = Usuario.objects.get(usuario_id=usuario_id)
        except Usuario.DoesNotExist:
            return Response({'error': 'Usuario no encontrado'}, status=404)

        texto = request.data.get('contenido')
        
        if not texto or texto.strip() == '':
            return Response({'error': 'El texto es obligatorio'}, status=400)

        try:
            comentario = Comentario.objects.create(
                contenido=texto,
                usuario=usuario,
                idea=idea
            )
            serializer = ComentarioSerializer(comentario, context={'request': request})
            return Response(serializer.data, status=201)
        
        except Exception as e:
            return Response({'error': str(e)}, status=500)
    
    return Response({'error': 'Método no permitido'}, status=405)


@api_view(['PUT'])
@permission_classes([IsAuthenticated])
def editar_comentario(request, comentario_id):
    
    usuario_id = request.auth.payload.get('user_id')
    usuario_id = int(usuario_id) if usuario_id else None
    
    if not usuario_id:
        return Response({'error': 'No autenticado'}, status=401)
    
    try:
        comentario = Comentario.objects.get(comentario_id=comentario_id)
        
        if int(comentario.usuario.usuario_id) != int(usuario_id):
            return Response({'error': 'No puedes editar comentarios de otros usuarios'}, status=403)
        
        texto = request.data.get('contenido')
        if not texto or texto.strip() == '':
            return Response({'error': 'El texto es obligatorio'}, status=400)
        
        comentario.contenido = texto
        comentario.save()
        
        serializer = ComentarioSerializer(comentario, context={'request': request})
        return Response(serializer.data, status=200)
    
    except Comentario.DoesNotExist:
        return Response({'error': 'Comentario no encontrado'}, status=404)
    except Exception as e:
        return Response({'error': str(e)}, status=500)


@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
def eliminar_comentario(request, comentario_id):
    
    usuario_id = request.auth.payload.get('user_id')
    usuario_id = int(usuario_id) if usuario_id else None
    
    if not usuario_id:
        return Response({'error': 'No autenticado'}, status=401)
    
    try:
        comentario = Comentario.objects.get(comentario_id=comentario_id)
        
        if int(comentario.usuario.usuario_id) != int(usuario_id):
            return Response({'error': 'No puedes eliminar comentarios de otros usuarios'}, status=403)
        
        comentario.delete()
        return Response({'mensaje': 'Comentario eliminado correctamente'}, status=200)
    
    except Comentario.DoesNotExist:
        return Response({'error': 'Comentario no encontrado'}, status=404)
    except Exception as e:
        return Response({'error': str(e)}, status=500)