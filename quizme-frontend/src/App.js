import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import Navbar from './components/Navbar';
import PrivateRoute from './components/PrivateRoute';

import Register from './pages/Register';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Lobby from './pages/Lobby';
import Quiz from './pages/Quiz';
import Leaderboard from './pages/Leaderboard';

const App = () => {
  return (
      <AuthProvider>
        <Router>
          <Navbar />
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<Login />} />

            {/* Protected routes */}
            <Route
                path="/dashboard"
                element={
                  <PrivateRoute>
                    <Dashboard />
                  </PrivateRoute>
                }
            />
            <Route
                path="/lobby"
                element={
                  <PrivateRoute>
                    <Lobby />
                  </PrivateRoute>
                }
            />
            <Route
                path="/quiz/:quizId"
                element={
                  <PrivateRoute>
                    <Quiz />
                  </PrivateRoute>
                }
            />
            <Route
                path="/leaderboard/:quizId"
                element={
                  <PrivateRoute>
                    <Leaderboard />
                  </PrivateRoute>
                }
            />
          </Routes>
        </Router>
      </AuthProvider>
  );
};

export default App;
