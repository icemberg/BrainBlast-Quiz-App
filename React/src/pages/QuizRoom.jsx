import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowRight, CheckCircle, XCircle, RefreshCw, BookOpen, Loader2 } from 'lucide-react';
import api from '../api/axios';
import { CircularProgress } from '@mui/material';
import { useToast } from '../context/ToastContext';
import ReactMarkdown from 'react-markdown';


const QuizRoom = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const [questions, setQuestions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentQIndex, setCurrentQIndex] = useState(0);
    const [responses, setResponses] = useState([]); // Array of { id, response }
    const [score, setScore] = useState(null);
    const [totalQuestions, setTotalQuestions] = useState(0);
    const { showToast } = useToast();

    // --- Answer Verification & Remedial Explanation State ---
    const [verificationResult, setVerificationResult] = useState(null); // { correct, correctAnswer } or null
    const [isVerifying, setIsVerifying] = useState(false);
    const [explanation, setExplanation] = useState(null); // Markdown string from AI
    const [isLoadingExplanation, setIsLoadingExplanation] = useState(false);
    const [optionsLocked, setOptionsLocked] = useState(false); // Prevent re-selecting after verification

    // Context from navigation (Category for Endless Mode)
    const category = location.state?.category;
    const title = location.state?.title || 'Quiz Challenge';

    useEffect(() => {
        // Reset state when ID changes (new quiz loaded)
        setScore(null);
        setCurrentQIndex(0);
        setResponses([]);
        setLoading(true);
        resetQuestionState();
        fetchQuiz();
    }, [id]);

    const resetQuestionState = () => {
        setVerificationResult(null);
        setExplanation(null);
        setIsLoadingExplanation(false);
        setIsVerifying(false);
        setOptionsLocked(false);
    };

    const fetchQuiz = async () => {
        try {
            const res = await api.get(`/quiz/getQuizQuestions/${id}`);
            setQuestions(res.data);
            setTotalQuestions(res.data.length);
            // Initialize empty responses
            setResponses(res.data.map(q => ({ id: q.id, response: '' })));
        } catch (error) {
            console.error(error);
            showToast("Quiz not found or error loading quiz.", "error");
            navigate('/home');
        } finally {

            setLoading(false);
        }
    };

    const handleOptionSelect = async (option) => {
        if (optionsLocked || isVerifying) return; // Prevent re-selection

        // Update the response
        const newResponses = [...responses];
        newResponses[currentQIndex].response = option;
        setResponses(newResponses);

        // Lock options and verify immediately
        setOptionsLocked(true);
        setIsVerifying(true);

        try {
            const currentQuestion = questions[currentQIndex];
            const res = await api.post('/quiz/verifyAnswer', {
                questionId: currentQuestion.id,
                selectedAnswer: option,
            });

            const verification = res.data;
            setVerificationResult(verification);

            // If wrong, fire the remedial explanation request in the background
            if (!verification.correct) {
                fetchRemedialExplanation(
                    currentQuestion.questionTitle,
                    option,
                    verification.correctAnswer
                );
            }
        } catch (error) {
            console.error("Verification failed:", error);
            showToast("Could not verify answer. Continuing anyway.", "error");
            // Unlock so the user can proceed
            setOptionsLocked(false);
        } finally {
            setIsVerifying(false);
        }
    };

    const fetchRemedialExplanation = async (question, userAnswer, correctAnswer) => {
        setIsLoadingExplanation(true);
        try {
            const res = await api.post('/quiz/remedial-explanation', {
                question,
                userAnswer,
                correctAnswer,
            });
            setExplanation(res.data.explanation);
        } catch (error) {
            console.error("Remedial explanation failed:", error);
            setExplanation(
                `### Explanation Unavailable\nWe couldn't fetch a detailed explanation right now. The correct answer is: **${correctAnswer}**`
            );
        } finally {
            setIsLoadingExplanation(false);
        }
    };

    const handleNext = () => {
        if (currentQIndex < questions.length - 1) {
            resetQuestionState();
            setCurrentQIndex(prev => prev + 1);
        } else {
            handleSubmit();
        }
    };

    const handleSubmit = async () => {
        try {
            setLoading(true);
            const res = await api.post(`/quiz/submitQuiz/${id}`, responses);
            setScore(res.data);
        } catch (error) {
            console.error(error);
            showToast("Failed to submit quiz. Please try again.", "error");
        } finally {

            setLoading(false);
        }
    };

    const handlePlayAgain = async () => {
        if (!category) {
            showToast("Cannot restart: Category unknown.", "error");
            return;
        }

        try {
            setLoading(true);

            // Fetch total questions for this category (Endless Mode)
            let numQ = 5;
            try {
                const countRes = await api.get(`/quiz/count/${category}`);
                if (countRes.data && !isNaN(countRes.data)) {
                    numQ = parseInt(countRes.data);
                }
            } catch (err) {
                console.error("Failed to fetch question count, defaulting to 10", err);
            }

            // Request new quiz
            const res = await api.post('/quiz/start', null, {
                params: {
                    category: category,
                    numQ: numQ,
                    title: title
                }
            });

            if (res.data && res.data.id) {
                navigate(`/quiz/${res.data.id}`, { state: { category, title } });
            } else {
                showToast("Failed to start new quiz.", "error");
                setLoading(false);
            }
        } catch (error) {
            console.error("Play Again Error:", error);
            showToast("Error starting new quiz.", "error");
            setLoading(false);
        }
    };

    // --- Helpers for option styling ---
    const getOptionStyle = (option) => {
        const isSelected = responses[currentQIndex]?.response === option;

        // Before verification: standard selected/unselected styles
        if (!verificationResult) {
            if (isSelected) return 'bg-purple-600/20 border-purple-500 text-white';
            return 'bg-gray-800 border-gray-700 text-gray-300 hover:bg-gray-700';
        }

        // After verification
        const isCorrectOption = option === verificationResult.correctAnswer;

        if (isCorrectOption) {
            // Always highlight the correct answer in green
            return 'bg-emerald-600/20 border-emerald-500 text-emerald-300';
        }
        if (isSelected && !verificationResult.correct) {
            // The user's wrong pick in red
            return 'bg-red-600/20 border-red-500 text-red-300';
        }
        // All other options: dimmed
        return 'bg-gray-800/50 border-gray-700/50 text-gray-500';
    };

    const getOptionIcon = (option) => {
        if (!verificationResult) return null;

        const isSelected = responses[currentQIndex]?.response === option;
        const isCorrectOption = option === verificationResult.correctAnswer;

        if (isCorrectOption) return <CheckCircle className="text-emerald-400 shrink-0" size={20} />;
        if (isSelected && !verificationResult.correct) return <XCircle className="text-red-400 shrink-0" size={20} />;
        return null;
    };


    if (loading) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center">
                <CircularProgress color="secondary" />
            </div>
        );
    }

    if (score) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center text-white p-4">
                <motion.div
                    initial={{ scale: 0.8, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    className="bg-gray-900 p-8 rounded-2xl max-w-md w-full text-center border border-gray-800"
                >
                    <TrophyIcon />
                    <h2 className="text-3xl font-bold mb-4">Quiz Completed!</h2>
                    <p className="text-xl text-purple-400 mb-8">{score}</p>

                    {/* Endless Mode Button */}
                    {category && (
                        <button
                            onClick={handlePlayAgain}
                            className="w-full mb-4 bg-gradient-to-r from-purple-600 to-pink-600 text-white font-bold py-3 rounded-lg hover:from-purple-500 hover:to-pink-500 transition-all flex items-center justify-center gap-2"
                        >
                            <RefreshCw size={20} /> Play Again
                        </button>
                    )}

                    <button
                        onClick={() => navigate('/home')}
                        className="w-full bg-white/10 text-white font-bold py-3 rounded-lg hover:bg-white/20 transition-colors"
                    >
                        Back to Dashboard
                    </button>
                </motion.div>
            </div>
        );
    }

    const currentQuestion = questions[currentQIndex];

    if (!currentQuestion) {
        return (
            <div className="min-h-screen bg-black flex items-center justify-center text-white">
                <div className="p-8 text-center">
                    <h2 className="text-2xl font-bold mb-4">No questions available</h2>
                    <button
                        onClick={() => navigate('/home')}
                        className="bg-white text-black px-6 py-2 rounded-lg font-bold hover:bg-gray-200"
                    >
                        Back to Dashboard
                    </button>
                </div>
            </div>
        );
    }


    return (
        <div className="min-h-screen bg-black text-white p-4 flex flex-col items-center justify-center">
            <div className="w-full max-w-3xl xl:max-w-5xl transition-all duration-300">
                <div className="flex justify-between items-center mb-8">
                    <span className="text-gray-400">Question {currentQIndex + 1}/{totalQuestions}</span>
                    <div className="w-1/3 h-2 bg-gray-800 rounded-full overflow-hidden">
                        <motion.div
                            initial={{ width: 0 }}
                            animate={{ width: `${((currentQIndex + 1) / totalQuestions) * 100}%` }}
                            className="h-full bg-gradient-to-r from-purple-500 to-pink-500"
                        />
                    </div>
                </div>

                <AnimatePresence mode='wait'>
                    <motion.div
                        key={currentQuestion.id}
                        initial={{ x: 20, opacity: 0 }}
                        animate={{ x: 0, opacity: 1 }}
                        exit={{ x: -20, opacity: 0 }}
                        className="bg-gray-900 p-8 rounded-2xl border border-gray-800 shadow-2xl"
                    >
                        <h2 className="text-2xl font-bold mb-8">{currentQuestion.questionTitle}</h2>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 xl:gap-6">
                            {[currentQuestion.option1, currentQuestion.option2, currentQuestion.option3, currentQuestion.option4].map((option, idx) => (
                                <button
                                    key={idx}
                                    onClick={() => handleOptionSelect(option)}
                                    disabled={optionsLocked}
                                    className={`p-4 rounded-xl text-left transition-all border flex items-center justify-between gap-3 ${getOptionStyle(option)} ${optionsLocked ? 'cursor-default' : 'cursor-pointer'}`}
                                >
                                    <span>
                                        <span className="font-bold mr-2">{String.fromCharCode(65 + idx)}.</span> {option}
                                    </span>
                                    {getOptionIcon(option)}
                                </button>
                            ))}
                        </div>

                        {/* Verification Result Banner */}
                        <AnimatePresence>
                            {verificationResult && (
                                <motion.div
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: -10 }}
                                    className={`mt-6 p-4 rounded-xl border flex items-center gap-3 ${
                                        verificationResult.correct
                                            ? 'bg-emerald-900/30 border-emerald-700 text-emerald-300'
                                            : 'bg-red-900/30 border-red-700 text-red-300'
                                    }`}
                                >
                                    {verificationResult.correct ? (
                                        <>
                                            <CheckCircle size={24} />
                                            <span className="font-semibold text-lg">Correct! Well done! 🎉</span>
                                        </>
                                    ) : (
                                        <>
                                            <XCircle size={24} />
                                            <span className="font-semibold text-lg">
                                                Incorrect! The correct answer is: <strong className="text-white">{verificationResult.correctAnswer}</strong>
                                            </span>
                                        </>
                                    )}
                                </motion.div>
                            )}
                        </AnimatePresence>

                        {/* AI Remedial Explanation Section */}
                        <AnimatePresence>
                            {verificationResult && !verificationResult.correct && (
                                <motion.div
                                    initial={{ opacity: 0, height: 0 }}
                                    animate={{ opacity: 1, height: 'auto' }}
                                    exit={{ opacity: 0, height: 0 }}
                                    className="mt-4 overflow-hidden"
                                >
                                    <div className="bg-gradient-to-br from-gray-800/80 to-gray-900/80 border border-purple-800/40 rounded-xl p-6">
                                        <div className="flex items-center gap-2 mb-4 text-purple-400">
                                            <BookOpen size={20} />
                                            <span className="font-bold text-sm uppercase tracking-wider">AI Tutor Explanation</span>
                                        </div>

                                        {isLoadingExplanation ? (
                                            <div className="flex items-center gap-3 text-gray-400 py-4">
                                                <Loader2 className="animate-spin" size={20} />
                                                <span>Searching the web and generating explanation...</span>
                                            </div>
                                        ) : explanation ? (
                                            <div className="prose prose-invert prose-sm max-w-none
                                                prose-headings:text-purple-300 prose-headings:font-bold
                                                prose-strong:text-white
                                                prose-p:text-gray-300 prose-p:leading-relaxed
                                                prose-li:text-gray-300
                                                prose-code:text-pink-300 prose-code:bg-gray-800 prose-code:px-1 prose-code:rounded">
                                                <ReactMarkdown>{explanation}</ReactMarkdown>
                                            </div>
                                        ) : null}
                                    </div>
                                </motion.div>
                            )}
                        </AnimatePresence>

                        {/* Next / Submit Button */}
                        <div className="mt-8 flex justify-end">
                            <button
                                onClick={handleNext}
                                disabled={!verificationResult && !responses[currentQIndex]?.response}
                                className={`px-8 py-3 font-bold rounded-lg transition-colors flex items-center ${
                                    !verificationResult && !responses[currentQIndex]?.response
                                        ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                                        : 'bg-white text-black hover:bg-gray-200'
                                }`}
                            >
                                {currentQIndex === totalQuestions - 1 ? 'Submit' : 'Next'} <ArrowRight className="ml-2" size={18} />
                            </button>
                        </div>
                    </motion.div>
                </AnimatePresence>
            </div>
        </div>
    );
};

const TrophyIcon = () => (
    <div className="flex justify-center mb-6">
        <div className="relative">
            <div className="absolute inset-0 bg-yellow-400 blur-xl opacity-20 rounded-full"></div>
            <svg
                className="w-24 h-24 text-yellow-400 relative z-10"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
            >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v13m0-13V6a2 2 0 112 2h-2zm0 0V5.5A2.5 2.5 0 109.5 8H12zm-7 4h14M5 12a2 2 0 110-4h14a2 2 0 110 4M5 12v7a2 2 0 002 2h10a2 2 0 002-2v-7" />
            </svg>
        </div>
    </div>
);

export default QuizRoom;
