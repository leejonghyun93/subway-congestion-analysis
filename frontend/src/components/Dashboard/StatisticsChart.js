import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Box, CircularProgress } from '@mui/material';
import { analyticsService } from '../../services/analyticsService';

const StatisticsChart = () => {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadChartData();
    }, []);

    const loadChartData = async () => {
        try {
            const response = await analyticsService.getHourlyStatistics('2', '강남역');
            const chartData = response.data.data.map((item) => ({
                hour: `${item.hourSlot}시`,
                congestion: item.avgCongestion,
            }));
            setData(chartData);
        } catch (error) {
            console.error('Failed to load chart data:', error);
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

    return (
        <ResponsiveContainer width="100%" height={300}>
            <LineChart data={data}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="hour" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="congestion" stroke="#8884d8" name="혼잡도 (%)" />
            </LineChart>
        </ResponsiveContainer>
    );
};

export default StatisticsChart;