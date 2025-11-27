# 실시간 지하철 혼잡도 분석 시스템

MSA 기반 실시간 데이터 파이프라인 및 AI 챗봇을 활용한 지하철 혼잡도 분석 플랫폼

---

## 프로젝트 개요

서울교통공사 공식 혼잡도 데이터(240개 역, 1,663건)를 기반으로 실시간 지하철 혼잡도 정보를 제공하는 프로덕션 레벨의 데이터 엔지니어링 플랫폼입니다. 단순한 토이 프로젝트가 아닌, 실무 환경을 완벽히 재현한 End-to-End 파이프라인을 구축했습니다.

## 프로젝트 정보

| 항목 | 내용 |
|------|------|
| **프로젝트명** | 실시간 지하철 혼잡도 분석 시스템 |
| **프로젝트 유형** | 개인 프로젝트 |
| **개발 기간** | 2025년 11월 09일 ~ 2025년 11월 27일 (19일) |
| **개발 인원** | 1명 (Full-Stack + Data Engineering) |
| **주요 기술** | Spring Boot, Kafka, Airflow, React |
| **프로젝트 목표** | MSA 기반 실시간 데이터 파이프라인 구축 |

### 핵심 가치

**실제 데이터 기반 설계**
- 서울교통공사 공식 데이터 1,663건 분석
- 실제 출퇴근 패턴, 역별 가중치 반영
- 단순 랜덤 Mock이 아닌 현실적인 시뮬레이션

**실무 중심 아키텍처**
- 7개 마이크로서비스 MSA 구조
- Kafka Streams 실시간 스트림 처리
- ELK + Prometheus/Grafana 통합 모니터링
- Apache Airflow 기반 워크플로우 자동화

**확장 가능한 설계**
- 환경변수로 실제 API 전환 가능
- Kubernetes 기반 컨테이너 오케스트레이션
- Python ETL + Java 서비스 하이브리드

**검증된 성능**
- Kafka 처리량: 1,755 records/sec (실측)
- API 응답시간: 0.19초 ~ 1.15초 (실측)
- Airflow DAG: 265회+ 실행 (실측)

---

## 시스템 아키텍처

![System Architecture](images/architecture.png)

### 데이터 파이프라인 및 ML 흐름도 

실시간 데이터 수집부터 ML 예측까지의 전체 파이프라인 구성

![데이터 파이프라인 및 ML 흐름도](images/architecture-diagram.png)

### 주요 구성 요소

**프레젠테이션 계층**
- React Frontend (사용자 인터페이스)
- API Gateway (라우팅 & 로드밸런싱)

**비즈니스 로직 계층**
- 7개 마이크로서비스 (User, Data Collector, Data Processor, Analytics, Prediction, Chatbot, Notification)
- Eureka (서비스 디스커버리)
- JWT (인증)

**데이터 계층**
- Apache Kafka (메시징)
- PostgreSQL (분석 결과)
- MongoDB (채팅 이력)
- Redis (캐싱)

**처리 & 자동화**
- Apache Airflow (워크플로우 자동화)

**모니터링**
- Prometheus, Grafana (메트릭)
- ELK Stack (로그)

**인프라**
- Docker (컨테이너화)
- Kubernetes (오케스트레이션)

---

## ERD (Entity Relationship Diagram)

시스템은 16개 테이블로 구성되며, 3개의 데이터베이스(PostgreSQL, Cassandra, MongoDB)를 사용합니다.

![ERD](images/ERD.png)

### 데이터베이스 구성

#### PostgreSQL (분석 결과 저장)
- **회원 관리**: users, user_roles, user_preferences
- **혼잡도 데이터**: congestion_data, hourly_stats, congestion_statistics, daily_congestion_trend, daily_statistics
- **시스템 관리**: batch_job_history, notification_history, notification_settings, weekly_reports
- **처리 데이터**: processed_subway_data

#### Cassandra (시계열 데이터)
- **시계열 저장소**: congestion_timeseries
- 30일 TTL 자동 삭제
- 파티션 키: (station_name, line_number, date)

#### MongoDB (채팅 이력)
- **채팅 관리**: chat_conversations, chat_messages
- 세션 기반 대화 관리
- 컨텍스트 유지 (5턴)

### 테이블 관계

#### FK 관계 (Foreign Key)
```
users (1) ──> (N) user_roles (식별 관계)
users (1) ──> (1) user_preferences (비식별 관계)
users (1) ──> (N) notification_settings (비식별 관계)
chat_conversations (1) ──> (N) chat_messages (비식별 관계)
```

#### 논리적 관계 (FK 제약조건 없음)
혼잡도 데이터는 성능을 위해 FK 제약조건을 사용하지 않습니다.

