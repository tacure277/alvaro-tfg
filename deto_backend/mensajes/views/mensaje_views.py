from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from django.db.models import Q
from mensajes.models.mensaje_model import Mensaje
from mensajes.serializers.mensaje_serializers import MensajeSerializer, ConversacionSerializer
from usuarios.models.usuario_model import Usuario


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def lista_conversaciones(request):
    usuario_id = request.auth.payload.get('user_id')
    usuario_id = int(usuario_id) if usuario_id else None
    
    if not usuario_id:
        return Response({'error': 'No autenticado'}, status=401)
    
    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)
    
    usuarios_chateados = Mensaje.objects.filter(
        Q(emisor=usuario) | Q(receptor=usuario)
    ).values_list('emisor_id', 'receptor_id')
    
    ids_unicos = set()
    for emisor_id, receptor_id in usuarios_chateados:
        if emisor_id != usuario_id:
            ids_unicos.add(emisor_id)
        if receptor_id != usuario_id:
            ids_unicos.add(receptor_id)
    
    conversaciones = []
    
    for otro_usuario_id in ids_unicos:
        try:
            otro_usuario = Usuario.objects.get(usuario_id=otro_usuario_id)
            
            ultimo_mensaje = Mensaje.objects.filter(
                Q(emisor=usuario, receptor=otro_usuario) | 
                Q(emisor=otro_usuario, receptor=usuario)
            ).order_by('-fecha_envio').first()
            
            no_leidos = Mensaje.objects.filter(
                emisor=otro_usuario,
                receptor=usuario,
                leido=False
            ).count()
            
            if ultimo_mensaje:
                from usuarios.serializers.usuario_serializers import UsuarioSerializer
                conversaciones.append({
                    'otro_usuario': UsuarioSerializer(otro_usuario, context={'request': request}).data,
                    'ultimo_mensaje': MensajeSerializer(ultimo_mensaje, context={'request': request}).data,
                    'mensajes_no_leidos': no_leidos
                })
        except Usuario.DoesNotExist:
            continue
    
    conversaciones.sort(
        key=lambda x: x['ultimo_mensaje']['fecha_envio'],
        reverse=True
    )
    
    return Response(conversaciones, status=200)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def mensajes_con_usuario(request, otro_usuario_id):
    usuario_id = request.auth.payload.get('user_id')
    usuario_id = int(usuario_id) if usuario_id else None
    
    if not usuario_id:
        return Response({'error': 'No autenticado'}, status=401)
    
    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
        otro_usuario = Usuario.objects.get(usuario_id=otro_usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)
    
    mensajes = Mensaje.objects.filter(
        Q(emisor=usuario, receptor=otro_usuario) | 
        Q(emisor=otro_usuario, receptor=usuario)
    ).order_by('fecha_envio')
    
    Mensaje.objects.filter(
        emisor=otro_usuario,
        receptor=usuario,
        leido=False
    ).update(leido=True)
    
    serializer = MensajeSerializer(mensajes, many=True, context={'request': request})
    return Response(serializer.data, status=200)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def enviar_mensaje(request):
    usuario_id = request.auth.payload.get('user_id')
    usuario_id = int(usuario_id) if usuario_id else None
    
    if not usuario_id:
        return Response({'error': 'No autenticado'}, status=401)
    
    receptor_id = request.data.get('receptor_id')
    texto = request.data.get('texto')
    
    if not receptor_id:
        return Response({'error': 'receptor_id es obligatorio'}, status=400)
    
    if not texto or texto.strip() == '':
        return Response({'error': 'El texto no puede estar vacío'}, status=400)
    
    try:
        emisor = Usuario.objects.get(usuario_id=usuario_id)
        receptor = Usuario.objects.get(usuario_id=receptor_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)
    
    if emisor == receptor:
        return Response({'error': 'No puedes enviarte mensajes a ti mismo'}, status=400)
    
    mensaje = Mensaje.objects.create(
        emisor=emisor,
        receptor=receptor,
        texto=texto
    )
    
    serializer = MensajeSerializer(mensaje, context={'request': request})
    return Response(serializer.data, status=201)


@api_view(['PUT'])
@permission_classes([IsAuthenticated])
def marcar_leido(request, mensaje_id):
    usuario_id = request.auth.payload.get('user_id')
    usuario_id = int(usuario_id) if usuario_id else None
    
    if not usuario_id:
        return Response({'error': 'No autenticado'}, status=401)
    
    try:
        mensaje = Mensaje.objects.get(mensaje_id=mensaje_id)
        
        if int(mensaje.receptor.usuario_id) != usuario_id:
            return Response({'error': 'No tienes permiso'}, status=403)
        
        mensaje.leido = True
        mensaje.save()
        
        return Response({'mensaje': 'Marcado como leído'}, status=200)
    
    except Mensaje.DoesNotExist:
        return Response({'error': 'Mensaje no encontrado'}, status=404)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def mensajes_no_leidos_total(request):
    usuario_id = request.auth.payload.get('user_id')
    usuario_id = int(usuario_id) if usuario_id else None
    
    if not usuario_id:
        return Response({'error': 'No autenticado'}, status=401)
    
    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
        total = Mensaje.objects.filter(receptor=usuario, leido=False).count()
        return Response({'total_no_leidos': total}, status=200)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)