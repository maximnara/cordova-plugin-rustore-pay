# Cordova Plugin Template

Универсальный шаблон для создания Cordova плагинов с поддержкой Android (Kotlin) и iOS (Swift).

## Как настроить шаблон

### 1. Скопируйте шаблон
```bash
git clone <your-template-repo> my-cordova-plugin
cd my-cordova-plugin
```

### 2. Замените все плейсхолдеры

Найдите и замените следующие плейсхолдеры на ваши значения:

#### Основные плейсхолдеры:
- `{{PLUGIN_ID}}` - ID плагина (например: `cordova-plugin-my-awesome-plugin`)
- `{{PLUGIN_NAME}}` - Название плагина (например: `My Awesome Plugin`)
- `{{PLUGIN_DESCRIPTION}}` - Описание плагина
- `{{PLUGIN_NPM_NAME}}` - Название в NPM (например: `cordova-plugin-my-awesome-plugin`)
- `{{PLUGIN_AUTHOR}}` - Ваше имя или название компании
- `{{PLUGIN_KEYWORD}}` - Ключевое слово для поиска в NPM
- `{{PLUGIN_REPOSITORY_URL}}` - URL репозитория (например: `https://github.com/username/cordova-plugin-my-awesome-plugin`)

#### JavaScript плейсхолдеры:
- `{{JS_FILE_NAME}}` - Название JS файла (например: `my-awesome-plugin`)
- `{{JS_MODULE_NAME}}` - Название модуля в JS (например: `MyAwesomePlugin`)
- `{{JS_TARGET}}` - Глобальный объект в window (например: `MyAwesomePlugin`)

#### Android плейсхолдеры:
- `{{ANDROID_PLUGIN_CLASS}}` - Название класса плагина (например: `MyAwesomePlugin`)
- `{{ANDROID_PACKAGE}}` - Пакет Android (например: `com.yourcompany.plugin`)
- `{{ANDROID_PACKAGE_PATH}}` - Путь пакета (например: `com/yourcompany/plugin`)
- `{{GRADLE_FILE_NAME}}` - Название gradle файла (например: `my-awesome-plugin`)

#### iOS плейсхолдеры:
- `{{IOS_PLUGIN_CLASS}}` - Название класса iOS плагина (например: `MyAwesomePlugin`)

### 3. Переименуйте файлы

Переименуйте шаблонные файлы согласно вашему плагину:

```bash
# JavaScript
mv www/plugin-template.js www/{{JS_FILE_NAME}}.js

# Android
mv src/android/PluginTemplate.kt src/android/{{ANDROID_PLUGIN_CLASS}}.kt
mv src/android/build-template.gradle src/android/{{GRADLE_FILE_NAME}}.gradle

# iOS
mv src/ios/PluginTemplate.h src/ios/{{IOS_PLUGIN_CLASS}}.h
mv src/ios/PluginTemplate.swift src/ios/{{IOS_PLUGIN_CLASS}}.swift
```

### 4. Добавьте свою логику

#### JavaScript (www/{{JS_FILE_NAME}}.js):
- Добавьте события плагина в объект `events`
- Реализуйте методы плагина
- Замените `exampleMethod` на ваши методы

#### Android (src/android/{{ANDROID_PLUGIN_CLASS}}.kt):
- Добавьте ваши методы в `execute()`
- Реализуйте логику для каждого метода
- Добавьте необходимые зависимости в gradle файл

#### iOS (src/ios/{{IOS_PLUGIN_CLASS}}.swift):
- Добавьте объявления методов в .h файл
- Реализуйте методы в .swift файле
- Добавьте CocoaPods зависимости если нужно

### 5. Настройте plugin.xml

Раскомментируйте и настройте необходимые секции:
- Android permissions
- Android application config
- Gradle dependencies
- Framework dependencies
- Resource files
- iOS CocoaPods dependencies
- iOS frameworks

### 6. Установите и тестируйте

```bash
# Добавьте плагин в ваш Cordova проект
cordova plugin add /path/to/your/plugin

# Или опубликуйте в NPM
npm publish
cordova plugin add your-plugin-name
```

## Структура шаблона

```
cordova-plugin-template/
├── plugin.xml                 # Конфигурация плагина
├── package.json               # NPM пакет
├── www/
│   └── plugin-template.js     # JavaScript интерфейс
├── src/
│   ├── android/
│   │   ├── PluginTemplate.kt      # Android реализация
│   │   └── build-template.gradle  # Android зависимости
│   └── ios/
│       ├── PluginTemplate.h       # iOS заголовочный файл
│       └── PluginTemplate.swift   # iOS реализация
└── README.md                  # Эта инструкция
```

## Примеры замены

### Пример 1: Плагин для камеры
```
{{PLUGIN_ID}} → cordova-plugin-awesome-camera
{{PLUGIN_NAME}} → Awesome Camera Plugin
{{JS_MODULE_NAME}} → AwesomeCamera
{{ANDROID_PLUGIN_CLASS}} → AwesomeCameraPlugin
{{ANDROID_PACKAGE}} → com.awesome.camera
```

### Пример 2: Плагин для аналитики
```
{{PLUGIN_ID}} → cordova-plugin-my-analytics
{{PLUGIN_NAME}} → My Analytics Plugin
{{JS_MODULE_NAME}} → MyAnalytics
{{ANDROID_PLUGIN_CLASS}} → MyAnalyticsPlugin
{{ANDROID_PACKAGE}} → com.mycompany.analytics
```

## Возможности шаблона

- ✅ Поддержка Android (Kotlin) и iOS (Swift)
- ✅ Promise-based JavaScript API
- ✅ Система событий
- ✅ Обработка ошибок
- ✅ Вспомогательные методы
- ✅ Готовая структура для расширения
- ✅ Современные версии Cordova (12.0+)
- ✅ Комментированные секции для настройки

## Поддержка

Если у вас есть вопросы или предложения по улучшению шаблона, создайте issue в репозитории.