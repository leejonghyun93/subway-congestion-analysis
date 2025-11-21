import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from './contexts/AuthContext';

// Components
import Header from './components/Layout/Header';
import Dashboard from './components/Dashboard/Dashboard';
import StationSearch from './components/Search/StationSearch';
import ChatInterface from './components/Chatbot/ChatInterface';
import Login from './components/Auth/Login';
import Signup from './components/Auth/Signup';
import NotificationSettings from './components/Notification/NotificationSettings';
import NotificationHistory from './components/Notification/NotificationHistory';

const theme = createTheme({
    palette: {
        primary: {
            main: '#1976d2',
        },
        secondary: {
            main: '#dc004e',
        },
    },
});

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
            retry: 1,
        },
    },
});

// Protected Route
const ProtectedRoute = ({ children }) => {
    const { user, loading } = useAuth();

    if (loading) {
        return <div>Loading...</div>;
    }

    return user ? children : <Navigate to="/login" />;
};

function AppContent() {
    return (
        <Router>
            <CssBaseline />
            <Header />
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />  {/* 추가 */}
                <Route
                    path="/"
                    element={
                        <ProtectedRoute>
                            <Dashboard />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/search"
                    element={
                        <ProtectedRoute>
                            <StationSearch />
                        </ProtectedRoute>
                    }
                />
                <Route
                    path="/chatbot"
                    element={
                        <ProtectedRoute>
                            <ChatInterface />
                        </ProtectedRoute>
                    }
                />
                <Route path="/notifications" element={<NotificationSettings />} />
                <Route path="/notifications/history" element={<NotificationHistory />} />
            </Routes>
        </Router>
    );
}

function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <ThemeProvider theme={theme}>
                <AuthProvider>
                    <AppContent />
                </AuthProvider>
            </ThemeProvider>
        </QueryClientProvider>
    );
}

export default App;