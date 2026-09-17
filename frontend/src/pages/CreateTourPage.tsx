import { useState, type FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { useNavigate } from 'react-router-dom'
import { createTour } from '../api/tours'
import { AppLayout } from '../components/AppLayout'

export function CreateTourPage() {
  const navigate = useNavigate()

  const [form, setForm] = useState({
    title: '',
    description: '',
    destination: '',
    startDate: '',
    endDate: '',
    price: '',
    maxParticipants: '',
  })
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function update<K extends keyof typeof form>(key: K, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)

    if (new Date(form.endDate) < new Date(form.startDate)) {
      setError('End date cannot be before start date.')
      return
    }

    setSubmitting(true)
    try {
      const tour = await createTour({
        title: form.title,
        description: form.description || undefined,
        destination: form.destination,
        startDate: form.startDate,
        endDate: form.endDate,
        price: Number(form.price),
        maxParticipants: Number(form.maxParticipants),
      })
      navigate(`/tours/${tour.id}`, { replace: true })
    } catch (err) {
      setError(
        isAxiosError(err) && err.response?.data?.message
          ? String(err.response.data.message)
          : 'Could not create the tour. Please check your details.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">Create a tour</h1>

      <form onSubmit={handleSubmit} className="max-w-lg space-y-4">
        {error && (
          <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
            {error}
          </p>
        )}

        <Field label="Title" required value={form.title} onChange={(v) => update('title', v)} />
        <Field
          label="Description"
          as="textarea"
          value={form.description}
          onChange={(v) => update('description', v)}
        />
        <Field label="Destination" required value={form.destination} onChange={(v) => update('destination', v)} />

        <div className="grid grid-cols-2 gap-4">
          <Field
            label="Start date"
            type="date"
            required
            value={form.startDate}
            onChange={(v) => update('startDate', v)}
          />
          <Field label="End date" type="date" required value={form.endDate} onChange={(v) => update('endDate', v)} />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Field
            label="Price (INR)"
            type="number"
            min={0}
            required
            value={form.price}
            onChange={(v) => update('price', v)}
          />
          <Field
            label="Max participants"
            type="number"
            min={1}
            required
            value={form.maxParticipants}
            onChange={(v) => update('maxParticipants', v)}
          />
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
        >
          {submitting ? 'Creating…' : 'Create tour'}
        </button>

        <p className="text-xs text-slate-400 dark:text-slate-500">
          The tour is created as a draft. Publish it from My Tours to make it searchable.
        </p>
      </form>
    </AppLayout>
  )
}

function Field({
  label,
  value,
  onChange,
  as = 'input',
  type = 'text',
  required,
  min,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  as?: 'input' | 'textarea'
  type?: string
  required?: boolean
  min?: number
}) {
  const className =
    'mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-950 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100'

  return (
    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
      {label}
      {as === 'textarea' ? (
        <textarea
          value={value}
          onChange={(e) => onChange(e.target.value)}
          rows={3}
          className={className}
        />
      ) : (
        <input
          type={type}
          value={value}
          min={min}
          required={required}
          onChange={(e) => onChange(e.target.value)}
          className={className}
        />
      )}
    </label>
  )
}
