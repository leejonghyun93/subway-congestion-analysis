import React from 'react';
import { List, ListItem, ListItemText, Chip, Box } from '@mui/material';

const TopCongestedStations = ({ stations }) => {
    const getCongestionColor = (congestion) => {
        if (congestion >= 80) return 'error';
        if (congestion >= 60) return 'warning';
        return 'success';
    };

    return (
        <List sx={{ maxHeight: 320, overflow: 'auto' }}>
            {stations.map((station, index) => (
                <ListItem
                    key={index}
                    divider
                    secondaryAction={
                        <Chip
                            label={`${station.avgCongestion.toFixed(1)}%`}
                            color={getCongestionColor(station.avgCongestion)}
                            size="small"
                        />
                    }
                >
                    <ListItemText
                        primary={`${index + 1}. ${station.stationName}`}
                        secondary={`${station.lineNumber}호선`}
                    />
                </ListItem>
            ))}
        </List>
    );
};

export default TopCongestedStations;