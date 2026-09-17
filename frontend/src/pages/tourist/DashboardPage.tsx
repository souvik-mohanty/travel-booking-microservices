import { Link } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useMyBookingsQuery } from '@/features/bookings/api'
import { formatMoney } from '@/lib/format'
import { useAuthStore } from '@/store/authStore'

export function DashboardPage() {
  const user = useAuthStore((state) => state.user)
  const { data: bookings, isLoading } = useMyBookingsQuery()

  const counts = {
    upcoming: bookings?.filter((b) => b.status === 'PENDING').length ?? 0,
    active: bookings?.filter((b) => b.status === 'PAID').length ?? 0,
    completed: bookings?.filter((b) => b.status === 'COMPLETED').length ?? 0,
  }
  const recent = [...(bookings ?? [])].sort((a, b) => b.createdAt.localeCompare(a.createdAt)).slice(0, 5)

  return (
    <AppLayout>
      <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">Welcome back{user ? `, ${user.email.split('@')[0]}` : ''}</h1>
      <p className="mt-1 text-slate-500 dark:text-slate-400">Where will your next journey take you?</p>

      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        {[
          { label: 'Upcoming', value: counts.upcoming },
          { label: 'Active Bookings', value: counts.active },
          { label: 'Completed Trips', value: counts.completed },
        ].map((stat) => (
          <Card key={stat.label}>
            <CardContent className="px-5">
              <p className="text-sm text-slate-500 dark:text-slate-400">{stat.label}</p>
              <p className="mt-1 text-3xl font-semibold text-slate-900 dark:text-slate-100">
                {isLoading ? <Skeleton className="h-8 w-10" /> : stat.value}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="mt-8 flex items-center justify-between">
        <h2 className="font-semibold text-slate-900 dark:text-slate-100">Recent Bookings</h2>
        <Link to="/bookings" className="text-sm font-medium text-blue-600 underline dark:text-blue-400">
          View all
        </Link>
      </div>

      <div className="mt-3 space-y-3">
        {isLoading &&
          Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-16 rounded-lg" />)}

        {!isLoading && recent.length === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 p-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:text-slate-400">
            No bookings yet.{' '}
            <Link to="/tours" className="text-blue-600 underline dark:text-blue-400">
              Explore tours
            </Link>{' '}
            to plan your next journey.
          </p>
        )}

        {recent.map((booking) => (
          <Card key={booking.id} className="py-3">
            <CardContent className="flex items-center justify-between px-4">
              <div>
                <p className="font-mono text-xs text-slate-400">TF-{booking.id.slice(0, 8).toUpperCase()}</p>
                <p className="text-sm text-slate-600 dark:text-slate-400">{formatMoney(booking.totalPrice)}</p>
              </div>
              <div className="flex items-center gap-3">
                <StatusBadge status={booking.status} />
                <Button asChild size="sm" variant="outline">
                  <Link to={`/bookings/${booking.id}`}>View</Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </AppLayout>
  )
}
