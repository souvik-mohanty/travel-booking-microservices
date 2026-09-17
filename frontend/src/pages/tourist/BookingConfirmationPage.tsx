import { CheckCircle2 } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useBookingQuery } from '@/features/bookings/api'
import { useTourQuery } from '@/features/tours/api'
import { formatDateRange, formatMoney } from '@/lib/format'

export function BookingConfirmationPage() {
  const { id } = useParams<{ id: string }>()
  const { data: booking, isLoading } = useBookingQuery(id)
  const { data: tour } = useTourQuery(booking?.tourId)

  if (isLoading) {
    return (
      <AppLayout>
        <Skeleton className="h-64 rounded-lg" />
      </AppLayout>
    )
  }

  if (!booking) {
    return (
      <AppLayout>
        <p className="text-sm text-red-600 dark:text-red-400">Booking not found.</p>
      </AppLayout>
    )
  }

  const isPaid = booking.status === 'PAID' || booking.status === 'COMPLETED'

  return (
    <AppLayout>
      <div className="mx-auto max-w-lg text-center">
        <div
          className={`animate-scale-in mx-auto mb-4 flex size-16 items-center justify-center rounded-full ${
            isPaid ? 'bg-green-100 text-green-600 dark:bg-green-950 dark:text-green-400' : 'bg-amber-100 text-amber-600'
          }`}
        >
          <CheckCircle2 className="size-9" />
        </div>
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">
          {isPaid ? 'Booking Confirmed' : 'Payment processing'}
        </h1>
        <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
          {isPaid
            ? 'Payment received — enjoy your trip!'
            : "We're still confirming your payment. Refresh in a moment if this doesn't update."}
        </p>

        <Card className="mt-6 text-left">
          <CardContent className="space-y-2 px-5 text-sm">
            <div className="flex justify-between">
              <span className="text-slate-500 dark:text-slate-400">Booking ID</span>
              <span className="font-mono text-slate-900 dark:text-slate-100">TF-{booking.id.slice(0, 8).toUpperCase()}</span>
            </div>
            {tour && (
              <>
                <div className="flex justify-between">
                  <span className="text-slate-500 dark:text-slate-400">Tour</span>
                  <span className="text-slate-900 dark:text-slate-100">{tour.title}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500 dark:text-slate-400">Destination</span>
                  <span className="text-slate-900 dark:text-slate-100">{tour.destination}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500 dark:text-slate-400">Dates</span>
                  <span className="text-slate-900 dark:text-slate-100">
                    {formatDateRange(tour.startDate, tour.endDate)}
                  </span>
                </div>
              </>
            )}
            <div className="flex justify-between">
              <span className="text-slate-500 dark:text-slate-400">Travelers</span>
              <span className="text-slate-900 dark:text-slate-100">{booking.numberOfParticipants}</span>
            </div>
            <div className="flex justify-between border-t border-slate-100 pt-2 font-semibold dark:border-slate-800">
              <span className="text-slate-900 dark:text-slate-100">Amount Paid</span>
              <span className="text-slate-900 dark:text-slate-100">{formatMoney(booking.totalPrice)}</span>
            </div>
          </CardContent>
        </Card>

        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <Button asChild variant="outline">
            <Link to={`/bookings/${booking.id}`}>View Booking</Link>
          </Button>
          <Button asChild className="transition-all duration-200 hover:scale-[1.02] active:scale-95">
            <Link to="/tours">Explore More Tours</Link>
          </Button>
        </div>
      </div>
    </AppLayout>
  )
}
