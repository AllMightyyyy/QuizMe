import React, { useState, useEffect } from 'react';
import { Container, Typography, Button, List, ListItem, ListItemText, Box } from '@mui/material';
import axiosInstance from '../utils/axios';
import { toast, ToastContainer } from 'react-toastify';

const Lobby = () => {
    const [users, setUsers] = useState([]);
    const [isInLobby, setIsInLobby] = useState(false);

    useEffect(() => {
        fetchLobbyUsers();
    }, []);

    const fetchLobbyUsers = async () => {
        try {
            const response = await axiosInstance.get('/lobby/users');
            setUsers(response.data);
            // Determine if the current user is in the lobby
            const token = localStorage.getItem('token');
            if (token) {
                const decoded = JSON.parse(atob(token.split('.')[1]));
                setIsInLobby(response.data.some((user) => user.username === decoded.sub));
            }
        } catch (error) {
            console.error('Error fetching lobby users:', error);
            toast.error('Failed to load lobby users.');
        }
    };

    const handleJoinLobby = async () => {
        try {
            await axiosInstance.post('/lobby/join');
            setIsInLobby(true);
            fetchLobbyUsers();
            toast.success('Joined the lobby.');
        } catch (error) {
            console.error('Error joining lobby:', error);
            toast.error('Failed to join lobby.');
        }
    };

    const handleLeaveLobby = async () => {
        try {
            await axiosInstance.post('/lobby/leave');
            setIsInLobby(false);
            fetchLobbyUsers();
            toast.success('Left the lobby.');
        } catch (error) {
            console.error('Error leaving lobby:', error);
            toast.error('Failed to leave lobby.');
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
