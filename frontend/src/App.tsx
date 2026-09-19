import type { ReactNode } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from '@/app/router/ProtectedRoute'
import { RequireRole } from '@/app/router/RequireRole'
import { homePathForRole } from '@/lib/roles'
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
import { MyBusinessPage } from '@/pages/business/MyBusinessPage'
import { MyHotelsPage } from '@/pages/business/MyHotelsPage'

import { AdminDashboardPage } from '@/pages/admin/AdminDashboardPage'
import { AdminUsersPage } from '@/pages/admin/AdminUsersPage'
import { AdminBusinessesPage } from '@/pages/admin/AdminBusinessesPage'
import { AdminHotelsPage } from '@/pages/admin/AdminHotelsPage'

// Business accounts land on My Tours and admins on the analytics dashboard
// (see lib/roles.ts).
function HomeRedirect() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const user = useAuthStore((state) => state.user)
  if (!isAuthenticated) return <HomePage />
  return <Navigate to={homePathForRole(user?.role)} replace />
}

// Unknown /admin/... paths: send a signed-in user to their own home instead of
// a 404.
function RoleHomeRedirect() {
  const user = useAuthStore((state) => state.user)
  return <Navigate to={homePathForRole(user?.role)} replace />
}

// Booking, paying and the tourist dashboard are for tourists only -- the
// booking/payment services enforce the same rule server-side.
function TouristOnly({ children }: { children: ReactNode }) {
  return (
    <ProtectedRoute>
      <RequireRole roles={['TOURIST']}>{children}</RequireRole>
    </ProtectedRoute>
  )
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
          <TouristOnly>
            <DashboardPage />
          </TouristOnly>
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
          <TouristOnly>
            <MyBookingsPage />
          </TouristOnly>
        }
      />
      <Route
        path="/bookings/:id"
        element={
          <TouristOnly>
            <BookingDetailPage />
          </TouristOnly>
        }
      />
      <Route
        path="/bookings/:id/pay"
        element={
          <TouristOnly>
            <BookingPaymentPage />
          </TouristOnly>
        }
      />
      <Route
        path="/bookings/:id/confirmation"
        element={
          <TouristOnly>
            <BookingConfirmationPage />
          </TouristOnly>
        }
      />

      {/* Business */}
      <Route
        path="/tours/new"
        element={
          <ProtectedRoute>
            <RequireRole roles={['BUSINESS']}>
              <CreateTourPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />
      <Route
        path="/my-tours"
        element={
          <ProtectedRoute>
            <RequireRole roles={['BUSINESS']}>
              <MyToursPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />

      <Route
        path="/business"
        element={
          <ProtectedRoute>
            <RequireRole roles={['BUSINESS']}>
              <MyBusinessPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />
      <Route
        path="/hotels"
        element={
          <ProtectedRoute>
            <RequireRole roles={['BUSINESS']}>
              <MyHotelsPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />

      {/* Admin */}
      <Route
        path="/admin/users"
        element={
          <ProtectedRoute>
            <RequireRole roles={['ADMIN']}>
              <AdminUsersPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/businesses"
        element={
          <ProtectedRoute>
            <RequireRole roles={['ADMIN']}>
              <AdminBusinessesPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/hotels"
        element={
          <ProtectedRoute>
            <RequireRole roles={['ADMIN']}>
              <AdminHotelsPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin"
        element={
          <ProtectedRoute>
            <RequireRole roles={['ADMIN']}>
              <AdminDashboardPage />
            </RequireRole>
          </ProtectedRoute>
        }
      />

      {/* Unknown /admin/... paths: back to the user's own home */}
      <Route
        path="/admin/*"
        element={
          <ProtectedRoute>
            <RoleHomeRedirect />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
