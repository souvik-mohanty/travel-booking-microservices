import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useMyBookingsQuery } from '@/features/bookings/api'
import { formatMoney } from '@/lib/format'
import type { Booking, BookingStatus } from '@/types/booking'

const TABS: { value: BookingStatus; label: string }[] = [
  { value: 'PENDING', label: 'Upcoming' },
  { value: 'PAID', label: 'Active' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' },
]

export function MyBookingsPage() {
  const { data: bookings, isLoading } = useMyBookingsQuery()

  const byStatus = useMemo(() => {
    const map = new Map<BookingStatus, Booking[]>()
    for (const tab of TABS) map.set(tab.value, [])
    for (const booking of bookings ?? []) {
      map.get(booking.status)?.push(booking)
    }
    for (const list of map.values()) {
      list.sort((a, b) => b.createdAt.localeCompare(a.createdAt))
    }
    return map
  }, [bookings])

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">My Bookings</h1>

      {isLoading && (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-20 rounded-lg" />
          ))}
        </div>
      )}

      {!isLoading && (
        <Tabs defaultValue="PENDING">
          <TabsList>
            {TABS.map((tab) => (
              <TabsTrigger key={tab.value} value={tab.value}>
                {tab.label} ({byStatus.get(tab.value)?.length ?? 0})
              </TabsTrigger>
            ))}
          </TabsList>

          {TABS.map((tab) => (
            <TabsContent key={tab.value} value={tab.value} className="mt-4 space-y-3">
              {(byStatus.get(tab.value)?.length ?? 0) === 0 && (
                <p className="rounded-lg border border-dashed border-slate-300 p-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:text-slate-400">
                  No bookings here yet.{' '}
                  <Link to="/tours" className="text-blue-600 underline dark:text-blue-400">
                    Explore tours
                  </Link>{' '}
                  to plan your next journey.
                </p>
              )}
              {byStatus.get(tab.value)?.map((booking) => (
                <Card key={booking.id} className="animate-fade-in-up py-4">
                  <CardContent className="flex items-center justify-between gap-4 px-5">
                    <div>
                      <p className="font-mono text-xs text-slate-400">TF-{booking.id.slice(0, 8).toUpperCase()}</p>
                      <p className="text-sm text-slate-600 dark:text-slate-400">
                        {booking.numberOfParticipants} traveler{booking.numberOfParticipants === 1 ? '' : 's'} ·{' '}
                        {formatMoney(booking.totalPrice)}
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      <StatusBadge status={booking.status} />
                      <Button asChild size="sm" variant="outline">
                        <Link to={booking.status === 'PENDING' ? `/bookings/${booking.id}/pay` : `/bookings/${booking.id}`}>
                          {booking.status === 'PENDING' ? 'Pay now' : 'View'}
                        </Link>
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </TabsContent>
          ))}
        </Tabs>
      )}
    </AppLayout>
  )
}
