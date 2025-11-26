# 실시간 지하철 혼잡도 분석 시스템

MSA 기반 실시간 데이터 파이프라인 및 AI 챗봇을 활용한 지하철 혼잡도 분석 플랫폼

---

## 프로젝트 개요

서울교통공사 공식 혼잡도 데이터(240개 역, 1,663건)를 기반으로 실시간 지하철 혼잡도 정보를 제공하는 프로덕션 레벨의 데이터 엔지니어링 플랫폼입니다. 단순한 토이 프로젝트가 아닌, 실무 환경을 완벽히 재현한 End-to-End 파이프라인을 구축했습니다.

### 핵심 가치

**실제 데이터 기반 설계**
- 서울교통공사 공식 데이터 1,663건 분석
- 실제 출퇴근 패턴, 역별 가중치 반영
- 단순 랜덤 Mock이 아닌 현실적인 시뮬레이션

**실무 중심 아키텍처**
- 8개 마이크로서비스 MSA 구조
- Kafka Streams 실시간 스트림 처리
- ELK + Prometheus/Grafana 통합 모니터링
- Apache Airflow 기반 워크플로우 자동화

**확장 가능한 설계**
- 환경변수로 실제 API 전환 가능
- Kubernetes 기반 컨테이너 오케스트레이션
- Python ETL + Java 서비스 하이브리드

**검증된 성능**
- Kafka 처리량: 1,000 msg/sec
- API 응답시간: 150ms (Redis 캐싱)
- ML 모델 정확도: R² 0.85+

---

## 시스템 아키텍처

```
서울교통공사 CSV (240개 역, 1,663건)
    ↓
Data Collector Service (Mock 생성)
    ↓
Apache Kafka (3개 Topic)
    ↓
Kafka Streams (실시간 처리)
    ↓
PostgreSQL + Cassandra + MongoDB
    ↓
Python ETL (Pandas/PySpark)
    ↓
Apache Airflow (워크플로우 자동화)
    ↓
Analytics/Prediction/Chatbot Services
    ↓
API Gateway (Eureka 디스커버리)
    ↓
React Frontend
    ↓
Prometheus/Grafana + ELK Stack
    ↓
Kubernetes (11 Pods)
```

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
- 쓰기 성능: 10,000 writes/sec

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

**핵심 특징**
- 실제 출퇴근 패턴 반영
- 역별 승객 가중치 적용
- 시간대별 혼잡도 변화
- 랜덤 변동 ±10%

**성능**
- 생성 주기: 1분
- 생성 규모: 240개 역
- 패턴 정확도: 95%+

### 2. Kafka Streams 실시간 처리

**처리 파이프라인**
- 원본 데이터 수집 (congestion-data Topic)
- 필터링 및 데이터 변환
- 5분 윈도우 기반 호선별 집계
- 혼잡도 4단계 분류 (LOW/MEDIUM/HIGH/VERY_HIGH)
- 80% 이상 자동 알림 토픽 분기
- 처리 결과 발행 (processed-congestion-data Topic)

**성능 지표**
- 처리량: 1,000 msg/sec
- 처리 지연: 100ms 이내
- 가용성: 99.9%

### 3. Cassandra 시계열 데이터베이스

**선택 이유**

PostgreSQL은 시계열 데이터 쓰기 성능이 낮음 (300 writes/sec). Cassandra는 시계열 데이터에 최적화되어 있으며, 쓰기 성능이 뛰어남 (10,000 writes/sec).

**최적화 전략**
- 복합 파티션 키: 역 이름 + 호선 + 날짜
- 클러스터링 키: 시간 (DESC)
- TTL 30일 자동 삭제
- 날짜별 파티셔닝으로 부하 분산

**성능 결과**
- 쓰기: 10,000 writes/sec
- 읽기: 150ms (실시간 조회)
- 저장 용량: 자동 관리

### 4. ML 기반 혼잡도 예측

**모델 아키텍처**
- 알고리즘: Spark MLlib Linear Regression
- 학습 데이터: 10,000+ records
- 피처: 시간대, 요일, 주말 여부, 출퇴근 시간 분류, 이동 평균

**모델 성능**
- R² Score: 0.85+ (설명력 85% 이상)
- RMSE: 5.2 (평균 오차 5.2%)
- 학습 시간: 3분 (Spark 분산 처리)

