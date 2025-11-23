# План реализации и внедрения Impact Analysis Plugin

## 📋 Общий план

Этот документ описывает как реализован плагин и как можно его внедрить в реальный проект.

---

## ✅ Что уже реализовано

### 1. Структура плагина (100% готово)

```
impact-analysis-plugin/
├── src/main/kotlin/com/impactanalysis/
│   ├── ImpactAnalysisPlugin.kt              ✅ Главный класс плагина
│   ├── extension/
│   │   └── ImpactAnalysisExtension.kt       ✅ DSL конфигурация
│   ├── git/
│   │   └── GitClient.kt                     ✅ Работа с Git через JGit
│   ├── dependency/
│   │   ├── ModuleDependencyGraph.kt         ✅ Граф зависимостей
│   │   └── DependencyAnalyzer.kt            ✅ Анализ зависимостей
│   ├── scope/
│   │   └── TestScopeCalculator.kt           ✅ Расчет scope тестов
│   ├── tasks/
│   │   ├── CalculateImpactTask.kt           ✅ Задача анализа
│   │   ├── GetChangedFilesTask.kt           ✅ Получение файлов
│   │   └── RunImpactTestsTask.kt            ✅ Запуск тестов
│   └── model/
│       ├── ImpactAnalysisResult.kt          ✅ Модели данных
│       └── TestType.kt                      ✅ Типы тестов
├── examples/                                 ✅ Примеры конфигураций
├── build.gradle.kts                         ✅ Build конфигурация
├── README.md                                ✅ Документация
├── ARCHITECTURE.md                          ✅ Архитектура
├── QUICK_START.md                           ✅ Быстрый старт
└── SUMMARY.md                               ✅ Резюме
```

### 2. Ключевые компоненты

✅ **GitClient** - полностью рабочий клиент для Git

- Получение изменений между коммитами
- Поддержка uncommitted изменений
- Сравнение веток

✅ **ModuleDependencyGraph** - граф зависимостей

- Построение графа через Gradle API
- Транзитивный анализ зависимостей
- Экспорт в DOT формат

✅ **DependencyAnalyzer** - анализатор

- Определение модуля для файла
- Распознавание тестовых файлов
- Распознавание конфигурационных файлов

✅ **TestScopeCalculator** - калькулятор scope

- Применение правил из конфигурации
- Обработка критических изменений
- Генерация списка задач

✅ **Gradle Tasks** - 5 задач

- calculateImpact
- getChangedFiles
- getChangedFilesForLint
- runImpactTests
- impactTest

### 3. Документация

✅ **README.md** - полная документация с примерами
✅ **ARCHITECTURE.md** - детальная архитектура
✅ **QUICK_START.md** - быстрый старт
✅ **SUMMARY.md** - резюме проекта
✅ **Примеры** - для Android, Backend, Microservices

---

## 🚀 Шаги для внедрения

### Этап 1: Локальная разработка и тестирование

#### 1.1 Сборка плагина

```bash
# В директории плагина
./gradlew build

# Публикация в локальный Maven репозиторий
./gradlew publishToMavenLocal
```

#### 1.2 Тестирование на тестовом проекте

Создайте тестовый multi-module проект:

```
test-project/
├── app/
├── feature-auth/
├── feature-profile/
└── core-network/
```

В `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()  // Для локального тестирования
        gradlePluginPortal()
    }
}

// Для разработки можно использовать includeBuild
// includeBuild("../impact-analysis-plugin")
```

В корневом `build.gradle.kts`:

```kotlin
plugins {
    id("com.impactanalysis.plugin") version "1.0.0"
}

impactAnalysis {
    baseBranch.set("origin/main")
    
    unitTests {
        whenChanged("src/main/**")
        runOnlyInChangedModules = false
    }
}
```

Запуск:

```bash
./gradlew calculateImpact
cat build/impact-analysis/result.json
```

### Этап 2: Внедрение в реальный проект

#### 2.1 Публикация плагина

**Вариант A: Gradle Plugin Portal (рекомендуется)**

1. Создайте аккаунт на https://plugins.gradle.org
2. Получите API ключи
3. Добавьте в `gradle.properties`:

```properties
gradle.publish.key=<your-key>
gradle.publish.secret=<your-secret>
```

4. Опубликуйте:

```bash
./gradlew publishPlugins
```

**Вариант B: Корпоративный Maven репозиторий**

В `build.gradle.kts` плагина:

```kotlin
publishing {
    repositories {
        maven {
            name = "corporate"
            url = uri("https://maven.company.com/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}
```

Публикация:

```bash
./gradlew publish
```

**Вариант C: GitHub Packages**

```kotlin
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/your-org/impact-analysis-plugin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

#### 2.2 Подключение в проекте

В целевом проекте, в корневом `build.gradle.kts`:

```kotlin
plugins {
    id("com.impactanalysis.plugin") version "1.0.0"
}

