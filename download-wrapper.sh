#!/bin/bash

# Скрипт для скачивания gradle-wrapper.jar

echo "Скачиваю gradle-wrapper.jar..."

URL="https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar"
OUTPUT="gradle/wrapper/gradle-wrapper.jar"

# Создаем директорию если не существует
mkdir -p "$(dirname "$OUTPUT")"

# Скачиваем файл
if command -v curl &> /dev/null; then
    curl -L "$URL" -o "$OUTPUT"
elif command -v wget &> /dev/null; then
    wget "$URL" -O "$OUTPUT"
else
    echo "✗ Ошибка: curl или wget не найдены"
    echo "Установите curl или wget, или скачайте вручную:"
    echo "  URL: $URL"
    echo "  Сохраните как: $OUTPUT"
    exit 1
fi

if [ -f "$OUTPUT" ]; then
    echo "✓ gradle-wrapper.jar успешно скачан!"
    echo ""
    echo "Дайте права на выполнение gradlew:"
    chmod +x gradlew
    echo ""
    echo "Теперь вы можете запустить:"
    echo "  ./gradlew build"
    echo "  ./gradlew test"
else
    echo "✗ Ошибка при скачивании"
    echo "Скачайте вручную:"
    echo "  URL: $URL"
    echo "  Сохраните как: $OUTPUT"
    exit 1
fi
