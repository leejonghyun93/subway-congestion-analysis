import React from 'react';
import {
    List,
    ListItem,
    ListItemText,
    Box,
    Typography,
    Chip,
} from '@mui/material';

const TopCongestedStations = ({ data }) => {
    const getCongestionColor = (level) => {
        if (!level) return 'default';
        if (level >= 80) return 'error';
        if (level >= 60) return 'warning';
        if (level >= 40) return 'success';
        return 'info';
    };

    const formatCongestion = (level) => {
        if (level === null || level === undefined) return '0.0';
        return typeof level === 'number' ? level.toFixed(1) : '0.0';
    };

    const formatPassengerCount = (count) => {
        if (count === null || count === undefined) return 0;
        return typeof count === 'number' ? count : 0;
    };

    if (!data || data.length === 0) {
        return (
            <Typography variant="body2" color="text.secondary" align="center" sx={{ p: 4 }}>
                데이터가 없습니다.
            </Typography>
        );
    }

    return (
        <List>
            {data.map((station, index) => {
                // 안전한 값 추출
                const stationName = station?.stationName || '알 수 없음';
                const lineNumber = station?.lineNumber || '-';
                const congestionLevel = station?.congestionLevel || 0;
                const passengerCount = station?.passengerCount || 0;

                return (
                    <ListItem
                        key={index}
                        sx={{
                            mb: 1,
                            borderRadius: 1,
                            border: '1px solid',
                            borderColor: 'divider',
                        }}
                    >
                        <Box
                            sx={{
                                width: 40,
                                height: 40,
                                borderRadius: '50%',
                                display: 'flex',
                                justifyContent: 'center',
                                alignItems: 'center',
                                backgroundColor: 'primary.main',
                                color: 'white',
                                fontWeight: 'bold',
                                mr: 2,
                            }}
                        >
                            {index + 1}
                        </Box>
                        <ListItemText
                            primary={
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <Typography variant="body1" fontWeight="bold">
                                        {stationName}
                                    </Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        ({lineNumber}호선)
                                    </Typography>
                                </Box>
                            }
                            secondary={`승객 수: ${formatPassengerCount(passengerCount)}명`}
                        />
                        <Chip
                            label={`${formatCongestion(congestionLevel)}%`}
                            color={getCongestionColor(congestionLevel)}
                            size="small"
                        />
                    </ListItem>
                );
            })}
        </List>
    );
};

export default TopCongestedStations;