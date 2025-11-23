# Impact Analysis Gradle Plugin 🎯

Универсальный Gradle плагин для анализа Git изменений и автоматического определения scope тестов и файлов для линтинга в
multi-module проектах.

## 🚀 Возможности

- ✅ **Анализ Git изменений** - определяет измененные файлы между коммитами/ветками
- ✅ **Граф зависимостей модулей** - автоматически строит и анализирует зависимости между модулями
- ✅ **Умное определение scope тестов** - определяет какие тесты нужно запускать (unit, integration, UI и т.д.)
- ✅ **Multi-module support** - работает с любой структурой проекта, независимо от конфигурации
- ✅ **Список файлов для линтинга** - отдает список измененных файлов для детекта/линтеров
- ✅ **Гибкая конфигурация** - настраиваемые правила через DSL
- ✅ **Поддержка различных типов тестов** - unit, integration, UI, E2E, API, performance и т.д.

## 📦 Установка

### 1. Добавьте плагин в ваш проект

**build.gradle.kts (root проекта):**

```kotlin
plugins {
    id("com.impactanalysis.plugin") version "1.0.0"
}
```

**build.gradle (root проекта):**

```groovy
plugins {
    id 'com.impactanalysis.plugin' version '1.0.0'
}
```

### 2. Или добавьте из локального репозитория (для разработки)

**settings.gradle.kts:**

```kotlin
pluginManagement {
    includeBuild("path/to/impact-analysis-plugin")
}
```

## ⚙️ Конфигурация

### Базовая конфигурация

```kotlin
impactAnalysis {
    // Базовая ветка для сравнения (по умолчанию: origin/main)
    baseBranch.set("origin/develop")
    
    // Сравниваемая ветка (по умолчанию: HEAD)
    compareBranch.set("HEAD")
    
    // Включить анализ uncommitted изменений
    includeUncommittedChanges.set(true)
    
    // Запускать все тесты при изменении критических файлов
    runAllTestsOnCriticalChanges.set(true)
    
    // Запускать unit тесты по умолчанию
    runUnitTestsByDefault.set(true)
    
    // Критические пути
    criticalPaths.set(listOf(
        "build.gradle",
        "build.gradle.kts",
        "gradle.properties"
    ))
    
    // Расширения файлов для линтинга
    lintFileExtensions.set(listOf("kt", "java", "xml"))
}
```

### Настройка правил для типов тестов

```kotlin
impactAnalysis {
    // Unit тесты - запускаются при изменении любого кода
    unitTests {
        whenChanged("src/main/**", "src/test/**")
        runOnlyInChangedModules = false // Запускать во всех зависимых модулях
    }
    
    // Integration тесты - только при изменении определенных файлов
    integrationTests {
        whenChanged("**/repository/**", "**/database/**", "**/api/**")
        runOnlyInChangedModules = true
    }
    
    // UI тесты - при изменении UI компонентов
    uiTests {
        whenChanged("**/ui/**", "**/res/layout/**", "**/compose/**")
        runOnlyInChangedModules = false
    }
    
    // E2E тесты - при изменении критических частей
    e2eTests {
        whenChanged("**/feature/**")
        runOnlyInChangedModules = false
    }
    
    // API тесты
    apiTests {
        whenChanged("**/api/**", "**/network/**")
        runOnlyInChangedModules = true
    }
}
```

## 🎯 Использование

### Доступные задачи

1. **`calculateImpact`** - Рассчитать impact analysis
   ```bash
   ./gradlew calculateImpact
   ```

   Результат сохраняется в `build/impact-analysis/result.json`

2. **`getChangedFiles`** - Получить список всех измененных файлов
   ```bash
   ./gradlew getChangedFiles
   ```

   Результат: `build/impact-analysis/changed-files.txt`

3. **`getChangedFilesForLint`** - Получить список файлов для линтинга
   ```bash
   ./gradlew getChangedFilesForLint
   ```

   Результат: `build/impact-analysis/lint-files.txt`

4. **`runImpactTests`** - Запустить тесты на основе impact analysis
   ```bash
   ./gradlew runImpactTests
   ```

5. **`impactTest`** - Полный flow: analyze + run tests
   ```bash
   ./gradlew impactTest
   ```

### Параметры командной строки

```bash
# Указать базовую ветку
./gradlew calculateImpact -PbaseBranch=origin/main

# Указать сравниваемую ветку
./gradlew calculateImpact -PcompareBranch=feature/my-feature

# Запустить только определенные типы тестов
./gradlew runImpactTests -PtestTypes=unit,integration

# Продолжить выполнение даже при ошибках
./gradlew runImpactTests -PcontinueOnFailure=true
```

## 📊 Примеры использования

### Пример 1: Простой multi-module проект

**Структура проекта:**

```
my-app/
├── app/
├── feature-auth/
├── feature-profile/
├── core-network/
└── core-database/
```

**build.gradle.kts:**

```kotlin
plugins {
    id("com.impactanalysis.plugin") version "1.0.0"
}

impactAnalysis {
    baseBranch.set("origin/main")
    
    unitTests {
        whenChanged("src/main/**", "src/test/**")
        runOnlyInChangedModules = false
    }
}
```

**Использование:**

```bash
# Анализируем изменения
./gradlew calculateImpact

# Результат покажет:
# - Измененные файлы: feature-auth/src/main/LoginViewModel.kt
# - Затронутые модули: :feature-auth, :app (зависит от feature-auth)
# - Тесты для запуска: :feature-auth:test, :app:test
```

### Пример 2: Android проект с разными типами тестов

