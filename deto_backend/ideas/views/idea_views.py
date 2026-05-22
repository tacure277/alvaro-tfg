import os
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from ideas.serializers.idea_serializers import IdeaSerializer
from usuarios.models.usuario_model import Usuario
from ideas.models.idea_model import Idea


@api_view(['GET'])
@permission_classes([AllowAny])
def lista_ideas(request):
    usuario_id_context = request.query_params.get('usuario_id')
    ideas = Idea.objects.all().order_by('-fecha_publicacion')
    serializer = IdeaSerializer(
        ideas, 
        many=True, 
        context={'request': request, 'usuario_id': usuario_id_context}
    )
    return Response(serializer.data)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def crear_idea(request):
    usuario_id = request.auth.payload.get('user_id')

    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)

    es_anonima = request.data.get('es_anonima', False)

    if isinstance(es_anonima, str):
        es_anonima = es_anonima.lower() == 'true'

    # ✅ CORREGIDO: Acepta 'archivo' (nuevo) o 'imagen' (retrocompatibilidad)
    archivo = request.FILES.get('archivo') or request.FILES.get('imagen')

    try:
        idea = Idea.objects.create(
            titulo=request.data.get('titulo'),
            descripcion=request.data.get('descripcion'),
            es_anonima=es_anonima,
            archivo=archivo,
            usuario=usuario
        )
    except Exception as e:
        return Response({'error': str(e)}, status=500)

    serializer = IdeaSerializer(idea, context={'request': request, 'usuario_id': usuario_id})
    return Response(serializer.data, status=201)


@api_view(['GET'])
@permission_classes([AllowAny])
def detalle_idea(request, idea_id):
    usuario_id_context = request.query_params.get('usuario_id')
    
    try:
        idea = Idea.objects.get(idea_id=idea_id)
        serializer = IdeaSerializer(idea, context={'request': request, 'usuario_id': usuario_id_context})
        return Response(serializer.data)
    except Idea.DoesNotExist:
        return Response({'error': 'Idea no encontrada'}, status=404)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def mis_ideas(request):
    usuario_id = request.auth.payload.get('user_id')
    
    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)

    ideas = Idea.objects.filter(usuario=usuario).order_by('-fecha_publicacion')
    serializer = IdeaSerializer(ideas, many=True, context={'request': request, 'usuario_id': usuario_id})
    return Response(serializer.data, status=200)


@api_view(['PUT'])
@permission_classes([IsAuthenticated])
def editar_idea(request, idea_id):
    usuario_id = request.auth.payload.get('user_id')
    usuario_id = int(usuario_id) if usuario_id else None
    
    if not usuario_id:
        return Response({'error': 'No autenticado'}, status=401)
    
    try:
        idea = Idea.objects.get(idea_id=idea_id)
        
        if int(idea.usuario.usuario_id) != int(usuario_id):
            return Response({'error': 'No puedes editar ideas de otros usuarios'}, status=403)
        
        if 'titulo' in request.data:
            idea.titulo = request.data['titulo']
        
        if 'descripcion' in request.data:
            idea.descripcion = request.data['descripcion']
        
        # ✅ CORREGIDO: Acepta 'archivo' o 'imagen'
        nuevo_archivo = request.FILES.get('archivo') or request.FILES.get('imagen')
        
        if nuevo_archivo:
            if idea.archivo:
                try:
                    if os.path.isfile(idea.archivo.path):
                        os.remove(idea.archivo.path)
                except:
                    pass
            
            idea.archivo = nuevo_archivo
        
        idea.save()
        
        serializer = IdeaSerializer(idea, context={'request': request, 'usuario_id': usuario_id})
        return Response(serializer.data, status=200)
    
    except Idea.DoesNotExist:
        return Response({'error': 'Idea no encontrada'}, status=404)
    except Exception as e:
        return Response({'error': str(e)}, status=500)


@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
def eliminar_idea(request, idea_id):
    usuario_id = request.auth.payload.get('user_id')
    usuario_id = int(usuario_id) if usuario_id else None
    
    if not usuario_id:
        return Response({'error': 'No autenticado'}, status=401)
    
    try:
        idea = Idea.objects.get(idea_id=idea_id)
        
        if int(idea.usuario.usuario_id) != int(usuario_id):
            return Response({'error': 'No puedes eliminar ideas de otros usuarios'}, status=403)
        
        if idea.archivo:
            try:
                if os.path.isfile(idea.archivo.path):
                    os.remove(idea.archivo.path)
            except:
                pass
        
        idea.delete()
        return Response({'mensaje': 'Idea eliminada correctamente'}, status=200)
    
    except Idea.DoesNotExist:
        return Response({'error': 'Idea no encontrada'}, status=404)
    except Exception as e:
        return Response({'error': str(e)}, status=500)