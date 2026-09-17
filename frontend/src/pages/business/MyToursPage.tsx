import { Link } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useAllToursQuery, useCancelTourMutation, usePublishTourMutation } from '@/features/tours/api'
import { formatDateRange, formatMoney } from '@/lib/format'
import { useAuthStore } from '@/store/authStore'

// catalog-service's GET /api/tours returns every tour system-wide -- there's
// no "mine" endpoint, so ownership filtering happens client-side against
// createdBy, same as the pre-rewrite app did.
export function MyToursPage() {
  const user = useAuthStore((state) => state.user)
  const { data: tours, isLoading } = useAllToursQuery()
  const publishTour = usePublishTourMutation()
  const cancelTour = useCancelTourMutation()

  const myTours = (tours ?? []).filter((tour) => tour.createdBy === user?.userId)

  return (
    <AppLayout>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">My Tours</h1>
        <Button asChild className="transition-all duration-200 hover:scale-[1.02] active:scale-95">
          <Link to="/tours/new">Create a tour</Link>
        </Button>
      </div>

      {isLoading && (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-24 rounded-lg" />
          ))}
        </div>
      )}

      {!isLoading && myTours.length === 0 && (
        <p className="rounded-lg border border-dashed border-slate-300 p-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:text-slate-400">
          You haven't created any tours yet.
        </p>
      )}

      <div className="space-y-3">
        {myTours.map((tour) => (
          <Card key={tour.id} className="animate-fade-in-up py-4">
            <CardContent className="flex items-center justify-between gap-4 px-5">
              <div>
                <Link to={`/tours/${tour.id}`} className="font-medium text-slate-900 hover:text-blue-700 dark:text-slate-100">
                  {tour.title}
                </Link>
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  {tour.destination} · {formatDateRange(tour.startDate, tour.endDate)} · {formatMoney(tour.price)}
                </p>
              </div>
              <div className="flex items-center gap-3">
                <StatusBadge status={tour.status} />
                {tour.status === 'DRAFT' && (
                  <Button
                    size="sm"
                    onClick={() => publishTour.mutate(tour.id)}
                    disabled={publishTour.isPending}
                    className="transition-all duration-200 hover:scale-[1.02] active:scale-95"
                  >
                    Publish
                  </Button>
                )}
                {tour.status !== 'CANCELLED' && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => cancelTour.mutate(tour.id)}
                    disabled={cancelTour.isPending}
                  >
                    Cancel
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </AppLayout>
  )
}
