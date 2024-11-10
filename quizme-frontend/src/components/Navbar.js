import React, { useContext } from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../contexts/AuthContext';
import axios from 'axios';

const Navbar = () => {
    const { auth, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const loadData = async () => {
        try {
            await axios.post(
                'http://localhost:8081/api/admin/load-sample-data',
                {},
                {
                    headers: {
                        Authorization: `Bearer ${auth.token}`
                    }
                }
            );
            alert("Data loaded successfully!");
        } catch (error) {
            console.error("Error loading data:", error);
            alert("Failed to load data. Please try again.");
        }
    };

    const startQuiz = async () => {
        try {
            // Replace `1` with the appropriate quizId if dynamic
            await axios.post(
                'http://localhost:8081/api/admin/start-quiz?quizId=1',
                {},
                {
                    headers: {
                        Authorization: `Bearer ${auth.token}`
                    }
                }
            );
            alert("Quiz started successfully!");
        } catch (error) {
            console.error("Error starting quiz:", error);
            alert("Failed to start quiz. Please try again.");
        }
    };

    const isAdmin = auth.user && auth.user.roles && auth.user.roles.includes("ROLE_ADMIN");

    return (
        <AppBar position="static">
            <Toolbar>
                <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                    QuizMe
                </Typography>
                <Box>
                    {auth.token ? (
                        <>
                            <Button color="inherit" component={Link} to="/dashboard">
                                Dashboard
                            </Button>
                            {isAdmin && (
                                <>
                                    <Button color="inherit" onClick={loadData}>
                                        Load Data
                                    </Button>
                                    <Button color="inherit" onClick={startQuiz}>
                                        Start Quiz
                                    </Button>
                                </>
                            )}
                            <Button color="inherit" onClick={handleLogout}>
                                Logout
                            </Button>
                        </>
                    ) : (
                        <>
                            <Button color="inherit" component={Link} to="/login">
                                Login
                            </Button>
                            <Button color="inherit" component={Link} to="/register">
                                Register
                            </Button>
                        </>
                    )}
                </Box>
            </Toolbar>
        </AppBar>
    );
};

export default Navbar;
