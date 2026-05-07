# 📜 Changelog

Todas as mudanças relevantes deste projeto serão documentadas neste arquivo.

O formato segue o padrão inspirado em **Keep a Changelog**, e o versionamento segue **Semantic Versioning**.

---

## [Unreleased]
### Planejado
- Avanço automático para a próxima música ao finalizar a faixa atual.
- Controles de reprodução disponíveis na tela bloqueada.
- Integração completa com controles Bluetooth (Play, Pause, Next, Previous).
- Melhorias visuais na interface (UI/UX).
- Serviço em segundo plano para reprodução contínua.

---
## [0.5.0] - 2026-05-05

### ✨ Adicionado
- Reprodução de músicas locais utilizando `MediaStore`.
- Listagem de músicas disponíveis no dispositivo em uma `ListView`.
- Controles completos de reprodução:
  - Play
  - Pause
  - Stop
  - Próxima faixa
  - Faixa anterior
- Exibição do tempo atual da música.
- Exibição do tempo total da faixa.
- Barra de progresso (`SeekBar`) sincronizada com a reprodução.
- Interação direta com a `SeekBar` para avançar ou retroceder a música.
- Controle de permissões para acesso a arquivos de áudio (`READ_MEDIA_AUDIO`).

### 🔧 Alterado
- Fixação da orientação da tela no modo retrato para evitar interrupções da reprodução.
- Organização do fluxo de reprodução para garantir apenas uma música ativa por vez.
- Padronização do ciclo de vida do `MediaPlayer`.

### ✅ Melhorado
- Estabilidade geral do player.
- Experiência do usuário durante Play/Pause/Stop.
- Sincronização precisa entre tempo exibido e progresso da música.
- Compatibilidade com dispositivos Bluetooth para saída de áudio.

### 📦 Build
- Geração automática de APK de debug (`app-debug.apk`) para testes.
- Build estável e consistente sem erros de compilação.

---

## 🧠 Observações
- Este projeto adota uma estratégia de desenvolvimento incremental.
- Funcionalidades mais avançadas (serviços em segundo plano e mídia de sistema) serão adicionadas após a consolidação da base atual.

---