import React, { useState, useEffect } from 'react';
import { Container, Stack, Paper, Typography, Box, CircularProgress } from '@mui/material';
import RealtimeStatus from './RealtimeStatus';
import StatisticsChart from './StatisticsChart';
import TopCongestedStations from './TopCongestedStations';
import { analyticsService } from '../../services/analyticsService';

const Dashboard = () => {
    const [loading, setLoading] = useState(true);
    const [dashboardData, setDashboardData] = useState({ topCongested: [] });

    useEffect(() => {
        loadDashboardData();
        const interval = setInterval(loadDashboardData, 30000);
        return () => clearInterval(interval);
    }, []);

    const loadDashboardData = async () => {
        try {
            const topResponse = await analyticsService.getTopCongestedStations(5);
            const topData = topResponse.data.data || [];
            setDashboardData({ topCongested: topData });
        } catch (error) {
            console.error('Failed to load dashboard data:', error);
            setDashboardData({ topCongested: [] });
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    const avgCong = dashboardData.topCongested.length > 0
        ? (dashboardData.topCongested.reduce((s, x) => s + x.congestionLevel, 0) / dashboardData.topCongested.length).toFixed(1)
        : 0;

    return (
        <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
            <Paper sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
                <Typography variant="h4">실시간 대시보드</Typography>
                <Typography variant="body1">지하철 혼잡도 모니터링 시스템</Typography>
            </Paper>

            <Box sx={{ display: 'flex', gap: 3 }}>
                <Box sx={{ flex: '1 1 66%' }}>
                    <Paper sx={{ p: 3, mb: 3 }}>
                        <Typography variant="h6" gutterBottom>실시간 혼잡도 현황</Typography>
                        <RealtimeStatus />
                    </Paper>

                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>시간대별 평균 혼잡도</Typography>
                        <StatisticsChart stationName="강남역" lineNumber="2" />
                    </Paper>
                </Box>

                <Stack spacing={1.5} sx={{ flex: '1 1 34%' }}>
                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6" sx={{ mb: 1, fontSize: '1.1rem' }}>TOP 5 혼잡역</Typography>
                        <Box sx={{ height: '180px', overflow: 'auto' }}>
                            <TopCongestedStations data={dashboardData.topCongested} />
                        </Box>
                    </Paper>

                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6" sx={{ mb: 1, fontSize: '1.1rem' }}>실시간 통계</Typography>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                            <Box sx={{ flex: 1, textAlign: 'center', p: 1, bgcolor: '#e3f2fd', borderRadius: 1 }}>
                                <Typography variant="caption" color="text.secondary">평균</Typography>
                                <Typography variant="h6" color="primary">{avgCong}%</Typography>
                            </Box>
                            <Box sx={{ flex: 1, textAlign: 'center', p: 1, bgcolor: '#f3e5f5', borderRadius: 1 }}>
                                <Typography variant="caption" color="text.secondary">역수</Typography>
                                <Typography variant="h6" color="secondary">{dashboardData.topCongested.length}</Typography>
                            </Box>
                            <Box sx={{ flex: 1, textAlign: 'center', p: 1, bgcolor: '#fff3e0', borderRadius: 1 }}>
                                <Typography variant="caption" color="text.secondary">최고</Typography>
                                <Typography variant="body2" color="warning.main" fontWeight="bold">
                                    {dashboardData.topCongested[0]?.stationName || '-'}
                                </Typography>
                            </Box>
                        </Box>
                    </Paper>

                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6" sx={{ mb: 1, fontSize: '1.1rem' }}>시스템 상태</Typography>
                        <Stack spacing={0.8}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', p: 0.8, bgcolor: '#f5f5f5', borderRadius: 1 }}>
                                <Typography variant="body2">데이터 수집</Typography>
                                <Typography variant="body2" color="success.main" fontWeight="bold">정상</Typography>
                            </Box>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', p: 0.8, bgcolor: '#f5f5f5', borderRadius: 1 }}>
                                <Typography variant="body2">분석 서비스</Typography>
                                <Typography variant="body2" color="success.main" fontWeight="bold">정상</Typography>
                            </Box>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', p: 0.8, bgcolor: '#f5f5f5', borderRadius: 1 }}>
                                <Typography variant="body2">업데이트</Typography>
                                <Typography variant="body2" color="primary" sx={{ fontSize: '0.8rem' }}>
                                    {new Date().toLocaleTimeString('ko-KR')}
                                </Typography>
                            </Box>
                        </Stack>
                    </Paper>
                </Stack>
            </Box>
        </Container>
    );
};

export default Dashboard;
