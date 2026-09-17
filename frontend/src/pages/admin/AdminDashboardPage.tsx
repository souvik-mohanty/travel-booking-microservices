import { useMemo } from 'react'
import { AppLayout } from '@/components/layout/AppLayout'
import { RankedBarChart } from '@/components/charts/RankedBarChart'
import { RevenueTrendChart } from '@/components/charts/RevenueTrendChart'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useBookingSummaryQuery, useDailyBookingStatsQuery, useStatsByTourQuery } from '@/features/analytics/api'
import { useAllToursQuery } from '@/features/tours/api'
import { formatMoney } from '@/lib/format'

export function AdminDashboardPage() {
  const { data: summary, isLoading: summaryLoading } = useBookingSummaryQuery()
  const { data: daily, isLoading: dailyLoading } = useDailyBookingStatsQuery()
  const { data: byTour, isLoading: byTourLoading } = useStatsByTourQuery()
  const { data: tours } = useAllToursQuery()

  const tourTitleById = useMemo(() => new Map((tours ?? []).map((tour) => [tour.id, tour.title])), [tours])

  const topTours = useMemo(
    () =>
      [...(byTour ?? [])]
        .sort((a, b) => b.revenue - a.revenue)
        .slice(0, 8)
        .map((stat) => ({
          id: stat.tourId,
          label: tourTitleById.get(stat.tourId) ?? `Tour ${stat.tourId.slice(0, 8)}`,
          value: stat.revenue,
        })),
    [byTour, tourTitleById],
  )

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">Admin Dashboard</h1>

      <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Card>
          <CardContent className="px-5">
            <p className="text-sm text-slate-500 dark:text-slate-400">Total Bookings</p>
            <p className="mt-1 text-3xl font-semibold text-slate-900 dark:text-slate-100">
              {summaryLoading ? <Skeleton className="h-8 w-16" /> : summary?.totalBookings}
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="px-5">
            <p className="text-sm text-slate-500 dark:text-slate-400">Total Revenue</p>
            <p className="mt-1 text-3xl font-semibold text-slate-900 dark:text-slate-100">
              {summaryLoading ? <Skeleton className="h-8 w-24" /> : formatMoney(summary?.totalRevenue ?? 0)}
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <CardContent className="px-5">
            <h2 className="mb-4 font-medium text-slate-900 dark:text-slate-100">Revenue Trend (last 30 days)</h2>
            {dailyLoading ? <Skeleton className="h-64 rounded-lg" /> : <RevenueTrendChart data={daily ?? []} />}
          </CardContent>
        </Card>
        <Card>
          <CardContent className="px-5">
            <h2 className="mb-4 font-medium text-slate-900 dark:text-slate-100">Top Tours by Revenue</h2>
            {byTourLoading ? <Skeleton className="h-64 rounded-lg" /> : <RankedBarChart data={topTours} />}
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
