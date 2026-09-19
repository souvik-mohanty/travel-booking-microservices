import { useState } from 'react'
import { isAxiosError } from 'axios'
import { CalendarDays, MapPin, Users } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { useCreateBookingMutation } from '@/features/bookings/api'
import { useTourQuery } from '@/features/tours/api'
import { formatDateRange, formatMoney } from '@/lib/format'
import { useAuthStore } from '@/store/authStore'

export function TourDetailPage() {
  const { id } = useParams<{ id: string }>()
  const user = useAuthStore((state) => state.user)
  const navigate = useNavigate()
  const { data: tour, isLoading, isError } = useTourQuery(id)
  const createBooking = useCreateBookingMutation()

  const [participants, setParticipants] = useState(1)
  const [bookingError, setBookingError] = useState<string | null>(null)

  if (isLoading) {
    return (
      <AppLayout>
        <Skeleton className="h-64 rounded-lg" />
      </AppLayout>
    )
  }

  if (isError || !tour) {
    return (
      <AppLayout>
        <p className="text-sm text-red-600 dark:text-red-400">Tour not found.</p>
      </AppLayout>
    )
  }

  const isOwner = user?.userId === tour.createdBy
  // Booking is for tourists only (booking-service enforces it too).
  const canBook = user?.role === 'TOURIST' && !isOwner && tour.status === 'PUBLISHED'
  const total = tour.price * participants

  async function handleBook() {
    if (!tour) return
    setBookingError(null)
    try {
      const booking = await createBooking.mutateAsync({ tourId: tour.id, numberOfParticipants: participants })
      navigate(`/bookings/${booking.id}/pay`)
    } catch (err) {
      setBookingError(
        isAxiosError(err) ? (err.response?.data as string) || 'Could not create booking.' : 'Could not create booking.',
      )
    }
  }

  return (
    <AppLayout>
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <div className="mb-2 flex items-center gap-3">
            <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">{tour.title}</h1>
            <StatusBadge status={tour.status} />
          </div>
          <p className="flex items-center gap-1 text-sm text-slate-500 dark:text-slate-400">
            <MapPin className="size-4" />
            {tour.destination}
          </p>
          <p className="mt-1 flex items-center gap-1 text-sm text-slate-500 dark:text-slate-400">
            <CalendarDays className="size-4" />
            {formatDateRange(tour.startDate, tour.endDate)}
          </p>
          <p className="mt-1 flex items-center gap-1 text-sm text-slate-500 dark:text-slate-400">
            <Users className="size-4" />
            Up to {tour.maxParticipants} travelers
          </p>

          {tour.description && (
            <div className="mt-6">
              <h2 className="mb-2 font-medium text-slate-900 dark:text-slate-100">Overview</h2>
              <p className="whitespace-pre-line text-sm text-slate-600 dark:text-slate-400">{tour.description}</p>
            </div>
          )}

          {isOwner && (
            <p className="mt-6 text-sm text-slate-500 dark:text-slate-400">
              You created this tour.{' '}
              <Link to="/my-tours" className="font-medium text-blue-600 underline dark:text-blue-400">
                Manage it from My Tours
              </Link>
              .
            </p>
          )}
        </div>

        <div>
          <Card>
            <CardContent className="px-5">
              <h2 className="mb-4 font-semibold text-slate-900 dark:text-slate-100">
                {canBook ? 'Book this tour' : 'Booking'}
              </h2>

              {!canBook && !isOwner && (
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  This tour is {tour.status.toLowerCase()} and isn't open for booking right now.
                </p>
              )}

              {canBook && (
                <>
                  <Label htmlFor="participants" className="mb-1.5">
                    Travelers
                  </Label>
                  <Input
                    id="participants"
                    type="number"
                    min={1}
                    max={tour.maxParticipants}
                    value={participants}
                    onChange={(e) => setParticipants(Math.max(1, Number(e.target.value) || 1))}
                  />

                  <div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-4 dark:border-slate-800">
                    <span className="text-sm text-slate-500 dark:text-slate-400">Total</span>
                    <span className="text-lg font-semibold text-slate-900 dark:text-slate-100">
                      {formatMoney(total)}
                    </span>
                  </div>
                  <p className="mt-1 text-xs text-slate-400">Final price is calculated by the server at checkout.</p>

                  {bookingError && <p className="mt-3 text-sm text-red-600 dark:text-red-400">{bookingError}</p>}

                  <Button
                    onClick={handleBook}
                    disabled={createBooking.isPending}
                    className="mt-4 w-full transition-all duration-200 hover:scale-[1.02] active:scale-95"
                  >
                    {createBooking.isPending ? 'Creating booking…' : 'Continue to payment'}
                  </Button>
                </>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </AppLayout>
  )
}
