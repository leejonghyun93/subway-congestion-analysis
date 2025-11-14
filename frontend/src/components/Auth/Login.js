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
import { useAuth } from '../../contexts/AuthContext';
import LoginIcon from '@mui/icons-material/Login';

const Login = () => {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await login(username, password);
            navigate('/');
        } catch (error) {
            setError(error.response?.data?.message || '로그인에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="sm" sx={{ mt: 8 }}>
            <Paper sx={{ p: 4 }}>
                <Box textAlign="center" mb={3}>
                    <LoginIcon sx={{ fontSize: 48, color: 'primary.main' }} />
                    <Typography variant="h4" component="h1" gutterBottom>
                        로그인
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
                        margin="normal"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                    <TextField
                        fullWidth
                        label="비밀번호"
                        type="password"
                        margin="normal"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />

                    <Button
                        fullWidth
                        type="submit"
                        variant="contained"
                        size="large"
                        disabled={loading}
                        sx={{ mt: 3, mb: 2 }}
                    >
                        {loading ? '로그인 중...' : '로그인'}
                    </Button>

                    <Box textAlign="center">
                        <Link
                            component="button"
                            variant="body2"
                            type="button"  // 추가
                            onClick={(e) => {
                                e.preventDefault();  // 추가
                                navigate('/signup');
                            }}
                            sx={{ cursor: 'pointer', textDecoration: 'none' }}
                        >
                            계정이 없으신가요? 회원가입
                        </Link>
                    </Box>
                </form>
            </Paper>
        </Container>
    );
};

export default Login;