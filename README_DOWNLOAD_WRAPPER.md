# Как исправить ошибку "Could not find GradleWrapperMain"

## Проблема

```
Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain
```

## Причина

Отсутствует файл `gradle/wrapper/gradle-wrapper.jar`

## Решение (выберите любой способ)

### ✅ Способ 1: Через браузер (САМЫЙ ПРОСТОЙ)

1. **Создайте папки:**
    - Создайте папку `gradle` в корне проекта
    - Внутри создайте папку `wrapper`
    - Путь должен быть: `E:\StudioProjects\work\inpact_analis\gradle\wrapper\`

2. **Скачайте файл:**
    - Откройте в браузере:
      ```
      https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
      ```
    - Файл начнет скачиваться автоматически
    - Сохраните его в папку `gradle\wrapper\`
    - Итоговый путь: `E:\StudioProjects\work\inpact_analis\gradle\wrapper\gradle-wrapper.jar`

3. **Проверьте:**
    - Файл должен существовать: `gradle\wrapper\gradle-wrapper.jar`
    - Размер файла: ~60 KB

4. **Запустите:**
   ```powershell
   .\gradlew.bat build
   ```

---

### ✅ Способ 2: Одна команда PowerShell

Скопируйте и вставьте в PowerShell (всю команду целиком):

```powershell
New-Item -ItemType Directory -Path "gradle\wrapper" -Force | Out-Null; (New-Object System.Net.WebClient).DownloadFile("https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar", "gradle\wrapper\gradle-wrapper.jar"); Write-Host "Готово! Запустите: .\gradlew.bat build" -ForegroundColor Green
```

---

### ✅ Способ 3: Через PowerShell скрипт

```powershell
.\download-wrapper.ps1
```

Если не работает, попробуйте:

```powershell
powershell -ExecutionPolicy Bypass -File .\download-wrapper.ps1
```

---

### ✅ Способ 4: Вручную через curl (если есть Git Bash)

Откройте Git Bash и выполните:

```bash
mkdir -p gradle/wrapper
curl -L https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar
```

---

## После скачивания

Проверьте что файл существует:

```powershell
dir gradle\wrapper\gradle-wrapper.jar
```

Должно показать файл размером ~60 KB.

Затем запустите:

```powershell
.\gradlew.bat build
```

При первом запуске Gradle скачается автоматически (~100 MB).

---

## Проверка Java

Перед запуском убедитесь что Java установлена:

```powershell
java -version
```

Должно показать Java 17 или выше.

Если Java нет:

1. Скачайте: https://adoptium.net/temurin/releases/?version=17
2. Установите
3. Перезапустите PowerShell

---

## Что делать дальше

После успешной сборки:

```powershell
# Запустить тесты
.\gradlew.bat test

# Тесты с отчетами
.\gradlew.bat testWithReport

# Все задачи
.\gradlew.bat tasks
```

---

## Нужна помощь?

Проверьте:

- ✅ Файл существует: `gradle\wrapper\gradle-wrapper.jar`
- ✅ Java установлена: `java -version` (должна быть 17+)
- ✅ Вы в корне проекта: `dir` должен показать `build.gradle.kts`

**Я рекомендую Способ 1 (через браузер) - это самый надежный способ!**
