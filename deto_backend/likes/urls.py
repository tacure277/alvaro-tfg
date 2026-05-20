from django.urls import path
from likes.views import like_views

urlpatterns = [
    path('<int:idea_id>/', like_views.toggle_like, name='toggle_like'),
]