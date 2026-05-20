from django.db import models
from usuarios.models.usuario_model import Usuario
from ideas.models.idea_model import Idea

class Like(models.Model):
    like_id = models.AutoField(primary_key=True)
    fecha_like = models.DateTimeField(auto_now_add=True)

    usuario = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name='likes'
    )

    idea = models.ForeignKey(
        Idea,
        on_delete=models.CASCADE,
        related_name='likes'
    )

    class Meta:
        db_table = 'likes'
        unique_together = ('usuario', 'idea')  
        verbose_name = 'Like'
        verbose_name_plural = 'Likes'

    def __str__(self):
        return f"{self.usuario.nombre} → Idea {self.idea.idea_id}"