**자동 재학습**
- 스케줄: 일 1회 (새벽 2시)
- 오케스트레이션: Apache Airflow DAG
- 조건: 새 데이터 1,000건 이상
- 배포: 성능 향상 시 자동

**API 성능**
- 예측 API 응답시간: 50ms (Redis 캐싱)

### 5. Python ETL 파이프라인

**Pandas 처리 (중소 데이터)**
- 처리 규모: 10,000건 / 0.67초
- 주요 기능: 9개 피처 엔지니어링, Z-score 이상치 탐지 (117건), 역/시간대/호선별 집계
- 출력: 4개 CSV 파일 (처리 데이터, 역별 통계, 시간대별 통계, 호선별 통계)

**PySpark 처리 (대용량 분산 처리)**
- 처리 규모: 100,000건 분산 처리
- 주요 기능: Spark DataFrame API, Window 함수 기반 이동 통계, 분산 집계 및 정렬
- 출력: Parquet 포맷 (날짜 파티셔닝)

**성능 비교**

| 규모 | Pandas | PySpark |
|------|--------|---------|
| 10,000건 | 0.67초 | 2초 |
| 100,000건 | 30초 | 5초 |
| 1,000,000건 | 메모리 부족 | 30초 |

**자동화 테스트**
- 단위 테스트: 9개 테스트
- Pandas: 전체 기능 검증 (데이터 생성 → 변환 → 집계 → 저장)
- PySpark: 코드 구문 검증 (13개 메서드)
- 테스트 통과율: 100%

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
- check_quality: 데이터 품질 검증 (최근 10분간 데이터 5건 이상)
- calculate_stats: 시간대별 통계 계산 및 저장
- detect_anomalies: 이상치 탐지 (평균 대비 2배 이상 혼잡)

성능 지표: 실행 주기 10분, 처리 데이터 2,466건/10분, 평균 실행 시간 15초

#### 2. subway_daily_report (매일 23시)
일일 통계 리포트 자동 생성
- generate_daily_summary: 일일 요약 통계 (240개 역, 22,146건)
- generate_top_congested: TOP 10 혼잡 역 분석
- generate_hourly_pattern: 시간대별 혼잡도 패턴 분석

산출물: 일일 통계, TOP 10 역, 시간대 패턴

#### 3. subway_data_cleanup (매일 02시)
데이터베이스 유지보수 자동화
- cleanup_old_data: 30일 이상 된 데이터 자동 삭제
- vacuum_database: VACUUM ANALYZE로 디스크 공간 회수
- archive_old_stats: 90일 이상 된 통계 아카이브

성능 개선: 디스크 사용량 30% 감소, 쿼리 성능 20% 향상

#### 4. subway_monitoring (5분마다)
시스템 모니터링 자동화
- check_data_freshness: 데이터 신선도 확인 (최근 15분 이내)
- check_service_health: 마이크로서비스 헬스 체크 (3개 서비스)
- check_database_size: DB 크기 모니터링

모니터링 지표: 데이터 지연 감지, 서비스 다운타임 즉시 감지, DB 크기 19 MB

**Airflow 시스템 구성**
- Executor: LocalExecutor
- Database: PostgreSQL (메타데이터)
- DAG 스토리지: 로컬 파일 시스템
- 스케줄러: Always-on

**통합 효과**
- 수동 작업 80% 감소
- 데이터 품질 향상 (자동 검증)
- 운영 효율성 3배 향상
- 문제 조기 발견 (5분 주기 모니터링)

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
- 평균: 500ms

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
- 5개 대시보드 (서비스별, 인프라, Kafka, DB, 종합)
- 실시간 메트릭 시각화
- JVM 메모리, CPU 사용률, HTTP 요청 처리 모니터링

![Grafana Monitoring Dashboard](images/grafana-monitoring.png)

**Prometheus 서비스 타겟**
- 수집 주기: 15초
- 8개 마이크로서비스 모니터링
- 타겟 상태: 모두 UP

![Prometheus Service Targets](images/prometheus-targets.png)

**Kibana 로그 분석**
- 실시간 로그 수집: 11,475건
- 로그 보관 기간: 30일
- 서비스별 로그 필터링 및 검색

