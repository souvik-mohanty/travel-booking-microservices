import { useEffect, useState, type FormEvent } from 'react'
import { isAxiosError } from 'axios'
import {
  activateRoom,
  createRoom,
  deactivateRoom,
  deleteRoom,
  getReservationsForRoom,
  getRoomsForHotel,
  updateRoom,
} from '../api/rooms'
import type { Room, RoomFormValues, RoomReservation } from '../types/hotel'
import { formatMoney } from '../lib/format'

const EMPTY_FORM: RoomFormValues = {
  roomType: '',
  pricePerNight: 0,
  capacity: 1,
  totalRooms: 1,
}

function toFormValues(room: Room): RoomFormValues {
  return {
    roomType: room.roomType,
    pricePerNight: room.pricePerNight,
    capacity: room.capacity,
    totalRooms: room.totalRooms,
  }
}

export function RoomManager({ hotelId }: { hotelId: string }) {
  const [rooms, setRooms] = useState<Room[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [reservationsFor, setReservationsFor] = useState<string | null>(null)
  const [actioningId, setActioningId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      setRooms(await getRoomsForHotel(hotelId))
    } catch {
      setError('Could not load rooms.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hotelId])

  async function handleActivate(id: string) {
    setActioningId(id)
    try {
      const updated = await activateRoom(id)
      setRooms((prev) => prev.map((r) => (r.id === id ? updated : r)))
    } finally {
      setActioningId(null)
    }
  }

  async function handleDeactivate(id: string) {
    setActioningId(id)
    try {
      const updated = await deactivateRoom(id)
      setRooms((prev) => prev.map((r) => (r.id === id ? updated : r)))
    } finally {
      setActioningId(null)
    }
  }

  async function handleDelete(id: string) {
    setActioningId(id)
    try {
      const updated = await deleteRoom(id)
      setRooms((prev) => prev.map((r) => (r.id === id ? updated : r)))
    } finally {
      setActioningId(null)
    }
  }

  return (
    <div className="mt-3 border-t border-slate-200 pt-3 dark:border-slate-800">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="text-sm font-semibold text-slate-700 dark:text-slate-300">Room types</h3>
        {!creating && (
          <button
            onClick={() => setCreating(true)}
            className="rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
          >
            Add room type
          </button>
        )}
      </div>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

      {creating && (
        <RoomForm
          initial={EMPTY_FORM}
          submitLabel="Add room type"
          onCancel={() => setCreating(false)}
          onSubmit={async (values) => {
            const saved = await createRoom(hotelId, values)
            setRooms((prev) => [...prev, saved])
            setCreating(false)
          }}
        />
      )}

      {!loading && !error && rooms.length === 0 && !creating && (
        <p className="text-sm text-slate-500 dark:text-slate-400">No room types yet.</p>
      )}

      <div className="space-y-2">
        {rooms.map((room) =>
          editingId === room.id ? (
            <RoomForm
              key={room.id}
              initial={toFormValues(room)}
              submitLabel="Save changes"
              onCancel={() => setEditingId(null)}
              onSubmit={async (values) => {
                const saved = await updateRoom(room.id, values)
                setRooms((prev) => prev.map((r) => (r.id === room.id ? saved : r)))
                setEditingId(null)
              }}
            />
          ) : (
            <div key={room.id} className="rounded-md border border-slate-200 p-3 dark:border-slate-800">
              <div className="flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-slate-900 dark:text-slate-100">{room.roomType}</span>
                    <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                      {room.status}
                    </span>
                  </div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">
                    {formatMoney(room.pricePerNight)}/night · sleeps {room.capacity} · {room.totalRooms} room(s) of
                    this type
                  </p>
                </div>

                <div className="flex flex-wrap gap-2">
                  <button
                    onClick={() => setReservationsFor(reservationsFor === room.id ? null : room.id)}
                    className="rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                  >
                    {reservationsFor === room.id ? 'Hide bookings' : 'View bookings'}
                  </button>
                  <button
                    onClick={() => setEditingId(room.id)}
                    className="rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                  >
                    Edit
                  </button>
                  {room.status !== 'ACTIVE' && room.status !== 'DELETED' && (
                    <button
                      onClick={() => handleActivate(room.id)}
                      disabled={actioningId === room.id}
                      className="rounded-md bg-blue-600 px-2.5 py-1 text-xs font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
                    >
                      Activate
                    </button>
                  )}
                  {room.status === 'ACTIVE' && (
                    <button
                      onClick={() => handleDeactivate(room.id)}
                      disabled={actioningId === room.id}
                      className="rounded-md border border-slate-300 px-2.5 py-1 text-xs text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 disabled:opacity-50 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                    >
                      Deactivate
                    </button>
                  )}
                  {room.status !== 'DELETED' && (
                    <button
                      onClick={() => handleDelete(room.id)}
                      disabled={actioningId === room.id}
                      className="rounded-md border border-red-300 px-2.5 py-1 text-xs text-red-600 transition hover:bg-red-50 disabled:opacity-50 dark:border-red-800 dark:text-red-400 dark:hover:bg-red-950"
                    >
                      Delete
                    </button>
                  )}
                </div>
              </div>

              {reservationsFor === room.id && <RoomReservations roomId={room.id} />}
            </div>
          ),
        )}
      </div>
    </div>
  )
}

// Hotel-owner visibility into which tours have booked this room type.
function RoomReservations({ roomId }: { roomId: string }) {
  const [reservations, setReservations] = useState<RoomReservation[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getReservationsForRoom(roomId)
      .then(setReservations)
      .catch(() => setError('Could not load bookings for this room.'))
  }, [roomId])

  return (
    <div className="mt-3 rounded-md bg-slate-50 p-3 text-sm dark:bg-slate-950">
      {error && <p className="text-red-600 dark:text-red-400">{error}</p>}
      {!error && reservations === null && <p className="text-slate-500 dark:text-slate-400">Loading…</p>}
      {reservations !== null && reservations.length === 0 && (
        <p className="text-slate-500 dark:text-slate-400">No tours have booked this room type yet.</p>
      )}
      {reservations !== null && reservations.length > 0 && (
        <table className="w-full text-left text-xs">
          <thead className="text-slate-400 dark:text-slate-500">
            <tr>
              <th className="pb-1 pr-3 font-medium">Dates</th>
              <th className="pb-1 pr-3 font-medium">Rooms</th>
              <th className="pb-1 pr-3 font-medium">Status</th>
              <th className="pb-1 font-medium">Tour ID</th>
            </tr>
          </thead>
          <tbody>
            {reservations.map((reservation) => (
              <tr key={reservation.id} className="border-t border-slate-200 dark:border-slate-800">
                <td className="py-1 pr-3 text-slate-700 dark:text-slate-300">
                  {reservation.startDate} → {reservation.endDate}
                </td>
                <td className="py-1 pr-3 text-slate-700 dark:text-slate-300">{reservation.roomsReserved}</td>
                <td className="py-1 pr-3">
                  <span
                    className={
                      reservation.status === 'ACTIVE'
                        ? 'text-emerald-600 dark:text-emerald-400'
                        : 'text-slate-400 dark:text-slate-500'
                    }
                  >
                    {reservation.status}
                  </span>
                </td>
                <td className="py-1 font-mono text-slate-500 dark:text-slate-400">
                  {reservation.tourId.slice(0, 8)}…
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

function RoomForm({
  initial,
  submitLabel,
  onSubmit,
  onCancel,
}: {
  initial: RoomFormValues
  submitLabel: string
  onSubmit: (values: RoomFormValues) => Promise<void>
  onCancel: () => void
}) {
  const [form, setForm] = useState(initial)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function update<K extends keyof RoomFormValues>(key: K, value: RoomFormValues[K]) {
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
          : 'Could not save this room type.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  const inputClassName =
    'mt-1 w-full rounded-md border border-slate-300 px-2.5 py-1.5 text-sm text-slate-900 outline-none transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-950 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100'

  return (
    <form
      onSubmit={handleSubmit}
      className="mb-3 space-y-3 rounded-md border border-slate-200 bg-slate-50 p-3 dark:border-slate-800 dark:bg-slate-950"
    >
      {error && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
          {error}
        </p>
      )}

      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
        Room type
        <input
          required
          placeholder="e.g. Deluxe, Standard"
          value={form.roomType}
          onChange={(e) => update('roomType', e.target.value)}
          className={inputClassName}
        />
      </label>

      <div className="grid grid-cols-3 gap-3">
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Price/night (INR)
          <input
            type="number"
            min={0}
            required
            value={form.pricePerNight}
            onChange={(e) => update('pricePerNight', Number(e.target.value))}
            className={inputClassName}
          />
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Capacity
          <input
            type="number"
            min={1}
            required
            value={form.capacity}
            onChange={(e) => update('capacity', Number(e.target.value))}
            className={inputClassName}
          />
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Total rooms
          <input
            type="number"
            min={1}
            required
            value={form.totalRooms}
            onChange={(e) => update('totalRooms', Number(e.target.value))}
            className={inputClassName}
          />
        </label>
      </div>

      <div className="flex gap-3">
        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
        >
          {submitting ? 'Saving…' : submitLabel}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
        >
          Cancel
        </button>
      </div>
    </form>
  )
}
