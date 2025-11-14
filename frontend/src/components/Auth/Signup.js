import React, { useState } from 'react';
import {
    Container,
    Paper,
    TextField,
    Button,
    Typography,
    Box,
    Alert,
    Link,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import PersonAddIcon from '@mui/icons-material/PersonAdd';

const Signup = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        nickname: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        // 비밀번호 확인
        if (formData.password !== formData.confirmPassword) {
            setError('비밀번호가 일치하지 않습니다.');
            return;
        }

        // 비밀번호 길이 체크
        if (formData.password.length < 8) {
            setError('비밀번호는 8자 이상이어야 합니다.');
            return;
        }

        setLoading(true);

        try {
            const response = await authService.signup(
                formData.username,
                formData.email,
                formData.password,
                formData.nickname || formData.username
            );

            if (response.data.success) {
                alert('회원가입이 완료되었습니다! 로그인해주세요.');
                navigate('/login');
            }
        } catch (error) {
            setError(error.response?.data?.message || '회원가입에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="sm" sx={{ mt: 8 }}>
            <Paper sx={{ p: 4 }}>
                <Box textAlign="center" mb={3}>
                    <PersonAddIcon sx={{ fontSize: 48, color: 'primary.main' }} />
                    <Typography variant="h4" component="h1" gutterBottom>
                        회원가입
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        지하철 혼잡도 분석 시스템
                    </Typography>
                </Box>

                {error && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                    </Alert>
                )}

                <form onSubmit={handleSubmit}>
                    <TextField
                        fullWidth
                        label="사용자명"
                        name="username"
                        margin="normal"
                        value={formData.username}
                        onChange={handleChange}
                        required
                        helperText="4~20자의 영문, 숫자"
                    />

                    <TextField
                        fullWidth
                        label="이메일"
                        name="email"
                        type="email"
                        margin="normal"
                        value={formData.email}
                        onChange={handleChange}
                        required
                    />

                    <TextField
                        fullWidth
                        label="비밀번호"
                        name="password"
                        type="password"
                        margin="normal"
                        value={formData.password}
                        onChange={handleChange}
                        required
                        helperText="8자 이상"
                    />

                    <TextField
                        fullWidth
                        label="비밀번호 확인"
                        name="confirmPassword"
                        type="password"
                        margin="normal"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        required
                    />

                    <TextField
                        fullWidth
                        label="닉네임 (선택)"
                        name="nickname"
                        margin="normal"
                        value={formData.nickname}
                        onChange={handleChange}
                        helperText="입력하지 않으면 사용자명이 닉네임이 됩니다"
                    />

                    <Button
                        fullWidth
                        type="submit"
                        variant="contained"
                        size="large"
                        disabled={loading}
                        sx={{ mt: 3, mb: 2 }}
                    >
                        {loading ? '가입 중...' : '회원가입'}
                    </Button>

                    <Box textAlign="center">
                        <Link
                            component="button"
                            variant="body2"
                            type="button"
                            onClick={(e) => {
                                e.preventDefault();
                                navigate('/login');
                            }}
                            sx={{ cursor: 'pointer', textDecoration: 'none' }}
                        >
                            이미 계정이 있으신가요? 로그인
                        </Link>
                    </Box>
                </form>
            </Paper>
        </Container>
    );
};

export default Signup;