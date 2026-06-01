import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Users, FileQuestion, CheckCircle, Activity } from 'lucide-react';
import api from '../../api/axios';


const StatCard = ({ title, value, icon: Icon, color }) => (
    <motion.div
        whileHover={{ y: -5 }}
        className={`bg-white p-6 rounded-xl border border-gray-200 shadow-lg`}
    >
        <div className="flex items-center justify-between">
            <div>
                <p className="text-gray-500 text-sm font-medium">{title}</p>
                <h3 className="text-2xl font-bold text-gray-900 mt-1">{value}</h3>
            </div>
            <div className={`p-3 rounded-lg ${color} bg-opacity-20`}>
                <Icon size={24} className={color.replace('bg-', 'text-')} />
            </div>
        </div>
    </motion.div>
);

const DashboardOverview = () => {
    const [stats, setStats] = React.useState({
        totalQuizzes: 0,
        activeUsers: 0,
        completed: 0,
        avgScore: 0,
        recentActivity: []
    });

    React.useEffect(() => {
        fetchStats();
        const interval = setInterval(fetchStats, 5000); // Poll every 5 seconds for real-time updates
        return () => clearInterval(interval);
    }, []);


    const fetchStats = async () => {
        try {
            const response = await api.get('/admin/stats');
            setStats(response.data);
        } catch (error) {
            console.error("Error fetching stats", error);
        }
    };

    const statCards = [
        { title: 'Total Quizzes', value: stats.totalQuizzes, icon: FileQuestion, color: 'bg-blue-500' },
        { title: 'Active Users', value: stats.activeUsers, icon: Users, color: 'bg-green-500' },
        { title: 'Completed', value: stats.completed, icon: CheckCircle, color: 'bg-purple-500' },
        { title: 'Avg Score', value: `${stats.avgScore}%`, icon: Activity, color: 'bg-pink-500' },
    ];

    return (
        <div>
            <h2 className="text-3xl font-bold text-white mb-8">Dashboard Overview</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 xl:gap-8 gap-6">
                {statCards.map((stat, index) => (
                    <StatCard key={index} {...stat} />
                ))}
            </div>

            <div className="mt-8 grid grid-cols-1 gap-6">
                <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-xl">
                    <h3 className="text-xl font-bold text-gray-900 mb-4">Recent Activity</h3>
                    <div className="space-y-4">
                        {stats.recentActivity.length === 0 ? (
                            <p className="text-gray-500">No recent activity found.</p>
                        ) : (
                            stats.recentActivity.map((activity) => (
                                <div key={activity.id} className="flex items-center p-3 bg-gray-50 rounded-lg border border-gray-100">
                                    <div className="w-2 h-2 rounded-full bg-green-400 mr-3"></div>
                                    <p className="text-gray-700 text-sm">
                                        User <span className="font-bold text-gray-900">{activity.username}</span> completed <span className="font-medium text-purple-600">{activity.quizTitle}</span>
                                    </p>
                                    <div className="ml-auto flex items-center gap-4">
                                        <span className="font-bold text-gray-900">Score: {activity.score}/{activity.totalQuestions}</span>
                                        <span className="text-xs text-gray-500">{new Date(activity.submissionDate).toLocaleString()}</span>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};


export default DashboardOverview;
