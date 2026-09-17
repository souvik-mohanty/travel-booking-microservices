import { useEffect, useState } from 'react'
import { isAxiosError } from 'axios'
import { getActiveActivities, getActivity } from '../api/activities'
import { addActivityToTour, getActivitiesForTour, removeActivityFromTour } from '../api/tourActivities'
import type { Activity } from '../types/activity'
import type { TourActivityLink } from '../types/itinerary'
import { formatMoney } from '../lib/format'

export function TourActivities({
  tourId,
  tourStartDate,
  tourEndDate,
  isOwner,
}: {
  tourId: string
  tourStartDate: string
  tourEndDate: string
  isOwner: boolean
}) {
  const [links, setLinks] = useState<TourActivityLink[]>([])
  const [activities, setActivities] = useState<Record<string, Activity>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [adding, setAdding] = useState(false)
  const [available, setAvailable] = useState<Activity[]>([])
  const [selectedId, setSelectedId] = useState('')
  const [scheduledDate, setScheduledDate] = useState(tourStartDate)
  const [scheduledTime, setScheduledTime] = useState('')
  const [addError, setAddError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [removingId, setRemovingId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const data = await getActivitiesForTour(tourId)
      data.sort((a, b) =>
        (a.scheduledDate + (a.scheduledTime ?? '')).localeCompare(b.scheduledDate + (b.scheduledTime ?? '')),
      )
      setLinks(data)

      const details = await Promise.all(
        data.map((link) => getActivity(link.activityId).catch(() => null)),
      )
      setActivities(
        Object.fromEntries(
          details.filter((a): a is Activity => a !== null).map((a) => [a.id, a]),
        ),
      )
    } catch {
      setError('Could not load activities for this tour.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tourId])

  useEffect(() => {
    if (!adding) return
    getActiveActivities()
      .then(setAvailable)
      .catch(() => setAddError('Could not load available activities.'))
  }, [adding])

  async function handleAdd() {
    if (!selectedId || !scheduledDate) return
    setAddError(null)
    setSubmitting(true)
    try {
      const link = await addActivityToTour(tourId, {
        activityId: selectedId,
        scheduledDate,
        scheduledTime: scheduledTime || undefined,
      })
      setLinks((prev) =>
        [...prev, link].sort((a, b) =>
          (a.scheduledDate + (a.scheduledTime ?? '')).localeCompare(b.scheduledDate + (b.scheduledTime ?? '')),
        ),
      )
      const activity = available.find((a) => a.id === selectedId)
      if (activity) setActivities((prev) => ({ ...prev, [activity.id]: activity }))
      setAdding(false)
      setSelectedId('')
      setScheduledDate(tourStartDate)
      setScheduledTime('')
    } catch (err) {
      setAddError(
        isAxiosError(err) && err.response?.data?.message
          ? String(err.response.data.message)
          : 'Could not attach that activity.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRemove(activityId: string) {
    setRemovingId(activityId)
    try {
      await removeActivityFromTour(tourId, activityId)
      setLinks((prev) => prev.filter((link) => link.activityId !== activityId))
    } catch {
      setError('Could not remove that activity.')
    } finally {
      setRemovingId(null)
    }
  }

  const attachedIds = new Set(links.map((link) => link.activityId))
  const pickableActivities = available.filter((a) => !attachedIds.has(a.id))

  return (
    <div className="mt-8">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100">Activities</h2>
        {isOwner && !adding && (
          <button
            onClick={() => setAdding(true)}
            className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
          >
            Add activity
          </button>
        )}
      </div>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="mb-3 text-sm text-red-600 dark:text-red-400">{error}</p>}

      {adding && (
        <div className="mb-4 space-y-3 rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900">
          {addError && (
            <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
              {addError}
            </p>
          )}
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
            Choose an activity -- river rafting, paragliding, transport, etc.
            <select
              value={selectedId}
              onChange={(e) => setSelectedId(e.target.value)}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-950 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
            >
              <option value="">Select an activity…</option>
              {pickableActivities.map((activity) => (
                <option key={activity.id} value={activity.id}>
                  [{activity.category}] {activity.name} — {formatMoney(activity.price)}
                </option>
              ))}
            </select>
          </label>

          <div className="grid grid-cols-2 gap-3">
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
              Scheduled date
              <input
                type="date"
                required
                min={tourStartDate}
                max={tourEndDate}
                value={scheduledDate}
                onChange={(e) => setScheduledDate(e.target.value)}
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-950 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
              />
            </label>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
              Time (optional)
              <input
                type="time"
                value={scheduledTime}
                onChange={(e) => setScheduledTime(e.target.value)}
                className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-950 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
              />
            </label>
          </div>

          <div className="flex gap-3">
            <button
              onClick={handleAdd}
              disabled={!selectedId || !scheduledDate || submitting}
              className="rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
            >
              {submitting ? 'Adding…' : 'Attach'}
            </button>
            <button
              type="button"
              onClick={() => {
                setAdding(false)
                setSelectedId('')
                setScheduledDate(tourStartDate)
                setScheduledTime('')
                setAddError(null)
              }}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      {!loading && !error && links.length === 0 && !adding && (
        <p className="text-sm text-slate-500 dark:text-slate-400">No activities attached yet.</p>
      )}

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        {links.map((link) => {
          const activity = activities[link.activityId]
          return (
            <div
              key={link.id}
              className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
            >
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-medium text-slate-900 dark:text-slate-100">
                    {activity?.name ?? 'Activity'}
                  </span>
                  {activity && (
                    <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                      {activity.category}
                    </span>
                  )}
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  {link.scheduledDate}
                  {link.scheduledTime ? ` at ${link.scheduledTime.slice(0, 5)}` : ''}
                  {activity ? ` · ${formatMoney(activity.price)} · ${activity.durationMinutes} min` : ''}
                </p>
              </div>
              {isOwner && (
                <button
                  onClick={() => handleRemove(link.activityId)}
                  disabled={removingId === link.activityId}
                  className="rounded-md border border-red-300 px-3 py-1.5 text-sm text-red-600 transition hover:bg-red-50 disabled:opacity-50 dark:border-red-800 dark:text-red-400 dark:hover:bg-red-950"
                >
                  Remove
                </button>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}
