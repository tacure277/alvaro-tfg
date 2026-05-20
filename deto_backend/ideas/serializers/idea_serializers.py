from rest_framework import serializers
from ideas.models.idea_model import Idea
from usuarios.serializers.usuario_serializers import UsuarioSerializer


class IdeaSerializer(serializers.ModelSerializer):
    autor = serializers.SerializerMethodField()
    num_comentarios = serializers.SerializerMethodField()
    num_likes = serializers.SerializerMethodField()
    usuario_dio_like = serializers.SerializerMethodField()
    
    archivo_url = serializers.SerializerMethodField()
    tipo_archivo = serializers.CharField(read_only=True)
    
    imagen_url = serializers.SerializerMethodField()
    
    foto_autor_url = serializers.SerializerMethodField()
    autor_info = UsuarioSerializer(source='usuario', read_only=True)

    class Meta:
        model = Idea
        fields = [
            'idea_id', 'titulo', 'descripcion', 'es_anonima',
            'fecha_publicacion', 'usuario_id', 'autor',
            'num_comentarios', 'num_likes', 'usuario_dio_like',
            'archivo_url', 'tipo_archivo',  
            'imagen_url',  
            'foto_autor_url', 'autor_info'
        ]

    def get_autor(self, obj):
        if obj.es_anonima:
            return "Anónimo"
        return obj.usuario.nombre if obj.usuario else "Usuario"

    def get_num_comentarios(self, obj):
        return obj.comentarios.count()

    def get_num_likes(self, obj):
        return obj.likes.count()

    def get_usuario_dio_like(self, obj):
        usuario_id = self.context.get('usuario_id')
        if not usuario_id:
            return False
        return obj.likes.filter(usuario_id=usuario_id).exists()

    def get_archivo_url(self, obj):
        if obj.archivo:
            request = self.context.get('request')
            if request:
                return request.build_absolute_uri(obj.archivo.url)
            return obj.archivo.url
        return None
    
    def get_imagen_url(self, obj):
        if obj.tipo_archivo == 'imagen' and obj.archivo:
            request = self.context.get('request')
            if request:
                return request.build_absolute_uri(obj.archivo.url)
            return obj.archivo.url
        return None

    def get_foto_autor_url(self, obj):
        if obj.es_anonima or not obj.usuario:
            return None

        if obj.usuario.foto_perfil:
            request = self.context.get('request')
            if request:
                return request.build_absolute_uri(obj.usuario.foto_perfil.url)
            return obj.usuario.foto_perfil.url
        return None