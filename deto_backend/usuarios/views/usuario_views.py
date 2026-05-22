from django.contrib.auth.hashers import check_password, make_password
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from rest_framework_simplejwt.tokens import RefreshToken
import os
import time
from django.db.models import Q
 
from usuarios.models.usuario_model import Usuario
from usuarios.serializers.usuario_serializers import (
    RegistroSerializer,
    LoginSerializer,
    UsuarioSerializer
)


@api_view(['POST'])
@permission_classes([AllowAny])
def registro(request):
    serializer = RegistroSerializer(data=request.data)

    if not serializer.is_valid():
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    correo = serializer.validated_data['correo']

    if Usuario.objects.filter(correo=correo).exists():
        return Response(
            {'error': 'El correo ya está registrado'},
            status=status.HTTP_400_BAD_REQUEST
        )

    usuario = Usuario.objects.create(
        nombre=serializer.validated_data['nombre'],
        correo=correo,
        contraseña=make_password(serializer.validated_data['contraseña'])
    )

    refresh = RefreshToken.for_user(usuario)

    return Response({
        'access': str(refresh.access_token),
        'refresh': str(refresh),
        'usuario': UsuarioSerializer(usuario, context={'request': request}).data
    }, status=status.HTTP_201_CREATED)


@api_view(['POST'])
@permission_classes([AllowAny])
def login(request):

    serializer = LoginSerializer(data=request.data)

    if not serializer.is_valid():
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    correo = serializer.validated_data['correo']
    contraseña = serializer.validated_data['contraseña']

    try:
        usuario = Usuario.objects.get(correo=correo)

        password_valida = check_password(contraseña, usuario.contraseña)
        if not password_valida:
            return Response(
                {'error': 'Correo o contraseña incorrectos'},
                status=status.HTTP_401_UNAUTHORIZED
            )

        refresh = RefreshToken.for_user(usuario)

        return Response({
            'access': str(refresh.access_token),
            'refresh': str(refresh),
            'usuario': UsuarioSerializer(usuario, context={'request': request}).data
        }, status=status.HTTP_200_OK)

    except Usuario.DoesNotExist:
        return Response(
            {'error': 'Correo o contraseña incorrectos'},
            status=status.HTTP_401_UNAUTHORIZED
        )


@api_view(['GET', 'PUT'])
@permission_classes([IsAuthenticated])
def perfil(request):

    usuario_id = request.auth.payload.get('user_id')

    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)

    if request.method == 'GET':
        serializer = UsuarioSerializer(usuario, context={'request': request})
        return Response(serializer.data, status=200)

    elif request.method == 'PUT':
        nombre = request.data.get('nombre')
        descripcion = request.data.get('descripcion')

        if nombre:
            usuario.nombre = nombre
        if descripcion is not None:
            usuario.descripcion = descripcion

        usuario.save()

        serializer = UsuarioSerializer(usuario, context={'request': request})
        return Response(serializer.data, status=200)


@api_view(['GET'])
@permission_classes([AllowAny])
def perfil_por_id(request, usuario_id):

    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
        serializer = UsuarioSerializer(usuario, context={'request': request})
        return Response(serializer.data, status=status.HTTP_200_OK)
    except Usuario.DoesNotExist:
        return Response(
            {'error': 'Usuario no encontrado'},
            status=status.HTTP_404_NOT_FOUND
        )


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def actualizar_foto_perfil(request):

    usuario_id = request.auth.payload.get('user_id')

    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)

    if 'foto' not in request.FILES:
        return Response({'error': 'No se proporcionó ninguna imagen'}, status=400)

    foto = request.FILES['foto']

    if not foto.content_type.startswith('image/'):
        return Response({'error': 'El archivo no es una imagen válida'}, status=400)

    if usuario.foto_perfil:
        if os.path.isfile(usuario.foto_perfil.path):
            print(f"🗑️ Eliminando foto anterior: {usuario.foto_perfil.path}")
            os.remove(usuario.foto_perfil.path)

    timestamp = int(time.time())
    extension = os.path.splitext(foto.name)[1]
    nombre_archivo = f"usuarios/{usuario.usuario_id}_{timestamp}{extension}"

    usuario.foto_perfil.save(nombre_archivo, foto)

    serializer = UsuarioSerializer(usuario, context={'request': request})

    return Response(serializer.data, status=200)


