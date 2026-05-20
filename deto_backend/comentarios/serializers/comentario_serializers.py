from rest_framework import serializers
from comentarios.models.comentario_model import Comentario

class ComentarioSerializer(serializers.ModelSerializer):
    nombre_autor = serializers.SerializerMethodField()
    
    class Meta:
        model = Comentario
        fields = ['comentario_id', 'contenido', 'fecha_comentario', 'nombre_autor']  
    def get_nombre_autor(self, obj):
        return obj.usuario.nombre

    def get_autor(self, obj):
        return obj.usuario.nombre if obj.usuario else "Usuario"

    def get_foto_autor_url(self, obj):
        if obj.usuario and obj.usuario.foto_perfil:
            request = self.context.get('request')
            if request:
                return request.build_absolute_uri(obj.usuario.foto_perfil.url)
            return obj.usuario.foto_perfil.url
        return None