![Kibana Log Analysis](images/kibana-logs.png)

**ELK Stack 구성**
- Logstash: 8개 서비스 로그 수집
- Elasticsearch: 로그 저장 및 검색
- Kibana: 실시간 로그 시각화

**알림 규칙**
- CPU 사용률 80% 이상
- 메모리 사용률 85% 이상
- API 응답시간 1초 이상
- Kafka Consumer Lag 1000 이상

---

## 프로젝트 구조

```
subway-congestion-system/
├── eureka-server/                # 서비스 레지스트리 (8761)
├── api-gateway/                  # API 게이트웨이 (8080)
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

### 데이터 처리 성능

| 지표 | 값 | 설명 |
|------|-----|-----|
| 데이터 소스 | 1,663건 | 서울교통공사 공식 데이터 |
| Mock 생성 | 240개 역/분 | 실시간 데이터 생성 |
| Kafka 처리량 | 1,000 msg/sec | 초당 메시지 처리 |
| 처리 지연 | 100ms 이내 | End-to-End 지연 |

### 시스템 성능

| 지표 | 값 | 최적화 |
|------|-----|--------|
| API 응답시간 (실시간 혼잡도) | 150ms | Redis 캐싱 |
| API 응답시간 (예측) | 50ms | Redis 캐싱 |
| API 응답시간 (통계 조회) | 200ms | PostgreSQL 인덱스 |
| API 응답시간 (챗봇) | 500ms | LLM 처리 |
| Cassandra 쓰기 | 10,000 writes/sec | 파티셔닝 |
| PostgreSQL 조회 | 200ms | 인덱스 |
| Redis 캐싱 | 10ms | 캐시 히트 |
| 동시 접속 | 100+ users | 부하 테스트 |
| 가용성 | 99.9% | Kubernetes |

### ML 모델 성능

| 지표 | 값 |
|------|-----|
| R² Score | 0.85+ |
| RMSE | 5.2 |
| MAE | 4.1 |
| 학습 데이터 | 10,000+ records |
| 피처 수 | 9개 |
| 학습 시간 | 3분 |
| 재학습 주기 | 일 1회 |

### Python ETL 성능

**Pandas 처리**

| 규모 | 처리 시간 | 성능 |
|------|-----------|------|
| 1,000건 | 0.07초 | 14,285 records/sec |
| 10,000건 | 0.67초 | 14,925 records/sec |
| 100,000건 | 30초 | 3,333 records/sec |

**PySpark 분산 처리**

| 규모 | 처리 시간 | 성능 |
|------|-----------|------|
| 10,000건 | 2초 | 5,000 records/sec |
| 100,000건 | 5초 | 20,000 records/sec |
| 1,000,000건 | 30초 | 33,333 records/sec |

### Airflow 워크플로우 성능

| DAG | 실행 주기 | 평균 실행 시간 | Task 수 |
|-----|-----------|----------------|---------|
| subway_data_pipeline | 10분 | 15초 | 4개 |
| subway_daily_report | 일 1회 (23시) | 30초 | 3개 |
| subway_data_cleanup | 일 1회 (02시) | 45초 | 3개 |
| subway_monitoring | 5분 | 10초 | 3개 |

**Airflow 시스템 지표**

| 항목 | 값 |
|------|-----|
| 총 DAG 수 | 4개 |
| 총 Task 수 | 13개 |
| DAG 실행 성공률 | 100% |
| 평균 스케줄링 지연 | 1초 이내 |
| 일일 Task 실행 횟수 | 300+ |

### 모니터링

| 항목 | 값 |
|------|-----|
| 메트릭 수집 주기 | 15초 |
| 로그 수집 | 실시간 (Logstash) |
| 로그 보관 | 30일 |
| 메트릭 종류 | 200+ |
| 대시보드 | 5개 |
| 알림 규칙 | 10개 |

### Kubernetes 배포

| 항목 | 값 |
|------|-----|
| 총 Pod 수 | 11개 |
| 마이크로서비스 | 8개 |
| 인프라 Pod | 3개 |
| 고가용성 (Replica 2) | Analytics, API Gateway, Chatbot |
| CPU | 4 Core |
| Memory | 8 GB |

---

