from rest_framework import serializers
from mensajes.models.mensaje_model import Mensaje
from usuarios.serializers.usuario_serializers import UsuarioSerializer


class MensajeSerializer(serializers.ModelSerializer):
    emisor_info = UsuarioSerializer(source='emisor', read_only=True)
    receptor_info = UsuarioSerializer(source='receptor', read_only=True)
    
    class Meta:
        model = Mensaje
        fields = [
            'mensaje_id',
            'emisor',
            'receptor',
            'texto',
            'fecha_envio',
            'leido',
            'emisor_info',
            'receptor_info'
        ]
        read_only_fields = ['mensaje_id', 'fecha_envio']


class ConversacionSerializer(serializers.Serializer):
    """Serializer para lista de conversaciones con último mensaje"""
    otro_usuario = UsuarioSerializer()
    ultimo_mensaje = MensajeSerializer()
    mensajes_no_leidos = serializers.IntegerField()
