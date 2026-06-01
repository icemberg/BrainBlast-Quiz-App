import React, { useState, useEffect } from 'react';
import { DataGrid } from '@mui/x-data-grid';
import { Button, Dialog, DialogTitle, DialogContent, TextField, DialogActions } from '@mui/material';
import { Plus, Edit, Trash } from 'lucide-react';
import api from '../../api/axios';
import { motion } from 'framer-motion';
import { useToast } from '../../context/ToastContext';


const QuestionManagement = () => {
    const [questions, setQuestions] = useState([]);
    const { showToast } = useToast();

    const [open, setOpen] = useState(false);
    const [formData, setFormData] = useState({
        questionTitle: '',
        option1: '',
        option2: '',
        option3: '',
        option4: '',
        rightAnswer: '',
        category: '',
        difficultylevel: 'Medium'
    });
    const [isEdit, setIsEdit] = useState(false);
    const [selectedId, setSelectedId] = useState(null);

    useEffect(() => {
        fetchQuestions();
    }, []);

    const fetchQuestions = async () => {
        try {
            const response = await api.get('/question/allQuestions');
            setQuestions(response.data);
        } catch (error) {
            console.error("Error fetching questions", error);
        }
    };

    const handleOpen = () => {
        setFormData({
            questionTitle: '',
            option1: '',
            option2: '',
            option3: '',
            option4: '',
            rightAnswer: '',
            category: '',
            difficultylevel: 'Medium'
        });
        setIsEdit(false);
        setOpen(true);
    };

    const handleEdit = (row) => {
        setFormData(row);
        setIsEdit(true);
        setSelectedId(row.id);
        setOpen(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm("Are you sure you want to delete this question?")) {
            try {
                await api.delete(`/question/deleteQuestion/${id}`);
                fetchQuestions();
                showToast('Question deleted successfully', 'success');
            } catch (error) {
                console.error("Error deleting question", error);
                showToast('Failed to delete question', 'error');
            }

        }
    };

    const handleSave = async () => {
        try {
            if (isEdit) {
                await api.put(`/question/updateQuestion/${selectedId}`, formData);
            } else {
                await api.post('/question/addQuestion', formData);
            }
            setOpen(false);
            fetchQuestions();
            showToast(isEdit ? 'Question updated successfully' : 'Question added successfully', 'success');
        } catch (error) {
            console.error("Error saving question", error);
            showToast('Failed to save question', 'error');
        }

    };

    const columns = [
        { field: 'id', headerName: 'ID', width: 70 },
        { field: 'questionTitle', headerName: 'Question', width: 300 },
        { field: 'category', headerName: 'Category', width: 130 },
        { field: 'difficultylevel', headerName: 'Difficulty', width: 130 },
        {
            field: 'actions',
            headerName: 'Actions',
            width: 150,
            renderCell: (params) => (
                <div className="flex space-x-2 mt-2">
                    <button onClick={() => handleEdit(params.row)} className="text-blue-400 hover:text-blue-300">
                        <Edit size={18} />
                    </button>
                    <button onClick={() => handleDelete(params.row.id)} className="text-red-400 hover:text-red-300">
                        <Trash size={18} />
                    </button>
                </div>
            )
        }
    ];

    return (
        <div className="text-gray-900">
            <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold text-white">Question Bank</h2>
                <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                    <Button
                        variant="contained"
                        startIcon={<Plus />}
                        onClick={handleOpen}
                        sx={{ background: 'linear-gradient(45deg, #7c3aed 30%, #c026d3 90%)' }}
                    >
                        Add Question
                    </Button>
                </motion.div>
            </div>

            <div style={{ height: 600, width: '100%' }} className="bg-white rounded-lg p-4 shadow-xl border border-gray-200">
                <DataGrid
                    rows={questions}
                    columns={columns}
                    pageSize={10}
                    rowsPerPageOptions={[10]}
                    checkboxSelection={false}
                    sx={{
                        border: 0,
                        color: 'black',
                        '& .MuiDataGrid-cell': {
                            borderColor: 'rgba(0,0,0,0.1)'
                        },
                        '& .MuiDataGrid-columnHeaders': {
                            borderColor: 'rgba(0,0,0,0.1)',
                            backgroundColor: 'rgba(0,0,0,0.05)',
                            color: 'black'
                        },
                        '& .MuiDataGrid-footerContainer': {
                            borderColor: 'rgba(0,0,0,0.1)'
                        },
                        '& .MuiTablePagination-root': {
                            color: 'black'
                        }
                    }}
                />
            </div>


            <Dialog open={open} onClose={() => setOpen(false)} maxWidth="md" fullWidth>
                <DialogTitle>{isEdit ? 'Edit Question' : 'Add New Question'}</DialogTitle>
                <DialogContent>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-2">
                        <TextField
                            label="Question Title"
                            fullWidth
                            variant="outlined"
                            className="col-span-2"
                            value={formData.questionTitle}
                            onChange={(e) => setFormData({ ...formData, questionTitle: e.target.value })}
                        />
                        <TextField
                            label="Category"
                            fullWidth
                            value={formData.category}
                            onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                        />
                        <TextField
                            label="Difficulty"
                            fullWidth
                            value={formData.difficultylevel}
                            onChange={(e) => setFormData({ ...formData, difficultylevel: e.target.value })}
                        />
                        <TextField
                            label="Option 1"
                            fullWidth
                            value={formData.option1}
                            onChange={(e) => setFormData({ ...formData, option1: e.target.value })}
                        />
                        <TextField
                            label="Option 2"
                            fullWidth
                            value={formData.option2}
                            onChange={(e) => setFormData({ ...formData, option2: e.target.value })}
                        />
                        <TextField
                            label="Option 3"
                            fullWidth
                            value={formData.option3}
                            onChange={(e) => setFormData({ ...formData, option3: e.target.value })}
                        />
                        <TextField
                            label="Option 4"
                            fullWidth
                            value={formData.option4}
                            onChange={(e) => setFormData({ ...formData, option4: e.target.value })}
                        />
                        <TextField
                            label="Right Answer"
                            fullWidth
                            className="col-span-2"
                            value={formData.rightAnswer}
                            onChange={(e) => setFormData({ ...formData, rightAnswer: e.target.value })}
                        />
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpen(false)}>Cancel</Button>
                    <Button onClick={handleSave} variant="contained" color="primary">Save</Button>
                </DialogActions>
            </Dialog>
        </div>
    );
};

export default QuestionManagement;
