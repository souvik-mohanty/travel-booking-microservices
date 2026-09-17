import { useEffect, useState, type FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { createHotel, getAllHotels, updateHotel } from '../api/hotels'
import { createRoom } from '../api/rooms'
import { AppLayout } from '../components/AppLayout'
import { RoomManager } from '../components/RoomManager'
import { useAuth } from '../context/AuthContext'
import type { Hotel, HotelFormValues, RoomFormValues } from '../types/hotel'

const EMPTY_FORM: HotelFormValues = {
  name: '',
  description: '',
  address: '',
  city: '',
  state: '',
  country: '',
}

const EMPTY_ROOM_FORM: RoomFormValues = {
  roomType: 'Standard',
  pricePerNight: 0,
  capacity: 2,
  totalRooms: 1,
}

function toFormValues(hotel: Hotel): HotelFormValues {
  return {
    name: hotel.name,
    description: hotel.description ?? '',
    address: hotel.address ?? '',
    city: hotel.city ?? '',
    state: hotel.state ?? '',
    country: hotel.country ?? '',
  }
}

// hotel-service's Create/UpdateHotelRequest treat "" the same as "not
// provided" for optional fields -- send undefined so an emptied-out field
// actually clears rather than round-tripping as "".
function toRequest(form: HotelFormValues): HotelFormValues {
  return {
    name: form.name,
    description: form.description || undefined,
    address: form.address || undefined,
    city: form.city || undefined,
    state: form.state || undefined,
    country: form.country || undefined,
  }
}

export function MyHotelsPage() {
  const { user } = useAuth()
  const [hotels, setHotels] = useState<Hotel[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [expandedId, setExpandedId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      // hotel-service has no "my hotels" endpoint -- filter client-side.
      const all = await getAllHotels()
      setHotels(all.filter((h) => h.ownerId === user?.userId))
    } catch {
      setError('Could not load your hotels. Is hotel-service running?')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.userId])

  return (
    <AppLayout>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">My Hotels</h1>
        {!creating && (
          <button
            onClick={() => setCreating(true)}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 dark:bg-blue-500 dark:text-white"
          >
            Add a hotel
          </button>
        )}
      </div>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="mb-4 text-sm text-red-600 dark:text-red-400">{error}</p>}

      {creating && (
        <HotelForm
          initial={EMPTY_FORM}
          initialRoom={EMPTY_ROOM_FORM}
          includeRoom
          submitLabel="Add hotel"
          onCancel={() => setCreating(false)}
          onSubmit={async (values, roomValues) => {
            const hotel = await createHotel(toRequest(values))
            if (roomValues) {
              await createRoom(hotel.id, roomValues)
            }
            setHotels((prev) => [...prev, hotel])
            setCreating(false)
            setExpandedId(hotel.id)
          }}
        />
      )}

      {!loading && !error && hotels.length === 0 && !creating && (
        <p className="text-sm text-slate-500 dark:text-slate-400">You haven&apos;t added any hotels yet.</p>
      )}

      <div className="space-y-4">
        {hotels.map((hotel) =>
          editingId === hotel.id ? (
            <HotelForm
              key={hotel.id}
              initial={toFormValues(hotel)}
              submitLabel="Save changes"
              onCancel={() => setEditingId(null)}
              onSubmit={async (values) => {
                const saved = await updateHotel(hotel.id, toRequest(values))
                setHotels((prev) => prev.map((h) => (h.id === hotel.id ? saved : h)))
                setEditingId(null)
              }}
            />
          ) : (
            <div
              key={hotel.id}
              className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
            >
              <div className="flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-900 dark:text-slate-100">{hotel.name}</span>
                    <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                      {hotel.status}
                    </span>
                  </div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">
                    {[hotel.city, hotel.state, hotel.country].filter(Boolean).join(', ') || 'No address set'}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => setExpandedId(expandedId === hotel.id ? null : hotel.id)}
                    className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                  >
                    {expandedId === hotel.id ? 'Hide rooms' : 'Manage rooms'}
                  </button>
                  <button
                    onClick={() => setEditingId(hotel.id)}
                    className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                  >
                    Edit
                  </button>
                </div>
              </div>

              {expandedId === hotel.id && <RoomManager hotelId={hotel.id} />}
            </div>
          ),
        )}
      </div>
    </AppLayout>
  )
}

function HotelForm({
  initial,
  initialRoom,
  includeRoom = false,
  submitLabel,
  onSubmit,
  onCancel,
}: {
  initial: HotelFormValues
  initialRoom?: RoomFormValues
  includeRoom?: boolean
  submitLabel: string
  onSubmit: (values: HotelFormValues, roomValues?: RoomFormValues) => Promise<void>
  onCancel: () => void
}) {
  const [form, setForm] = useState(initial)
  const [roomForm, setRoomForm] = useState<RoomFormValues>(initialRoom ?? EMPTY_ROOM_FORM)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function update<K extends keyof HotelFormValues>(key: K, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  function updateRoom<K extends keyof RoomFormValues>(key: K, value: RoomFormValues[K]) {
    setRoomForm((prev) => ({ ...prev, [key]: value }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await onSubmit(form, includeRoom ? roomForm : undefined)
    } catch (err) {
      setError(
        isAxiosError(err) && err.response?.data?.message
          ? String(err.response.data.message)
          : 'Could not save this hotel. Please check your details.',
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
      className="mb-4 max-w-lg space-y-4 rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
    >
      {error && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
          {error}
        </p>
      )}

      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
        Hotel name
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

      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
        Address
        <input
          value={form.address ?? ''}
          onChange={(e) => update('address', e.target.value)}
          className={inputClassName}
        />
      </label>

      <div className="grid grid-cols-3 gap-4">
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          City
          <input value={form.city ?? ''} onChange={(e) => update('city', e.target.value)} className={inputClassName} />
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          State
          <input
            value={form.state ?? ''}
            onChange={(e) => update('state', e.target.value)}
            className={inputClassName}
          />
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Country
          <input
            value={form.country ?? ''}
            onChange={(e) => update('country', e.target.value)}
            className={inputClassName}
          />
        </label>
      </div>

      {includeRoom && (
        <div className="space-y-3 border-t border-slate-200 pt-4 dark:border-slate-800">
          <p className="text-sm font-medium text-slate-700 dark:text-slate-300">
            First room type -- you can add more later from &quot;Manage rooms&quot;.
          </p>

          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
            Room type
            <input
              required
              placeholder="e.g. Deluxe, Standard"
              value={roomForm.roomType}
              onChange={(e) => updateRoom('roomType', e.target.value)}
              className={inputClassName}
            />
          </label>

          <div className="grid grid-cols-3 gap-4">
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
              Price/night (INR)
              <input
                type="number"
                min={0}
                required
                value={roomForm.pricePerNight}
                onChange={(e) => updateRoom('pricePerNight', Number(e.target.value))}
                className={inputClassName}
              />
            </label>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
              Room capacity
              <input
                type="number"
                min={1}
                required
                value={roomForm.capacity}
                onChange={(e) => updateRoom('capacity', Number(e.target.value))}
                className={inputClassName}
              />
            </label>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
              Number of rooms
              <input
                type="number"
                min={1}
                required
                value={roomForm.totalRooms}
                onChange={(e) => updateRoom('totalRooms', Number(e.target.value))}
                className={inputClassName}
              />
            </label>
          </div>
        </div>
      )}

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
