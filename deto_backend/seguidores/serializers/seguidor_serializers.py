from rest_framework import serializers
from seguidores.models.seguidor_model import Seguidor

class SeguidorSerializer(serializers.ModelSerializer):
    seguidor_nombre = serializers.CharField(source='seguidor.nombre', read_only=True)
    seguido_nombre = serializers.CharField(source='seguido.nombre', read_only=True)

    class Meta:
        model = Seguidor
        fields = ['seguidor_id', 'seguidor', 'seguido', 'fecha_seguimiento', 
                  'seguidor_nombre', 'seguido_nombre']
  