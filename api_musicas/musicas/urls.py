from django.urls import path
from. import views

urlpatterns = [
    path("musicas/", views.listar_musicas),
    path("tocar/<str:nome>/", views.tocar_musica),
]
