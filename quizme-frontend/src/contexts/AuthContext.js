import React, { createContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode'; // Corrected import
import axiosInstance from '../utils/axios';

// Create the context
export const AuthContext = createContext();

// Create the provider component
export const AuthProvider = ({ children }) => {
    const [auth, setAuth] = useState({
        token: null,
        user: null,
    });

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setAuth({
                    token,
                    user: decoded,
                });
            } catch (error) {
                console.error('Invalid token:', error);
                localStorage.removeItem('token');
            }
        }
    }, []);

    const login = async (username, password) => {
        try {
            const response = await axiosInstance.post('/auth/login', { username, password });
            if (response.data.success) {
                const token = response.data.data.token; // Adjust based on ApiResponse
                const decoded = jwtDecode(token);
                localStorage.setItem('token', token);
                setAuth({
                    token,
                    user: decoded,
                });
                return { success: true };
            } else {
                return { success: false, message: response.data.message };
            }
        } catch (error) {
            console.error('Login error:', error);
            return { success: false, message: error.response?.data?.message || 'Login failed' };
        }
    };

    const register = async (username, password, profilePhoto) => {
        try {
            const formData = new FormData();
            formData.append('username', username);
            formData.append('password', password);
            if (profilePhoto) {
                formData.append('profilePhoto', profilePhoto);
            }

            const response = await axiosInstance.post('/auth/register', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });

            if (response.data.success) {
                return { success: true, message: response.data.message };
            } else {
                return { success: false, message: response.data.message };
            }
        } catch (error) {
            console.error('Registration error:', error);
            let message = 'Registration failed';
            if (error.response && error.response.data) {
                if (typeof error.response.data === 'string') {
                    message = error.response.data;
                } else if (error.response.data.message) {
                    message = error.response.data.message;
                }
            }
            return { success: false, message };
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        setAuth({
            token: null,
            user: null,
        });
    };

    return (
        <AuthContext.Provider value={{ auth, login, logout, register }}>
            {children}
        </AuthContext.Provider>
    );
};