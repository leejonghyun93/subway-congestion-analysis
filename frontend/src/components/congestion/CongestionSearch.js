import React, { useState } from 'react';
import {
    Container, Paper, Grid, TextField, Button, MenuItem,
    Box, Typography, Card, CardContent, CircularProgress,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { analyticsService } from '../../services/analyticsService';

const CongestionSearch = () => {
    const [line, setLine] = useState('2');
    const [station, setStation] = useState('');
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleSearch = async () => {
        if (!station.trim()) {
            setError('역 이름을 입력해주세요.');
            return;
        }

        setLoading(true);
        setError(null);
        setResult(null);

        try {
            let stationName = station.trim();
            stationName = stationName.replace(/역$/, '') + '역';

            const response = await analyticsService.getRealtimeCongestion(stationName, line);

            if (response.data.success && response.data.data) {
                setResult(response.data.data);
            } else {
                setError('해당 역의 혼잡도 정보를 찾을 수 없습니다.');
            }
        } catch (err) {
            console.error(err);
            setError('혼잡도 조회 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') handleSearch();
    };

    const safeValue = (value, defaultValue = 0) => (value == null ? defaultValue : value);

    const getCongestionStatus = (avg) => {
        if (avg >= 80) return '매우혼잡';
        if (avg >= 60) return '혼잡';
        if (avg >= 40) return '보통';
        return '여유';
    };

    const getCongestionColor = (avg) => {
        if (avg >= 80) return '#f44336';
        if (avg >= 60) return '#ff9800';
        if (avg >= 40) return '#4caf50';
        return '#2196f3';
    };

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            <Paper sx={{ p: 4 }}>
                <Typography variant="h5" align="center" sx={{ mb: 3 }}>혼잡도 조회</Typography>

                <Grid container spacing={2} sx={{ mb: 4 }}>
                    <Grid item xs={12} sm={3}>
                        <TextField
                            select
                            fullWidth
                            label="호선"
                            value={line}
                            onChange={(e) => setLine(e.target.value)}
                        >
                            {[1,2,3,4,5,6,7,8,9].map(num => (
                                <MenuItem key={num} value={String(num)}>{num}호선</MenuItem>
                            ))}
                        </TextField>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                        <TextField
                            fullWidth
                            label="역 이름"
                            placeholder="예: 강남, 홍대입구"
                            value={station}
                            onChange={(e) => setStation(e.target.value)}
                            onKeyPress={handleKeyPress}
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

                {loading && <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}><CircularProgress /></Box>}

                {error && <Box sx={{ my: 4 }}><Typography color="error" align="center">{error}</Typography></Box>}

                {result && !loading && (
                    <Card sx={{ mt: 3 }}>
                        <CardContent>
                            <Box sx={{ textAlign: 'center', mb: 3 }}>
                                <Typography variant="h4">{result.stationName || '알 수 없음'}</Typography>
                                <Typography variant="subtitle1" color="text.secondary">{result.lineNumber || '-'}호선</Typography>
                            </Box>

                            <Box sx={{
                                display: 'flex', justifyContent: 'center', alignItems: 'center', my: 4
                            }}>
                                <Box sx={{
                                    width: 200, height: 200, borderRadius: '50%',
                                    display: 'flex', flexDirection: 'column',
                                    justifyContent: 'center', alignItems: 'center',
                                    backgroundColor: getCongestionColor(safeValue(result.avgCongestion)),
                                    color: 'white',
                                }}>
                                    <Typography variant="h2" fontWeight="bold">
                                        {safeValue(result.avgCongestion).toFixed(1)}%
                                    </Typography>
                                    <Typography variant="h6">
                                        {result.avgCongestion == null ? "N/A" : getCongestionStatus(safeValue(result.avgCongestion))}
                                    </Typography>

                                </Box>
                            </Box>

                            <Grid container spacing={2}>
                                <Grid item xs={6}>
                                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                        <Typography variant="body2" color="text.secondary">승객 수</Typography>
                                        <Typography variant="h6">약 {safeValue(result.passengerCount)}명</Typography>
                                    </Paper>
                                </Grid>
                                <Grid item xs={6}>
                                    <Paper sx={{ p: 2, textAlign: 'center', bgcolor: '#f5f5f5' }}>
                                        <Typography variant="body2" color="text.secondary">상태</Typography>
                                        <Typography variant="h6">{getCongestionStatus(safeValue(result.avgCongestion))}</Typography>
                                    </Paper>
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                )}
            </Paper>
        </Container>
    );
};

export default CongestionSearch;
