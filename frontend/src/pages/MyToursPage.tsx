import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { cancelTour, getAllTours, publishTour } from '../api/tours'
import { AppLayout } from '../components/AppLayout'
import { StatusBadge } from '../components/StatusBadge'
import { TourBookings } from '../components/TourBookings'
import { useAuth } from '../context/AuthContext'
import { formatDateRange, formatMoney } from '../lib/format'
import type { Tour } from '../types/tour'

export function MyToursPage() {
  const { user } = useAuth()
  const [tours, setTours] = useState<Tour[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actioningId, setActioningId] = useState<string | null>(null)
  const [expandedId, setExpandedId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      // tour-service has no "my tours" endpoint yet -- GET /api/tours returns
      // every tour system-wide, so this filters client-side.
      const all = await getAllTours()
      setTours(all.filter((tour) => tour.createdBy === user?.userId))
    } catch {
      setError('Could not load your tours. Is tour-service running?')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.userId])

  async function handlePublish(id: string) {
    setActioningId(id)
    try {
      const updated = await publishTour(id)
      setTours((prev) => prev.map((t) => (t.id === id ? updated : t)))
    } catch {
      setError('Could not publish that tour.')
    } finally {
      setActioningId(null)
    }
  }

  async function handleCancel(id: string) {
    setActioningId(id)
    try {
      const updated = await cancelTour(id)
      setTours((prev) => prev.map((t) => (t.id === id ? updated : t)))
    } catch {
      setError('Could not cancel that tour.')
    } finally {
      setActioningId(null)
    }
  }

  return (
    <AppLayout>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">My Tours</h1>
        <Link
          to="/tours/new"
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 dark:bg-blue-500 dark:text-white"
        >
          Create a tour
        </Link>
      </div>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="mb-4 text-sm text-red-600 dark:text-red-400">{error}</p>}

      {!loading && tours.length === 0 && !error && (
        <p className="text-sm text-slate-500 dark:text-slate-400">
          You haven&apos;t created any tours yet.{' '}
          <Link to="/tours/new" className="font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400">
            Create one
          </Link>
          .
        </p>
      )}

      <div className="space-y-3">
        {tours.map((tour) => (
          <div
            key={tour.id}
            className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
          >
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <Link to={`/tours/${tour.id}`} className="font-medium text-slate-900 hover:underline dark:text-slate-100">
                    {tour.title}
                  </Link>
                  <StatusBadge status={tour.status} />
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  {tour.destination} · {formatDateRange(tour.startDate, tour.endDate)} · {formatMoney(tour.price)}
                </p>
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => setExpandedId(expandedId === tour.id ? null : tour.id)}
                  className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                >
                  {expandedId === tour.id ? 'Hide bookings' : 'View bookings'}
                </button>
                {tour.status === 'DRAFT' && (
                  <button
                    onClick={() => handlePublish(tour.id)}
                    disabled={actioningId === tour.id}
                    className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
                  >
                    Publish
                  </button>
                )}
                {tour.status !== 'CANCELLED' && (
                  <button
                    onClick={() => handleCancel(tour.id)}
                    disabled={actioningId === tour.id}
                    className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 disabled:opacity-50 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                  >
                    Cancel
                  </button>
                )}
              </div>
            </div>

            {expandedId === tour.id && <TourBookings tourId={tour.id} />}
          </div>
        ))}
      </div>
    </AppLayout>
  )
}
