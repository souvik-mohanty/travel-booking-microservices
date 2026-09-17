import type { CSSProperties } from 'react'
import { MapPin, Users } from 'lucide-react'
import { Link } from 'react-router-dom'
import { Card, CardContent } from '@/components/ui/card'
import { formatDateRange, formatMoney } from '@/lib/format'
import type { TourSearchResult } from '@/types/search'
import type { Tour } from '@/types/tour'

// Accepts either a full Tour (owner/business views) or a lighter
// TourSearchResult (public discovery, search-service-backed) -- both carry
// the same display fields.
type TourCardData = Pick<
  Tour | TourSearchResult,
  'id' | 'title' | 'destination' | 'startDate' | 'endDate' | 'price' | 'maxParticipants'
>

export function TourCard({ tour, index = 0 }: { tour: TourCardData; index?: number }) {
  return (
    <Link
      to={`/tours/${tour.id}`}
      style={{ '--tf-delay': index % 9 } as CSSProperties}
      className="animate-fade-in-up animate-stagger group block h-full"
    >
      <Card className="h-full gap-3 py-4 transition-all duration-200 hover:-translate-y-1 hover:border-blue-400 hover:shadow-lg dark:hover:border-blue-500">
        <CardContent className="flex h-full flex-col px-4">
          <h3 className="font-semibold text-slate-900 group-hover:text-blue-700 dark:text-slate-100 dark:group-hover:text-blue-400">
            {tour.title}
          </h3>
          <p className="mt-1 flex items-center gap-1 text-sm text-slate-500 dark:text-slate-400">
            <MapPin className="size-3.5" />
            {tour.destination}
          </p>
          <p className="mt-1 text-xs text-slate-400 dark:text-slate-500">
            {formatDateRange(tour.startDate, tour.endDate)}
          </p>
          <p className="mt-1 flex items-center gap-1 text-xs text-slate-400 dark:text-slate-500">
            <Users className="size-3.5" />
            Up to {tour.maxParticipants} travelers
          </p>
          <div className="mt-auto flex items-end justify-between pt-3">
            <div>
              <span className="text-xs text-slate-400">From</span>
              <p className="font-semibold text-slate-900 dark:text-slate-100">{formatMoney(tour.price)}</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </Link>
  )
}
