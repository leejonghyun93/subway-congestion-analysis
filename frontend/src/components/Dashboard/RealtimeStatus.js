import React, { useState, useEffect } from 'react';
import { Box, Typography, CircularProgress } from '@mui/material';
import { analyticsService } from '../../services/analyticsService';

const RealtimeStatus = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadRealtimeData();
        const interval = setInterval(loadRealtimeData, 30000); // 30초마다 갱신

        return () => clearInterval(interval);
    }, []);

    const loadRealtimeData = async () => {
        try {
            // 예시: 2호선 강남역 조회
            const response = await analyticsService.getRealtimeCongestion('2', '강남역');
            setData(response.data.data);
        } catch (error) {
            console.error('Failed to load realtime data:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" height={300}>
                <CircularProgress />
            </Box>
        );
    }

    if (!data) {
        return <Typography>데이터를 불러올 수 없습니다.</Typography>;
    }

    return (
        <Box textAlign="center" p={3}>
            <Typography variant="h3" color="primary" gutterBottom>
                {data.congestion ? `${data.congestion.toFixed(1)}%` : 'N/A'}
            </Typography>
            <Typography variant="h6" gutterBottom>
                {data.stationName} ({data.lineNumber}호선)
            </Typography>
            <Typography variant="body2" color="text.secondary">
                마지막 업데이트: {new Date(data.timestamp).toLocaleString()}
            </Typography>
        </Box>
    );
};

export default RealtimeStatus;