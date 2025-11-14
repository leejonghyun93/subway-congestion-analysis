import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import DirectionsSubwayIcon from '@mui/icons-material/DirectionsSubway';
import LogoutIcon from '@mui/icons-material/Logout';
import LoginIcon from '@mui/icons-material/Login';

const Header = () => {
    const navigate = useNavigate();
    const { user, logout } = useAuth();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <AppBar position="static">
            <Toolbar>
                <DirectionsSubwayIcon sx={{ mr: 2 }} />
                <Typography variant="h6" component="div" sx={{ flexGrow: 1, cursor: 'pointer' }} onClick={() => navigate('/')}>
                    지하철 혼잡도 분석 시스템
                </Typography>

                <Button color="inherit" onClick={() => navigate('/')}>
                    대시보드
                </Button>
                <Button color="inherit" onClick={() => navigate('/search')}>
                    혼잡도 조회
                </Button>
                <Button color="inherit" onClick={() => navigate('/chatbot')}>
                    AI 챗봇
                </Button>

                {user ? (
                    <>
                        <Typography variant="body2" sx={{ mx: 2 }}>
                            {user.username}님
                        </Typography>
                        <Button color="inherit" startIcon={<LogoutIcon />} onClick={handleLogout}>
                            로그아웃
                        </Button>
                    </>
                ) : (
                    <Button color="inherit" startIcon={<LoginIcon />} onClick={() => navigate('/login')}>
                        로그인
                    </Button>
                )}
            </Toolbar>
        </AppBar>
    );
};

export default Header;