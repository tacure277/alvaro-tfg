from django.db import models
from django.contrib.auth.models import AbstractBaseUser, BaseUserManager, PermissionsMixin
from django.utils import timezone


class UsuarioManager(BaseUserManager):
    def create_user(self, correo, nombre, contraseña=None, **extra_fields):
        if not correo:
            raise ValueError('El correo es obligatorio')
        if not nombre:
            raise ValueError('El nombre es obligatorio')

        correo = self.normalize_email(correo)
        usuario = self.model(
            correo=correo,
            nombre=nombre,
            **extra_fields
        )
        usuario.set_password(contraseña)
        usuario.save(using=self._db)
        return usuario

    def create_superuser(self, correo, nombre, contraseña=None, **extra_fields):
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)

        return self.create_user(correo, nombre, contraseña, **extra_fields)


class Usuario(AbstractBaseUser, PermissionsMixin):
    usuario_id = models.AutoField(primary_key=True)
    nombre = models.CharField(max_length=30)
    correo = models.CharField(max_length=100, unique=True)
    contraseña = models.CharField(max_length=128)
    descripcion = models.TextField(null=True, blank=True)
    fecha_creacion = models.DateTimeField(auto_now_add=True)
    foto_perfil = models.ImageField(upload_to="usuarios/", null=True, blank=True)

    is_active = models.BooleanField(default=True)
    is_staff = models.BooleanField(default=False)
    date_joined = models.DateTimeField(default=timezone.now)

    USERNAME_FIELD = 'correo'
    REQUIRED_FIELDS = ['nombre']

    objects = UsuarioManager()

    class Meta:
        db_table = 'usuarios'
        verbose_name = 'Usuario'
        verbose_name_plural = 'usuarios'

    def save(self, *args, **kwargs):
        if not self.nombre:
            raise ValueError("Nombre vacío")
        if not self.correo:
            raise ValueError("Correo vacío")
        if "@" not in self.correo:
            raise ValueError("Correo no válido")

        super().save(*args, **kwargs)

    def __str__(self):
        return f"[{self.usuario_id}] {self.nombre} ({self.correo})"

    @property
    def id(self):
        return self.usuario_id