# Video Editor Android

Um editor de vídeo simples para Android 14+ que permite cortar vídeos e aplicar filtros básicos.

## Funcionalidades

- ✅ Selecionar vídeos da galeria
- ✅ Reproduzir vídeos
- ✅ Cortar vídeos (definir início e fim)
- ✅ Aplicar filtros (Sepia, Preto e Branco, Contraste Alto)
- ✅ Salvar vídeos editados na galeria

## Requisitos

- Android 14 (API 24) ou superior
- Permissões de leitura/escrita de mídia

## Como usar

1. Abra o aplicativo
2. Clique em "Selecionar Vídeo" para escolher um vídeo da galeria
3. Use os controles para:
   - Reproduzir/pausar o vídeo
   - Cortar o vídeo definindo início e fim
   - Aplicar filtros
4. Clique em "Salvar Vídeo" para salvar o resultado na galeria

## Build

### Via Android Studio
1. Abra o projeto no Android Studio
2. Execute o build normalmente

### Via Command Line
```bash
./gradlew assembleDebug
```

### Via GitHub Actions
O workflow `buildapkdebug.yml` irá construir automaticamente o APK de debug quando houver push ou pull request na branch main/master.

## Tecnologias Utilizadas

- **Media3 (ExoPlayer)**: Para reprodução e processamento de vídeo
- **Kotlin**: Linguagem de programação
- **Android Jetpack**: Componentes modernos do Android
- **Material Design 3**: Interface moderna

## Estrutura do Projeto

```
app/
├── src/main/
│   ├── java/com/zashed/videoeditor/
│   │   └── MainActivity.kt          # Atividade principal
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml    # Layout da interface
│   │   └── values/
│   │       ├── strings.xml          # Strings do aplicativo
│   │       ├── colors.xml           # Cores do tema
│   │       └── themes.xml           # Temas
│   └── AndroidManifest.xml          # Manifest do aplicativo
└── build.gradle                     # Configuração do Gradle

.github/workflows/
└── buildapkdebug.yml               # Workflow de CI/CD
```

## Permissões

O aplicativo solicita as seguintes permissões:

- `READ_EXTERNAL_STORAGE`: Para acessar vídeos da galeria
- `WRITE_EXTERNAL_STORAGE`: Para salvar vídeos editados
- `READ_MEDIA_VIDEO`: Para Android 13+ (acesso a vídeos)
- `READ_MEDIA_IMAGES`: Para Android 13+ (acesso a imagens)

## Desenvolvimento

Para contribuir ou modificar o projeto:

1. Clone o repositório
2. Abra no Android Studio
3. Faça suas modificações
4. Teste em um dispositivo/emulador
5. Envie um pull request

## Licença

Este projeto é distribuído sob a licença MIT.
