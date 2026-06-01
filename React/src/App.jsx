import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import ProtectedRoute from './components/ProtectedRoute';

import Login from './pages/Login';
import Register from './pages/Register';
import LandingPage from './pages/LandingPage';
import OAuth2RedirectHandler from './components/OAuth2RedirectHandler';
import DashboardLayout from './components/DashboardLayout';
import DashboardOverview from './pages/admin/DashboardOverview';
import QuestionManagement from './pages/admin/QuestionManagement';
import CreateQuiz from './pages/admin/CreateQuiz';
import QuizRoom from './pages/QuizRoom';
import Home from './pages/Home';

function App() {
  return (
    <Router>
      <ToastProvider>
        <AuthProvider>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />


            {/* Protected Routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="/home" element={<Home />} />
              <Route path="/quiz/:id" element={<QuizRoom />} />
              <Route path="/dashboard" element={<DashboardLayout />}>
                <Route index element={<DashboardOverview />} />
                <Route path="questions" element={<QuestionManagement />} />
                <Route path="create-quiz" element={<CreateQuiz />} />
              </Route>
            </Route>

            {/* Catch all - Redirect to Landing */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </ToastProvider>

    </Router>
  );
}

export default App;

