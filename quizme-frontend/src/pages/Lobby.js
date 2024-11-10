import React, { useState, useEffect, useContext } from 'react';
import { Container, Typography, Button, List, ListItem, ListItemText, Box } from '@mui/material';
import axiosInstance from '../utils/axios';
import { AuthContext } from '../contexts/AuthContext';
import { toast, ToastContainer } from 'react-toastify';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const Lobby = () => {
    const { auth } = useContext(AuthContext);
    const [users, setUsers] = useState([]);
    const [isInLobby, setIsInLobby] = useState(false);
    const [client, setClient] = useState(null);

    useEffect(() => {
        fetchLobbyUsers();
        setupWebSocket();
        // Cleanup on unmount
        return () => {
            if (client) {
                client.deactivate();
            }
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const fetchLobbyUsers = async () => {
        try {
            const response = await axiosInstance.get('/lobby/users');
            if (response.data.success) {
                setUsers(response.data.data);
                const currentUser = response.data.data.find(user => user.username === auth.user.sub);
                setIsInLobby(!!currentUser);
            } else {
                toast.error(response.data.message || 'Failed to load lobby users.');
            }
        } catch (error) {
            console.error('Error fetching lobby users:', error);
            toast.error('Failed to load lobby users.');
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
                console.log('Connected to WebSocket for Lobby');
                stompClient.subscribe('/topic/lobby/users', (message) => {
                    const updatedUsers = JSON.parse(message.body);
                    setUsers(updatedUsers);
                    const currentUser = updatedUsers.find(user => user.username === auth.user.sub);
                    setIsInLobby(!!currentUser);
                });

                stompClient.subscribe('/topic/quiz/start', (message) => {
                    toast.info('A new quiz has started!');
                    // Optionally navigate to quiz page
                });

                stompClient.subscribe('/topic/quiz/reset', (message) => {
                    toast.info('The quiz has been reset by admin.');
                    setIsInLobby(false);
                    setUsers([]);
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

    const handleJoinLobby = async () => {
        try {
            await axiosInstance.post('/lobby/join');
            setIsInLobby(true);
            toast.success('Joined the lobby.');
        } catch (error) {
            console.error('Error joining lobby:', error);
            toast.error(error.response?.data?.message || 'Failed to join lobby.');
        }
    };

    const handleLeaveLobby = async () => {
        try {
            await axiosInstance.post('/lobby/leave');
            setIsInLobby(false);
            toast.success('Left the lobby.');
        } catch (error) {
            console.error('Error leaving lobby:', error);
            toast.error(error.response?.data?.message || 'Failed to leave lobby.');
        }
    };

    return (
        <Container maxWidth="sm">
            <ToastContainer />
            <Box sx={{ mt: 5, textAlign: 'center' }}>
                <Typography variant="h4" gutterBottom>
                    Quiz Lobby
                </Typography>
                {isInLobby ? (
                    <Button variant="contained" color="secondary" onClick={handleLeaveLobby}>
                        Leave Lobby
                    </Button>
                ) : (
                    <Button variant="contained" color="primary" onClick={handleJoinLobby}>
                        Join Lobby
                    </Button>
                )}
                <Box sx={{ mt: 4 }}>
                    <Typography variant="h6">Users in Lobby:</Typography>
                    <List>
                        {users.map((user) => (
                            <ListItem key={user.id}>
                                <ListItemText primary={user.username} />
                            </ListItem>
                        ))}
                    </List>
                </Box>
            </Box>
        </Container>
    );
};

export default Lobby;