@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
def eliminar_foto_perfil(request):
    usuario_id = request.auth.payload.get('user_id')

    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)

    if usuario.foto_perfil:
        if os.path.isfile(usuario.foto_perfil.path):
            os.remove(usuario.foto_perfil.path)
        usuario.foto_perfil = None
        usuario.save()
        return Response({'mensaje': 'Foto eliminada correctamente'}, status=200)
    else:
        return Response({'error': 'El usuario no tiene foto de perfil'}, status=400)


@api_view(['PUT'])
@permission_classes([IsAuthenticated])
def actualizar_perfil(request):

    usuario_id = request.auth.payload.get('user_id')

    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)

    nombre = request.data.get('nombre')
    descripcion = request.data.get('descripcion')

    if nombre:
        usuario.nombre = nombre
    if descripcion is not None:
        usuario.descripcion = descripcion

    usuario.save()

    serializer = UsuarioSerializer(usuario, context={'request': request})
    return Response(serializer.data, status=200)


@api_view(['GET'])
@permission_classes([IsAuthenticated])
def buscar_usuarios(request):
    query = request.query_params.get('q', '').strip()
    
    if not query:
        usuarios = Usuario.objects.all()[:50]
    else:
        usuarios = Usuario.objects.filter(
            Q(nombre__icontains=query) | Q(correo__icontains=query)
        )[:20]
    
    serializer = UsuarioSerializer(usuarios, many=True, context={'request': request})
    return Response(serializer.data)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def cambiar_password(request):
    usuario_id = request.auth.payload.get('user_id')

    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)

    password_actual = request.data.get('password_actual')
    nueva_password = request.data.get('nueva_password')

    if not password_actual or not nueva_password:
        return Response(
            {'error': 'Todos los campos son requeridos'},
            status=400
        )

    if not check_password(password_actual, usuario.contraseña):
        return Response(
            {'error': 'Contraseña actual incorrecta'},
            status=401
        )

    if len(nueva_password) < 6:
        return Response(
            {'error': 'La nueva contraseña debe tener al menos 6 caracteres'},
            status=400
        )

    usuario.contraseña = make_password(nueva_password)
    usuario.save()

    return Response({'mensaje': 'Contraseña actualizada correctamente'}, status=200)


@api_view(['POST'])
@permission_classes([IsAuthenticated])
def editar_perfil(request):
    usuario_id = request.auth.payload.get('user_id')
    
    try:
        usuario = Usuario.objects.get(usuario_id=usuario_id)
    except Usuario.DoesNotExist:
        return Response({'error': 'Usuario no encontrado'}, status=404)
    
    if 'nombre' in request.data:
        usuario.nombre = request.data['nombre']
    
    if 'descripcion' in request.data:
        usuario.descripcion = request.data['descripcion']
    
    if 'foto_perfil' in request.FILES:
        foto = request.FILES['foto_perfil']
        
        if usuario.foto_perfil:
            if os.path.isfile(usuario.foto_perfil.path):
                os.remove(usuario.foto_perfil.path)
        
        timestamp = int(time.time())
        extension = os.path.splitext(foto.name)[1]
        nombre_archivo = f"usuarios/{usuario.usuario_id}_{timestamp}{extension}"
        usuario.foto_perfil.save(nombre_archivo, foto)
    
    usuario.save()
    
    serializer = UsuarioSerializer(usuario, context={'request': request})
    return Response(serializer.data, status=200)