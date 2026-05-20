from django.db import models
from usuarios.models.usuario_model import Usuario


class Mensaje(models.Model):
    mensaje_id = models.AutoField(primary_key=True)
    emisor = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name='mensajes_enviados'
    )
    receptor = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name='mensajes_recibidos'
    )
    texto = models.TextField()
    fecha_envio = models.DateTimeField(auto_now_add=True)
    leido = models.BooleanField(default=False)

    class Meta:
        db_table = 'mensajes'
        verbose_name = 'Mensaje'
        verbose_name_plural = 'Mensajes'
        ordering = ['fecha_envio']

    def __str__(self):
        return f"De {self.emisor.nombre} a {self.receptor.nombre}: {self.texto[:30]}"
