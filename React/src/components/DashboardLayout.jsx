import React, { useState } from 'react';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { motion } from 'framer-motion';
import {
    LayoutDashboard,
    FileQuestion,
    PlusCircle,
    LogOut,
    Menu,
    X
} from 'lucide-react';
import { IconButton } from '@mui/material';

const DashboardLayout = ({ children }) => {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const { logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const navItems = [
        { icon: LayoutDashboard, label: 'Overview', path: '/dashboard' },
        { icon: FileQuestion, label: 'Questions', path: '/dashboard/questions' },
        { icon: PlusCircle, label: 'Create Quiz', path: '/dashboard/create-quiz' },
    ];

    return (
        <div className="flex h-screen bg-gray-900 text-white overflow-hidden">
            {/* Sidebar */}
            <motion.div
                className={`bg-gray-800 border-r border-gray-700 h-full flex flex-col ${isSidebarOpen ? 'w-64' : 'w-20'} transition-width duration-300`}
                initial={false}
                animate={{ width: isSidebarOpen ? 256 : 80 }}
            >
                <div className="p-4 flex items-center justify-between border-b border-gray-700">
                    {isSidebarOpen && <h1 className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-purple-400 to-pink-600">QuizAdmin</h1>}
                    <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="p-2 hover:bg-gray-700 rounded-lg">
                        {isSidebarOpen ? <X size={20} /> : <Menu size={20} />}
                    </button>
                </div>

                <nav className="flex-1 p-2 space-y-2 mt-4">
                    {navItems.map((item) => {
                        const Icon = item.icon;
                        const isActive = location.pathname === item.path;
                        return (
                            <Link to={item.path} key={item.path}>
                                <div className={`flex items-center p-3 rounded-lg transition-colors ${isActive ? 'bg-purple-600' : 'hover:bg-gray-700'}`}>
                                    <Icon size={24} className={isActive ? 'text-white' : 'text-gray-400'} />
                                    {isSidebarOpen && <span className="ml-3 font-medium">{item.label}</span>}
                                </div>
                            </Link>
                        );
                    })}
                </nav>

                <div className="p-4 border-t border-gray-700">
                    <button onClick={handleLogout} className="flex items-center w-full p-2 text-red-400 hover:bg-red-400/10 rounded-lg transition-colors">
                        <LogOut size={24} />
                        {isSidebarOpen && <span className="ml-3 font-medium">Logout</span>}
                    </button>
                </div>
            </motion.div>

            {/* Main Content */}
            <div className="flex-1 overflow-auto bg-gray-900">
                <main className="p-8">
                    {children ? children : <Outlet />}
                </main>
            </div>
        </div>
    );
};

export default DashboardLayout;