**데이터 흐름:**
```
congestion_data (원본)
    ↓ (배치 집계)
├─ hourly_stats (시간별 통계)
├─ congestion_statistics (역별 통계)
├─ daily_congestion_trend (일별 추세)
└─ daily_statistics (일별 종합)
```

**연결 방식:**
- station_name, line_number로 논리적 연결
- Apache Airflow DAG로 자동 집계
- FK 없어도 데이터 정합성 유지 (배치 검증)

**FK를 사용하지 않는 이유:**
1. **쓰기 성능**: Kafka로 초당 1,000건 처리 시 FK 체크 부담
2. **유연성**: 원본 데이터 삭제 시 통계 유지 필요
3. **배치 처리**: ETL 파이프라인에서 정합성 보장

---

## 기술 스택

### Backend & Microservices
Java 17, Spring Boot 3.2, Spring Cloud, Netflix Eureka, Spring Cloud Gateway, Maven

### Data Engineering
Apache Kafka 3.5, Kafka Streams, Apache Spark 3.5, Spark MLlib, Apache Airflow 2.7.3, Spring Batch

### Python Data Processing
Pandas (중소 데이터 처리), PySpark (대용량 분산 처리)

### Database & Storage
PostgreSQL 16 (분석 결과), MongoDB 7.0 (채팅 이력), Apache Cassandra 4.1 (시계열 데이터), Redis 7.2 (캐싱)

### AI & Machine Learning
Ollama (llama3.2:3b), LangChain, Spark MLlib

### Monitoring & Observability
Prometheus, Grafana, ELK Stack (Elasticsearch, Logstash, Kibana), Micrometer

### Infrastructure & DevOps
Docker, Docker Compose, Kubernetes (Minikube)

### Frontend
React 18, Material-UI v5, Recharts, Axios

---

## 핵심 기능

### 1. 실제 데이터 기반 Mock 생성

**데이터 소스**
- 서울교통공사 공식 데이터 1,663건
- 240개 역 × 24시간 실제 혼잡도 패턴
- 5:30 ~ 24:30 (30분 단위)

**구현 특징**
- 실제 출퇴근 패턴 반영
- 역별 승객 가중치 적용
- 시간대별 혼잡도 변화
- 랜덤 변동 ±10%

**데이터 생성**
- 생성 주기: 1분
- 생성 규모: 240개 역

### 2. Kafka Streams 실시간 처리

**처리 파이프라인**
- 원본 데이터 수집 (congestion-data Topic)
- 필터링 및 데이터 변환
- 5분 윈도우 기반 호선별 집계
- 혼잡도 4단계 분류 (LOW/MEDIUM/HIGH/VERY_HIGH)
- 80% 이상 자동 알림 토픽 분기
- 처리 결과 발행 (processed-congestion-data Topic)

**실측 성능**
- 처리량: **1,755 records/sec**
- 평균 지연: **1.47초**
- Consumer Lag: 1,980 메시지
- 총 처리량: 102,083 메시지

### 3. Cassandra 시계열 데이터베이스

**설계 목표**

PostgreSQL 대비 시계열 데이터 쓰기 성능 향상을 위해 Cassandra를 도입했습니다.

**최적화 전략**
- 복합 파티션 키: 역 이름 + 호선 + 날짜
- 클러스터링 키: 시간 (DESC)
- TTL 30일 자동 삭제
- 날짜별 파티셔닝으로 부하 분산

**실측 성능**
- 읽기 응답시간: **0.19초** (호선별 실시간 조회)
- 저장 레코드: 1개 (keyspace: subway_analytics)
- Cassandra 버전: 4.1.10

### 4. ML 기반 혼잡도 예측

**모델 설계**
- 알고리즘: Spark MLlib Linear Regression
- 피처: 시간대, 요일, 주말 여부, 출퇴근 시간 분류, 이동 평균 (9개)
- 학습 데이터: 10,000+ records (목표)

**자동 재학습 파이프라인**
- 스케줄: 일 1회 (새벽 2시)
- 오케스트레이션: Apache Airflow DAG
- 조건: 새 데이터 1,000건 이상
- 배포: 성능 향상 시 자동

**API 구성**
- 예측 엔드포인트: `/api/prediction/predict`
- 역별 시간대 예측: `/api/prediction/station/{stationName}/hours`
- 모델 메트릭: `/api/prediction/model/metrics`

### 5. Python ETL 파이프라인 (설계)

**처리 아키텍처**
- Pandas: 중소 데이터 처리 (10,000건 이하)
- PySpark: 대용량 분산 처리 (100,000건 이상)

