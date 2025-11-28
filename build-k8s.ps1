# - Maven quiet (-q)
# - Docker quiet (-q)
# - 간결한 로그

Write-Host "=== Kubernetes Image Build ===" -ForegroundColor Cyan

# Minikube Docker 환경
& minikube -p minikube docker-env --shell powershell | Invoke-Expression

$services = @(
    @{Name="eureka-server"; Port=8761; Dir="eureka-server"},
    @{Name="api-gateway"; Port=8080; Dir="api-gateway"},
    @{Name="analytics-service"; Port=8083; Dir="analytics-service"},
    @{Name="chatbot-service"; Port=8085; Dir="chatbot-service"},
    @{Name="data-collector-service"; Port=8081; Dir="data-collector-service"}
)

foreach ($svc in $services) {
    Write-Host "`n[$($svc.Name)]" -ForegroundColor Green

    cd "D:\subway-congestion-system\$($svc.Dir)"

    # Dockerfile 생성
    @"
FROM eclipse-temurin:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE $($svc.Port)
ENTRYPOINT ["java", "-jar", "app.jar"]
"@ > Dockerfile

    # 테스트 삭제
    if (Test-Path "src\test") { Remove-Item -Recurse -Force "src\test" }

    # Maven 빌드
    mvn clean package -DskipTests -q

    # Docker 빌드
    docker build -t "subway/$($svc.Name):latest" . -q

    Write-Host "Built" -ForegroundColor Cyan
}

cd D:\subway-congestion-system

Write-Host "`n=== Done ===" -ForegroundColor Green
Write-Host "Run: kubectl apply -f k8s/namespace.yaml" -ForegroundColor Yellow