import React, { useState, useEffect, useContext } from 'react';
import { Container, Typography, Grid, Card, CardContent, CardActions, Button, Box, Pagination } from '@mui/material';
import axiosInstance from '../utils/axios';
import { useNavigate } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';
import { AuthContext } from '../contexts/AuthContext';

const Dashboard = () => {
    const { auth } = useContext(AuthContext);
    const [quizzes, setQuizzes] = useState([]);
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const navigate = useNavigate();

    useEffect(() => {
        fetchQuizzes(page);
    }, [page]);

    const fetchQuizzes = async (currentPage) => {
        try {
            const response = await axiosInstance.get('/quizzes', {
                params: {
                    page: currentPage - 1, // Backend pages are 0-indexed
                    size: 10, // Adjust as needed
                },
            });
            if (response.data.success) {
                setQuizzes(response.data.data.content);
                setTotalPages(response.data.data.totalPages);
            } else {
                toast.error(response.data.message || 'Failed to load quizzes.');
            }
        } catch (error) {
            console.error('Error fetching quizzes:', error);
            toast.error('Failed to load quizzes.');
        }
    };

    const handleStartQuiz = (quizId) => {
        navigate(`/quiz/${quizId}`);
    };

    const handlePageChange = (event, value) => {
        setPage(value);
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
                <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                    <Pagination count={totalPages} page={page} onChange={handlePageChange} color="primary" />
                </Box>
            </Box>
        </Container>
    );
};

export default Dashboard;
