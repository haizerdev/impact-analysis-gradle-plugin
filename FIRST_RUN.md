# ⚡ Быстрый запуск (First Run)

## 🚨 Если получили ошибку "Could not find GradleWrapperMain"

Это означает что отсутствует `gradle-wrapper.jar`. Выберите один из способов:

### Способ 1: Автоматический скрипт (рекомендуется)

**Windows (PowerShell):**
```powershell
.\download-wrapper.ps1
```

**Linux/Mac:**

```bash
chmod +x download-wrapper.sh
./download-wrapper.sh
```

### Способ 2: Через браузер (самый простой)

1. **Откройте в браузере:**
   ```
   https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
   ```

2. **Сохраните файл как:**
   ```
   gradle/wrapper/gradle-wrapper.jar
   ```
   (Создайте папки `gradle/wrapper` если их нет)

### Способ 3: Через PowerShell (одна команда)

```powershell
New-Item -ItemType Directory -Path "gradle/wrapper" -Force; Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle/wrapper/gradle-wrapper.jar"
```

### Способ 4: Через curl (Linux/Mac)

```bash
mkdir -p gradle/wrapper
curl -L https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar
chmod +x gradlew
```

---

## ✅ После скачивания gradle-wrapper.jar

### Шаг 1: Проверьте что Java установлена

```powershell
java -version
```

Должно показать Java 17 или выше.

Если Java нет, скачайте: https://adoptium.net/temurin/releases/?version=17

### Шаг 2: Запустите сборку

**Windows:**
```powershell
.\gradlew.bat build
```

**Linux/Mac:**

```bash
./gradlew build
```

При первом запуске Gradle автоматически скачается (~100 MB).

### Шаг 3: Запустите тесты

**Windows:**
```powershell
.\gradlew.bat test
```

**Linux/Mac:**

```bash
./gradlew test
```

### Шаг 4: Посмотрите отчет тестов

Откройте в браузере:

- **Windows:** `build\reports\tests\test\index.html`
- **Linux/Mac:** `build/reports/tests/test/index.html`

---

## 🎉 Готово!

Теперь доступны все команды:

```powershell
# Windows
.\gradlew.bat tasks              # Список задач
.\gradlew.bat build              # Сборка
.\gradlew.bat test               # Тесты
.\gradlew.bat testWithReport     # Тесты + отчеты
.\gradlew.bat publishToMavenLocal # Публикация

# Linux/Mac
./gradlew tasks
./gradlew build
./gradlew test
./gradlew testWithReport
./gradlew publishToMavenLocal
```

---

## 📝 Частые проблемы

### ❌ "Could not find or load main class org.gradle.wrapper.GradleWrapperMain"

**Причина:** Отсутствует `gradle-wrapper.jar`

**Решение:** Используйте один из способов выше для скачивания

### ❌ "java command not found"

**Причина:** Java не установлена или не в PATH

**Решение:**

1. Скачайте Java 17: https://adoptium.net/temurin/releases/?version=17
2. Установите
3. Перезапустите терминал

### ❌ "Permission denied" (Linux/Mac)

**Причина:** Нет прав на выполнение

**Решение:**

```bash
chmod +x gradlew
chmod +x download-wrapper.sh
```

### ❌ "cannot be loaded because running scripts is disabled" (Windows)

**Причина:** PowerShell блокирует выполнение скриптов

**Решение:** Используйте `.\gradlew.bat` вместо `.\gradlew`

Или разрешите выполнение скриптов:

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

## 📚 Дальнейшие шаги

После успешного запуска:

1. **Запустите тесты:** `.\gradlew.bat testWithReport`
2. **Изучите документацию:** [README.md](README.md)
3. **Попробуйте примеры:** [examples/](examples/)
4. **Прочитайте про тесты:** [README_TESTS.md](README_TESTS.md)

---

## 🆘 Нужна помощь?

**Проверьте что у вас есть:**

- ✅ Java 17+ установлена: `java -version`
- ✅ Файл существует: `gradle/wrapper/gradle-wrapper.jar`
- ✅ Права на выполнение (Linux/Mac): `chmod +x gradlew`

**Подробная документация:** [SETUP.md](SETUP.md)
