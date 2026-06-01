import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
    Zap, LogIn, ChevronRight, Brain, Trophy, Activity,
    BarChart3, Globe, Clock, Layers, Star, CheckCircle,
    ArrowRight, Quote, Menu, X
} from 'lucide-react';
import FloatingShapes from '../components/FloatingShapes';
import { useAuth } from '../context/AuthContext';
import logo from "../assets/image.png";
import api from '../api/axios';

// --- Components ---

const Navbar = ({ scrolled }) => {
    const navigate = useNavigate();
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    return (
        <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${scrolled ? 'bg-[#0a0a0a]/80 backdrop-blur-md border-b border-white/5 py-4' : 'bg-transparent py-6'}`}>
            <div className="max-w-7xl mx-auto px-6 flex justify-between items-center">
                <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate('/')}>
                    <div className="w-10 h-10 bg-gradient-to-br from-purple-600 to-pink-500 rounded-xl flex items-center justify-center shadow-lg shadow-purple-500/20">
                        <img src={logo} alt="BrainBlast" className="w-10 h-10 object-contain brightness-110 mix-blend-multiply shadow-purple-420/5" />
                    </div>
                    <span className="text-2xl font-bold tracking-tight text-white">
                        BrainBlast
                    </span>
                </div>

                {/* Desktop Nav */}
                <div className="hidden md:flex items-center gap-8">
                    {['Features', 'Categories', 'How it Works', 'Testimonials'].map((item) => (
                        <a key={item} href={`#${item.toLowerCase().replace(/ /g, '-')}`} className="text-gray-400 hover:text-white text-sm font-medium transition-colors">
                            {item}
                        </a>
                    ))}
                </div>

                <div className="hidden md:flex items-center gap-4">
                    <button onClick={() => navigate('/login')} className="text-white font-medium hover:text-purple-400 transition-colors">Sign In</button>
                    <button
                        onClick={() => navigate('/register')}
                        className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-500 hover:to-pink-500 shadow-lg shadow-purple-500/25 transition-all font-bold text-sm relative overflow-hidden group"
                    >
                        <span className="relative z-10">Sign Up Free</span>
                        <div className="absolute inset-0 bg-white/20 translate-y-full group-hover:translate-y-0 transition-transform duration-300" />
                    </button>
                </div>

                {/* Mobile Toggle */}
                <button className="md:hidden text-white" onClick={() => setMobileMenuOpen(!mobileMenuOpen)}>
                    {mobileMenuOpen ? <X /> : <Menu />}
                </button>
            </div>

            {/* Mobile Menu */}
            <AnimatePresence>
                {mobileMenuOpen && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: 'auto', opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        className="md:hidden bg-[#0a0a0a] border-b border-white/10 overflow-hidden"
                    >
                        <div className="px-6 py-6 flex flex-col gap-4">
                            {['Features', 'Categories', 'How it Works', 'Testimonials'].map((item) => (
                                <a key={item} href={`#${item.toLowerCase().replace(/ /g, '-')}`} onClick={() => setMobileMenuOpen(false)} className="text-gray-300 text-lg">
                                    {item}
                                </a>
                            ))}
                            <hr className="border-white/10 my-2" />
                            <button onClick={() => navigate('/login')} className="text-white text-left text-lg">Sign In</button>
                            <button onClick={() => navigate('/register')} className="text-purple-400 text-left text-lg font-bold">Sign Up Free</button>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </nav>
    );
};

const FeatureCard = ({ icon: Icon, title, desc, color }) => (
    <motion.div
        whileHover={{ y: -10 }}
        className="p-8 rounded-3xl bg-white/5 border border-white/5 hover:border-white/10 transition-colors relative overflow-hidden group"
    >
        <div className={`w-14 h-14 rounded-2xl ${color} bg-opacity-10 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300`}>
            <Icon size={28} className={color.replace('bg-', 'text-').replace('-500', '-400')} />
        </div>
        <h3 className="text-xl font-bold mb-3 text-white">{title}</h3>
        <p className="text-gray-400 leading-relaxed">{desc}</p>
        <div className="absolute inset-0 bg-gradient-to-br from-white/0 to-white/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none" />
    </motion.div>
);

const CategoryCard = ({ title, count, image, onClick }) => (
    <motion.div
        whileHover={{ scale: 1.02 }}
        onClick={onClick}
        className="relative h-64 rounded-3xl overflow-hidden cursor-pointer group"
    >
        <img src={image} alt={title} className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/40 to-transparent" />
        <div className="absolute bottom-0 left-0 p-6">
            <h3 className="text-2xl font-bold text-white mb-1 group-hover:text-purple-300 transition-colors">{title}</h3>
            <p className="text-gray-300 text-sm">{count} Questions</p>
        </div>
        <div className="absolute top-4 right-4 w-10 h-10 bg-white/10 backdrop-blur-md rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all transform translate-y-2 group-hover:translate-y-0">
            <ArrowRight size={18} className="text-white" />
        </div>
    </motion.div>
);

