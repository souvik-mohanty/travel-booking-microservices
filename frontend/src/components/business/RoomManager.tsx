import { useState, type ReactNode } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { ConfirmDialog } from '@/components/common/ConfirmDialog'
import { StatusBadge } from '@/components/common/StatusBadge'
import { TextField } from '@/components/forms/TextField'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Form } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import {
  fetchRoomAvailability,
  useActivateRoomMutation,
  useCreateRoomMutation,
  useDeactivateRoomMutation,
  useDeleteRoomMutation,
  useRoomsForHotelQuery,
  useUpdateRoomMutation,
} from '@/features/hotels/api'
import { runAction } from '@/lib/actions'
import { apiErrorMessage } from '@/lib/errors'
import { formatMoney } from '@/lib/format'
import type { Room, RoomAvailability, RoomFormValues } from '@/types/hotel'

// Form values stay strings (what <input type="number"> hands react-hook-form)
// and are converted when submitting.
const roomSchema = z.object({
  roomType: z.string().trim().min(1, 'Room type is required').max(100),
  pricePerNight: z
    .string()
    .min(1, 'Price is required')
    .refine((v) => Number(v) >= 0, 'Price cannot be negative'),
  capacity: z
    .string()
    .min(1, 'Required')
    .refine((v) => Number.isInteger(Number(v)) && Number(v) >= 1, 'At least 1 guest'),
  totalRooms: z
    .string()
    .min(1, 'Required')
    .refine((v) => Number.isInteger(Number(v)) && Number(v) >= 1, 'At least 1 room'),
})

type RoomFormInput = z.infer<typeof roomSchema>

function toFormInput(room?: Room): RoomFormInput {
  return {
    roomType: room?.roomType ?? '',
    pricePerNight: room ? String(room.pricePerNight) : '',
    capacity: room ? String(room.capacity) : '2',
    totalRooms: room ? String(room.totalRooms) : '1',
  }
}

function toRequest(values: RoomFormInput): RoomFormValues {
  return {
    roomType: values.roomType.trim(),
    pricePerNight: Number(values.pricePerNight),
    capacity: Number(values.capacity),
    totalRooms: Number(values.totalRooms),
  }
}

