#from django.shortcuts import render

from django.http import JsonResponse, FileResponse
import os
import string

def encontrar_pasta_musicas():

    for letra in string.ascii_uppercase:

        unidade = f"{letra}:/"
        caminho = os.path.join(unidade, "musicas")

        if os.path.exists(caminho):
            return caminho

    return None


def listar_musicas(request):

    pasta = encontrar_pasta_musicas()

    if pasta is None:
        return JsonResponse({
            "erro": "Nenhuma pasta 'musicas' encontrada nos dispositivos"
        })

    arquivos = os.listdir(pasta)

    musicas = [f for f in arquivos if f.endswith(".mp3")]

    return JsonResponse({
        "pasta_encontrada": pasta,
        "musicas": musicas
    })
    
def tocar_musica(request, nome):

    pasta = encontrar_pasta_musicas()

    if pasta is None:
        return JsonResponse({"erro": "Pasta não encontrada"})

    caminho = os.path.join(pasta, nome)

    if not os.path.exists(caminho):
        return JsonResponse({"erro": "Música não encontrada"})

    return FileResponse(open(caminho, "rb"), content_type="audio/mpeg")