import React, { useState, useEffect, useContext, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Typography, Button, Card, CardContent, RadioGroup, FormControlLabel, Radio, Box, CircularProgress } from '@mui/material';
import axiosInstance from '../utils/axios';
import { AuthContext } from '../contexts/AuthContext';
import { toast, ToastContainer } from 'react-toastify';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const Quiz = () => {
    const { quizId } = useParams();
    const { auth } = useContext(AuthContext);
    const [quiz, setQuiz] = useState(null);
    const [currentQuestion, setCurrentQuestion] = useState(null);
    const [selectedOption, setSelectedOption] = useState('');
    const [leaderboard, setLeaderboard] = useState([]);
    const [client, setClient] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [timer, setTimer] = useState(0);
    const [correctAnswers, setCorrectAnswers] = useState([]);
    const timerRef = useRef(null);

    useEffect(() => {
        fetchQuiz();
        setupWebSocket();
        // Cleanup on unmount
        return () => {
            if (client) {
                client.deactivate();
            }
            clearInterval(timerRef.current);
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [quizId]);

    const fetchQuiz = async () => {
        try {
            const response = await axiosInstance.get(`/quizzes/${quizId}`);
            setQuiz(response.data);
            setIsLoading(false);
        } catch (error) {
            console.error('Error fetching quiz:', error);
            toast.error('Failed to load quiz.');
            setIsLoading(false);
        }
    };

    const setupWebSocket = () => {
        const socket = new SockJS('http://localhost:8081/ws');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            connectHeaders: {
                username: auth.user.sub, // Send username as header
            },
            debug: function (str) {
                console.log(str);
            },
            reconnectDelay: 5000, // Attempt reconnection after 5 seconds
            onConnect: () => {
                console.log('Connected to WebSocket');
                stompClient.subscribe('/topic/quiz/question', (message) => {
                    const question = JSON.parse(message.body);
                    console.log("Received question:", message.body);
                    setCurrentQuestion(question);
                    setSelectedOption('');
                    setCorrectAnswers([]);
                    setTimer(question.timeLimit);
                    startTimer(question.timeLimit);
                });

                stompClient.subscribe('/topic/quiz/correctAnswer', (message) => {
                    const correctOptions = JSON.parse(message.body);
                    setCorrectAnswers(correctOptions);
                    // Stop the timer
                    setTimer(0);
                    clearInterval(timerRef.current);
                });

                stompClient.subscribe('/topic/quiz/leaderboard', (message) => {
                    const leaderboardData = JSON.parse(message.body);
                    setLeaderboard(leaderboardData);
                });

                stompClient.subscribe('/topic/quiz/end', (message) => {
                    toast.info(message.body);
                    // Optionally, navigate to leaderboard or disable further interactions
                });

                // Initialize quiz questions
                stompClient.publish({
                    destination: `/app/quiz/initialize`,
                    body: JSON.stringify({ quizId: parseInt(quizId) }),
                });
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
                toast.error('WebSocket connection error.');
            },
            onDisconnect: () => {
                console.log('Disconnected from WebSocket');
                toast.warn('Disconnected from quiz. Attempting to reconnect...');
            },
        });

        stompClient.activate();
        setClient(stompClient);
    };

    const startTimer = (duration) => {
        setTimer(duration);
        clearInterval(timerRef.current);
        timerRef.current = setInterval(() => {
            setTimer((prev) => {
                if (prev <= 1) {
                    clearInterval(timerRef.current);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
    };

    const handleSubmitAnswer = () => {
        if (!selectedOption) {
            toast.warning('Please select an option.');
            return;
        }

        const answerMessage = {
            username: auth.user.sub,
            questionId: currentQuestion.id,
            selectedOptionId: parseInt(selectedOption),
        };

        client.publish({
            destination: '/app/quiz/answer',
            body: JSON.stringify(answerMessage),
        });

        setHasAnswered(true);
        toast.success('Answer submitted!');
    };

    if (isLoading) {
        return (
            <Container maxWidth="sm" sx={{ mt: 5, textAlign: 'center' }}>
                <CircularProgress />
            </Container>
        );
    }

    return (
        <Container maxWidth="md">
            <ToastContainer />
            <Box sx={{ mt: 5 }}>
                <Typography variant="h4" gutterBottom>
                    {quiz.title}
                </Typography>
                <Typography variant="body1" gutterBottom>
                    {quiz.description}
                </Typography>

                {currentQuestion ? (
                    <Card sx={{ mt: 3 }}>
                        <CardContent>
                            <Box display="flex" justifyContent="space-between" alignItems="center">
                                <Typography variant="h6">{currentQuestion.content}</Typography>
                                <Typography variant="h6">Time: {timer}s</Typography>
                            </Box>
                            <RadioGroup
                                value={selectedOption}
                                onChange={(e) => setSelectedOption(e.target.value)}
                            >
                                {currentQuestion.options.map((option) => (
                                    <FormControlLabel
                                        key={option.id}
                                        value={option.id.toString()}
                                        control={<Radio />}
                                        label={option.text}
                                        disabled={timer === 0 || correctAnswers.length > 0 || hasAnswered}
                                        style={
                                            correctAnswers.find((opt) => opt.id === option.id)
                                                ? { backgroundColor: '#d4edda' } // Green for correct
                                                : hasAnswered && selectedOption === option.id.toString()
                                                    ? { backgroundColor: '#f8d7da' } // Red for incorrect
                                                    : {}
                                        }
                                    />
                                ))}
                            </RadioGroup>
                            {timer > 0 && correctAnswers.length === 0 && !hasAnswered && (
                                <Button variant="contained" color="primary" onClick={handleSubmitAnswer}>
                                    Submit Answer
                                </Button>
                            )}
                            {correctAnswers.length > 0 && (
                                <Typography variant="body1" color="green" sx={{ mt: 2 }}>
                                    Correct Answer(s):{' '}
                                    {correctAnswers.map((opt) => opt.text).join(', ')}
                                </Typography>
                            )}
                        </CardContent>
                    </Card>
                ) : (
                    <Typography variant="h6" sx={{ mt: 3 }}>
                        Waiting for the next question...
                    </Typography>
                )}

                <Box sx={{ mt: 5 }}>
                    <Typography variant="h5" gutterBottom>
                        Leaderboard
                    </Typography>
                    <Box>
                        {leaderboard.length > 0 ? (
                            leaderboard.map((score, index) => (
                                <Typography key={score.id} variant="body1">
                                    {index + 1}. {score.user.username} - {score.points} points
                                </Typography>
                            ))
                        ) : (
                            <Typography variant="body1">No scores yet.</Typography>
                        )}
                    </Box>
                </Box>
            </Box>
        </Container>
    );
};

export default Quiz;
