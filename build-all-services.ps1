# - Eclipse Temurin 사용
# - 테스트 폴더 자동 삭제
# - 상세한 에러 처리

Write-Host "Building all services for Kubernetes..." -ForegroundColor Cyan

# Minikube Docker 환경 사용
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

$services = @(
    @{Name="eureka-server"; Port=8761},
    @{Name="api-gateway"; Port=8080},
    @{Name="analytics-service"; Port=8083},
    @{Name="chatbot-service"; Port=8085},
    @{Name="data-collector-service"; Port=8081}
)

foreach ($service in $services) {
    $serviceName = $service.Name
    $servicePort = $service.Port

    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "Building $serviceName..." -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green

    Set-Location "D:\subway-congestion-system\$serviceName"

    # Dockerfile 생성 (Eclipse Temurin 사용)
    @"
FROM eclipse-temurin:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE $servicePort
ENTRYPOINT ["java", "-jar", "app.jar"]
"@ | Out-File -FilePath Dockerfile -Encoding UTF8 -NoNewline

    # 테스트 파일 삭제 (있으면)
    if (Test-Path "src\test") {
        Write-Host "Removing test directory..." -ForegroundColor Yellow
        Remove-Item -Recurse -Force "src\test" -ErrorAction SilentlyContinue
    }

    # Maven 빌드
    Write-Host "Building with Maven..." -ForegroundColor Yellow
    mvn clean package -DskipTests

    if ($LASTEXITCODE -eq 0) {
        # Docker 이미지 빌드
        Write-Host "Building Docker image..." -ForegroundColor Yellow
        docker build -t "subway/$serviceName:latest" .

        if ($LASTEXITCODE -eq 0) {
            Write-Host "$serviceName built successfully!" -ForegroundColor Cyan
        } else {
            Write-Host "Docker build failed for $serviceName" -ForegroundColor Red
            Set-Location "D:\subway-congestion-system"
            exit 1
        }
    } else {
        Write-Host "Maven build failed for $serviceName" -ForegroundColor Red
        Set-Location "D:\subway-congestion-system"
        exit 1
    }
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "All services built successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "`nDocker images:" -ForegroundColor Cyan
docker images | Select-String "subway"

Set-Location "D:\subway-congestion-system"

Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "  cd k8s" -ForegroundColor White
Write-Host "  kubectl apply -f namespace.yaml" -ForegroundColor White
Write-Host "  kubectl apply -f infrastructure/" -ForegroundColor White
Write-Host "  kubectl apply -f services/" -ForegroundColor White