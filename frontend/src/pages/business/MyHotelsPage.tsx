import { useState } from 'react'
import { HotelFormDialog } from '@/components/business/HotelFormDialog'
import { RoomManager } from '@/components/business/RoomManager'
import { StatusBadge } from '@/components/common/StatusBadge'
import { AppLayout } from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useAllHotelsQuery } from '@/features/hotels/api'
import { useAuthStore } from '@/store/authStore'
import type { Hotel } from '@/types/hotel'

function HotelCard({ hotel }: { hotel: Hotel }) {
  const [showRooms, setShowRooms] = useState(false)
  const location = [hotel.address, hotel.city, hotel.state, hotel.country].filter(Boolean).join(', ')

  return (
    <Card className="animate-fade-in-up py-4">
      <CardContent className="space-y-4 px-5">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <div className="flex items-center gap-3">
              <h2 className="font-medium text-slate-900 dark:text-slate-100">{hotel.name}</h2>
              <StatusBadge status={hotel.status} />
            </div>
            {location && <p className="mt-0.5 text-sm text-slate-500 dark:text-slate-400">{location}</p>}
            {hotel.description && <p className="mt-2 max-w-2xl text-sm text-slate-600 dark:text-slate-300">{hotel.description}</p>}
          </div>
          <div className="flex gap-2">
            <HotelFormDialog
              hotel={hotel}
              trigger={
                <Button size="sm" variant="outline">
                  Edit
                </Button>
              }
            />
            <Button size="sm" onClick={() => setShowRooms((current) => !current)}>
              {showRooms ? 'Hide rooms' : 'Manage rooms'}
            </Button>
          </div>
        </div>

        {hotel.status === 'SUSPENDED' && (
          <p className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
            An administrator has suspended this hotel, so its rooms can&apos;t be booked until it is reinstated.
          </p>
        )}

        {showRooms && <RoomManager hotelId={hotel.id} />}
      </CardContent>
    </Card>
  )
}

export function MyHotelsPage() {
  const user = useAuthStore((state) => state.user)
  const { data: hotels, isLoading, isError, refetch } = useAllHotelsQuery()

  // No "my hotels" endpoint exists, so filter all hotels by owner.
  const myHotels = (hotels ?? []).filter((hotel) => hotel.ownerId === user?.userId)

  return (
    <AppLayout>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">My Hotels</h1>
        <HotelFormDialog
          trigger={<Button className="transition-all duration-200 hover:scale-[1.02] active:scale-95">Add hotel</Button>}
        />
      </div>

      {isLoading && (
        <div className="space-y-3">
          {Array.from({ length: 2 }).map((_, i) => (
            <Skeleton key={i} className="h-28 rounded-lg" />
          ))}
        </div>
      )}

      {isError && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Could not load your hotels.{' '}
          <button onClick={() => refetch()} className="font-medium underline">
            Try again
          </button>
        </div>
      )}

      {!isLoading && !isError && myHotels.length === 0 && (
        <p className="rounded-lg border border-dashed border-slate-300 p-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:text-slate-400">
          You haven&apos;t added any hotels yet.
        </p>
      )}

      <div className="space-y-3">
        {myHotels.map((hotel) => (
          <HotelCard key={hotel.id} hotel={hotel} />
        ))}
      </div>
    </AppLayout>
  )
}
