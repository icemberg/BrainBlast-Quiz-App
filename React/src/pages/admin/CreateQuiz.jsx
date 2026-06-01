import React, { useState } from 'react';
import { TextField, Button, MenuItem, Paper, Typography } from '@mui/material';
import api from '../../api/axios';
import { useToast } from '../../context/ToastContext';

import { motion } from 'framer-motion';
import { Sparkles, ArrowRight } from 'lucide-react';

const CreateQuiz = () => {
    const [title, setTitle] = useState('');
    const [category, setCategory] = useState('');
    const [numQ, setNumQ] = useState(5);
    const { showToast } = useToast();


    const handleCreate = async () => {
        try {
            // Need to pass query params correctly
            await api.post(`/quiz/createQuiz?category=${category}&numQ=${numQ}&title=${title}`);
            showToast('Quiz Created Successfully! Students can now take this quiz.', 'success');
        } catch (error) {
            showToast('Error creating quiz. Please check if enough questions exist for this category.', 'error');
            console.error(error);
        }

    };

    return (
        <div className="max-w-2xl mx-auto">
            <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="bg-gray-800 p-8 rounded-2xl border border-gray-700 shadow-2xl"
            >
                <div className="flex items-center mb-6">
                    <div className="p-3 bg-purple-500/20 rounded-lg mr-4">
                        <Sparkles className="text-purple-400" size={32} />
                    </div>
                    <div>
                        <h2 className="text-3xl font-bold text-white">Create New Quiz</h2>
                        <p className="text-gray-400">Generate a random quiz from the question bank.</p>
                    </div>
                </div>

                <div className="space-y-6">
                    <TextField
                        label="Quiz Title"
                        fullWidth
                        variant="filled"
                        className="bg-gray-700/50 rounded-t-lg"
                        sx={{ input: { color: 'white' }, label: { color: 'gray' } }}
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                    />

                    <div className="grid grid-cols-2 gap-6">
                        <TextField
                            label="Category"
                            fullWidth
                            variant="filled"
                            className="bg-gray-700/50 rounded-t-lg"
                            sx={{ input: { color: 'white' }, label: { color: 'gray' } }}
                            value={category}
                            onChange={(e) => setCategory(e.target.value)}
                        />
                        <TextField
                            label="Number of Questions"
                            type="number"
                            fullWidth
                            variant="filled"
                            className="bg-gray-700/50 rounded-t-lg"
                            sx={{ input: { color: 'white' }, label: { color: 'gray' } }}
                            value={numQ}
                            onChange={(e) => setNumQ(e.target.value)}
                        />
                    </div>

                    <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                        <Button
                            variant="contained"
                            fullWidth
                            size="large"
                            onClick={handleCreate}
                            endIcon={<ArrowRight />}
                            sx={{
                                background: 'linear-gradient(45deg, #7c3aed 30%, #ec4899 90%)',
                                height: 56,
                                fontSize: '1.1rem'
                            }}
                        >
                            Generate Quiz
                        </Button>
                    </motion.div>


                </div>
            </motion.div>
        </div>
    );
};

export default CreateQuiz;
