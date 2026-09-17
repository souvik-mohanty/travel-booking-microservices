import { useEffect, useState, type FormEvent } from 'react'
import { isAxiosError } from 'axios'
import { addLeg, getLegsForTour, removeLeg } from '../api/tourLegs'
import { getAllHotels, getHotel } from '../api/hotels'
import { getRoom, getRoomAvailability, getRoomsForHotel } from '../api/rooms'
import type { Hotel, Room, RoomAvailability } from '../types/hotel'
import type { CreateTourLegRequest, TourLeg } from '../types/itinerary'
import { formatDateRange } from '../lib/format'

export function TourItinerary({
  tourId,
  tourDestination,
  isOwner,
}: {
  tourId: string
  tourDestination: string
  isOwner: boolean
}) {
  const [legs, setLegs] = useState<TourLeg[]>([])
  const [hotelNames, setHotelNames] = useState<Record<string, string>>({})
  const [roomTypes, setRoomTypes] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [adding, setAdding] = useState(false)
  const [removingId, setRemovingId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const data = await getLegsForTour(tourId)
      setLegs(data)

      const hotelIds = [...new Set(data.map((leg) => leg.hotelId))]
      const roomIds = [...new Set(data.map((leg) => leg.roomId))]

      const [hotels, rooms] = await Promise.all([
        Promise.all(hotelIds.map((id) => getHotel(id).catch(() => null))),
        Promise.all(roomIds.map((id) => getRoom(id).catch(() => null))),
      ])

      setHotelNames(
        Object.fromEntries(hotels.filter((h): h is Hotel => h !== null).map((h) => [h.id, h.name])),
      )
      setRoomTypes(
        Object.fromEntries(rooms.filter((r): r is Room => r !== null).map((r) => [r.id, r.roomType])),
      )
    } catch {
      setError('Could not load the itinerary.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tourId])

  async function handleRemove(legId: string) {
    setRemovingId(legId)
    try {
      await removeLeg(tourId, legId)
      setLegs((prev) => prev.filter((leg) => leg.id !== legId))
    } catch {
      setError('Could not remove that leg.')
    } finally {
      setRemovingId(null)
    }
  }

  return (
    <div className="mt-8">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100">Itinerary</h2>
        {isOwner && !adding && (
          <button
            onClick={() => setAdding(true)}
            className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
          >
            Add stay
          </button>
        )}
      </div>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="mb-3 text-sm text-red-600 dark:text-red-400">{error}</p>}

      {adding && (
        <AddLegForm
          tourId={tourId}
          onCancel={() => setAdding(false)}
          onAdded={(leg) => {
            setLegs((prev) => [...prev, leg])
            setAdding(false)
            load()
          }}
        />
      )}

      {!loading && !error && legs.length === 0 && !adding && (
        <p className="text-sm text-slate-500 dark:text-slate-400">
          No stays added yet -- which hotel will be used during the trip.
        </p>
      )}

      <ol className="space-y-3">
        {legs.map((leg) => (
          <li
            key={leg.id}
            className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
          >
            <div>
              <p className="font-medium text-slate-900 dark:text-slate-100">
                {leg.sequenceOrder}. {leg.destination || tourDestination}
              </p>
              <p className="text-sm text-slate-500 dark:text-slate-400">
                {formatDateRange(leg.startDate, leg.endDate)} ·{' '}
                {hotelNames[leg.hotelId] ?? 'Hotel'} — {roomTypes[leg.roomId] ?? 'Room'} × {leg.roomsBooked}
              </p>
            </div>
            {isOwner && (
              <button
                onClick={() => handleRemove(leg.id)}
                disabled={removingId === leg.id}
                className="rounded-md border border-red-300 px-3 py-1.5 text-sm text-red-600 transition hover:bg-red-50 disabled:opacity-50 dark:border-red-800 dark:text-red-400 dark:hover:bg-red-950"
              >
                Remove
              </button>
            )}
          </li>
        ))}
      </ol>
    </div>
  )
}

function AddLegForm({
  tourId,
  onCancel,
  onAdded,
}: {
  tourId: string
  onCancel: () => void
  onAdded: (leg: TourLeg) => void
}) {
  const [hotels, setHotels] = useState<Hotel[]>([])
  const [rooms, setRooms] = useState<Room[]>([])
  const [availability, setAvailability] = useState<RoomAvailability | null>(null)
  const [availabilityError, setAvailabilityError] = useState<string | null>(null)

  const [form, setForm] = useState({
    destination: '',
    startDate: '',
    endDate: '',
    hotelId: '',
    roomId: '',
    roomsBooked: 1,
  })
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    getAllHotels()
      .then((all) => setHotels(all.filter((h) => h.status === 'ACTIVE')))
      .catch(() => setError('Could not load hotels.'))
  }, [])

  useEffect(() => {
    if (!form.hotelId) {
      setRooms([])
      return
    }
    getRoomsForHotel(form.hotelId)
      .then((all) => setRooms(all.filter((r) => r.status === 'ACTIVE')))
      .catch(() => setError('Could not load rooms for that hotel.'))
  }, [form.hotelId])

  useEffect(() => {
    setAvailability(null)
    setAvailabilityError(null)
    if (!form.roomId || !form.startDate || !form.endDate) return
    if (form.startDate >= form.endDate) return

    getRoomAvailability(form.roomId, form.startDate, form.endDate)
      .then(setAvailability)
      .catch(() => setAvailabilityError('Could not check availability for these dates.'))
  }, [form.roomId, form.startDate, form.endDate])

  function update<K extends keyof typeof form>(key: K, value: (typeof form)[K]) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const request: CreateTourLegRequest = {
        destination: form.destination || undefined,
        startDate: form.startDate,
        endDate: form.endDate,
        hotelId: form.hotelId,
        roomId: form.roomId,
        roomsBooked: form.roomsBooked,
      }
      const leg = await addLeg(tourId, request)
      onAdded(leg)
    } catch (err) {
      setError(
        isAxiosError(err) && err.response?.data?.message
          ? String(err.response.data.message)
          : 'Could not add this stay. Please check your details.',
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
        Destination (optional -- defaults to the tour&apos;s own destination)
        <input
          value={form.destination}
          onChange={(e) => update('destination', e.target.value)}
          className={inputClassName}
        />
      </label>

      <div className="grid grid-cols-2 gap-3">
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Check-in
          <input
            type="date"
            required
            value={form.startDate}
            onChange={(e) => update('startDate', e.target.value)}
            className={inputClassName}
          />
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Check-out
          <input
            type="date"
            required
            value={form.endDate}
            onChange={(e) => update('endDate', e.target.value)}
            className={inputClassName}
          />
        </label>
      </div>

      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
        Hotel
        <select
          required
          value={form.hotelId}
          onChange={(e) => update('hotelId', e.target.value)}
          className={inputClassName}
        >
          <option value="">Select a registered hotel…</option>
          {hotels.map((hotel) => (
            <option key={hotel.id} value={hotel.id}>
              {hotel.name}
              {hotel.city ? ` — ${hotel.city}` : ''}
            </option>
          ))}
        </select>
      </label>

      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
        Room type
        <select
          required
          disabled={!form.hotelId}
          value={form.roomId}
          onChange={(e) => update('roomId', e.target.value)}
          className={inputClassName}
        >
          <option value="">{form.hotelId ? 'Select a room type…' : 'Choose a hotel first'}</option>
          {rooms.map((room) => (
            <option key={room.id} value={room.id}>
              {room.roomType} (sleeps {room.capacity})
            </option>
          ))}
        </select>
      </label>

      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
        Number of rooms
        <input
          type="number"
          min={1}
          required
          value={form.roomsBooked}
          onChange={(e) => update('roomsBooked', Number(e.target.value))}
          className={inputClassName}
        />
      </label>

      {availabilityError && <p className="text-sm text-red-600 dark:text-red-400">{availabilityError}</p>}
      {availability && (
        <p
          className={
            availability.availableRooms >= form.roomsBooked
              ? 'text-sm text-emerald-600 dark:text-emerald-400'
              : 'text-sm text-amber-600 dark:text-amber-400'
          }
        >
          {availability.availableRooms} of {availability.totalRooms} room(s) available for these dates.
        </p>
      )}

      <div className="flex gap-3">
        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
        >
          {submitting ? 'Adding…' : 'Add stay'}
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
