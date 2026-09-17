import { useEffect, useState } from 'react'
import { getBookingsForUser } from '../api/bookings'
import { getAllBusinesses } from '../api/business'
import { getAllHotels } from '../api/hotels'
import { getAllTours } from '../api/tours'
import { StatusBadge } from './StatusBadge'
import { TripPanel } from './TripPanel'
import type { AdminUser } from '../types/adminUser'
import type { Booking } from '../types/booking'
import type { Business } from '../types/business'
import type { Hotel } from '../types/hotel'
import type { Tour } from '../types/tour'
import { formatDate, formatMoney } from '../lib/format'

// Admin "full visibility" into one account: bookings for a tourist,
// businesses/hotels/tours owned for a business.
export function AdminUserDetail({ user }: { user: AdminUser }) {
  const [bookings, setBookings] = useState<Booking[] | null>(null)
  const [businesses, setBusinesses] = useState<Business[] | null>(null)
  const [hotels, setHotels] = useState<Hotel[] | null>(null)
  const [tours, setTours] = useState<Tour[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setError(null);
    (async () => {
      try {
        if (user.role === 'TOURIST') {
          setBookings(await getBookingsForUser(user.id))
        } else if (user.role === 'BUSINESS') {
          const [allBusinesses, allHotels, allTours] = await Promise.all([
            getAllBusinesses(),
            getAllHotels(),
            getAllTours(),
          ])
          setBusinesses(allBusinesses.filter((b) => b.ownerId === user.id))
          setHotels(allHotels.filter((h) => h.ownerId === user.id))
          setTours(allTours.filter((t) => t.createdBy === user.id))
        }
      } catch {
        setError('Could not load full details for this account.')
      }
    })()
  }, [user.id, user.role])

  if (user.role === 'ADMIN') {
    return <p className="text-sm text-slate-500 dark:text-slate-400">No additional detail for admin accounts.</p>
  }

  return (
    <div className="space-y-4">
      {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

      {user.role === 'TOURIST' && (
        <div>
          <h4 className="mb-2 text-sm font-semibold text-slate-700 dark:text-slate-300">Bookings</h4>
          {bookings === null && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
          {bookings !== null && bookings.length === 0 && (
            <p className="text-sm text-slate-500 dark:text-slate-400">No bookings.</p>
          )}
          <ul className="space-y-2">
            {bookings?.map((booking) => (
              <li key={booking.id} className="text-sm">
                <div className="flex items-center gap-2">
                  <StatusBadge status={booking.status} />
                  <span className="text-slate-700 dark:text-slate-300">
                    {booking.numberOfParticipants} participant(s) · {formatMoney(booking.totalPrice)} · booked{' '}
                    {formatDate(booking.createdAt)}
                  </span>
                </div>
                {(booking.status === 'PAID' || booking.status === 'COMPLETED') && (
                  <TripPanel bookingId={booking.id} role="admin" />
                )}
              </li>
            ))}
          </ul>
        </div>
      )}

      {user.role === 'BUSINESS' && (
        <>
          <div>
            <h4 className="mb-2 text-sm font-semibold text-slate-700 dark:text-slate-300">Businesses</h4>
            {businesses === null && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
            {businesses !== null && businesses.length === 0 && (
              <p className="text-sm text-slate-500 dark:text-slate-400">None.</p>
            )}
            <ul className="space-y-1">
              {businesses?.map((business) => (
                <li key={business.id} className="flex items-center gap-2 text-sm">
                  <span className="text-slate-900 dark:text-slate-100">{business.name}</span>
                  <span className="text-slate-400 dark:text-slate-500">({business.status})</span>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="mb-2 text-sm font-semibold text-slate-700 dark:text-slate-300">Hotels</h4>
            {hotels === null && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
            {hotels !== null && hotels.length === 0 && (
              <p className="text-sm text-slate-500 dark:text-slate-400">None.</p>
            )}
            <ul className="space-y-1">
              {hotels?.map((hotel) => (
                <li key={hotel.id} className="flex items-center gap-2 text-sm">
                  <span className="text-slate-900 dark:text-slate-100">{hotel.name}</span>
                  <span className="text-slate-400 dark:text-slate-500">({hotel.status})</span>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="mb-2 text-sm font-semibold text-slate-700 dark:text-slate-300">Tours</h4>
            {tours === null && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
            {tours !== null && tours.length === 0 && (
              <p className="text-sm text-slate-500 dark:text-slate-400">None.</p>
            )}
            <ul className="space-y-1">
              {tours?.map((tour) => (
                <li key={tour.id} className="flex items-center gap-2 text-sm">
                  <StatusBadge status={tour.status} />
                  <span className="text-slate-900 dark:text-slate-100">{tour.title}</span>
                </li>
              ))}
            </ul>
          </div>
        </>
      )}
    </div>
  )
}
