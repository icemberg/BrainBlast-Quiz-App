import React, { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const OAuth2RedirectHandler = () => {
    const [searchParams] = useSearchParams();
    const { loginWithToken } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        const token = searchParams.get('token');
        const role = searchParams.get('role');
        const username = searchParams.get('username');

        if (token && username) {
            loginWithToken(token, username, role || 'USER');

            if (role === 'ADMIN') {
                window.location.href = '/dashboard';
            } else {
                window.location.href = '/home';
            }
        } else {
            window.location.href = '/login';
        }
    }, [searchParams, loginWithToken]);

    return (
        <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center text-white">
            <div className="flex flex-col items-center gap-4">
                <div className="w-10 h-10 border-2 border-purple-500 border-t-white rounded-full animate-spin"></div>
                <p className="text-gray-400">Authenticating with Google...</p>
            </div>
        </div>
    );
};

export default OAuth2RedirectHandler;