impactAnalysis {
    baseBranch.set("origin/main")
    includeUncommittedChanges.set(true)
    runAllTestsOnCriticalChanges.set(true)
    
    // Настройка под ваш проект
    unitTests {
        whenChanged("src/main/**", "src/test/**")
        runOnlyInChangedModules = false
    }
    
    integrationTests {
        whenChanged("**/repository/**", "**/database/**")
        runOnlyInChangedModules = true
    }
}
```

#### 2.3 Первый запуск и валидация

```bash
# 1. Проверка что плагин работает
./gradlew tasks --group "impact analysis"

# Должны появиться задачи:
# - calculateImpact
# - getChangedFiles
# - getChangedFilesForLint
# - runImpactTests
# - impactTest

# 2. Создайте тестовое изменение
echo "// test" >> some-module/src/main/SomeFile.kt
git add .
git commit -m "test: impact analysis"

# 3. Запустите анализ
./gradlew calculateImpact

# 4. Проверьте результат
cat build/impact-analysis/result.json
```

### Этап 3: Интеграция с CI/CD

#### 3.1 GitHub Actions

Создайте `.github/workflows/impact-tests.yml`:

```yaml
name: Impact Tests

on:
  pull_request:
    branches: [ main, develop ]

jobs:
  impact-analysis:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout
        uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Важно для Git анализа!
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
      
      - name: Calculate Impact
        run: ./gradlew calculateImpact -PbaseBranch=origin/${{ github.base_ref }}
      
      - name: Upload Impact Analysis Results
        uses: actions/upload-artifact@v3
        with:
          name: impact-analysis
          path: build/impact-analysis/
      
      - name: Run Impact Tests
        run: ./gradlew runImpactTests -PcontinueOnFailure=false
      
      - name: Lint Changed Files
        run: |
          if [ -s build/impact-analysis/lint-files.txt ]; then
            ./gradlew detektCheck
          fi
```

#### 3.2 GitLab CI

Создайте `.gitlab-ci.yml`:

```yaml
stages:
  - analysis
  - test
  - lint

variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

impact-analysis:
  stage: analysis
  image: openjdk:17-jdk
  script:
    - git fetch origin $CI_MERGE_REQUEST_TARGET_BRANCH_NAME
    - ./gradlew calculateImpact -PbaseBranch=origin/$CI_MERGE_REQUEST_TARGET_BRANCH_NAME
  artifacts:
    paths:
      - build/impact-analysis/
    reports:
      junit: '**/build/test-results/test/TEST-*.xml'

impact-tests:
  stage: test
  image: openjdk:17-jdk
  dependencies:
    - impact-analysis
  script:
    - ./gradlew runImpactTests

lint-changed:
  stage: lint
  image: openjdk:17-jdk
  dependencies:
    - impact-analysis
  script:
    - |
      if [ -s build/impact-analysis/lint-files.txt ]; then
        ./gradlew detektCheck
      fi
```

#### 3.3 Jenkins

Создайте `Jenkinsfile`:

```groovy
pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Impact Analysis') {
            steps {
                sh './gradlew calculateImpact -PbaseBranch=origin/main'
                archiveArtifacts artifacts: 'build/impact-analysis/**', fingerprint: true
            }
        }
        
        stage('Run Impact Tests') {
            steps {
                sh './gradlew runImpactTests'
            }
        }
        
        stage('Lint Changed Files') {
            steps {
                script {
                    def lintFiles = readFile('build/impact-analysis/lint-files.txt').trim()
                    if (lintFiles) {
                        sh './gradlew detektCheck'
                    }
                }
            }
        }
    }
    
    post {
        always {
            junit '**/build/test-results/test/TEST-*.xml'
        }
    }
}
```

### Этап 4: Постепенное внедрение (Rollout Strategy)

#### 4.1 Фаза 1: Только логирование (неделя 1-2)

```kotlin
impactAnalysis {
    baseBranch.set("origin/main")
    // Пока просто смотрим что анализируется
}
```

Запускайте в CI:

```bash
./gradlew calculateImpact
cat build/impact-analysis/result.json
```

Анализируйте результаты, но не запускайте тесты на основе них.

#### 4.2 Фаза 2: Параллельно с полным прогоном (неделя 3-4)

```yaml
# GitHub Actions
- name: Run All Tests (baseline)
  run: ./gradlew test

- name: Run Impact Tests (comparison)
  run: ./gradlew runImpactTests
  continue-on-error: true
