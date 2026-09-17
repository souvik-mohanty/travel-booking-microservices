import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import { ProtectedRoute } from './routes/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { OAuth2RedirectPage } from './pages/OAuth2RedirectPage'
import { ToursPage } from './pages/ToursPage'
import { TourDetailPage } from './pages/TourDetailPage'
import { CreateTourPage } from './pages/CreateTourPage'
import { MyToursPage } from './pages/MyToursPage'
import { MyBookingsPage } from './pages/MyBookingsPage'
import { NotFoundPage } from './pages/NotFoundPage'

function HomeRedirect() {
  const { isAuthenticated } = useAuth()
  return <Navigate to={isAuthenticated ? '/tours' : '/login'} replace />
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/oauth2/redirect" element={<OAuth2RedirectPage />} />

      <Route
        path="/tours"
        element={
          <ProtectedRoute>
            <ToursPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/tours/new"
        element={
          <ProtectedRoute>
            <CreateTourPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/tours/:id"
        element={
          <ProtectedRoute>
            <TourDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/my-tours"
        element={
          <ProtectedRoute>
            <MyToursPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/bookings"
        element={
          <ProtectedRoute>
            <MyBookingsPage />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
