import React, { useState, useEffect } from 'react';
import {
    Container,
    Paper,
    Typography,
    Box,
    TextField,
    Button,
    Switch,
    FormControlLabel,
    List,
    ListItem,
    ListItemText,
    IconButton,
    Chip,
    Alert,
    MenuItem,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Snackbar
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import NotificationsIcon from '@mui/icons-material/Notifications';
import axios from 'axios';

const NotificationSettings = () => {
    const [settings, setSettings] = useState([]);
    const [openDialog, setOpenDialog] = useState(false);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

    // 새 알림 설정 폼
    const [newSetting, setNewSetting] = useState({
        userId: 'user123', // 실제로는 로그인한 사용자 ID
        email: '',
        lineNumber: '2',
        stationName: '강남역',
        thresholdCongestion: 80,
        enabled: true
    });

    useEffect(() => {
        loadSettings();
    }, []);

    const loadSettings = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/notification/settings?userId=user123');
            if (response.data.success) {
                setSettings(response.data.data);
            }
        } catch (error) {
            console.error('Failed to load settings:', error);
        }
    };

    const handleCreateSetting = async () => {
        try {
            const response = await axios.post('http://localhost:8080/api/notification/settings', newSetting);

            if (response.data.success) {
                setSnackbar({ open: true, message: '알림 설정이 추가되었습니다!', severity: 'success' });
                setOpenDialog(false);
                loadSettings();
                // 폼 초기화
                setNewSetting({
                    userId: 'user123',
                    email: '',
                    lineNumber: '2',
                    stationName: '강남역',
                    thresholdCongestion: 80,
                    enabled: true
                });
            }
        } catch (error) {
            setSnackbar({ open: true, message: '알림 설정 추가 실패', severity: 'error' });
            console.error('Failed to create setting:', error);
        }
    };

    const handleDeleteSetting = async (id) => {
        if (!window.confirm('이 알림 설정을 삭제하시겠습니까?')) return;

        try {
            const response = await axios.delete(`http://localhost:8080/api/notification/settings/${id}`);
            if (response.data.success) {
                setSnackbar({ open: true, message: '알림 설정이 삭제되었습니다', severity: 'success' });
                loadSettings();
            }
        } catch (error) {
            setSnackbar({ open: true, message: '삭제 실패', severity: 'error' });
            console.error('Failed to delete setting:', error);
        }
    };

    const handleTestEmail = async () => {
        if (!newSetting.email) {
            setSnackbar({ open: true, message: '이메일을 입력해주세요', severity: 'warning' });
            return;
        }

        try {
            const testEmailRequest = {
                to: newSetting.email,
                subject: '[테스트] 지하철 혼잡도 알림',
                content: '이것은 테스트 이메일입니다. 알림이 정상적으로 작동합니다!',
                lineNumber: '2',
                stationName: '강남역',
                congestion: 85.5
            };

            const response = await axios.post('http://localhost:8080/api/notification/email', testEmailRequest);

            if (response.data.success) {
                setSnackbar({ open: true, message: '테스트 이메일 발송 완료!', severity: 'success' });
            }
        } catch (error) {
            setSnackbar({ open: true, message: '이메일 발송 실패', severity: 'error' });
            console.error('Failed to send test email:', error);
        }
    };

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            {/* 헤더 */}
            <Paper sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <NotificationsIcon sx={{ fontSize: 40 }} />
                    <Box>
                        <Typography variant="h4">알림 설정</Typography>
                        <Typography variant="body2">혼잡도 알림을 받을 역을 설정하세요</Typography>
                    </Box>
                </Box>
            </Paper>

            {/* 안내 */}
            <Alert severity="info" sx={{ mb: 3 }}>
                설정한 역의 혼잡도가 임계값을 초과하면 이메일로 알림을 받습니다.
            </Alert>

            {/* 알림 추가 버튼 */}
            <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => setOpenDialog(true)}
                sx={{ mb: 2 }}
            >
                새 알림 추가
            </Button>

            {/* 알림 목록 */}
            <Paper sx={{ p: 2 }}>
                <Typography variant="h6" sx={{ mb: 2 }}>내 알림 설정 ({settings.length})</Typography>

                {settings.length === 0 ? (
                    <Typography color="text.secondary" align="center" sx={{ py: 4 }}>
                        설정된 알림이 없습니다. 새 알림을 추가해보세요!
                    </Typography>
                ) : (
                    <List>
                        {settings.map((setting) => (
                            <ListItem
                                key={setting.id}
                                sx={{
                                    border: '1px solid',
                                    borderColor: 'divider',
                                    borderRadius: 1,
                                    mb: 1
                                }}
                                secondaryAction={
                                    <IconButton edge="end" onClick={() => handleDeleteSetting(setting.id)}>
                                        <DeleteIcon />
                                    </IconButton>
                                }
                            >
                                <ListItemText
                                    primary={
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                            <Typography variant="body1" fontWeight="bold">
                                                {setting.stationName}
                                            </Typography>
                                            <Chip label={`${setting.lineNumber}호선`} size="small" color="primary" />
                                            <Chip
                                                label={setting.enabled ? '활성' : '비활성'}
                                                size="small"
                                                color={setting.enabled ? 'success' : 'default'}
                                            />
                                        </Box>
                                    }
                                    secondary={
                                        <Box sx={{ mt: 1 }}>
                                            <Typography variant="body2" color="text.secondary">
                                                📧 {setting.email}
                                            </Typography>
                                            <Typography variant="body2" color="text.secondary">
                                                🔔 혼잡도 {setting.thresholdCongestion}% 초과 시 알림
                                            </Typography>
                                        </Box>
                                    }
                                />
                            </ListItem>
                        ))}
                    </List>
                )}
            </Paper>

            {/* 알림 추가 다이얼로그 */}
            <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
                <DialogTitle>새 알림 추가</DialogTitle>
                <DialogContent>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
                        <TextField
                            label="이메일"
                            type="email"
                            fullWidth
                            value={newSetting.email}
                            onChange={(e) => setNewSetting({ ...newSetting, email: e.target.value })}
                            placeholder="example@email.com"
                        />

                        <TextField
                            select
                            label="호선"
                            fullWidth
                            value={newSetting.lineNumber}
                            onChange={(e) => setNewSetting({ ...newSetting, lineNumber: e.target.value })}
                        >
                            {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                                <MenuItem key={num} value={String(num)}>
                                    {num}호선
                                </MenuItem>
                            ))}
                        </TextField>

                        <TextField
                            label="역 이름"
                            fullWidth
                            value={newSetting.stationName}
                            onChange={(e) => setNewSetting({ ...newSetting, stationName: e.target.value })}
                            placeholder="예: 강남역"
                        />

                        <TextField
                            label="혼잡도 임계값 (%)"
                            type="number"
                            fullWidth
                            value={newSetting.thresholdCongestion}
                            onChange={(e) => setNewSetting({ ...newSetting, thresholdCongestion: Number(e.target.value) })}
                            inputProps={{ min: 0, max: 100 }}
                        />

                        <FormControlLabel
                            control={
                                <Switch
                                    checked={newSetting.enabled}
                                    onChange={(e) => setNewSetting({ ...newSetting, enabled: e.target.checked })}
                                />
                            }
                            label="알림 활성화"
                        />

                        <Button variant="outlined" onClick={handleTestEmail}>
                            테스트 이메일 발송
                        </Button>
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDialog(false)}>취소</Button>
                    <Button onClick={handleCreateSetting} variant="contained">추가</Button>
                </DialogActions>
            </Dialog>

            {/* 스낵바 */}
            <Snackbar
                open={snackbar.open}
                autoHideDuration={3000}
                onClose={() => setSnackbar({ ...snackbar, open: false })}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert severity={snackbar.severity} sx={{ width: '100%' }}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Container>
    );
};

export default NotificationSettings;