```

Сравнивайте результаты и время выполнения.

#### 4.3 Фаза 3: Impact tests для feature веток (неделя 5-6)

```kotlin
// build.gradle.kts
impactAnalysis {
    baseBranch.set("origin/main")
    
    // Только для feature веток используем impact analysis
    if (System.getenv("CI_BRANCH")?.startsWith("feature/") == true) {
        runUnitTestsByDefaultProperty.set(true)
    }
}
```

#### 4.4 Фаза 4: Полное внедрение (неделя 7+)

Переключитесь полностью на impact tests для всех PR.

---

## 🔧 Настройка под конкретные проекты

### Android проект

```kotlin
impactAnalysis {
    baseBranch.set("origin/develop")
    
    criticalPaths.set(listOf(
        "build.gradle",
        "gradle.properties",
        "proguard-rules.pro",
        "AndroidManifest.xml"
    ))
    
    unitTests {
        whenChanged("src/main/**", "src/test/**")
        runOnlyInChangedModules = false
    }
    
    integrationTests {
        whenChanged("**/ui/**", "**/activity/**", "**/fragment/**")
        runOnlyInChangedModules = true
    }
    
    uiTests {
        whenChanged("**/compose/**", "**/res/layout/**")
        runOnlyInChangedModules = false
    }
    
    lintFileExtensions.set(listOf("kt", "java", "xml"))
}
```

### Backend проект

```kotlin
impactAnalysis {
    baseBranch.set("origin/main")
    
    criticalPaths.set(listOf(
        "application.yml",
        "application-*.yml",
        "flyway/migrations/**"
    ))
    
    unitTests {
        whenChanged("src/main/**")
        runOnlyInChangedModules = false
    }
    
    integrationTests {
        whenChanged("**/repository/**", "**/dao/**")
        runOnlyInChangedModules = false
    }
    
    apiTests {
        whenChanged("**/controller/**", "**/api/**")
        runOnlyInChangedModules = true
    }
}
```

### Microservices

```kotlin
impactAnalysis {
    baseBranch.set("origin/main")
    
    criticalPaths.set(listOf(
        "libs/common/**",
        "libs/api-contracts/**",
        "docker-compose.yml"
    ))
    
    runAllTestsOnCriticalChanges.set(true)
    
    // Contract тесты при изменении API
    testType(TestType.CONTRACT) {
        whenChanged("**/api/**", "**/contract/**", "libs/api-contracts/**")
        runOnlyInChangedModules = false
    }
}
```

---

## 📊 Мониторинг и метрики

### Что отслеживать

1. **Время выполнения**
    - До: полный прогон всех тестов
    - После: impact tests
    - Процент экономии

2. **Покрытие**
    - Все ли необходимые тесты запускаются
    - Нет ли ложных пропусков

3. **Стабильность**
    - Количество false positives
    - Количество missed issues

### Дашборд (пример для Grafana)

```yaml
# Метрики для Prometheus
impact_analysis_execution_time_seconds
impact_analysis_changed_files_total
impact_analysis_affected_modules_total
impact_analysis_tests_executed_total
impact_analysis_time_saved_seconds
```

---

## 🎯 Критерии успеха

### Week 1-2: Пилот

- ✅ Плагин успешно установлен
- ✅ Анализ работает корректно
- ✅ Результаты логируются

### Week 3-4: Валидация

- ✅ Impact tests находят те же проблемы что и full tests
- ✅ Время экономится в среднем на 40%+
- ✅ Нет критических false negatives

### Week 5-6: Раскатка

- ✅ Работает на feature ветках
- ✅ Команда довольна результатами
- ✅ CI/CD стал быстрее

### Week 7+: Production

- ✅ Все PR используют impact tests
- ✅ Метрики собираются и анализируются
- ✅ ROI положительный

---

## 🚧 Потенциальные проблемы и решения

### Проблема 1: Плагин не находит модули

**Причина:** Нестандартная структура проекта

**Решение:**

```kotlin
// Можно вручную указать модули если нужно
impactAnalysis {
    // Плагин автоматически найдет через Gradle API
    // Но можно добавить логирование для отладки
}
```

### Проблема 2: Тесты пропускаются

**Причина:** Слишком строгие правила `whenChanged`

**Решение:**

```kotlin
unitTests {
    // Более широкие паттерны
    whenChanged("**/*.kt", "**/*.java")
    runOnlyInChangedModules = false
}
```

### Проблема 3: Граф зависимостей неполный

**Причина:** Динамические зависимости

**Решение:**

```kotlin
// Запускать все тесты для критических изменений
criticalPaths.set(listOf(
    "build.gradle",
    "dependencies.gradle"
))
runAllTestsOnCriticalChanges.set(true)
```

---

## 📞 Поддержка

- **Документация:** [README.md](README.md)
- **Быстрый старт:** [QUICK_START.md](QUICK_START.md)
- **Архитектура:** [ARCHITECTURE.md](ARCHITECTURE.md)
- **Примеры:** [examples/](examples/)

---

## ✅ Чеклист внедрения

- [ ] Собрать плагин (`./gradlew build`)
- [ ] Протестировать на тестовом проекте
- [ ] Опубликовать плагин
- [ ] Подключить в целевом проекте
- [ ] Настроить под специфику проекта
- [ ] Запустить первый анализ
- [ ] Интегрировать с CI/CD
- [ ] Запустить пилот на неделю
- [ ] Собрать метрики
- [ ] Раскатить на все ветки
- [ ] Настроить мониторинг
- [ ] Обучить команду

**Удачи с внедрением! 🚀**
