from django.db import models
from usuarios.models.usuario_model import Usuario
from ideas.models.idea_model import Idea


class Comentario(models.Model):
    comentario_id = models.AutoField(primary_key=True)
    contenido = models.TextField()
    fecha_comentario = models.DateTimeField(auto_now_add=True)

    usuario = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name='comentarios'
    )

    idea = models.ForeignKey(
        Idea,
        on_delete=models.CASCADE,
        related_name='comentarios'
    )

    comentario_padre = models.ForeignKey(
        'self',
        null=True,
        blank=True,
        on_delete=models.CASCADE,
        related_name='respuestas'
    )

    class Meta:
        db_table = 'comentarios'
        verbose_name = 'Comentario'
        verbose_name_plural = 'comentarios'
        ordering = ['fecha_comentario']

    def save(self, *args, **kwargs):
        if not self.contenido:
            raise ValueError("Contenido vacío")
        if not self.usuario_id:
            raise ValueError("Usuario requerido")
        if not self.idea_id:
            raise ValueError("Idea requerida")
        if self.comentario_padre and self.comentario_padre.comentario_padre:
            raise ValueError("No se puede responder a una respuesta")
        super().save(*args, **kwargs)

    def __str__(self):
        return f"[{self.comentario_id}] {self.usuario.nombre}: {self.contenido[:30]}..."