# Quick Start Guide - Impact Analysis Plugin

## 🚀 Быстрый старт за 5 минут

### Шаг 1: Установка

Добавьте плагин в корневой `build.gradle.kts`:

```kotlin
plugins {
    id("com.impactanalysis.plugin") version "1.0.0"
}
```

### Шаг 2: Минимальная конфигурация

```kotlin
impactAnalysis {
    baseBranch.set("origin/main")  // Ваша базовая ветка
}
```

### Шаг 3: Первый запуск

```bash
# Анализируем изменения
./gradlew calculateImpact

# Смотрим результат
cat build/impact-analysis/result.json
```

**Готово!** 🎉 Теперь плагин проанализирует изменения и покажет какие тесты нужно запустить.

---

## 📋 Базовые команды

### 1. Анализ изменений

```bash
./gradlew calculateImpact
```

**Результат:** `build/impact-analysis/result.json` содержит:

- Список измененных файлов
- Затронутые модули
- Тесты для запуска
- Файлы для линтинга

### 2. Получить список измененных файлов

```bash
./gradlew getChangedFiles
```

**Результат:** `build/impact-analysis/changed-files.txt`

### 3. Получить файлы для линтинга

```bash
./gradlew getChangedFilesForLint
```

**Результат:** `build/impact-analysis/lint-files.txt`

### 4. Запустить только необходимые тесты

```bash
./gradlew impactTest
```

Это автоматически:

1. Проанализирует изменения
2. Определит какие тесты запускать
3. Запустит только необходимые тесты

---

## ⚙️ Типичные конфигурации

### Для Android проекта

```kotlin
impactAnalysis {
    baseBranch.set("origin/develop")
    
    // Unit тесты
    unitTests {
        whenChanged("src/main/**")
        runOnlyInChangedModules = false
    }
    
    // UI тесты
    uiTests {
        whenChanged("**/compose/**", "**/res/layout/**")
        runOnlyInChangedModules = false
    }
    
    // Файлы для линтинга
    lintFileExtensions.set(listOf("kt", "java", "xml"))
}
```

### Для Backend (Spring Boot)

```kotlin
impactAnalysis {
    baseBranch.set("origin/main")
    
    // Unit тесты
    unitTests {
        whenChanged("src/main/**")
        runOnlyInChangedModules = false
    }
    
    // Integration тесты
    integrationTests {
        whenChanged("**/repository/**", "**/database/**")
        runOnlyInChangedModules = false
    }
    
    // API тесты
    apiTests {
        whenChanged("**/controller/**", "**/api/**")
        runOnlyInChangedModules = true
    }
}
```

### Для Microservices

```kotlin
impactAnalysis {
    baseBranch.set("origin/main")
    
    // Критические изменения в shared библиотеках
    criticalPaths.set(listOf(
        "libs/common/**",
        "libs/api-contracts/**"
    ))
    
    runAllTestsOnCriticalChanges.set(true)
    
    unitTests {
        whenChanged("src/main/**")
        runOnlyInChangedModules = false
    }
    
    e2eTests {
        whenChanged("services/**")
        runOnlyInChangedModules = false
    }
}
```

---

## 🔧 Интеграция с CI/CD

### GitHub Actions

```yaml
name: Impact Tests

on:
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Важно!
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Run Impact Tests
        run: ./gradlew impactTest -PbaseBranch=origin/${{ github.base_ref }}
```

### GitLab CI

```yaml
impact-test:
  stage: test
  script:
    - git fetch origin $CI_MERGE_REQUEST_TARGET_BRANCH_NAME
    - ./gradlew impactTest -PbaseBranch=origin/$CI_MERGE_REQUEST_TARGET_BRANCH_NAME
```

### Jenkins

```groovy
stage('Impact Tests') {
    steps {
        sh './gradlew impactTest -PbaseBranch=origin/main'
    }
}
```

---

## 🎯 Интеграция с детектом/линтером

### Вариант 1: Прямая интеграция

```kotlin
tasks.register("detektChanged") {
    dependsOn("getChangedFilesForLint")
    
    doLast {
        val files = file("build/impact-analysis/lint-files.txt").readLines()
        if (files.isNotEmpty()) {
            exec {
                commandLine("./gradlew", "detekt", "-Pdetekt.files=${files.joinToString(",")}")
            }
        }
    }
}
```

### Вариант 2: В CI/CD

