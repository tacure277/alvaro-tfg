from rest_framework import serializers
from likes.models.like_model import Like

class LikeSerializer(serializers.ModelSerializer):
    usuario_nombre = serializers.CharField(source='usuario.nombre', read_only=True)
    idea_titulo = serializers.CharField(source='idea.titulo', read_only=True)

    class Meta:
        model = Like
        fields = ['like_id', 'usuario_id', 'idea_id', 'fecha_like', 'usuario_nombre', 'idea_titulo']