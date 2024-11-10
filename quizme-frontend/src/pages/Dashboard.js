import React, { useState, useEffect } from 'react';
import { Container, Typography, Grid, Card, CardContent, CardActions, Button, Box } from '@mui/material';
import axiosInstance from '../utils/axios';
import { useNavigate } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';

const Dashboard = () => {
    const [quizzes, setQuizzes] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        fetchQuizzes();
    }, []);

    const fetchQuizzes = async () => {
        try {
            const response = await axiosInstance.get('/quizzes');
            setQuizzes(response.data);
        } catch (error) {
            console.error('Error fetching quizzes:', error);
            toast.error('Failed to load quizzes.');
        }
    };

    const handleStartQuiz = (quizId) => {
        navigate(`/quiz/${quizId}`);
    };

    return (
        <Container maxWidth="lg">
            <ToastContainer />
            <Box sx={{ mt: 5 }}>
                <Typography variant="h4" gutterBottom>
                    Available Quizzes
                </Typography>
                <Grid container spacing={3}>
                    {quizzes.map((quiz) => (
                        <Grid item xs={12} sm={6} md={4} key={quiz.id}>
                            <Card>
                                <CardContent>
                                    <Typography variant="h5">{quiz.title}</Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        {quiz.description}
                                    </Typography>
                                </CardContent>
                                <CardActions>
                                    <Button size="small" onClick={() => handleStartQuiz(quiz.id)}>
                                        Start Quiz
                                    </Button>
                                </CardActions>
                            </Card>
                        </Grid>
                    ))}
                </Grid>
            </Box>
        </Container>
    );
};

export default Dashboard;
