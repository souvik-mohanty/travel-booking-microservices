import { useEffect, useState, type CSSProperties, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { searchTours } from '../api/search'
import { AppLayout } from '../components/AppLayout'
import { useAuth } from '../context/AuthContext'
import { formatDateRange, formatMoney } from '../lib/format'
import type { TourSearchResult } from '../types/search'

export function ToursPage() {
  const { user } = useAuth()
  const [q, setQ] = useState('')
  const [destination, setDestination] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')

  const [results, setResults] = useState<TourSearchResult[]>([])
  const [totalResults, setTotalResults] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  async function runSearch() {
    setLoading(true)
    setError(null)
    try {
      const page = await searchTours({
        q,
        destination,
        minPrice: minPrice ? Number(minPrice) : undefined,
        maxPrice: maxPrice ? Number(maxPrice) : undefined,
      })
      setResults(page.results)
      setTotalResults(page.totalResults)
    } catch {
      setError('Could not load tours. Is search-service running?')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    runSearch()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    runSearch()
  }

  return (
    <AppLayout>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">Browse Tours</h1>
        {user?.role === 'BUSINESS' && (
          <Link
            to="/tours/new"
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 dark:bg-blue-500 dark:text-white"
          >
            Create a tour
          </Link>
        )}
      </div>

      <form
        onSubmit={handleSubmit}
        className="mb-8 grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900 sm:grid-cols-5"
      >
        <input
          type="text"
          placeholder="Search tours…"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100 sm:col-span-2"
        />
        <input
          type="text"
          placeholder="Destination"
          value={destination}
          onChange={(e) => setDestination(e.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
        />
        <input
          type="number"
          placeholder="Min price"
          min={0}
          value={minPrice}
          onChange={(e) => setMinPrice(e.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
        />
        <input
          type="number"
          placeholder="Max price"
          min={0}
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
        />
        <button
          type="submit"
          className="rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 dark:bg-blue-500 dark:text-white sm:col-span-5"
        >
          Search
        </button>
      </form>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading tours…</p>}
      {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

      {!loading && !error && results.length === 0 && (
        <p className="text-sm text-slate-500 dark:text-slate-400">
          No published tours match your search yet. Try clearing the filters
          {user?.role === 'BUSINESS' && (
            <>
              , or{' '}
              <Link to="/tours/new" className="font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400">
                create one
              </Link>
            </>
          )}
          .
        </p>
      )}

      {!loading && !error && results.length > 0 && (
        <>
          <p className="mb-3 text-sm text-slate-500 dark:text-slate-400">{totalResults} tour(s) found</p>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {results.map((tour, index) => (
              <Link
                key={tour.id}
                to={`/tours/${tour.id}`}
                style={{ '--tf-delay': index % 9 } as CSSProperties}
                className="animate-fade-in-up animate-stagger rounded-lg border border-slate-200 bg-white p-4 transition-all duration-200 hover:-translate-y-1 hover:border-blue-400 hover:shadow-lg dark:border-slate-800 dark:bg-slate-900 dark:hover:border-blue-500"
              >
                <h2 className="font-semibold text-slate-900 dark:text-slate-100">{tour.title}</h2>
                <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{tour.destination}</p>
                <p className="mt-1 text-xs text-slate-400 dark:text-slate-500">
                  {formatDateRange(tour.startDate, tour.endDate)}
                </p>
                <p className="mt-3 font-medium text-slate-900 dark:text-slate-100">{formatMoney(tour.price)}</p>
              </Link>
            ))}
          </div>
        </>
      )}
    </AppLayout>
  )
}