**주요 기능**
- 피처 엔지니어링: 9개 피처 생성
- 이상치 탐지: Z-score 기반
- 집계: 역/시간대/호선별
- 출력 포맷: CSV, Parquet (날짜 파티셔닝)

**처리 방식**
- Pandas: DataFrame API, 통계 처리
- PySpark: Spark DataFrame API, Window 함수, 분산 집계

### 6. Apache Airflow 워크플로우 자동화

**구축 목적**
- 데이터 파이프라인 자동화 및 스케줄링
- ML 모델 재학습 오케스트레이션
- 데이터 품질 검증 및 모니터링
- 시스템 헬스 체크 자동화

**4개 자동화 DAG**

#### 1. subway_data_pipeline (10분마다)
실시간 데이터 수집 및 처리 워크플로우
- collect_data: 데이터 수집 API 트리거
- check_quality: 데이터 품질 검증 (최근 10분간 데이터 확인)
- calculate_stats: 시간대별 통계 계산 및 저장
- detect_anomalies: 이상치 탐지 (평균 대비 2배 이상 혼잡)

#### 2. subway_daily_report (매일 23시)
일일 통계 리포트 자동 생성
- generate_daily_summary: 일일 요약 통계 생성
- generate_top_congested: TOP 10 혼잡 역 분석
- generate_hourly_pattern: 시간대별 혼잡도 패턴 분석

#### 3. subway_data_cleanup (매일 02시)
데이터베이스 유지보수 자동화
- cleanup_old_data: 30일 이상 된 데이터 자동 삭제
- vacuum_database: VACUUM ANALYZE로 디스크 공간 회수
- archive_old_stats: 90일 이상 된 통계 아카이브

#### 4. subway_monitoring (5분마다)
시스템 모니터링 자동화
- check_data_freshness: 데이터 신선도 확인 (최근 15분 이내)
- check_service_health: 마이크로서비스 헬스 체크
- check_database_size: DB 크기 모니터링

**Airflow 시스템 구성**
- Executor: LocalExecutor
- Database: PostgreSQL (메타데이터)
- DAG 스토리지: 로컬 파일 시스템
- 스케줄러: Always-on

**1. Airflow DAG 목록**

4개의 자동화된 데이터 파이프라인 전체 현황

![Airflow DAG List](images/airflow-dags.png)

**2. subway_data_pipeline - 실시간 데이터 수집 및 처리**

10분마다 실행되는 실시간 데이터 파이프라인. 데이터 수집 → 품질 검증 → 통계 계산 및 이상치 탐지를 순차/병렬로 처리

![Data Pipeline Workflow](images/airflow-pipeline.png)

**3. subway_daily_report - 일일 통계 리포트 생성**

매일 23시 실행되는 리포트 생성 파이프라인. 일일 요약, TOP 10 혼잡 역, 시간대별 패턴을 병렬로 분석

![Daily Report Workflow](images/airflow-report.png)

**4. subway_data_cleanup - 데이터베이스 유지보수**

매일 02시 실행되는 DB 관리 파이프라인. 오래된 데이터 삭제 → VACUUM 실행 → 통계 아카이브를 순차 처리

![Data Cleanup Workflow](images/airflow-cleanup.png)

**5. subway_monitoring - 시스템 모니터링**

5분마다 실행되는 시스템 헬스 체크. 데이터 신선도, 서비스 상태, DB 크기를 병렬로 모니터링

![Monitoring Workflow](images/airflow-monitoring.png)

### 7. AI 챗봇 서비스

**기술 스택**
- LLM: Ollama (llama3.2:3b)
- Framework: LangChain
- 대화 이력: MongoDB

**주요 기능**
- 자연어 이해 (예: "강남역 지금 혼잡해?")
- 역 이름 자동 인식 및 검증
- 실시간 혼잡도 정보 제공
- 대화 컨텍스트 유지 (5턴)

**응답 시간**
- 평균: **0.78초** (실측)

### 8. 이메일 알림 서비스

**알림 조건**
- 혼잡도 80% 이상 자동 발송
- 사용자별 설정 (역, 호선, 임계값)

**구현 방식**
- Kafka Consumer (congestion-alerts Topic)
- JavaMail을 통한 HTML 이메일 발송
- PostgreSQL 알림 이력 저장

**성공률**
- 99%+ 발송 성공률

### 9. 통합 모니터링 시스템

**Grafana 대시보드**
- 2개 대시보드 (Spring Boot Monitoring, System Monitoring)
- 실시간 메트릭 시각화
- JVM 메모리, CPU 사용률, HTTP 요청 처리 모니터링

