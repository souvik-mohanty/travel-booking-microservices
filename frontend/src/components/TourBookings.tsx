import { useEffect, useState } from 'react'
import { getBookingsForTour } from '../api/bookings'
import { StatusBadge } from './StatusBadge'
import { TripPanel } from './TripPanel'
import type { Booking } from '../types/booking'
import { formatDate, formatMoney } from '../lib/format'

// Business-side view of who booked a tour, with trip/logbook access per
// booking -- see TripPanel role="business".
export function TourBookings({ tourId }: { tourId: string }) {
  const [bookings, setBookings] = useState<Booking[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    getBookingsForTour(tourId)
      .then(setBookings)
      .catch(() => setError('Could not load bookings for this tour.'))
      .finally(() => setLoading(false))
  }, [tourId])

  return (
    <div className="mt-3 border-t border-slate-200 pt-3 dark:border-slate-800">
      <h3 className="mb-2 text-sm font-semibold text-slate-700 dark:text-slate-300">Bookings</h3>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

      {!loading && !error && bookings.length === 0 && (
        <p className="text-sm text-slate-500 dark:text-slate-400">No one has booked this tour yet.</p>
      )}

      <div className="space-y-2">
        {bookings.map((booking) => (
          <div key={booking.id} className="rounded-md border border-slate-200 p-3 dark:border-slate-800">
            <div className="flex items-center gap-2">
              <StatusBadge status={booking.status} />
              <span className="text-sm text-slate-700 dark:text-slate-300">
                {booking.numberOfParticipants} participant(s) · {formatMoney(booking.totalPrice)} · booked{' '}
                {formatDate(booking.createdAt)}
              </span>
            </div>

            {(booking.status === 'PAID' || booking.status === 'COMPLETED') && (
              <TripPanel bookingId={booking.id} role="business" />
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
