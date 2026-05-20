from django.db import models
from django.core.exceptions import ValidationError
from usuarios.models.usuario_model import Usuario
import os


def validate_file_extension(value):
    ext = os.path.splitext(value.name)[1].lower()
    valid_extensions = [
        # Imágenes
        '.jpg', '.jpeg', '.png', '.gif', '.webp',
        # Documentos
        '.pdf',
        # Videos
        '.mp4', '.mov', '.avi', '.mkv', '.webm'
    ]
    if ext not in valid_extensions:
        raise ValidationError(f'Formato no permitido. Usa: {", ".join(valid_extensions)}')


def validate_file_size(value):
    """Validar tamaño máximo según tipo"""
    filesize = value.size
    ext = os.path.splitext(value.name)[1].lower()
    
    # Límites por tipo
    if ext in ['.jpg', '.jpeg', '.png', '.gif', '.webp']:
        max_size = 5 * 1024 * 1024  
    elif ext == '.pdf':
        max_size = 10 * 1024 * 1024  
    elif ext in ['.mp4', '.mov', '.avi', '.mkv', '.webm']:
        max_size = 50 * 1024 * 1024  
    else:
        max_size = 10 * 1024 * 1024  
    
    if filesize > max_size:
        max_mb = max_size / (1024 * 1024)
        raise ValidationError(f'Tamaño máximo permitido: {max_mb}MB')


class Idea(models.Model):
    TIPO_ARCHIVO_CHOICES = [
        ('imagen', 'Imagen'),
        ('documento', 'Documento'),
        ('video', 'Video'),
        ('ninguno', 'Sin archivo'),
    ]
    
    idea_id = models.AutoField(primary_key=True)
    titulo = models.CharField(max_length=100)
    descripcion = models.TextField()
    es_anonima = models.BooleanField(default=False)
    fecha_publicacion = models.DateTimeField(auto_now_add=True)
    
    archivo = models.FileField(
        upload_to="ideas/",
        null=True,
        blank=True,
        validators=[validate_file_extension, validate_file_size]
    )
    
    tipo_archivo = models.CharField(
        max_length=20,
        choices=TIPO_ARCHIVO_CHOICES,
        default='ninguno'
    )
    
    @property
    def imagen(self):
        return self.archivo if self.tipo_archivo == 'imagen' else None
    
    usuario = models.ForeignKey(
        Usuario,
        on_delete=models.CASCADE,
        related_name='ideas'
    )

    class Meta:
        db_table = 'ideas'
        verbose_name = 'Idea'
        verbose_name_plural = 'ideas'
        ordering = ['-fecha_publicacion']

    def save(self, *args, **kwargs):
        if not self.titulo:
            raise ValueError("Título vacío")
        if not self.descripcion:
            raise ValueError("Descripción vacía")
        if not self.usuario_id:
            raise ValueError("Usuario requerido")
        
        if self.archivo:
            ext = os.path.splitext(self.archivo.name)[1].lower()
            if ext in ['.jpg', '.jpeg', '.png', '.gif', '.webp']:
                self.tipo_archivo = 'imagen'
            elif ext == '.pdf':
                self.tipo_archivo = 'documento'
            elif ext in ['.mp4', '.mov', '.avi', '.mkv', '.webm']:
                self.tipo_archivo = 'video'
        else:
            self.tipo_archivo = 'ninguno'
        
        super().save(*args, **kwargs)

    def __str__(self):
        return f"[{self.idea_id}] {self.titulo}"