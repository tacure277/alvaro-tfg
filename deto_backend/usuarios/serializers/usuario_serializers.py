from rest_framework import serializers
from usuarios.models.usuario_model import Usuario

class UsuarioSerializer(serializers.ModelSerializer):
    foto_perfil_url = serializers.SerializerMethodField()
    num_seguidores = serializers.SerializerMethodField()
    num_siguiendo = serializers.SerializerMethodField()
    num_ideas = serializers.SerializerMethodField()
    lo_sigo = serializers.SerializerMethodField()  # ✅ NUEVO

    class Meta:
        model = Usuario
        fields = ['usuario_id', 'nombre', 'correo', 'descripcion', 'fecha_creacion', 
                  'foto_perfil_url', 'num_seguidores', 'num_siguiendo', 'num_ideas', 'lo_sigo']  # ✅ AÑADIDO
    
    def get_foto_perfil_url(self, obj):
        if obj.foto_perfil:
            request = self.context.get('request')
            return request.build_absolute_uri(obj.foto_perfil.url)
        return None
    
    def get_num_seguidores(self, obj):
        return obj.seguidores.count()
    
    def get_num_siguiendo(self, obj):
        return obj.siguiendo.count()
    
    def get_num_ideas(self, obj):
        return obj.ideas.count()
    
    def get_lo_sigo(self, obj):
        usuario_id = self.context.get('usuario_id')
        if not usuario_id:
            return False
        
        from seguidores.models.seguidor_model import Seguidor
        return Seguidor.objects.filter(seguidor_id=usuario_id, seguido=obj).exists()


class RegistroSerializer(serializers.ModelSerializer):
    class Meta:
        model = Usuario
        fields = ['nombre', 'correo', 'contraseña']
        extra_kwargs = {
            'contraseña': {'write_only': True}
        }

    def validate_nombre(self, value):
        if len(value) < 3:
            raise serializers.ValidationError("El nombre debe tener al menos 3 caracteres")
        return value

    def validate_correo(self, value):
        if not value.endswith('@gmail.com'):
            raise serializers.ValidationError("El correo debe ser @gmail.com")
        return value

    def validate_contraseña(self, value):
        if len(value) < 8:
            raise serializers.ValidationError("La contraseña debe tener al menos 8 caracteres")
        if not any(char.isdigit() for char in value):
            raise serializers.ValidationError("La contraseña debe tener al menos un número")
        return value


class LoginSerializer(serializers.Serializer):
    correo = serializers.EmailField()
    contraseña = serializers.CharField()