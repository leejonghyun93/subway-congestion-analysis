#- Bash 스크립트
#- 빌드+배포 통합
#- Pod 상태 자동 확인

#!/bin/bash

echo "Building Docker images..."
eval $(minikube docker-env)

services=("eureka-server" "api-gateway" "analytics-service" "chatbot-service" "data-collector-service")

for service in "${services[@]}"; do
    echo "Building $service..."
    cd $service
    mvn clean package -DskipTests
    docker build -t subway/$service:latest .
    cd ..
done

echo "Deploying to Kubernetes..."
kubectl apply -f k8s/infrastructure/
kubectl apply -f k8s/services/

echo "Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod --all -n subway-system --timeout=300s

echo "Deployment complete!"
kubectl get pods -n subway-system
kubectl get svc -n subway-system