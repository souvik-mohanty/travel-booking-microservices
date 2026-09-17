import { useEffect, useState, type FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getTour } from '../api/tours'
import { createBooking } from '../api/bookings'
import { AppLayout } from '../components/AppLayout'
import { StatusBadge } from '../components/StatusBadge'
import { TourActivities } from '../components/TourActivities'
import { TourItinerary } from '../components/TourItinerary'
import { useAuth } from '../context/AuthContext'
import { formatDateRange, formatMoney } from '../lib/format'
import type { Tour } from '../types/tour'

export function TourDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [tour, setTour] = useState<Tour | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [participants, setParticipants] = useState(1)
  const [booking, setBooking] = useState(false)
  const [bookingError, setBookingError] = useState<string | null>(null)
  const [bookingDone, setBookingDone] = useState(false)

  useEffect(() => {
    if (!id) return
    getTour(id)
      .then(setTour)
      .catch(() => setError('Tour not found.'))
      .finally(() => setLoading(false))
  }, [id])

  async function handleBook(event: FormEvent) {
    event.preventDefault()
    if (!tour) return

    setBooking(true)
    setBookingError(null)
    try {
      await createBooking({ tourId: tour.id, numberOfParticipants: participants })
      setBookingDone(true)
    } catch (err) {
      setBookingError(
        isAxiosError(err) && err.response?.data?.message
          ? String(err.response.data.message)
          : 'Could not create the booking. Please try again.',
      )
    } finally {
      setBooking(false)
    }
  }

  if (loading) {
    return (
      <AppLayout>
        <p className="text-sm text-slate-500 dark:text-slate-400">Loading tour…</p>
      </AppLayout>
    )
  }

  if (error || !tour) {
    return (
      <AppLayout>
        <p className="text-sm text-red-600 dark:text-red-400">{error ?? 'Tour not found.'}</p>
        <Link to="/tours" className="mt-4 inline-block text-sm underline">
          Back to tours
        </Link>
      </AppLayout>
    )
  }

  const isOwner = user?.userId === tour.createdBy
  const canBook = tour.status === 'PUBLISHED' && !isOwner

  return (
    <AppLayout>
      <Link to="/tours" className="text-sm text-slate-500 underline dark:text-slate-400">
        ← Back to tours
      </Link>

      <div className="mt-4 grid grid-cols-1 gap-8 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <div className="mb-2 flex items-center gap-3">
            <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">{tour.title}</h1>
            <StatusBadge status={tour.status} />
          </div>
          <p className="text-slate-500 dark:text-slate-400">{tour.destination}</p>
          <p className="mt-1 text-sm text-slate-400 dark:text-slate-500">
            {formatDateRange(tour.startDate, tour.endDate)} · Up to {tour.maxParticipants} participants
          </p>
          {tour.description && (
            <p className="mt-6 whitespace-pre-line text-slate-700 dark:text-slate-300">{tour.description}</p>
          )}
        </div>

        <div className="rounded-lg border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
          <p className="text-2xl font-semibold text-slate-900 dark:text-slate-100">{formatMoney(tour.price)}</p>
          <p className="text-xs text-slate-400 dark:text-slate-500">per participant</p>

          {isOwner && (
            <p className="mt-4 text-sm text-slate-500 dark:text-slate-400">
              This is your tour. Manage it from{' '}
              <Link to="/my-tours" className="font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400">
                My Tours
              </Link>
              .
            </p>
          )}

          {!isOwner && tour.status !== 'PUBLISHED' && (
            <p className="mt-4 text-sm text-slate-500 dark:text-slate-400">
              This tour isn&apos;t open for booking right now.
            </p>
          )}

          {canBook && !bookingDone && (
            <form onSubmit={handleBook} className="mt-4">
              <label className="mb-3 block text-sm font-medium text-slate-700 dark:text-slate-300">
                Participants
                <input
                  type="number"
                  min={1}
                  max={tour.maxParticipants}
                  value={participants}
                  onChange={(e) => setParticipants(Number(e.target.value))}
                  className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                />
              </label>

              <p className="mb-3 text-sm text-slate-500 dark:text-slate-400">
                Total: {formatMoney(tour.price * participants)}
              </p>

              {bookingError && <p className="mb-3 text-sm text-red-600 dark:text-red-400">{bookingError}</p>}

              <button
                type="submit"
                disabled={booking}
                className="w-full rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
              >
                {booking ? 'Booking…' : 'Book now'}
              </button>
            </form>
          )}

          {bookingDone && (
            <div className="mt-4 rounded-md bg-emerald-50 p-3 text-sm text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300">
              Booking created. It starts as <strong>PENDING</strong> until payment is processed.{' '}
              <button onClick={() => navigate('/bookings')} className="font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400">
                View my bookings
              </button>
            </div>
          )}
        </div>
      </div>

      <TourItinerary tourId={tour.id} tourDestination={tour.destination} isOwner={isOwner} />
      <TourActivities
        tourId={tour.id}
        tourStartDate={tour.startDate}
        tourEndDate={tour.endDate}
        isOwner={isOwner}
      />
    </AppLayout>
  )
}
