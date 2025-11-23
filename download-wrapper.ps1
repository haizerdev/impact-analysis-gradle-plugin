# Скрипт для скачивания gradle-wrapper.jar

Write-Host "Скачиваю gradle-wrapper.jar..." -ForegroundColor Green

$url = "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar"
$output = "gradle/wrapper/gradle-wrapper.jar"

# Создаем директорию если не существует
$dir = Split-Path -Parent $output
if (-not (Test-Path $dir)) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
}

try {
    # Скачиваем файл
    Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing
    Write-Host "✓ gradle-wrapper.jar успешно скачан!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Теперь вы можете запустить:" -ForegroundColor Yellow
    Write-Host "  .\gradlew.bat build" -ForegroundColor Cyan
    Write-Host "  .\gradlew.bat test" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Ошибка при скачивании: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Скачайте вручную:" -ForegroundColor Yellow
    Write-Host "  1. Откройте в браузере: $url" -ForegroundColor Cyan
    Write-Host "  2. Сохраните как: $output" -ForegroundColor Cyan
}
