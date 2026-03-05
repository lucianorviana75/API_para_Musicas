import os
import string
import time

def listear_unidades():
    unidades = []
    for letra in string.ascii_uppercase:
        caminho = f"{letra}:/"
        if os.path.exists(caminho):
            unidades.append(caminho)
    return unidades

unidades_anteriores = listear_unidades()

print("Monitorando pendrives...")

while True:
    
    unidades_atuais = listear_unidades()
    novas = set(unidades_atuais) - set(unidades_anteriores)
    if novas:
        for unidade in novas:
            print("Novo dispositivo conectado:", unidade)
            pasta = os.path.join(unidade, "musicas")
            if os.path.exists(pasta):
                print("🎵 Pasta de músicas encontrada:",pasta)
                
    unidades_anteriores = unidades_atuais
    
    time.sleep(3)            