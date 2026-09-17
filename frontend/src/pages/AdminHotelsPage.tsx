import { useEffect, useState } from 'react'
import { getAllHotels, reinstateHotel, suspendHotel } from '../api/hotels'
import { AppLayout } from '../components/AppLayout'
import type { Hotel } from '../types/hotel'

export function AdminHotelsPage() {
  const [hotels, setHotels] = useState<Hotel[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actioningId, setActioningId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      setHotels(await getAllHotels())
    } catch {
      setError('Could not load hotels. Is hotel-service running?')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function handleSuspend(id: string) {
    setActioningId(id)
    try {
      const updated = await suspendHotel(id)
      setHotels((prev) => prev.map((h) => (h.id === id ? updated : h)))
    } catch {
      setError('Could not suspend that hotel.')
    } finally {
      setActioningId(null)
    }
  }

  async function handleReinstate(id: string) {
    setActioningId(id)
    try {
      const updated = await reinstateHotel(id)
      setHotels((prev) => prev.map((h) => (h.id === id ? updated : h)))
    } catch {
      setError('Could not reinstate that hotel.')
    } finally {
      setActioningId(null)
    }
  }

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">Hotels</h1>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="mb-4 text-sm text-red-600 dark:text-red-400">{error}</p>}

      {!loading && !error && hotels.length === 0 && (
        <p className="text-sm text-slate-500 dark:text-slate-400">No hotels registered yet.</p>
      )}

      <div className="space-y-3">
        {hotels.map((hotel) => (
          <div
            key={hotel.id}
            className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
          >
            <div>
              <div className="flex items-center gap-2">
                <span className="font-medium text-slate-900 dark:text-slate-100">{hotel.name}</span>
                <span
                  className={
                    hotel.status === 'SUSPENDED'
                      ? 'rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700 dark:bg-red-950 dark:text-red-300'
                      : 'rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300'
                  }
                >
                  {hotel.status}
                </span>
              </div>
              <p className="text-sm text-slate-500 dark:text-slate-400">
                {[hotel.city, hotel.state, hotel.country].filter(Boolean).join(', ') || 'No address set'}
              </p>
            </div>

            {hotel.status === 'SUSPENDED' ? (
              <button
                onClick={() => handleReinstate(hotel.id)}
                disabled={actioningId === hotel.id}
                className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
              >
                Reinstate
              </button>
            ) : (
              <button
                onClick={() => handleSuspend(hotel.id)}
                disabled={actioningId === hotel.id}
                className="rounded-md border border-red-300 px-3 py-1.5 text-sm text-red-600 transition hover:bg-red-50 disabled:opacity-50 dark:border-red-800 dark:text-red-400 dark:hover:bg-red-950"
              >
                Suspend
              </button>
            )}
          </div>
        ))}
      </div>
    </AppLayout>
  )
}
