import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import { useAuth } from './context/AuthContext'

import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Incidents from './pages/Incidents'
import NewIncident from './pages/NewIncident'
import IncidentDetail from './pages/IncidentDetail'
import Knowledge from './pages/Knowledge'
import Predictions from './pages/Predictions'
import Security from './pages/Security'

function HomeRedirect() {
  const { user, isLoading } = useAuth()
  if (isLoading) return null
  return <Navigate to={user ? '/dashboard' : '/login'} replace />
}

export default function App() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <Routes>
        <Route path="/" element={<HomeRedirect />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/incidents" element={<ProtectedRoute><Incidents /></ProtectedRoute>} />
        <Route path="/incidents/new" element={<ProtectedRoute><NewIncident /></ProtectedRoute>} />
        <Route path="/incidents/:id" element={<ProtectedRoute><IncidentDetail /></ProtectedRoute>} />
        <Route path="/knowledge" element={<ProtectedRoute><Knowledge /></ProtectedRoute>} />
        <Route path="/predictions" element={<ProtectedRoute><Predictions /></ProtectedRoute>} />
        <Route path="/security" element={<ProtectedRoute><Security /></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  )
}
