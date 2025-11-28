import React, { useState, useEffect } from 'react';
import {
    Container,
    Paper,
    Typography,
    Box,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Chip,
    TextField
} from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import axios from 'axios';

const NotificationHistory = () => {
    const [history, setHistory] = useState([]);
    const [stats, setStats] = useState({ successful: 0, failed: 0, total: 0 });
    const [filterEmail, setFilterEmail] = useState('');

    useEffect(() => {
        loadHistory();
        loadStats();
    }, []);

    const loadHistory = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/notification/history');
            if (response.data.success) {
                setHistory(response.data.data);
            }
        } catch (error) {
            console.error('Failed to load history:', error);
        }
    };

    const loadStats = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/notification/stats');
            if (response.data.success) {
                setStats(response.data.data);
            }
        } catch (error) {
            console.error('Failed to load stats:', error);
        }
    };

    const filteredHistory = filterEmail
        ? history.filter(h => h.recipient.toLowerCase().includes(filterEmail.toLowerCase()))
        : history;

    return (
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            {/* 헤더 */}
            <Paper sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <HistoryIcon sx={{ fontSize: 40 }} />
                    <Box>
                        <Typography variant="h4">알림 이력</Typography>
                        <Typography variant="body2">발송된 알림을 확인하세요</Typography>
                    </Box>
                </Box>
            </Paper>

            {/* 통계 */}
            <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
                <Paper sx={{ flex: 1, p: 2, textAlign: 'center', bgcolor: '#e3f2fd' }}>
                    <Typography variant="h4" color="primary">{stats.total}</Typography>
                    <Typography variant="body2">전체 발송</Typography>
                </Paper>
                <Paper sx={{ flex: 1, p: 2, textAlign: 'center', bgcolor: '#e8f5e9' }}>
                    <Typography variant="h4" color="success.main">{stats.successful}</Typography>
                    <Typography variant="body2">성공</Typography>
                </Paper>
                <Paper sx={{ flex: 1, p: 2, textAlign: 'center', bgcolor: '#ffebee' }}>
                    <Typography variant="h4" color="error.main">{stats.failed}</Typography>
                    <Typography variant="body2">실패</Typography>
                </Paper>
            </Box>

            {/* 필터 */}
            <Box sx={{ mb: 2 }}>
                <TextField
                    label="이메일로 검색"
                    size="small"
                    value={filterEmail}
                    onChange={(e) => setFilterEmail(e.target.value)}
                    placeholder="example@email.com"
                />
            </Box>

            {/* 이력 테이블 */}
            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>발송 시간</TableCell>
                            <TableCell>수신자</TableCell>
                            <TableCell>제목</TableCell>
                            <TableCell>타입</TableCell>
                            <TableCell>상태</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {filteredHistory.map((item) => (
                            <TableRow key={item.id}>
                                <TableCell>{new Date(item.sentAt).toLocaleString('ko-KR')}</TableCell>
                                <TableCell>{item.recipient}</TableCell>
                                <TableCell>{item.subject}</TableCell>
                                <TableCell>
                                    <Chip label={item.notificationType} size="small" />
                                </TableCell>
                                <TableCell>
                                    <Chip
                                        label={item.status}
                                        size="small"
                                        color={item.status === 'SUCCESS' ? 'success' : 'error'}
                                    />
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
    );
};

export default NotificationHistory;