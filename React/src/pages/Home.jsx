import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Play, Star, Trophy, Zap, Activity, Users, LogOut, ChevronRight } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import FloatingShapes from '../components/FloatingShapes';
import logo from "../assets/image.png";

const Home = () => {
    const [quizzes, setQuizzes] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();
    const { user, logout } = useAuth();


    useEffect(() => {
        const fetchQuizzes = async () => {
            try {
                const res = await api.get('/quiz/allQuizzes');
                setQuizzes(res.data);
            } catch (error) {
                console.error("Error fetching quizzes", error);
            } finally {
                setLoading(false);
            }
        };
        fetchQuizzes();
    }, []);

    const containerVariants = {
        hidden: { opacity: 0 },
        visible: {
            opacity: 1,
            transition: { staggerChildren: 0.1 }
        }
    };


    const itemVariants = {
        hidden: { y: 20, opacity: 0 },
        visible: { y: 0, opacity: 1 }
    };

    return (
        <div className="min-h-screen bg-[#0a0a0a] text-white relative overflow-hidden font-sans selection:bg-purple-500 selection:text-white">
            <FloatingShapes />

            {/* Navbar */}
            <nav className="relative z-50 px-6 py-6 flex justify-between items-center max-w-7xl mx-auto">
                <div className="flex items-center gap-2">
                    <div className="w-10 h-10 bg-gradient-to-br from-purple-600 to-pink-500 rounded-xl flex items-center justify-center shadow-lg shadow-purple-500/20">
                        <img src={logo} alt="BrainBlast" className="w-10 h-10 object-contain brightness-110 mix-blend-multiply shadow-purple-420/5" />
                    </div>
                    <span className="text-2xl font-bold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
                        BrainBlast
                    </span>
                </div>

                <div className="flex items-center gap-6">


                    <div className="h-6 w-px bg-white/10 hidden md:block"></div>

                    <div className="flex items-center gap-3">
                        <div className="text-right hidden sm:block">
                            <p className="text-sm font-bold text-white leading-tight">Welcome</p>
                            <p className="text-xs text-gray-400 uppercase tracking-wider">Learner</p>
                        </div>
                        <button
                            onClick={logout}
                            className="p-2 rounded-full bg-white/5 hover:bg-white/10 border border-white/5 transition-colors group"
                            title="Logout"
                        >
                            <LogOut size={18} className="text-gray-400 group-hover:text-red-400 transition-colors" />
                        </button>
                    </div>
                </div>
            </nav>

            {/* Hero Section */}
            <main className="relative z-10 max-w-7xl mx-auto px-6 pt-12 pb-24">
                <motion.div
                    initial="hidden"
                    animate="visible"
                    variants={containerVariants}
                    className="text-center mb-24 max-w-4xl mx-auto"
                >
                    <motion.div variants={itemVariants} className="inline-block mb-4">
                        <span className="px-4 py-1.5 rounded-full border border-purple-500/30 bg-purple-500/10 text-purple-300 text-sm font-medium backdrop-blur-sm">
                            ✨ The Future of Learning is Here
                        </span>
                    </motion.div>

                    <motion.h1 variants={itemVariants} className="text-5xl md:text-7xl font-extrabold tracking-tight mb-8 leading-tight">
                        Master Any Subject with <br />
                        <span className="bg-clip-text text-transparent bg-gradient-to-r from-purple-400 via-pink-500 to-red-500">
                            Lightning Speed
                        </span>
                    </motion.h1>

                    <motion.p variants={itemVariants} className="text-xl text-gray-400 mb-10 max-w-2xl mx-auto leading-relaxed">
                        Compete in real-time, track your progress, and challenge yourself with our curated collection of premium quizzes.
                    </motion.p>

                    <motion.div variants={itemVariants} className="flex justify-center gap-4">
                        <button
                            onClick={() => document.getElementById('quiz-grid').scrollIntoView({ behavior: 'smooth' })}
                            className="px-8 py-4 bg-white text-black font-bold rounded-2xl hover:bg-gray-100 transition-transform hover:scale-105 shadow-xl shadow-white/10 flex items-center gap-2"
                        >
                            Explore Quizzes <ChevronRight size={20} />
                        </button>
                    </motion.div>
                </motion.div>

                {/* Quiz Grid */}
                <div id="quiz-grid" className="scroll-mt-24">
                    <div className="flex items-center justify-between mb-10">
                        <h2 className="text-3xl font-bold">Trending Quizzes</h2>
                        <div className="flex gap-2">
                            <button className="px-4 py-2 rounded-lg bg-white/5 border border-white/10 text-sm font-medium hover:bg-white/10 transition-colors">All</button>
                            <button className="px-4 py-2 rounded-lg bg-transparent text-gray-400 text-sm font-medium hover:text-white transition-colors">Latest</button>
                            <button className="px-4 py-2 rounded-lg bg-transparent text-gray-400 text-sm font-medium hover:text-white transition-colors">Popular</button>
                        </div>
                    </div>

                    {loading ? (
                        <div className="flex justify-center py-20">
                            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-purple-500"></div>
                        </div>
                    ) : (
                        <motion.div
                            initial="hidden"
                            whileInView="visible"
                            viewport={{ once: true }}
                            variants={containerVariants}
                            className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 xl:gap-8"
                        >
                            {quizzes.length === 0 ? (
                                <div className="col-span-3 text-center py-20 bg-white/5 rounded-3xl border border-dashed border-white/10">
                                    <Trophy size={48} className="mx-auto text-gray-600 mb-4" />
                                    <p className="text-xl text-gray-400">No quizzes available right now.</p>
                                    <p className="text-gray-500 text-sm">Check back later or ask an admin to create one!</p>
                                </div>
                            ) : (
                                quizzes.map((quiz, index) => (
                                    <motion.div
                                        key={quiz.id}
                                        variants={itemVariants}
                                        whileHover={{ y: -10, transition: { duration: 0.3 } }}
                                        onClick={() => navigate(`/quiz/${quiz.id}`, { state: { category: quiz.category || 'General', title: quiz.title } })}
                                        className="group relative bg-[#1a1a1a] rounded-3xl p-1 overflow-hidden cursor-pointer"
                                    >
                                        <div className="absolute inset-0 bg-gradient-to-br from-purple-500/20 via-transparent to-pink-500/20 opacity-0 group-hover:opacity-100 transition-opacity duration-500" />

                                        <div className="relative h-full bg-[#151515] rounded-[22px] p-6 border border-white/5 group-hover:border-purple-500/30 transition-colors flex flex-col">

                                            <div className="flex justify-between items-start mb-6">
                                                <div className="p-3 bg-gradient-to-br from-purple-500/10 to-pink-500/10 rounded-2xl group-hover:scale-110 transition-transform duration-300">
                                                    {index % 2 === 0 ? <Trophy className="text-purple-400" size={24} /> : <Activity className="text-pink-400" size={24} />}
                                                </div>
                                                <span className="px-3 py-1 rounded-full bg-white/5 border border-white/10 text-xs font-medium text-gray-400 uppercase tracking-wide">
                                                    {quiz.category || 'General'}
                                                </span>
                                            </div>

                                            <h3 className="text-2xl font-bold mb-3 text-white group-hover:text-purple-300 transition-colors line-clamp-2">
                                                {quiz.title}
                                            </h3>

                                            <div className="mt-auto pt-6 border-t border-white/5 flex items-center justify-between">
                                                <div className="flex items-center gap-1.5 text-gray-400 text-sm">
                                                    <div className="flex text-yellow-500">
                                                        <Star size={14} fill="currentColor" />
                                                        <Star size={14} fill="currentColor" />
                                                        <Star size={14} fill="currentColor" />
                                                        <Star size={14} fill="currentColor" />
                                                        <Star size={14} className="text-gray-600" fill="currentColor" />
                                                    </div>
                                                    <span>(4.2)</span>
                                                </div>
                                                <div className="flex items-center gap-3 text-sm font-medium">
                                                    <span className="text-gray-400">{quiz.questions ? quiz.questions.length : 0} Qs</span>
                                                    <div className="w-8 h-8 rounded-full bg-white text-black flex items-center justify-center transform group-hover:translate-x-1 transition-transform">
                                                        <Play size={14} fill="currentColor" />
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </motion.div>
                                ))
                            )}
                        </motion.div>
                    )}
                </div>
            </main>

            {/* Footer */}
            <footer className="relative z-10 border-t border-white/10 py-12 text-center">
                <p className="text-gray-500 text-sm">
                    © 2026 BrainBlast Quiz App. Designed with 💜 for learners.
                </p>
            </footer>
        </div>
    );
};

export default Home;


