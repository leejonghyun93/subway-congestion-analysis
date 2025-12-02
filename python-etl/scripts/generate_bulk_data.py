import pandas as pd
from datetime import datetime, timedelta
import random

def generate_congestion_level(rate):
    """혼잡도 레벨 분류"""
    if rate < 30:
        return 'LOW'
    elif rate < 60:
        return 'MEDIUM'
    elif rate < 80:
        return 'HIGH'
    else:
        return 'VERY_HIGH'

def apply_time_pattern(base_rate, hour):
    """시간대별 패턴 적용"""
    # 출퇴근 시간 (7-9시, 18-20시) 혼잡
    if hour in [7, 8, 9, 18, 19, 20]:
        multiplier = random.uniform(1.3, 1.5)
    # 점심시간 (12-13시) 약간 혼잡
    elif hour in [12, 13]:
        multiplier = random.uniform(1.1, 1.3)
    # 심야 (0-5시) 한산
    elif hour in [0, 1, 2, 3, 4, 5]:
        multiplier = random.uniform(0.3, 0.5)
    # 일반 시간
    else:
        multiplier = random.uniform(0.9, 1.1)

    return min(100, base_rate * multiplier)

def parse_original_csv(df):
    """
    원본 CSV를 파싱해서 시간대별 혼잡도 추출
    """
    print("원본 CSV 파싱 중...")

    time_columns = [
        '5시30분', '6시00분', '6시30분', '7시00분', '7시30분', '8시00분',
        '8시30분', '9시00분', '9시30분', '10시00분', '10시30분', '11시00분',
        '11시30분', '12시00분', '12시30분', '13시00분', '13시30분', '14시00분',
        '14시30분', '15시00분', '15시30분', '16시00분', '16시30분', '17시00분',
        '17시30분', '18시00분', '18시30분', '19시00분', '19시30분', '20시00분',
        '20시30분', '21시00분', '21시30분', '22시00분', '22시30분', '23시00분',
        '23시30분', '00시00분', '00시30분'
    ]

    parsed_data = []

    for _, row in df.iterrows():
        station_name = row['출발역']
        line_number = row['호선']

        for time_col in time_columns:
            if time_col in row and pd.notna(row[time_col]):
                # 혼잡도 값 추출 (문자열에서 숫자만)
                congestion_str = str(row[time_col]).replace('%', '').strip()
                try:
                    congestion_rate = float(congestion_str)
                except:
                    congestion_rate = 50.0  # 기본값

                parsed_data.append({
                    'station_name': station_name,
                    'line_number': line_number,
                    'time_str': time_col,
                    'congestion_rate': congestion_rate
                })

    print(f"파싱 완료: {len(parsed_data)}건")
    return parsed_data

def generate_bulk_data(input_csv_path, output_csv_path):
    """
    CSV 1,662건을 30일치로 확장
    """

    print("=" * 60)
    print("대용량 데이터 생성 시작")
    print("=" * 60)

    # 1. 원본 CSV 로드 (인코딩 자동 감지)
    print(f"\n[1/4] 원본 CSV 로딩: {input_csv_path}")

    try:
        df = pd.read_csv(input_csv_path, encoding='utf-8')
        print("인코딩: UTF-8")
    except:
        try:
            df = pd.read_csv(input_csv_path, encoding='cp949')
            print("인코딩: CP949")
        except:
            try:
                df = pd.read_csv(input_csv_path, encoding='euc-kr')
                print("인코딩: EUC-KR")
            except:
                df = pd.read_csv(input_csv_path, encoding='latin1')
                print("인코딩: Latin1")

    print(f"원본 데이터: {len(df):,}건")
    print(f"컬럼: {list(df.columns)}")

    # 2. 원본 CSV 파싱
    print("\n[2/4] 원본 데이터 파싱 중...")
    parsed_data = parse_original_csv(df)

    # 3. 30일치 데이터 생성
    print("\n[3/4] 30일치 데이터 생성 중...")
    start_date = datetime.now() - timedelta(days=30)
    all_data = []

    total_records = len(parsed_data) * 30
    current_record = 0

    # 30일 반복
    for day in range(30):
        current_date = start_date + timedelta(days=day)

        for item in parsed_data:
            # 시간 문자열 파싱 (예: "7시30분" → hour=7, minute=30)
            time_str = item['time_str']
            hour = int(time_str.split('시')[0])
            minute = int(time_str.split('시')[1].replace('분', ''))

            timestamp = current_date.replace(
                hour=hour,
                minute=minute,
                second=0,
                microsecond=0
            )

            # 기본 혼잡도
            base_rate = item['congestion_rate']

            # 시간대별 패턴 적용
            adjusted_rate = apply_time_pattern(base_rate, hour)

            # ±10% 랜덤 변동
            random_factor = random.uniform(0.9, 1.1)
            final_rate = min(100, max(0, adjusted_rate * random_factor))

            # 새 레코드 생성
            new_record = {
               'station_name': item['station_name'],
               'line_number': item['line_number'],
               'congestion_level': round(final_rate, 1),  # float로 저장
               'passenger_count': int(final_rate * 30),   # 승객수 추가 (혼잡도 × 30)
               'timestamp': timestamp.strftime('%Y-%m-%d %H:%M:%S')
            }
            all_data.append(new_record)

            current_record += 1

            # 진행률 표시 (10,000건마다)
            if current_record % 10000 == 0:
                progress = (current_record / total_records) * 100
                print(f"  진행: {current_record:,} / {total_records:,} ({progress:.1f}%)")

        # 하루 완료
        print(f"  Day {day + 1}/30 완료 ({current_record:,}건)")

    # 4. DataFrame 생성 및 저장
    print(f"\n[4/4] CSV 저장 중: {output_csv_path}")
    result_df = pd.DataFrame(all_data)
    result_df.to_csv(output_csv_path, index=False, encoding='utf-8')

    # 5. 결과 출력
    import os
    file_size_mb = os.path.getsize(output_csv_path) / (1024 * 1024)

    print("\n" + "=" * 60)
    print("생성 완료!")
    print("=" * 60)
    print(f"총 레코드: {len(result_df):,}건")
    print(f"파일 크기: {file_size_mb:.2f} MB")
    print(f"기간: 30일")
    print(f"저장 위치: {output_csv_path}")

    # 6. 샘플 데이터 출력
    print("\n샘플 데이터 (처음 5건):")
    print(result_df.head())

    return result_df

if __name__ == '__main__':
    # 원본 CSV 경로
    input_path = 'D:/subway-congestion-system/data-collector-service/src/main/resources/data/congestion_data.csv'
    output_path = 'D:/subway-congestion-system/python-etl/scripts/bulk_data_7million.csv'

    print("CSV 확장 스크립트")
    print(f"입력: {input_path}")
    print(f"출력: {output_path}")
    print()

    df = generate_bulk_data(input_path, output_path)

    print("\n 완료!")