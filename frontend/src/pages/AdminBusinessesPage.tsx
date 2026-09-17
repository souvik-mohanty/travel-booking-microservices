import { useEffect, useState } from 'react'
import { getAllBusinesses, reinstateBusiness, suspendBusiness } from '../api/business'
import { AppLayout } from '../components/AppLayout'
import type { Business } from '../types/business'

export function AdminBusinessesPage() {
  const [businesses, setBusinesses] = useState<Business[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actioningId, setActioningId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      setBusinesses(await getAllBusinesses())
    } catch {
      setError('Could not load businesses. Is business-service running?')
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
      const updated = await suspendBusiness(id)
      setBusinesses((prev) => prev.map((b) => (b.id === id ? updated : b)))
    } catch {
      setError('Could not suspend that business.')
    } finally {
      setActioningId(null)
    }
  }

  async function handleReinstate(id: string) {
    setActioningId(id)
    try {
      const updated = await reinstateBusiness(id)
      setBusinesses((prev) => prev.map((b) => (b.id === id ? updated : b)))
    } catch {
      setError('Could not reinstate that business.')
    } finally {
      setActioningId(null)
    }
  }

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">Businesses</h1>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="mb-4 text-sm text-red-600 dark:text-red-400">{error}</p>}

      {!loading && !error && businesses.length === 0 && (
        <p className="text-sm text-slate-500 dark:text-slate-400">No businesses registered yet.</p>
      )}

      <div className="space-y-3">
        {businesses.map((business) => (
          <div
            key={business.id}
            className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
          >
            <div>
              <div className="flex items-center gap-2">
                <span className="font-medium text-slate-900 dark:text-slate-100">{business.name}</span>
                <span
                  className={
                    business.status === 'SUSPENDED'
                      ? 'rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700 dark:bg-red-950 dark:text-red-300'
                      : 'rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300'
                  }
                >
                  {business.status}
                </span>
              </div>
              <p className="text-sm text-slate-500 dark:text-slate-400">
                {[business.city, business.state, business.country].filter(Boolean).join(', ') || 'No address set'}
                {business.email ? ` · ${business.email}` : ''}
              </p>
            </div>

            {business.status === 'SUSPENDED' ? (
              <button
                onClick={() => handleReinstate(business.id)}
                disabled={actioningId === business.id}
                className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
              >
                Reinstate
              </button>
            ) : (
              <button
                onClick={() => handleSuspend(business.id)}
                disabled={actioningId === business.id}
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