const StepCard = ({ number, title, desc }) => (
    <div className="relative z-10 flex flex-col items-center text-center">
        <div className="w-16 h-16 rounded-full bg-gradient-to-br from-purple-600 to-pink-600 flex items-center justify-center text-2xl font-bold text-white mb-6 shadow-lg shadow-purple-500/30">
            {number}
        </div>
        <h3 className="text-xl font-bold text-white mb-3">{title}</h3>
        <p className="text-gray-400">{desc}</p>
    </div>
);

const TestimonialCard = ({ quote, name, role, avatar }) => (
    <div className="bg-white/5 border border-white/5 p-8 rounded-3xl relative">
        <Quote className="absolute top-8 right-8 text-white/10" size={40} />
        <p className="text-lg text-gray-300 italic mb-6">"{quote}"</p>
        <div className="flex items-center gap-4">
            <img src={avatar} alt={name} className="w-12 h-12 rounded-full object-cover border-2 border-purple-500" />
            <div>
                <h4 className="text-white font-bold">{name}</h4>
                <p className="text-purple-400 text-sm">{role}</p>
            </div>
        </div>
    </div>
);

// --- Main Page ---

const LandingPage = () => {
    const navigate = useNavigate();
    const { user } = useAuth();
    const [scrolled, setScrolled] = useState(false);
    const [startingQuiz, setStartingQuiz] = useState(false);

    useEffect(() => {
        const handleScroll = () => setScrolled(window.scrollY > 50);
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, [user, navigate]);

    const handleCategoryClick = async (categoryTitle) => {
        if (!user) {
            navigate('/login');
            return;
        }

        // Map Display Title to Internal Category Name
        let category = categoryTitle;
        if (categoryTitle.includes("Technology")) category = "Technology";
        if (categoryTitle.includes("Science")) category = "Science";
        if (categoryTitle.includes("History")) category = "History";
        if (categoryTitle.includes("Arts")) category = "Arts";
        if (categoryTitle.includes("Sports")) category = "Sports";
        if (categoryTitle.includes("General")) category = "General";

        setStartingQuiz(true);
        try {
            const res = await api.post('/quiz/start', null, {
                params: {
                    category: category,
                    numQ: 10,
                    title: `${categoryTitle} Challenge`
                }
            });
            // If successful, backend returns Quiz object which has ID
            if (res.data && res.data.id) {
                navigate(`/quiz/${res.data.id}`, { state: { category: category, title: `${categoryTitle} Challenge` } });
            } else {
                alert("Failed to create quiz: No ID returned.");
            }
        } catch (error) {
            console.error("Failed to start quiz:", error);
            alert("Could not start quiz. Please try again.");
        } finally {
            setStartingQuiz(false);
        }
    };

    return (
        <div className="min-h-screen bg-[#0a0a0a] text-white font-sans selection:bg-purple-500 selection:text-white">
            <Navbar scrolled={scrolled} />

            {/* Shapes Background */}
            <div className="fixed inset-0 overflow-hidden pointer-events-none z-0">
                <FloatingShapes />
            </div>

            {/* 1. Hero Section */}
            <section className="relative z-10 pt-40 pb-32 px-6">
                <div className="max-w-7xl mx-auto text-center">
                    <motion.div
                        initial={{ opacity: 0, y: 30 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.8 }}
                    >
                        <span className="inline-block px-5 py-2 rounded-full border border-purple-500/30 bg-purple-500/10 text-purple-300 text-sm font-bold mb-8 backdrop-blur-sm">
                            🚀 The Future of Gamified Learning
                        </span>

                        <h1 className="text-5xl md:text-7xl lg:text-8xl xl:text-9xl font-black tracking-tight mb-8 leading-[1.1]">
                            Think Fast. Learn Faster. <br />
                            <span className="bg-clip-text text-transparent bg-gradient-to-r from-purple-400 via-pink-500 to-red-500">
                                Win Every Quiz.
                            </span>
                        </h1>

                        <p className="text-xl md:text-2xl text-gray-400 mb-12 max-w-3xl mx-auto leading-relaxed font-light">
                            Interactive quizzes that challenge your mind, track your growth, and ignite your competitive spirit. Join thousands of learners today.
                        </p>

                        <div className="flex flex-col sm:flex-row justify-center gap-5">
                            <motion.button
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                                onClick={() => navigate('/register')}
                                className="px-10 py-5 bg-white text-black text-lg font-bold rounded-2xl shadow-[0_0_20px_rgba(255,255,255,0.3)] hover:shadow-[0_0_30px_rgba(255,255,255,0.5)] transition-all flex items-center justify-center gap-2"
                            >
                                Start Quiz Now <Zap size={20} className="text-purple-600" fill="currentColor" />
                            </motion.button>
                            <motion.button
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                                onClick={() => document.getElementById('categories').scrollIntoView({ behavior: 'smooth' })}
                                className="px-10 py-5 bg-[#1a1a1a] border border-white/10 text-white text-lg font-bold rounded-2xl hover:bg-[#252525] transition-colors flex items-center justify-center gap-2"
                            >
                                Explore Categories <ChevronRight size={20} />
                            </motion.button>
                        </div>
                    </motion.div>
                </div>
            </section>

            {/* 2. Key Features */}
            <section id="features" className="relative z-10 py-24 bg-[#0a0a0a]">
                <div className="max-w-7xl mx-auto px-6">
                    <div className="text-center mb-16">
                        <h2 className="text-4xl md:text-5xl font-bold mb-4">Why BrainBlast?</h2>
                        <p className="text-gray-400 text-lg">Everything you need to master any subject.</p>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 xl:gap-8 gap-6">
                        <FeatureCard
                            icon={Clock}
                            color="bg-purple-500"
                            title="Real-Time Challenges"
                            desc="Take timed quizzes and test your reflexes against the clock."
                        />
                        <FeatureCard
                            icon={BarChart3}
                            color="bg-pink-500"
                            title="Progress Tracking"
                            desc="Visualize your learning journey with detailed performance analytics."
                        />
                        <FeatureCard
                            icon={Globe}
                            color="bg-emerald-500"
                            title="Global Leaderboards"
                            desc="Compete with learners worldwide and climb the ranks."
                        />
                        <FeatureCard
                            icon={Brain}
                            color="bg-blue-500"
                            title="Personalized Insights"
                            desc="Get smarter recommendations based on your weak areas."
                        />
                    </div>
                </div>
            </section>

            {/* 3. Categories */}
            <section id="categories" className="relative z-10 py-24 bg-[#111]">
                <div className="max-w-7xl mx-auto px-6">
                    <div className="flex justify-between items-end mb-12">
                        <div>
                            <h2 className="text-4xl md:text-5xl font-bold mb-4">Choose Your Challenge</h2>
                            <p className="text-gray-400 text-lg">Dive into thousands of quizzes across diverse topics.</p>
                        </div>
                        <button className="hidden md:flex items-center gap-2 text-purple-400 font-bold hover:text-purple-300 transition-colors">
                            View All <ArrowRight size={18} />
                        </button>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        <CategoryCard
                            title="Technology & Coding"
                            count="120+"
                            image="https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&q=80&w=800"
                            onClick={() => handleCategoryClick("Technology")}
                        />
                        <CategoryCard
                            title="Science & Nature"
                            count="85+"
                            image="https://images.unsplash.com/photo-1532094349884-543bc11b234d?auto=format&fit=crop&q=80&w=800"
                            onClick={() => handleCategoryClick("Science")}
                        />
                        <CategoryCard
                            title="History & Culture"
                            count="200+"
                            image="https://images.unsplash.com/photo-1461360370896-922624d12aa1?auto=format&fit=crop&q=80&w=800"
                            onClick={() => handleCategoryClick("History")}
                        />
                        <CategoryCard
                            title="Arts & Literature"
                            count="64+"
                            image="https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&q=80&w=800"
                            onClick={() => handleCategoryClick("Arts")}
                        />
                        <CategoryCard
                            title="Sports"
                            count="150+"
                            image="https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&q=80&w=800"
                            onClick={() => handleCategoryClick("Sports")}
                        />
                        <CategoryCard
                            title="General Knowledge"
                            count="500+"
                            image="https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&q=80&w=800"
                            onClick={() => handleCategoryClick("General")}
                        />
                    </div>
                </div>
            </section>

            {/* 4. How It Works */}
            <section id="how-it-works" className="relative z-10 py-24 bg-[#0a0a0a]">
                <div className="max-w-7xl mx-auto px-6">
                    <div className="text-center mb-20">
                        <h2 className="text-4xl md:text-5xl font-bold mb-4">How It Works</h2>
                        <p className="text-gray-400 text-lg">Start learning in 3 simple steps.</p>
                    </div>

                    <div className="relative grid grid-cols-1 md:grid-cols-3 gap-12">
                        {/* Connecting Line (Desktop) */}
                        <div className="hidden md:block absolute top-8 left-[16%] right-[16%] h-0.5 bg-gradient-to-r from-purple-900 via-pink-900 to-purple-900 z-0"></div>

                        <StepCard
                            number="1"
                            title="Create an Account"
                            desc="Sign up for free and set up your learner profile in seconds."
                        />
                        <StepCard
                            number="2"
                            title="Select a Quiz"
                            desc="Choose from 50+ categories or let our AI recommend one for you."
                        />
                        <StepCard
                            number="3"
                            title="Climb the Ranks"
                            desc="Answer correctly, earn points, and dominate the global leaderboard."
                        />
                    </div>
                </div>
            </section>

            {/* 5. Testimonials */}
            <section id="testimonials" className="relative z-10 py-24 bg-[#111]">
                <div className="max-w-7xl mx-auto px-6">
                    <h2 className="text-4xl md:text-5xl font-bold text-center mb-16">Loved by Learners</h2>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                        <TestimonialCard
                            quote="BrainBlast has completely changed how I study for exams. It's actually fun now!"
                            name="Sarah Johnson"
                            role="Computer Science Student"
                            avatar="https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=150"
                        />
                        <TestimonialCard
                            quote="The competitive element keeps me coming back. I love seeing my name on the leaderboard."
                            name="Michael Chen"
                            role="Trivia Enthusiast"
                            avatar="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=150"
                        />
                        <TestimonialCard
                            quote="A clean, beautiful interface that just works. The best quiz app I've used so far."
                            name="Jessica Williams"
                            role="Teacher"
                            avatar="https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&q=80&w=150"
                        />
                    </div>
                </div>
            </section>

            {/* 6. CTA */}
            <section className="relative z-10 py-32 px-6">
                <div className="absolute inset-0 bg-gradient-to-b from-transparent to-purple-900/20 pointer-events-none"></div>
                <div className="max-w-4xl mx-auto text-center bg-gradient-to-br from-purple-600/20 to-pink-600/20 border border-white/10 backdrop-blur-3xl rounded-[3rem] p-12 md:p-20 relative overflow-hidden">
                    <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-white/50 to-transparent"></div>

                    <h2 className="text-5xl md:text-6xl font-black mb-8">Ready to test your limits?</h2>
                    <p className="text-xl text-gray-300 mb-10 max-w-2xl mx-auto">
                        Join the community today and start your journey towards mastery. It's free!
                    </p>
                    <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={() => navigate('/register')}
                        className="px-12 py-5 bg-white text-black text-xl font-bold rounded-2xl shadow-xl hover:shadow-2xl transition-all"
                    >
                        Start Your First Quiz
                    </motion.button>
                </div>
            </section>

            {/* Footer */}
            <footer className="relative z-10 bg-[#050505] border-t border-white/10 pt-20 pb-10 px-6">
                <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-12 mb-16">
                    <div>
                        <div className="flex items-center gap-2 mb-6">
                            <div className="w-8 h-8 bg-gradient-to-br from-purple-600 to-pink-500 rounded-lg flex items-center justify-center">
                                <Zap fill="white" size={16} />
                            </div>
                            <span className="text-xl font-bold">BrainBlast</span>
                        </div>
                        <p className="text-gray-500 text-sm leading-relaxed">
                            The ultimate destination for gamified learning and competitive quizzing.
                        </p>
                    </div>
                    <div>
                        <h4 className="font-bold mb-6">Platform</h4>
                        <ul className="space-y-4 text-gray-500 text-sm">
                            <li className="hover:text-white cursor-pointer">Features</li>
                            <li className="hover:text-white cursor-pointer">Live Quizzes</li>
                            <li className="hover:text-white cursor-pointer">Leaderboard</li>
                        </ul>
                    </div>
                    <div>
                        <h4 className="font-bold mb-6">Company</h4>
                        <ul className="space-y-4 text-gray-500 text-sm">
                            <li className="hover:text-white cursor-pointer">About Us</li>
                            <li className="hover:text-white cursor-pointer">Careers</li>
                            <li className="hover:text-white cursor-pointer">Contact</li>
                        </ul>
                    </div>
                    <div>
                        <h4 className="font-bold mb-6">Legal</h4>
                        <ul className="space-y-4 text-gray-500 text-sm">
                            <li className="hover:text-white cursor-pointer">Privacy Policy</li>
                            <li className="hover:text-white cursor-pointer">Terms of Service</li>
                        </ul>
                    </div>
                </div>
                <div className="max-w-7xl mx-auto border-t border-white/5 pt-8 text-center text-gray-600 text-sm">
                    © 2026 BrainBlast Quiz App. All rights reserved.
                </div>
            </footer>
        </div>
    );
};

export default LandingPage;
