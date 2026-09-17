import { useEffect, useMemo, useState } from 'react'
import { getBookingSummary, getDailyBookingStats, getStatsByTour } from '../api/analytics'
import { getAllBusinesses } from '../api/business'
import { getTour } from '../api/tours'
import { AppLayout } from '../components/AppLayout'
import { RankedBarChart, type RankedBarDatum } from '../components/charts/RankedBarChart'
import { RevenueTrendChart } from '../components/charts/RevenueTrendChart'
import { formatDate, formatMoney } from '../lib/format'
import type { BookingSummary, DailyBookingStats, TourBookingStats } from '../types/analytics'
import type { Business } from '../types/business'
import type { Tour } from '../types/tour'

export function AdminAnalyticsPage() {
  const [summary, setSummary] = useState<BookingSummary | null>(null)
  const [daily, setDaily] = useState<DailyBookingStats[]>([])
  const [byTour, setByTour] = useState<TourBookingStats[]>([])
  const [tours, setTours] = useState<Record<string, Tour>>({})
  const [businesses, setBusinesses] = useState<Business[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    Promise.all([getBookingSummary(), getDailyBookingStats(), getStatsByTour(), getAllBusinesses()])
      .then(async ([summaryData, dailyData, byTourData, businessData]) => {
        setSummary(summaryData)
        setDaily(dailyData)
        setByTour(byTourData)
        setBusinesses(businessData)

        const resolved = await Promise.all(
          byTourData.map((row) => getTour(row.tourId).catch(() => null)),
        )
        setTours(
          Object.fromEntries(
            resolved.filter((t): t is Tour => t !== null).map((t) => [t.id, t]),
          ),
        )
      })
      .catch(() => setError('Could not load analytics. Is analytics-service running?'))
      .finally(() => setLoading(false))
  }, [])

  const topTours: RankedBarDatum[] = useMemo(
    () =>
      [...byTour]
        .sort((a, b) => b.revenue - a.revenue)
        .slice(0, 8)
        .map((row) => ({
          id: row.tourId,
          label: tours[row.tourId]?.title ?? row.tourId.slice(0, 8) + '…',
          value: row.revenue,
        })),
    [byTour, tours],
  )

  const topBusinesses: RankedBarDatum[] = useMemo(() => {
    const revenueByOwner = new Map<string, number>()
    for (const row of byTour) {
      const ownerId = tours[row.tourId]?.createdBy
      if (!ownerId) continue
      revenueByOwner.set(ownerId, (revenueByOwner.get(ownerId) ?? 0) + row.revenue)
    }

    return [...revenueByOwner.entries()]
      .map(([ownerId, revenue]) => ({
        id: ownerId,
        label: businesses.find((b) => b.ownerId === ownerId)?.name ?? ownerId.slice(0, 8) + '…',
        value: revenue,
      }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 8)
  }, [byTour, tours, businesses])

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">Analytics</h1>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="mb-4 text-sm text-red-600 dark:text-red-400">{error}</p>}

      {!loading && !error && summary && (
        <>
          <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <StatTile label="Total bookings" value={summary.totalBookings.toLocaleString('en-IN')} />
            <StatTile label="Total revenue" value={formatMoney(summary.totalRevenue)} />
          </div>

          <div className="mb-8 rounded-lg border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
            <h2 className="mb-3 text-lg font-semibold text-slate-900 dark:text-slate-100">Revenue trend</h2>
            <RevenueTrendChart data={daily} />
          </div>

          <div className="mb-8 grid grid-cols-1 gap-4 lg:grid-cols-2">
            <div className="rounded-lg border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
              <h2 className="mb-3 text-lg font-semibold text-slate-900 dark:text-slate-100">
                Top tours by revenue
              </h2>
              <RankedBarChart data={topTours} />
            </div>
            <div className="rounded-lg border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
              <h2 className="mb-3 text-lg font-semibold text-slate-900 dark:text-slate-100">
                Top businesses by revenue
              </h2>
              <RankedBarChart data={topBusinesses} />
            </div>
          </div>

          <div className="mb-8">
            <h2 className="mb-3 text-lg font-semibold text-slate-900 dark:text-slate-100">
              Daily bookings (last 30 days)
            </h2>
            {daily.length === 0 ? (
              <p className="text-sm text-slate-500 dark:text-slate-400">No bookings in this range.</p>
            ) : (
              <div className="overflow-x-auto rounded-lg border border-slate-200 dark:border-slate-800">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 text-slate-500 dark:bg-slate-900 dark:text-slate-400">
                    <tr>
                      <th className="px-4 py-2 font-medium">Date</th>
                      <th className="px-4 py-2 font-medium">Bookings</th>
                      <th className="px-4 py-2 font-medium">Revenue</th>
                    </tr>
                  </thead>
                  <tbody>
                    {daily.map((row) => (
                      <tr key={row.date} className="border-t border-slate-200 dark:border-slate-800">
                        <td className="px-4 py-2 text-slate-700 dark:text-slate-300">{formatDate(row.date)}</td>
                        <td className="px-4 py-2 tabular-nums text-slate-700 dark:text-slate-300">
                          {row.bookingCount}
                        </td>
                        <td className="px-4 py-2 tabular-nums text-slate-700 dark:text-slate-300">
                          {formatMoney(row.revenue)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <div>
            <h2 className="mb-3 text-lg font-semibold text-slate-900 dark:text-slate-100">Top tours by revenue</h2>
            {byTour.length === 0 ? (
              <p className="text-sm text-slate-500 dark:text-slate-400">No bookings yet.</p>
            ) : (
              <div className="overflow-x-auto rounded-lg border border-slate-200 dark:border-slate-800">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 text-slate-500 dark:bg-slate-900 dark:text-slate-400">
                    <tr>
                      <th className="px-4 py-2 font-medium">Tour</th>
                      <th className="px-4 py-2 font-medium">Destination</th>
                      <th className="px-4 py-2 font-medium">Bookings</th>
                      <th className="px-4 py-2 font-medium">Revenue</th>
                    </tr>
                  </thead>
                  <tbody>
                    {byTour.map((row) => (
                      <tr key={row.tourId} className="border-t border-slate-200 dark:border-slate-800">
                        <td className="px-4 py-2 text-slate-700 dark:text-slate-300">
                          {tours[row.tourId]?.title ?? row.tourId.slice(0, 8) + '…'}
                        </td>
                        <td className="px-4 py-2 text-slate-700 dark:text-slate-300">
                          {tours[row.tourId]?.destination ?? '—'}
                        </td>
                        <td className="px-4 py-2 tabular-nums text-slate-700 dark:text-slate-300">
                          {row.bookingCount}
                        </td>
                        <td className="px-4 py-2 tabular-nums text-slate-700 dark:text-slate-300">
                          {formatMoney(row.revenue)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </AppLayout>
  )
}

function StatTile({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
      <p className="text-sm text-slate-500 dark:text-slate-400">{label}</p>
      <p className="mt-1 text-3xl font-semibold text-slate-900 dark:text-slate-100">{value}</p>
    </div>
  )
}
