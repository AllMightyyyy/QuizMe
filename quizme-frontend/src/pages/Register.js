import React, { useState, useContext } from 'react';
import { Container, TextField, Button, Typography, Box } from '@mui/material';
import { AuthContext } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const Register = () => {
    const { register } = useContext(AuthContext);
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        username: '',
        password: '',
        profilePhoto: null,
    });

    const handleChange = (e) => {
        const { name, value, files } = e.target;
        if (name === 'profilePhoto') {
            setFormData((prev) => ({ ...prev, profilePhoto: files[0] }));
        } else {
            setFormData((prev) => ({ ...prev, [name]: value }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const { username, password, profilePhoto } = formData;
        const response = await register(username, password, profilePhoto);
        if (response.success) {
            toast.success(response.message || 'Registration successful! Please login.');
            navigate('/login');
        } else {
            toast.error(`Registration failed: ${response.message}`);
        }
    };

    return (
        <Container maxWidth="sm">
            <ToastContainer />
            <Box sx={{ mt: 5 }}>
                <Typography variant="h4" gutterBottom>
                    Register
                </Typography>
                <form onSubmit={handleSubmit} encType="multipart/form-data">
                    <TextField
                        label="Username"
                        name="username"
                        value={formData.username}
                        onChange={handleChange}
                        fullWidth
                        required
                        margin="normal"
                    />
                    <TextField
                        label="Password"
                        name="password"
                        type="password"
                        value={formData.password}
                        onChange={handleChange}
                        fullWidth
                        required
                        margin="normal"
                    />
                    <Button variant="contained" component="label" sx={{ mt: 2 }}>
                        Upload Profile Photo
                        <input type="file" hidden name="profilePhoto" onChange={handleChange} />
                    </Button>
                    {formData.profilePhoto && (
                        <Typography variant="body2" sx={{ mt: 1 }}>
                            Selected File: {formData.profilePhoto.name}
                        </Typography>
                    )}
                    <Button type="submit" variant="contained" color="primary" fullWidth sx={{ mt: 3 }}>
                        Register
                    </Button>
                </form>
            </Box>
        </Container>
    );
};

export default Register;
