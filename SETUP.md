# 🚀 Установка и первый запуск

## 📋 Предварительные требования

- ✅ **Java 17+** установлена
- ✅ **Git** установлен

## 🔧 Первоначальная настройка

### Windows

```powershell
# 1. Скачайте gradle-wrapper.jar (если его нет)
# Вариант A: Через PowerShell
Invoke-WebRequest -Uri https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar -OutFile gradle/wrapper/gradle-wrapper.jar

# Вариант B: Если есть Gradle установлен глобально
gradle wrapper --gradle-version 8.5

# 2. Запустите первую сборку (скачает Gradle автоматически)
.\gradlew.bat build

# 3. Запустите тесты
.\gradlew.bat test
```

### Linux/Mac

```bash
# 1. Дайте права на выполнение
chmod +x gradlew

# 2. Скачайте gradle-wrapper.jar (если его нет)
# Вариант A: Через curl
curl -L https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar

# Вариант B: Если есть Gradle установлен глобально
gradle wrapper --gradle-version 8.5

# 3. Запустите первую сборку
./gradlew build

# 4. Запустите тесты
./gradlew test
```

## 🎯 Быстрый старт

### 1. Проверка что все работает

**Windows:**

```powershell
.\gradlew.bat tasks
```

**Linux/Mac:**

```bash
./gradlew tasks
```

Вы должны увидеть список доступных задач, включая:

```
impact analysis tasks
--------------------
calculateImpact - Calculate impact analysis based on Git changes
getChangedFiles - Get list of changed files from Git
getChangedFilesForLint - Get list of changed files for linting
impactTest - Calculate impact and run affected tests
runImpactTests - Run tests based on impact analysis results
```

### 2. Сборка плагина

```bash
# Windows
.\gradlew.bat build

# Linux/Mac
./gradlew build
```

### 3. Запуск тестов

```bash
# Все тесты
.\gradlew.bat test              # Windows
./gradlew test                  # Linux/Mac

# Тесты + отчеты
.\gradlew.bat testWithReport    # Windows
./gradlew testWithReport        # Linux/Mac
```

## 📦 Публикация в локальный Maven

Для тестирования плагина в другом проекте:

```bash
# Windows
.\gradlew.bat publishToMavenLocal

# Linux/Mac
./gradlew publishToMavenLocal
```

После этого плагин будет доступен в `~/.m2/repository/`

## 🔍 Структура проекта

```
impact-analysis-plugin/
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar          # Gradle wrapper (скачается автоматически)
│       └── gradle-wrapper.properties   # Конфигурация wrapper
├── gradlew                             # Скрипт для Unix/Linux/Mac
├── gradlew.bat                         # Скрипт для Windows
├── build.gradle.kts                    # Build конфигурация
├── settings.gradle.kts                 # Settings
├── src/
│   ├── main/kotlin/                    # Исходный код плагина
│   └── test/kotlin/                    # Тесты
├── examples/                           # Примеры конфигураций
└── *.md                                # Документация
```

## 🚀 Использование в своем проекте

### Вариант 1: Из локального Maven

После `publishToMavenLocal`, в вашем проекте:

**settings.gradle.kts:**

```kotlin
pluginManagement {
    repositories {
        mavenLocal()  // Добавьте это
        gradlePluginPortal()
    }
}
```

**build.gradle.kts:**

```kotlin
plugins {
    id("com.impactanalysis.plugin") version "1.0.0"
}
```

### Вариант 2: Через includeBuild (для разработки)

В вашем проекте, **settings.gradle.kts:**

```kotlin
includeBuild("../impact-analysis-plugin")
```

**build.gradle.kts:**

```kotlin
plugins {
    id("com.impactanalysis.plugin")
}
```

## 🧪 Проверка работы тестов

```bash
# 1. Запустить все тесты
.\gradlew.bat test              # Windows
./gradlew test                  # Linux/Mac

# 2. Посмотреть отчет
# Откройте в браузере: build/reports/tests/test/index.html

# 3. С coverage
.\gradlew.bat testWithReport    # Windows
./gradlew testWithReport        # Linux/Mac

# Отчеты:
# - Test: build/reports/tests/test/index.html
# - Coverage: build/reports/jacoco/test/html/index.html
```

## 🐛 Troubleshooting

### Проблема: gradlew не запускается

**Windows:**

```powershell
# Проверьте что Java установлена
java -version

# Должно быть Java 17 или выше
# Если нет, установите: https://adoptium.net/
```

**Linux/Mac:**

```bash
# Дайте права на выполнение
chmod +x gradlew

# Проверьте Java
java -version
```

### Проблема: gradle-wrapper.jar not found

**Решение:**

```bash
# Скачайте вручную
# Windows (PowerShell):
Invoke-WebRequest -Uri https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar -OutFile gradle/wrapper/gradle-wrapper.jar

# Linux/Mac:
curl -L https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar

# Или используйте глобальный Gradle:
gradle wrapper --gradle-version 8.5
```

### Проблема: Permission denied (Linux/Mac)

```bash
chmod +x gradlew
./gradlew build
```

### Проблема: Out of Memory

```bash
# Увеличьте память в gradle.properties (уже настроено):
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
```

## ✅ Checklist первого запуска

- [ ] Java 17+ установлена
- [ ] Git установлен
- [ ] Скачан gradle-wrapper.jar
- [ ] Права на выполнение для gradlew (Linux/Mac)
- [ ] `gradlew tasks` работает
- [ ] `gradlew build` прошла успешно
- [ ] `gradlew test` все тесты прошли
- [ ] Отчеты сгенерированы

## 📚 Дополнительные ресурсы

- [README.md](README.md) - Основная документация
- [QUICK_START.md](QUICK_START.md) - Быстрый старт
- [README_TESTS.md](README_TESTS.md) - Документация по тестам
- [ARCHITECTURE.md](ARCHITECTURE.md) - Архитектура плагина

## 🎉 Готово!

После успешной сборки можете использовать плагин!

```bash
# Опубликуйте в локальный Maven
.\gradlew.bat publishToMavenLocal

# Теперь можете использовать в других проектах
```

**Следующие шаги:**

1. Изучите [QUICK_START.md](QUICK_START.md)
2. Попробуйте примеры в [examples/](examples/)
3. Прочитайте [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) для внедрения
