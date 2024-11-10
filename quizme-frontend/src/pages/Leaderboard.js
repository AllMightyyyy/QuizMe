import React, { useState, useEffect } from 'react';
import { Container, Typography, List, ListItem, ListItemText, Box } from '@mui/material';
import axiosInstance from '../utils/axios';
import { useParams } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';

const Leaderboard = () => {
    const { quizId } = useParams();
    const [leaderboard, setLeaderboard] = useState([]);

    useEffect(() => {
        fetchLeaderboard();
    }, [quizId]);

    const fetchLeaderboard = async () => {
        try {
            const response = await axiosInstance.get(`/scores/leaderboard/${quizId}`);
            setLeaderboard(response.data);
        } catch (error) {
            console.error('Error fetching leaderboard:', error);
            toast.error('Failed to load leaderboard.');
        }
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
