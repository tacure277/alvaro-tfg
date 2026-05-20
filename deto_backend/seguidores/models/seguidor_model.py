from django.db import models
from usuarios.models.usuario_model import Usuario


class Seguidor(models.Model):
    seguimiento_id = models.AutoField(primary_key=True)

    fecha_seguimiento = models.DateTimeField(auto_now_add=True)

    seguidor = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name='siguiendo'
    )

    seguido = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name='seguidores'
    )

    class Meta:
        db_table = 'seguidores'
        unique_together = ('seguidor', 'seguido')
        verbose_name = 'Seguidor'
        verbose_name_plural = 'Seguidores'

    def save(self, *args, **kwargs):
        if self.seguidor == self.seguido:
            raise ValueError("No puedes seguirte a ti mismo")
        super().save(*args, **kwargs)

    def __str__(self):
        return f"{self.seguidor.nombre} sigue a {self.seguido.nombre}"