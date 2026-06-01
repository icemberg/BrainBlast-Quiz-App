import React, { createContext, useState, useContext, useEffect } from 'react';
import api from '../api/axios';
import { useToast } from './ToastContext';


const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [user, setUser] = useState(null);
    const { showToast } = useToast();

    useEffect(() => {
        if (token) {
            localStorage.setItem('token', token);
        } else {
            localStorage.removeItem('token');
            setUser(null);
        }

        // Handle Tab Close / Browser Close
        const handleTabClose = () => {
            if (token) {
                // Use sendBeacon for reliable disconnected on unload
                const blob = new Blob([JSON.stringify({})], { type: 'application/json' });
                navigator.sendBeacon(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/auth/disconnect`, blob);

                // Fallback for older browsers or if beacon fails (though beacon is best for unload)
                // Note: we can't set headers easily with beacon, so this relies on the backend 
                // potentially handling it or us relying on the session/cookie if we had one. 
                // Since we use Bearer tokens, beacon is tricky. 
                // Alternative: Synchronous XHR (deprecated but works) or fetch keepalive.

                fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/auth/disconnect`, {
                    method: 'POST',
                    headers: { 'Authorization': `Bearer ${token}` },
                    keepalive: true
                });
            }
        };

        window.addEventListener('beforeunload', handleTabClose);

        return () => {
            window.removeEventListener('beforeunload', handleTabClose);
        };
    }, [token]);


    const login = async (username, password) => {
        try {
            const response = await api.post('/auth/login', { username, password });
            const { token, role, username: user } = response.data;
            setToken(token);
            setUser({ username: user, role });
            localStorage.setItem('user', JSON.stringify({ username: user, role }));
            showToast('Login successful!', 'success');
            return role; // Return role to allow redirection
        } catch (error) {
            console.error("Login failed", error);
            showToast(error.response?.data?.message || 'Login failed. Please check your credentials.', 'error');
            return false;
        }
    };


    const register = async (username, password) => {
        try {
            await api.post('/auth/register', { username, password, role: 'USER' });
            showToast('Registration successful! Please login.', 'success');
            return true;
        } catch (error) {
            console.error("Registration failed", error);
            showToast(error.response?.data?.message || 'Registration failed.', 'error');
            return false;
        }
    }

    const loginWithToken = (token, username, role) => {
        setToken(token);
        setUser({ username, role });
        localStorage.setItem('user', JSON.stringify({ username, role }));
        showToast('Login successful via Google!', 'success');
    };



    const logout = async () => {
        try {
            await api.post('/auth/disconnect');
        } catch (e) {
            console.error("Logout disconnect failed", e);
        }
        setToken(null);
        localStorage.removeItem('user');
    };


    return (

        <AuthContext.Provider value={{ token, user, login, register, logout, loginWithToken }}>
            {children}
        </AuthContext.Provider>
    );

};

export const useAuth = () => useContext(AuthContext);