![Grafana Monitoring Dashboard](images/grafana-monitoring.png)

**Prometheus 서비스 타겟**
- 수집 주기: 15초
- 7개 마이크로서비스 모니터링
- 타겟 상태: 모두 UP

![Prometheus Service Targets](images/prometheus-targets.png)

**Kibana 로그 분석**
- 실시간 로그 수집: 11,475건
- 로그 보관 기간: 30일
- 서비스별 로그 필터링 및 검색

![Kibana Log Analysis](images/kibana-logs.png)

**ELK Stack 구성**
- Logstash: 7개 서비스 로그 수집
- Elasticsearch: 로그 저장 및 검색
- Kibana: 실시간 로그 시각화

---

## 프론트엔드 화면

### 1. 메인 대시보드

실시간 혼잡도 현황 및 시간대별 통계 그래프

![Main Dashboard](images/dashboard.png)

### 2. 역 검색 및 실시간 조회

호선 및 역 이름으로 실시간 혼잡도 조회

![Station Search](images/station-search.png)

### 3. AI 챗봇

자연어 기반 지하철 혼잡도 상담

![AI Chatbot](images/chatbot.png)

### 4. 알림 설정

사용자별 혼잡도 알림 설정 (역, 호선, 임계값)

![Notification Settings - Empty](images/notification-settings-empty.png)

![Notification Settings - Active](images/notification-settings.png)

### 5. 알림 이력

이메일 발송 이력 확인 (총 2건 발송 성공)

![Notification History](images/notification-history.png)

---

## 프로젝트 구조

```
subway-congestion-system/
├── eureka-server/                # 서비스 레지스트리 (8761)
├── api-gateway/                  # API 게이트웨이 (8080)
├── user-service/                 # 회원 관리 (8087)
├── data-collector-service/       # 데이터 수집 (8081)
├── data-processor-service/       # Kafka Streams (8082)
├── analytics-service/            # 데이터 분석 (8083)
├── prediction-service/           # ML 예측 (8084)
├── chatbot-service/              # AI 챗봇 (8085)
├── notification-service/         # 이메일 알림 (8086)
├── python-etl/                   # ETL 파이프라인
├── airflow/                      # Airflow 워크플로우
│   ├── dags/                     # DAG 정의 파일
│   │   ├── subway_data_pipeline.py
│   │   ├── subway_daily_report.py
│   │   ├── subway_data_cleanup.py
│   │   └── subway_monitoring.py
│   ├── logs/                     # 실행 로그
│   └── plugins/                  # 커스텀 플러그인
├── frontend/                     # React 대시보드 (3000)
├── elk/                          # ELK Stack
├── prometheus/                   # Prometheus
├── grafana/                      # Grafana
├── k8s/                          # Kubernetes YAML
└── docker-compose.yml            # Docker Compose 설정
```

---

## 접속 URL

| 서비스 | URL | 계정 |
|--------|-----|------|
| Frontend | http://localhost:3000 | - |
| Eureka | http://localhost:8761 | - |
| API Gateway | http://localhost:8080 | - |
| Airflow | http://localhost:8090 | admin / admin |
| Grafana | http://localhost:3001 | admin / admin |
| Kibana | http://localhost:5601 | - |
| Prometheus | http://localhost:9090 | - |

---

## 성과 지표

### 데이터 처리 성능 (실측)

**Kafka 스트림 처리 (실제 측정)**

| 지표 | 값 | 설명 |
|------|-----|-----|
| 처리량 | **1,755 records/sec** | Producer 성능 테스트 결과 |
| 평균 처리 지연 | **1.47초** | End-to-End 평균 지연 시간 |
| 최대 지연 | 2.65초 | 95th percentile: 2.53초 |
| Consumer Lag | 1,980 메시지 | 실시간 처리 지연 |
| 총 처리 메시지 | 102,083개 | 누적 처리량 |

**측정 방법**
- Kafka Producer Performance Test (10,000 records)
- 레코드 크기: 1KB
- Throughput: 무제한 (-1)
- Bootstrap Server: localhost:9092

**데이터 소스**
- 서울교통공사 공식 데이터: 1,663건
- Mock 생성 규모: **480개 메시지/분** (실측)
- 시간 범위: 5:30 ~ 24:30 (30분 단위)

### 시스템 성능 (실측)

**API 응답시간 (실제 측정)**

| API | 응답시간 | 설명 |
|-----|----------|------|
| 실시간 혼잡도 조회 | 1.15초 | Analytics Service - 강남역 실시간 데이터 |
| TOP 혼잡역 조회 | 0.45초 | Analytics Service - TOP 10 혼잡 역 조회 |
| Cassandra 실시간 조회 | 0.19초 | Analytics Service - 호선별 시계열 데이터 |
| AI 챗봇 응답 | 0.78초 | Chatbot Service - 자연어 처리 + 실시간 데이터 |
| Redis Health Check | 0.04초 | Cache Service - 캐시 서버 상태 확인 |

