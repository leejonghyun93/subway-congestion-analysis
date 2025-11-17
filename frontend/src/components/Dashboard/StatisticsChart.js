import React, { useState, useEffect } from 'react';
import { Box, TextField, MenuItem, CircularProgress, Typography } from '@mui/material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { analyticsService } from '../../services/analyticsService';

const StatisticsChart = () => {
    const [loading, setLoading] = useState(false);
    const [line, setLine] = useState('2');
    const [station, setStation] = useState('강남역');
    const [data, setData] = useState([]);

    useEffect(() => {
        loadStatistics();
    }, [line, station]);

    const loadStatistics = async () => {
        setLoading(true);
        try {
            const response = await analyticsService.getHourlyStatistics(station, line);

            if (response.data.success && response.data.data) {
                const statsData = response.data.data;
                const hourlyData = Array.from({ length: 24 }, (_, hour) => {
                    const hourStats = statsData.find(s => s.hour === hour);
                    return {
                        hour: `${hour}시`,
                        congestion: hourStats?.avgCongestion || 0,
                    };
                });
                setData(hourlyData);
            } else {
                setData([]);
            }
        } catch (error) {
            console.error('Failed to load statistics:', error);
            setData([]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box>
            <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
                <TextField
                    select
                    label="호선"
                    value={line}
                    onChange={(e) => setLine(e.target.value)}
                    sx={{ minWidth: 120 }}
                    size="small"
                >
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                        <MenuItem key={num} value={String(num)}>
                            {num}호선
                        </MenuItem>
                    ))}
                </TextField>
                <TextField
                    label="역 이름"
                    value={station}
                    onChange={(e) => setStation(e.target.value)}
                    placeholder="예: 강남역"
                    size="small"
                />
            </Box>

            {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
                    <CircularProgress />
                </Box>
            ) : data.length > 0 ? (
                <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={data}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="hour" />
                        <YAxis label={{ value: '혼잡도 (%)', angle: -90, position: 'insideLeft' }} domain={[0, 100]} />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="congestion" stroke="#8884d8" activeDot={{ r: 8 }} name="평균 혼잡도" />
                    </LineChart>
                </ResponsiveContainer>
            ) : (
                <Typography variant="body1" color="text.secondary" align="center" sx={{ p: 4 }}>
                    데이터가 없습니다.
                </Typography>
            )}
        </Box>
    );
};

export default StatisticsChart;