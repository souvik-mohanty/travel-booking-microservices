import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from '@/app/router/ProtectedRoute'
import { RequireRole } from '@/app/router/RequireRole'
import { useAuthStore } from '@/store/authStore'

import { HomePage } from '@/pages/public/HomePage'
import { AboutPage } from '@/pages/public/AboutPage'
import { ContactPage } from '@/pages/public/ContactPage'
import { LoginPage } from '@/pages/public/LoginPage'
import { RegisterPage } from '@/pages/public/RegisterPage'
import { OAuth2RedirectPage } from '@/pages/public/OAuth2RedirectPage'
import { UnauthorizedPage } from '@/pages/public/UnauthorizedPage'
import { NotFoundPage } from '@/pages/public/NotFoundPage'

import { DashboardPage } from '@/pages/tourist/DashboardPage'
import { ExploreToursPage } from '@/pages/tourist/ExploreToursPage'
import { DestinationsPage } from '@/pages/tourist/DestinationsPage'
import { TourDetailPage } from '@/pages/tourist/TourDetailPage'
import { BookingPaymentPage } from '@/pages/tourist/BookingPaymentPage'
import { BookingConfirmationPage } from '@/pages/tourist/BookingConfirmationPage'
import { BookingDetailPage } from '@/pages/tourist/BookingDetailPage'
import { MyBookingsPage } from '@/pages/tourist/MyBookingsPage'

import { CreateTourPage } from '@/pages/business/CreateTourPage'
import { MyToursPage } from '@/pages/business/MyToursPage'

import { AdminDashboardPage } from '@/pages/admin/AdminDashboardPage'

// NOTE: My Business / My Hotels (business portal) and the admin
// customer/business/hotel management tables are not yet ported to this
// architecture -- see plan Phase 5/7. Only the admin analytics dashboard is
// wired up for /admin today.
function HomeRedirect() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const user = useAuthStore((state) => state.user)
  if (!isAuthenticated) return <HomePage />
  return <Navigate to={user?.role === 'ADMIN' ? '/admin' : '/tours'} replace />
}

function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/about" element={<AboutPage />} />
      <Route path="/contact" element={<ContactPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/oauth2/redirect" element={<OAuth2RedirectPage />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      {/* Tourist + shared (any authenticated user) */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/tours"
        element={
          <ProtectedRoute>
            <ExploreToursPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/destinations"
        element={
          <ProtectedRoute>
            <DestinationsPage />
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
        path="/bookings"
        element={
          <ProtectedRoute>
            <MyBookingsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/bookings/:id"
        element={
          <ProtectedRoute>
            <BookingDetailPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/bookings/:id/pay"
        element={
          <ProtectedRoute>
            <BookingPaymentPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/bookings/:id/confirmation"
        element={
          <ProtectedRoute>
            <BookingConfirmationPage />
          </ProtectedRoute>
        }
      />

      {/* Business */}
      <Route
        path="/tours/new"
        element={
          <ProtectedRoute>
            <RequireRole role="BUSINESS">
              <CreateTourPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />
      <Route
        path="/my-tours"
        element={
          <ProtectedRoute>
            <RequireRole role="BUSINESS">
              <MyToursPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />

      {/* Admin */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute>
            <RequireRole role="ADMIN">
              <AdminDashboardPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