```bash
# Получаем измененные файлы
./gradlew getChangedFilesForLint

# Запускаем детект только на них
if [ -s build/impact-analysis/lint-files.txt ]; then
  ./gradlew detekt --input=$(cat build/impact-analysis/lint-files.txt | tr '\n' ',')
fi
```

---

## 📊 Чтение результатов

### Через командную строку

```bash
# Просмотр результата
cat build/impact-analysis/result.json | jq '.'

# Только затронутые модули
cat build/impact-analysis/result.json | jq '.affectedModules'

# Только тесты для запуска
cat build/impact-analysis/result.json | jq '.testsToRun'
```

### Через Gradle задачу

```kotlin
tasks.register("showImpact") {
    dependsOn("calculateImpact")
    
    doLast {
        val result = com.google.gson.Gson().fromJson(
            file("build/impact-analysis/result.json").readText(),
            com.impactanalysis.model.ImpactAnalysisResult::class.java
        )
        
        println("Affected modules: ${result.affectedModules}")
        println("Tests to run: ${result.testsToRun}")
    }
}
```

---

## 💡 Полезные советы

### 1. Сравнение с конкретной веткой

```bash
./gradlew calculateImpact -PbaseBranch=origin/develop
```

### 2. Игнорировать uncommitted изменения

```kotlin
impactAnalysis {
    includeUncommittedChanges.set(false)
}
```

### 3. Запустить только unit тесты

```bash
./gradlew runImpactTests -PtestTypes=unit
```

### 4. Запустить несколько типов тестов

```bash
./gradlew runImpactTests -PtestTypes=unit,integration
```

### 5. Продолжить при ошибках

```bash
./gradlew runImpactTests -PcontinueOnFailure=true
```

---

## 🐛 Troubleshooting

### Проблема: "Git repository not found"

**Решение:** Убедитесь что вы в Git репозитории и есть `.git` папка

### Проблема: "No changes detected"

**Решение:**

- Проверьте что есть коммиты
- Используйте `git status` для проверки изменений
- Проверьте правильность `baseBranch`

### Проблема: "Module not found for file"

**Решение:**

- Файл может быть в корне проекта (не в модуле)
- Проверьте что у модуля есть `build.gradle` файл

### Проблема: Плагин не находит тесты

**Решение:**

- Убедитесь что задачи тестов существуют (`:test`, `:integrationTest` и т.д.)
- Проверьте конфигурацию правил `whenChanged`

---

## 📚 Дальнейшее чтение

- [README.md](README.md) - Полная документация
- [ARCHITECTURE.md](ARCHITECTURE.md) - Архитектура плагина
- [examples/](examples/) - Примеры конфигураций

---

## 🎓 Обучающие примеры

### Пример 1: Базовое использование

```bash
# 1. Создайте коммит с изменениями
git add MyFile.kt
git commit -m "Update MyFile"

# 2. Запустите анализ
./gradlew calculateImpact

# 3. Посмотрите результат
cat build/impact-analysis/result.json
```

### Пример 2: Сравнение веток

```bash
# Сравнить вашу feature ветку с main
git checkout feature/my-feature
./gradlew calculateImpact -PbaseBranch=origin/main
```

### Пример 3: CI/CD workflow

```bash
# 1. В PR, сравниваем с target веткой
./gradlew calculateImpact -PbaseBranch=origin/main

# 2. Запускаем только нужные тесты
./gradlew runImpactTests

# 3. Линтим только измененные файлы
./gradlew getChangedFilesForLint
./gradlew detekt --input=@build/impact-analysis/lint-files.txt
```

---

## ⚡ Быстрые шаблоны

### Минимальная конфигурация

```kotlin
plugins {
    id("com.impactanalysis.plugin") version "1.0.0"
}

impactAnalysis {
    baseBranch.set("origin/main")
}
```

### Полная конфигурация

```kotlin
plugins {
    id("com.impactanalysis.plugin") version "1.0.0"
}

impactAnalysis {
    baseBranch.set("origin/main")
    compareBranch.set("HEAD")
    includeUncommittedChanges.set(true)
    runAllTestsOnCriticalChanges.set(true)
    runUnitTestsByDefault.set(true)
    
    criticalPaths.set(listOf(
        "build.gradle",
        "gradle.properties"
    ))
    
    lintFileExtensions.set(listOf("kt", "java"))
    
    unitTests {
        whenChanged("src/main/**")
        runOnlyInChangedModules = false
    }
}
```

---

**Готовы начать?** Попробуйте плагин прямо сейчас! 🚀

```bash
./gradlew calculateImpact
```
