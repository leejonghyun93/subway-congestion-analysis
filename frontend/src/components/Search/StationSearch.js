import React, { useState } from 'react';
import {
    Container,
    Paper,
    TextField,
    Button,
    Grid,
    Typography,
    Box,
    Card,
    CardContent,
    MenuItem,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { analyticsService } from '../../services/analyticsService';
import { predictionService } from '../../services/predictionService';

const StationSearch = () => {
    const [lineNumber, setLineNumber] = useState('2');
    const [stationName, setStationName] = useState('');
    const [realtimeData, setRealtimeData] = useState(null);
    const [predictionData, setPredictionData] = useState(null);
    const [loading, setLoading] = useState(false);

    const lines = ['1', '2', '3', '4', '5', '6', '7', '8', '9'];

    const handleSearch = async () => {
        if (!stationName) {
            alert('역 이름을 입력해주세요');
            return;
        }

        setLoading(true);
        try {
            // 실시간 데이터 조회
            const realtimeResponse = await analyticsService.getRealtimeCongestion(lineNumber, stationName);
            setRealtimeData(realtimeResponse.data.data);

            // 예측 데이터 조회
            const predictionResponse = await predictionService.predictNow(lineNumber, stationName);
            setPredictionData(predictionResponse.data.data);
        } catch (error) {
            console.error('Search failed:', error);
            alert('데이터를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="md" sx={{ mt: 4 }}>
            <Paper sx={{ p: 3 }}>
                <Typography variant="h5" gutterBottom>
                    혼잡도 조회
                </Typography>

                <Grid container spacing={2} sx={{ mt: 2 }}>
                    <Grid item xs={12} sm={3}>
                        <TextField
                            select
                            fullWidth
                            label="호선"
                            value={lineNumber}
                            onChange={(e) => setLineNumber(e.target.value)}
                        >
                            {lines.map((line) => (
                                <MenuItem key={line} value={line}>
                                    {line}호선
                                </MenuItem>
                            ))}
                        </TextField>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                        <TextField
                            fullWidth
                            label="역 이름"
                            placeholder="예: 강남역"
                            value={stationName}
                            onChange={(e) => setStationName(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                        />
                    </Grid>

                    <Grid item xs={12} sm={3}>
                        <Button
                            fullWidth
                            variant="contained"
                            startIcon={<SearchIcon />}
                            onClick={handleSearch}
                            disabled={loading}
                            sx={{ height: '56px' }}
                        >
                            검색
                        </Button>
                    </Grid>
                </Grid>

                {/* 검색 결과 */}
                {realtimeData && (
                    <Box sx={{ mt: 4 }}>
                        <Grid container spacing={2}>
                            <Grid item xs={12} md={6}>
                                <Card>
                                    <CardContent>
                                        <Typography variant="h6" gutterBottom>
                                            실시간 혼잡도
                                        </Typography>
                                        <Typography variant="h3" color="primary">
                                            {realtimeData.congestion ? `${realtimeData.congestion.toFixed(1)}%` : 'N/A'}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                                            {realtimeData.stationName} ({realtimeData.lineNumber}호선)
                                        </Typography>
                                    </CardContent>
                                </Card>
                            </Grid>

                            {predictionData && (
                                <Grid item xs={12} md={6}>
                                    <Card>
                                        <CardContent>
                                            <Typography variant="h6" gutterBottom>
                                                예측 혼잡도
                                            </Typography>
                                            <Typography variant="h3" color="secondary">
                                                {predictionData.predictedCongestion.toFixed(1)}%
                                            </Typography>
                                            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                                                신뢰도: {(predictionData.confidence * 100).toFixed(1)}%
                                            </Typography>
                                        </CardContent>
                                    </Card>
                                </Grid>
                            )}
                        </Grid>
                    </Box>
                )}
            </Paper>
        </Container>
    );
};

export default StationSearch;