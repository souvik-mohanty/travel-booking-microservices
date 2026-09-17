import { useEffect, useState, type FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { cancelTrip, completeTrip, createTrip, getTripForBooking, startTrip } from '../api/trips'
import { addTripLogEntry, getTripLog } from '../api/tripLogs'
import type { Trip, TripLogEntry, TripLogEntryType } from '../types/trip'
import { formatDateTime } from '../lib/format'

const LOG_TYPE_LABELS: Record<TripLogEntryType, string> = {
  CHECK_IN: 'Check-in',
  CHECK_OUT: 'Check-out',
  ACTIVITY_COMPLETED: 'Activity completed',
  DESTINATION_REACHED: 'Destination reached',
  OTHER: 'Note',
}

const LOG_TYPES: TripLogEntryType[] = ['CHECK_IN', 'CHECK_OUT', 'ACTIVITY_COMPLETED', 'DESTINATION_REACHED', 'OTHER']

// role="tourist": can create/start/complete/cancel the trip (trip-service's
// createdBy), sees the logbook read-only.
// role="business": can't touch the trip's lifecycle, but can write logbook
// entries if they own the tour (enforced server-side either way).
// role="admin": read-only on everything -- no lifecycle actions, can't write
// logbook entries, just visibility (server-side access is a bypass, not an
// ownership match, so this is purely a UI restriction).
export function TripPanel({ bookingId, role }: { bookingId: string; role: 'tourist' | 'business' | 'admin' }) {
  const [trip, setTrip] = useState<Trip | null>(null)
  const [notScheduled, setNotScheduled] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actioning, setActioning] = useState(false)
  const [expanded, setExpanded] = useState(false)

  async function load() {
    setLoading(true)
    setError(null)
    setNotScheduled(false)
    try {
      setTrip(await getTripForBooking(bookingId))
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 404) {
        setNotScheduled(true)
      } else {
        setError('Could not load trip status.')
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (expanded) load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expanded, bookingId])

  async function handleCreateTrip() {
    setActioning(true)
    setError(null)
    try {
      setTrip(await createTrip(bookingId))
      setNotScheduled(false)
    } catch {
      setError('Could not schedule a trip for this booking.')
    } finally {
      setActioning(false)
    }
  }

  async function handleLifecycle(action: 'start' | 'complete' | 'cancel') {
    if (!trip) return
    setActioning(true)
    setError(null)
    try {
      const fn = action === 'start' ? startTrip : action === 'complete' ? completeTrip : cancelTrip
      setTrip(await fn(trip.id))
    } catch {
      setError(`Could not ${action} the trip.`)
    } finally {
      setActioning(false)
    }
  }

  return (
    <div className="mt-3 border-t border-slate-200 pt-3 dark:border-slate-800">
      <button
        onClick={() => setExpanded((prev) => !prev)}
        className="text-sm font-medium text-slate-700 underline dark:text-slate-300"
      >
        {expanded ? 'Hide trip & logbook' : 'View trip & logbook'}
      </button>

      {expanded && (
        <div className="mt-3">
          {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
          {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

          {!loading && notScheduled && role === 'tourist' && (
            <button
              onClick={handleCreateTrip}
              disabled={actioning}
              className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
            >
              {actioning ? 'Scheduling…' : 'Start trip tracking'}
            </button>
          )}

          {!loading && notScheduled && role !== 'tourist' && (
            <p className="text-sm text-slate-500 dark:text-slate-400">
              The tourist hasn&apos;t started trip tracking for this booking yet.
            </p>
          )}

          {!loading && trip && (
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                  {trip.status}
                </span>
                {role === 'tourist' && trip.status === 'SCHEDULED' && (
                  <button
                    onClick={() => handleLifecycle('start')}
                    disabled={actioning}
                    className="text-xs font-medium text-slate-700 underline disabled:opacity-50 dark:text-slate-300"
                  >
                    Start
                  </button>
                )}
                {role === 'tourist' && trip.status === 'IN_PROGRESS' && (
                  <button
                    onClick={() => handleLifecycle('complete')}
                    disabled={actioning}
                    className="text-xs font-medium text-slate-700 underline disabled:opacity-50 dark:text-slate-300"
                  >
                    Complete
                  </button>
                )}
                {role === 'tourist' && (trip.status === 'SCHEDULED' || trip.status === 'IN_PROGRESS') && (
                  <button
                    onClick={() => handleLifecycle('cancel')}
                    disabled={actioning}
                    className="text-xs font-medium text-red-600 underline disabled:opacity-50 dark:text-red-400"
                  >
                    Cancel
                  </button>
                )}
              </div>

              <TripLogbook tripId={trip.id} canWrite={role === 'business'} />
            </div>
          )}
        </div>
      )}
    </div>
  )
}

function TripLogbook({ tripId, canWrite }: { tripId: string; canWrite: boolean }) {
  const [entries, setEntries] = useState<TripLogEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [adding, setAdding] = useState(false)

  const [form, setForm] = useState<{ type: TripLogEntryType; description: string; location: string }>({
    type: 'CHECK_IN',
    description: '',
    location: '',
  })
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      setEntries(await getTripLog(tripId))
    } catch {
      setError('Could not load the logbook.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tripId])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setFormError(null)
    setSubmitting(true)
    try {
      const entry = await addTripLogEntry(tripId, {
        type: form.type,
        description: form.description || undefined,
        location: form.location || undefined,
      })
      setEntries((prev) => [...prev, entry])
      setAdding(false)
      setForm({ type: 'CHECK_IN', description: '', location: '' })
    } catch (err) {
      setFormError(
        isAxiosError(err) && err.response?.data?.message
          ? String(err.response.data.message)
          : 'Could not add that logbook entry.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  const inputClassName =
    'mt-1 w-full rounded-md border border-slate-300 px-2.5 py-1.5 text-sm text-slate-900 outline-none transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-950 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100'

  return (
    <div>
      <div className="mb-2 flex items-center justify-between">
        <h4 className="text-sm font-semibold text-slate-700 dark:text-slate-300">Logbook</h4>
        {canWrite && !adding && (
          <button
            onClick={() => setAdding(true)}
            className="rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
          >
            Add entry
          </button>
        )}
      </div>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

      {adding && (
        <form
          onSubmit={handleSubmit}
          className="mb-3 space-y-2 rounded-md border border-slate-200 bg-slate-50 p-3 dark:border-slate-800 dark:bg-slate-950"
        >
          {formError && <p className="text-sm text-red-600 dark:text-red-400">{formError}</p>}
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
            Type
            <select
              value={form.type}
              onChange={(e) => setForm((prev) => ({ ...prev, type: e.target.value as TripLogEntryType }))}
              className={inputClassName}
            >
              {LOG_TYPES.map((type) => (
                <option key={type} value={type}>
                  {LOG_TYPE_LABELS[type]}
                </option>
              ))}
            </select>
          </label>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
            Description
            <input
              value={form.description}
              onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
              className={inputClassName}
            />
          </label>
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
            Location
            <input
              value={form.location}
              onChange={(e) => setForm((prev) => ({ ...prev, location: e.target.value }))}
              className={inputClassName}
            />
          </label>
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={submitting}
              className="rounded-md bg-blue-600 px-3 py-1.5 text-xs font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
            >
              {submitting ? 'Saving…' : 'Add'}
            </button>
            <button
              type="button"
              onClick={() => setAdding(false)}
              className="rounded-md border border-slate-300 px-3 py-1.5 text-xs text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {!loading && !error && entries.length === 0 && !adding && (
        <p className="text-sm text-slate-500 dark:text-slate-400">No logbook entries yet.</p>
      )}

      <ol className="space-y-2">
        {entries.map((entry) => (
          <li key={entry.id} className="text-sm">
            <span className="font-medium text-slate-900 dark:text-slate-100">{LOG_TYPE_LABELS[entry.type]}</span>
            <span className="text-slate-400 dark:text-slate-500"> · {formatDateTime(entry.occurredAt)}</span>
            {entry.location && <span className="text-slate-500 dark:text-slate-400"> · {entry.location}</span>}
            {entry.description && (
              <p className="text-slate-600 dark:text-slate-300">{entry.description}</p>
            )}
          </li>
        ))}
      </ol>
    </div>
  )
}