function RoomFormDialog({ hotelId, room, trigger }: { hotelId: string; room?: Room; trigger: ReactNode }) {
  const [open, setOpen] = useState(false)
  const createRoom = useCreateRoomMutation(hotelId)
  const updateRoom = useUpdateRoomMutation(hotelId)
  const form = useForm<RoomFormInput>({ resolver: zodResolver(roomSchema), defaultValues: toFormInput(room) })
  const saving = createRoom.isPending || updateRoom.isPending

  async function onSubmit(values: RoomFormInput) {
    const request = toRequest(values)
    try {
      if (room) {
        await runAction(() => updateRoom.mutateAsync({ id: room.id, request }), 'Room updated.')
      } else {
        await runAction(() => createRoom.mutateAsync({ hotelId, request }), 'Room added (as a draft until you activate it).')
      }
      setOpen(false)
    } catch {
      // runAction already showed the error toast; keep the dialog open.
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        setOpen(next)
        if (next) form.reset(toFormInput(room))
      }}
    >
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{room ? 'Edit room' : 'Add a room'}</DialogTitle>
          <DialogDescription>
            {room ? 'Update this room type.' : 'A room type is a group of identical rooms (e.g. "Deluxe double").'}
          </DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <TextField control={form.control} name="roomType" label="Room type" placeholder="Deluxe double" />
            <div className="grid gap-4 sm:grid-cols-3">
              <TextField control={form.control} name="pricePerNight" label="Price / night (₹)" type="number" min={0} step="0.01" />
              <TextField control={form.control} name="capacity" label="Guests" type="number" min={1} />
              <TextField control={form.control} name="totalRooms" label="Rooms" type="number" min={1} />
            </div>
            <Button type="submit" disabled={saving} className="w-full">
              {saving ? 'Saving…' : room ? 'Save changes' : 'Add room'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}

function AvailabilityDialog({ room, trigger }: { room: Room; trigger: ReactNode }) {
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [result, setResult] = useState<RoomAvailability | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [checking, setChecking] = useState(false)

  async function check() {
    setError(null)
    setResult(null)
    setChecking(true)
    try {
      setResult(await fetchRoomAvailability(room.id, startDate, endDate))
    } catch (err) {
      setError(apiErrorMessage(err, 'Could not check availability.'))
    } finally {
      setChecking(false)
    }
  }

  return (
    <Dialog>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Availability: {room.roomType}</DialogTitle>
          <DialogDescription>See how many of these rooms are still free between two dates.</DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label htmlFor={`avail-start-${room.id}`}>From</Label>
            <Input id={`avail-start-${room.id}`} type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor={`avail-end-${room.id}`}>To</Label>
            <Input id={`avail-end-${room.id}`} type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
          </div>
        </div>
        <Button onClick={check} disabled={!startDate || !endDate || endDate < startDate || checking}>
          {checking ? 'Checking…' : 'Check availability'}
        </Button>
        {endDate && startDate && endDate < startDate && (
          <p className="text-sm text-red-600">The end date must be on or after the start date.</p>
        )}
        {error && <p className="text-sm text-red-600">{error}</p>}
        {result && (
          <p className="rounded-md bg-slate-50 p-3 text-sm text-slate-700">
            <strong>{result.availableRooms}</strong> of {result.totalRooms} rooms available ({result.reservedRooms} reserved).
          </p>
        )}
      </DialogContent>
    </Dialog>
  )
}

// Rooms of one hotel: list, add, edit, activate/deactivate, delete, availability.
export function RoomManager({ hotelId }: { hotelId: string }) {
  const { data: rooms, isLoading, isError, refetch } = useRoomsForHotelQuery(hotelId)
  const activateRoom = useActivateRoomMutation(hotelId)
  const deactivateRoom = useDeactivateRoomMutation(hotelId)
  const deleteRoom = useDeleteRoomMutation(hotelId)

  const visibleRooms = (rooms ?? []).filter((room) => room.status !== 'DELETED')

  return (
    <div className="space-y-3 border-t border-slate-100 pt-4 dark:border-slate-800">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Rooms</h3>
        <RoomFormDialog
          hotelId={hotelId}
          trigger={
            <Button size="sm" variant="outline">
              Add room
            </Button>
          }
        />
      </div>

      {isLoading && <Skeleton className="h-16 rounded-md" />}

      {isError && (
        <p className="text-sm text-red-600">
          Could not load rooms.{' '}
          <button onClick={() => refetch()} className="font-medium underline">
            Try again
          </button>
        </p>
      )}

      {!isLoading && !isError && visibleRooms.length === 0 && (
        <p className="text-sm text-slate-500 dark:text-slate-400">No rooms yet. Add the first room type for this hotel.</p>
      )}

      {visibleRooms.length > 0 && (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Room type</TableHead>
              <TableHead>Price / night</TableHead>
              <TableHead>Guests</TableHead>
              <TableHead>Rooms</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {visibleRooms.map((room) => (
              <TableRow key={room.id}>
                <TableCell className="font-medium">{room.roomType}</TableCell>
                <TableCell>{formatMoney(room.pricePerNight)}</TableCell>
                <TableCell>{room.capacity}</TableCell>
                <TableCell>{room.totalRooms}</TableCell>
                <TableCell>
                  <StatusBadge status={room.status} />
                </TableCell>
                <TableCell>
                  <div className="flex flex-wrap justify-end gap-2">
                    {room.status === 'ACTIVE' ? (
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => runAction(() => deactivateRoom.mutateAsync(room.id), 'Room deactivated.').catch(() => {})}
                        disabled={deactivateRoom.isPending}
                      >
                        Deactivate
                      </Button>
                    ) : (
                      <Button
                        size="sm"
                        onClick={() => runAction(() => activateRoom.mutateAsync(room.id), 'Room activated.').catch(() => {})}
                        disabled={activateRoom.isPending}
                      >
                        Activate
                      </Button>
                    )}
                    <AvailabilityDialog
                      room={room}
                      trigger={
                        <Button size="sm" variant="outline">
                          Availability
                        </Button>
                      }
                    />
                    <RoomFormDialog
                      hotelId={hotelId}
                      room={room}
                      trigger={
                        <Button size="sm" variant="outline">
                          Edit
                        </Button>
                      }
                    />
                    <ConfirmDialog
                      destructive
                      title={`Delete “${room.roomType}”?`}
                      description="The room type will be removed. Rooms already reserved for tours are not affected, but you won't be able to reserve it again."
                      confirmLabel="Delete room"
                      onConfirm={() => runAction(() => deleteRoom.mutateAsync(room.id), 'Room deleted.')}
                      trigger={
                        <Button size="sm" variant="outline" className="text-red-600 hover:text-red-700">
                          Delete
                        </Button>
                      }
                    />
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  )
}
