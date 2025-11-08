# 지하철 혼잡도 분석 시스템

데이터 엔지니어링 학습 및 포트폴리오 프로젝트

## 프로젝트 소개

서울시 실시간 지하철 데이터를 수집·분석하여 혼잡도 정보를 제공하는 시스템입니다.
MSA 아키텍처와 빅데이터 처리 기술을 학습하며 실제 프로덕션 환경을 고려하여 설계했습니다.

**학습 목표**
- 실시간 데이터 파이프라인 구축
- 대용량 데이터 처리 및 분석
- MSA 기반 서비스 설계 및 구현
- AI/LLM 서비스 통합

## 기술 스택

### Backend
- Java 17, Spring Boot 3.2
- Spring Cloud (Eureka, Gateway)
- Maven Multi-module

### Data Pipeline
- Apache Kafka - 실시간 스트리밍
- Apache Spark - 분산 데이터 처리
- Apache Airflow - 워크플로우 스케줄링
- Spring Batch - 배치 처리

### Database
- MongoDB - 원본 데이터 저장
- PostgreSQL - 분석 결과 저장
- Redis - 캐싱

### AI
- Ollama - LLM
- LangChain - AI 체인 구성
- Spark MLlib - 머신러닝

### Infrastructure
- Docker Compose - 로컬 개발 환경
- Minikube - 쿠버네티스 학습

### Frontend
- React

## 시스템 아키텍처
```
서울 열린데이터 API
        ↓
Data Collector Service (수집)
        ↓
    Kafka (스트리밍)
        ↓
Data Processor Service (전처리)
        ↓
    Spark (분석)
        ↓
Analytics Service (통계/예측)
        ↓
API Gateway (라우팅)
        ↓
    API Service / Chatbot Service
        ↓
    React Frontend
```

**데이터 저장소**
- MongoDB: 원본 실시간 데이터
- PostgreSQL: 집계/통계 데이터
- Redis: API 응답 캐시

## 주요 기능

### 완료된 기능
- 실시간 지하철 위치 데이터 수집 (1분 주기)
- Kafka를 통한 데이터 스트리밍
- MongoDB 원본 데이터 저장
- MSA 기반 서비스 디스커버리 (Eureka)

### 구현 예정
- Spark를 활용한 데이터 전처리 및 집계
- 혼잡도 패턴 분석 및 예측 모델
- LLM 기반 대화형 챗봇
- 사용자 맞춤 알림 서비스
- 실시간 대시보드

## 프로젝트 구조
```
subway-congestion-system/
├── eureka-server/              # 서비스 레지스트리
├── api-gateway/                # API 게이트웨이
├── data-collector-service/     # 데이터 수집
├── data-processor-service/     # 전처리 (예정)
├── analytics-service/          # 분석 (예정)
├── chatbot-service/            # 챗봇 (예정)
├── api-service/                # API (예정)
└── docker-compose.yml          # 인프라 구성
```

## 개발 진행 상황

### Phase 1: 인프라 구축 (완료)
- [x] Maven 멀티모듈 프로젝트 구성
- [x] Eureka Server
- [x] API Gateway
- [x] Docker Compose 인프라 설정

### Phase 2: 데이터 수집 (완료)
- [x] 서울 Open API 연동
- [x] Kafka Producer 구현
- [x] MongoDB 저장
- [x] 스케줄링 (자동 수집)

### Phase 3: 데이터 처리 (진행 예정)
- [ ] Kafka Consumer
- [ ] Spark 전처리 파이프라인
- [ ] PostgreSQL 적재

### Phase 4: 데이터 분석 (진행 예정)
- [ ] Spark SQL 집계
- [ ] 혼잡도 패턴 분석
- [ ] MLlib 예측 모델

### Phase 5: 서비스 구현 (진행 예정)
- [ ] REST API
- [ ] Ollama + LangChain 챗봇
- [ ] Redis 캐싱

### Phase 6: 프론트엔드 (진행 예정)
- [ ] React 대시보드
- [ ] 실시간 지도 시각화

## 실행 방법

### 사전 요구사항
- JDK 17
- Maven 3.8+
- Docker & Docker Compose

### 2. 인프라 실행
```bash
docker-compose up -d
```

실행되는 서비스:
- MongoDB (27017)
- Kafka + Zookeeper (9092)
- PostgreSQL (5432)
- Redis (6379)

### 3. 애플리케이션 실행
```bash
# 전체 빌드
mvn clean install

# 각 서비스 실행 (별도 터미널)
cd eureka-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd data-collector-service && mvn spring-boot:run
```

[//]: # (### 4. 동작 확인)

[//]: # ()
[//]: # (- Eureka: http://localhost:8761)

[//]: # (- 서비스 상태: http://localhost:8081/collect/status)

[//]: # (- 수동 데이터 수집:)

[//]: # (```bash)

[//]: # (  curl -X POST "http://localhost:8081/collect/manual?stationName=강남")

[//]: # (```)

## 설정

[//]: # (### API 키 발급)

[//]: # (서울 열린데이터광장: https://data.seoul.go.kr)

[//]: # ()
[//]: # (`data-collector-service/src/main/resources/application.yml`)

[//]: # (```yaml)

[//]: # (subway:)

[//]: # (  api:)

[//]: # (    key: 발급받은_API_키)

[//]: # (```)

[//]: # (### 수집 대상 역 설정)

[//]: # (`application.yml`에서 수정 가능:)

[//]: # (```yaml)

[//]: # (subway:)

[//]: # (  target-stations:)

[//]: # (    - 강남)

[//]: # (    - 신림)

[//]: # (    - 잠실)

[//]: # (    # 추가 가능)

[//]: # (```)

## 개발 환경

- OS: Windows 11
- IDE: IntelliJ IDEA 2024.x
- Build Tool: Maven
- Version Control: Git

[//]: # (## 참고 자료)

[//]: # ()
[//]: # (- 서울 열린데이터광장 API: https://data.seoul.go.kr)

[//]: # (- Spring Cloud Netflix: https://spring.io/projects/spring-cloud-netflix)

[//]: # (- Apache Kafka Docs: https://kafka.apache.org/documentation/)

[//]: # (- Apache Spark Docs: https://spark.apache.org/docs/latest/)


---

**포트폴리오 프로젝트 진행 중**  
데이터 엔지니어 취업 준비를 위한 실전 프로젝트입니다.