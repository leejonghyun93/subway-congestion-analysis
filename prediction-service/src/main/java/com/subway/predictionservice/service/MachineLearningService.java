package com.subway.predictionservice.service;

import com.subway.predictionservice.dto.ModelMetrics;
import com.subway.predictionservice.entity.CongestionStatistics;
import com.subway.predictionservice.repository.CongestionStatisticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MachineLearningService {


    @Autowired
    @Lazy
    private SparkSession sparkSession;

    @Autowired
    private CongestionStatisticsRepository congestionStatisticsRepository;

    @Value("${spark.model.save-path}")
    private String modelSavePath;

    private LinearRegressionModel currentModel;
    private ModelMetrics currentMetrics;
    private String currentModelVersion;
    private boolean initialized = false;

    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    log.info("Lazy initializing Machine Learning Service...");
                    try {
                        loadOrTrainModel();
                        initialized = true;
                    } catch (Exception e) {
                        log.error("Failed to initialize ML model: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void loadOrTrainModel() {
        String latestModelPath = getLatestModelPath();

        if (latestModelPath != null && new File(latestModelPath).exists()) {
            log.info("Loading existing model from: {}", latestModelPath);
            try {
                currentModel = LinearRegressionModel.load(latestModelPath);
                currentModelVersion = new File(latestModelPath).getName();
                log.info("Model loaded successfully: {}", currentModelVersion);
            } catch (Exception e) {
                log.error("Failed to load model, training new one: {}", e.getMessage());
                trainNewModel();
            }
        } else {
            log.info("No existing model found, training new model...");
            trainNewModel();
        }
    }

    public ModelMetrics trainNewModel() {
        log.info("Starting model training...");

        try {
            List<CongestionStatistics> trainingData = congestionStatisticsRepository.findAllForTraining();

            if (trainingData.isEmpty()) {
                log.warn("No training data available");
                return null;
            }

            log.info("Loaded {} records for training", trainingData.size());

            Dataset<Row> dataFrame = createDataFrame(trainingData);

            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(new String[]{"hourSlot", "lineNumberEncoded", "dayOfWeek"})
                    .setOutputCol("features");

            Dataset<Row> assembledData = assembler.transform(dataFrame);

            Dataset<Row>[] splits = assembledData.randomSplit(new double[]{0.8, 0.2}, 42);
            Dataset<Row> trainingSet = splits[0];
            Dataset<Row> testSet = splits[1];

            log.info("Training set size: {}, Test set size: {}",
                    trainingSet.count(), testSet.count());

            LinearRegression lr = new LinearRegression()
                    .setLabelCol("avgCongestion")
                    .setFeaturesCol("features")
                    .setMaxIter(100)
                    .setRegParam(0.3)
                    .setElasticNetParam(0.8);

            currentModel = lr.fit(trainingSet);

            Dataset<Row> predictions = currentModel.transform(testSet);

            RegressionEvaluator evaluator = new RegressionEvaluator()
                    .setLabelCol("avgCongestion")
                    .setPredictionCol("prediction");

            double rmse = evaluator.setMetricName("rmse").evaluate(predictions);
            double mae = evaluator.setMetricName("mae").evaluate(predictions);
            double r2 = evaluator.setMetricName("r2").evaluate(predictions);

            log.info("Model Training Results - RMSE: {}, MAE: {}, R²: {}", rmse, mae, r2);

            currentModelVersion = "model_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String savePath = modelSavePath + "/" + currentModelVersion;

            try {
                File saveDir = new File(modelSavePath);
                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }
                currentModel.write().overwrite().save(savePath);
                log.info("Model saved to: {}", savePath);
            } catch (IOException e) {
                log.error("Failed to save model: {}", e.getMessage());
            }

            currentMetrics = ModelMetrics.builder()
                    .modelVersion(currentModelVersion)
                    .rmse(rmse)
                    .mae(mae)
                    .r2Score(r2)
                    .trainingDataSize(trainingData.size())
                    .trainedAt(LocalDateTime.now())
                    .lastUsedAt(LocalDateTime.now())
                    .build();

            return currentMetrics;

        } catch (Exception e) {
            log.error("Model training failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public Double predict(String lineNumber, String stationName, Integer hourSlot, Integer dayOfWeek) {
        ensureInitialized();

        if (currentModel == null) {
            log.warn("Model not available for prediction");
            return null;
        }

        try {
            List<PredictionData> dataList = new ArrayList<>();
            dataList.add(new PredictionData(
                    lineNumber,
                    stationName,
                    hourSlot,
                    encodeLineNumber(lineNumber),
                    dayOfWeek != null ? dayOfWeek : getCurrentDayOfWeek()
            ));

            Dataset<Row> inputData = sparkSession.createDataFrame(dataList, PredictionData.class);

            VectorAssembler assembler = new VectorAssembler()
                    .setInputCols(new String[]{"hourSlot", "lineNumberEncoded", "dayOfWeek"})
                    .setOutputCol("features");

            Dataset<Row> assembledData = assembler.transform(inputData);
            Dataset<Row> predictions = currentModel.transform(assembledData);

            Row result = predictions.first();
            double prediction = result.getAs("prediction");

            prediction = Math.max(0, Math.min(100, prediction));

            log.debug("Prediction: line={}, station={}, hour={} -> congestion={}",
                    lineNumber, stationName, hourSlot, prediction);

            if (currentMetrics != null) {
                currentMetrics.setLastUsedAt(LocalDateTime.now());
            }

            return prediction;

        } catch (Exception e) {
            log.error("Prediction failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public ModelMetrics getCurrentMetrics() {
        ensureInitialized();
        return currentMetrics;
    }

    public String getCurrentModelVersion() {
        ensureInitialized();
        return currentModelVersion;
    }

    private Dataset<Row> createDataFrame(List<CongestionStatistics> data) {
        List<TrainingData> trainingDataList = new ArrayList<>();

        for (CongestionStatistics stat : data) {
            trainingDataList.add(new TrainingData(
                    stat.getLineNumber(),
                    stat.getStationName(),
                    stat.getHourSlot(),
                    stat.getAvgCongestion(),
                    encodeLineNumber(stat.getLineNumber()),
                    getCurrentDayOfWeek()
            ));
        }

        return sparkSession.createDataFrame(trainingDataList, TrainingData.class);
    }

    private Integer encodeLineNumber(String lineNumber) {
        try {
            return Integer.parseInt(lineNumber.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer getCurrentDayOfWeek() {
        return LocalDateTime.now().getDayOfWeek().getValue() % 7;
    }

    private String getLatestModelPath() {
        File modelDir = new File(modelSavePath);
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            return null;
        }

        File[] modelFiles = modelDir.listFiles((dir, name) -> name.startsWith("model_"));
        if (modelFiles == null || modelFiles.length == 0) {
            return null;
        }

        File latestModel = modelFiles[0];
        for (File file : modelFiles) {
            if (file.lastModified() > latestModel.lastModified()) {
                latestModel = file;
            }
        }

        return latestModel.getAbsolutePath();
    }

    // Inner classes
    public static class TrainingData {
        private String lineNumber;
        private String stationName;
        private Integer hourSlot;
        private Double avgCongestion;
        private Integer lineNumberEncoded;
        private Integer dayOfWeek;

        public TrainingData() {}

        public TrainingData(String lineNumber, String stationName, Integer hourSlot,
                            Double avgCongestion, Integer lineNumberEncoded, Integer dayOfWeek) {
            this.lineNumber = lineNumber;
            this.stationName = stationName;
            this.hourSlot = hourSlot;
            this.avgCongestion = avgCongestion;
            this.lineNumberEncoded = lineNumberEncoded;
            this.dayOfWeek = dayOfWeek;
        }

        public String getLineNumber() { return lineNumber; }
        public void setLineNumber(String lineNumber) { this.lineNumber = lineNumber; }
        public String getStationName() { return stationName; }
        public void setStationName(String stationName) { this.stationName = stationName; }
        public Integer getHourSlot() { return hourSlot; }
        public void setHourSlot(Integer hourSlot) { this.hourSlot = hourSlot; }
        public Double getAvgCongestion() { return avgCongestion; }
        public void setAvgCongestion(Double avgCongestion) { this.avgCongestion = avgCongestion; }
        public Integer getLineNumberEncoded() { return lineNumberEncoded; }
        public void setLineNumberEncoded(Integer lineNumberEncoded) { this.lineNumberEncoded = lineNumberEncoded; }
        public Integer getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    }

    public static class PredictionData {
        private String lineNumber;
        private String stationName;
        private Integer hourSlot;
        private Integer lineNumberEncoded;
        private Integer dayOfWeek;

        public PredictionData() {}

        public PredictionData(String lineNumber, String stationName, Integer hourSlot,
                              Integer lineNumberEncoded, Integer dayOfWeek) {
            this.lineNumber = lineNumber;
            this.stationName = stationName;
            this.hourSlot = hourSlot;
            this.lineNumberEncoded = lineNumberEncoded;
            this.dayOfWeek = dayOfWeek;
        }

        public String getLineNumber() { return lineNumber; }
        public void setLineNumber(String lineNumber) { this.lineNumber = lineNumber; }
        public String getStationName() { return stationName; }
        public void setStationName(String stationName) { this.stationName = stationName; }
        public Integer getHourSlot() { return hourSlot; }
        public void setHourSlot(Integer hourSlot) { this.hourSlot = hourSlot; }
        public Integer getLineNumberEncoded() { return lineNumberEncoded; }
        public void setLineNumberEncoded(Integer lineNumberEncoded) { this.lineNumberEncoded = lineNumberEncoded; }
        public Integer getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    }
}