import React, { useState, useRef, useEffect } from 'react';
import {
    Container,
    Paper,
    Box,
    TextField,
    Button,
    Typography,
    List,
    ListItem,
    Avatar,
    CircularProgress,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import PersonIcon from '@mui/icons-material/Person';
import { chatbotService } from '../../services/chatbotService';

const ChatInterface = () => {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [sessionId] = useState(() => `session_${Date.now()}`);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSend = async () => {
        if (!input.trim()) return;

        const userMessage = { role: 'user', content: input };
        setMessages((prev) => [...prev, userMessage]);
        setInput('');
        setLoading(true);

        try {
            const response = await chatbotService.sendMessage(input, sessionId);
            const botMessage = {
                role: 'assistant',
                content: response.data.data.response,
            };
            setMessages((prev) => [...prev, botMessage]);
        } catch (error) {
            console.error('Chat failed:', error);
            const errorMessage = {
                role: 'assistant',
                content: '죄송합니다. 응답을 생성하는데 실패했습니다.',
            };
            setMessages((prev) => [...prev, errorMessage]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            <Paper sx={{ height: '70vh', display: 'flex', flexDirection: 'column' }}>
                {/* Header */}
                <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
                    <Typography variant="h6">AI 챗봇</Typography>
                    <Typography variant="body2" color="text.secondary">
                        지하철 혼잡도에 대해 물어보세요
                    </Typography>
                </Box>

                {/* Messages */}
                <List sx={{ flex: 1, overflow: 'auto', p: 2 }}>
                    {messages.map((message, index) => (
                        <ListItem
                            key={index}
                            sx={{
                                flexDirection: message.role === 'user' ? 'row-reverse' : 'row',
                                alignItems: 'flex-start',
                                mb: 2,
                            }}
                        >
                            <Avatar sx={{ mx: 1, bgcolor: message.role === 'user' ? 'primary.main' : 'secondary.main' }}>
                                {message.role === 'user' ? <PersonIcon /> : <SmartToyIcon />}
                            </Avatar>
                            <Paper
                                sx={{
                                    p: 2,
                                    maxWidth: '70%',
                                    bgcolor: message.role === 'user' ? 'primary.light' : 'grey.100',
                                }}
                            >
                                <Typography>{message.content}</Typography>
                            </Paper>
                        </ListItem>
                    ))}
                    {loading && (
                        <ListItem>
                            <Avatar sx={{ mx: 1, bgcolor: 'secondary.main' }}>
                                <SmartToyIcon />
                            </Avatar>
                            <CircularProgress size={24} />
                        </ListItem>
                    )}
                    <div ref={messagesEndRef} />
                </List>

                {/* Input */}
                <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider', display: 'flex', gap: 1 }}>
                    <TextField
                        fullWidth
                        placeholder="메시지를 입력하세요..."
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && !e.shiftKey && handleSend()}
                        disabled={loading}
                    />
                    <Button
                        variant="contained"
                        endIcon={<SendIcon />}
                        onClick={handleSend}
                        disabled={loading || !input.trim()}
                    >
                        전송
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
};

export default ChatInterface;