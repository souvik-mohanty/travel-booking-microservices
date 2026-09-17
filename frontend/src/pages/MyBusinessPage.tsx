import { useEffect, useState, type FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { createBusiness, getAllBusinesses, updateBusiness } from '../api/business'
import { ActivityManager } from '../components/ActivityManager'
import { AppLayout } from '../components/AppLayout'
import { useAuth } from '../context/AuthContext'
import type { Business, BusinessFormValues } from '../types/business'

const EMPTY_FORM: BusinessFormValues = {
  name: '',
  description: '',
  phone: '',
  email: '',
  address: '',
  city: '',
  state: '',
  country: '',
}

function toFormValues(business: Business): BusinessFormValues {
  return {
    name: business.name,
    description: business.description ?? '',
    phone: business.phone ?? '',
    email: business.email ?? '',
    address: business.address ?? '',
    city: business.city ?? '',
    state: business.state ?? '',
    country: business.country ?? '',
  }
}

// business-service's Create/UpdateBusinessRequest treat "" the same as
// "not provided" for optional fields -- send undefined instead so an
// emptied-out field actually clears rather than round-tripping as "".
function toRequest(form: BusinessFormValues): BusinessFormValues {
  return {
    name: form.name,
    description: form.description || undefined,
    phone: form.phone || undefined,
    email: form.email || undefined,
    address: form.address || undefined,
    city: form.city || undefined,
    state: form.state || undefined,
    country: form.country || undefined,
  }
}

export function MyBusinessPage() {
  const { user } = useAuth()
  const [business, setBusiness] = useState<Business | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [editing, setEditing] = useState(false)

  async function load() {
    setLoading(true)
    setLoadError(null)
    try {
      // business-service has no "my business" endpoint -- filter client-side.
      const all = await getAllBusinesses()
      setBusiness(all.find((b) => b.ownerId === user?.userId) ?? null)
    } catch {
      setLoadError('Could not load your business. Is business-service running?')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.userId])

  function handleSaved(saved: Business) {
    setBusiness(saved)
    setEditing(false)
  }

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">My Business</h1>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {loadError && <p className="text-sm text-red-600 dark:text-red-400">{loadError}</p>}

      {!loading && !loadError && (!business || editing) && (
        <BusinessForm
          initial={business ? toFormValues(business) : EMPTY_FORM}
          submitLabel={business ? 'Save changes' : 'Create business'}
          onCancel={business ? () => setEditing(false) : undefined}
          onSubmit={async (values) => {
            const saved = business
              ? await updateBusiness(business.id, toRequest(values))
              : await createBusiness(toRequest(values))
            handleSaved(saved)
          }}
        />
      )}

      {!loading && !loadError && business && !editing && (
        <div className="max-w-lg rounded-lg border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900">
          <div className="mb-4 flex items-start justify-between">
            <div>
              <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100">{business.name}</h2>
              <span className="mt-1 inline-block rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                {business.status}
              </span>
            </div>
            <button
              onClick={() => setEditing(true)}
              className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
            >
              Edit
            </button>
          </div>

          {business.description && (
            <p className="mb-4 text-sm text-slate-600 dark:text-slate-300">{business.description}</p>
          )}

          <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
            <DetailRow label="Email" value={business.email} />
            <DetailRow label="Phone" value={business.phone} />
            <DetailRow label="Address" value={business.address} />
            <DetailRow label="City" value={business.city} />
            <DetailRow label="State" value={business.state} />
            <DetailRow label="Country" value={business.country} />
          </dl>
        </div>
      )}

      {!loading && !loadError && business && !editing && <ActivityManager businessId={business.id} />}
    </AppLayout>
  )
}

function DetailRow({ label, value }: { label: string; value: string | null }) {
  if (!value) return null
  return (
    <>
      <dt className="text-slate-400 dark:text-slate-500">{label}</dt>
      <dd className="text-slate-900 dark:text-slate-100">{value}</dd>
    </>
  )
}

function BusinessForm({
  initial,
  submitLabel,
  onSubmit,
  onCancel,
}: {
  initial: BusinessFormValues
  submitLabel: string
  onSubmit: (values: BusinessFormValues) => Promise<void>
  onCancel?: () => void
}) {
  const [form, setForm] = useState(initial)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function update<K extends keyof BusinessFormValues>(key: K, value: string) {
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
          : 'Could not save your business. Please check your details.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="max-w-lg space-y-4">
      {error && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
          {error}
        </p>
      )}

      <Field label="Business name" required value={form.name} onChange={(v) => update('name', v)} />
      <Field label="Description" as="textarea" value={form.description ?? ''} onChange={(v) => update('description', v)} />

      <div className="grid grid-cols-2 gap-4">
        <Field label="Email" type="email" value={form.email ?? ''} onChange={(v) => update('email', v)} />
        <Field label="Phone" value={form.phone ?? ''} onChange={(v) => update('phone', v)} />
      </div>

      <Field label="Address" value={form.address ?? ''} onChange={(v) => update('address', v)} />

      <div className="grid grid-cols-3 gap-4">
        <Field label="City" value={form.city ?? ''} onChange={(v) => update('city', v)} />
        <Field label="State" value={form.state ?? ''} onChange={(v) => update('state', v)} />
        <Field label="Country" value={form.country ?? ''} onChange={(v) => update('country', v)} />
      </div>

      <div className="flex gap-3">
        <button
          type="submit"
          disabled={submitting}
          className="flex-1 rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
        >
          {submitting ? 'Saving…' : submitLabel}
        </button>
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  )
}

function Field({
  label,
  value,
  onChange,
  as = 'input',
  type = 'text',
  required,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  as?: 'input' | 'textarea'
  type?: string
  required?: boolean
}) {
  const className =
    'mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-950 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100'

  return (
    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
      {label}
      {as === 'textarea' ? (
        <textarea value={value} onChange={(e) => onChange(e.target.value)} rows={3} className={className} />
      ) : (
        <input
          type={type}
          value={value}
          required={required}
          onChange={(e) => onChange(e.target.value)}
          className={className}
        />
      )}
    </label>
  )
}
