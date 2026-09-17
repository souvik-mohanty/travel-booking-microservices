import { useEffect, useState, type FormEvent } from 'react'
import { isAxiosError } from 'axios'
import {
  activateActivity,
  createActivity,
  deactivateActivity,
  deleteActivity,
  getActivitiesForBusiness,
  updateActivity,
} from '../api/activities'
import type { Activity, ActivityCategory, ActivityFormValues } from '../types/activity'
import { formatMoney } from '../lib/format'

const CATEGORIES: ActivityCategory[] = ['ADVENTURE', 'SIGHTSEEING', 'TRANSPORT', 'OTHER']

const EMPTY_FORM: ActivityFormValues = {
  name: '',
  description: '',
  location: '',
  price: 0,
  durationMinutes: 60,
  maxParticipants: 1,
  category: 'ADVENTURE',
}

function toFormValues(activity: Activity): ActivityFormValues {
  return {
    name: activity.name,
    description: activity.description ?? '',
    location: activity.location ?? '',
    price: activity.price,
    durationMinutes: activity.durationMinutes,
    maxParticipants: activity.maxParticipants,
    category: activity.category,
  }
}

// business-service's Create/UpdateActivityRequest treat "" the same as
// "not provided" for optional string fields -- send undefined so an
// emptied-out field actually clears rather than round-tripping as "".
function toRequest(form: ActivityFormValues): ActivityFormValues {
  return {
    ...form,
    description: form.description || undefined,
    location: form.location || undefined,
  }
}

export function ActivityManager({ businessId }: { businessId: string }) {
  const [activities, setActivities] = useState<Activity[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [actioningId, setActioningId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      setActivities(await getActivitiesForBusiness(businessId))
    } catch {
      setError('Could not load activities.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [businessId])

  async function handleActivate(id: string) {
    setActioningId(id)
    try {
      const updated = await activateActivity(id)
      setActivities((prev) => prev.map((a) => (a.id === id ? updated : a)))
    } finally {
      setActioningId(null)
    }
  }

  async function handleDeactivate(id: string) {
    setActioningId(id)
    try {
      const updated = await deactivateActivity(id)
      setActivities((prev) => prev.map((a) => (a.id === id ? updated : a)))
    } finally {
      setActioningId(null)
    }
  }

  async function handleDelete(id: string) {
    setActioningId(id)
    try {
      const updated = await deleteActivity(id)
      setActivities((prev) => prev.map((a) => (a.id === id ? updated : a)))
    } finally {
      setActioningId(null)
    }
  }

  return (
    <div className="mt-8">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100">Activities</h2>
        {!creating && (
          <button
            onClick={() => setCreating(true)}
            className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
          >
            Add activity
          </button>
        )}
      </div>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

      {creating && (
        <ActivityForm
          initial={EMPTY_FORM}
          submitLabel="Add activity"
          onCancel={() => setCreating(false)}
          onSubmit={async (values) => {
            const saved = await createActivity(businessId, toRequest(values))
            setActivities((prev) => [...prev, saved])
            setCreating(false)
          }}
        />
      )}

      {!loading && !error && activities.length === 0 && !creating && (
        <p className="text-sm text-slate-500 dark:text-slate-400">
          No activities yet -- e.g. river rafting, paragliding, or a vehicle-based transport offering.
        </p>
      )}

      <div className="space-y-3">
        {activities.map((activity) =>
          editingId === activity.id ? (
            <ActivityForm
              key={activity.id}
              initial={toFormValues(activity)}
              submitLabel="Save changes"
              onCancel={() => setEditingId(null)}
              onSubmit={async (values) => {
                const saved = await updateActivity(activity.id, toRequest(values))
                setActivities((prev) => prev.map((a) => (a.id === activity.id ? saved : a)))
                setEditingId(null)
              }}
            />
          ) : (
            <div
              key={activity.id}
              className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
            >
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-medium text-slate-900 dark:text-slate-100">{activity.name}</span>
                  <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                    {activity.category}
                  </span>
                  <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                    {activity.status}
                  </span>
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  {formatMoney(activity.price)} · {activity.durationMinutes} min · up to {activity.maxParticipants}{' '}
                  participants
                  {activity.location ? ` · ${activity.location}` : ''}
                </p>
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => setEditingId(activity.id)}
                  className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                >
                  Edit
                </button>
                {activity.status !== 'ACTIVE' && activity.status !== 'DELETED' && (
                  <button
                    onClick={() => handleActivate(activity.id)}
                    disabled={actioningId === activity.id}
                    className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
                  >
                    Activate
                  </button>
                )}
                {activity.status === 'ACTIVE' && (
                  <button
                    onClick={() => handleDeactivate(activity.id)}
                    disabled={actioningId === activity.id}
                    className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 disabled:opacity-50 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                  >
                    Deactivate
                  </button>
                )}
                {activity.status !== 'DELETED' && (
                  <button
                    onClick={() => handleDelete(activity.id)}
                    disabled={actioningId === activity.id}
                    className="rounded-md border border-red-300 px-3 py-1.5 text-sm text-red-600 transition hover:bg-red-50 disabled:opacity-50 dark:border-red-800 dark:text-red-400 dark:hover:bg-red-950"
                  >
                    Delete
                  </button>
                )}
              </div>
            </div>
          ),
        )}
      </div>
    </div>
  )
}

function ActivityForm({
  initial,
  submitLabel,
  onSubmit,
  onCancel,
}: {
  initial: ActivityFormValues
  submitLabel: string
  onSubmit: (values: ActivityFormValues) => Promise<void>
  onCancel: () => void
}) {
  const [form, setForm] = useState(initial)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function update<K extends keyof ActivityFormValues>(key: K, value: ActivityFormValues[K]) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await onSubmit(form)
    } catch (err) {
      setError(
        isAxiosError(err) && err.response?.data?.message
          ? String(err.response.data.message)
          : 'Could not save this activity. Please check your details.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  const inputClassName =
    'mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-950 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100'

  return (
    <form
      onSubmit={handleSubmit}
      className="mb-4 space-y-3 rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
    >
      {error && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
          {error}
        </p>
      )}

      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
        Name
        <input
          required
          value={form.name}
          onChange={(e) => update('name', e.target.value)}
          className={inputClassName}
        />
      </label>

      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
        Description
        <textarea
          rows={2}
          value={form.description ?? ''}
          onChange={(e) => update('description', e.target.value)}
          className={inputClassName}
        />
      </label>

      <div className="grid grid-cols-2 gap-3">
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Category
          <select
            value={form.category}
            onChange={(e) => update('category', e.target.value as ActivityCategory)}
            className={inputClassName}
          >
            {CATEGORIES.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Location
          <input
            value={form.location ?? ''}
            onChange={(e) => update('location', e.target.value)}
            className={inputClassName}
          />
        </label>
      </div>

      <div className="grid grid-cols-3 gap-3">
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Price (INR)
          <input
            type="number"
            min={0}
            required
            value={form.price}
            onChange={(e) => update('price', Number(e.target.value))}
            className={inputClassName}
          />
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Duration (min)
          <input
            type="number"
            min={1}
            required
            value={form.durationMinutes}
            onChange={(e) => update('durationMinutes', Number(e.target.value))}
            className={inputClassName}
          />
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Max participants
          <input
            type="number"
            min={1}
            required
            value={form.maxParticipants}
            onChange={(e) => update('maxParticipants', Number(e.target.value))}
            className={inputClassName}
          />
        </label>
      </div>

      <div className="flex gap-3">
        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
        >
          {submitting ? 'Saving…' : submitLabel}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
        >
          Cancel
        </button>
      </div>
    </form>
  )
}
