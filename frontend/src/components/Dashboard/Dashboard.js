import React, { useState, useEffect } from 'react';
import { Container, Grid, Paper, Typography, Box, CircularProgress } from '@mui/material';
import { analyticsService } from '../../services/analyticsService';
import TopCongestedStations from './TopCongestedStations';
import RealtimeStatus from './RealtimeStatus';
import StatisticsChart from './StatisticsChart';

const Dashboard = () => {
    const [topStations, setTopStations] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        try {
            const response = await analyticsService.getTopCongestedStations(10);
            setTopStations(response.data.data);
        } catch (error) {
            console.error('Failed to load dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
            <Typography variant="h4" gutterBottom>
                실시간 대시보드
            </Typography>

            <Grid container spacing={3}>
                {/* 실시간 혼잡 현황 */}
                <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 2, height: 400 }}>
                        <Typography variant="h6" gutterBottom>
                            실시간 혼잡 현황
                        </Typography>
                        <RealtimeStatus />
                    </Paper>
                </Grid>

                {/* TOP 10 혼잡 역 */}
                <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 2, height: 400 }}>
                        <Typography variant="h6" gutterBottom>
                            TOP 10 혼잡 역
                        </Typography>
                        <TopCongestedStations stations={topStations} />
                    </Paper>
                </Grid>

                {/* 시간대별 혼잡도 차트 */}
                <Grid item xs={12}>
                    <Paper sx={{ p: 2 }}>
                        <Typography variant="h6" gutterBottom>
                            시간대별 평균 혼잡도
                        </Typography>
                        <StatisticsChart />
                    </Paper>
                </Grid>
            </Grid>
        </Container>
    );
};

export default Dashboard;