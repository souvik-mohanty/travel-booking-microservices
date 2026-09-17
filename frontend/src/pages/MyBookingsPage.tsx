import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { cancelBooking, getMyBookings } from '../api/bookings'
import { AppLayout } from '../components/AppLayout'
import { StatusBadge } from '../components/StatusBadge'
import { TripPanel } from '../components/TripPanel'
import { formatDate, formatMoney } from '../lib/format'
import type { Booking } from '../types/booking'

export function MyBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actioningId, setActioningId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const mine = await getMyBookings()
      setBookings(mine.sort((a, b) => b.createdAt.localeCompare(a.createdAt)))
    } catch {
      setError('Could not load your bookings. Is booking-service running?')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function handleCancel(id: string) {
    setActioningId(id)
    try {
      const updated = await cancelBooking(id)
      setBookings((prev) => prev.map((b) => (b.id === id ? updated : b)))
    } catch {
      setError('Could not cancel that booking.')
    } finally {
      setActioningId(null)
    }
  }

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">My Bookings</h1>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="mb-4 text-sm text-red-600 dark:text-red-400">{error}</p>}

      {!loading && bookings.length === 0 && !error && (
        <p className="text-sm text-slate-500 dark:text-slate-400">
          No bookings yet.{' '}
          <Link to="/tours" className="font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400">
            Browse tours
          </Link>{' '}
          to make one.
        </p>
      )}

      <div className="space-y-3">
        {bookings.map((booking) => (
          <div
            key={booking.id}
            className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
          >
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <Link
                    to={`/tours/${booking.tourId}`}
                    className="font-medium text-slate-900 hover:underline dark:text-slate-100"
                  >
                    Tour booking
                  </Link>
                  <StatusBadge status={booking.status} />
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  {booking.numberOfParticipants} participant(s) · {formatMoney(booking.totalPrice)} · booked{' '}
                  {formatDate(booking.createdAt)}
                </p>
              </div>

              {(booking.status === 'PENDING' || booking.status === 'PAID') && (
                <button
                  onClick={() => handleCancel(booking.id)}
                  disabled={actioningId === booking.id}
                  className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 disabled:opacity-50 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                >
                  Cancel
                </button>
              )}
            </div>

            {(booking.status === 'PAID' || booking.status === 'COMPLETED') && (
              <TripPanel bookingId={booking.id} role="tourist" />
            )}
          </div>
        ))}
      </div>
    </AppLayout>
  )
}
