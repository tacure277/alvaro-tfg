from django.contrib import admin
from django.urls import path, include

from deto_backend import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),
    path('auth/', include('usuarios.urls')),
    
    # ✅ RUTAS ESPECÍFICAS PRIMERO
    path('likes/', include('likes.urls')),
    path('seguidores/', include('seguidores.urls')),
    path('mensajes/', include('mensajes.urls')),
    
    # ⚠️ RUTAS GENÉRICAS AL FINAL
    path('', include('ideas.urls')),
    path('', include('comentarios.urls')),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)