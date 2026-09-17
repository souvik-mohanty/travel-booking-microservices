import { useMemo, type CSSProperties } from 'react'
import { MapPin } from 'lucide-react'
import { Link } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useTourSearchQuery } from '@/features/search/api'

// There is no first-class "Destination" entity anywhere in the backend --
// a Tour only has a plain `destination` string field. This page is a
// client-side aggregation over real, published tours (search-service),
// grouped by that string -- not a separate backend resource. No destination
// images are shown because no image field exists on Tour/TourSearchResult
// anywhere in the API.
export function DestinationsPage() {
  const { data, isLoading } = useTourSearchQuery({ size: 100 })

  const destinations = useMemo(() => {
    const counts = new Map<string, number>()
    for (const tour of data?.results ?? []) {
      counts.set(tour.destination, (counts.get(tour.destination) ?? 0) + 1)
    }
    return Array.from(counts.entries())
      .map(([destination, tourCount]) => ({ destination, tourCount }))
      .sort((a, b) => b.tourCount - a.tourCount)
  }, [data])

  return (
    <AppLayout>
      <h1 className="mb-2 text-2xl font-semibold text-slate-900 dark:text-slate-100">Destinations</h1>
      <p className="mb-6 text-sm text-slate-500 dark:text-slate-400">
        Grouped from currently published tours.
      </p>

      {isLoading && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-28 rounded-lg" />
          ))}
        </div>
      )}

      {!isLoading && destinations.length === 0 && (
        <p className="rounded-lg border border-dashed border-slate-300 p-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:text-slate-400">
          No published tours yet, so no destinations to show.
        </p>
      )}

      {!isLoading && destinations.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {destinations.map(({ destination, tourCount }, i) => (
            <Link
              key={destination}
              to={`/tours?destination=${encodeURIComponent(destination)}`}
              style={{ '--tf-delay': i % 9 } as CSSProperties}
              className="animate-fade-in-up animate-stagger group block"
            >
              <Card className="gap-2 py-4 transition-all duration-200 hover:-translate-y-1 hover:border-blue-400 hover:shadow-lg dark:hover:border-blue-500">
                <CardContent className="flex items-center gap-4 px-4">
                  <div className="tf-gradient-bar flex size-12 shrink-0 items-center justify-center rounded-lg text-white">
                    <MapPin className="size-6" />
                  </div>
                  <div>
                    <h2 className="font-semibold text-slate-900 group-hover:text-blue-700 dark:text-slate-100 dark:group-hover:text-blue-400">
                      {destination}
                    </h2>
                    <p className="text-sm text-slate-500 dark:text-slate-400">
                      {tourCount} tour{tourCount === 1 ? '' : 's'} available
                    </p>
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </AppLayout>
  )
}
