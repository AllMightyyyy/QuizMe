import React, { useState, useEffect, useContext } from 'react';
import { Container, Typography, List, ListItem, ListItemText, Box, Avatar, Grid } from '@mui/material';
import axiosInstance from '../utils/axios';
import { useParams } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';
import { AuthContext } from '../contexts/AuthContext';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const Leaderboard = () => {
    const { quizId } = useParams();
    const { auth } = useContext(AuthContext);
    const [leaderboard, setLeaderboard] = useState([]);
    const [client, setClient] = useState(null);

    useEffect(() => {
        fetchLeaderboard();
        setupWebSocket();
        // Cleanup on unmount
        return () => {
            if (client) {
                client.deactivate();
            }
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [quizId]);

    const fetchLeaderboard = async () => {
        try {
            const response = await axiosInstance.get(`/scores/leaderboard/${quizId}`);
            if (response.data.success) {
                setLeaderboard(response.data.data);
            } else {
                toast.error(response.data.message || 'Failed to load leaderboard.');
            }
        } catch (error) {
            console.error('Error fetching leaderboard:', error);
            toast.error('Failed to load leaderboard.');
        }
    };

    const setupWebSocket = () => {
        const socket = new SockJS('http://localhost:8081/ws');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            connectHeaders: {
                username: auth.user.sub,
            },
            debug: function (str) {
                console.log(str);
            },
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to WebSocket for Leaderboard');
                stompClient.subscribe('/topic/quiz/leaderboard', (message) => {
                    const leaderboardData = JSON.parse(message.body);
                    setLeaderboard(leaderboardData);
                });

                stompClient.subscribe('/topic/quiz/end', (message) => {
                    toast.info('Quiz has ended. Final leaderboard is displayed.');
                });
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
                toast.error('WebSocket connection error.');
            },
        });

        stompClient.activate();
        setClient(stompClient);
    };

    return (
        <Container maxWidth="sm">
            <ToastContainer />
            <Box sx={{ mt: 5 }}>
                <Typography variant="h4" gutterBottom>
                    Leaderboard
                </Typography>
                <List>
                    {leaderboard.map((score, index) => (
                        <ListItem key={score.id}>
                            <Avatar src={score.user.profilePhoto || '/default-avatar.png'} alt={score.user.username} sx={{ mr: 2 }} />
                            <ListItemText
                                primary={`${index + 1}. ${score.user.username}`}
                                secondary={`Points: ${score.points}`}
                            />
                        </ListItem>
                    ))}
                </List>
            </Box>
        </Container>
    );
};

export default Leaderboard;
