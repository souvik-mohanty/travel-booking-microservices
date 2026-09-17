import { useState } from 'react'
import { Printer } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useBookingQuery, useCancelBookingMutation } from '@/features/bookings/api'
import { useTourQuery } from '@/features/tours/api'
import { formatDateRange, formatDateTime, formatMoney } from '@/lib/format'

// No invoice-generation endpoint exists in booking- or payment-service --
// this renders a receipt from real booking/tour data and hands off to the
// browser's own print dialog, rather than faking a backend-generated PDF.
export function BookingDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { data: booking, isLoading } = useBookingQuery(id)
  const { data: tour } = useTourQuery(booking?.tourId)
  const cancelBooking = useCancelBookingMutation()
  const [cancelling, setCancelling] = useState(false)

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

  const canCancel = booking.status === 'PENDING' || booking.status === 'PAID'

  async function handleCancel() {
    if (!booking || !confirm('Cancel this booking?')) return
    setCancelling(true)
    try {
      await cancelBooking.mutateAsync(booking.id)
    } finally {
      setCancelling(false)
    }
  }

  return (
    <AppLayout>
      <div className="mx-auto max-w-lg">
        <div className="mb-6 flex items-center justify-between print:hidden">
          <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">Booking Details</h1>
          <Button variant="outline" size="sm" onClick={() => window.print()} className="gap-2">
            <Printer className="size-4" />
            Print receipt
          </Button>
        </div>

        <Card>
          <CardContent className="space-y-3 px-5 text-sm">
            <div className="flex justify-between">
              <span className="text-slate-500 dark:text-slate-400">Booking ID</span>
              <span className="font-mono text-slate-900 dark:text-slate-100">
                TF-{booking.id.slice(0, 8).toUpperCase()}
              </span>
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
                  <span className="text-slate-500 dark:text-slate-400">Travel dates</span>
                  <span className="text-slate-900 dark:text-slate-100">
                    {formatDateRange(tour.startDate, tour.endDate)}
                  </span>
                </div>
              </>
            )}
            <div className="flex justify-between">
              <span className="text-slate-500 dark:text-slate-400">Booked on</span>
              <span className="text-slate-900 dark:text-slate-100">{formatDateTime(booking.createdAt)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500 dark:text-slate-400">Travelers</span>
              <span className="text-slate-900 dark:text-slate-100">{booking.numberOfParticipants}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500 dark:text-slate-400">Booking status</span>
              <StatusBadge status={booking.status} />
            </div>
            <div className="flex justify-between border-t border-slate-100 pt-3 text-base font-semibold dark:border-slate-800">
              <span className="text-slate-900 dark:text-slate-100">Total</span>
              <span className="text-slate-900 dark:text-slate-100">{formatMoney(booking.totalPrice)}</span>
            </div>
          </CardContent>
        </Card>

        <div className="mt-6 flex flex-wrap gap-3 print:hidden">
          {booking.status === 'PENDING' && (
            <Button asChild className="transition-all duration-200 hover:scale-[1.02] active:scale-95">
              <Link to={`/bookings/${booking.id}/pay`}>Complete payment</Link>
            </Button>
          )}
          {canCancel && (
            <Button variant="outline" onClick={handleCancel} disabled={cancelling}>
              {cancelling ? 'Cancelling…' : 'Cancel Booking'}
            </Button>
          )}
          <Button asChild variant="ghost">
            <Link to="/bookings">Back to My Bookings</Link>
          </Button>
        </div>
      </div>
    </AppLayout>
  )
}
