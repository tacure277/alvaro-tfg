from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework import status
from seguidores.models.seguidor_model import Seguidor
from usuarios.models.usuario_model import Usuario


@api_view(['POST'])
def seguir(request, usuario_id):
    seguidor_id = request.data.get('seguidor_id')
    try:
        seguidor = Usuario.objects.get(usuario_id=seguidor_id)
        seguido = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)
 
    if seguidor == seguido:
        return Response({'error': 'No puedes seguirte a ti mismo'}, status=400)
 
    seguimiento_existente = Seguidor.objects.filter(seguidor=seguidor, seguido=seguido).first()
    if seguimiento_existente:
        return Response({'error': 'Ya sigues a este usuario'}, status=400)
 
    Seguidor.objects.create(seguidor=seguidor, seguido=seguido)
    return Response({'mensaje': 'Ahora sigues a este usuario'}, status=201)
 
 
@api_view(['DELETE'])
def dejar_seguir(request, usuario_id):
    seguidor_id = request.data.get('seguidor_id')
    try:
        seguidor = Usuario.objects.get(usuario_id=seguidor_id)
        seguido = Usuario.objects.get(usuario_id=usuario_id)
        seguimiento = Seguidor.objects.get(seguidor=seguidor, seguido=seguido)
        seguimiento.delete()
        return Response({'mensaje': 'Dejaste de seguir a este usuario'})
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)
    except Seguidor.DoesNotExist:
        return Response({'error': 'No seguías a este usuario'}, status=404)
 
 
@api_view(['GET'])
def lista_seguidores(request, usuario_id):
    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
        seguidores = Seguidor.objects.filter(seguido=usuario)
        data = [{'usuario_id': s.seguidor.usuario_id, 'nombre': s.seguidor.nombre} 
                for s in seguidores]
        return Response(data)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)
 
 
@api_view(['GET'])
def lista_siguiendo(request, usuario_id):
    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
        siguiendo = Seguidor.objects.filter(seguidor=usuario)
        data = [{'usuario_id': s.seguido.usuario_id, 'nombre': s.seguido.nombre} 
                for s in siguiendo]
        return Response(data)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)


@api_view(['GET'])
@permission_classes([AllowAny])
def verificar_si_sigue(request, usuario_id, seguidor_id):
 

    try:
        seguidor = Usuario.objects.get(usuario_id=seguidor_id)
        seguido = Usuario.objects.get(usuario_id=usuario_id)
        
        existe = Seguidor.objects.filter(
            seguidor=seguidor,
            seguido=seguido
        ).exists()
        
        return Response({'sigue': existe}, status=200)
    
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)