```kotlin
impactAnalysis {
    baseBranch.set("origin/develop")
    
    // Unit тесты
    unitTests {
        whenChanged("src/main/**")
        runOnlyInChangedModules = false
    }
    
    // Android Instrumentation тесты
    testType(TestType.INTEGRATION) {
        whenChanged("**/ui/**", "**/activity/**", "**/fragment/**")
        runOnlyInChangedModules = true
    }
    
    // UI тесты (Compose/Espresso)
    uiTests {
        whenChanged("**/compose/**", "**/res/layout/**")
        runOnlyInChangedModules = false
    }
}
```

### Пример 3: Интеграция с детектом

```kotlin
// build.gradle.kts
tasks.register("lintChangedFiles") {
    dependsOn("getChangedFilesForLint")
    
    doLast {
        val changedFiles = file("build/impact-analysis/lint-files.txt")
        if (changedFiles.exists() && changedFiles.readText().isNotEmpty()) {
            val files = changedFiles.readText().split("\n")
            
            // Запускаем детект только на измененных файлах
            exec {
                commandLine("./gradlew", "detekt", "-Pdetekt.files=${files.joinToString(",")}")
            }
        }
    }
}
```

### Пример 4: CI/CD интеграция (GitHub Actions)

```yaml
name: Run Impact Tests

on:
  pull_request:
    branches: [ main, develop ]

jobs:
  impact-test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Важно для Git анализа
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Calculate Impact
        run: ./gradlew calculateImpact -PbaseBranch=origin/${{ github.base_ref }}
      
      - name: Show Impact Analysis
        run: cat build/impact-analysis/result.json
      
      - name: Run Impact Tests
        run: ./gradlew runImpactTests -PcontinueOnFailure=true
      
      - name: Lint Changed Files
        run: |
          if [ -s build/impact-analysis/lint-files.txt ]; then
            ./gradlew detektCheck --include-build=$(cat build/impact-analysis/lint-files.txt)
          fi
```

### Пример 5: GitLab CI

```yaml
impact-analysis:
  stage: test
  script:
    - git fetch origin $CI_MERGE_REQUEST_TARGET_BRANCH_NAME
    - ./gradlew calculateImpact -PbaseBranch=origin/$CI_MERGE_REQUEST_TARGET_BRANCH_NAME
    - ./gradlew runImpactTests
  artifacts:
    paths:
      - build/impact-analysis/
    reports:
      junit: '**/build/test-results/test/TEST-*.xml'
```

## 🏗️ Архитектура

### Как это работает

1. **Git Analysis** - Плагин анализирует изменения в Git между двумя коммитами/ветками
2. **Module Detection** - Определяет к каким модулям относятся измененные файлы
3. **Dependency Graph** - Строит граф зависимостей между модулями
4. **Impact Calculation** - Определяет все затронутые модули (включая зависимые)
5. **Test Scope** - На основе правил определяет какие тесты нужно запустить
6. **Execution** - Запускает необходимые тесты

### Компоненты плагина

- **GitClient** - работа с Git через JGit
- **ModuleDependencyGraph** - граф зависимостей модулей
- **DependencyAnalyzer** - анализ зависимостей и определение модулей
- **TestScopeCalculator** - расчет scope тестов
- **Tasks** - Gradle задачи для выполнения операций

## 🔧 Расширенные возможности

### Экспорт графа зависимостей

Вы можете экспортировать граф зависимостей в формате DOT для визуализации:

```kotlin
tasks.register("exportDependencyGraph") {
    doLast {
        val graph = ModuleDependencyGraph(project)
        file("build/dependency-graph.dot").writeText(graph.toDotFormat())
        println("Dependency graph exported to build/dependency-graph.dot")
        println("Visualize it with: dot -Tpng build/dependency-graph.dot -o graph.png")
    }
}
```

### Кастомные типы тестов

```kotlin
enum class MyTestType(val taskSuffix: String) {
    SCREENSHOT("screenshotTest"),
    ACCESSIBILITY("a11yTest"),
    SECURITY("securityTest")
}

// В конфигурации
impactAnalysis {
    testType(MyTestType.SCREENSHOT) {
        whenChanged("**/ui/**")
    }
}
```

## 📝 Формат результата

**build/impact-analysis/result.json:**

```json
{
  "changedFiles": [
    {
      "path": "feature-auth/src/main/LoginViewModel.kt",
      "module": ":feature-auth",
      "changeType": "MODIFIED",
      "language": "KOTLIN"
    }
  ],
  "affectedModules": [
    ":feature-auth",
    ":app"
  ],
  "testsToRun": {
    "UNIT": [
      ":feature-auth:test",
      ":app:test"
    ],
    "INTEGRATION": [
      ":feature-auth:integrationTest"
    ]
  },
  "filesToLint": [
    "feature-auth/src/main/LoginViewModel.kt"
  ],
  "timestamp": 1234567890
}
```

## 🎨 Best Practices

1. **Используйте в CI/CD** - Экономьте время на тестировании, запуская только необходимые тесты
2. **Настройте правила** - Определите четкие правила для каждого типа тестов
3. **Критические пути** - Укажите файлы, изменение которых требует полного тестирования
4. **Линтинг** - Запускайте линтеры только на измененных файлах
5. **Мониторинг** - Сохраняйте результаты impact analysis как артефакты

## 🤝 Преимущества

- ⚡ **Скорость** - Запускает только необходимые тесты
- 💰 **Экономия** - Сокращает время CI/CD и затраты на инфраструктуру
- 🎯 **Точность** - Определяет все затронутые модули через граф зависимостей
- 🔧 **Гибкость** - Работает с любой структурой проекта
- 📦 **Универсальность** - Не зависит от конфигурации конкретного проекта

## 📄 Лицензия

MIT License

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