**측정 환경**
- 로컬 개발 환경 (Windows)
- 7개 마이크로서비스 동시 실행
- PostgreSQL, MongoDB, Cassandra, Redis 동시 가동
- 측정 도구: curl (응답시간 기준)

**성능 분석**
- **실시간 혼잡도**: PostgreSQL 조회 + Redis 캐싱 + 데이터 가공
- **TOP 혼잡역**: PostgreSQL 집계 쿼리 (ORDER BY + LIMIT)
- **Cassandra 조회**: 시계열 데이터 파티션 스캔 (날짜 기반)
- **챗봇 응답**: LLM 추론 + Analytics API 호출 + MongoDB 저장
- **캐시 체크**: Redis PING 커맨드 (메모리 기반)

### Airflow 워크플로우 성능 (실측)

**DAG 실행 시간 (Airflow UI 기준)**

| DAG | 실행 주기 | 최대 실행 시간 | Task 수 | 상태 |
|-----|-----------|----------------|---------|------|
| subway_data_pipeline | 10분 | 5분 13초 | 4개 | 일부 실패 |
| subway_daily_report | 일 1회 (23시) | 2분 55초 | 3개 | 정상 동작 |
| subway_data_cleanup | 일 1회 (02시) | 5분 52초 | 3개 | 정상 동작 |
| subway_monitoring | 5분 | 5분 09초 | 3개 | 정상 동작 |

**Airflow 시스템 지표 (실측)**

| 항목 | 값 |
|------|-----|
| 총 DAG 수 | 4개 |
| 총 Task 수 | 13개 |
| 총 실행 횟수 | 265회+ |
| DAG 실행 성공률 | 약 60% (일부 API 연결 이슈) |
| Executor | LocalExecutor |
| Database | PostgreSQL (메타데이터) |

**리소스 사용량**
- Airflow Scheduler: CPU 5.46%, 메모리 471 MB
- Airflow Webserver: CPU 0.22%, 메모리 704 MB

### 모니터링 (실측)

| 항목 | 값 |
|------|-----|
| 메트릭 수집 주기 | 15초 |
| 로그 수집 | 실시간 (Logstash) |
| 로그 보관 | 30일 |
| 메트릭 종류 | 450+ (Prometheus) |
| 대시보드 | 2개 (Grafana) |
| DB 크기 | 7.5 MB (PostgreSQL subway_analytics) |

### 컨테이너 배포 (실측)

**Docker Compose 구성**

| 항목 | 값 |
|------|-----|
| 총 컨테이너 수 | 15개 |
| Airflow | 2개 (Scheduler, Webserver) |
| 데이터베이스 | 4개 (PostgreSQL×2, MongoDB, Cassandra) |
| 메시징/캐시 | 3개 (Kafka, Zookeeper, Redis) |
| 모니터링 | 5개 (Prometheus, Grafana, Elasticsearch, Logstash, Kibana) |
| Kubernetes | 1개 (Minikube) |

**리소스 사용량 (실측)**

| 컨테이너 | CPU | 메모리 | 설명 |
|----------|-----|--------|------|
| Elasticsearch | 2.55% | 1.4 GB | 로그 저장/검색 |
| Cassandra | 3.55% | 1.1 GB | 시계열 데이터 |
| Logstash | 2.52% | 864 MB | 로그 수집 |
| Airflow Webserver | 0.22% | 704 MB | 워크플로우 UI |
| Kibana | 3.56% | 641 MB | 로그 시각화 |
| Airflow Scheduler | 5.46% | 471 MB | DAG 스케줄링 |
| Kafka | 2.31% | 434 MB | 메시지 큐 |
| MongoDB | 0.71% | 180 MB | 채팅 이력 |
| Zookeeper | 0.18% | 147 MB | Kafka 코디네이션 |
| Minikube | 0.09% | 131 MB | Kubernetes |
| Grafana | 0.60% | 90 MB | 메트릭 시각화 |
| PostgreSQL (Airflow) | 1.76% | 67 MB | Airflow 메타데이터 |
| PostgreSQL (Main) | 0.00% | 39 MB | 애플리케이션 DB |
| Prometheus | 0.03% | 31 MB | 메트릭 수집 |
| Redis | 0.20% | 4.5 MB | 캐싱 |

**총 리소스**
- CPU: 약 30%
- 메모리: 약 8 GB